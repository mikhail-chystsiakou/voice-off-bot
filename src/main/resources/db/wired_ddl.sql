CREATE TABLE IF NOT EXISTS user_audios
(
    user_id bigint,
    file_id varchar,
    file_order_number serial,
    recording_timestamp timestamp with time zone default current_timestamp,

    constraint audios_pk primary key (user_id, file_order_number)
);

CREATE TABLE IF NOT EXISTS follow_requests
(
    user_id bigint,
    followee_id bigint,

    constraint follow_requests_pk primary key (user_id, followee_id)
);

CREATE TABLE IF NOT EXISTS user_subscriptions
(
    user_id bigint,
    followee_id bigint,
    last_pull_timestamp timestamp with time zone,

    constraint pk_user_subscriptions_pk primary key (user_id, followee_id)
);

CREATE TABLE IF NOT EXISTS users
(
    user_id bigint NOT NULL ,
    registered_date timestamp without time zone,
    chat_id bigint,

    CONSTRAINT users_pkey PRIMARY KEY (user_id)
);