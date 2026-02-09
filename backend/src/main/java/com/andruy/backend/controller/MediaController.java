package com.andruy.backend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andruy.backend.model.MediaFile;
import com.andruy.backend.service.MediaService;

@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping
    public ResponseEntity<List<MediaFile>> listMedia() {
        return ResponseEntity.ok(mediaService.listMediaFiles());
    }

    @GetMapping("/stream/{filename}")
    public ResponseEntity<Resource> streamMedia(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {

        long fileSize = mediaService.getFileSize(filename);
        String contentType = mediaService.getContentType(filename);

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

            Resource resource = mediaService.getMediaResource(filename);
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

        Resource resource = mediaService.getMediaResource(filename);
        return ResponseEntity.ok()
            .contentLength(fileSize)
            .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .header("Accept-Ranges", "bytes")
            .body(resource);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadMedia(@PathVariable String filename) throws IOException {
        long fileSize = mediaService.getFileSize(filename);
        String contentType = mediaService.getContentType(filename);
        Resource resource = mediaService.getMediaResource(filename);

        return ResponseEntity.ok()
            .contentLength(fileSize)
            .contentType(MediaType.parseMediaType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(resource);
    }
}
