delete from user_audios where file_id like 'audio_%';

insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-40-06',
           to_timestamp('2023-04-09 20:33:10', 'YYYY-MM-DD HH24:MI:SS'),
           1644,
           1644,
           33868920);

insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-40-11',
           to_timestamp('2023-04-09 20:33:11', 'YYYY-MM-DD HH24:MI:SS'),
           1410,
           1410,
           29046872);

insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-40-23',
           to_timestamp('2023-04-09 20:33:12', 'YYYY-MM-DD HH24:MI:SS'),
           4423,
           4423,
           91131904);

insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-40-43',
           to_timestamp('2023-04-09 20:33:13', 'YYYY-MM-DD HH24:MI:SS'),
           2344,
           2344,
           48302928);


insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-40-47',
           to_timestamp('2023-04-09 20:33:14', 'YYYY-MM-DD HH24:MI:SS'),
           1736,
           1736,
           35778560);


insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-40-58',
           to_timestamp('2023-04-09 20:33:15', 'YYYY-MM-DD HH24:MI:SS'),
           1199,
           1199,
           24707072);

insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-41-02',
           to_timestamp('2023-04-09 20:33:16', 'YYYY-MM-DD HH24:MI:SS'),
           753,
           753,
           15519744);

insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-41-09',
           to_timestamp('2023-04-09 20:33:17', 'YYYY-MM-DD HH24:MI:SS'),
           1725,
           1725,
           35553280);


insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-40-55',
           to_timestamp('2023-04-09 20:33:18', 'YYYY-MM-DD HH24:MI:SS'),
           1241,
           1241,
           25581128);


insert into user_audios (user_id, file_id, recording_timestamp, duration, message_id, file_size)
VALUES (
           245924084,
           'audio_2023-04-09_22-39-51',
           to_timestamp('2023-04-09 20:33:19', 'YYYY-MM-DD HH24:MI:SS'),
           2434,
           2434,
           50147328);

select to_char(recording_timestamp, 'DD_HH24_MI_SS_MS_') || duration || '_' || file_id
from user_audios u where file_id like 'audio%' order by recording_timestamp desc;