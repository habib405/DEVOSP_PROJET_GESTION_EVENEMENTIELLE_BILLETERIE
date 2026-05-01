package com.projet.billeterie.back.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.projet.billeterie.back.entity.Order;
import com.projet.billeterie.back.entity.TicketType;
import com.projet.billeterie.back.entity.User;
import com.projet.billeterie.back.exception.ResourceNotFoundException;
import com.projet.billeterie.back.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private static final Color GOLD = new Color(201, 168, 76);
    private static final Color DARK = new Color(34, 34, 34);

    private final TicketTypeRepository ticketTypeRepository;

    /**
     * Generates a PDF invoice and returns its raw bytes.
     */
    public byte[] generate(Order order, User buyer, List<UUID> ticketTypeIds) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            document.open();

            // ── Header ─────────────────────────────────────────────────────
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, GOLD);
            Paragraph title = new Paragraph("EventPlatform", titleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            document.add(title);

            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            document.add(new Paragraph("Plateforme de billetterie événementielle", subtitleFont));
            document.add(new Paragraph(" "));

            Font invoiceLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, DARK);
            document.add(new Paragraph("FACTURE / INVOICE", invoiceLabelFont));
            document.add(new Paragraph(" "));

            // ── Invoice info table ─────────────────────────────────────────
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1f, 1f});

            addInfoBlock(infoTable, "FACTURÉ À",
                    buyer.getFirstName() + " " + buyer.getLastName() + "\n" + buyer.getEmail());
            addInfoBlock(infoTable, "DÉTAILS",
                    "N° Facture  : " + shortId(order.getId()) + "\n" +
                    "N° Commande : " + order.getId() + "\n" +
                    "Date        : " + (order.getCreatedAt() != null ? order.getCreatedAt().format(df) : ""));

            document.add(infoTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // ── Items table ────────────────────────────────────────────────
            PdfPTable items = new PdfPTable(new float[]{4f, 1f, 1.5f, 1.5f});
            items.setWidthPercentage(100);

            addHeaderCell(items, "Description");
            addHeaderCell(items, "Qté");
            addHeaderCell(items, "Prix unit.");
            addHeaderCell(items, "Total");

            // Group line items
            Map<UUID, Long> grouped = ticketTypeIds.stream()
                    .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

            double computedTotal = 0;
            for (Map.Entry<UUID, Long> e : grouped.entrySet()) {
                TicketType tt = ticketTypeRepository.findById(e.getKey())
                        .orElseThrow(() -> new ResourceNotFoundException("TicketType not found: " + e.getKey()));
                long qty = e.getValue();
                double unit = tt.getPrice();
                double line = unit * qty;
                computedTotal += line;

                String desc = tt.getEvent().getTitle() + " — " + tt.getName();
                addCell(items, desc, Element.ALIGN_LEFT);
                addCell(items, String.valueOf(qty), Element.ALIGN_CENTER);
                addCell(items, String.format("%.2f €", unit), Element.ALIGN_RIGHT);
                addCell(items, String.format("%.2f €", line), Element.ALIGN_RIGHT);
            }

            // Total row
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, DARK);
            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL TTC", totalFont));
            totalLabel.setColspan(3);
            totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalLabel.setPadding(8);
            totalLabel.setBackgroundColor(new Color(245, 245, 245));
            items.addCell(totalLabel);

            PdfPCell totalValue = new PdfPCell(new Phrase(
                    String.format("%.2f €", order.getTotalAmount() != null ? order.getTotalAmount() : computedTotal),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, GOLD)));
            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalValue.setPadding(8);
            totalValue.setBackgroundColor(new Color(245, 245, 245));
            items.addCell(totalValue);

            document.add(items);

            // ── Footer ─────────────────────────────────────────────────────
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
            document.add(new Paragraph("Paiement effectué via Stripe — statut : " + order.getStatus(), footerFont));
            document.add(new Paragraph("Merci pour votre achat. Vos billets et QR codes sont envoyés par email.", footerFont));

            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            log.error("Failed to generate invoice PDF: {}", ex.getMessage(), ex);
            throw new RuntimeException("Invoice generation failed", ex);
        }
    }

    private static String shortId(UUID id) {
        return "INV-" + id.toString().substring(0, 8).toUpperCase();
    }

    private static void addInfoBlock(PdfPTable table, String label, String value) {
        Font lf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, GOLD);
        Font vf = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK);

        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPaddingBottom(6);
        cell.addElement(new Paragraph(label, lf));
        for (String line : value.split("\n")) {
            cell.addElement(new Paragraph(line, vf));
        }
        table.addCell(cell);
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setBackgroundColor(DARK);
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setBorderColor(DARK);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String text, int align) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setPadding(8);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(new Color(220, 220, 220));
        table.addCell(cell);
    }
}
