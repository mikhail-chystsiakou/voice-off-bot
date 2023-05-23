package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.converter.Converter;
import org.example.dto.UserDTO;
import org.example.enums.MessageType;
import org.example.enums.Queries;
import org.example.ffmpeg.FFMPEG;
import org.example.ffmpeg.FFMPEGResult;
import org.example.model.UserInfo;
import org.example.service.impl.UserServiceImpl;
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

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("{userId}")
    public ResponseEntity<Resource> pull(@PathVariable Long userId, HttpServletRequest request) {

        MessageType type = MessageType.DATA;
        long nextPullTimestamp = System.currentTimeMillis();

        List<UserServiceImpl.FolloweePullTimestamp> followeesPullTimestamps = jdbcTemplate.queryForStream(
            Queries.GET_LAST_PULL_TIME.getValue(),
            (rs, rn) -> {
                UserServiceImpl.FolloweePullTimestamp obj = new UserServiceImpl.FolloweePullTimestamp();
                obj.setFolloweeId(rs.getLong("user_id"));
                obj.setLastPullTimestamp(rs.getTimestamp("last_pull_timestamp").getTime());

                return obj;
            },
            userId
        ).collect(Collectors.toList());

        if (followeesPullTimestamps.size() > 0)
        {
            FFMPEGResult localFile = ffmpeg.produceFiles(type,
                                                         followeesPullTimestamps.get(0).getFolloweeId(),
                                                         followeesPullTimestamps.get(0).getLastPullTimestamp(), nextPullTimestamp,
                                                         userId
            );

            FileSystemResource fsr = new FileSystemResource(localFile.getAbsoluteFileURL());

            String contentType = request.getServletContext().getMimeType(localFile.getAbsoluteFileURL());


            if(contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fsr.getFilename() + "\"")
                .body(fsr);
        }
        return ResponseEntity.noContent().build();
    }
}
