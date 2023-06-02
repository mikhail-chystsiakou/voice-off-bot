package org.example.util;

import lombok.extern.slf4j.Slf4j;
import org.example.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
@Slf4j
public class FileUtils {
    @Autowired
    BotConfig botConfig;

    public boolean moveFileAbsolute(String from, String to) {
        try {
            log.info("moveFileAbsolute: Moving {} to {}", from, to);
            Files.move(Path.of(from), Path.of(to), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public String getProfilePicturePath(long userId) {
        String folderPath = botConfig.getStoragePath() + botConfig.getProfilePicturesPath();
        File folder = new File(folderPath);
        final File[] files = folder.listFiles((dir,name) -> name.matches(userId + ".*?"));
        if (files == null || files.length == 0) return null;
        return files[0].getAbsolutePath();
    }

    public String getProfilePicturePath(long userId, String fileId) {
        String pathPrefix = botConfig.getStoragePath() + botConfig.getProfilePicturesPath();
        if (!pathPrefix.endsWith(File.separator)) {
            pathPrefix += File.separator;
        }

        return pathPrefix + userId + "_" + fileId + ".jpg";
    }
}
