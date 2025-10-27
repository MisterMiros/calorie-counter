-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- auth_user integrates with Spring Security
CREATE TABLE IF NOT EXISTS auth_user (
  id UUID PRIMARY KEY,
  username TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  is_admin BOOLEAN NOT NULL DEFAULT FALSE,
  is_system BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- app_user has 1:1 with auth_user
CREATE TABLE IF NOT EXISTS app_user (
  id UUID PRIMARY KEY,
  auth_user_id UUID NOT NULL UNIQUE REFERENCES auth_user(id),
  name TEXT,
  gender TEXT,
  date_of_birth DATE,
  current_weight_kg NUMERIC(6,2),
  height_cm NUMERIC(5,2),
  activity_level TEXT NOT NULL,
  daily_calorie_goal_kcal NUMERIC(6,1),
  timezone TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS weight_history (
  id UUID PRIMARY KEY,
  app_user_id UUID NOT NULL REFERENCES app_user(id),
  ts TIMESTAMPTZ NOT NULL,
  weight_kg NUMERIC(6,2) NOT NULL,
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- food catalog
CREATE TABLE IF NOT EXISTS food (
  id UUID PRIMARY KEY,
  owner_id UUID NOT NULL REFERENCES auth_user(id),
  type TEXT NOT NULL,
  density_g_per_ml NUMERIC(10,5),
  pack_g NUMERIC(10,3),
  item_g NUMERIC(10,3),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS food_macros (
  food_id UUID PRIMARY KEY REFERENCES food(id),
  protein_g NUMERIC(8,3) NOT NULL,
  fat_g NUMERIC(8,3) NOT NULL,
  carb_g NUMERIC(8,3) NOT NULL
);

CREATE TABLE IF NOT EXISTS food_translation (
  id UUID PRIMARY KEY,
  food_id UUID NOT NULL REFERENCES food(id),
  locale TEXT NOT NULL,
  name TEXT NOT NULL,
  producer TEXT,
  search_vector tsvector,
  UNIQUE(food_id, locale)
);

CREATE TABLE IF NOT EXISTS food_tag (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS food_tags (
  id UUID PRIMARY KEY,
  food_id UUID NOT NULL REFERENCES food(id),
  tag_id UUID NOT NULL REFERENCES food_tag(id)
);

-- exercise catalog
CREATE TABLE IF NOT EXISTS exercise (
  id UUID PRIMARY KEY,
  owner_id UUID NOT NULL REFERENCES auth_user(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS exercise_translation (
  id UUID PRIMARY KEY,
  exercise_id UUID NOT NULL REFERENCES exercise(id),
  locale TEXT NOT NULL,
  name TEXT NOT NULL,
  search_vector tsvector,
  UNIQUE(exercise_id, locale)
);

CREATE TABLE IF NOT EXISTS exercise_tag (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS exercise_tags (
  id UUID PRIMARY KEY,
  exercise_id UUID NOT NULL REFERENCES exercise(id),
  tag_id UUID NOT NULL REFERENCES exercise_tag(id)
);

-- muscles
CREATE TABLE IF NOT EXISTS muscle (
  id UUID PRIMARY KEY,
  name TEXT NOT NULL UNIQUE,
  group_name TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS exercise_muscles (
  id UUID PRIMARY KEY,
  exercise_id UUID NOT NULL REFERENCES exercise(id),
  muscle_id UUID NOT NULL REFERENCES muscle(id)
);

-- diary
CREATE TABLE IF NOT EXISTS diary (
  id UUID PRIMARY KEY,
  owner_id UUID NOT NULL REFERENCES auth_user(id),
  date DATE NOT NULL,
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ,
  UNIQUE(owner_id, date)
);

CREATE TABLE IF NOT EXISTS diary_entry (
  id UUID PRIMARY KEY,
  diary_id UUID NOT NULL REFERENCES diary(id) ON DELETE CASCADE,
  food_id UUID NOT NULL REFERENCES food(id),
  amount DECIMAL(12,4) NOT NULL,
  unit TEXT NOT NULL,
  meal TEXT NOT NULL,
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- training logs
CREATE TABLE IF NOT EXISTS training_log (
  id UUID PRIMARY KEY,
  owner_id UUID NOT NULL REFERENCES auth_user(id),
  date DATE NOT NULL,
  comment TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at TIMESTAMPTZ,
  UNIQUE(owner_id, date)
);

CREATE TABLE IF NOT EXISTS training_log_entry (
  id UUID PRIMARY KEY,
  training_log_id UUID NOT NULL REFERENCES training_log(id) ON DELETE CASCADE,
  exercise_id UUID NOT NULL REFERENCES exercise(id),
  duration_min DECIMAL(8,2),
  repetitions INT,
  weight_kg DECIMAL(8,3),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- indexes for performance
CREATE INDEX IF NOT EXISTS idx_food_owner_deleted ON food(owner_id, deleted_at);
CREATE INDEX IF NOT EXISTS idx_exercise_owner_deleted ON exercise(owner_id, deleted_at);
CREATE INDEX IF NOT EXISTS idx_diary_owner_date ON diary(owner_id, date);
CREATE INDEX IF NOT EXISTS idx_diary_deleted ON diary(deleted_at);
CREATE INDEX IF NOT EXISTS idx_training_log_owner_date ON training_log(owner_id, date);
CREATE INDEX IF NOT EXISTS idx_training_log_deleted ON training_log(deleted_at);

-- FTS and trigram indexes
CREATE INDEX IF NOT EXISTS idx_food_translation_search ON food_translation USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_food_translation_name_trgm ON food_translation USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_food_translation_producer_trgm ON food_translation USING GIN (producer gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_exercise_translation_search ON exercise_translation USING GIN (search_vector);
CREATE INDEX IF NOT EXISTS idx_exercise_translation_name_trgm ON exercise_translation USING GIN (name gin_trgm_ops);

-- Seed SHARED system user (owner of shared entries)
INSERT INTO auth_user(id, username, password_hash, enabled, is_admin, is_system)
VALUES ('7f761e74-2297-4b79-92f2-55307de133a4', 'SHARED', '-', FALSE, FALSE, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Seed initial muscles with groups
INSERT INTO muscle(id, name, group_name) VALUES
  (gen_random_uuid(), 'rectus abdominis', 'core'),
  (gen_random_uuid(), 'obliques', 'core'),
  (gen_random_uuid(), 'transverse abdominis', 'core'),
  (gen_random_uuid(), 'latissimus dorsi', 'back'),
  (gen_random_uuid(), 'erector spinae', 'back'),
  (gen_random_uuid(), 'trapezius', 'back'),
  (gen_random_uuid(), 'quadriceps', 'legs'),
  (gen_random_uuid(), 'hamstrings', 'legs'),
  (gen_random_uuid(), 'glutes', 'legs'),
  (gen_random_uuid(), 'calves', 'legs'),
  (gen_random_uuid(), 'pectoralis major', 'chest'),
  (gen_random_uuid(), 'deltoids', 'shoulders'),
  (gen_random_uuid(), 'biceps brachii', 'arms'),
  (gen_random_uuid(), 'triceps brachii', 'arms'),
  (gen_random_uuid(), 'forearms', 'arms')
ON CONFLICT DO NOTHING;