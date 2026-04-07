package com.TravelMedicineAdvisory.Server.core.seeder;

import com.TravelMedicineAdvisory.Server.domain.ebook.Ebook;
import com.TravelMedicineAdvisory.Server.domain.ebook.EbookRepository;
import com.TravelMedicineAdvisory.Server.domain.ebook.EbookVersion;
import com.TravelMedicineAdvisory.Server.domain.ebook.EbookVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class EbookSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(EbookSeeder.class);

    @Value("${app.features.seeder.enabled:true}")
    private boolean seederEnabled;

    private final EbookRepository ebookRepository;
    private final EbookVersionRepository versionRepository;

    public EbookSeeder(EbookRepository ebookRepository, EbookVersionRepository versionRepository) {
        this.ebookRepository = ebookRepository;
        this.versionRepository = versionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seederEnabled) return;

        if (ebookRepository.existsBySlug("tmag-travel-health-guide")) {
            return; // Already seeded
        }

        Ebook ebook = new Ebook();
        ebook.setTitle("The TMAG Travel Health Guide");
        ebook.setSlug("tmag-travel-health-guide");
        ebook.setShortDescription("The definitive guide to staying healthy on every international journey — written by certified travel medicine specialists.");
        ebook.setDescription("""
                Whether you're a first-time traveller or a seasoned globetrotter, The TMAG Travel Health Guide gives you the medical knowledge you need to stay safe anywhere in the world.

                Written by board-certified travel medicine physicians with decades of field experience, this comprehensive guide covers:

                • Pre-travel health assessments and vaccination schedules
                • Destination-specific disease risks and prevention strategies
                • Food & water safety, traveller's diarrhoea management
                • High-altitude illness, jet lag, and motion sickness
                • Managing chronic conditions while travelling
                • Mental health and wellbeing abroad
                • Emergency medical preparedness and travel insurance guidance
                • Post-travel health checks and repatriation

                Available in regional editions tailored to the specific health landscape of your destination.
                """);
        ebook.setAuthor("Dr. Sarah Chen, MD, DTM&H & Dr. James Okonkwo, MBBS, FFTM");
        ebook.setAuthorBio("Dr. Chen is a board-certified travel medicine specialist with 15 years of experience in tropical and expedition medicine. Dr. Okonkwo is a Fellow of the Faculty of Travel Medicine with extensive experience in Sub-Saharan African health systems.");
        ebook.setCoverUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800");
        ebook.setPageCount(248);
        ebook.setPublishedYear(2025);
        ebook.setIsbn("978-0-000-00001-0");
        ebook.setIsActive(true);
        ebook.setIsFeatured(true);
        ebook.setSortOrder(1);
        ebookRepository.save(ebook);

        // ─── Versions ─────────────────────────────────────────────────────────

        createVersion(ebook, "Global Edition", null, "Worldwide", "Global",
                new BigDecimal("29.99"), "USD", "$",
                "https://cdn.travelmedicineadvisory.com/ebooks/tmag-guide-global.pdf", "ebooks/tmag-guide-global.pdf",
                new BigDecimal("12.4"), 1);

        createVersion(ebook, "European Edition", "EU", "Europe", "Europe",
                new BigDecimal("24.99"), "EUR", "€",
                "https://cdn.travelmedicineadvisory.com/ebooks/tmag-guide-europe.pdf", "ebooks/tmag-guide-europe.pdf",
                new BigDecimal("12.1"), 2);

        createVersion(ebook, "UK Edition", "GB", "United Kingdom", "Europe",
                new BigDecimal("22.99"), "GBP", "£",
                "https://cdn.travelmedicineadvisory.com/ebooks/tmag-guide-uk.pdf", "ebooks/tmag-guide-uk.pdf",
                new BigDecimal("12.1"), 3);

        createVersion(ebook, "African Edition", null, "Africa", "Africa",
                new BigDecimal("12000"), "NGN", "₦",
                "https://cdn.travelmedicineadvisory.com/ebooks/tmag-guide-africa.pdf", "ebooks/tmag-guide-africa.pdf",
                new BigDecimal("11.8"), 4);

        createVersion(ebook, "South Asian Edition", null, "South Asia", "Asia",
                new BigDecimal("1999"), "INR", "₹",
                "https://cdn.travelmedicineadvisory.com/ebooks/tmag-guide-south-asia.pdf", "ebooks/tmag-guide-south-asia.pdf",
                new BigDecimal("11.8"), 5);

        logger.info("Ebook seeding completed — 'The TMAG Travel Health Guide' with 5 regional editions");
    }

    private void createVersion(Ebook ebook, String label, String countryCode, String countryName,
                                String region, BigDecimal price, String currency, String currencySymbol,
                                String fileUrl, String fileKey, BigDecimal fileSizeMb, int sortOrder) {
        EbookVersion version = new EbookVersion();
        version.setEbook(ebook);
        version.setLabel(label);
        version.setCountryCode(countryCode);
        version.setCountryName(countryName);
        version.setRegion(region);
        version.setPrice(price);
        version.setCurrency(currency);
        version.setCurrencySymbol(currencySymbol);
        version.setFileUrl(fileUrl);
        version.setFileKey(fileKey);
        version.setFileSizeMb(fileSizeMb);
        version.setIsActive(true);
        version.setSortOrder(sortOrder);
        versionRepository.save(version);
    }
}
