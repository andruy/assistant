package com.andruy.backend.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class InstagramRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private final String ACCOUNT_NAME = "account_name";
    private final String CREATED_ON = "created_on";

    public int saveUser(String suffix, String user, Timestamp date) {
        String query = "INSERT INTO public.ig_" + suffix + " (" + ACCOUNT_NAME + ", " + CREATED_ON + ") VALUES (?, ?)";

        return jdbcTemplate.update(query, user, date);
    }

    public List<Timestamp> getTimestamps() {
        String query = "SELECT DISTINCT " + CREATED_ON + " FROM public.ig_nmf";

        return jdbcTemplate.queryForList(query, Timestamp.class);
    }

    public List<Timestamp> getTimestamps(String suffix) {
        String query = "SELECT DISTINCT " + CREATED_ON + " FROM public.ig_" + suffix + " ORDER BY " + CREATED_ON + " DESC";

        return jdbcTemplate.queryForList(query, Timestamp.class);
    }

    public List<String> getUsers(String suffix, Timestamp date) {
        String query = "SELECT " + ACCOUNT_NAME + " FROM public.ig_" + suffix + " WHERE " + CREATED_ON + " = ?";

        return jdbcTemplate.queryForList(query, String.class, date);
    }

    public Timestamp getLatestTimestamp(String suffix) {
        String query = "SELECT MAX(" + CREATED_ON + ") FROM public.ig_" + suffix;
        return jdbcTemplate.queryForObject(query, Timestamp.class);
    }

    public int protectAccount(String user, Timestamp date) {
        String query = "UPDATE public.ig_nmf SET PROTECTED = 1 WHERE " + ACCOUNT_NAME + " = ? AND " + CREATED_ON + " = ?";

        return jdbcTemplate.update(query, user, date);
    }
}
