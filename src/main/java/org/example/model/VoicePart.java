package org.example.model;

import lombok.Data;

@Data
public class VoicePart {
    public long recordingTimestamp;
    public long duration;
    public String description;
    public long pullCount;
    public long messageId;
}