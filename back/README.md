# Gestion Événementielle — Backend

Plateforme de gestion événementielle et billetterie — Spring Boot 4 / Java 21 / PostgreSQL.

## Stack technique

- **Framework** : Spring Boot 4.0.3
- **Java** : 21
- **Base de données** : PostgreSQL (port 5430)
- **Sécurité** : JWT (HMAC-SHA256)
- **Email** : SMTP Gmail (Spring Mail)
- **QR Code** : ZXing 3.5.3
- **Build** : Maven

## Lancer le projet

1. Copier `application.properties` et renseigner les variables d'environnement (DB, JWT, SMTP)
2. Démarrer PostgreSQL (ou utiliser `docker compose up`)
3. `./mvnw spring-boot:run`

## Variables d'environnement requises

| Variable | Description |
|---|---|
| `DB_USERNAME` | Utilisateur PostgreSQL |
| `DB_PASSWORD` | Mot de passe PostgreSQL |
| `JWT_SECRET` | Clé secrète JWT (base64, 64+ chars) |
| `MAIL_USERNAME` | Adresse Gmail |
| `MAIL_PASSWORD` | App password Gmail |
