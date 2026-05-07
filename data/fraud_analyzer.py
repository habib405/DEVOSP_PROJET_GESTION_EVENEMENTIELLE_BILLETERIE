"""
Utilitaire pour l'analyse de fraude - Isolation Forest
Fournit des fonctions de chargement, fusion et statistiques des données
"""

import pandas as pd
import numpy as np
from pathlib import Path
from typing import Tuple, Dict, Optional


class FraudAnalyzer:
    """Classe utilitaire pour l'analyse de détection de fraude"""
    
    def __init__(self, data_dir: str = "data"):
        """
        Initialise l'analyseur de fraude
        
        Args:
            data_dir: Répertoire contenant les données
        """
        self.data_dir = Path(data_dir)
        self.fraud_df = None
        self.transactions_df = None
        self.merged_df = None
    
    def load_fraud_data(self) -> pd.DataFrame:
        """Charge les données de fraude détectée"""
        fraud_path = self.data_dir / "datasets" / "fraud_monitoring_from_isolation_forest.csv"
        self.fraud_df = pd.read_csv(fraud_path)
        print(f"✓ Chargé {len(self.fraud_df)} détections de fraude")
        return self.fraud_df
    
    def load_transactions_data(self) -> pd.DataFrame:
        """Charge les données de transactions"""
        transactions_path = self.data_dir / "datasets" / "transactions_50000.csv"
        self.transactions_df = pd.read_csv(transactions_path, parse_dates=["timestamp"])
        print(f"✓ Chargé {len(self.transactions_df)} transactions")
        return self.transactions_df
    
    def merge_data(self) -> pd.DataFrame:
        """Fusionne les données de transactions et de fraude"""
        if self.fraud_df is None:
            self.load_fraud_data()
        if self.transactions_df is None:
            self.load_transactions_data()
        
        self.merged_df = self.transactions_df.merge(
            self.fraud_df[["order_id", "score_anomalie", "type_fraude"]],
            on="order_id",
            how="left"
        )
        print(f"✓ Fusion réalisée")
        return self.merged_df
    
    def get_fraud_statistics(self) -> Dict:
        """Retourne les statistiques globales de fraude"""
        if self.fraud_df is None:
            self.load_fraud_data()
        if self.transactions_df is None:
            self.load_transactions_data()
        
        stats = {
            "total_transactions": len(self.transactions_df),
            "fraud_count": len(self.fraud_df),
            "fraud_percentage": (len(self.fraud_df) / len(self.transactions_df)) * 100,
            "velocity_count": len(self.fraud_df[self.fraud_df["type_fraude"] == "VELOCITY"]),
            "volume_count": len(self.fraud_df[self.fraud_df["type_fraude"] == "VOLUME"]),
            "score_anomalie_stats": {
                "min": float(self.fraud_df["score_anomalie"].min()),
                "max": float(self.fraud_df["score_anomalie"].max()),
                "mean": float(self.fraud_df["score_anomalie"].mean()),
                "median": float(self.fraud_df["score_anomalie"].median()),
                "std": float(self.fraud_df["score_anomalie"].std())
            }
        }
        return stats
    
    def get_fraud_by_user(self) -> pd.DataFrame:
        """Retourne les statistiques de fraude par utilisateur"""
        if self.merged_df is None:
            self.merge_data()
        
        fraud_by_user = self.merged_df[self.merged_df["type_fraude"].notna()].groupby("user_id").agg({
            "order_id": "count",
            "score_anomalie": ["min", "max", "mean"],
            "type_fraude": lambda x: "/".join(x.unique()),
            "montant": "sum"
        }).reset_index()
        
        fraud_by_user.columns = ["user_id", "nb_fraud_orders", "min_score", "max_score", 
                                 "mean_score", "fraud_types", "total_amount"]
        
        return fraud_by_user.sort_values("mean_score", ascending=False)
    
    def filter_fraud(self, 
                    fraud_type: Optional[str] = None,
                    score_min: float = 0,
                    score_max: float = 1) -> pd.DataFrame:
        """
        Filtre les données de fraude
        
        Args:
            fraud_type: Type de fraude à filtrer (VELOCITY/VOLUME/None pour tous)
            score_min: Score d'anomalie minimum
            score_max: Score d'anomalie maximum
        
        Returns:
            DataFrame filtré
        """
        if self.fraud_df is None:
            self.load_fraud_data()
        
        filtered = self.fraud_df.copy()
        
        if fraud_type:
            filtered = filtered[filtered["type_fraude"] == fraud_type]
        
        filtered = filtered[
            (filtered["score_anomalie"] >= score_min) &
            (filtered["score_anomalie"] <= score_max)
        ]
        
        return filtered
    
    def export_suspicious_users(self, output_path: str = "suspicious_users.csv"):
        """Exporte la liste des utilisateurs suspects"""
        suspicious = self.get_fraud_by_user()
        suspicious.to_csv(output_path, index=False)
        print(f"✓ Exporté {len(suspicious)} utilisateurs suspects vers {output_path}")
        return suspicious
    
    def print_summary(self):
        """Affiche un résumé des statistiques"""
        stats = self.get_fraud_statistics()
        
        print("\n" + "="*60)
        print("📊 RÉSUMÉ DE DÉTECTION DE FRAUDE - ISOLATION FOREST")
        print("="*60)
        print(f"\n📋 DONNÉES:")
        print(f"  • Total transactions : {stats['total_transactions']:,}")
        print(f"  • Transactions suspectes : {stats['fraud_count']:,} ({stats['fraud_percentage']:.2f}%)")
        
        print(f"\n🚨 TYPES DE FRAUDE:")
        print(f"  • VELOCITY : {stats['velocity_count']:,} ({stats['velocity_count']/stats['fraud_count']*100:.1f}%)")
        print(f"  • VOLUME : {stats['volume_count']:,} ({stats['volume_count']/stats['fraud_count']*100:.1f}%)")
        
        print(f"\n📈 SCORE D'ANOMALIE:")
        score_stats = stats['score_anomalie_stats']
        print(f"  • Min : {score_stats['min']:.4f}")
        print(f"  • Max : {score_stats['max']:.4f}")
        print(f"  • Moyen : {score_stats['mean']:.4f}")
        print(f"  • Médian : {score_stats['median']:.4f}")
        print(f"  • Écart-type : {score_stats['std']:.4f}")
        
        print(f"\n👥 UTILISATEURS SUSPECTS (TOP 5):")
        fraud_by_user = self.get_fraud_by_user().head(5)
        for idx, row in fraud_by_user.iterrows():
            print(f"  {idx+1}. User {row['user_id'][:8]}... : "
                  f"Score moyen={row['mean_score']:.4f}, "
                  f"Commandes={row['nb_fraud_orders']}, "
                  f"Types={row['fraud_types']}")
        
        print("\n" + "="*60 + "\n")


def main():
    """Exemple d'utilisation"""
    analyzer = FraudAnalyzer()
    
    # Charger les données
    analyzer.load_fraud_data()
    analyzer.load_transactions_data()
    analyzer.merge_data()
    
    # Afficher le résumé
    analyzer.print_summary()
    
    # Filtrer les fraudes VELOCITY
    print("\n🏃 TOP 10 FRAUDES VELOCITY (plus hauts scores):")
    velocity = analyzer.filter_fraud(fraud_type="VELOCITY").sort_values("score_anomalie", ascending=False)
    print(velocity.head(10)[["order_id", "score_anomalie"]].to_string(index=False))
    
    # Filtrer les fraudes VOLUME
    print("\n📦 TOP 10 FRAUDES VOLUME (plus hauts scores):")
    volume = analyzer.filter_fraud(fraud_type="VOLUME").sort_values("score_anomalie", ascending=False)
    print(volume.head(10)[["order_id", "score_anomalie"]].to_string(index=False))
    
    # Exporter les utilisateurs suspects
    analyzer.export_suspicious_users()


if __name__ == "__main__":
    main()
