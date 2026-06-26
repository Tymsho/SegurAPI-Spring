package com.first.api.first_api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@SuppressWarnings("null")
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde se guardarán los archivos subidos.", ex);
        }
    }

    public String guardarArchivo(MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new RuntimeException("El archivo está vacío");
        }
        
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Validación de extensión segura
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png");
        
        if (!allowedExtensions.contains(fileExtension)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Solo se permiten: " + allowedExtensions);
        }

        try {
            if (fileName.contains("..")) {
                throw new IllegalArgumentException("El archivo contiene una secuencia de ruta inválida " + fileName);
            }
            // Generar nombre unico
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo guardar el archivo " + fileName + ". Por favor intenta de nuevo!", ex);
        }
    }
}
