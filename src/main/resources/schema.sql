CREATE TABLE IF NOT EXISTS language (
    id INTEGER PRIMARY KEY,
    title TEXT NOT NULL UNIQUE,
    -- Noun case and no. point back to language; insert language first, then cases/no.s, then update these.
    head_case_id INTEGER REFERENCES noun_case(id),
    head_no_id INTEGER REFERENCES noun_no(id)
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
    ordinal INTEGER NOT NULL,
    UNIQUE (language_id, title)
);

CREATE TABLE IF NOT EXISTS noun_gender (
    id INTEGER PRIMARY KEY,
    language_id INTEGER NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    UNIQUE (language_id, title)
);

CREATE TABLE IF NOT EXISTS noun_declension (
    id INTEGER PRIMARY KEY,
    language_id INTEGER NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    ordinal INTEGER NOT NULL,
    UNIQUE (language_id, title)
);

-- Only noun identity and gloss.
CREATE TABLE IF NOT EXISTS noun (
    id INTEGER PRIMARY KEY,
    language_id INTEGER NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    gloss TEXT,
    gender_id INTEGER NOT NULL REFERENCES noun_gender(id),
    declension_id INTEGER NOT NULL REFERENCES noun_declension(id)
);

-- All realisations of a given noun, subject to case, etc.
CREATE TABLE IF NOT EXISTS inflection (
    id INTEGER PRIMARY KEY,
    noun_id INTEGER NOT NULL REFERENCES noun(id) ON DELETE CASCADE,
    case_id INTEGER NOT NULL REFERENCES noun_case(id),
    no_id INTEGER NOT NULL REFERENCES noun_no(id),
    spelling TEXT NOT NULL,
    UNIQUE (noun_id, case_id, no_id)
);

CREATE TABLE IF NOT EXISTS sentence (
    id INTEGER PRIMARY KEY,
    noun_id INTEGER NOT NULL REFERENCES noun(id) ON DELETE CASCADE,
    sentence TEXT NOT NULL,
    gloss TEXT,
    -- Indicate the inflection the noun appears in for this sentence.
    inflection_id INTEGER REFERENCES inflection(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS revision (
    id INTEGER PRIMARY KEY,
    inflection_id INTEGER NOT NULL REFERENCES inflection(id) ON DELETE CASCADE,
    interval_days INTEGER NOT NULL DEFAULT 0,
    ease_factor REAL NOT NULL DEFAULT 2.5, -- Default starting ease.
    repetitions INTEGER NOT NULL DEFAULT 0,
    due_date TEXT, -- If null, should be treated as unseen.
    last_seen TEXT
);

CREATE INDEX IF NOT EXISTS index_case_language ON noun_case(language_id);
CREATE INDEX IF NOT EXISTS index_no_language ON noun_no(language_id);
CREATE INDEX IF NOT EXISTS index_noun_language ON noun(language_id);
CREATE INDEX IF NOT EXISTS index_inflection_noun ON inflection(noun_id);
