CREATE TABLE card (
    id INTEGER NOT NULL PRIMARY KEY,
    number INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    real_name VARCHAR(255) NOT NULL,
    rarity VARCHAR(32) NOT NULL,
    property VARCHAR(32) NOT NULL,
    cost NUMERIC(2, 1) NOT NULL,
    military VARCHAR(32) NOT NULL,
    job VARCHAR(32) NOT NULL,
    star INTEGER DEFAULT 0,
    face_url VARCHAR(255),
    illust_url VARCHAR(255),
    initial_atk INTEGER,
    initial_def INTEGER,
    initial_spd INTEGER,
    initial_vir INTEGER,
    initial_stg INTEGER,
    final_atk INTEGER,
    final_def INTEGER,
    final_spd INTEGER,
    final_vir INTEGER,
    final_stg INTEGER,
    personality VARCHAR(255),
    slogan VARCHAR(255),
    history TEXT,
    train_skills VARCHAR(255)
)