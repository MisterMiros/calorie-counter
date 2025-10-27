-- Functions to maintain search_vector for translation tables with locale-aware regconfig

-- Helper function: map locale to regconfig name
CREATE OR REPLACE FUNCTION map_locale_to_regconfig(loc TEXT)
RETURNS regconfig AS $$
BEGIN
  IF loc IS NULL THEN
    RETURN 'simple';
  END IF;
  CASE lower(loc)
    WHEN 'en' THEN RETURN 'english';
    WHEN 'english' THEN RETURN 'english';
    WHEN 'ru' THEN RETURN 'russian';
    WHEN 'russian' THEN RETURN 'russian';
    ELSE RETURN 'simple';
  END CASE;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Food translation trigger function
CREATE OR REPLACE FUNCTION trg_food_translation_tsvector()
RETURNS trigger AS $$
DECLARE
  cfg regconfig;
  txt TEXT;
BEGIN
  cfg := map_locale_to_regconfig(NEW.locale);
  txt := coalesce(NEW.name, '') || ' ' || coalesce(NEW.producer, '');
  NEW.search_vector := to_tsvector(cfg, txt);
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS food_translation_tsvector_biud ON food_translation;
CREATE TRIGGER food_translation_tsvector_biud
BEFORE INSERT OR UPDATE OF name, producer, locale
ON food_translation
FOR EACH ROW
EXECUTE FUNCTION trg_food_translation_tsvector();

-- Exercise translation trigger function
CREATE OR REPLACE FUNCTION trg_exercise_translation_tsvector()
RETURNS trigger AS $$
DECLARE
  cfg regconfig;
BEGIN
  cfg := map_locale_to_regconfig(NEW.locale);
  NEW.search_vector := to_tsvector(cfg, coalesce(NEW.name, ''));
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS exercise_translation_tsvector_biud ON exercise_translation;
CREATE TRIGGER exercise_translation_tsvector_biud
BEFORE INSERT OR UPDATE OF name, locale
ON exercise_translation
FOR EACH ROW
EXECUTE FUNCTION trg_exercise_translation_tsvector();
