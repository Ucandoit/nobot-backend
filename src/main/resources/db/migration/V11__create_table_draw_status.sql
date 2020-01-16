CREATE TABLE draw_status (
    login VARCHAR(32) NOT NULL PRIMARY KEY REFERENCES account (login),
    fu_number INTEGER NOT NULL,
    ji_number INTEGER NOT NULL,
    fukubiki_number INTEGER NOT NULL
)