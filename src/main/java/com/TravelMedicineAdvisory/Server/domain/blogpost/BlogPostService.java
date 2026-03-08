package com.TravelMedicineAdvisory.Server.domain.blogpost;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlogPostService {

    private final BlogPostRepository repository;
    private final UserRepository userRepository;

    public BlogPostService(BlogPostRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Page<BlogPostResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public BlogPostResponse findById(Long id) {
        BlogPost entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("BlogPost not found"));
        return toResponse(entity);
    }

    public BlogPostResponse create(BlogPostRequest request) {
        BlogPost entity = new BlogPost();
        mapRequestToEntity(request, entity);
        BlogPost saved = repository.save(entity);
        return toResponse(saved);
    }

    public BlogPostResponse update(Long id, BlogPostRequest request) {
        BlogPost entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("BlogPost not found"));
        mapRequestToEntity(request, entity);
        BlogPost saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("BlogPost not found");
        }
        repository.deleteById(id);
    }

    private BlogPostResponse toResponse(BlogPost entity) {
        return new BlogPostResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getSlug(),
            entity.getExcerpt(),
            entity.getContent(),
            entity.getCategory(),
            entity.getReadTime(),
            entity.getPublishedAt(),
            entity.getPublished(),
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getFeaturedImage() != null ? entity.getFeaturedImage().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(BlogPostRequest request, BlogPost entity) {
        entity.setTitle(request.title());
        entity.setSlug(request.slug());
        entity.setExcerpt(request.excerpt());
        entity.setContent(request.content());
        entity.setCategory(request.category());
        entity.setReadTime(request.readTime());
        entity.setPublishedAt(request.publishedAt());
        entity.setPublished(request.isPublished());
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
