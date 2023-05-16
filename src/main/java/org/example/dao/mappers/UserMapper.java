package org.example.dao.mappers;

import org.example.dao.UserDAO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<UserDAO>
{
    @Override
    public UserDAO mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        return new UserDAO(rs.getLong(1));
    }
}
