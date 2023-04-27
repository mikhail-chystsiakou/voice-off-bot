package org.example.ffmpeg;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
public class VirtualFileInfo {

    private long userId;
    private long dateFrom; // unix milliseconds
    private long dateTo; // unix milliseconds
    private String authorUserName;
    private String authorFirstName;
    private String authorLastName;

    public VirtualFileInfo(long userId, long dateFrom, long dateTo) {
        this.userId = userId;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }
}
