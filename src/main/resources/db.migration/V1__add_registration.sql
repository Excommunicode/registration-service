CREATE TABLE IF NOT EXISTS registrations
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    phone    VARCHAR(255) NOT NULL,
    event_id BIGINT       NOT NULL,
    number   INT          NOT NULL
);
