package com.onlinemenu.api.media.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Storage settings, bound from {@code app.storage.*}.
 *
 * @param localRoot directory where images are written (mounted as a Docker volume)
 */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String localRoot) {
}
