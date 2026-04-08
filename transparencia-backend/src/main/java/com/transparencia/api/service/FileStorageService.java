package com.transparencia.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadRootPath;

    public FileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadRootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRootPath);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo crear el directorio de subida", ex);
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo es obligatorio");
        }

        String originalFilename = StringUtils.cleanPath(
            file.getOriginalFilename() == null ? "archivo" : file.getOriginalFilename()
        );
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Nombre de archivo invalido");
        }

        String generatedFileName = UUID.randomUUID() + "-" + originalFilename;

        Path targetDirectory = this.uploadRootPath;
        if (StringUtils.hasText(subDirectory)) {
            String cleanSubDirectory = StringUtils.cleanPath(subDirectory);
            if (cleanSubDirectory.contains("..")) {
                throw new IllegalArgumentException("Subdirectorio invalido");
            }
            targetDirectory = this.uploadRootPath.resolve(cleanSubDirectory).normalize();
        }

        try {
            Files.createDirectories(targetDirectory);
            Path targetFile = targetDirectory.resolve(generatedFileName).normalize();
            if (!targetFile.startsWith(this.uploadRootPath)) {
                throw new IllegalArgumentException("Ruta de almacenamiento invalida");
            }
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            return this.uploadRootPath.relativize(targetFile).toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo almacenar el archivo", ex);
        }
    }

    public Resource loadAsResource(String relativePath) {
        try {
            Path filePath = resolvePath(relativePath);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new IllegalArgumentException("No se pudo leer el archivo solicitado");
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar el archivo", ex);
        }
    }

    public Path resolvePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new IllegalArgumentException("La ruta relativa es obligatoria");
        }

        String cleanPath = StringUtils.cleanPath(relativePath);
        if (cleanPath.contains("..")) {
            throw new IllegalArgumentException("Ruta relativa invalida");
        }

        Path resolvedPath = this.uploadRootPath.resolve(cleanPath).normalize();
        if (!resolvedPath.startsWith(this.uploadRootPath)) {
            throw new IllegalArgumentException("Ruta fuera del directorio permitido");
        }
        return resolvedPath;
    }
}
