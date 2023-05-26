package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.converter.Converter;
import org.example.dto.UserDTO;
import org.example.enums.MessageType;
import org.example.enums.Queries;
import org.example.ffmpeg.FFMPEG;
import org.example.ffmpeg.FFMPEGResult;
import org.example.model.UserInfo;
import org.example.service.UpdateHandler;
import org.example.service.impl.UserServiceImpl;
import org.example.util.PullProcessingSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.enums.Queries.SET_PULL_TIMESTAMP;

@RestController
@RequestMapping("api/v1/audio")
@RequiredArgsConstructor
public class AudioController
{
    private final UserServiceImpl userService;

    private final Converter<UserInfo, UserDTO> userConverter;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    FFMPEG ffmpeg;

    @Autowired
    PullProcessingSet pullProcessingSet;

    @GetMapping("{userId}")
    public ResponseEntity<Object> pull(@PathVariable Long userId) {

        boolean res = pullProcessingSet.getAndSetPullProcessingForUser(userId);
        System.out.println("pullProcessingSet.getAndSetPullProcessingForUser(userId) = " + res);
        if (!res) {
            return ResponseEntity.badRequest().body("Request already processed");
        }

        if (!userService.isDataAvailable(userId)){
            System.out.println("no data available");
            pullProcessingSet.finishProcessingForUser(userId);
            return ResponseEntity.noContent().build();
        }

        MessageType type = MessageType.DATA;

        UserServiceImpl.FolloweePullTimestamp followeesPullTimestamps = jdbcTemplate.queryForStream(
            Queries.GET_LAST_PULL_TIME.getValue(),
            (rs, rn) -> {
                UserServiceImpl.FolloweePullTimestamp obj = new UserServiceImpl.FolloweePullTimestamp();
                obj.setFolloweeId(rs.getLong("user_id"));
                obj.setLastPullTimestamp(rs.getTimestamp("last_pull_timestamp").getTime());

                return obj;
            },
            userId
        ).findFirst().orElse(null);

        System.out.println("followeesPullTimestamps" + followeesPullTimestamps);

        if (followeesPullTimestamps != null)
        {
            FFMPEGResult localFile = ffmpeg.produceFiles(type,
                                                         followeesPullTimestamps.getFolloweeId(),
                                                         followeesPullTimestamps.getLastPullTimestamp(),
                                                         userId
            );

            FileSystemResource fsr = new FileSystemResource(localFile.getAbsoluteFileURL());

            jdbcTemplate.update(SET_PULL_TIMESTAMP.getValue(),
                                new Timestamp(localFile.getLastFileRecordingTimestamp()), userId, followeesPullTimestamps.getFolloweeId()
            );
            System.out.println("setting timestamp for data on " + followeesPullTimestamps.getFolloweeId() + "-" + userId);

//            userService.cleanup(localFile.getAbsoluteFileURL());
            pullProcessingSet.finishProcessingForUser(userId);

            return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/opus"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fsr.getFilename() + "\"")
                .body(fsr);
        }
        pullProcessingSet.finishProcessingForUser(userId);
        return ResponseEntity.noContent().build();
    }
}
