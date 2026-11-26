CREATE TABLE station (
    code VARCHAR(8) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    lat  DOUBLE PRECISION,
    lng  DOUBLE PRECISION
);

-- runs_on_mask: bit0 = Monday ... bit6 = Sunday (127 = daily)
CREATE TABLE train (
    number       VARCHAR(10) PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    runs_on_mask INT NOT NULL DEFAULT 127
);

-- Times are minutes since midnight of the train's ORIGIN day.
-- A stop after midnight simply has a value >= 1440 (e.g. 06:45 next day = 1440 + 405).
CREATE TABLE train_stop (
    train_number VARCHAR(10) NOT NULL REFERENCES train(number) ON DELETE CASCADE,
    seq          INT NOT NULL,
    station_code VARCHAR(8) NOT NULL REFERENCES station(code),
    arrival_min   INT,
    departure_min INT,
    distance_km   INT NOT NULL DEFAULT 0,
    PRIMARY KEY (train_number, seq)
);
CREATE INDEX idx_train_stop_station ON train_stop(station_code);
