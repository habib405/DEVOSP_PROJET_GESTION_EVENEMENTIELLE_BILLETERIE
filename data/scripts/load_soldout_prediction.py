import psycopg2
import pandas as pd
import uuid

# -----------------------------
# CONFIGURATION
# -----------------------------
CSV_PATH = "C:/Users/Etudiant/OneDrive/Desktop/GIT/DEVOSP_PROJET_GESTION_EVENEMENTIELLE_BILLETERIE/data/datasets/soldout_prediction.csv"

DB_HOST = "localhost"
DB_PORT = 5430
DB_NAME = "postgres"
DB_USER = "postgres"
DB_PASSWORD = "postgres"

TABLE_NAME = "soldout_prediction"

# -----------------------------
# LECTURE DU CSV
# -----------------------------
df = pd.read_csv(CSV_PATH, parse_dates=["timestamp", "soldout_time_pred"])
print(f"Chargement du fichier : {CSV_PATH}")
print(f"{len(df)} lignes trouvées.")

# -----------------------------
# CONNEXION À POSTGRESQL
# -----------------------------
conn = psycopg2.connect(
    host=DB_HOST,
    port=DB_PORT,
    dbname=DB_NAME,
    user=DB_USER,
    password=DB_PASSWORD
)
cursor = conn.cursor()

print("Connexion PostgreSQL OK.")

# -----------------------------
# INSERTION DES DONNÉES
# -----------------------------
insert_query = f"""
INSERT INTO {TABLE_NAME} (
    id, event_id, timestamp,
    tickets_cumules, velocity_tpm,
    pred_time_to_soldout_min, soldout_time_pred,
    created_at
)
VALUES (
    %s, %s, %s,
    %s, %s,
    %s, %s,
    NOW()
)
ON CONFLICT (id) DO NOTHING;
"""

rows_inserted = 0

for _, row in df.iterrows():
    cursor.execute(insert_query, (
        str(uuid.uuid4()),                # id
        row["event_id"],                  # event_id
        row["timestamp"],                 # timestamp
        int(row["tickets_cumules"]),      # tickets cumulés
        float(row["velocity_tpm"]),       # vélocité
        float(row["pred_time_to_soldout_min"]),  # prédiction
        row["soldout_time_pred"]          # timestamp prédiction
    ))
    rows_inserted += 1

conn.commit()

print(f"Insertion terminée : {rows_inserted} lignes insérées dans {TABLE_NAME}.")

cursor.close()
conn.close()
print("Connexion fermée.")
