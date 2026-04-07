package com.TravelMedicineAdvisory.Server.domain.ebook;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "ebook_cart_items")
@SQLDelete(sql = "UPDATE ebook_cart_items SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebook_id", nullable = false)
    private Ebook ebook;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ebook_version_id", nullable = false)
    private EbookVersion ebookVersion;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Ebook getEbook() { return ebook; }
    public void setEbook(Ebook ebook) { this.ebook = ebook; }

    public EbookVersion getEbookVersion() { return ebookVersion; }
    public void setEbookVersion(EbookVersion ebookVersion) { this.ebookVersion = ebookVersion; }
}
