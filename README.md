# 🎟️ Plateforme de Gestion Événementielle et Billetterie

Ce projet est une application complète (Full-Stack) de gestion d'événements et de billetterie en ligne, développée dans le cadre de notre cursus de Master 1 DFS_DE à l'Université de Corse.

Le projet met l'accent sur les bonnes pratiques de développement logiciel et l'approche DevOps / SRE : architecture distribuée, conteneurisation (Docker), orchestration (Kubernetes), sécurité (JWT) et automatisation CI/CD.

---

## 🏗️ Architecture du Projet (Monorepo)

Le dépôt est structuré de la manière suivante pour séparer clairement les responsabilités :

* 📁 **`back/`** : API REST développée en Java avec Spring Boot 3 et Spring Security (JWT).
* 📁 **`Frontend/`** : Interface utilisateur React/Vite.
* 📁 **`data/`** : Scripts d'initialisation et configuration de la base de données PostgreSQL.
* 📁 **`k8s/`** : Manifestes Kubernetes pour le déploiement de l'infrastructure de production.
* 📁 **`.github/workflows/`** : Pipelines CI/CD automatisés avec GitHub Actions.
* 📄 **`docker-compose.yml`** : Fichier d'orchestration pour le développement local.

---

## 👥 L'Équipe du Projet

Projet collaboratif réalisé par :
* **Mouhamadou Habib DIAO**
* **Mehdi**
* **Abdoul**
* **Yousra**

---

## 🌐 Environnement de Production (AWS & Kubernetes)

L'application est déployée automatiquement via un pipeline CI/CD sur une instance AWS EC2 utilisant un cluster Kubernetes (K3s). 

* **Déclenchement :** Tout push sur la branche `main` déclenche le build des images sur Docker Hub et la mise à jour des Pods.
* **Accès au site en direct :** `http://16.16.196.41:30000`

---

## 🚀 Démarrage Rapide (Développement Local)

Pour travailler sur le projet en local, l'application est entièrement conteneurisée. Vous n'avez pas besoin d'installer Java ou une base de données sur votre machine.

**Prérequis :** Avoir [Docker](https://www.docker.com/) et Docker Compose installés.

1. Clonez le dépôt :
   ```bash
   git clone [https://github.com/habib405/DEVOSP_PROJET_GESTION_EVENEMENTIELLE_BILLETERIE.git](https://github.com/habib405/DEVOSP_PROJET_GESTION_EVENEMENTIELLE_BILLETERIE.git)
   cd DEVOSP_PROJET_GESTION_EVENEMENTIELLE_BILLETERIE