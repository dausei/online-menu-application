package com.onlinemenu.api.media.model;

import org.springframework.core.io.Resource;

/** An image loaded from storage together with its content type. */
public record StoredImage(Resource resource, String contentType) {
}
