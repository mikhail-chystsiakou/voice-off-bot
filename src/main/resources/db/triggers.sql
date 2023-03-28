drop trigger if exists increment_audio_number_within_user on user_audios;
drop function if exists increment_audio_number_within_user;

CREATE OR REPLACE FUNCTION increment_audio_number_within_user()
    RETURNS TRIGGER AS $$
DECLARE
    generated_number INTEGER;
BEGIN
    SELECT file_order_number INTO generated_number
    FROM user_audios
    WHERE user_id = NEW.user_id
    ORDER BY file_order_number DESC
    LIMIT 1;

    IF generated_number IS NOT NULL THEN
        NEW.file_order_number = generated_number + 1;
    ELSE
        NEW.file_order_number = 1;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER increment_audio_number_within_user
    BEFORE INSERT ON user_audios
    FOR EACH ROW
EXECUTE FUNCTION increment_audio_number_within_user();