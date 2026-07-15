package org.example.rwandasupplychain.Services;

import org.example.rwandasupplychain.DTOs.CertificationDtos.CertificationResponse;
import org.example.rwandasupplychain.Entities.Product;
import org.example.rwandasupplychain.Entities.QualityCertification;
import org.example.rwandasupplychain.Repositories.QualityCertificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CertificationService {

    private final QualityCertificationRepository certificationRepository;
    private final ProductService productService;
    private final FileStorageService fileStorageService;

    public CertificationService(QualityCertificationRepository certificationRepository,
                                ProductService productService,
                                FileStorageService fileStorageService) {
        this.certificationRepository = certificationRepository;
        this.productService = productService;
        this.fileStorageService = fileStorageService;
    }

    public CertificationResponse upload(UUID productId, String certificationName, String issuingBody,
                                        String certificateNumber, LocalDate issueDate, LocalDate expiryDate,
                                        MultipartFile file) {
        Product product = productService.findEntity(productId);
        String fileUrl = fileStorageService.store(file);

        QualityCertification certification = new QualityCertification();
        certification.setProduct(product);
        certification.setCertificationName(certificationName);
        certification.setIssuingBody(issuingBody);
        certification.setCertificateNumber(certificateNumber);
        certification.setIssueDate(issueDate);
        certification.setExpiryDate(expiryDate);
        certification.setFileUrl(fileUrl);

        return toResponse(certificationRepository.save(certification));
    }

    @Transactional(readOnly = true)
    public List<CertificationResponse> getByProduct(UUID productId) {
        return certificationRepository.findByProduct_Id(productId).stream().map(this::toResponse).toList();
    }

    private CertificationResponse toResponse(QualityCertification c) {
        return new CertificationResponse(
                c.getId(), c.getProduct().getId(), c.getCertificationName(), c.getIssuingBody(),
                c.getCertificateNumber(), c.getIssueDate(), c.getExpiryDate(), c.getFileUrl(), c.getUploadedAt()
        );
    }
}