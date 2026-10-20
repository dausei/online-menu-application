package com.onlinemenu.api.media.service.impl;

import com.onlinemenu.api.common.exception.BadRequestException;
import com.onlinemenu.api.media.model.ImageType;
import com.onlinemenu.api.media.model.StoredImage;
import com.onlinemenu.api.media.service.ImageService;
import com.onlinemenu.api.media.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.UUID;

/** Validates images and delegates storing to {@link StorageService}. */
@Service
public class ImageServiceImpl implements ImageService {

    private final StorageService storage;

    public ImageServiceImpl(StorageService storage) {
        this.storage = storage;
    }

    @Override
    public String upload(String folder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        ImageType type = ImageType.fromContentType(file.getContentType())
                .orElseThrow(() -> new BadRequestException(
                        "Unsupported image type: " + file.getContentType() + ". Allowed: jpeg, png, webp"));

        String key = folder + "/" + UUID.randomUUID() + "." + type.extension();
        try (InputStream in = file.getInputStream()) {
            storage.put(key, in);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read uploaded file", e);
        }
        return key;
    }

    @Override
    public StoredImage load(String key) {
        String contentType = ImageType.fromKey(key)
                .map(ImageType::contentType)
                .orElse("application/octet-stream");
        return new StoredImage(storage.load(key), contentType);
    }

    @Override
    public void delete(String key) {
        if (key != null) {
            storage.delete(key);
        }
    }
}
