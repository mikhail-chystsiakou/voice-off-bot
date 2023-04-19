package org.example.service;

import lombok.Data;
import org.example.config.BotConfig;
import org.example.config.DataSourceConfig;
import org.example.dao.UserDAO;
import org.example.dao.mappers.UserMapper;
import org.example.enums.FollowQueries;
import org.example.enums.Queries;
import org.example.ffmpeg.FileInfo;
import org.example.model.UserInfo;
import org.example.repository.UserRepository;
import org.example.ffmpeg.FFMPEG;
import org.example.ffmpeg.FFMPEGResult;
import org.example.util.ExecuteFunction;
import org.example.util.FileUtils;
import org.example.util.ThreadLocalMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.Constants.Messages.OK_RECORDED;
import static org.example.enums.Queries.*;
import static org.example.util.ThreadLocalMap.KEY_USER_INFO;

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
    @Lazy
    ExecuteFunction executeFunction;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ThreadLocalMap tlm;

    @Autowired
    ButtonsService buttonsService;

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

    public int removeRequestToConfirm(Long userId, Long contactId){
        return  jdbcTemplate.update(Queries.REMOVE_REQUEST_TO_CONFIRM.getValue(), userId, contactId);
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
            String followersString = String.join(", ", followers);
            sm.setText(prefix + followersString);
        }
        return sm;
    }

    public void storeFeedback(long userId, long messageId, String text, String fileId) {
        jdbcTemplate.update(STORE_FEEDBACK.getValue(), userId, messageId, text, fileId);
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
            String followersString = String.join(", ", subscriptions);
            sm.setText(prefix + followersString);
        }
        return sm;
    }

    public int unsubscribe(Message message, UserInfo followee){
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

    public int removeFollower(Message message, UserInfo follower) throws TelegramApiException {
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

    public int updateNotificationSettings(long userId, int value)
    {
        return jdbcTemplate.update(Queries.UPDATE_NOTIFICATION_BY_USER.getValue(), value, userId);
    }

    public int updateFeedbackAllowed(long userId, boolean isFeedbackAllowed)
    {
        return jdbcTemplate.update(Queries.UPDATE_FEEDBACK_ALLOWED_BY_USER.getValue(), isFeedbackAllowed, userId);
    }

    public int updateFeedbackEnabled(long userId, boolean isFeedbackEnabled)
    {
        return jdbcTemplate.update(Queries.UPDATE_FEEDBACK_ENABLED_BY_USER.getValue(), isFeedbackEnabled, userId);
    }

    //Map<timezone, users>
    public Map<Integer, List<Long>> getUsersForDelayNotifications(Long userId)
    {
        final Map<Integer, List<Long>> result = new HashMap<>();
        jdbcTemplate.query(
            GET_USERS_FOR_DELAY_NOTIFICATIONS.getValue(),
            (rs, rn) -> {
                long id = rs.getLong("user_id");
                int timezone = rs.getInt("time_zone");
                result.compute(timezone, (k, v) -> {
                    if (v == null) v = new LinkedList<>();
                    v.add(id);
                    return  v;
                });
                return result;
            },
            userId);

        return result;
    }

    public List<Long> getUsersForInstantNotifications(Long userId){
        return jdbcTemplate.queryForList(GET_USERS_FOR_INSTANT_NOTIFICATIONS.getValue(), new Object[]{userId}, Long.class);
    }

    public void addUserNotification(Long userId, LocalTime estimatedTimeWithTimeZone)
    {
        if (!isNotificationForUserExists(userId)){
            jdbcTemplate.update(Queries.ADD_USER_NOTIFICATION.getValue(), userId, estimatedTimeWithTimeZone);
        }
    }

    private boolean isNotificationForUserExists(Long userId)
    {
        int result = jdbcTemplate.queryForObject(Queries.CHECK_USER_NOTIFICATION.getValue(), new Object[]{userId}, Integer.class);
        return result != 0;
    }

    public List<Long> getDelayNotifications()
    {
        return jdbcTemplate.queryForList(Queries.GET_CHAT_ID_FOR_NOTIFICATIONS.getValue(), Long.class);
    }

    public int deleteUserFromDelayNotification(Long userId)
    {
        return jdbcTemplate.update(Queries.DELETE_USER_FROM_DELAY_NOTIFICATIONS.getValue(), userId);
    }

    public FileInfo getFileIdByUserAndMessageId(Long userId, Integer messageId)
    {
        return jdbcTemplate.query(
            GET_FILE_ID_BY_USER_AND_MESSAGE_ID.getValue(),
            (rs, rn) -> {
                String fileId = rs.getString("file_id");
                int duration = Integer.parseInt(rs.getString("duration"));
                long recordingTimestamp = rs.getTimestamp("recording_timestamp").getTime();
                return new FileInfo(userId, fileId, duration, recordingTimestamp);
            },
            userId,
            messageId).stream().findFirst().orElse(null);
    }

    @Data
    private static class FolloweePullTimestamp {
        long followeeId;
        long lastPullTimestamp;
    }

    @Data
    public static class VoicePart {
        long recordingTimestamp;
        long duration;
        String description;
        long pullCount;
        long messageId;
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
        long nextPullTimestamp = System.currentTimeMillis();
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
                new Timestamp(nextPullTimestamp), userId
        ).collect(Collectors.toList());

        List<SendAudio> voices = new ArrayList<>(followeesPullTimestamps.size());
        for (FolloweePullTimestamp fpt : followeesPullTimestamps) {
            System.out.println(fpt);
            statsService.setFolloweeId(fpt.followeeId);
            statsService.setLastPullTimestamp(fpt.lastPullTimestamp);

            // collect recordings
            FFMPEGResult localFile = ffmpeg.produceFiles(fpt.followeeId, fpt.lastPullTimestamp, nextPullTimestamp);
            long lastFileRecordingTimestamp = localFile.getLastFileRecordingTimestamp();
            statsService.setPullTimestamp(lastFileRecordingTimestamp);
            System.out.println("Last recording timestamp: " + new Timestamp(lastFileRecordingTimestamp));
            jdbcTemplate.update(SET_PULL_TIMESTAMP.getValue(),
                new Timestamp(lastFileRecordingTimestamp), userId, fpt.followeeId
            );

            SendAudio sendAudio = new SendAudio();
            sendAudio.setParseMode("Markdown");

            // todo OH MY GOD sql inside loop inside synchronized -_-
            List<VoicePart> voiceParts = Collections.emptyList();
            synchronized (this) {
                voiceParts = getVoiceParts(fpt.followeeId, fpt.lastPullTimestamp, lastFileRecordingTimestamp);
                System.out.println(Arrays.asList(voiceParts.toArray()));
                for (VoicePart vp : voiceParts) {
                    vp.setPullCount(vp.getPullCount() + 1);
                    jdbcTemplate.update(SET_PULL_COUNT.getValue(), vp.getPullCount(), fpt.followeeId, vp.messageId);
                    // update messages
                    System.out.println("updating message id " + vp.messageId + " of " + fpt.followeeId + " to pull count " + vp.getPullCount());
                    Integer followeeOkMessageId = jdbcTemplate.queryForObject(GET_OK_MESSAGE_ID.getValue(),
                            new Object[] { fpt.followeeId, vp.messageId },
                            Integer.class);
                    if (followeeOkMessageId != null) {
                        EditMessageText emt = new EditMessageText();
                        emt.setChatId(fpt.followeeId);
                        if (vp.getPullCount() == 1) {
                            emt.setText(OK_RECORDED + ". Recording was pulled 1 time");

                        } else {

                            emt.setText(OK_RECORDED + ". Recording was pulled " + vp.getPullCount() + " times");
                        }
                        emt.setMessageId(followeeOkMessageId);
                        emt.setReplyMarkup(buttonsService.getButtonForDeletingRecord((int)vp.messageId));
                        try {
                            executeFunction.execute(emt);
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }


                }
            }

            Path filePath = Paths.get(localFile.getAbsoluteFileURL());
            long fileSize = 0;
            try {
                fileSize = Files.size(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("setting pull file size " + fileSize);
            statsService.setFileSize(fileSize);
            InputFile in = new InputFile();
            in.setMedia(filePath.toFile(), localFile.getAudioTitle());
            sendAudio.setAudio(in);
            int duration = 0;
            for (VoicePart vp : voiceParts) {
                duration += vp.duration;
            }
            sendAudio.setDuration(duration);
            sendAudio.setTitle(localFile.getAudioTitle());
            sendAudio.setChatId(chatId);
            sendAudio.setPerformer(localFile.getAudioAuthor());
            if (voiceParts.size() > 1 || (voiceParts.size() == 1 && voiceParts.get(0).getDescription() != null)) {
                sendAudio.setReplyMarkup(buttonsService.getShowTimestampsButton());
            }
            String profilePicture = fileUtils.getProfilePicturePath(fpt.followeeId);
            if (profilePicture !=null) {
                sendAudio.setThumb(new InputFile(new File(profilePicture), "cover"));
            }
            System.out.println("Sending file " + localFile.getAbsoluteFileURL() + " for user " + userId + " from user " + fpt.followeeId);
            voices.add(sendAudio);
        }
        return voices;
    }

    public void cleanup(SendAudio sendAudio) {
        String filePath = sendAudio.getFile().getNewMediaFile().getAbsolutePath();
        try {
            System.out.println("Deleting temp path: " + filePath);
            Files.delete(Path.of(filePath));
            Files.delete(Path.of(filePath.replaceFirst("(\\.\\w+)$", ".list")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getAudioCaption(List<VoicePart> voiceParts, int zoneOffset) {
        long start = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("`yyyy.MM.dd, HH:mm:ss`");
        sdf.setTimeZone(getTimeZoneByOffset(zoneOffset));
        StringJoiner sj = new StringJoiner("\n");
        for (VoicePart vp : voiceParts) {

            String timeHandle = getTimeHandle(start);
            String recordingTimestamp = sdf.format(new Timestamp(vp.recordingTimestamp));
            String voiceDescription = vp.getDescription();
            voiceDescription = voiceDescription == null ? "" : "\n" + voiceDescription;

            String caption = timeHandle + " - " + recordingTimestamp + voiceDescription;
            sj.add(caption);

            start += vp.duration;
        }
        return "\n" + sj.toString();
    }

    private TimeZone getTimeZoneByOffset(int zoneOffset) {
        String zoneId = "GMT";
        if (zoneOffset < 0) {
            zoneId += "-";
        } else {
            zoneId += "+";
        }
        zoneId += zoneOffset / 100;
        zoneId += ":";
        zoneId += String.format("%02d", zoneOffset % 100);
        TimeZone tz = TimeZone.getTimeZone(zoneId);
        System.out.println("timezone: " + tz.getDisplayName() + ", from " + zoneId);
        return tz;
    }

    private String getTimeHandle(long start) {
        long hours = start / 3600;
        long seconds = start % 60;
        long minutes = (start - (hours * 3600)) / 60;
        String timeHandle = "";
        if (hours > 0) {
            timeHandle += hours + ":";
        }
        timeHandle += String.format("%02d:%02d", minutes, seconds);
        return timeHandle;
    }

    /**
     * grouped by day. durations are summed, recording timestamp is the timestamp of earliest recording
     */
    private List<VoicePart> getVoiceParts(long userId, long from, long to) {
        return jdbcTemplate.queryForStream(
                Queries.GET_VOICE_PARTS.getValue(),
                (rs, rn) -> {
                    VoicePart vp = new VoicePart();
                    vp.recordingTimestamp = rs.getTimestamp("recording_timestamp").getTime();
                    vp.duration = rs.getLong("duration");
                    vp.description = rs.getString("description");
                    vp.pullCount = rs.getLong("pull_count");
                    vp.messageId = rs.getLong("message_id");
                    return vp;
                },
                userId, new Timestamp(from), new Timestamp(to)
                ).collect(Collectors.toList());

    }

    public void loadUserInfo(long userId) {
        UserInfo userInfo = userRepository.loadUserInfoById(userId);
        tlm.put(KEY_USER_INFO, userInfo);
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
}
