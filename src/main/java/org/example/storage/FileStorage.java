package org.example.storage;

import lombok.extern.slf4j.Slf4j;
import org.example.config.BotConfig;
import org.example.dto.FileDTO;
import org.example.enums.MessageType;
import org.example.enums.Queries;
import org.example.model.UserAudio;
import org.example.service.UserService;
import org.example.util.ExecuteFunction;
import org.example.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.GetFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

/**
 * FILENAME_DATE_PATTERN = "YYYY_MM_DD_HH24_MM_SS_SSS_{duration}_{fileId}.oga"
 */
@Component
@Slf4j
public class FileStorage {

    private static final String FILENAME_DATE_PATTERN = "YYYY_MM_DD_HH24_MM_SS_SSS_{duration}_{fileId}.opus";
    public static final String DEFAULT_AUDIO_EXTENSION = "opus";

    public static final String TYPE_FEEDBACK = "TYPE_FEEDBACK";

    @Autowired
    FileUtils fileUtils;

    @Autowired
    BotConfig botConfig;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Lazy
    @Autowired
    ExecuteFunction execute;

    @Lazy
    @Autowired
    UserService userService;

    public void storeFile(long userId, String fileId, int duration, Integer messageId, Long replyModeFolloweeId, Integer replyModeMessageId) {
        MessageType messageType = MessageType.DATA;
        if (replyModeFolloweeId != null) {
            messageType = MessageType.REPLY;
        }
        storeFile(userId, fileId, duration, messageId, DEFAULT_AUDIO_EXTENSION, messageType, replyModeFolloweeId, replyModeMessageId);
    }

    public void storeFile(long userId, String fileId, int duration, Integer messageId, String extension) {
        storeFile(userId, fileId, duration, messageId, extension, MessageType.DATA, null, null);
    }

    public void storeFile(
            long userId, String fileId, int duration,
            Integer messageId, String extension, MessageType messageType, Long replyModeFolloweeId, Integer replyModeMessageId) {
        log.debug("Storing file {} of type {} for user: {}", fileId, extension, userId);
        long recordingTimestamp = Instant.now().toEpochMilli();
        Timestamp sqlTimestamp = new Timestamp(recordingTimestamp);

        String sourceFilename = "";
        GetFile getFileCommand = new GetFile();
        getFileCommand.setFileId(fileId);
        try {
            org.telegram.telegrambots.meta.api.objects.File downloadedFile
                    = execute.execute(getFileCommand);
            sourceFilename = downloadedFile.getFilePath();
        } catch (Exception e) {
            e.printStackTrace();
            return;
//            throw new RuntimeException(e);
        }

        // rewrite file path
        // for case when tgapi server is running in docker
        Pattern p = Pattern.compile(".*" + botConfig.getToken() + "(/.*)");
        Matcher m = p.matcher(sourceFilename);
        m.find();
        String localSourceFileName = botConfig.getStoragePath() + "/tgapi/" + botConfig.getToken() +  m.group(1);

        String dir = getFileDir(userId, recordingTimestamp, messageType);
        String destFilename = getFullFilePath(
                userId, recordingTimestamp, duration, fileId, extension, messageType, replyModeFolloweeId);
        System.out.println("Copying from " + sourceFilename + " to " + destFilename);
                userId, recordingTimestamp, duration, fileId, extension, messageType, messageId, replyModeFolloweeId);
        System.out.println("Copying from " + localSourceFileName + " to " + destFilename);
        long fileSize = 0;
        try {
            Files.createDirectories(Paths.get(dir));
            fileUtils.moveFileAbsolute(localSourceFileName, destFilename);
            Path filePath = Paths.get(destFilename);
            fileSize = Files.size(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        jdbcTemplate.update(Queries.ADD_AUDIO.getValue(),
                userId, fileId, duration, sqlTimestamp, messageId, fileSize, replyModeMessageId
        );
        System.out.println("File " + fileId + " stored to db");
    }

    public void storeFile(MultipartFile multipartFile, FileDTO fileDTO, UserAudio userAudio) throws IOException {
        File dest = new File(getFullFilePath(
                userAudio.getUserInfo().getUserId(),
                userAudio.getRecordingTimestamp().getTime(),
                userAudio.getDuration(),
                userAudio.getFileId(),
                DEFAULT_AUDIO_EXTENSION,
                fileDTO.getMessageType(),
                userAudio.getUserInfo().getReplyModeFolloweeId()
        ));
        if (!dest.exists() && !dest.mkdirs()) {
            throw new RuntimeException("Did not manage to create folders for voice");
        }
        multipartFile.transferTo(dest);
    }

    public String getFullFilePath(long userId, long recordingTimestamp, int duration, String fileId,
                                  String extension, MessageType messageType, Long replyModeFolloweeId) {
        String dir = getFileDir(userId, recordingTimestamp, messageType);
        return dir + createFileName(recordingTimestamp, duration, fileId, extension, messageType, replyModeFolloweeId);
    }

    public String getFileDir(long userId, long recordingTimestamp, MessageType messageType) {
        String pathPrefix = botConfig.getStoragePath() + messageType.getDir();
        if (!pathPrefix.endsWith(File.separator)) {
            pathPrefix += File.separator;
        }
        pathPrefix += userId + File.separator;

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy" + File.separator + "MM");
        String timePrefix = sdf.format(new Date(recordingTimestamp)) + File.separator;
        return pathPrefix + timePrefix;
    }

    public String createFileName(long timestamp, int duration, String fileId,
                                  String extension, MessageType messageType, Long replyModeFolloweeId) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_HH_mm_ss_SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timePrefix = sdf.format(new Date(timestamp));
        return
                timePrefix
                    + "_" + duration
//                    + "_" + messageId
                    + (MessageType.REPLY.equals(messageType)
                        ? (replyModeFolloweeId == null) ? "_0" : ("_" + replyModeFolloweeId)
                        : "")
                    + "_" + fileId
                    + "." + extension;
    }

}
