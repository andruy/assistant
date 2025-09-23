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
    private final String ACCOUNT_NAME = "account_name";
    private final String CREATED_ON = "created_on";

    public int saveUser(String suffix, String user, Date date) {
        String query = "INSERT INTO public.ig_" + suffix + " (" + ACCOUNT_NAME + ", " + CREATED_ON + ") VALUES (?, ?)";

        return jdbcTemplate.update(query, user, date);
    }

    public List<Date> getDates() {
        String query = "SELECT DISTINCT " + CREATED_ON + " FROM public.ig_nmf";

        return jdbcTemplate.queryForList(query, Date.class);
    }

    public List<String> getUsers(String suffix, Date date) {
        String query = "SELECT " + ACCOUNT_NAME + " FROM public.ig_" + suffix + " WHERE " + CREATED_ON + " = ?";

        return jdbcTemplate.queryForList(query, String.class, date);
    }

    public int protectAccount(String user, Date date) {
        String query = "UPDATE public.ig_nmf SET PROTECTED = 1 WHERE " + ACCOUNT_NAME + " = ? AND " + CREATED_ON + " = ?";

        return jdbcTemplate.update(query, user, date);
    }
}
