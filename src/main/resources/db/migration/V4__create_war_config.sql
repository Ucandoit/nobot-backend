CREATE TABLE war_config (
    login VARCHAR(32) NOT NULL,
    line integer NOT NULL,
    fp BOOLEAN NOT NULL,
    enabled BOOLEAN NOT NULL,
    CONSTRAINT war_config_pk PRIMARY KEY (login)
)