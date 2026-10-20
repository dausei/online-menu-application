package com.onlinemenu.api.media.model;

import java.util.Arrays;
import java.util.Optional;

/** Image formats accepted for menu item photos. */
public enum ImageType {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    WEBP("image/webp", "webp");

    private final String contentType;
    private final String extension;

    ImageType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public String contentType() { return contentType; }

    public String extension() { return extension; }

    public static Optional<ImageType> fromContentType(String contentType) {
        return Arrays.stream(values())
                .filter(t -> t.contentType.equalsIgnoreCase(contentType))
                .findFirst();
    }

    /** Detects the type by the key's extension, e.g. "menu-items/abc.png". */
    public static Optional<ImageType> fromKey(String key) {
        String lower = key.toLowerCase();
        return Arrays.stream(values())
                .filter(t -> lower.endsWith("." + t.extension))
                .findFirst();
    }
}
