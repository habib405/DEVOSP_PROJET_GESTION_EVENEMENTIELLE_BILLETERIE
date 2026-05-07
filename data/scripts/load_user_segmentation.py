import psycopg2
import pandas as pd
import uuid

# -----------------------------
# CONFIGURATION
# -----------------------------
CSV_PATH = "C:/Users/Etudiant/OneDrive/Desktop/GIT/DEVOSP_PROJET_GESTION_EVENEMENTIELLE_BILLETERIE/data/datasets/user_segmentation.csv"

DB_HOST = "localhost"
DB_PORT = 5430
DB_NAME = "postgres"
DB_USER = "postgres"
DB_PASSWORD = "postgres"

TABLE_NAME = "user_segmentation"

# -----------------------------
# LECTURE DU CSV
# -----------------------------
df = pd.read_csv(CSV_PATH)
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
    id, user_id, cluster,
    nb_commandes, montant_total, montant_moyen,
    nb_tickets_total, nb_tickets_moyen,
    delta_moyen, delta_min, delta_25, delta_75,
    created_at
)
VALUES (
    %s, %s, %s,
    %s, %s, %s,
    %s, %s,
    %s, %s, %s, %s,
    NOW()
)
ON CONFLICT (id) DO NOTHING;
"""

rows_inserted = 0

for _, row in df.iterrows():
    cursor.execute(insert_query, (
        str(uuid.uuid4()),          # id
        row["user_id"],             # user_id
        int(row["cluster"]),        # cluster
        int(row["nb_commandes"]),
        float(row["montant_total"]),
        float(row["montant_moyen"]),
        int(row["nb_tickets_total"]),
        float(row["nb_tickets_moyen"]),
        float(row["delta_moyen"]),
        float(row["delta_min"]),
        float(row["delta_25"]),
        float(row["delta_75"])
    ))
    rows_inserted += 1

conn.commit()

print(f"Insertion terminée : {rows_inserted} lignes insérées dans {TABLE_NAME}.")

cursor.close()
conn.close()
print("Connexion fermée.")
