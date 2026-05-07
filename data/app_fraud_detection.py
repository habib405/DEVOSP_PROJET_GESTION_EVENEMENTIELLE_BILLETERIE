"""
Application Streamlit - Détection de Fraude (Isolation Forest)
Objectif: Identifier les comportements suspects (bots, scalpers)
Variables: vitesse d'achat, volume, fréquence
Sorties: score d'anomalie, type de fraude (VELOCITY / VOLUME)
"""

import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path

# Configuration de la page
st.set_page_config(
    page_title="Détection de Fraude - Isolation Forest",
    page_icon="🚨",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Style
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (12, 6)

# ========================= CHARGEMENT DES DONNÉES =========================

@st.cache_data
def load_fraud_data():
    """Charge les données de fraude détectée"""
    fraud_path = Path(__file__).parent / "data" / "datasets" / "fraud_monitoring_from_isolation_forest.csv"
    return pd.read_csv(fraud_path)

@st.cache_data
def load_transactions_data():
    """Charge les données de transactions"""
    transactions_path = Path(__file__).parent / "data" / "datasets" / "transactions_50000.csv"
    df = pd.read_csv(transactions_path, parse_dates=["timestamp"])
    return df

# Chargement des données
fraud_df = load_fraud_data()
transactions_df = load_transactions_data()

# Fusion des données
merged_df = transactions_df.merge(
    fraud_df[["order_id", "score_anomalie", "type_fraude"]],
    on="order_id",
    how="left"
)

# ========================= ENTÊTE =========================
st.markdown("# 🚨 Détection de Fraude - Isolation Forest")
st.markdown("""
**Objectif**: Identifier les comportements suspects (bots, scalpers, acheteurs malveillants)
- **Modèle**: Isolation Forest
- **Variables**: Vitesse d'achat, Volume, Fréquence
- **Sorties**: Score d'anomalie + Type de fraude (VELOCITY/VOLUME)
""")

# ========================= MÉTRIQUES GLOBALES =========================
st.markdown("---")
col1, col2, col3, col4 = st.columns(4)

with col1:
    st.metric(
        label="Nombre total de transactions",
        value=f"{len(transactions_df):,}",
        delta=None
    )

with col2:
    n_fraud = fraud_df.shape[0]
    pct_fraud = (n_fraud / len(transactions_df)) * 100
    st.metric(
        label="Transactions suspectes",
        value=f"{n_fraud:,}",
        delta=f"{pct_fraud:.2f}% du total",
        delta_color="inverse"
    )

with col3:
    velocity_count = fraud_df[fraud_df["type_fraude"] == "VELOCITY"].shape[0]
    st.metric(
        label="Fraudes VELOCITY",
        value=f"{velocity_count:,}",
        delta="Achats rapides",
        delta_color="off"
    )

with col4:
    volume_count = fraud_df[fraud_df["type_fraude"] == "VOLUME"].shape[0]
    st.metric(
        label="Fraudes VOLUME",
        value=f"{volume_count:,}",
        delta="Volumes élevés",
        delta_color="off"
    )

# ========================= FILTRES ET SIDEBAR =========================
st.markdown("---")

with st.sidebar:
    st.markdown("## ⚙️ Filtres et Options")
    
    fraud_type = st.multiselect(
        "Type de fraude",
        options=["VELOCITY", "VOLUME"],
        default=["VELOCITY", "VOLUME"],
        key="fraud_type_filter"
    )
    
    score_range = st.slider(
        "Plage de score d'anomalie",
        min_value=float(fraud_df["score_anomalie"].min()),
        max_value=float(fraud_df["score_anomalie"].max()),
        value=(float(fraud_df["score_anomalie"].min()), float(fraud_df["score_anomalie"].max())),
        key="score_range"
    )
    
    st.markdown("### Statistiques")
    st.info(f"""
    - **Score min**: {fraud_df['score_anomalie'].min():.4f}
    - **Score max**: {fraud_df['score_anomalie'].max():.4f}
    - **Score moyen**: {fraud_df['score_anomalie'].mean():.4f}
    - **Score médian**: {fraud_df['score_anomalie'].median():.4f}
    """)

# Application des filtres
filtered_fraud = fraud_df[
    (fraud_df["type_fraude"].isin(fraud_type)) &
    (fraud_df["score_anomalie"] >= score_range[0]) &
    (fraud_df["score_anomalie"] <= score_range[1])
].copy()

# ========================= VISUALISATIONS =========================
st.markdown("## 📊 Analyse des Anomalies")

col_viz1, col_viz2 = st.columns(2)

# Graphique 1: Distribution des scores d'anomalie
with col_viz1:
    st.markdown("### Distribution des scores d'anomalie")
    fig, ax = plt.subplots(figsize=(10, 5))
    
    # Tous les scores
    ax.hist(fraud_df["score_anomalie"], bins=50, alpha=0.5, label="Tous", color="steelblue", edgecolor="black")
    
    # Scores filtrés
    ax.hist(filtered_fraud["score_anomalie"], bins=50, alpha=0.7, label="Filtrés", color="coral", edgecolor="black")
    
    ax.set_xlabel("Score d'anomalie")
    ax.set_ylabel("Fréquence")
    ax.set_title("Scores d'anomalie (Isolation Forest)")
    ax.legend()
    ax.grid(True, alpha=0.3)
    
    st.pyplot(fig)

# Graphique 2: Répartition par type de fraude
with col_viz2:
    st.markdown("### Répartition par type de fraude")
    fig, ax = plt.subplots(figsize=(10, 5))
    
    fraud_counts = filtered_fraud["type_fraude"].value_counts()
    colors = ["#FF6B6B", "#4ECDC4"]  # Rouge pour VELOCITY, Teal pour VOLUME
    
    wedges, texts, autotexts = ax.pie(
        fraud_counts.values,
        labels=fraud_counts.index,
        autopct='%1.1f%%',
        colors=colors,
        startangle=90,
        textprops={'fontsize': 12, 'weight': 'bold'}
    )
    
    ax.set_title(f"Types de fraude détectée (n={len(filtered_fraud)})")
    
    st.pyplot(fig)

# ========================= COMPARAISON VELOCITY vs VOLUME =========================
st.markdown("## 🔍 Analyse Détaillée par Type de Fraude")

col_comp1, col_comp2 = st.columns(2)

# VELOCITY
with col_comp1:
    st.markdown("### 🏃 Fraudes VELOCITY (Achats rapides)")
    velocity_data = filtered_fraud[filtered_fraud["type_fraude"] == "VELOCITY"]
    
    if len(velocity_data) > 0:
        st.metric(
            label="Nombre",
            value=len(velocity_data),
            delta=f"{(len(velocity_data)/len(filtered_fraud)*100):.1f}% du total" if len(filtered_fraud) > 0 else "0%"
        )
        
        col_v1, col_v2, col_v3 = st.columns(3)
        with col_v1:
            st.metric("Score moyen", f"{velocity_data['score_anomalie'].mean():.4f}")
        with col_v2:
            st.metric("Score min", f"{velocity_data['score_anomalie'].min():.4f}")
        with col_v3:
            st.metric("Score max", f"{velocity_data['score_anomalie'].max():.4f}")
    else:
        st.warning("Aucune fraude VELOCITY dans les filtres sélectionnés")

# VOLUME
with col_comp2:
    st.markdown("### 📦 Fraudes VOLUME (Volumes élevés)")
    volume_data = filtered_fraud[filtered_fraud["type_fraude"] == "VOLUME"]
    
    if len(volume_data) > 0:
        st.metric(
            label="Nombre",
            value=len(volume_data),
            delta=f"{(len(volume_data)/len(filtered_fraud)*100):.1f}% du total" if len(filtered_fraud) > 0 else "0%"
        )
        
        col_vo1, col_vo2, col_vo3 = st.columns(3)
        with col_vo1:
            st.metric("Score moyen", f"{volume_data['score_anomalie'].mean():.4f}")
        with col_vo2:
            st.metric("Score min", f"{volume_data['score_anomalie'].min():.4f}")
        with col_vo3:
            st.metric("Score max", f"{volume_data['score_anomalie'].max():.4f}")
    else:
        st.warning("Aucune fraude VOLUME dans les filtres sélectionnés")

# ========================= TABLEAU DÉTAILLÉ =========================
st.markdown("## 📋 Tableau des Transactions Suspectes")

# Préparation du tableau détaillé
display_df = filtered_fraud.copy()
display_df = display_df.merge(
    transactions_df[["order_id", "user_id", "timestamp", "montant", "nb_tickets"]],
    on="order_id",
    how="left"
)

# Réorganisation des colonnes
display_df = display_df[[
    "id", "order_id", "user_id", "timestamp", 
    "montant", "nb_tickets", "score_anomalie", "type_fraude"
]]

# Formatage
display_df["score_anomalie"] = display_df["score_anomalie"].apply(lambda x: f"{x:.4f}")
display_df["montant"] = display_df["montant"].apply(lambda x: f"${x:.2f}")

# Affichage du tableau avec pagination
st.dataframe(
    display_df.sort_values("score_anomalie", ascending=False),
    use_container_width=True,
    height=400
)

# Option de téléchargement
csv_data = display_df.to_csv(index=False)
st.download_button(
    label="📥 Télécharger les données filtrées (CSV)",
    data=csv_data,
    file_name="fraud_detection_results.csv",
    mime="text/csv"
)

# ========================= STATISTIQUES AVANCÉES =========================
st.markdown("---")
st.markdown("## 📈 Statistiques Avancées")

col_stat1, col_stat2 = st.columns(2)

with col_stat1:
    st.markdown("### Score d'anomalie par type")
    
    fig, ax = plt.subplots(figsize=(10, 5))
    
    velocity_scores = filtered_fraud[filtered_fraud["type_fraude"] == "VELOCITY"]["score_anomalie"]
    volume_scores = filtered_fraud[filtered_fraud["type_fraude"] == "VOLUME"]["score_anomalie"]
    
    data_to_plot = [velocity_scores, volume_scores]
    
    bp = ax.boxplot(data_to_plot, labels=["VELOCITY", "VOLUME"], patch_artist=True)
    
    # Couleurs
    colors = ["#FF6B6B", "#4ECDC4"]
    for patch, color in zip(bp['boxes'], colors):
        patch.set_facecolor(color)
        patch.set_alpha(0.7)
    
    ax.set_ylabel("Score d'anomalie")
    ax.set_title("Distribution des scores par type de fraude")
    ax.grid(True, alpha=0.3, axis='y')
    
    st.pyplot(fig)

with col_stat2:
    st.markdown("### Résumé statistique")
    
    summary_stats = fraud_df.groupby("type_fraude")["score_anomalie"].describe()
    st.dataframe(summary_stats.round(4), use_container_width=True)

# ========================= PIED DE PAGE =========================
st.markdown("---")
st.markdown("""
### 📚 Informations sur le Modèle
- **Algorithme**: Isolation Forest (Scikit-learn)
- **Contamination**: ~5% d'anomalies attendues
- **Features utilisées**: 
  - Nombre de commandes par utilisateur
  - Montant total et moyen
  - Nombre de tickets total et moyen
  - Delta temporel moyen, min, 25e et 75e percentile
  - Nombre et ratio de commandes rapides (< 10 secondes)

**Types de fraude**:
- 🏃 **VELOCITY**: Comportement suspect - achats rapides successifs (bots, scalpers)
- 📦 **VOLUME**: Comportement suspect - volumes anormalement élevés

**Score d'anomalie**: Plus la valeur est élevée, plus le comportement est suspect (0 = normal, 1 = très suspect)
""")

st.markdown("""
*Application créée pour l'analyse de détection de fraude sur les transactions e-commerce*
""")
