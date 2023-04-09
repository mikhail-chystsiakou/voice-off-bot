package org.example.service;

import org.example.config.BotConfig;
import org.example.ffmpeg.FFMPEG;
import org.example.ffmpeg.FFMPEGResult;
import org.example.util.ExecuteFunction;
import org.example.config.DataSourceConfig;
import org.example.dao.UserDAO;
import org.example.dao.mappers.UserMapper;
import org.example.enums.FollowQueries;
import org.example.enums.Queries;
import org.example.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.enums.Queries.SET_PULL_TIMESTAMP;

@Component
public class UserService
{
    private static final String VIRTUAL_TIMESTAMP_PATTERN = "yyyyMMddHHmmssSSS";
    JdbcTemplate jdbcTemplate;

    @Autowired
    FileUtils fileUtils;

    @Autowired
    BotConfig botConfig;

    @Autowired
    FFMPEG ffmpeg;

    @Autowired
    StatsService statsService;

    @Autowired
    public UserService(DataSourceConfig dataSourceConfig)
    {
        this.jdbcTemplate = dataSourceConfig.jdbcTemplate();
    }

    public int addUser(Long userId, Long chatId, String userName, String firstName, String lastName){
        return jdbcTemplate.update(Queries.ADD_USER.getValue(), userId, userName, firstName, lastName, chatId);
    }

    public int saveAudio(Long userId, String fileId, Integer messageId) {
        return jdbcTemplate.update(Queries.ADD_AUDIO.getValue(), userId, fileId, messageId);
    }

    public Long getUserById(Long userId){
        return jdbcTemplate.query(Queries.GET_USER_BY_ID.getValue(), new Object[]{userId}, new UserMapper())
            .stream().findFirst().map(UserDAO::getId).orElse(null);
    }

    public Integer getSubscriberByUserIdAndSubscriberId(Long userId, Long subscriberId){
       return jdbcTemplate.queryForObject(Queries.CHECK_FOLLOWING.getValue(), new Object[]{userId, subscriberId}, Integer.class);
    }

    public int addContact(Long userId, Long contactId){
        jdbcTemplate.update(Queries.REMOVE_REQUEST_TO_CONFIRM.getValue(), userId, contactId);
        return jdbcTemplate.update(Queries.ADD_CONTACT.getValue(), userId, contactId);
    }

    public Long getChatIdByUserId(Long contactId)
    {
        return jdbcTemplate.queryForObject(Queries.GET_CHAT_ID_BY_USER_ID.getValue(), new Object[]{contactId}, Long.class);
    }

    public int addRequestToConfirm(Long userId, Long contactId)
    {
        return jdbcTemplate.update(Queries.ADD_REQUEST_TO_CONFIRM.getValue(), userId, contactId);
    }

    public Long getUserByFoloweeId(Long foloweeId)
    {
        try
        {
            return jdbcTemplate.queryForObject(Queries.GET_USER_ID_BY_FOLLOWEE_ID.getValue(), new Object[]{foloweeId}, Long.class);
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    public Integer getRequestRecord(Long userId, Long followeeId)
    {
        return jdbcTemplate.queryForObject(Queries.CHECK_FOLLOWEE.getValue(), new Object[]{userId, followeeId}, Integer.class);
    }

    public SendMessage getFollowers(Long userId, Long chatId) {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        List<String> followers = jdbcTemplate.queryForList(FollowQueries.GET_FOLLOWERS.getValue(), new Object[]{userId}, String.class);
        if (followers.isEmpty()) {
            sm.setText("No followers yet");
        } else {
            String prefix = "You have " + followers.size() + " followers by now: ";
            if (followers.size() == 1) {
                prefix = "You have 1 follower by now: ";
            }
            String followersString = followers.stream()
                    .map(i -> "@" + i)
                    .collect(Collectors.joining(", "));
            sm.setText(prefix + followersString);
        }
        return sm;
    }

    public SendMessage getSubscriptions(Long userId, Long chatId) {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        List<String> subscriptions = jdbcTemplate.queryForList(FollowQueries.GET_SUBSCRIPTIONS.getValue(), new Object[]{userId}, String.class);
        if (subscriptions.isEmpty()) {
            sm.setText("No subscriptions yet");
        } else {
            String prefix = "You have " + subscriptions.size() + " subscriptions by now: ";
            if (subscriptions.size() == 1) {
                prefix = "You have 1 subscription by now: ";
            }
            String followersString = subscriptions.stream()
                    .map(i -> "@" + i)
                    .collect(Collectors.joining(", "));
            sm.setText(prefix + followersString);
        }
        return sm;
    }

    public int unsubscribe(Message message, UserService.UserInfo followee){
        return jdbcTemplate.update(FollowQueries.UNSUBSCRIBE.getValue(), message.getFrom().getId(), followee.getUserId());
    }

//    public SendMessage unsubscribe(Message message, String followeeName, ExecuteFunction execute) throws TelegramApiException {
//        SendMessage sm = new SendMessage();
//        sm.setChatId(message.getChatId());
//        if (followeeName.startsWith("@")) {
//            followeeName = followeeName.substring(1);
//        }
//        UserInfo followee = loadUserInfoByName(followeeName);
//        int updatedRows = jdbcTemplate.update(FollowQueries.UNSUBSCRIBE.getValue(), message.getFrom().getId(), followee.getUserId());
//        if (updatedRows > 0) {
//            sm.setText("Ok, unsubscribed");
//
//            SendMessage unsubscribeNotification = new SendMessage();
//            unsubscribeNotification.setChatId(followee.getChatId());
//            unsubscribeNotification.setText("@" + message.getFrom().getUserName() + " unsubscribed");
//            execute.execute(unsubscribeNotification);
//        } else {
//            sm.setText("Nothing changed, are you really subscribed to @" + followeeName + "?");
//        }
//        return sm;
//    }

    public int removeFollower(Message message, UserService.UserInfo follower) throws TelegramApiException {
        return jdbcTemplate.update(FollowQueries.UNSUBSCRIBE.getValue(), follower.getUserId(), message.getFrom().getId());
    }

//    public SendMessage removeFollower(Message message, String followerName, ExecuteFunction execute) throws TelegramApiException {
//        SendMessage sm = new SendMessage();
//        sm.setChatId(message.getChatId());
//        if (followerName.startsWith("@")) {
//            followerName = followerName.substring(1);
//        }
//        UserInfo follower = loadUserInfoByName(followerName);
//        int updatedRows = jdbcTemplate.update(FollowQueries.UNSUBSCRIBE.getValue(), follower.getUserId(), message.getFrom().getId());
//        if (updatedRows > 0) {
//            sm.setText("Ok, user will no longer receive your updates");
//
//            SendMessage unsubscribeNotification = new SendMessage();
//            unsubscribeNotification.setChatId(follower.getChatId());
//            unsubscribeNotification.setText("@" + message.getFrom().getUserName() + " revoked your subscription");
//            execute.execute(unsubscribeNotification);
//        } else {
//            sm.setText("Nothing changed, are you really subscribed to @" + followerName + "?");
//        }
//        return sm;
//    }

    public SendMessage removeUser(Long userId, Long chatId) {
        SendMessage sm = new SendMessage();
        sm.setChatId(chatId);
        int updatedRows = jdbcTemplate.update(Queries.REMOVE_USER.getValue(), userId);
        if (updatedRows > 0) {
            sm.setText("Good bye!");
        } else {
            sm.setText("You are not registered");
        }
        return sm;
    }

    public int removeRecordByUserIdAndMessageId(Long userId, String messageId)
    {
        return jdbcTemplate.update(Queries.REMOVE_LAST_USER_RECORD.getValue(), userId, Integer.valueOf(messageId));
    }

    public Map<String, String> getUserNamesByUserId(Long userId)
    {
        return jdbcTemplate.queryForStream(
            Queries.GET_USER_NAMES_BY_USER_ID.getValue(),
            (rs, rn) -> {
                Map<String, String> result = new HashMap<>();
                result.put("username", rs.getString("username"));
                result.put("first_name", rs.getString("first_name"));
                result.put("last_name", rs.getString("last_name"));
                return result;
            },
            userId
        ).findFirst().orElse(null);
    }

    public int updateNameColumn(Long userId, String columnName, String valueToSet)
    {
        return jdbcTemplate.update("update users set " + columnName + " = ? where user_id = ?", valueToSet, userId);
    }

    private static class FolloweePullTimestamp {
        long followeeId;
        long lastPullTimestamp;
    }

    public static class VoicePart {
        String recordingDate;
        long duration;
    }

    public boolean isDataAvailable(Long userId) {
        return jdbcTemplate.queryForStream(
                Queries.GET_LAST_PULL_TIME.getValue(),
                (rs, rn) -> {
                    FolloweePullTimestamp obj = new FolloweePullTimestamp();
                    obj.followeeId = rs.getLong("followee_id");
                    obj.lastPullTimestamp = rs.getTimestamp("last_pull_timestamp").getTime();

                    return obj;
                },
                Timestamp.from(Instant.now()), userId
        ).findAny().isPresent();
    }

    public List<SendAudio> pullAllRecordsForUser(Long userId, Long chatId)
    {
        Instant nextPullTimestamp = Instant.now();
        // list of followees with their last pull timestamps
        // only followees with available recordings selected
        List<FolloweePullTimestamp> followeesPullTimestamps = jdbcTemplate.queryForStream(
                Queries.GET_LAST_PULL_TIME.getValue(),
                (rs, rn) -> {
                    FolloweePullTimestamp obj = new FolloweePullTimestamp();
                    obj.followeeId = rs.getLong("followee_id");
                    obj.lastPullTimestamp = rs.getTimestamp("last_pull_timestamp").getTime();

                    return obj;
                },
                Timestamp.from(nextPullTimestamp), userId
        ).collect(Collectors.toList());

        List<SendAudio> voices = new ArrayList<>(followeesPullTimestamps.size());
        for (FolloweePullTimestamp fpt : followeesPullTimestamps) {


            // collect recordings
            SimpleDateFormat sdf = new SimpleDateFormat(VIRTUAL_TIMESTAMP_PATTERN);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String timeFrom = sdf.format(new Date(fpt.lastPullTimestamp));
            String timeTo = sdf.format(new Date(nextPullTimestamp.toEpochMilli()));
            String virtualFileName = botConfig.getVfsHost() + "/voice/"
                    + fpt.followeeId + "_" + timeFrom + "_" + timeTo;
            FFMPEGResult localFile = ffmpeg.produceFiles(virtualFileName);
            long lastFileRecordingTimestamp = localFile.getLastFileRecordingTimestamp();
            System.out.println("Last recording timestamp: " + sdf.format(lastFileRecordingTimestamp) + " - " + lastFileRecordingTimestamp);
            jdbcTemplate.update(SET_PULL_TIMESTAMP.getValue(),
                new Timestamp(lastFileRecordingTimestamp), userId, fpt.followeeId
            );

            SendAudio sendAudio = new SendAudio();

            List<VoicePart> voiceParts = getVoiceParts(fpt.followeeId, fpt.lastPullTimestamp, nextPullTimestamp.toEpochMilli());
            if (voiceParts.size() > 1) {
                sendAudio.setCaption(getAudioCaption(voiceParts));
            }

            Path filePath = Paths.get(localFile.getAbsoluteFileURL());
            long fileSize = 0;
            try {
                fileSize = Files.size(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            InputFile in = new InputFile();
            in.setMedia(filePath.toFile(), localFile.getAudioTitle());
            sendAudio.setAudio(in);
            sendAudio.setTitle(localFile.getAudioTitle());
            statsService.setFileSize(fileSize);
            // update last pull timestamp
            statsService.setUserId(userId);
            statsService.setFolloweeId(fpt.followeeId);
            statsService.setLastPullTimestamp(fpt.lastPullTimestamp);
            statsService.setPullTimestamp(lastFileRecordingTimestamp);

            sendAudio.setChatId(chatId);
            sendAudio.setPerformer(localFile.getAudioAuthor());
            String profilePicture = fileUtils.getProfilePicturePath(fpt.followeeId);
            if (profilePicture !=null) {
                sendAudio.setThumb(new InputFile(new File(profilePicture), "cover"));
            }
            System.out.println("Sending file " + localFile.getAbsoluteFileURL() + " for user " + userId + " from user " + fpt.followeeId);
            voices.add(sendAudio);
        }
        return voices;
    }

    private String getAudioCaption(List<VoicePart> voiceParts) {
        long start = 0;
        StringJoiner sj = new StringJoiner("\n");
        for (VoicePart vp : voiceParts) {
            long hours = start / 3600;
            long seconds = start % 60;
            long minutes = (start - (hours * 3600)) / 60;
            String caption = "";
            if (hours > 0) {
                caption += hours + ":";
            }
            caption += String.format("%02d:%02d - %s", minutes, seconds, vp.recordingDate);
            sj.add(caption);
            start += vp.duration;
        }

        return sj.toString();
    }

    /**
     * grouped by day. durations are summed, recording timestamp is the timestamp of earliest recording
     */
    private List<VoicePart> getVoiceParts(long userId, long from, long to) {
        return jdbcTemplate.queryForStream(
                Queries.GET_VOICE_PARTS.getValue(),
                (rs, rn) -> {
                    VoicePart vp = new VoicePart();
                    vp.recordingDate = rs.getString("recording_day");
                    vp.duration = rs.getLong("sum_duration");
                    return vp;
                },
                userId, new Timestamp(from), new Timestamp(to)
                ).collect(Collectors.toList());

    }

    private class Recording {
        String userName;
        String fileId;
        Timestamp recordingDate;

        public Recording(String userName, String fileId, Timestamp recordingDate) {
            this.userName = userName;
            this.fileId = fileId;
            this.recordingDate = recordingDate;
        }

        public String getUserName() {
            return userName;
        }

        public String getFileId() {
            return fileId;
        }

        public Timestamp getRecordingDate() {
            return recordingDate;
        }
    }

    UserService.UserInfo loadUserInfoById(Long userId) {
        try
        {
            return jdbcTemplate.queryForObject(
                    Queries.GET_USER_ID_BY_ID.getValue(),
                    (rs, n) -> {
                        String userName = rs.getString("user_name");
                        String followeeChatId = rs.getString("chat_id");
                        return new UserService.UserInfo(userId, followeeChatId);
                    },
                    userId
            );
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    class UserInfo {
        Long userId;
        String chatId;

        public UserInfo(Long userId, String chatId) {
            this.userId = userId;
            this.chatId = chatId;
        }

        public Long getUserId() {
            return userId;
        }

        public String getChatId() {
            return chatId;
        }
    }
}
