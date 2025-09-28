package com.andruy.backend.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import com.andruy.backend.model.DirectoryCorrection;

public class DirectoryCorrectionRowMapper implements RowMapper<DirectoryCorrection> {
    @Override
    public DirectoryCorrection mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        return new DirectoryCorrection(
            rs.getString("name"),
            rs.getString("alias")
        );
    }
}
