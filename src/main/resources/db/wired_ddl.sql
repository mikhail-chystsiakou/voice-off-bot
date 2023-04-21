CREATE TABLE IF NOT EXISTS users
(
    user_id bigint NOT NULL ,
    username varchar,
    first_name varchar,
    last_name varchar,
    registered_date timestamp without time zone,
    chat_id bigint,
    time_zone bigint default 300,
    feedback_mode_allowed bool default false,
    feedback_mode_enabled bool default false,
    reply_mode_followee_id bigint,
    reply_mode_message_id bigint,
    notifications int default 0,

    CONSTRAINT users_pkey PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS user_audios
(
    user_id bigint,
    file_id varchar,
    file_order_number serial,
    duration bigint,
    message_id bigint,
    description varchar,
    file_size bigint, -- in bytes
    recording_timestamp timestamp with time zone default current_timestamp,
    pull_count bigint default 0,
    ok_message_id bigint,
    reply_to_message_id bigint,

    constraint audios_pk primary key (user_id, file_order_number)
);
CREATE INDEX user_audios_timestamp_index ON user_audios (user_id, recording_timestamp);

CREATE TABLE IF NOT EXISTS follow_requests
(
    user_id bigint,
    followee_id bigint,
    latest_request_timestamp timestamp with time zone default current_timestamp,

    constraint follow_requests_pk primary key (user_id, followee_id),
    constraint follow_requests_fk FOREIGN KEY(user_id) REFERENCES users (user_id) on delete cascade
);

CREATE TABLE IF NOT EXISTS user_subscriptions
(
    user_id bigint,
    followee_id bigint,
    last_pull_timestamp timestamp with time zone,
    last_reply_pull_timestamp timestamp with time zone,

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
    pull_timestamp      timestamp with time zone,
    start_timestamp     timestamp with time zone,
    end_before_upload_timestamp       timestamp with time zone,
    end_timestamp       timestamp with time zone,
    processing_time_millis bigint,
    file_size          bigint,
    processing_time     interval
);

CREATE INDEX pull_stats_index ON pull_stats (user_id, followee_id, pull_timestamp);

create table user_feedbacks (
    user_id bigint,
    message_id bigint,
    text varchar,
    file_id varchar,
    recording_timestamp timestamp with time zone default current_timestamp,

    primary key (user_id, message_id)
);

CREATE TABLE IF NOT EXISTS users_notifications
(
    user_id bigint,
    estimated_time time,

    constraint users_notifications_fk FOREIGN KEY(user_id) REFERENCES users (user_id) on delete cascade
);

create table user_replies (
  user_id bigint,
  subscriber_id bigint,
  last_pull_timestamp timestamp with time zone,

  constraint replies_pk primary key (user_id, subscriber_id)
);

create table pull_messages (
   followee_id bigint,
   pull_message_id bigint primary key,
   orig_message_ids varchar
);
