import uuid
import random
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

# -----------------------------
# CONFIGURATION
# -----------------------------
N_USERS = 10000
N_EVENTS = 20
N_TRANSACTIONS = 50000
FRAUD_RATE = 0.05  # 5% de fraude simulée

# -----------------------------
# 1. Génération des utilisateurs
# -----------------------------
users = [str(uuid.uuid4()) for _ in range(N_USERS)]

# -----------------------------
# 2. Génération des événements
# -----------------------------
events = []
for i in range(N_EVENTS):
    events.append({
        "event_id": str(uuid.uuid4()),
        "prix": random.randint(20, 150),
        "date_event": datetime(2026, random.randint(1, 12), random.randint(1, 28))
    })

# -----------------------------
# 3. Génération des transactions
# -----------------------------
transactions = []

start_date = datetime(2025, 1, 1)

for i in range(N_TRANSACTIONS):
    user = random.choice(users)
    event = random.choice(events)

    # Timestamp réaliste
    timestamp = start_date + timedelta(
        days=random.randint(0, 400),
        seconds=random.randint(0, 86400)
    )

    # Nombre de tickets
    nb_tickets = np.random.choice([1, 2, 3, 4, 5], p=[0.6, 0.2, 0.1, 0.07, 0.03])

    # Montant total
    montant = nb_tickets * event["prix"]

    # Simulation fraude (scalpers)
    is_fraud = np.random.rand() < FRAUD_RATE

    # Vélocité (temps entre commandes)
    velocity = np.random.exponential(scale=300)  # 300 sec en moyenne
    if is_fraud:
        velocity = np.random.exponential(scale=2)  # scalpers = vitesse anormale

    transactions.append({
        "order_id": str(uuid.uuid4()),
        "user_id": user,
        "event_id": event["event_id"],
        "timestamp": timestamp,
        "nb_tickets": nb_tickets,
        "montant": montant,
        "velocity_seconds": velocity,
        "is_fraud": int(is_fraud)
    })

# -----------------------------
# 4. Export CSV
# -----------------------------
df = pd.DataFrame(transactions)
df = df.sort_values("timestamp")

df.to_csv("../datasets/transactions_50000.csv", index=False)

print("Dataset généré : data/datasets/transactions_50000.csv")
