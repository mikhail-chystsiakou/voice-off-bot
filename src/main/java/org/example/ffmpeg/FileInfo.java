package org.example.ffmpeg;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FileInfo {
    private long userId;
    private String fileId;
    private int duration;
    private long recordingTimestamp; // unix millis
    private Integer messageId;
    private Long replyModeFolloweeId;
}
