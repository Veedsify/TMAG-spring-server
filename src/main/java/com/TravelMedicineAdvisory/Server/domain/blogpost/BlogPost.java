package com.TravelMedicineAdvisory.Server.domain.blogpost;

import com.TravelMedicineAdvisory.Server.core.base.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import com.TravelMedicineAdvisory.Server.core.storage.Attachment;
import com.TravelMedicineAdvisory.Server.domain.user.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_posts")
@SQLDelete(sql = "UPDATE blog_posts SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class BlogPost extends BaseEntity {

    private String title;
    private String slug;
    @Column(columnDefinition = "TEXT")
    private String excerpt;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String category;
    @Column(name = "read_time")
    private Integer readTime;
    @Column(name = "is_published")
    private Boolean isPublished;
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "featured_image_id")
    private Attachment featuredImage;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getReadTime() {
        return readTime;
    }

    public void setReadTime(Integer readTime) {
        this.readTime = readTime;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Boolean getPublished() {
        return isPublished;
    }

    public void setPublished(Boolean published) {
        isPublished = published;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Attachment getFeaturedImage() {
        return featuredImage;
    }

    public void setFeaturedImage(Attachment featuredImage) {
        this.featuredImage = featuredImage;
    }
}
