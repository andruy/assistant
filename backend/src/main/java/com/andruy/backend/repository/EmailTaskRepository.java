package com.andruy.backend.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmailTaskRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<String> getEmailActions() {
        String query = "SELECT subject FROM public.email_actions";

        return jdbcTemplate.queryForList(query, String.class);
    }
}
