package com.projet.billeterie.back.config;

import com.projet.billeterie.back.entity.*;
import com.projet.billeterie.back.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Peuplement (seeding) initial de la base.
 * Ne fait rien si la table users est déjà peuplée.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.seed.default-password:Password123!}")
    private String defaultPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Seeding disabled (app.seed.enabled=false).");
            return;
        }
        if (userRepository.count() > 0) {
            log.info("Seeding skipped — DB already populated ({} users).", userRepository.count());
            return;
        }

        log.info("=== Starting database peuplement ===");
        seed();
        log.info("=== Database peuplement complete ===");
    }

    private void seed() {
        // ── Users ────────────────────────────────────────────────────────────
        String pwd = passwordEncoder.encode(defaultPassword);

        User admin = userRepository.save(User.builder()
                .firstName("Alice").lastName("Admin")
                .email("admin@eventplatform.com")
                .passwordHash(pwd).role(UserRole.ADMIN).build());

        User organizer = userRepository.save(User.builder()
                .firstName("Olivier").lastName("Organizer")
                .email("organizer@eventplatform.com")
                .passwordHash(pwd).role(UserRole.ORGANIZER).build());

        User staff = userRepository.save(User.builder()
                .firstName("Sam").lastName("Staff")
                .email("staff@eventplatform.com")
                .passwordHash(pwd).role(UserRole.STAFF).build());

        User attendee = userRepository.save(User.builder()
                .firstName("Mehdi").lastName("Mehdi")
                .email("mehdikarkache@gmail.com")
                .passwordHash(pwd).role(UserRole.ATTENDEE).build());

        User attendee2 = userRepository.save(User.builder()
                .firstName("Mehdi").lastName("Karkache")
                .email("attendee@eventplatform.com")
                .passwordHash(pwd).role(UserRole.ATTENDEE).build());

        log.info("Seeded {} users.", userRepository.count());

        // ── Venues ───────────────────────────────────────────────────────────
        Venue palaisCorse = venueRepository.save(Venue.builder()
                .name("Palais des Congrès d'Ajaccio")
                .address("Quai l'Herminier")
                .city("Ajaccio")
                .maxCapacity(800).build());

        Venue theatreBastia = venueRepository.save(Venue.builder()
                .name("Théâtre Municipal de Bastia")
                .address("Rue Favalelli")
                .city("Bastia")
                .maxCapacity(450).build());

        Venue arenaCorte = venueRepository.save(Venue.builder()
                .name("Arena Universitaria")
                .address("Campus Mariani")
                .city("Corte")
                .maxCapacity(1200).build());

        log.info("Seeded {} venues.", venueRepository.count());

        // ── Events + Ticket Types ────────────────────────────────────────────
        LocalDateTime now = LocalDateTime.now();

        Event tech = eventRepository.save(Event.builder()
                .title("DevOps Summit 2026")
                .description("Une journée dédiée aux pratiques DevOps : CI/CD, conteneurs, observabilité, SRE. Conférences, workshops et networking.")
                .startDate(now.plusDays(30).withHour(9).withMinute(0))
                .endDate(now.plusDays(30).withHour(18).withMinute(0))
                .maxCapacity(500)
                .currentAttendees(0)
                .status(EventStatus.PUBLISHED)
                .organizerId(organizer.getId())
                .venue(palaisCorse).build());

        ticketTypeRepository.saveAll(List.of(
                TicketType.builder().event(tech).name("Standard").price(49.00f).totalQuantity(300).soldQuantity(0).build(),
                TicketType.builder().event(tech).name("VIP").price(149.00f).totalQuantity(100).soldQuantity(0).build(),
                TicketType.builder().event(tech).name("Étudiant").price(15.00f).totalQuantity(100).soldQuantity(0).build()
        ));

        Event concert = eventRepository.save(Event.builder()
                .title("I Muvrini — Concert Acoustique")
                .description("Concert exceptionnel du groupe corse I Muvrini en formation acoustique. Une soirée unique au cœur de Bastia.")
                .startDate(now.plusDays(45).withHour(20).withMinute(30))
                .endDate(now.plusDays(45).withHour(23).withMinute(0))
                .maxCapacity(400)
                .currentAttendees(0)
                .status(EventStatus.PUBLISHED)
                .organizerId(organizer.getId())
                .venue(theatreBastia).build());

        ticketTypeRepository.saveAll(List.of(
                TicketType.builder().event(concert).name("Catégorie 2").price(35.00f).totalQuantity(250).soldQuantity(0).build(),
                TicketType.builder().event(concert).name("Catégorie 1").price(55.00f).totalQuantity(120).soldQuantity(0).build(),
                TicketType.builder().event(concert).name("Carré Or").price(95.00f).totalQuantity(30).soldQuantity(0).build()
        ));

        Event sport = eventRepository.save(Event.builder()
                .title("Marathon de Corte")
                .description("Marathon annuel à travers les montagnes de Corte. Trois parcours : 10km, semi-marathon, marathon complet.")
                .startDate(now.plusDays(60).withHour(8).withMinute(0))
                .endDate(now.plusDays(60).withHour(16).withMinute(0))
                .maxCapacity(1000)
                .currentAttendees(0)
                .status(EventStatus.PUBLISHED)
                .organizerId(organizer.getId())
                .venue(arenaCorte).build());

        ticketTypeRepository.saveAll(List.of(
                TicketType.builder().event(sport).name("10 km").price(20.00f).totalQuantity(400).soldQuantity(0).build(),
                TicketType.builder().event(sport).name("Semi-marathon").price(35.00f).totalQuantity(350).soldQuantity(0).build(),
                TicketType.builder().event(sport).name("Marathon").price(50.00f).totalQuantity(250).soldQuantity(0).build()
        ));

        // Un événement DRAFT pour montrer le filtre PUBLISHED
        Event draft = eventRepository.save(Event.builder()
                .title("Workshop Cybersécurité (à venir)")
                .description("Atelier hands-on sur la sécurité applicative.")
                .startDate(now.plusDays(90).withHour(10).withMinute(0))
                .endDate(now.plusDays(90).withHour(17).withMinute(0))
                .maxCapacity(80)
                .currentAttendees(0)
                .status(EventStatus.DRAFT)
                .organizerId(organizer.getId())
                .venue(palaisCorse).build());

        ticketTypeRepository.save(
                TicketType.builder().event(draft).name("Pass Workshop").price(80.00f).totalQuantity(80).soldQuantity(0).build()
        );

        log.info("Seeded {} events / {} ticket types.",
                eventRepository.count(), ticketTypeRepository.count());

        log.info("Default credentials (password = '{}'):", defaultPassword);
        log.info("  ADMIN     : admin@eventplatform.com");
        log.info("  ORGANIZER : organizer@eventplatform.com");
        log.info("  STAFF     : staff@eventplatform.com");
        log.info("  ATTENDEE  : mehdikarkache@gmail.com");
        log.info("  ATTENDEE  : attendee@eventplatform.com");
    }
}
