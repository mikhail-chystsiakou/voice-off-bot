package org.example.ffmpeg;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
@Data
public class FileInfo {
    private long userId;
    private String fileId;
    private int duration;
    private long recordingTimestamp; // unix millis

    public long getUserId() {
        return userId;
    }

    public String getFileId() {
        return fileId;
    }

    public int getDuration() {
        return duration;
    }

    public long getRecordingTimestamp() {
        return recordingTimestamp;
    }
}
