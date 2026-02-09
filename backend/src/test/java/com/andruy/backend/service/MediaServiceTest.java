package com.andruy.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

import com.andruy.backend.exception.NotFoundException;
import com.andruy.backend.model.MediaFile;

@SuppressWarnings("ResultOfMethodCallIgnored")
class MediaServiceTest {

    private MediaService mediaService;
    private File mediaDir;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService();
        mediaDir = new File("media");
        mediaDir.mkdirs();
    }

    @AfterEach
    void tearDown() {
        File[] files = mediaDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    // --- listMediaFiles ---

    @Test
    @DisplayName("Should return empty list when media directory is empty")
    void listMediaFiles_WhenEmpty_ReturnsEmptyList() {
        List<MediaFile> result = mediaService.listMediaFiles();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return video files from media directory")
    void listMediaFiles_WhenVideoFilesExist_ReturnsThem() throws IOException {
        createTestFile("video1.mp4");
        createTestFile("video2.webm");

        List<MediaFile> result = mediaService.listMediaFiles();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(MediaFile::name)
            .containsExactly("video1.mp4", "video2.webm");
    }

    @Test
    @DisplayName("Should filter out non-video files")
    void listMediaFiles_FiltersNonVideoFiles() throws IOException {
        createTestFile("video.mp4");
        createTestFile("readme.txt");
        createTestFile("image.png");

        List<MediaFile> result = mediaService.listMediaFiles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("video.mp4");
    }

    @Test
    @DisplayName("Should recognize all supported video extensions")
    void listMediaFiles_RecognizesAllVideoExtensions() throws IOException {
        createTestFile("a.mp4");
        createTestFile("b.webm");
        createTestFile("c.ogg");
        createTestFile("d.mov");
        createTestFile("e.avi");
        createTestFile("f.mkv");

        List<MediaFile> result = mediaService.listMediaFiles();

        assertThat(result).hasSize(6);
    }

    @Test
    @DisplayName("Should return files sorted alphabetically by name")
    void listMediaFiles_ReturnsSortedByName() throws IOException {
        createTestFile("charlie.mp4");
        createTestFile("alpha.mp4");
        createTestFile("bravo.mp4");

        List<MediaFile> result = mediaService.listMediaFiles();

        assertThat(result).extracting(MediaFile::name)
            .containsExactly("alpha.mp4", "bravo.mp4", "charlie.mp4");
    }

    @Test
    @DisplayName("Should populate file size in MediaFile")
    void listMediaFiles_PopulatesFileSize() throws IOException {
        createTestFile("video.mp4", "some content here");

        List<MediaFile> result = mediaService.listMediaFiles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).size()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should populate content type in MediaFile")
    void listMediaFiles_PopulatesContentType() throws IOException {
        createTestFile("video.mp4");

        List<MediaFile> result = mediaService.listMediaFiles();

        assertThat(result.get(0).type()).isEqualTo("video/mp4");
    }

    // --- getContentType ---

    @Test
    @DisplayName("Should return correct content types for all video extensions")
    void getContentType_ReturnsCorrectTypes() {
        assertThat(mediaService.getContentType("file.mp4")).isEqualTo("video/mp4");
        assertThat(mediaService.getContentType("file.webm")).isEqualTo("video/webm");
        assertThat(mediaService.getContentType("file.ogg")).isEqualTo("video/ogg");
        assertThat(mediaService.getContentType("file.mov")).isEqualTo("video/quicktime");
        assertThat(mediaService.getContentType("file.avi")).isEqualTo("video/x-msvideo");
        assertThat(mediaService.getContentType("file.mkv")).isEqualTo("video/x-matroska");
    }

    @Test
    @DisplayName("Should return octet-stream for unknown extensions")
    void getContentType_UnknownExtension_ReturnsOctetStream() {
        assertThat(mediaService.getContentType("file.xyz")).isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("Should handle case-insensitive extensions")
    void getContentType_CaseInsensitive() {
        assertThat(mediaService.getContentType("file.MP4")).isEqualTo("video/mp4");
        assertThat(mediaService.getContentType("file.WebM")).isEqualTo("video/webm");
    }

    // --- getMediaResource ---

    @Test
    @DisplayName("Should return resource for existing file")
    void getMediaResource_WhenFileExists_ReturnsResource() throws IOException {
        createTestFile("video.mp4");

        Resource resource = mediaService.getMediaResource("video.mp4");

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    @DisplayName("Should throw NotFoundException for missing file")
    void getMediaResource_WhenFileMissing_ThrowsNotFoundException() {
        assertThatThrownBy(() -> mediaService.getMediaResource("nonexistent.mp4"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("nonexistent.mp4");
    }

    @Test
    @DisplayName("Should throw SecurityException for path traversal with ..")
    void getMediaResource_WithDotDot_ThrowsSecurityException() {
        assertThatThrownBy(() -> mediaService.getMediaResource("../etc/passwd"))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("Should throw SecurityException for path traversal with slash")
    void getMediaResource_WithSlash_ThrowsSecurityException() {
        assertThatThrownBy(() -> mediaService.getMediaResource("sub/file.mp4"))
            .isInstanceOf(SecurityException.class);
    }

    @Test
    @DisplayName("Should throw SecurityException for path traversal with backslash")
    void getMediaResource_WithBackslash_ThrowsSecurityException() {
        assertThatThrownBy(() -> mediaService.getMediaResource("sub\\file.mp4"))
            .isInstanceOf(SecurityException.class);
    }

    // --- getFileSize ---

    @Test
    @DisplayName("Should return correct file size")
    void getFileSize_ReturnsCorrectSize() throws IOException {
        String content = "test video content";
        createTestFile("video.mp4", content);

        long size = mediaService.getFileSize("video.mp4");

        assertThat(size).isEqualTo(content.length());
    }

    @Test
    @DisplayName("Should throw NotFoundException for missing file size lookup")
    void getFileSize_WhenFileMissing_ThrowsNotFoundException() {
        assertThatThrownBy(() -> mediaService.getFileSize("nonexistent.mp4"))
            .isInstanceOf(NotFoundException.class);
    }

    // --- helpers ---

    private void createTestFile(String name) throws IOException {
        createTestFile(name, "dummy");
    }

    private void createTestFile(String name, String content) throws IOException {
        File file = new File(mediaDir, name);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}
