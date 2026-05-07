# Guide de présentation orale - Dashboard analytique

Ce document sert de support de préparation pour présenter le projet à l'oral. L'objectif est de pouvoir expliquer clairement le besoin, la solution, les fonctionnalités, les données utilisées et la valeur métier de chaque onglet.

---

## 1. Présentation rapide du projet

### Titre du projet
Dashboard analytique Streamlit pour l'analyse de fraude, la segmentation des utilisateurs et la prédiction de sold-out.

### Problème traité
Dans une activité e-commerce ou billetterie, il est utile de :
- détecter les comportements suspects ou frauduleux,
- mieux comprendre les profils utilisateurs,
- anticiper une rupture de stock ou un sold-out d'événement.

### Réponse apportée par le projet
Le dashboard regroupe trois analyses complémentaires dans une seule interface interactive :
- détection de fraude avec Isolation Forest,
- segmentation clients avec K-Means,
- prédiction de sold-out à partir d'un fichier de résultats pré-calculés.

### Message clé à dire à l'oral
L'intérêt principal du projet est de transformer des données brutes en indicateurs actionnables, directement exploitables par des équipes fraude, marketing et opérations.

---

## 2. Architecture générale du projet

### Fichier principal
- `app_analytics_dashboard.py` : application Streamlit principale.

### Fichiers de données utilisés
- `data/datasets/fraud_monitoring_from_isolation_forest.csv`
- `data/datasets/user_segmentation.csv`
- `data/datasets/transactions_50000.csv`
- `data/datasets/soldout_prediction.csv`

### Principe de fonctionnement
L'application :
1. charge les données avec `@st.cache_data`,
2. prépare les jeux de données,
3. affiche trois onglets,
4. permet de filtrer, visualiser et exporter les résultats.

### Point technique important
Le dashboard ne se limite pas à afficher des tableaux. Il propose une lecture analytique des données avec :
- des métriques globales,
- des filtres dynamiques,
- des graphiques,
- des tableaux détaillés,
- des exports CSV.

---

## 3. Structure de l'interface

### En-tête général
Le dashboard affiche un titre global et un sous-titre qui résument les trois analyses.

### Les trois onglets
- `🚨 Détection de Fraude`
- `👥 Segmentation des Utilisateurs`
- `📦 Prédiction Sold-Out`

### Ce que cela montre à l'oral
L'interface est pensée pour être simple à comprendre : une problématique par onglet, un jeu de métriques clair, puis les visualisations et enfin les données détaillées.

---

## 4. Détail complet de l'onglet 1 - Détection de fraude

### Objectif métier
Identifier les transactions suspectes pouvant correspondre à :
- des bots,
- des scripts automatisés,
- des achats massifs anormaux,
- des comportements de revente ou de scalping.

### Modèle utilisé
Isolation Forest.

### Pourquoi ce modèle est pertinent
Isolation Forest est adapté à la détection d'anomalies car il repère les observations rares ou isolées par rapport au comportement normal.

### Données exploitées
- score d'anomalie,
- type de fraude :
	- `VELOCITY` = achats très rapides et répétés sur une courte période,
	- `VOLUME` = volumes ou montants anormalement élevés,
- montant,
- nombre de tickets,
- horodatage,
- identification utilisateur et commande.

### Métriques affichées
L'onglet affiche quatre indicateurs principaux :
- nombre total de transactions,
- nombre de transactions suspectes,
- nombre de fraudes de type VELOCITY,
- nombre de fraudes de type VOLUME.

### Comment les interpréter à l'oral
- Le total des transactions donne l'échelle du jeu de données.
- Le nombre de transactions suspectes permet de voir le volume d'anomalies détectées.
- VELOCITY renvoie à des achats très rapides et répétitifs.
- VOLUME renvoie à des volumes anormalement élevés.

### Filtres disponibles
- filtre par type de fraude,
- filtre par plage de score d'anomalie.

### Intérêt des filtres
Ils permettent de se concentrer sur une catégorie de fraude précise ou sur les cas les plus risqués.

### Visualisations proposées
1. Histogramme des scores d'anomalie.
2. Camembert de répartition par type de fraude.
3. Boxplot des scores par type de fraude.
4. Tableau de statistiques descriptives par type de fraude.

### Ce qu'il faut dire pour chaque graphique
- L'histogramme montre la distribution globale et permet de repérer la concentration des scores.
- Le camembert montre la part de chaque type de fraude.
- Le boxplot compare les distributions et met en évidence des différences de dispersion.
- Le tableau statistique aide à quantifier les tendances observées.

### Données détaillées affichées
Le tableau final combine les informations frauduleuses avec le contexte transactionnel :
- `order_id`,
- `user_id`,
- `score_anomalie`,
- `type_fraude`,
- `montant`,
- `nb_tickets`,
- `timestamp`.

### Export
Les résultats filtrés peuvent être téléchargés en CSV.

### Phrase simple à dire à l'oral
Cet onglet permet d'identifier rapidement les cas les plus suspects, puis de les explorer avec des filtres et de les exporter pour analyse manuelle.

---

## 5. Détail complet de l'onglet 2 - Segmentation des utilisateurs

### Objectif métier
Regrouper les utilisateurs en profils homogènes pour mieux comprendre leurs comportements d'achat et adapter les actions marketing.

### Modèle utilisé
K-Means.

### Pourquoi ce modèle est pertinent
K-Means est adapté quand on veut créer des groupes à partir de caractéristiques numériques proches.

### Variables utilisées
L'analyse repose notamment sur :
- nombre de commandes,
- montant total,
- montant moyen,
- nombre de tickets total,
- nombre de tickets moyen,
- délai moyen entre achats,
- quartiles et minimums sur les délais.

### Pré-traitements appliqués
- standardisation avec `StandardScaler`,
- transformation logarithmique sur certaines variables très asymétriques.

### Pourquoi ces pré-traitements sont importants
Ils évitent qu'une variable domine les autres et améliorent la qualité de la segmentation.

### Métriques affichées
- nombre d'utilisateurs,
- nombre de clusters détectés,
- commandes moyennes,
- panier moyen.

### Filtres disponibles
- sélection de clusters,
- nombre minimum de commandes,
- montant total maximum.

### Utilité des filtres
Ils servent à isoler un segment particulier, comme les gros acheteurs, les clients occasionnels ou les profils à forte activité.

### Visualisations proposées
1. Distribution des utilisateurs par cluster.
2. Tableau des caractéristiques moyennes par cluster.
3. Scatter plot panier moyen vs fréquence d'achat.
4. Scatter plot vitesse d'achat vs montant total.

### Comment commenter les graphiques
- La distribution des clusters montre la taille relative de chaque segment.
- Le tableau de cluster donne le profil moyen de chaque groupe.
- Le scatter panier/fréquence illustre la relation entre activité et valeur d'achat.
- Le scatter vitesse/montant aide à repérer des comportements rapides ou atypiques.

### Profils des clusters
Pour chaque cluster, le dashboard affiche :
- nombre d'utilisateurs,
- commandes par utilisateur,
- panier moyen,
- délai moyen entre achats.

### Ce que cela apporte à l'entreprise
Cette segmentation aide à :
- personnaliser les campagnes marketing,
- cibler les utilisateurs à fort potentiel,
- améliorer la rétention,
- mieux comprendre la valeur de chaque segment.

### Données détaillées affichées
Le tableau final présente :
- `user_id`,
- `cluster` si disponible,
- `nb_commandes`,
- `montant_total`,
- `montant_moyen`,
- `nb_tickets_moyen`,
- `delta_moyen`.

### Export
Les résultats filtrés peuvent être exportés en CSV.

### Phrase simple à dire à l'oral
Cet onglet transforme un ensemble d'indicateurs utilisateurs en segments lisibles et exploitables pour les décisions marketing.

---

## 6. Détail complet de l'onglet 3 - Prédiction de sold-out

### Objectif métier
Anticiper le moment où un événement risque d'être complet.

### Approche retenue
L'onglet utilise directement les prédictions stockées dans `soldout_prediction.csv`.

### Ce que cela signifie à l'oral
Le modèle a déjà produit des prédictions et l'application les présente dans une interface interactive. Cela permet de se concentrer sur l'analyse et la décision plutôt que sur le calcul en direct.

### Variables présentes dans le fichier
- `event_id`,
- `timestamp`,
- `tickets_cumules`,
- `velocity_tpm`,
- `pred_time_to_soldout_min`,
- `soldout_time_pred`.

### Interprétation des variables
- `tickets_cumules` montre le volume déjà vendu.
- `velocity_tpm` mesure la vitesse de vente en tickets par minute.
- `pred_time_to_soldout_min` indique le temps estimé avant rupture.
- `soldout_time_pred` donne l'heure estimée du sold-out.

### Métriques affichées
- nombre d'événements analysés,
- vélocité moyenne,
- nombre moyen de tickets cumulés,
- temps moyen avant sold-out.

### Pourquoi ces métriques sont utiles
Elles donnent un aperçu rapide de l'état général du stock et de l'urgence potentielle.

### Filtres disponibles
- vélocité minimale,
- temps maximum avant sold-out,
- tickets cumulés minimum.

### Intérêt des filtres
Ils permettent d'isoler les événements les plus critiques ou les plus rapides à se vendre.

### Visualisations proposées
1. Histogramme des temps avant sold-out.
2. Histogramme de la vélocité d'achat.
3. Scatter plot vélocité vs temps avant sold-out.
4. Scatter plot tickets cumulés vs temps avant sold-out.
5. Répartition des événements par catégorie de criticité.
6. Tableau de statistiques par catégorie.

### Catégorisation métier des événements
Le dashboard classe les événements en quatre catégories :
- Critique : moins d'une heure,
- Urgent : entre 1h et 6h,
- Modéré : entre 6h et 1 jour,
- Stable : plus d'un jour.

### Comment expliquer cette catégorisation
Elle sert à traduire une prédiction numérique en un niveau de priorité compréhensible par un décideur.

### Données détaillées affichées
Le tableau contient :
- `event_id`,
- `timestamp`,
- `tickets_cumules`,
- `velocity_tpm`,
- `pred_time_to_soldout_min`,
- `soldout_time_pred`.

### Export
Les prédictions filtrées sont téléchargeables en CSV.

### Phrase simple à dire à l'oral
Cet onglet aide les équipes opérationnelles à savoir quels événements nécessitent une action rapide pour éviter une rupture ou mieux gérer le stock.

---

## 7. Résumé fonctionnel du dashboard

### Ce que fait l'application
Elle centralise trois usages analytiques dans une seule interface :
- surveiller la fraude,
- segmenter les clients,
- anticiper les sold-outs.

### Ce qui fait sa valeur
Le dashboard est utile parce qu'il combine :
- des indicateurs synthétiques,
- des graphiques de lecture rapide,
- des données détaillées,
- des filtres,
- des exports exploitables.

### Message de synthèse à l'oral
Le projet transforme la donnée en outil d'aide à la décision pour trois équipes différentes : fraude, marketing et opérations.

---

## 8. Déroulé conseillé pour la présentation orale

### 1. Introduction
Présenter le problème métier et les trois analyses.

### 2. Architecture
Expliquer le rôle de `app_analytics_dashboard.py` et des CSV.

### 3. Démonstration onglet par onglet
- onglet fraude,
- onglet segmentation,
- onglet sold-out.

### 4. Conclusion
Résumer les bénéfices : lecture rapide, pilotage opérationnel, export des données.

### Durée orale possible
- 1 minute : introduction,
- 2 à 3 minutes : onglet fraude,
- 2 à 3 minutes : onglet segmentation,
- 2 à 3 minutes : onglet sold-out,
- 1 minute : conclusion.

---

## 9. Questions probables du jury et réponses courtes

### Pourquoi avoir choisi Streamlit ?
Parce que Streamlit permet de créer rapidement une interface interactive, lisible et adaptée à une démonstration de données.

### Pourquoi Isolation Forest ?
Parce que c'est un algorithme adapté à la détection d'anomalies dans des données transactionnelles.

### Pourquoi K-Means ?
Parce que c'est une méthode simple et efficace pour créer des groupes homogènes à partir de variables numériques.

### Pourquoi utiliser un CSV pour le sold-out ?
Parce que l'objectif du dashboard est ici la visualisation et l'analyse. Le modèle peut être intégré ensuite si besoin.

### À quoi servent les exports CSV ?
À transmettre les résultats à d'autres équipes ou à poursuivre l'analyse dans Excel, Python ou un autre outil.

### Pourquoi les filtres sont-ils importants ?
Ils permettent d'explorer des sous-ensembles précis et de rendre la lecture plus opérationnelle.

---

## 10. Mini script de présentation

Tu peux t'entraîner avec ce discours court :

> J'ai développé un dashboard Streamlit qui regroupe trois analyses complémentaires. La première détecte les comportements suspects avec Isolation Forest. La deuxième segmente les utilisateurs avec K-Means pour mieux comprendre leurs profils. La troisième anticipe les sold-outs à partir de prédictions déjà calculées. L'application propose des métriques, des filtres, des graphiques et des exports CSV pour que les résultats soient directement exploitables par une équipe métier.

---

## 11. Conseils pour bien présenter

- Commence par le besoin métier, pas par le code.
- Explique chaque graphique en une phrase simple.
- Insiste sur la valeur ajoutée décisionnelle.
- Ne lis pas tout le tableau à l'écran, sélectionne les éléments utiles.
- Termine par les usages concrets : fraude, marketing, opérations.

---

## 12. Conclusion courte

Ce projet montre comment des modèles ML et des données préparées peuvent être transformés en un outil métier simple à utiliser. L'objectif final n'est pas seulement de prédire, mais de rendre les résultats compréhensibles et actionnables.
