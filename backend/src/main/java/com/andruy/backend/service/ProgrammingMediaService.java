package com.andruy.backend.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.andruy.backend.exception.NotFoundException;
import com.andruy.backend.model.DirectoryListing;
import com.andruy.backend.model.MediaFile;

import jakarta.annotation.PostConstruct;

@Service
public class ProgrammingMediaService {
    private static final Logger log = LoggerFactory.getLogger(ProgrammingMediaService.class);
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
        "mp4", "webm", "ogg", "mov", "avi", "mkv"
    );

    private final Path rootDir;

    public ProgrammingMediaService(@Value("${my.programming.directory}") String programmingDirectory) {
        this.rootDir = Path.of(programmingDirectory).toAbsolutePath().normalize();
    }

    @PostConstruct
    void reportRootAvailability() {
        if (Files.isDirectory(rootDir)) {
            log.info("Programming media root is available: {}", rootDir);
        } else {
            log.warn("Programming media root is not present on this host: {} (endpoints will return empty listings)", rootDir);
        }
    }

    public DirectoryListing list(String relativePath) {
        if (!Files.isDirectory(rootDir)) {
            return new DirectoryListing(List.of(), List.of());
        }
        Path dir = resolveDirectory(relativePath);
        File[] entries = dir.toFile().listFiles();
        if (entries == null) {
            return new DirectoryListing(List.of(), List.of());
        }

        List<String> folders = Arrays.stream(entries)
            .filter(File::isDirectory)
            .map(File::getName)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();

        List<MediaFile> files = Arrays.stream(entries)
            .filter(File::isFile)
            .filter(f -> VIDEO_EXTENSIONS.contains(getExtension(f.getName())))
            .map(f -> new MediaFile(
                f.getName(),
                f.length(),
                getContentType(f.getName()),
                f.lastModified()
            ))
            .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
            .toList();

        return new DirectoryListing(folders, files);
    }

    public Resource getMediaResource(String relativePath) {
        return new FileSystemResource(resolveFile(relativePath));
    }

    public long getFileSize(String relativePath) throws IOException {
        return Files.size(resolveFile(relativePath));
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

    private Path resolveDirectory(String relativePath) {
        Path resolved = resolveAndValidate(relativePath);
        if (!Files.exists(resolved) || !Files.isDirectory(resolved)) {
            throw new NotFoundException("Directory not found: " + relativePath);
        }
        return resolved;
    }

    private Path resolveFile(String relativePath) {
        Path resolved = resolveAndValidate(relativePath);
        if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
            throw new NotFoundException("File not found: " + relativePath);
        }
        return resolved;
    }

    private Path resolveAndValidate(String relativePath) {
        String safe = relativePath == null ? "" : relativePath;
        Path resolved = rootDir.resolve(safe).normalize();
        if (!resolved.startsWith(rootDir)) {
            throw new SecurityException("Path escapes root: " + relativePath);
        }
        return resolved;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');

        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }

        return filename.substring(dot + 1).toLowerCase();
    }
}
