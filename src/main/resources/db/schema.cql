CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    name TEXT,
    email TEXT
);

CREATE INDEX IF NOT EXISTS ON users (email);