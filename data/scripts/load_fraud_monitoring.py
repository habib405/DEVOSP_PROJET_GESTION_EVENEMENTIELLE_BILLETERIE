import psycopg2
import pandas as pd

CSV_PATH = "C:/Users/Etudiant/OneDrive/Desktop/GIT/DEVOSP_PROJET_GESTION_EVENEMENTIELLE_BILLETERIE/data/datasets/fraud_monitoring_from_isolation_forest.csv"

DB_HOST = "localhost"
DB_PORT = 5430
DB_NAME = "postgres"
DB_USER = "postgres"
DB_PASSWORD = "postgres"

TABLE_NAME = "fraud_monitoring"

df = pd.read_csv(CSV_PATH)
print(f"Chargement du fichier : {CSV_PATH}")
print(f"{len(df)} lignes trouvées.")

conn = psycopg2.connect(
    host=DB_HOST,
    port=DB_PORT,
    dbname=DB_NAME,
    user=DB_USER,
    password=DB_PASSWORD
)
cursor = conn.cursor()

print("Connexion PostgreSQL OK.")

insert_query = f"""
INSERT INTO {TABLE_NAME} (id, detected_at, order_id, score_anomalie, type_fraude)
VALUES (%s, NOW(), %s, %s, %s)
ON CONFLICT (id) DO NOTHING;
"""

rows_inserted = 0

for _, row in df.iterrows():
    cursor.execute(insert_query, (
        row["id"],
        row["order_id"],
        float(row["score_anomalie"]),
        row["type_fraude"]
    ))
    rows_inserted += 1

conn.commit()

print(f"Insertion terminée : {rows_inserted} lignes insérées dans {TABLE_NAME}.")

cursor.close()
conn.close()
print("Connexion fermée.")
