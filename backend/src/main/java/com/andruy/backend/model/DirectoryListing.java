package com.andruy.backend.model;

import java.util.List;

public record DirectoryListing(List<String> folders, List<MediaFile> files) { }
