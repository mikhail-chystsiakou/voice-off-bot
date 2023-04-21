package org.example.storage;

import org.example.config.BotConfig;
import org.example.enums.MessageType;
import org.example.enums.Queries;
import org.example.util.ExecuteFunction;
import org.example.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
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
public class FileStorage {
    private static final Logger logger = LoggerFactory.getLogger(FileStorage.class);

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
        logger.debug("Storing file {} of type {} for user: {}", fileId, extension, userId);
        System.out.println("Storing file {} of type {} for user: {}" + fileId + extension + userId + messageType);
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

        String dir = getFileDir(userId, recordingTimestamp, messageType);
        String destFilename = getFullFilePath(
                userId, recordingTimestamp, duration, fileId, extension, messageType, messageId, replyModeFolloweeId);
        System.out.println("Copying from " + sourceFilename + " to " + destFilename);
        long fileSize = 0;
        try {
            Files.createDirectories(Paths.get(dir));
            fileUtils.moveFileAbsolute(sourceFilename, destFilename);
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

    public String getFullFilePath(long userId, long recordingTimestamp, int duration, String fileId,
                                  String extension, MessageType messageType, Integer messageId, Long replyModeFolloweeId) {
        String dir = getFileDir(userId, recordingTimestamp, messageType);
        String destFilename = dir + createFileName(recordingTimestamp, duration, fileId, extension, messageId, replyModeFolloweeId);
        return destFilename;
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
                                  String extension, Integer messageId, Long replyModeFolloweeId) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_HH_mm_ss_SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timePrefix = sdf.format(new Date(timestamp));
        return
                timePrefix
                    + "_" + duration
                    + "_" + messageId
                    + ((replyModeFolloweeId == null) ? "_0" : ("_" + replyModeFolloweeId))
                    + "_" + fileId
                    + "." + extension;
    }

}
