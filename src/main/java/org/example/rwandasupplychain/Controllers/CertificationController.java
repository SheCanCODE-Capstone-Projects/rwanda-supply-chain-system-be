package org.example.rwandasupplychain.Controllers;

import org.example.rwandasupplychain.DTOs.CertificationDtos.CertificationResponse;
import org.example.rwandasupplychain.Services.CertificationService;
import org.example.rwandasupplychain.Services.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/certifications")
public class CertificationController {

    private final CertificationService certificationService;
    private final FileStorageService fileStorageService;

    public CertificationController(CertificationService certificationService, FileStorageService fileStorageService) {
        this.certificationService = certificationService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificationResponse> upload(
            @RequestParam UUID productId,
            @RequestParam String certificationName,
            @RequestParam(required = false) String issuingBody,
            @RequestParam(required = false) String certificateNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
            @RequestParam MultipartFile file
    ) {
        CertificationResponse response = certificationService.upload(
                productId, certificationName, issuingBody, certificateNumber, issueDate, expiryDate, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/product/{productId}")
    public List<CertificationResponse> getByProduct(@PathVariable UUID productId) {
        return certificationService.getByProduct(productId);
    }

    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        try {
            Resource resource = new UrlResource(fileStorageService.resolve(filename).toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }
}