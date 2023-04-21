CREATE TABLE IF NOT EXISTS users_notifications
(
    user_id bigint,
    estimated_time time,

    constraint users_notifications_fk FOREIGN KEY(user_id) REFERENCES users (user_id) on delete cascade
);

select message_id, reply_to_message_id, recording_timestamp from user_audios order by recording_timestamp desc;

alter table user_replies rename column user_message_id to message_id;

select * from user_replies;


alter table user_replies drop column reply_to_message_id;-- bigint;

create table user_replies (
                              user_id bigint,
                              message_id bigint,
                              subscriber_id bigint,
                              last_pull_timestamp timestamp with time zone,

                              constraint replies_pk primary key (user_id, user_message_id, subscriber_id)
);

alter table user_audios alter column pull_count set default 0;

select * from user_replies;
select * from user_audios where reply_to_message_id is not null;
select * from user_subscriptions where user_id = 245924084;

-- get reply last time
-- если есть реплай, то вернуть его пул таймстамп (0 или реальный)
SELECT us.followee_id,
       ua_subscriber.reply_to_message_id,
       ua_subscriber.message_id,
       min(coalesce(ur.last_pull_timestamp, to_timestamp(0)))
FROM
    user_subscriptions us,
    user_audios ua_author,
    user_audios ua_subscriber
        left join user_replies ur
                  on ur.user_id = us.followee_id
                      and ur.subscriber_id = us.user_id
                      and ur.message_id = ua_author.message_id
WHERE
        us.followee_id = 1113718747
  and ua_author.user_id = us.followee_id
  and ua_subscriber.user_id = us.user_id
  and ua_subscriber.reply_to_message_id = ua_author.message_id
  and ur.message_id = ua_subscriber.reply_to_message_id
  and (ua_subscriber.recording_timestamp > ur.last_pull_timestamp or ur.last_pull_timestamp is null)
group by us.followee_id, ua_subscriber.reply_to_message_id, ua_subscriber.message_id
having count(1) >= 1
limit 1;

select 1;

SELECT us.followee_id,
       ua_subscriber.reply_to_message_id,
       ua_subscriber.message_id,
       min(coalesce(ur.last_pull_timestamp, to_timestamp(0)))
FROM
    user_subscriptions us
        join user_audios ua_author on ua_author.user_id = us.followee_id
        join user_audios ua_subscriber
             on ua_subscriber.user_id = us.user_id
                 and ua_subscriber.reply_to_message_id = ua_author.message_id
        left join user_replies ur
                  on ur.user_id = us.followee_id
                      and ur.subscriber_id = us.user_id
                      and ur.message_id = ua_author.message_id
                      and (ua_subscriber.recording_timestamp > ur.last_pull_timestamp or ur.last_pull_timestamp is null)
WHERE
        us.followee_id = 1113718747
group by us.followee_id, ua_subscriber.reply_to_message_id, ua_subscriber.message_id
having count(1) >= 1
limit 1;

select * from user_audios where reply_to_message_id is not null; -- 245924084
select * from user_audios where message_id = 1991;
select * from user_audios order by recording_timestamp desc;

create table delivered_audios (
                                  orig_message_id bigint, -- original author message
                                  subscriber_id bigint,
                                  delivery_message_id bigint, -- message for subscripber from author
);


create table user_replies (
                              author_id bigint,

                              orig_message_id bigint, -- original author message
                              delivered_message_id bigint, -- message for subscripber from author
                              reply_to_message_id bigint, -- reference to original author message from subscriber reply


);

-- new message: