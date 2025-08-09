CREATE TABLE IF NOT EXISTS journals (
  id               BIGSERIAL PRIMARY KEY,
  title            VARCHAR(255) NOT NULL,
  message          TEXT NOT NULL,
  created_at       TIMESTAMPTZ NOT NULL,
  last_modified_at TIMESTAMPTZ NOT NULL
);

-- Helpful index for listing by recency (optional)
CREATE INDEX IF NOT EXISTS idx_journals_created_at ON journals (created_at DESC);
