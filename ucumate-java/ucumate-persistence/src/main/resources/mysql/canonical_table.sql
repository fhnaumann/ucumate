CREATE TABLE IF NOT EXISTS ucumate_canonical (
    `unit_key` VARCHAR(255) PRIMARY KEY,
    `magnitude` TEXT NOT NULL,
    `cfPrefix` TEXT NOT NULL,
    `term` TEXT NOT NULL,
    `special` BOOLEAN NOT NULL,
    `specialName` TEXT,
    `specialUnit` TEXT,
    `specialValue` TEXT
    );
