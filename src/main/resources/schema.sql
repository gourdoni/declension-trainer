CREATE TABLE IF NOT EXISTS language (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL UNIQUE,
    -- Noun case and no. point back to language; insert language first, then cases/no.s, then update these.
    head_case_id INTEGER REFERENCES noun_case(id) ON DELETE CASCADE,
    head_no_id INTEGER REFERENCES noun_no(id) ON DELETE CASCADE,
);

CREATE TABLE IF NOT EXISTS noun_case (
    id INTEGER PRIMARY KEY,
    language_id INTEGER NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    ordinal INTEGER NOT NULL,
    -- Handle vestigial cases, etc. (e.g. Classical Latin locative).
    is_optional INTEGER NOT NULL DEFAULT 0,
    UNIQUE (language_id, title)
);

CREATE TABLE IF NOT EXISTS noun_no (
    id INTEGER PRIMARY KEY,
    language_id INTEGER NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    ordinal INTEGER NOT NULL, -- ?
    UNIQUE (language_id, title)
);

CREATE TABLE IF NOT EXISTS noun_gender (
    id INTEGER PRIMARY KEY,
    language_id INTEGER NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    UNIQUE (language_id, title)
);
