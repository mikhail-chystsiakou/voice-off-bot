package org.example.ffmpeg;

import lombok.extern.slf4j.Slf4j;
import org.example.config.BotConfig;
import org.example.config.Config;
import org.example.enums.MessageType;
import org.example.enums.Queries;
import org.example.storage.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
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
    FileStorage fileStorage;

    @Autowired
    public FFMPEG(Config config) {
        String osName = System.getProperty("os.name");
    }

    public FFMPEGResult produceFiles(MessageType type, long userId, long from, Long replyFolloweeId) {
        log.debug("FFMPEG called with params: {}, {}, {}, {}", type, userId, from, replyFolloweeId);
        String tempStoragePath = botConfig.getStoragePath() + botConfig.getTmpPath() + File.separator;

        VirtualFileInfo virtualFileInfo = loadVirtualFile(userId, from, System.currentTimeMillis());
        if (MessageType.REPLY.equals(type)) {
            virtualFileInfo.setReplyFolloweeId(replyFolloweeId);
        }
        log.debug("VirtualFileInfo: {}", virtualFileInfo);

        String outputFileSuffix = tempStoragePath + System.currentTimeMillis() + "_" + new Random().nextInt(9_999_999);
        String outputFile = outputFileSuffix + ".opus";
        String listFile = outputFileSuffix + ".list";
        long lastFileRecordingTimestamp = 0;

        try {
            lastFileRecordingTimestamp = createListFile(type, listFile, loadFileInfo(virtualFileInfo));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String audioAuthor = getAudioAuthor(virtualFileInfo);
        String audioTitle = getAudioTitle(type, virtualFileInfo);
        String command = MessageFormat.format(COMMAND_PATTERN, listFile, audioAuthor, audioTitle, outputFile);
        log.debug(command);
        try {
            execFFMPEG(command);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new FFMPEGResult(outputFile, listFile, audioAuthor, audioTitle, lastFileRecordingTimestamp);
    }

    private long getUserTo(long userReplyFrom, long userReplyTo, boolean isReply)
    {
        String query = isReply ? Queries.GET_USER_AUDIO_TO_FOR_REPLY.getValue() : Queries.GET_USER_AUDIO_TO.getValue();
        Object[] params = isReply ? new Object[]{userReplyFrom, userReplyTo} : new Object[]{userReplyFrom};
        return Objects.requireNonNull(jdbcTemplate.queryForObject(query, params, Timestamp.class)).getTime();
    }

    private long getUserFrom(long userReplyFrom, long userReplyTo, long from, boolean isReply)
    {
        String query = isReply ? Queries.GET_USER_AUDIO_FROM_FOR_REPLY.getValue() : Queries.GET_USER_AUDIO_FROM.getValue();
        Object[] params = isReply ? new Object[]{userReplyFrom, new Timestamp(from), userReplyTo} : new Object[]{userReplyFrom, new Timestamp(from)};
        return Objects.requireNonNull(jdbcTemplate.queryForObject(query, params, Timestamp.class)).getTime();
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

    private String getAudioTitle(MessageType type, VirtualFileInfo vfi) {
        long userReplyFrom = vfi.getUserId();
        long userReplyTo = MessageType.REPLY.equals(type) ? vfi.getReplyFolloweeId() : 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        long from = getUserFrom(userReplyFrom, userReplyTo, vfi.getDateFrom(), MessageType.REPLY.equals(type));
        long to = getUserTo(userReplyFrom, userReplyTo, MessageType.REPLY.equals(type));

        String fromStr = sdf.format(new Date(from));
        String toStr = sdf.format(new Date(to));
        String title = "Diary from " + fromStr;
        if (MessageType.REPLY.equals(type)) {
            title = "Reply from " + fromStr;
        }
        if (!toStr.equals(fromStr)) title += " to " + toStr;

        return title;
    }


    private static final Pattern VIRTUAL_FILE_REGEXP = Pattern.compile(
            "(?<userId>\\d+)_(?<dateFrom>\\d+)_(?<dateTo>\\d+)"
    );
    private static final String DATE_FORMAT = "yyyyMMddHHmmssSSS";

    private VirtualFileInfo loadVirtualFile(long userId, long from, long to) {
        VirtualFileInfo result = new VirtualFileInfo(userId, from, to);
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
        log.debug(fullCommand);
        ProcessBuilder pb = new ProcessBuilder(procCommand, "-c", commandLine);
        Process process = pb.start();
        int res = process.waitFor();
        log.debug("ffmpeg finished with result " + res);
    }

    private long createListFile(MessageType type, String listFilePath, List<FileInfo> virtualFiles) throws IOException {
        log.info("createListFile: type {}, listFilePath {}, virtualFiles {}", type, listFilePath, virtualFiles);
        File listFile = new File(listFilePath);
        new File(listFilePath.substring(0, listFilePath.lastIndexOf("/"))).mkdirs();
        log.debug(listFilePath);
        listFile.createNewFile();
        long lastFileRecordingTimestamp = 0;
        try (Writer bw = new BufferedWriter(new FileWriter(listFile))) {
            for (FileInfo file : virtualFiles) {
//                Date recordingTimestamp = new Date(file.getRecordingTimestamp());
//                SimpleDateFormat sdf = new SimpleDateFormat(FILE_DATE_PATTERN);
//                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
//                String recordingTimestampString = sdf.format(recordingTimestamp);

//                String filePrefix = botConfig.getStoragePath() + botConfig.getVoicesPath() + File.separator;
//                + file.getUserId() + File.separator
//                        + recordingTimestampString + "_" + file.getDuration() + "_" + file.getFileId() + ".opus";
                // todo
                String filePath = fileStorage.getFullFilePath(
                    file.getUserId(),
                    file.getRecordingTimestamp(),
                    file.getDuration(),
                    file.getFileId(),
                    FileStorage.DEFAULT_AUDIO_EXTENSION,
                    type,
                    file.getReplyModeFolloweeId()
                );
                String fileString = "file '" + filePath + "'\n";
                log.debug("Will concat file: " + filePath);
                bw.write(fileString);
                lastFileRecordingTimestamp = file.getRecordingTimestamp();
            }
            bw.flush();
        }
        return lastFileRecordingTimestamp;
    }


    private List<FileInfo> loadFileInfo(VirtualFileInfo virtualFileInfo) {
        String dataQuery = "select\n" +
                "    ua.file_id, ua.duration, ua.recording_timestamp, ua.message_id, null followee_id\n" +
                "from user_audios ua left join user_feedbacks uf on ua.message_id = uf.message_id \n" +
                "where ua.user_id = ?\n" +
                "  and ua.recording_timestamp > ? and ua.recording_timestamp <= ?\n" +
                "  and ua.reply_to_message_id is null\n" +
                "  and uf.user_id is null\n" +
                "order by ua.recording_timestamp";

        String replyQuery = "select\n" +
                "    ua.file_id, ua.duration, ua.recording_timestamp, ua.message_id, pm.followee_id\n" +
                "from user_audios ua, pull_messages pm\n" +
                "where ua.user_id = ?\n" +
                "  and ua.recording_timestamp > ? and ua.recording_timestamp <= ?\n" +
                "  and pm.pull_message_id = ua.reply_to_message_id\n" +
                "  and pm.followee_id = ?\n" +
                "order by ua.recording_timestamp";

        String query = dataQuery;
        Object[] args = new Object[] {virtualFileInfo.getUserId(),
                new Timestamp(virtualFileInfo.getDateFrom()),
                new Timestamp(virtualFileInfo.getDateTo())
        };

        if (virtualFileInfo.getReplyFolloweeId() != null) {
            query = replyQuery;
            args = new Object[] {
                    virtualFileInfo.getUserId(),
                    new Timestamp(virtualFileInfo.getDateFrom()),
                    new Timestamp(virtualFileInfo.getDateTo()),
                    virtualFileInfo.getReplyFolloweeId()
            };
        }



        List<FileInfo> parts = jdbcTemplate.queryForStream(
                query,
                (rs, rn) -> {
                    long userId = virtualFileInfo.getUserId();
                    String fileId = rs.getString("file_id");
                    int duration = Integer.parseInt(rs.getString("duration"));
                    long recordingTimestamp = rs.getTimestamp("recording_timestamp").getTime();
                    int messageId = rs.getInt("message_id");
                    Long replyToFolloweeId = rs.getLong("followee_id");
                    if (replyToFolloweeId == 0) replyToFolloweeId = null;
                    FileInfo fi = new FileInfo(userId, fileId, duration, recordingTimestamp, messageId, replyToFolloweeId);
                    log.debug("found file info: " + fi);
                    return fi;
                },
                args
        ).collect(Collectors.toList());

        log.debug("loading file infos, query: {}, args: {}, result: {}",
                query,
                args,
                parts
        );
        List<FileInfo> result = new ArrayList<>();
        long sumDuration = 0;
        final long maxDuration = 1 * 60 * 60; // 2 hour max
        for (FileInfo fi : parts) {
            sumDuration += fi.getDuration();
            if (result.isEmpty() || sumDuration < maxDuration) {
                result.add(fi);
            } else {
                break;
            }
        }
        log.debug("Items after 1h filtration: {}", result);
        return result;
    }


}
