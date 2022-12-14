CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name  VARCHAR(255)                            NOT NULL,
    email VARCHAR(255)                            NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    description  VARCHAR(5000),
    requester_id BIGINT REFERENCES users (id),
    created   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS items
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(5000),
    available   BOOLEAN,
    owner_id    BIGINT REFERENCES users (id),
    request_id  BIGINT REFERENCES requests (id),
    CONSTRAINT uq_owner_item_name UNIQUE (owner_id, name)
);

CREATE TABLE IF NOT EXISTS bookings
(
    id         BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    start_date TIMESTAMP WITHOUT TIME ZONE,
    end_date   TIMESTAMP WITHOUT TIME ZONE,
    item_id    BIGINT REFERENCES items (id),
    booker_id  BIGINT REFERENCES users (id),
    status     VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS comments
(
    id        BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL PRIMARY KEY,
    text      VARCHAR(10000),
    item_id   BIGINT REFERENCES items (id),
    author_id BIGINT REFERENCES users (id),
    created   TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
