package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.config.StorageProperties;
import br.ufscar.glitchdex.exception.StorageException;
import br.ufscar.glitchdex.exception.StorageFileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for handling file storage operations.
 * Manages storing, loading, and deleting files from the filesystem.
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path rootLocation;

    /**
     * Constructs a FileStorageService and initializes the storage directory.
     *
     * @param properties The storage configuration properties.
     */
    @Autowired
    public FileStorageService(StorageProperties properties) {
        rootLocation = Paths.get(properties.getLocation());
        try {
            Files.createDirectories(rootLocation);
            log.info("Storage directory created at: {}", rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage location: {}", rootLocation, e);
            throw new StorageException("Could not initialize storage", e);
        }
    }

    /**
     * Stores a file in the storage directory with a unique name.
     *
     * @param file The file to store.
     * @return The unique filename of the stored file, or null if the file is empty.
     * @throws StorageException if there is an issue storing the file.
     */
    public String store(MultipartFile file) {
        if (file.isEmpty() || null == file.getOriginalFilename()) {
            log.warn("Attempt to store an empty or unnamed file");
            return null;
        }
        try {
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = filename.substring(filename.lastIndexOf('.'));
            String newFilename = UUID.randomUUID() + extension;
            Path destinationFile = rootLocation.resolve(newFilename);
            log.info("Storing file {} as {}", filename, newFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("File {} stored successfully", newFilename);
            return newFilename;
        } catch (IOException e) {
            log.error("Failed to store file {}", file.getOriginalFilename(), e);
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }


    /**
     * Loads a file as a resource from the storage directory.
     *
     * @param filename The name of the file to load.
     * @return The loaded file as a Resource.
     * @throws StorageFileNotFoundException if the file does not exist or cannot be read.
     */
    public Resource loadAsResource(String filename) {
        log.info("Loading file as resource: {}", filename);
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                log.warn("Could not read file: {}", filename);
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            log.error("Could not read file due to malformed URL: {}", filename, e);
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    /**
     * Deletes a file from the storage directory.
     *
     * @param filename The name of the file to delete.
     */
    public void delete(String filename) {
        if (null == filename || filename.isBlank()) {
            return;
        }
        log.info("Deleting file: {}", filename);
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
            log.info("File {} deleted successfully", filename);
        } catch (IOException e) {
            log.error("Failed to delete file {}: {}", filename, e.getMessage());
        }
    }

    /**
     * Replaces an old file with a new one.
     *
     * @param oldFilename The name of the file to replace.
     * @param newFile     The new file.
     * @return The unique filename of the new file.
     */
    public String replace(String oldFilename, MultipartFile newFile) {
        if (null == newFile || newFile.isEmpty()) {
            return oldFilename;
        }
        log.info("Replacing file {} with a new file", oldFilename);
        String newFilename = store(newFile);
        if (null != newFilename) {
            delete(oldFilename);
        }
        return newFilename;
    }
}