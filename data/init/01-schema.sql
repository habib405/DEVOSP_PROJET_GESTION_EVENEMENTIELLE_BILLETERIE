-- ============================================================
-- SCHEMA.SQL — Schéma complet PostgreSQL
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================
-- TABLE: users
-- ============================
CREATE TABLE public.users (
    id uuid PRIMARY KEY,
    created_at timestamp(6) NOT NULL,
    email varchar(255) NOT NULL UNIQUE,
    first_name varchar(255) NOT NULL,
    last_name varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role varchar(255) NOT NULL,
    CONSTRAINT users_role_check CHECK (role IN ('ADMIN','ORGANIZER','STAFF','ATTENDEE'))
);

-- ============================
-- TABLE: venues
-- ============================
CREATE TABLE public.venues (
    id uuid PRIMARY KEY,
    address varchar(255) NOT NULL,
    city varchar(255) NOT NULL,
    max_capacity integer NOT NULL,
    name varchar(255) NOT NULL
);

-- ============================
-- TABLE: events
-- ============================
CREATE TABLE public.events (
    id uuid PRIMARY KEY,
    current_attendees integer NOT NULL,
    description text,
    end_date timestamp(6) NOT NULL,
    max_capacity integer NOT NULL,
    organizer_id uuid NOT NULL,
    start_date timestamp(6) NOT NULL,
    status varchar(255) NOT NULL,
    title varchar(255) NOT NULL,
    venue_id uuid,
    CONSTRAINT events_status_check CHECK (status IN ('DRAFT','PUBLISHED','CANCELLED','COMPLETED'))
);

-- ============================
-- TABLE: ticket_types
-- ============================
CREATE TABLE public.ticket_types (
    id uuid PRIMARY KEY,
    name varchar(255) NOT NULL,
    price real NOT NULL,
    sold_quantity integer NOT NULL,
    total_quantity integer NOT NULL,
    event_id uuid NOT NULL
);

-- ============================
-- TABLE: orders
-- ============================
CREATE TABLE public.orders (
    id uuid PRIMARY KEY,
    created_at timestamp(6) NOT NULL,
    status varchar(255) NOT NULL,
    total_amount real NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT orders_status_check CHECK (status IN (
        'PENDING','LOCKED','PAYMENT_PENDING','CONFIRMED','CANCELLED','REFUNDED'
    ))
);

-- ============================
-- TABLE: registrations
-- ============================
CREATE TABLE public.registrations (
    id uuid PRIMARY KEY,
    event_id uuid NOT NULL,
    order_id uuid NOT NULL,
    qr_code text,
    registered_at timestamp(6) NOT NULL,
    status varchar(255) NOT NULL,
    user_id uuid NOT NULL,
    ticket_type_id uuid NOT NULL,
    CONSTRAINT registrations_status_check CHECK (status IN ('PENDING','CONFIRMED','CANCELLED'))
);

-- ============================
-- TABLE: check_ins
-- ============================
CREATE TABLE public.check_ins (
    id uuid PRIMARY KEY,
    checked_in_at timestamp(6) NOT NULL,
    is_valid boolean NOT NULL,
    staff_id uuid NOT NULL,
    registration_id uuid NOT NULL
);

-- ============================
-- TABLE: fraud_monitoring
-- ============================
CREATE TABLE public.fraud_monitoring (
    id uuid PRIMARY KEY,
    detected_at timestamp(6),
    order_id uuid,
    score_anomalie double precision,
    type_fraude varchar(255)
);

-- ============================
-- TABLE: soldout_prediction
-- ============================
CREATE TABLE public.soldout_prediction (
    id uuid PRIMARY KEY,
    created_at timestamp(6),
    event_id uuid,
    pred_time_to_soldout_min double precision,
    soldout_time_pred timestamp(6),
    tickets_cumules integer,
    "timestamp" timestamp(6),
    velocity_tpm double precision
);

-- ============================
-- TABLE: user_segmentation
-- ============================
CREATE TABLE public.user_segmentation (
    id uuid PRIMARY KEY,
    cluster integer,
    created_at timestamp(6),
    delta25 double precision,
    delta75 double precision,
    delta_min double precision,
    delta_moyen double precision,
    montant_moyen double precision,
    montant_total double precision,
    nb_commandes integer,
    nb_tickets_moyen double precision,
    nb_tickets_total integer,
    user_id uuid
);

-- ============================================================
-- FOREIGN KEYS (reconstruites proprement)
-- ============================================================

-- events
ALTER TABLE public.events
    ADD CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES public.users(id);

ALTER TABLE public.events
    ADD CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES public.venues(id);

-- ticket_types
ALTER TABLE public.ticket_types
    ADD CONSTRAINT fk_tickettypes_event FOREIGN KEY (event_id) REFERENCES public.events(id);

-- orders
ALTER TABLE public.orders
    ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES public.users(id);

-- registrations
ALTER TABLE public.registrations
    ADD CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES public.events(id);

ALTER TABLE public.registrations
    ADD CONSTRAINT fk_reg_order FOREIGN KEY (order_id) REFERENCES public.orders(id);

ALTER TABLE public.registrations
    ADD CONSTRAINT fk_reg_user FOREIGN KEY (user_id) REFERENCES public.users(id);

ALTER TABLE public.registrations
    ADD CONSTRAINT fk_reg_ticket FOREIGN KEY (ticket_type_id) REFERENCES public.ticket_types(id);

-- check_ins
ALTER TABLE public.check_ins
    ADD CONSTRAINT fk_checkins_registration FOREIGN KEY (registration_id) REFERENCES public.registrations(id);

ALTER TABLE public.check_ins
    ADD CONSTRAINT fk_checkins_staff FOREIGN KEY (staff_id) REFERENCES public.users(id);

-- fraud_monitoring
ALTER TABLE public.fraud_monitoring
    ADD CONSTRAINT fk_fraud_order FOREIGN KEY (order_id) REFERENCES public.orders(id);

-- soldout_prediction
ALTER TABLE public.soldout_prediction
    ADD CONSTRAINT fk_soldout_event FOREIGN KEY (event_id) REFERENCES public.events(id);

-- user_segmentation
ALTER TABLE public.user_segmentation
    ADD CONSTRAINT fk_seg_user FOREIGN KEY (user_id) REFERENCES public.users(id);
