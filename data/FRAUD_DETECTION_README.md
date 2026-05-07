# 🚨 Application Streamlit - Détection de Fraude (Isolation Forest)

## 📋 Description

Application interactive de détection de fraude utilisant l'algorithme **Isolation Forest**. Elle analyse les comportements suspects dans les transactions e-commerce pour identifier les bots, scalpers et autres activités malveillantes.

## 🎯 Objectifs

- **Identifier les comportements suspects** : bots, scalpers, acheteurs malveillants
- **Détecter deux types de fraude** :
  - 🏃 **VELOCITY** : Achats rapides et successifs (delta < 10 secondes)
  - 📦 **VOLUME** : Volumes anormalement élevés
- **Fournir des scores d'anomalie** : mesure du degré de suspicion (0=normal, 1=très suspect)

## 📊 Features Utilisées

Le modèle Isolation Forest utilise les variables suivantes :

| Variable | Description |
|----------|-------------|
| `nb_commandes` | Nombre total de commandes par utilisateur |
| `montant_total` | Montant total dépensé |
| `montant_moyen` | Montant moyen par commande |
| `nb_tickets_total` | Nombre total de tickets achetés |
| `nb_tickets_moyen` | Nombre moyen de tickets par commande |
| `delta_moyen` | Délai moyen entre deux commandes (en secondes) |
| `delta_min` | Délai minimum entre deux commandes |
| `delta_25` / `delta_75` | 25e et 75e percentiles des délais |
| `nb_rapid` | Nombre de commandes "rapides" (< 10s) |
| `ratio_rapid` | Ratio de commandes rapides |

## 🚀 Installation et Lancement

### 1. Prérequis
- Python 3.8+
- pip ou conda

### 2. Installation des dépendances

```bash
pip install -r requirements.txt
```

### 3. Lancement de l'application

```bash
streamlit run app_fraud_detection.py
```

L'application s'ouvrira automatiquement dans votre navigateur (par défaut: `http://localhost:8501`)

## 📁 Structure des données

```
data/
├── datasets/
│   ├── transactions_50000.csv          # Données brutes de transactions
│   ├── fraud_monitoring_from_isolation_forest.csv  # Résultats détectés
│   └── ...
└── notebooks/
    └── isolation_forest_fraud_detection.ipynb  # Notebook d'analyse
```

### Format du fichier de fraude

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | UUID | Identifiant unique de la détection |
| `order_id` | UUID | Identifiant de la commande |
| `score_anomalie` | Float | Score d'anomalie (0-1, plus élevé = plus suspect) |
| `type_fraude` | String | Type détecté : VELOCITY ou VOLUME |

## 🎨 Interface Streamlit

### 1. Métriques Globales
- Total des transactions
- Nombre et % de transactions suspectes
- Compte par type de fraude

### 2. Filtres (Sidebar)
- Sélection du type de fraude (VELOCITY/VOLUME)
- Plage de scores d'anomalie
- Statistiques récapitulatives

### 3. Visualisations
- **Histogramme** : Distribution des scores d'anomalie
- **Camembert** : Répartition des types de fraude
- **Boxplot** : Comparaison des scores par type
- **Tableau** : Liste détaillée des transactions suspectes

### 4. Export de données
- Téléchargement CSV des résultats filtrés

## 🔧 Paramètres du Modèle

Le modèle Isolation Forest dans le notebook utilise :

```python
IsolationForest(
    n_estimators=200,      # Nombre d'arbres
    contamination=0.05,    # ~5% d'anomalies attendues
    random_state=42
)
```

### Seuils de Classification

- **VELOCITY** : `ratio_rapid >= 90e percentile`
- **VOLUME** : `montant_total >= 90e percentile`

## 📈 Interprétation des Résultats

### Score d'Anomalie
- Score proche de **0** : Comportement normal
- Score proche de **0.5** : Comportement légèrement suspect
- Score proche de **1** : Comportement très suspect

### Type de Fraude

**VELOCITY** - Achats rapides
- Caractéristiques typiques : succession d'achats rapides (< 10 secondes)
- Cas d'usage : détection de bots, scripts automatisés

**VOLUME** - Volumes élevés
- Caractéristiques typiques : montants très importants ou nombre de tickets exceptionnels
- Cas d'usage : détection de scalpers, reventes massives

## 🔍 Cas d'Usage

1. **Monitoring en temps réel** : Affichage du dashboard pour surveiller les fraudes
2. **Investigation** : Filtrer par type et analyser les patterns
3. **Export de données** : Télécharger les résultats pour traitement externe
4. **Statistiques** : Analyser les distributions et tendances

## 💡 Améliorations Possibles

- Intégration avec une base de données PostgreSQL
- Alertes en temps réel
- Machine Learning online : réentraînement continu
- API REST pour intégration backend
- Visualisations temps réel avec Plotly
- Classement par sévérité
- Export Parquet pour Big Data

## 📝 Exemple d'Utilisation

```bash
# Lancer Streamlit
streamlit run app_fraud_detection.py

# Dans l'interface :
# 1. Ouvrir la sidebar (Filtres et Options)
# 2. Sélectionner les types de fraude
# 3. Ajuster la plage de scores
# 4. Observer les graphiques et tableaux
# 5. Télécharger les données si besoin
```

## 🐛 Dépannage

### "ModuleNotFoundError: No module named 'streamlit'"
```bash
pip install streamlit
```

### "FileNotFoundError: data/datasets/fraud_monitoring_from_isolation_forest.csv"
- Vérifier que le fichier existe au chemin relatif
- Vérifier les chemins dans le script (relative au répertoire racine)

### Données vides dans le tableau
- Vérifier que `transactions_50000.csv` est présent
- Vérifier que les colonnes `order_id` correspondent entre les fichiers

## 📚 Références

- [Isolation Forest - Scikit-learn](https://scikit-learn.org/stable/modules/generated/sklearn.ensemble.IsolationForest.html)
- [Streamlit Documentation](https://docs.streamlit.io/)
- [Pandas Documentation](https://pandas.pydata.org/docs/)
- [Matplotlib Documentation](https://matplotlib.org/stable/contents.html)

## 📞 Support

Pour toute question ou problème, consultez :
- Le notebook `isolation_forest_fraud_detection.ipynb` pour comprendre le modèle
- Les fichiers CSV pour vérifier les données
- La documentation Streamlit pour les customisations UI

---
*Créée pour l'analyse de détection de fraude sur les transactions e-commerce*
