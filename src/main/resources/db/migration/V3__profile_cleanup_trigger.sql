CREATE OR REPLACE FUNCTION cleanup_orphan_profiles()
RETURNS TRIGGER AS $$
BEGIN
DELETE FROM profiles
WHERE user_id NOT IN (SELECT id FROM users);
RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cleanup_profiles
    AFTER DELETE ON users
    FOR EACH STATEMENT
    EXECUTE FUNCTION cleanup_orphan_profiles();