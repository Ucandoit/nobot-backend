CREATE TABLE task (
    id SERIAL PRIMARY KEY,
    login VARCHAR(32) NOT NULL REFERENCES account (login),
    task_type VARCHAR(32) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE,
    stop_time TIMESTAMP WITH TIME ZONE,
    repeat INTEGER
)