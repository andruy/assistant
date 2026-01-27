package com.andruy.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.andruy.backend.model.DirectoryCorrection;

class DirectoryCorrectionRowMapperTest {
    private DirectoryCorrectionRowMapper rowMapper;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        rowMapper = new DirectoryCorrectionRowMapper();
        resultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Should map ResultSet to DirectoryCorrection correctly")
    void mapRow_MapsFieldsCorrectly() throws SQLException {
        when(resultSet.getString("name")).thenReturn("Original Name");
        when(resultSet.getString("alias")).thenReturn("Corrected Alias");

        DirectoryCorrection result = rowMapper.mapRow(resultSet, 0);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Original Name");
        assertThat(result.alias()).isEqualTo("Corrected Alias");
    }

    @Test
    @DisplayName("Should handle null values in ResultSet")
    void mapRow_WithNullValues_ReturnsRecordWithNulls() throws SQLException {
        when(resultSet.getString("name")).thenReturn(null);
        when(resultSet.getString("alias")).thenReturn(null);

        DirectoryCorrection result = rowMapper.mapRow(resultSet, 0);

        assertThat(result).isNotNull();
        assertThat(result.name()).isNull();
        assertThat(result.alias()).isNull();
    }

    @Test
    @DisplayName("Should handle empty strings")
    void mapRow_WithEmptyStrings_ReturnsRecordWithEmptyStrings() throws SQLException {
        when(resultSet.getString("name")).thenReturn("");
        when(resultSet.getString("alias")).thenReturn("");

        DirectoryCorrection result = rowMapper.mapRow(resultSet, 0);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEmpty();
        assertThat(result.alias()).isEmpty();
    }

    @Test
    @DisplayName("Should handle special characters in values")
    void mapRow_WithSpecialCharacters_MapsCorrectly() throws SQLException {
        when(resultSet.getString("name")).thenReturn("Name with 'quotes' and \"double\"");
        when(resultSet.getString("alias")).thenReturn("Alias-with_special.chars@#$");

        DirectoryCorrection result = rowMapper.mapRow(resultSet, 0);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Name with 'quotes' and \"double\"");
        assertThat(result.alias()).isEqualTo("Alias-with_special.chars@#$");
    }

    @Test
    @DisplayName("Should handle unicode characters")
    void mapRow_WithUnicodeCharacters_MapsCorrectly() throws SQLException {
        when(resultSet.getString("name")).thenReturn("Name with émojis 🎵");
        when(resultSet.getString("alias")).thenReturn("日本語");

        DirectoryCorrection result = rowMapper.mapRow(resultSet, 0);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Name with émojis 🎵");
        assertThat(result.alias()).isEqualTo("日本語");
    }

    @Test
    @DisplayName("Should handle very long strings")
    void mapRow_WithLongStrings_MapsCorrectly() throws SQLException {
        String longName = "A".repeat(500);
        String longAlias = "B".repeat(500);
        when(resultSet.getString("name")).thenReturn(longName);
        when(resultSet.getString("alias")).thenReturn(longAlias);

        DirectoryCorrection result = rowMapper.mapRow(resultSet, 0);

        assertThat(result).isNotNull();
        assertThat(result.name()).hasSize(500);
        assertThat(result.alias()).hasSize(500);
    }

    @Test
    @DisplayName("Should ignore row number parameter")
    void mapRow_WithDifferentRowNumbers_MapsSameWay() throws SQLException {
        when(resultSet.getString("name")).thenReturn("Name");
        when(resultSet.getString("alias")).thenReturn("Alias");

        DirectoryCorrection result0 = rowMapper.mapRow(resultSet, 0);
        DirectoryCorrection result1 = rowMapper.mapRow(resultSet, 1);
        DirectoryCorrection result100 = rowMapper.mapRow(resultSet, 100);

        assertThat(result0).isEqualTo(result1);
        assertThat(result1).isEqualTo(result100);
    }
}
