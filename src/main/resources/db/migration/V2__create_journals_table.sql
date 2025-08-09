CREATE TABLE IF NOT EXISTS journals (
  id               BIGSERIAL PRIMARY KEY,
  title            VARCHAR(255) NOT NULL,
  message          TEXT NOT NULL,
  created_at       TIMESTAMPTZ NOT NULL,
  last_modified_at TIMESTAMPTZ NOT NULL,
  user_id          BIGINT NOT NULL
);

-- Helpful indexes
CREATE INDEX IF NOT EXISTS idx_journals_created_at ON journals (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_journals_user_id     ON journals (user_id);