CREATE TABLE feedback (
    id          INTEGER PRIMARY KEY,
    job_id      INTEGER NOT NULL UNIQUE REFERENCES jobs(id),
    rating      INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT    CHECK (comment IS NULL OR length(comment) <= 500),
    created_at  TEXT    NOT NULL
);
