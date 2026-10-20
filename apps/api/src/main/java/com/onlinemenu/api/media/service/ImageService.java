package com.onlinemenu.api.media.service;

import com.onlinemenu.api.media.model.StoredImage;
import org.springframework.web.multipart.MultipartFile;

/** Public API of the media module: validates, stores, loads and deletes images. */
public interface ImageService {

    /**
     * Validates (type: jpeg/png/webp) and stores an uploaded image.
     *
     * @param folder logical folder, e.g. "menu-items"
     * @return the storage key to keep in the database
     */
    String upload(String folder, MultipartFile file);

    /** Loads an image by its storage key. */
    StoredImage load(String key);

    /** Deletes an image; a {@code null} key is ignored. */
    void delete(String key);
}
