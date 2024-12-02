ALTER TABLE registrations
    ADD COLUMN created_date_time TIMESTAMP   NOT NULL,
    ADD COLUMN status            VARCHAR(20) NOT NULL,
    ADD COLUMN rejected_reason   VARCHAR(255);