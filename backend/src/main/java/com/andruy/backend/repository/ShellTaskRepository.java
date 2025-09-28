package com.andruy.backend.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.andruy.backend.mapper.DirectoryCorrectionRowMapper;
import com.andruy.backend.model.DirectoryCorrection;

@Repository
public class ShellTaskRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<DirectoryCorrection> getDirectories() {
        String query = "SELECT name, alias FROM public.directories";

        return jdbcTemplate.query(query, new DirectoryCorrectionRowMapper());
    }

    public List<String> getEmailActions() {
        String query = "SELECT subject FROM public.email_actions";

        return jdbcTemplate.queryForList(query, String.class);
    }
}
