CREATE TABLE IF NOT EXISTS users
(
    user_id bigint NOT NULL ,
    username varchar,
    first_name varchar,
    last_name varchar,
    registered_date timestamp without time zone,
    chat_id bigint,

    CONSTRAINT users_pkey PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS user_audios
(
    user_id bigint,
    file_id varchar,
    file_order_number serial,
    duration bigint,
    message_id bigint,
    recording_timestamp timestamp with time zone default current_timestamp,

    constraint audios_pk primary key (user_id, file_order_number)
);
CREATE INDEX user_audios_timestamp_index ON user_audios (user_id, recording_timestamp);

CREATE TABLE IF NOT EXISTS follow_requests
(
    user_id bigint,
    followee_id bigint,

    constraint follow_requests_pk primary key (user_id, followee_id),
    constraint follow_requests_fk FOREIGN KEY(user_id) REFERENCES users (user_id) on delete cascade
);

CREATE TABLE IF NOT EXISTS user_subscriptions
(
    user_id bigint,
    followee_id bigint,
    last_pull_timestamp timestamp with time zone,

    constraint user_subscriptions_pk primary key (user_id, followee_id),
    constraint subscriptions_to_user_fk FOREIGN KEY(user_id) REFERENCES users (user_id) on delete cascade,
    constraint subscriptions_to_followee_fk FOREIGN KEY(followee_id) REFERENCES users (user_id) on delete cascade
);

create table pull_stats
(
    pull_stat_id    serial,
    user_id             bigint,
    followee_id         bigint,
    last_pull_timestamp timestamp with time zone,
    pull_timestamp      timestamp with time zone
);

CREATE INDEX index_name ON pull_stats (user_id, followee_id, pull_timestamp);