package org.example.util;

import org.example.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class FileUtils {
    @Autowired
    BotConfig botConfig;

    public boolean moveFileAbsolute(String from, String to) {
        File fromFile = new File(from);
        File toFile = new File(to);
        System.out.println("Renaming " + fromFile + " to " + toFile);
        System.out.println("From exists: " + fromFile.exists());
        return fromFile.renameTo(toFile);
    }

    public String getProfilePicturePath(long userId) {
        String folderPath = botConfig.getStoragePath() + botConfig.getProfilePicturesPath();
        File folder = new File(folderPath);
        final File[] files = folder.listFiles((dir,name) -> name.matches(userId + ".*?"));
        if (files == null) return null;
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
