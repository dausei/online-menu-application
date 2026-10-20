package com.onlinemenu.api.media.storage.impl;

import com.onlinemenu.api.common.exception.BadRequestException;
import com.onlinemenu.api.common.exception.NotFoundException;
import com.onlinemenu.api.media.config.StorageProperties;
import com.onlinemenu.api.media.storage.StorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Stores files on the local file system (inside the container: a named Docker volume). */
@Service
public class LocalFileStorageService implements StorageService {

    private final Path root;

    public LocalFileStorageService(StorageProperties properties) {
        this.root = Path.of(properties.localRoot()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create storage directory " + root, e);
        }
    }

    @Override
    public void put(String key, InputStream data) {
        Path target = resolve(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot store object " + key, e);
        }
    }

    @Override
    public Resource load(String key) {
        Path path = resolve(key);
        if (!Files.isRegularFile(path)) {
            throw new NotFoundException("Object", key);
        }
        return new FileSystemResource(path);
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot delete object " + key, e);
        }
    }

    /** Resolves a key inside the root and blocks path traversal like "../../etc/passwd". */
    private Path resolve(String key) {
        Path path = root.resolve(key).normalize();
        if (!path.startsWith(root)) {
            throw new BadRequestException("Invalid object key");
        }
        return path;
    }
}
