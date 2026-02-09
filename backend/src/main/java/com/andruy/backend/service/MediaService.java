package com.andruy.backend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.andruy.backend.exception.NotFoundException;
import com.andruy.backend.model.MediaFile;

@Service
public class MediaService {
    private static final Path MEDIA_DIR = Path.of("media");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
        "mp4", "webm", "ogg", "mov", "avi", "mkv"
    );

    public List<MediaFile> listMediaFiles() {
        File dir = MEDIA_DIR.toFile();

        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(files)
            .filter(File::isFile)
            .filter(f -> {
                String ext = getExtension(f.getName());
                return VIDEO_EXTENSIONS.contains(ext);
            })
            .map(f -> new MediaFile(
                f.getName(),
                f.length(),
                getContentType(f.getName()),
                f.lastModified()
            ))
            .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
            .toList();
    }

    public Resource getMediaResource(String filename) {
        Path filePath = resolveAndValidate(filename);

        if (filePath == null) {
            throw new IllegalArgumentException("File path must not be null");
        }

        return new FileSystemResource(filePath);
    }

    public long getFileSize(String filename) throws IOException {
        Path filePath = resolveAndValidate(filename);

        return Files.size(filePath);
    }

    public String getContentType(String filename) {
        String ext = getExtension(filename);
        return switch (ext) {
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "ogg" -> "video/ogg";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";
            case "mkv" -> "video/x-matroska";
            default -> "application/octet-stream";
        };
    }

    private Path resolveAndValidate(String filename) {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new SecurityException("Invalid filename");
        }

        Path filePath = MEDIA_DIR.resolve(filename).normalize();
        if (!filePath.startsWith(MEDIA_DIR)) {
            throw new SecurityException("Invalid filename");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new NotFoundException("Media file not found: " + filename);
        }

        return filePath;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');

        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }

        return filename.substring(dot + 1).toLowerCase();
    }
}
