package com.TravelMedicineAdvisory.Server.domain.countryaccommodation;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;

import com.TravelMedicineAdvisory.Server.domain.country.Country;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "country_accommodations")
@SQLDelete(sql = "UPDATE country_accommodations SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")

public class CountryAccommodation extends BaseEntity {

    private String city;
    private String type;
    private String name;
    @Column(name = "avg_price_per_night", precision = 10, scale = 2)
    private BigDecimal avgPricePerNight;
    private String currency;
    private Double rating;
    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;


    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAvgPricePerNight() {
        return avgPricePerNight;
    }

    public void setAvgPricePerNight(BigDecimal avgPricePerNight) {
        this.avgPricePerNight = avgPricePerNight;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }
}
