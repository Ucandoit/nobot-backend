CREATE TABLE account (
    login VARCHAR(32) NOT NULL,
    name VARCHAR(32) NOT NULL,
    cookie VARCHAR(255) NOT NULL,
    expiration_date TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT account_pk PRIMARY KEY (login)
)