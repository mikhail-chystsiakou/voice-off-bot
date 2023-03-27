CREATE TABLE IF NOT EXISTS folow_requests
(
    user_id bigint,
    folowee_id bigint
);

CREATE TABLE IF NOT EXISTS user_subscription
(
    user_id bigint,
    folowee_id bigint,
    last_pull_timestamp timestamp without time zone
);

CREATE TABLE IF NOT EXISTS users
(
    user_id bigint NOT NULL,
    registered_date timestamp without time zone,
    chat_id bigint,
    CONSTRAINT users_pkey PRIMARY KEY (user_id)
);