"""
Application Streamlit - Dashboard d'Analyse Complète
Analyse 1 - Détection de Fraude (Isolation Forest)
Analyse 2 - Segmentation des Utilisateurs (K-Means)
Analyse 3 - Prédiction de Sold-Out (Régression Logistique)
"""

import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from pathlib import Path
from sklearn.preprocessing import StandardScaler
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score

# ========================= CONFIGURATION =========================
st.set_page_config(
    page_title="Dashboard Analytique - Fraude, Segmentation & Sold-Out",
    page_icon="📊",
    layout="wide",
    initial_sidebar_state="expanded"
)

# Style global
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (12, 6)
plt.rcParams['font.size'] = 10

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

@st.cache_data
def load_segmentation_data():
    """Charge les données de segmentation des utilisateurs"""
    seg_path = Path(__file__).parent / "data" / "datasets" / "user_segmentation.csv"
    return pd.read_csv(seg_path)

@st.cache_data
def load_soldout_data():
    """Charge les données de prédiction de sold-out"""
    soldout_path = Path(__file__).parent / "data" / "datasets" / "soldout_prediction.csv"
    df = pd.read_csv(soldout_path, parse_dates=["timestamp", "soldout_time_pred"])
    return df

# Chargement des données
fraud_df = load_fraud_data()
transactions_df = load_transactions_data()
segmentation_df = load_segmentation_data()
soldout_df = load_soldout_data()

# Fusion des données pour fraude
merged_fraud_df = transactions_df.merge(
    fraud_df[["order_id", "score_anomalie", "type_fraude"]],
    on="order_id",
    how="left"
)

# ========================= ENTÊTE =========================
st.markdown("# 📊 Dashboard Analytique")
st.markdown("**Analyses ML : Détection de fraude, Segmentation des clients, Prédiction de sold-out**")

# ========================= ONGLETS PRINCIPALES =========================
tab1, tab2, tab3 = st.tabs([
    "🚨 Détection de Fraude", 
    "👥 Segmentation des Utilisateurs",
    "📦 Prédiction Sold-Out"
])

# (En-tête et onglets définis plus haut)

# =====================================================================
# TAB 1 - DÉTECTION DE FRAUDE
# =====================================================================
with tab1:
    st.header("🚨 Détection de Fraude (Isolation Forest)")
    st.markdown("""
    **Objectif**: Identifier les comportements suspects (bots, scalpers, acheteurs malveillants)
    - **Modèle**: Isolation Forest
    - **Variables**: Vitesse d'achat, Volume, Fréquence
    - **Sorties**: Score d'anomalie + Type de fraude (VELOCITY/VOLUME)
    """)
    
    # Métriques globales
    st.markdown("---")
    col1, col2, col3, col4 = st.columns(4)
    
    with col1:
        st.metric(
            label="Total Transactions",
            value=f"{len(transactions_df):,}",
            delta=None
        )
    
    with col2:
        n_fraud = fraud_df.shape[0]
        pct_fraud = (n_fraud / len(transactions_df)) * 100
        st.metric(
            label="Transactions Suspectes",
            value=f"{n_fraud:,}",
            delta=f"{pct_fraud:.2f}%",
            delta_color="inverse"
        )
    
    with col3:
        velocity_count = fraud_df[fraud_df["type_fraude"] == "VELOCITY"].shape[0]
        st.metric(
            label="VELOCITY Fraud",
            value=f"{velocity_count:,}",
            delta="Achats rapides"
        )
    
    with col4:
        volume_count = fraud_df[fraud_df["type_fraude"] == "VOLUME"].shape[0]
        st.metric(
            label="VOLUME Fraud",
            value=f"{volume_count:,}",
            delta="Volumes élevés"
        )
    
    # Filtres
    st.markdown("---")
    st.markdown("### Filtres")
    
    fraud_col1, fraud_col2 = st.columns(2)
    
    with fraud_col1:
        fraud_type_filter = st.multiselect(
            "Type de fraude",
            options=["VELOCITY", "VOLUME"],
            default=["VELOCITY", "VOLUME"],
            key="fraud_type_filter"
        )
    
    with fraud_col2:
        score_range = st.slider(
            "Plage de score d'anomalie",
            min_value=float(fraud_df["score_anomalie"].min()),
            max_value=float(fraud_df["score_anomalie"].max()),
            value=(float(fraud_df["score_anomalie"].min()), float(fraud_df["score_anomalie"].max())),
            key="score_range"
        )
    
    # Application des filtres
    filtered_fraud = fraud_df[
        (fraud_df["type_fraude"].isin(fraud_type_filter)) &
        (fraud_df["score_anomalie"] >= score_range[0]) &
        (fraud_df["score_anomalie"] <= score_range[1])
    ].copy()
    
    # Visualisations
    st.markdown("---")
    st.markdown("### 📈 Visualisations")
    
    fraud_viz_col1, fraud_viz_col2 = st.columns(2)
    
    # Histogramme
    with fraud_viz_col1:
        st.markdown("#### Distribution des scores d'anomalie")
        fig, ax = plt.subplots(figsize=(10, 5))
        
        ax.hist(fraud_df["score_anomalie"], bins=50, alpha=0.5, label="Tous", color="steelblue", edgecolor="black")
        ax.hist(filtered_fraud["score_anomalie"], bins=50, alpha=0.7, label="Filtrés", color="red", edgecolor="black")
        
        ax.set_xlabel("Score d'Anomalie")
        ax.set_ylabel("Fréquence")
        ax.legend()
        ax.grid(True, alpha=0.3)
        
        st.pyplot(fig)
        plt.close()
    
    # Pie chart
    with fraud_viz_col2:
        st.markdown("#### Répartition par type de fraude")
        fraud_type_dist = filtered_fraud["type_fraude"].value_counts()
        
        fig, ax = plt.subplots(figsize=(10, 5))
        colors = ["#ff6b6b", "#4ecdc4"]
        ax.pie(fraud_type_dist.values, labels=fraud_type_dist.index, autopct="%1.1f%%", 
               colors=colors, startangle=90)
        ax.set_title("Répartition des fraudes détectées")
        
        st.pyplot(fig)
        plt.close()
    
    # Boxplot
    fraud_viz_col3, fraud_viz_col4 = st.columns(2)
    
    with fraud_viz_col3:
        st.markdown("#### Score d'anomalie par type")
        fig, ax = plt.subplots(figsize=(10, 5))
        
        fraud_df.boxplot(column="score_anomalie", by="type_fraude", ax=ax)
        plt.suptitle("Distribution des scores par type de fraude")
        plt.xlabel("Type de Fraude")
        plt.ylabel("Score d'Anomalie")
        
        st.pyplot(fig)
        plt.close()
    
    with fraud_viz_col4:
        st.markdown("#### Statistiques détaillées")
        stats = filtered_fraud.groupby("type_fraude")["score_anomalie"].agg([
            "count", "mean", "median", "min", "max", "std"
        ]).round(4)
        
        st.dataframe(stats, use_container_width=True)
    
    # Tableau détaillé
    st.markdown("---")
    st.markdown("### 📋 Données Détaillées")
    
    # Fusion avec données de transactions pour plus de contexte
    fraud_details = filtered_fraud.copy()
    fraud_details = fraud_details.merge(
        transactions_df[["order_id", "user_id", "montant", "nb_tickets", "timestamp"]],
        on="order_id",
        how="left"
    )
    
    st.dataframe(
        fraud_details[[
            "order_id", "user_id", "score_anomalie", "type_fraude", 
            "montant", "nb_tickets", "timestamp"
        ]].sort_values("score_anomalie", ascending=False),
        use_container_width=True
    )
    
    # Export
    st.markdown("---")
    st.markdown("### 💾 Export des données")
    
    csv = filtered_fraud.to_csv(index=False)
    st.download_button(
        label="Télécharger les résultats filtrés (CSV)",
        data=csv,
        file_name="fraude_detection_results.csv",
        mime="text/csv"
    )

# =====================================================================
# TAB 2 - SEGMENTATION DES UTILISATEURS
# =====================================================================
with tab2:
    st.header("👥 Segmentation des Utilisateurs (K-Means)")
    st.markdown("""
    **Objectif**: Identifier des profils d'acheteurs pour le ciblage marketing
    - **Modèle**: K-Means Clustering
    - **Variables**: Panier moyen, Fréquence d'achat, Vitesse d'achat
    - **Résultat**: Groupes d'utilisateurs (clusters) exploitables
    """)
    
    # Analyse K-Means
    st.markdown("---")
    st.markdown("### 📊 Analyse des Clusters")
    
    # Préparation des features
    feature_cols = [
        "nb_commandes",
        "montant_total",
        "montant_moyen",
        "nb_tickets_total",
        "nb_tickets_moyen",
        "delta_moyen",
        "delta_min",
        "delta_25",
        "delta_75"
    ]
    
    X = segmentation_df[feature_cols].copy()
    
    # Log-transform
    for col in ["montant_total", "nb_commandes", "nb_tickets_total", "delta_moyen", "delta_min"]:
        X[col] = np.log1p(X[col])
    
    # Standardisation
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    
    # Utiliser les clusters existants si disponibles
    if "cluster" in segmentation_df.columns:
        segmentation_df_with_clusters = segmentation_df.copy()
        n_clusters_actual = segmentation_df["cluster"].nunique()
        st.info(f"✅ {n_clusters_actual} clusters détectés dans les données")
    else:
        st.warning("Colonnes 'cluster' non trouvées dans les données")
        n_clusters_actual = 0
    
    # Métriques globales
    col_seg1, col_seg2, col_seg3, col_seg4 = st.columns(4)
    
    with col_seg1:
        st.metric(
            label="Nombre d'utilisateurs",
            value=f"{len(segmentation_df):,}",
        )
    
    with col_seg2:
        if "cluster" in segmentation_df.columns:
            st.metric(
                label="Clusters détectés",
                value=n_clusters_actual,
            )
    
    with col_seg3:
        avg_orders = segmentation_df["nb_commandes"].mean()
        st.metric(
            label="Commandes moyennes",
            value=f"{avg_orders:.1f}",
        )
    
    with col_seg4:
        avg_amount = segmentation_df["montant_moyen"].mean()
        st.metric(
            label="Panier moyen (€)",
            value=f"{avg_amount:.2f}",
        )
    
    # Filtres
    st.markdown("---")
    st.markdown("### Filtres")
    
    seg_col1, seg_col2, seg_col3 = st.columns(3)
    
    with seg_col1:
        if "cluster" in segmentation_df.columns:
            cluster_filter = st.multiselect(
                "Sélectionner des clusters",
                options=sorted(segmentation_df["cluster"].unique()),
                default=sorted(segmentation_df["cluster"].unique()),
                key="cluster_filter"
            )
    
    with seg_col2:
        min_orders = st.slider(
            "Nombre minimum de commandes",
            min_value=int(segmentation_df["nb_commandes"].min()),
            max_value=int(segmentation_df["nb_commandes"].max()),
            value=int(segmentation_df["nb_commandes"].min()),
            key="min_orders"
        )
    
    with seg_col3:
        max_amount = st.slider(
            "Montant total maximum (€)",
            min_value=int(segmentation_df["montant_total"].min()),
            max_value=int(segmentation_df["montant_total"].max()),
            value=int(segmentation_df["montant_total"].max()),
            key="max_amount"
        )
    
    # Application des filtres
    if "cluster" in segmentation_df.columns:
        filtered_seg = segmentation_df[
            (segmentation_df["cluster"].isin(cluster_filter)) &
            (segmentation_df["nb_commandes"] >= min_orders) &
            (segmentation_df["montant_total"] <= max_amount)
        ].copy()
    else:
        filtered_seg = segmentation_df[
            (segmentation_df["nb_commandes"] >= min_orders) &
            (segmentation_df["montant_total"] <= max_amount)
        ].copy()
    
    # Visualisations
    st.markdown("---")
    st.markdown("### 📈 Visualisations")
    
    seg_viz_col1, seg_viz_col2 = st.columns(2)
    
    # Distribution des clusters
    with seg_viz_col1:
        st.markdown("#### Distribution des utilisateurs par cluster")
        
        if "cluster" in segmentation_df.columns:
            cluster_dist = filtered_seg["cluster"].value_counts().sort_index()
            
            fig, ax = plt.subplots(figsize=(10, 5))
            cluster_dist.plot(kind='bar', ax=ax, color='steelblue', edgecolor='black')
            
            ax.set_xlabel("Cluster")
            ax.set_ylabel("Nombre d'utilisateurs")
            ax.set_title("Distribution des utilisateurs par cluster")
            ax.grid(True, alpha=0.3, axis='y')
            plt.xticks(rotation=0)
            
            st.pyplot(fig)
            plt.close()
    
    # Caractéristiques moyennes par cluster
    with seg_viz_col2:
        st.markdown("#### Caractéristiques moyennes par cluster")
        
        if "cluster" in segmentation_df.columns:
            cluster_profiles = filtered_seg.groupby("cluster").agg({
                "nb_commandes": "mean",
                "montant_moyen": "mean",
                "delta_moyen": "mean",
                "nb_tickets_moyen": "mean"
            }).round(2)
            
            st.dataframe(cluster_profiles, use_container_width=True)
    
    # Scatter plots
    seg_viz_col3, seg_viz_col4 = st.columns(2)
    
    with seg_viz_col3:
        st.markdown("#### Panier moyen vs Fréquence d'achat")
        
        fig, ax = plt.subplots(figsize=(10, 5))
        
        if "cluster" in segmentation_df.columns:
            for cluster in sorted(filtered_seg["cluster"].unique()):
                cluster_data = filtered_seg[filtered_seg["cluster"] == cluster]
                ax.scatter(
                    cluster_data["nb_commandes"],
                    cluster_data["montant_moyen"],
                    label=f"Cluster {cluster}",
                    alpha=0.6,
                    s=50
                )
            
            ax.set_xlabel("Nombre de commandes")
            ax.set_ylabel("Panier moyen (€)")
            ax.set_title("Panier moyen vs Fréquence d'achat")
            ax.legend()
            ax.grid(True, alpha=0.3)
        else:
            ax.scatter(
                filtered_seg["nb_commandes"],
                filtered_seg["montant_moyen"],
                alpha=0.6,
                s=50
            )
            ax.set_xlabel("Nombre de commandes")
            ax.set_ylabel("Panier moyen (€)")
            ax.set_title("Panier moyen vs Fréquence d'achat")
            ax.grid(True, alpha=0.3)
        
        st.pyplot(fig)
        plt.close()
    
    with seg_viz_col4:
        st.markdown("#### Vitesse d'achat vs Montant total")
        
        fig, ax = plt.subplots(figsize=(10, 5))
        
        if "cluster" in segmentation_df.columns:
            for cluster in sorted(filtered_seg["cluster"].unique()):
                cluster_data = filtered_seg[filtered_seg["cluster"] == cluster]
                ax.scatter(
                    cluster_data["delta_moyen"] / 3600,  # Convertir en heures
                    cluster_data["montant_total"],
                    label=f"Cluster {cluster}",
                    alpha=0.6,
                    s=50
                )
            
            ax.set_xlabel("Délai moyen entre achats (heures)")
            ax.set_ylabel("Montant total (€)")
            ax.set_title("Vitesse d'achat vs Montant total")
            ax.legend()
            ax.grid(True, alpha=0.3)
        else:
            ax.scatter(
                filtered_seg["delta_moyen"] / 3600,
                filtered_seg["montant_total"],
                alpha=0.6,
                s=50
            )
            ax.set_xlabel("Délai moyen entre achats (heures)")
            ax.set_ylabel("Montant total (€)")
            ax.set_title("Vitesse d'achat vs Montant total")
            ax.grid(True, alpha=0.3)
        
        st.pyplot(fig)
        plt.close()
    
    # Profils des clusters
    if "cluster" in segmentation_df.columns:
        st.markdown("---")
        st.markdown("### 🎯 Profils des Clusters")
        
        for cluster_id in sorted(filtered_seg["cluster"].unique()):
            cluster_data = filtered_seg[filtered_seg["cluster"] == cluster_id]
            
            col_profile1, col_profile2, col_profile3, col_profile4 = st.columns(4)
            
            with col_profile1:
                st.metric(
                    label=f"Cluster {cluster_id}",
                    value=f"{len(cluster_data)} users"
                )
            
            with col_profile2:
                avg_cmd = cluster_data["nb_commandes"].mean()
                st.metric(
                    label="Commandes/user",
                    value=f"{avg_cmd:.1f}"
                )
            
            with col_profile3:
                avg_panier = cluster_data["montant_moyen"].mean()
                st.metric(
                    label="Panier moyen",
                    value=f"€{avg_panier:.2f}"
                )
            
            with col_profile4:
                avg_vitesse = cluster_data["delta_moyen"].mean() / 3600
                st.metric(
                    label="Délai moyen",
                    value=f"{avg_vitesse:.1f}h"
                )
    
    # Tableau détaillé
    st.markdown("---")
    st.markdown("### 📋 Données Détaillées")
    
    display_cols = [
        "user_id", "nb_commandes", "montant_total", "montant_moyen",
        "nb_tickets_moyen", "delta_moyen"
    ]
    
    if "cluster" in segmentation_df.columns:
        display_cols.insert(1, "cluster")
    
    st.dataframe(
        filtered_seg[display_cols].sort_values(
            "montant_total", ascending=False
        ),
        use_container_width=True
    )
    
    # Export
    st.markdown("---")
    st.markdown("### 💾 Export des données")
    
    csv_seg = filtered_seg.to_csv(index=False)
    st.download_button(
        label="Télécharger les résultats filtrés (CSV)",
        data=csv_seg,
        file_name="segmentation_users_results.csv",
        mime="text/csv"
    )

# =====================================================================
# TAB 3 - PRÉDICTION DE SOLD-OUT
# =====================================================================
with tab3:
    st.header("📦 Prédiction de Sold-Out (Régression Logistique)")
    st.markdown("""
    **Objectif**: Estimer la probabilité qu'un événement soit complet
    - **Modèle**: Régression Logistique
    - **Variables**: Vélocité d'achat, Tickets cumulés, Temps restant
    - **Sortie**: Temps estimé jusqu'au sold-out
    """)
    
    # Préparation des données
    st.markdown("---")
    st.markdown("### 📊 Analyse des Prédictions")
    
    # Obtenir les dernières prédictions par event_id
    latest_predictions = soldout_df.sort_values("timestamp").drop_duplicates(
        subset=["event_id"], 
        keep="last"
    ).copy()
    
    # Métriques globales
    col_sold1, col_sold2, col_sold3, col_sold4 = st.columns(4)
    
    with col_sold1:
        st.metric(
            label="Événements analysés",
            value=f"{len(latest_predictions):,}",
        )
    
    with col_sold2:
        avg_velocity = soldout_df["velocity_tpm"].mean()
        st.metric(
            label="Vélocité moyenne",
            value=f"{avg_velocity:.4f}",
            delta="tickets/min"
        )
    
    with col_sold3:
        avg_tickets = soldout_df["tickets_cumules"].mean()
        st.metric(
            label="Tickets cumulés (moy)",
            value=f"{avg_tickets:.0f}",
        )
    
    with col_sold4:
        avg_time_remaining = latest_predictions["pred_time_to_soldout_min"].mean()
        st.metric(
            label="Temps avant sold-out (moy)",
            value=f"{avg_time_remaining:.0f}",
            delta="minutes"
        )
    
    # Filtres
    st.markdown("---")
    st.markdown("### Filtres")
    
    sold_col1, sold_col2, sold_col3 = st.columns(3)
    
    with sold_col1:
        min_velocity = st.slider(
            "Vélocité minimale (tickets/min)",
            min_value=float(soldout_df["velocity_tpm"].min()),
            max_value=float(soldout_df["velocity_tpm"].max()),
            value=float(soldout_df["velocity_tpm"].quantile(0.25)),
            key="min_velocity"
        )
    
    with sold_col2:
        max_time = st.slider(
            "Temps max avant sold-out (minutes)",
            min_value=int(latest_predictions["pred_time_to_soldout_min"].min()),
            max_value=int(latest_predictions["pred_time_to_soldout_min"].max()),
            value=int(latest_predictions["pred_time_to_soldout_min"].max()),
            key="max_time_soldout"
        )
    
    with sold_col3:
        min_tickets = st.slider(
            "Tickets cumulés minimum",
            min_value=int(soldout_df["tickets_cumules"].min()),
            max_value=int(soldout_df["tickets_cumules"].max()),
            value=int(soldout_df["tickets_cumules"].quantile(0.25)),
            key="min_tickets"
        )
    
    # Application des filtres
    filtered_sold = latest_predictions[
        (latest_predictions["velocity_tpm"] >= min_velocity) &
        (latest_predictions["pred_time_to_soldout_min"] <= max_time) &
        (latest_predictions["tickets_cumules"] >= min_tickets)
    ].copy()
    
    # Visualisations
    st.markdown("---")
    st.markdown("### 📈 Visualisations")
    
    sold_viz_col1, sold_viz_col2 = st.columns(2)
    
    # Histogramme du temps avant sold-out
    with sold_viz_col1:
        st.markdown("#### Distribution - Temps avant sold-out")
        fig, ax = plt.subplots(figsize=(10, 5))
        
        ax.hist(
            latest_predictions["pred_time_to_soldout_min"],
            bins=40,
            alpha=0.7,
            label="Tous",
            color="steelblue",
            edgecolor="black"
        )
        ax.hist(
            filtered_sold["pred_time_to_soldout_min"],
            bins=40,
            alpha=0.7,
            label="Filtrés",
            color="red",
            edgecolor="black"
        )
        
        ax.set_xlabel("Temps avant sold-out (minutes)")
        ax.set_ylabel("Fréquence")
        ax.legend()
        ax.grid(True, alpha=0.3)
        
        st.pyplot(fig)
        plt.close()
    
    # Histogramme de vélocité
    with sold_viz_col2:
        st.markdown("#### Distribution - Vélocité d'achat")
        fig, ax = plt.subplots(figsize=(10, 5))
        
        ax.hist(
            soldout_df["velocity_tpm"],
            bins=40,
            alpha=0.7,
            color="green",
            edgecolor="black"
        )
        
        ax.set_xlabel("Vélocité (tickets/min)")
        ax.set_ylabel("Fréquence")
        ax.grid(True, alpha=0.3)
        
        st.pyplot(fig)
        plt.close()
    
    # Scatter plots
    sold_viz_col3, sold_viz_col4 = st.columns(2)
    
    with sold_viz_col3:
        st.markdown("#### Vélocité vs Temps avant sold-out")
        fig, ax = plt.subplots(figsize=(10, 5))
        
        ax.scatter(
            filtered_sold["velocity_tpm"],
            filtered_sold["pred_time_to_soldout_min"],
            alpha=0.6,
            s=50,
            c=filtered_sold["tickets_cumules"],
            cmap="viridis"
        )
        
        ax.set_xlabel("Vélocité (tickets/min)")
        ax.set_ylabel("Temps avant sold-out (minutes)")
        ax.set_title("Vélocité vs Temps avant sold-out")
        ax.grid(True, alpha=0.3)
        
        cbar = plt.colorbar(ax.collections[0], ax=ax)
        cbar.set_label("Tickets cumulés")
        
        st.pyplot(fig)
        plt.close()
    
    with sold_viz_col4:
        st.markdown("#### Tickets cumulés vs Temps avant sold-out")
        fig, ax = plt.subplots(figsize=(10, 5))
        
        ax.scatter(
            filtered_sold["tickets_cumules"],
            filtered_sold["pred_time_to_soldout_min"],
            alpha=0.6,
            s=50,
            c=filtered_sold["velocity_tpm"],
            cmap="plasma"
        )
        
        ax.set_xlabel("Tickets cumulés")
        ax.set_ylabel("Temps avant sold-out (minutes)")
        ax.set_title("Tickets vs Temps avant sold-out")
        ax.grid(True, alpha=0.3)
        
        cbar = plt.colorbar(ax.collections[0], ax=ax)
        cbar.set_label("Vélocité (tickets/min)")
        
        st.pyplot(fig)
        plt.close()
    
    # Statistiques par catégories de temps
    st.markdown("---")
    st.markdown("### 📊 Catégorisation des événements")
    
    # Créer des catégories de temps
    def categorize_time(minutes):
        if minutes < 60:
            return "Critique (< 1h)"
        elif minutes < 360:
            return "Urgent (1h-6h)"
        elif minutes < 1440:
            return "Modéré (6h-1j)"
        else:
            return "Stable (> 1j)"
    
    filtered_sold["categorie"] = filtered_sold["pred_time_to_soldout_min"].apply(categorize_time)
    
    cat_dist = filtered_sold["categorie"].value_counts()
    cat_order = ["Critique (< 1h)", "Urgent (1h-6h)", "Modéré (6h-1j)", "Stable (> 1j)"]
    cat_dist = cat_dist.reindex([c for c in cat_order if c in cat_dist.index])
    
    sold_stat_col1, sold_stat_col2 = st.columns(2)
    
    with sold_stat_col1:
        st.markdown("#### Répartition par catégorie")
        fig, ax = plt.subplots(figsize=(10, 5))
        
        colors = ["#d32f2f", "#ff9800", "#fbc02d", "#388e3c"]
        ax.bar(range(len(cat_dist)), cat_dist.values, color=colors[:len(cat_dist)], edgecolor="black")
        ax.set_xticks(range(len(cat_dist)))
        ax.set_xticklabels(cat_dist.index, rotation=45, ha="right")
        ax.set_ylabel("Nombre d'événements")
        ax.grid(True, alpha=0.3, axis="y")
        
        st.pyplot(fig)
        plt.close()
    
    with sold_stat_col2:
        st.markdown("#### Statistiques par catégorie")
        stats_by_cat = filtered_sold.groupby("categorie").agg({
            "pred_time_to_soldout_min": ["count", "mean", "min", "max"],
            "velocity_tpm": "mean",
            "tickets_cumules": "mean"
        }).round(2)
        
        st.dataframe(stats_by_cat, use_container_width=True)
    
    # Tableau détaillé
    st.markdown("---")
    st.markdown("### 📋 Données Détaillées")
    
    st.dataframe(
        filtered_sold[[
            "event_id", "timestamp", "tickets_cumules", "velocity_tpm",
            "pred_time_to_soldout_min", "soldout_time_pred"
        ]].sort_values("pred_time_to_soldout_min"),
        use_container_width=True
    )
    
    # Export
    st.markdown("---")
    st.markdown("### 💾 Export des données")
    
    csv_sold = filtered_sold[[
        "event_id", "timestamp", "tickets_cumules", "velocity_tpm",
        "pred_time_to_soldout_min", "soldout_time_pred"
    ]].to_csv(index=False)
    
    st.download_button(
        label="Télécharger les résultats filtrés (CSV)",
        data=csv_sold,
        file_name="soldout_prediction_results.csv",
        mime="text/csv"
    )
