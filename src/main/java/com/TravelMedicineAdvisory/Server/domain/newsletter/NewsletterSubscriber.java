package com.TravelMedicineAdvisory.Server.domain.newsletter;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;


@Entity
@Table(name = "newsletter_subscribers")
@SQLDelete(sql = "UPDATE newsletter_subscribers SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")

public class NewsletterSubscriber extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
