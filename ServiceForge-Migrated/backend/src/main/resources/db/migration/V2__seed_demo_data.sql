-- Demo seed data for ServiceForge-Migrated (Features 1–3)

INSERT OR IGNORE INTO technicians (id, name, active, age, area_served, vehicle_make, vehicle_model, vehicle_year, skills_csv)
VALUES
  (1, 'Alex Rivera', 1, 32, 'North', 'Ford', 'Transit', 2021, 'HVAC,Electrical'),
  (2, 'Sam Patel', 1, 41, 'South', 'Mercedes', 'Sprinter', 2020, 'Plumbing,Appliance');

-- Jobs in the current week window (UTC) for schedule view
INSERT OR IGNORE INTO jobs (id, technician_id, customer_name, start_time, end_time)
VALUES
  (1001, 1, 'Acme Corp', '2026-10-12T09:00:00Z', '2026-10-12T10:00:00Z'),
  (1002, 1, 'Globex',    '2026-10-13T13:00:00Z', '2026-10-13T14:30:00Z'),
  (1003, 2, 'Initech',   '2026-10-14T08:30:00Z', '2026-10-14T09:15:00Z');

INSERT OR IGNORE INTO parts (sku, name, description, active, orderable, quantity_on_hand)
VALUES
  ('P-100', 'Air Filter', 'Standard air filter', 1, 1, 25),
  ('P-101', 'Thermostat', 'Programmable thermostat', 1, 1, 10),
  ('P-102', 'Fuse', '15A fuse', 1, 1, 100),
  ('P-103', 'Legacy Valve', 'Deprecated valve', 0, 0, 0),
  ('P-104', 'Special Order Pump', 'Requires special order', 1, 0, 0);
