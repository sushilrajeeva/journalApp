-- Add the foreign key now that 'users' (V3) exists
ALTER TABLE journals
  ADD CONSTRAINT fk_journals_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
