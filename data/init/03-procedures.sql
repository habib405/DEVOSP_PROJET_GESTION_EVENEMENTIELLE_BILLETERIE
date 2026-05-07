-- ============================================================
-- PROCEDURES.SQL — Génération automatique des données massives
-- ============================================================

-- ============================================================
-- 1) Génération de 95 orders supplémentaires
-- ============================================================

DO $$
DECLARE
    i INT;
    order_id UUID;
BEGIN
    FOR i IN 6..100 LOOP
        order_id := gen_random_uuid();

        INSERT INTO public.orders (id, created_at, status, total_amount, user_id)
        VALUES (
            order_id,
            NOW() - (i || ' minutes')::interval,
            'CONFIRMED',
            round((random() * 80 + 10)::numeric, 2),
            '44444444-4444-4444-4444-444444444444'
        );
    END LOOP;
END $$;

-- ============================================================
-- 2) Génération de 300 registrations
-- ============================================================

DO $$
DECLARE
    i INT;
    reg_id UUID;
    order_id UUID;
    selected_event_id UUID;
    ticket_id UUID;
BEGIN
    FOR i IN 1..300 LOOP

        SELECT id INTO order_id
        FROM public.orders
        ORDER BY random()
        LIMIT 1;

        SELECT id, event_id INTO ticket_id, selected_event_id
        FROM public.ticket_types
        ORDER BY random()
        LIMIT 1;

        reg_id := gen_random_uuid();

        INSERT INTO public.registrations (
            id, event_id, order_id, qr_code, registered_at,
            status, user_id, ticket_type_id
        ) VALUES (
            reg_id,
            selected_event_id,
            order_id,
            'QR-' || i,
            NOW() - (i || ' minutes')::interval,
            'CONFIRMED',
            '44444444-4444-4444-4444-444444444444',
            ticket_id
        );
    END LOOP;
END $$;

-- ============================================================
-- 3) Génération de 30 check-ins
-- ============================================================

DO $$
DECLARE
    i INT;
    check_id UUID;
    reg_id UUID;
BEGIN
    FOR i IN 1..30 LOOP

        SELECT id INTO reg_id
        FROM public.registrations
        ORDER BY random()
        LIMIT 1;

        check_id := gen_random_uuid();

        INSERT INTO public.check_ins (
            id, checked_in_at, is_valid, staff_id, registration_id
        ) VALUES (
            check_id,
            NOW() - (i || ' minutes')::interval,
            TRUE,
            '33333333-3333-3333-3333-333333333333',
            reg_id
        );
    END LOOP;
END $$;

-- ============================================================
-- 4) Génération de 50 fraud_monitoring
-- ============================================================

DO $$
DECLARE
    i INT;
    fraud_id UUID;
    order_id UUID;
    fraud_types TEXT[] := ARRAY['MULTI_ACCOUNT', 'CARD_TESTING', 'SUSPICIOUS_BEHAVIOR'];
BEGIN
    FOR i IN 1..50 LOOP

        SELECT id INTO order_id
        FROM public.orders
        ORDER BY random()
        LIMIT 1;

        fraud_id := gen_random_uuid();

        INSERT INTO public.fraud_monitoring (
            id, detected_at, order_id, score_anomalie, type_fraude
        ) VALUES (
            fraud_id,
            NOW() - (i || ' hours')::interval,
            order_id,
            round((random() * 0.9 + 0.1)::numeric, 2),
            fraud_types[(random() * 2 + 1)::int]
        );
    END LOOP;
END $$;

-- ============================================================
-- 5) Génération de 50 soldout_prediction
-- ============================================================

DO $$
DECLARE
    i INT;
    pred_id UUID;
    event_id UUID;
BEGIN
    FOR i IN 1..50 LOOP

        SELECT id INTO event_id
        FROM public.events
        ORDER BY random()
        LIMIT 1;

        pred_id := gen_random_uuid();

        INSERT INTO public.soldout_prediction (
            id, created_at, event_id, pred_time_to_soldout_min,
            soldout_time_pred, tickets_cumules, "timestamp", velocity_tpm
        ) VALUES (
            pred_id,
            NOW() - (i || ' days')::interval,
            event_id,
            (random() * 300 + 30)::int,
            NOW() + ((random() * 5 + 1)::int || ' hours')::interval,
            (random() * 5000)::int,
            NOW() - (i || ' days')::interval,
            round((random() * 50 + 5)::numeric, 2)
        );
    END LOOP;
END $$;

-- ============================================================
-- 6) Génération de 50 user_segmentation
-- ============================================================

DO $$
DECLARE
    i INT;
    seg_id UUID;
    user_id UUID;
BEGIN
    FOR i IN 1..50 LOOP

        SELECT id INTO user_id
        FROM public.users
        ORDER BY random()
        LIMIT 1;

        seg_id := gen_random_uuid();

        INSERT INTO public.user_segmentation (
            id, user_id, cluster, created_at,
            montant_total, montant_moyen, nb_commandes,
            nb_tickets_total, nb_tickets_moyen,
            delta_min, delta25, delta75, delta_moyen
        ) VALUES (
            seg_id,
            user_id,
            (random() * 4 + 1)::int,
            NOW() - (i || ' days')::interval,
            round((random() * 500 + 50)::numeric, 2),
            round((random() * 80 + 10)::numeric, 2),
            (random() * 10 + 1)::int,
            (random() * 20 + 1)::int,
            round((random() * 5 + 1)::numeric, 2),
            round((random() * 2)::numeric, 2),
            round((random() * 5)::numeric, 2),
            round((random() * 10)::numeric, 2),
            round((random() * 4)::numeric, 2)
        );
    END LOOP;
END $$;
