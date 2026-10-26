-- ETSTCC-13074 test fixtures (data only, test scope; applied to temporary SQLite databases).
-- Generic placeholder customer names, no personal data. Ids do not collide with seeded jobs (1-4).
INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES (101, 1, 'Test Customer A', '2026-01-10T09:00', '2026-01-10T10:00', 'COMPLETED');
INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES (102, 1, 'Test Customer B', '2026-01-11T09:00', '2026-01-11T10:00', 'COMPLETED');
INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES (103, 2, 'Test Customer C', '2026-01-12T09:00', '2026-01-12T10:00', 'COMPLETED');
INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES (104, 1, 'Test Customer D', '2026-01-13T09:00', '2026-01-13T10:00', 'SCHEDULED');
INSERT INTO jobs(id, technician_id, customer_name, start_time, end_time, status) VALUES (105, 1, 'Test Customer E', '2026-01-14T09:00', '2026-01-14T10:00', 'CANCELLED');
