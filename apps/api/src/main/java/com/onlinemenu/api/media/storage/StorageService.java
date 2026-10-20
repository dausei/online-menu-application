package com.onlinemenu.api.media.storage;

import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * Abstraction over the place where binary files (food images) are stored.
 *
 * <p>Today it is implemented by {@link com.onlinemenu.api.media.storage.impl.LocalFileStorageService} (a Docker named volume).
 * Later an S3/MinIO implementation can be added without touching other modules —
 * the database only stores the object key, never a full path or URL.
 */
public interface StorageService {

    /** Saves the data under the given key, overwriting an existing object. */
    void put(String key, InputStream data);

    /** Loads an object. Throws {@link com.onlinemenu.api.common.exception.NotFoundException} if absent. */
    Resource load(String key);

    /** Deletes an object; does nothing if it does not exist. */
    void delete(String key);
}
