-- DEMO DATA ONLY. Fictional train numbers/timings so the engine can be tried end to end.
-- Replace with real timetable data (see tools/import_timetable.py).
INSERT INTO station (code, name) VALUES
 ('JP',   'Jaipur Jn'),
 ('NDLS', 'New Delhi'),
 ('KOTA', 'Kota Jn'),
 ('GAYA', 'Gaya Jn'),
 ('KQR',  'Koderma Jn');

INSERT INTO train (number, name, runs_on_mask) VALUES
 ('90001', 'Demo Pink City Express', 127),
 ('90002', 'Demo East Link',         127),
 ('90003', 'Demo Kota Passenger',     63),  -- not on Sunday
 ('90004', 'Demo Chambal Mail',      127),
 ('90005', 'Demo Gaya Shuttle',      127),
 ('90006', 'Demo Marudhar Night',    127),
 ('90007', 'Demo Gaya Evening',      127);

INSERT INTO train_stop (train_number, seq, station_code, arrival_min, departure_min, distance_km) VALUES
 ('90001', 1, 'JP',   NULL, 480,  0),
 ('90001', 2, 'NDLS', 810,  NULL, 308),
 ('90002', 1, 'NDLS', NULL, 900,  0),
 ('90002', 2, 'GAYA', 1770, 1775, 1000),
 ('90002', 3, 'KQR',  1845, NULL, 1095),
 ('90003', 1, 'JP',   NULL, 360,  0),
 ('90003', 2, 'KOTA', 690,  NULL, 250),
 ('90004', 1, 'KOTA', NULL, 780,  0),
 ('90004', 2, 'GAYA', 2160, NULL, 1100),
 ('90005', 1, 'GAYA', NULL, 810,  0),
 ('90005', 2, 'KQR',  900,  NULL, 95),
 ('90006', 1, 'JP',   NULL, 1200, 0),
 ('90006', 2, 'GAYA', 2520, NULL, 1230),
 ('90007', 1, 'GAYA', NULL, 1170, 0),
 ('90007', 2, 'KQR',  1260, NULL, 95);
