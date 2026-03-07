package com.projet.gestion.evenementielle.entity;

public enum OrderStatus {
    PENDING,         // Commande créée
    LOCKED,          // Verrouillée (stock réservé)
    PAYMENT_PENDING, // En attente de paiement
    CONFIRMED,       // Paiement validé
    CANCELLED,
    REFUNDED
}
