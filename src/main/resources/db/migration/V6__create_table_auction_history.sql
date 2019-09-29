CREATE TABLE auction_history (
    id SERIAL PRIMARY KEY,
    login VARCHAR(32) NOT NULL REFERENCES account (login),
    card_rarity VARCHAR(32) NOT NULL,
    card_name VARCHAR(255) NOT NULL,
    card_price INTEGER NOT NULL,
    snipe_time TIMESTAMP WITH TIME ZONE NOT NULL,
    bought BOOLEAN
)