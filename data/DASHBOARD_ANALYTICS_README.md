# 📊 Dashboard Analytique Complet - Documentation

## 🎯 Vue d'ensemble

Application Streamlit multi-onglets pour l'analyse ML de fraude, segmentation des utilisateurs et prédiction de sold-out.

### Trois analyses principales

#### 1️⃣ **Détection de Fraude (Isolation Forest)**
- **Objectif** : Identifier les comportements suspects (bots, scalpers)
- **Modèle** : Isolation Forest
- **Variables** : Vitesse d'achat, Volume, Fréquence
- **Sorties** : Score d'anomalie (0-1) + Type de fraude (VELOCITY/VOLUME)
- **Dataset** : data/datasets/fraud_monitoring_from_isolation_forest.csv

#### 2️⃣ **Segmentation des Utilisateurs (K-Means)**
- **Objectif** : Identifier les profils d'acheteurs pour le marketing ciblé
- **Modèle** : K-Means Clustering
- **Variables** : Panier moyen, Fréquence d'achat, Vitesse d'achat
- **Sorties** : Groupes d'utilisateurs (clusters) avec caractéristiques
- **Dataset** : data/datasets/user_segmentation.csv

#### 3️⃣ **Prédiction Sold-Out (Lecture CSV / Régression)**
- **Objectif** : Estimer le temps avant rupture de stock pour un événement
- **Approche** : L'onglet lit les prédictions pré-calculées depuis `data/datasets/soldout_prediction.csv` (colonne `pred_time_to_soldout_min`). L'entraînement en live n'est pas activé par défaut mais peut être intégré si nécessaire.
- **Variables clés** : `event_id`, `timestamp`, `tickets_cumules`, `velocity_tpm`, `pred_time_to_soldout_min`, `soldout_time_pred`
- **Dataset** : data/datasets/soldout_prediction.csv

---

## 🚀 Installation et Lancement

### 1. Prérequis
```bash
Python 3.8+
pip install -r requirements.txt
```

### 2. Lancer le Dashboard

Si vous utilisez l'environnement virtuel local `.venv` (recommandé) :

```powershell
.\.venv\Scripts\pip install -r requirements.txt
.\.venv\Scripts\streamlit run app_analytics_dashboard.py
```

Ou, avec l'interpréteur système :

```bash
streamlit run app_analytics_dashboard.py
```

L'application s'ouvrira sur `http://localhost:8501`.

---

## 📋 Fonctionnalités par Onglet

### Onglet 1️⃣ : Détection de Fraude

- Métriques Globales : total transactions, nombre/% suspectes, compteurs VELOCITY vs VOLUME
- Filtres : type de fraude, plage de score
- Visualisations : histogramme des scores, camembert par type, boxplot, stats descriptives
- Données détaillées : `order_id`, `user_id`, `score_anomalie`, `type_fraude`, `montant`, `nb_tickets`, `timestamp`
- Export : CSV des résultats filtrés

### Onglet 2️⃣ : Segmentation des Utilisateurs

- Métriques Globales : nombre d'utilisateurs, nombre de clusters, panier moyen
- Filtres : clusters, nombre min de commandes, montant max
- Visualisations : distribution des clusters, caractéristiques moyennes, scatter plots
- Données détaillées : `user_id`, `cluster`, `nb_commandes`, `montant_total`, `montant_moyen`, `nb_tickets_moyen`, `delta_moyen`
- Export : CSV des résultats filtrés

### Onglet 3️⃣ : Prédiction Sold-Out

- Métriques & Visualisations : temps moyen prédit avant rupture (`pred_time_to_soldout_min`), histogramme, tableau par événement
- Filtres : événement, plage temporelle
- Données détaillées : `event_id`, `timestamp`, `tickets_cumules`, `velocity_tpm`, `pred_time_to_soldout_min`, `soldout_time_pred`
- Export : CSV des prédictions filtrées

Remarque : l'onglet utilise le CSV `soldout_prediction.csv` comme source. Un flux de réentraînement depuis des données brutes peut être ajouté (bouton `Train`) si vous souhaitez générer de nouvelles prédictions en local.

---

## 📊 Interprétation des Résultats (courte)

- Score d'anomalie : 0.0–0.3 normal, 0.3–0.7 suspect, 0.7–1.0 très suspect
- VELOCITY = achats rapides (bots); VOLUME = volumes élevés (scalpers)
- Clusters : profils marketing (occasionnels, réguliers, gros clients, premium, etc.)

---

## 🔧 Configuration Technique

### Dépendances (extrait depuis `requirements.txt`)
```
pandas
numpy
scikit-learn
matplotlib
seaborn
streamlit
plotly (optionnel)
```

### Architecture (fichiers principaux)
```
app_analytics_dashboard.py
├── Chargement des données (cache)
├── Onglet Fraude
├── Onglet Segmentation
└── Onglet Sold-Out
```

### Fichiers de Données
```
data/datasets/
├── fraud_monitoring_from_isolation_forest.csv
├── user_segmentation.csv
├── transactions_50000.csv
└── soldout_prediction.csv  # Colonnes: event_id, timestamp, tickets_cumules, velocity_tpm, pred_time_to_soldout_min, soldout_time_pred
```

---

## 🐛 Dépannage

### Erreur : "FileNotFoundError: ..."
- Vérifier que les CSV existent sous `data/datasets/` et que les chemins sont relatifs au projet.

### Erreur : "ModuleNotFoundError: No module named 'streamlit'"
```bash
pip install -r requirements.txt
```

### Tableau vide
- Vérifier le chargement des données et les clés de fusion (`order_id`, `user_id`).
- Consulter les logs du serveur Streamlit pour erreurs.

### Avertissements de dépréciation Streamlit
- Vous pourriez voir des messages : "Please replace `use_container_width` with `width`".
- Solution : remplacer `st.dataframe(..., use_container_width=True)` par `st.dataframe(..., width='stretch')` dans `app_analytics_dashboard.py` pour supprimer les warnings.

---

## 📈 Cas d'Usage (rapide)

- Analyste Fraude : filtrer par VELOCITY, exporter les suspects
- Marketing : segmenter par cluster et planifier campagnes
- Ops : surveiller les prédictions de sold-out et ajuster la capacité

---

## 💡 Améliorations Futures

- Intégration PostgreSQL pour ingestion temps-réel
- Alertes automatiques pour fraudes critiques
- Réentraînement ML en continu et interface `Train` dans l'app
- API REST et dashboard temps-réel avec Plotly

---

*Dashboard créé pour l'analyse complète des transactions e-commerce*
*Dernière mise à jour : Mai 2026*
