import psycopg2
import pandas as pd
import uuid
from datetime import datetime

# -----------------------------
# CONFIGURATION
# -----------------------------
CSV_PATH = input("Chemin du CSV à charger : ").strip()
TABLE_NAME = input("Nom de la table PostgreSQL : ").strip()

DB_HOST = "localhost"
DB_PORT = 5430
DB_NAME = "postgres"
DB_USER = "postgres"
DB_PASSWORD = "postgres"

# -----------------------------
# LECTURE DU CSV
# -----------------------------
print(f"\n📥 Chargement du fichier : {CSV_PATH}")
df = pd.read_csv(CSV_PATH)

print(f"➡️ {len(df)} lignes trouvées.")
print(f"➡️ Colonnes détectées : {list(df.columns)}")

# -----------------------------
# CONNEXION À POSTGRESQL
# -----------------------------
print("\n🔌 Connexion à PostgreSQL...")

conn = psycopg2.connect(
    host=DB_HOST,
    port=DB_PORT,
    dbname=DB_NAME,
    user=DB_USER,
    password=DB_PASSWORD
)
cursor = conn.cursor()

print("✅ Connexion PostgreSQL OK.")

# -----------------------------
# CONSTRUCTION DE LA REQUÊTE SQL
# -----------------------------
columns = list(df.columns)

# placeholders : %s, %s, %s...
placeholders = ", ".join(["%s"] * len(columns))

# colonnes SQL : col1, col2, col3...
columns_sql = ", ".join(columns)

insert_query = f"""
INSERT INTO {TABLE_NAME} ({columns_sql})
VALUES ({placeholders})
ON CONFLICT DO NOTHING;
"""

print("\n🛠️ Requête SQL générée automatiquement :")
print(insert_query)

# -----------------------------
# INSERTION DES DONNÉES
# -----------------------------
rows_inserted = 0

print("\n🚀 Début de l'insertion...")

for _, row in df.iterrows():
    values = []

    for col in columns:
        val = row[col]

        # Conversion automatique des types
        if pd.isna(val):
            values.append(None)
        elif isinstance(val, str):
            values.append(val)
        elif isinstance(val, (int, float)):
            values.append(val)
        else:
            # Tentative de conversion datetime
            try:
                values.append(pd.to_datetime(val))
            except:
                values.append(val)

    cursor.execute(insert_query, tuple(values))
    rows_inserted += 1

conn.commit()

print(f"\n✅ Insertion terminée : {rows_inserted} lignes insérées dans {TABLE_NAME}.")

cursor.close()
conn.close()
print("🔒 Connexion fermée.")
