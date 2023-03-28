package org.example.service;

import org.example.config.DataSourceConfig;
import org.example.dao.UserDAO;
import org.example.dao.mappers.UserMapper;
import org.example.enums.Queries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class UserService
{
    JdbcTemplate jdbcTemplate;

    @Autowired
    public UserService(DataSourceConfig dataSourceConfig)
    {
        this.jdbcTemplate = dataSourceConfig.jdbcTemplate();
    }

    public int addUser(Long userId, Long chatId){
        return jdbcTemplate.update(Queries.ADD_USER.getValue(), userId, chatId);
    }

    public int saveAudio(Long userId, String fileId) {
        return jdbcTemplate.update(Queries.ADD_AUDIO.getValue(), userId, fileId);
    }

    public Long getUserById(Long userId){
        return jdbcTemplate.query(Queries.GET_USER_BY_ID.getValue(), new Object[]{userId}, new UserMapper())
            .stream().findFirst().map(UserDAO::getId).orElse(null);
    }

    public Integer getSubscriberByUserIdAndSubscriberId(Long userId, Long subscriberId){
       return jdbcTemplate.queryForObject(Queries.CHECK_FOLLOWING.getValue(), new Object[]{userId, subscriberId}, Integer.class);
    }

    public int addContact(Long userId, Long contactId){
        jdbcTemplate.update(Queries.REMOVE_REQUEST_TO_CONFIRM.getValue(), userId, contactId);
        return jdbcTemplate.update(Queries.ADD_CONTACT.getValue(), userId, contactId);
    }

    public Long getChatIdByUserId(Long contactId)
    {
        return jdbcTemplate.queryForObject(Queries.GET_CHAT_ID_BY_USER_ID.getValue(), new Object[]{contactId}, Long.class);
    }

    public int addRequestToConfirm(Long userId, Long contactId)
    {
        return jdbcTemplate.update(Queries.ADD_REQUEST_TO_CONFIRM.getValue(), userId, contactId);
    }

    public Long getUserByFoloweeId(Long foloweeId)
    {
        return jdbcTemplate.queryForObject(Queries.GET_USER_ID_BY_FOLLOWEE_ID.getValue(), new Object[]{foloweeId}, Long.class);
    }

    public Integer getRequestRecord(Long userId, Long foloweeId)
    {
        return jdbcTemplate.queryForObject(Queries.CHECK_FOLLOWEE.getValue(), new Object[]{userId, foloweeId}, Integer.class);
    }
}
