package org.example.ffmpeg;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FFMPEGResult {
    String absoluteFileURL;
    String tmpListFileURL;
    String audioAuthor;
    String audioTitle;
    long lastFileRecordingTimestamp;
}


