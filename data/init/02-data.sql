-- ============================================================
-- DATA.SQL — Données fixes initiales
-- ============================================================

-- ============================
-- USERS
-- ============================
INSERT INTO public.users (id, created_at, email, first_name, last_name, password_hash, role) VALUES
('11111111-1111-1111-1111-111111111111', NOW(), 'admin@example.com', 'Admin', 'System', 'hashedpass', 'ADMIN'),
('22222222-2222-2222-2222-222222222222', NOW(), 'organizer@example.com', 'Olivia', 'Organizer', 'hashedpass', 'ORGANIZER'),
('33333333-3333-3333-3333-333333333333', NOW(), 'staff@example.com', 'Sam', 'Staff', 'hashedpass', 'STAFF'),
('44444444-4444-4444-4444-444444444444', NOW(), 'attendee@example.com', 'Alice', 'Attendee', 'hashedpass', 'ATTENDEE');

-- ============================
-- VENUES
-- ============================
INSERT INTO public.venues (id, address, city, max_capacity, name) VALUES
('aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1', '123 Main St', 'Paris', 500, 'Grand Hall'),
('aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2', '45 Rue de Lyon', 'Marseille', 300, 'Mediterranee Center'),
('aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3', '78 Avenue Corse', 'Bastia', 200, 'Corsica Arena');

-- ============================
-- EVENTS
-- ============================
INSERT INTO public.events (
    id, current_attendees, description, end_date, max_capacity,
    organizer_id, start_date, status, title, venue_id
) VALUES
('bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1', 0, 'Tech conference', '2026-06-10 18:00', 500,
 '22222222-2222-2222-2222-222222222222', '2026-06-10 09:00', 'PUBLISHED', 'Tech Summit 2026',
 'aaaaaaa1-aaaa-aaaa-aaaa-aaaaaaaaaaa1'),

('bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbb2', 0, 'Music festival', '2026-07-20 23:00', 300,
 '22222222-2222-2222-2222-222222222222', '2026-07-20 14:00', 'PUBLISHED', 'Summer Beats',
 'aaaaaaa2-aaaa-aaaa-aaaa-aaaaaaaaaaa2'),

('bbbbbbb3-bbbb-bbbb-bbbb-bbbbbbbbbbb3', 0, 'Startup meetup', '2026-05-15 17:00', 200,
 '22222222-2222-2222-2222-222222222222', '2026-05-15 10:00', 'PUBLISHED', 'Corsica Innov Day',
 'aaaaaaa3-aaaa-aaaa-aaaa-aaaaaaaaaaa3');

-- ============================
-- TICKET TYPES
-- ============================
INSERT INTO public.ticket_types (id, name, price, sold_quantity, total_quantity, event_id) VALUES
('ccccccc1-cccc-cccc-cccc-ccccccccccc1', 'Standard', 29.99, 0, 300, 'bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1'),
('ccccccc2-cccc-cccc-cccc-ccccccccccc2', 'VIP', 79.99, 0, 200, 'bbbbbbb1-bbbb-bbbb-bbbb-bbbbbbbbbbb1'),

('ccccccc3-cccc-cccc-cccc-ccccccccccc3', 'General', 19.99, 0, 200, 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbb2'),
('ccccccc4-cccc-cccc-cccc-ccccccccccc4', 'Backstage', 99.99, 0, 100, 'bbbbbbb2-bbbb-bbbb-bbbb-bbbbbbbbbbb2'),

('ccccccc5-cccc-cccc-cccc-ccccccccccc5', 'Entry', 9.99, 0, 150, 'bbbbbbb3-bbbb-bbbb-bbbb-bbbbbbbbbbb3'),
('ccccccc6-cccc-cccc-cccc-ccccccccccc6', 'Premium', 39.99, 0, 50, 'bbbbbbb3-bbbb-bbbb-bbbb-bbbbbbbbbbb3');

-- ============================
-- ORDERS (100 lignes)
-- ============================

-- Pour garder le fichier lisible, on génère les 95 autres orders automatiquement dans procedures.sql
INSERT INTO public.orders (id, created_at, status, total_amount, user_id) VALUES
    ('00000001-0000-0000-0000-000000000001', NOW(), 'CONFIRMED', 29.99, '44444444-4444-4444-4444-444444444444'),
    ('00000002-0000-0000-0000-000000000002', NOW(), 'CONFIRMED', 59.99, '44444444-4444-4444-4444-444444444444'),
    ('00000003-0000-0000-0000-000000000003', NOW(), 'CONFIRMED', 49.99, '44444444-4444-4444-4444-444444444444'),
    ('00000004-0000-0000-0000-000000000004', NOW(), 'CONFIRMED', 19.99, '44444444-4444-4444-4444-444444444444'),
    ('00000005-0000-0000-0000-000000000005', NOW(), 'CONFIRMED', 35.00, '44444444-4444-4444-4444-444444444444');
