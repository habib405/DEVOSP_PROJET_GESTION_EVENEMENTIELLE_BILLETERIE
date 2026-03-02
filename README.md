# 🎟️ Plateforme de Gestion Événementielle et Billetterie

Ce projet est une application complète (Full-Stack) de gestion d'événements et de billetterie en ligne, développée dans le cadre de notre cursus de Master 1 DFS_DE à l'Université de Corse.

Le projet met l'accent sur les bonnes pratiques de développement logiciel : architecture distribuée, conteneurisation (Docker), sécurité (JWT) et méthodologie Agile (Jira/Git Workflow).

---

## 🏗️ Architecture du Projet (Monorepo)

Le dépôt est structuré de la manière suivante pour séparer clairement les responsabilités :

* 📁 **`back/`** : API REST développée en Java avec Spring Boot 3 et Spring Security (JWT).
* 📁 **`front/`** : Interface utilisateur (développement en cours).
* 📁 **`data/`** : Scripts d'initialisation et configuration de la base de données PostgreSQL.
* 📄 **`docker-compose.yml`** : Fichier d'orchestration pour lancer l'ensemble de l'environnement (Base de données, Backend, Frontend) en une seule commande.

---

## 👥 L'Équipe du Projet

Projet collaboratif réalisé par :
* **Mouhamadou Habib DIAO**
* **Mehdi**
* **Abdoul**
* **Yousra**

---

## 🚀 Démarrage Rapide (Environnement Docker)

L'application est entièrement conteneurisée. Vous n'avez pas besoin d'installer Java ou une base de données sur votre machine locale.

**Prérequis :** Avoir [Docker](https://www.docker.com/) et Docker Compose installés.

1. Clonez le dépôt :
   ```bash
   git clone [https://github.com/habib405/DEVOSP_PROJET_GESTION_EVENEMENTIELLE_BILLETERIE.git](https://github.com/habib405/DEVOSP_PROJET_GESTION_EVENEMENTIELLE_BILLETERIE.git)
   cd DEVOSP_PROJET_GESTION_EVENEMENTIELLE_BILLETERIE
