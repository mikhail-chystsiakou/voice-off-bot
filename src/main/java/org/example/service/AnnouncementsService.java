package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.FileTypes;
import org.example.enums.Queries;
import org.example.model.AnnouncementInfo;
import org.example.util.ExecuteFunction;
import org.example.util.SendVideoFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.enums.Queries.GET_ANNOUNCEMENTS_INFO;

@Component
@Slf4j
public class AnnouncementsService
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Lazy
    ExecuteFunction executeFunction;

    @Autowired
    @Lazy
    SendVideoFunction sendVideoFunction;

    public List<AnnouncementInfo> getAnnouncements()
    {
        List<AnnouncementInfo> result = new ArrayList<>();
        return jdbcTemplate.queryForStream(
            GET_ANNOUNCEMENTS_INFO.getValue(),
            (rs, rn) -> {
                AnnouncementInfo obj = new AnnouncementInfo();

                Long id = rs.getLong("id");
                String text = rs.getString("text");
                String fileId = rs.getString("file_id");
                FileTypes fileType = FileTypes.valueOf(rs.getString("file_type"));

                Optional<AnnouncementInfo> existingAnnouncement = result.stream().filter(a -> a.getId().equals(id)).findFirst();

                if (existingAnnouncement.isPresent()){
                    obj = existingAnnouncement.get();
                }
                else {
                    obj.setId(id);
                    obj.setText(text);
                }

                switch (fileType)
                {
                    case VIDEO -> obj.setVideoIds(addFileIdToList(obj.getVideoIds(), fileId));
                    case AUDIO -> obj.setAudioIds(addFileIdToList(obj.getAudioIds(), fileId));
                    case IMG -> obj.setImgIds(addFileIdToList(obj.getImgIds(), fileId));
                }

                return obj;
            }
        ).collect(Collectors.toList());
    }

    private List<String> addFileIdToList(List<String> list, String fileId) {
        List<String> newList = list == null ? new ArrayList() : new ArrayList(list);
        newList.add(fileId);
        return newList;
    }

    public void runAnnouncementProcess(AnnouncementInfo announcementInfo)
    {
        log.info("run announcement process, announcements id = " + announcementInfo.getId());
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS announcement_" + announcementInfo.getId() + "_temp\n" +
                                "(\n" +
                                "    chat_id bigint, \n" +
                                "\tconstraint announcemens_" + announcementInfo.getId() + "_temp_fk FOREIGN KEY(chat_id) REFERENCES users (chat_id) on delete cascade\n" +
                                ")");
        jdbcTemplate.update("insert into announcement_" + announcementInfo.getId() + "_temp (chat_id) select chat_id from users");

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> sentAnnouncements(announcementInfo), 1000, TimeUnit.MILLISECONDS);
    }

    public void sentAnnouncements(AnnouncementInfo announcementInfo){
        log.info("Sending announcement with id: " + announcementInfo.getId());

        boolean isAnnouncementExists = isAnnouncementExists(announcementInfo.getId());

        log.info("announcement with id: " + announcementInfo.getId() + ". isAnnouncementExists: " + isAnnouncementExists);

        if (isAnnouncementExists){
            SendVideo sendVideo = null;

            if (announcementInfo.getVideoIds() != null)
            {
                sendVideo = new SendVideo();

                sendVideo.setCaption(announcementInfo.getText());
                sendVideo.setVideo(new InputFile(new File(announcementInfo.getVideoIds().get(0))));
                sendVideo.setParseMode("Markdown");
            }

            List<Long> chatIds = jdbcTemplate.queryForList("DELETE FROM announcement_" + announcementInfo.getId() + "_temp where chat_id in (select chat_id from announcement_" + announcementInfo.getId() + "_temp limit 10) RETURNING chat_id", Long.class);
            SendVideo finalSendVideo = sendVideo;
            chatIds.forEach(chatId -> {
                if (finalSendVideo != null)
                {
                    finalSendVideo.setChatId(chatId);

                    try
                    {
                        sendVideoFunction.execute(finalSendVideo);
                    }
                    catch (TelegramApiException e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> sentAnnouncements(announcementInfo), 1000, TimeUnit.MILLISECONDS);
        }
        else
        {
            log.info("deleting temp announcement table for announcement with id = " + announcementInfo.getId());
            deleteTempAnnouncementTable(announcementInfo.getId());
        }
    }

    private void deleteTempAnnouncementTable(Long announcementId)
    {
        jdbcTemplate.update("DROP TABLE IF EXISTS announcement_" + announcementId + "_temp");
    }

    private boolean isAnnouncementExists(Long announcementsId)
    {
        int result = jdbcTemplate.queryForObject("select count(*) from announcement_" + announcementsId + "_temp", Integer.class);
        return result != 0;
    }

    public void setPassedForAnnouncement(Long id)
    {
        jdbcTemplate.update(Queries.SET_PASSED_TO_ANNOUNCEMENT.getValue(), id);
    }
}
