package com.andruy.backend.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InstagramRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int saveUser(String suffix, String user, Date date) {
        String query = "INSERT INTO IG_" + suffix + " (ACCOUNT_NAME, RECORDED_ON) VALUES (?, ?)";

        return jdbcTemplate.update(query, user, date);
    }

    public List<Date> getDates() {
        String query = "SELECT DISTINCT RECORDED_ON FROM IG_NMF";

        return jdbcTemplate.queryForList(query, Date.class);
    }

    public List<String> getUsers(String suffix, Date date) {
        String query = "SELECT ACCOUNT_NAME FROM IG_" + suffix + " WHERE TRUNC(RECORDED_ON) = ?";

        return jdbcTemplate.queryForList(query, String.class, date);
    }

    public int protectAccount(String suffix, String user, Date date) {
        String query = "UPDATE IG_" + suffix + " SET PROTECTED = 1 WHERE ACCOUNT_NAME = ? AND TRUNC(RECORDED_ON) = ?";

        return jdbcTemplate.update(query, user, date);
    }
}
