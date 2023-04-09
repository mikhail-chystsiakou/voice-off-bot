package org.example.ffmpeg;

import org.example.config.BotConfig;
import org.example.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class FFMPEG {
    private static final String COMMAND_PATTERN = "ffmpeg " +
            "-y -safe 0 -f concat -i {0} " +
            "-metadata artist=\"{1}\" -metadata title=\"{2}\" " +
//            "-c:a libmp3lame {3}";
            "-codec copy {3}";
    private static final String FILE_PATTERN = "echo $''file \\''{0}\\''''";
    private static final String FILE_LOCATION_PATTERN = "echo $''file \\''{0}\\''''";
    private static final String FILE_DATE_PATTERN = "yyyy" + File.separator + "MM" + File.separator + "dd_HH_mm_ss_SSS";


    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    BotConfig botConfig;

    @Autowired
    public FFMPEG(Config config) {
        String osName = System.getProperty("os.name");
    }

    public FFMPEGResult produceFiles(String virtualFile) {
        String tempStoragePath = botConfig.getStoragePath() + botConfig.getTmpPath() + File.separator;

        VirtualFileInfo virtualFileInfo = loadVirtualFile(virtualFile);

        String outputFileSuffix = tempStoragePath + System.currentTimeMillis() + "_" + new Random().nextInt(9_999_999);
        String outputFile = outputFileSuffix + ".opus";
        String listFile = outputFileSuffix + ".list";
        long lastFileRecordingTimestamp = 0;

        try {
            lastFileRecordingTimestamp = createListFile(listFile, loadFileInfo(virtualFileInfo));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String audioAuthor = getAudioAuthor(virtualFileInfo);
        String audioTitle = getAudioTitle(virtualFileInfo);
        String command = MessageFormat.format(COMMAND_PATTERN, listFile, audioAuthor, audioTitle, outputFile);
        System.out.println(command);
        try {
            execFFMPEG(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new FFMPEGResult(outputFile, listFile, audioAuthor, audioTitle, lastFileRecordingTimestamp);

    }

    private String getAudioAuthor(VirtualFileInfo vfi) {
        String audioAuthor = "";
        String username = vfi.getAuthorUserName() == null ? "" : vfi.getAuthorUserName();
        String firstName = vfi.getAuthorFirstName() == null ? "" : vfi.getAuthorFirstName();
        String lastName = vfi.getAuthorLastName() == null ? "" : vfi.getAuthorLastName();

        if (ObjectUtils.isEmpty(firstName)
                && ObjectUtils.isEmpty(lastName)) {
            audioAuthor = username;
        } else {
            audioAuthor += firstName;
            if (!audioAuthor.isBlank()) audioAuthor += " ";
            audioAuthor += lastName;
        }
        return audioAuthor;
    }

    private String getAudioTitle(VirtualFileInfo vfi) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String from = sdf.format(new Date(vfi.getDateFrom()));
        String to = sdf.format(new Date(vfi.getDateFrom()));
        String title = "Diary from " + from;
        if (!to.equals(from)) title += "to " + to;

        return title;
    }


    private static final Pattern VIRTUAL_FILE_REGEXP = Pattern.compile(
            "(?<userId>\\d+)_(?<dateFrom>\\d+)_(?<dateTo>\\d+)"
    );
    private static final String DATE_FORMAT = "yyyyMMddHHmmssSSS";

    /**
     * @param url in form of USERID_DATEFROM_DATETO.ogg
     */
    private VirtualFileInfo loadVirtualFile(String url) {
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
        try {
            dateFrom = sdf.parse(dateFromString).getTime();
            dateTo = sdf.parse(dateToString).getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Can't parse date from " + url);
        }
        VirtualFileInfo result = new VirtualFileInfo(userId, dateFrom, dateTo);
        jdbcTemplate.query("select username, first_name, last_name from users where user_id = ?",
                (rs) -> {
                    result.setAuthorUserName(rs.getString("username"));
                    result.setAuthorFirstName(rs.getString("first_name"));
                    result.setAuthorLastName(rs.getString("last_name"));
                },
                result.getUserId()
        );

        return result;
    }

    private void execFFMPEG(String command) throws InterruptedException, IOException {
        String osName = System.getProperty("os.name");
        String shellCommand = "/bin/sh";
        String commandLine = command;
        String procCommand = "/bin/sh";
        if (osName.startsWith("Windows")) {
            commandLine = command.replace("\\", "/");
            procCommand = "\"C:\\Program Files\\Git\\bin\\sh.exe\"";
        }
        String fullCommand = shellCommand + " \"" + commandLine + "\"";
        System.out.println(fullCommand);
        ProcessBuilder pb = new ProcessBuilder(procCommand, "-c", commandLine);
        Process process = pb.start();
        int res = process.waitFor();
        System.out.println("ffmpeg finished with result " + res);
    }

    private long createListFile(String listFilePath, List<FileInfo> virtualFiles) throws IOException {
        File listFile = new File(listFilePath);
        System.out.println(listFilePath);
        listFile.createNewFile();
        long lastFileRecordingTimestamp = 0;
        try (Writer bw = new BufferedWriter(new FileWriter(listFile))) {
            for (FileInfo file : virtualFiles) {
                Date recordingTimestamp = new Date(file.getRecordingTimestamp());
                SimpleDateFormat sdf = new SimpleDateFormat(FILE_DATE_PATTERN);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String recordingTimestampString = sdf.format(recordingTimestamp);

                String filePath = botConfig.getStoragePath() + botConfig.getVoicesPath() + File.separator + file.getUserId() + File.separator
                        + recordingTimestampString + "_" + file.getDuration() + "_" + file.getFileId() + ".opus";
                String fileString = "file '" + filePath + "'\n";
                bw.write(fileString);
                lastFileRecordingTimestamp = file.getRecordingTimestamp();
            }
            bw.flush();
        }
        return lastFileRecordingTimestamp;
    }


    private List<FileInfo> loadFileInfo(VirtualFileInfo virtualFileInfo) {
        String query = "select  " +
                "ua.file_id, ua.duration, ua.recording_timestamp " +
                "from user_audios ua " +
                "where ua.user_id = ? " +
                "and ua.recording_timestamp > ? and ua.recording_timestamp <= ? " +
                "order by ua.recording_timestamp";
        System.out.println(
                query + "; user_id: " + virtualFileInfo.getUserId()
                        + ", from: " + virtualFileInfo.getDateFrom() + ", to: " + virtualFileInfo.getDateTo());

        List<FileInfo> parts = jdbcTemplate.queryForStream(
                query,
                (rs, rn) -> {
                    long userId = virtualFileInfo.getUserId();
                    String fileId = rs.getString("file_id");
                    int duration = Integer.parseInt(rs.getString("duration"));
                    long recordingTimestamp = rs.getTimestamp("recording_timestamp").getTime();
                    return new FileInfo(userId, fileId, duration, recordingTimestamp);
                },
                virtualFileInfo.getUserId(),
                new Timestamp(virtualFileInfo.getDateFrom()),
                new Timestamp(virtualFileInfo.getDateTo())
        ).collect(Collectors.toList());
        List<FileInfo> result = new ArrayList<>();

        long sumDuration = 0;
        final long maxDuration = 2 * 60 * 60; // 2 hours
        for (FileInfo fi : parts) {
            sumDuration += fi.getDuration();
            if (sumDuration == 0 || sumDuration < maxDuration) {
                result.add(fi);
            } else {
                break;
            }
        }
        System.out.println(Arrays.toString(result.toArray()));
        return result;
    }


}
