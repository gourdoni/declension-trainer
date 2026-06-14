CREATE TABLE IF NOT EXISTS language (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS noun_case (
    id INTEGER PRIMARY KEY,
    language_id INTEGER NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    ordinal INTEGER NOT NULL,
    UNIQUE (language_id, title)
);

CREATE TABLE IF NOT EXISTS noun_no (
    id INTEGER PRIMARY KEY,
    language_id INTEGER NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    ordinal INTEGER NOT NULL, -- ?
    UNIQUE (language_id, title)
);

CREATE TABLE IF NOT EXISTS gender (
    id INTEGER PRIMARY KEY,
    language_id INTEGER NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    UNIQUE (language_id, title)
);
