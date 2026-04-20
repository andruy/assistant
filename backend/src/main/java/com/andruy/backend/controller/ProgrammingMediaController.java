package com.andruy.backend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.DirectoryListing;
import com.andruy.backend.service.ProgrammingMediaService;

@RestController
@RequestMapping("/api/programming")
public class ProgrammingMediaController {
    private final ProgrammingMediaService service;

    public ProgrammingMediaController(ProgrammingMediaService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public ResponseEntity<DirectoryListing> list(@RequestParam(defaultValue = "") String path) {
        return ResponseEntity.ok(service.list(path));
    }

    @GetMapping("/stream")
    public ResponseEntity<Resource> stream(
            @RequestParam String path,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

        long fileSize = service.getFileSize(path);
        String contentType = service.getContentType(path);

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            long start = Long.parseLong(ranges[0]);
            long end = ranges.length > 1 && !ranges[1].isEmpty()
                ? Long.parseLong(ranges[1])
                : fileSize - 1;

            if (start >= fileSize) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header("Content-Range", "bytes */" + fileSize)
                    .build();
            }

            end = Math.min(end, fileSize - 1);
            long contentLength = end - start + 1;

            Resource resource = service.getMediaResource(path);
            InputStream inputStream = resource.getInputStream();
            inputStream.skip(start);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
            headers.setContentLength(contentLength);
            headers.setContentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE));
            headers.set("Accept-Ranges", "bytes");

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(new InputStreamResource(inputStream));
        }

        Resource resource = service.getMediaResource(path);
        return ResponseEntity.ok()
            .contentLength(fileSize)
            .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .header("Accept-Ranges", "bytes")
            .body(resource);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) throws IOException {
        long fileSize = service.getFileSize(path);
        String contentType = service.getContentType(path);
        Resource resource = service.getMediaResource(path);
        String filename = Paths.get(path).getFileName().toString();

        return ResponseEntity.ok()
            .contentLength(fileSize)
            .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(resource);
    }
}
