package org.example.storage;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws ParseException {
//        System.out.println(String.format("<a href=\"https://t.me/%s\">%02d:%02d%s</a> ", "botname", 123 / 60, 123 % 60, "today"));
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd_HH_mm_ss.S");
//        System.out.println(sdf.format(new Date(System.currentTimeMillis())));
        VirtualFileInfo vfi = VirtualFileInfo.fromURL("245924084_20230407082950873_20230407083114946");
        System.out.println(
                vfi
        );
        System.out.println(new Timestamp(vfi.getDateFrom()));
        System.out.println(vfi.getDateFrom());

    }


}

@Data
@AllArgsConstructor
class VirtualFileInfo {
    private static final Pattern VIRTUAL_FILE_REGEXP = Pattern.compile(
            "(?<userId>\\d+)_(?<dateFrom>\\d+)_(?<dateTo>\\d+)"
    );
    private static final String DATE_FORMAT = "yyyyMMddHHmmssSSS";

    private long userId;
    private long dateFrom; // unix milliseconds
    private long dateTo; // unix milliseconds

    /**
     * @param url in form of USERID_DATEFROM_DATETO.ogg
     */
    public static VirtualFileInfo fromURL(String url) throws ParseException {
        Matcher m = VIRTUAL_FILE_REGEXP.matcher(url);
        if (!m.find()) {
            throw new IllegalArgumentException("Can't parse file from " + url);
        }
        long userId = Long.parseLong(m.group("userId"));
        String dateFromString = m.group("dateFrom");
        String dateToString = m.group("dateTo");
        long dateFrom;
        long dateTo;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.out.println(dateFromString);
        System.out.println(sdf.parse(dateFromString));
        try {
            dateFrom = sdf.parse(dateFromString).getTime();
            dateTo = sdf.parse(dateToString).getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Can't parse date from " + url);
        }
        return new VirtualFileInfo(userId, dateFrom, dateTo);
    }


}
