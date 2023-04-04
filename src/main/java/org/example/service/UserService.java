package org.example.service;

import org.example.util.ExecuteFunction;
import org.example.config.DataSourceConfig;
import org.example.dao.UserDAO;
import org.example.dao.mappers.UserMapper;
import org.example.enums.FollowQueries;
import org.example.enums.Queries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.example.enums.Queries.SET_PULL_TIMESTAMP;

@Component
public class UserService
{
    JdbcTemplate jdbcTemplate;

    @Autowired
    public UserService(DataSourceConfig dataSourceConfig)
    {
        this.jdbcTemplate = dataSourceConfig.jdbcTemplate();
    }

    public int addUser(Long userId, Long chatId, String userName){
        return jdbcTemplate.update(Queries.ADD_USER.getValue(), userId, userName, chatId);
    }

    public int saveAudio(Long userId, String fileId) {
        return jdbcTemplate.update(Queries.ADD_AUDIO.getValue(), userId, fileId);
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
        return jdbcTemplate.queryForObject(Queries.GET_USER_ID_BY_FOLLOWEE_ID.getValue(), new Object[]{foloweeId}, Long.class);
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

    public SendMessage unsubscribe(Message message, String followeeName, ExecuteFunction execute) throws TelegramApiException {
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());
        if (followeeName.startsWith("@")) {
            followeeName = followeeName.substring(1);
        }
        UserInfo followee = loadUserInfoByName(followeeName);
        int updatedRows = jdbcTemplate.update(FollowQueries.UNSUBSCRIBE.getValue(), message.getFrom().getId(), followee.getUserId());
        if (updatedRows > 0) {
            sm.setText("Ok, unsubscribed");

            SendMessage unsubscribeNotification = new SendMessage();
            unsubscribeNotification.setChatId(followee.getChatId());
            unsubscribeNotification.setText("@" + message.getFrom().getUserName() + " unsubscribed");
            execute.execute(unsubscribeNotification);
        } else {
            sm.setText("Nothing changed, are you really subscribed to @" + followeeName + "?");
        }
        return sm;
    }

    public SendMessage removeFollower(Message message, String followerName, ExecuteFunction execute) throws TelegramApiException {
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());
        if (followerName.startsWith("@")) {
            followerName = followerName.substring(1);
        }
        UserInfo follower = loadUserInfoByName(followerName);
        int updatedRows = jdbcTemplate.update(FollowQueries.UNSUBSCRIBE.getValue(), follower.getUserId(), message.getFrom().getId());
        if (updatedRows > 0) {
            sm.setText("Ok, user will no longer receive your updates");

            SendMessage unsubscribeNotification = new SendMessage();
            unsubscribeNotification.setChatId(follower.getChatId());
            unsubscribeNotification.setText("@" + message.getFrom().getUserName() + " revoked your subscription");
            execute.execute(unsubscribeNotification);
        } else {
            sm.setText("Nothing changed, are you really subscribed to @" + followerName + "?");
        }
        return sm;
    }

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

    public List<SendVoice> pullAllRecordsForUser(Long userId, Long chatId)
    {

        //u.user_name, ua.file_id, ua.recording_timestamp
        Stream<Recording> recordings = jdbcTemplate.queryForStream(
                Queries.PULL_RECORDS_BY_USER_ID.getValue(),
                (rs, rn) -> {
                    String userName = rs.getString("user_name");
                    String fileId = rs.getString("file_id");
                    Timestamp recordingTimestamp = rs.getTimestamp("recording_timestamp");
                    return new Recording(userName, fileId, recordingTimestamp);
                },
                userId);
//        List<String> fileNames = jdbcTemplate.queryForList(Queries.PULL_RECORDS_BY_USER_ID.getValue(), new Object[]{userId}, String.class);

        jdbcTemplate.update(SET_PULL_TIMESTAMP.getValue(), userId);

        List<SendVoice> voiceList = recordings
            .map(recording ->
                 {
                     SendVoice sendVoice = new SendVoice();
                     sendVoice.setVoice(new InputFile(recording.getFileId()));
                     sendVoice.setChatId(chatId);
                     SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                     String formattedTimestamp = sdf.format(recording.getRecordingDate());
                     sendVoice.setCaption(formattedTimestamp + " from @" + recording.getUserName());
                     return sendVoice;
                 })
            .collect(Collectors.toList());

        return voiceList;
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

    private UserInfo loadUserInfoByName(String userName) {
        return jdbcTemplate.queryForObject(
            Queries.GET_USER_ID_BY_NAME.getValue(),
            (rs, n) -> {
                Long followeeId = rs.getLong("user_id");
                String followeeChatId = rs.getString("chat_id");
                return new UserInfo(followeeId, userName, followeeChatId);
            },
            userName
        );
    }

    private class UserInfo {
        Long userId;
        String userName;
        String chatId;

        public UserInfo(Long userId, String userName, String chatId) {
            this.userId = userId;
            this.userName = userName;
            this.chatId = chatId;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getChatId() {
            return chatId;
        }
    }
}
