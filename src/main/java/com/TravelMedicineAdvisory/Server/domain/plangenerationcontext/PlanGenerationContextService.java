package com.TravelMedicineAdvisory.Server.domain.plangenerationcontext;

import com.TravelMedicineAdvisory.Server.core.storage.Attachment;
import com.TravelMedicineAdvisory.Server.core.storage.StorageService;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class PlanGenerationContextService {

    private final PlanGenerationContextRepository repository;
    private final StorageService storageService;

    public PlanGenerationContextService(PlanGenerationContextRepository repository, StorageService storageService) {
        this.repository = repository;
        this.storageService = storageService;
    }

    public List<PlanGenerationContextResponse> findAll() {
        return repository.findAllActive().stream().map(this::toResponse).toList();
    }

    public List<PlanGenerationContext> findEnabled() {
        return repository.findEnabled();
    }

    public PlanGenerationContextResponse create(String title, MultipartFile file) {
        String sourceType = resolveSourceType(file.getOriginalFilename(), file.getContentType());
        String rawText = extractText(file, sourceType);
        String synthesizedText = synthesize(rawText);

        Attachment stored = storageService.store(file, "admin-plan-contexts", null, "PlanGenerationContext");

        PlanGenerationContext entity = new PlanGenerationContext();
        entity.setTitle(title);
        entity.setSourceType(sourceType);
        entity.setFileName(file.getOriginalFilename());
        entity.setContentType(file.getContentType());
        entity.setStoragePath(stored.getStoragePath());
        entity.setRawText(rawText);
        entity.setSynthesizedText(synthesizedText);
        entity.setActive(true);
        return toResponse(repository.save(entity));
    }

    public PlanGenerationContextResponse updateActive(Long id, boolean active) {
        PlanGenerationContext entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Plan generation context not found"));
        entity.setActive(active);
        return toResponse(repository.save(entity));
    }

    public void delete(Long id) {
        PlanGenerationContext entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Plan generation context not found"));
        if (entity.getStoragePath() != null) {
            storageService.delete(entity.getStoragePath());
        }
        repository.delete(entity);
    }

    private PlanGenerationContextResponse toResponse(PlanGenerationContext entity) {
        return new PlanGenerationContextResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getSourceType(),
                entity.getFileName(),
                entity.getContentType(),
                entity.getStoragePath(),
                entity.getSynthesizedText(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String resolveSourceType(String fileName, String contentType) {
        String lowerName = fileName != null ? fileName.toLowerCase() : "";
        String lowerContentType = contentType != null ? contentType.toLowerCase() : "";
        if (lowerName.endsWith(".pdf") || lowerContentType.contains("pdf")) {
            return "pdf";
        }
        return "text";
    }

    private String extractText(MultipartFile file, String sourceType) {
        try {
            if ("pdf".equals(sourceType)) {
                return extractPdf(file.getBytes());
            }
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read uploaded context file", ex);
        }
    }

    private String extractPdf(byte[] bytes) throws IOException {
        try (PdfReader reader = new PdfReader(bytes)) {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            StringBuilder out = new StringBuilder();
            int pages = reader.getNumberOfPages();
            for (int i = 1; i <= pages; i++) {
                out.append(extractor.getTextFromPage(i)).append("\n");
            }
            return out.toString().trim();
        }
    }

    private String synthesize(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }
        String normalized = rawText.replace("\r", "").trim();
        if (normalized.length() <= 2000) {
            return normalized;
        }

        String intro = normalized.substring(0, Math.min(1000, normalized.length())).trim();
        String ending = normalized.substring(Math.max(0, normalized.length() - 1000)).trim();
        return intro + "\n\n...\n\n" + ending;
    }
}
