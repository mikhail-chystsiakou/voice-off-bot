package org.example.storage;

import lombok.AllArgsConstructor;
import org.example.bot.MyTelegramBot;
import org.example.config.BotConfig;
import org.example.enums.Queries;
import org.example.util.ExecuteFunction;
import org.example.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.TimeZone;

@Component
public class VoiceStorage {
    private static final Logger logger = LoggerFactory.getLogger(VoiceStorage.class);

    private final static String FILENAME_DATE_PATTERN = "YYYY_MM_DD_HH24_MM_SS_SSS_{duration}_{fileId}.oga";
    @Autowired
    FileUtils fileUtils;

    @Autowired
    BotConfig botConfig;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void storeVoice(long userId, String fileId, int duration, ExecuteFunction execute, Integer messageId) {
        logger.debug("Storing voice for user: {}", userId);
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

        String pathPrefix = botConfig.getStoragePath() + botConfig.getVoicesPath();
        if (!pathPrefix.endsWith(File.separator)) {
            pathPrefix += File.separator;
        }
        pathPrefix += userId + File.separator;

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy" + File.separator + "MM");
        String timePrefix = sdf.format(new Date(recordingTimestamp)) + File.separator;
        String prefix = pathPrefix + timePrefix;

        String destFilename = prefix + createFileName(recordingTimestamp, duration, fileId);
        System.out.println("Copying from " + sourceFilename + " to " + destFilename);
        long fileSize = 0;
        try {
            Files.createDirectories(Paths.get(prefix));
            fileUtils.moveFileAbsolute(sourceFilename, destFilename);
            Path filePath = Paths.get(destFilename);
            fileSize = Files.size(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        jdbcTemplate.update(Queries.ADD_AUDIO.getValue(),
                userId, fileId, duration, sqlTimestamp, messageId, fileSize
        );
        System.out.println("File " + fileId + " stored to db");
    }

    private String createFileName(long timestamp, int duration, String fileId) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd_HH_mm_ss_SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timePrefix = sdf.format(new Date(timestamp));
        return timePrefix + "_" + duration + "_" + fileId + ".opus";
    }

}
