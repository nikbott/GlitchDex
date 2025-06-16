package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.config.StorageProperties;
import br.ufscar.glitchdex.exception.StorageException;
import br.ufscar.glitchdex.exception.StorageFileNotFoundException;
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

@Service
public class FileStorageService {

    private final Path rootLocation;

    @Autowired
    public FileStorageService(StorageProperties properties) {
        rootLocation = Paths.get(properties.getLocation());
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            return null; // No file to store
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String newFilename = UUID.randomUUID() + "." + extension;

        try {
            if (originalFilename.contains("..")) {
                throw new StorageException("Cannot store file with relative path outside current directory " + originalFilename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, rootLocation.resolve(newFilename), StandardCopyOption.REPLACE_EXISTING);
                return newFilename;
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + newFilename, e);
        }
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    public void delete(String filename) {
        if (null == filename || filename.isBlank()) {
            return; // No file to delete
        }
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file " + filename, e);
        }
    }
}