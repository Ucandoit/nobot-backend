CREATE TABLE draw_history (
    id SERIAL PRIMARY KEY,
    login VARCHAR(32) NOT NULL REFERENCES account (login),
    draw_type VARCHAR(32) NOT NULL,
    card_rarity VARCHAR(32) NOT NULL,
    card_name VARCHAR(255) NOT NULL,
    draw_time TIMESTAMP WITH TIME ZONE NOT NULL
)