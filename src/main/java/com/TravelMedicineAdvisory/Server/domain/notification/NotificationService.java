package com.TravelMedicineAdvisory.Server.domain.notification;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository repository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Page<NotificationResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public NotificationResponse findById(Long id) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));
        return toResponse(entity);
    }

    public NotificationResponse create(NotificationRequest request) {
        Notification entity = new Notification();
        mapRequestToEntity(request, entity);
        Notification saved = repository.save(entity);
        return toResponse(saved);
    }

    public NotificationResponse update(Long id, NotificationRequest request) {
        Notification entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));
        mapRequestToEntity(request, entity);
        Notification saved = repository.save(entity);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Notification not found");
        }
        repository.deleteById(id);
    }

    private NotificationResponse toResponse(Notification entity) {
        return new NotificationResponse(
            entity.getId(),
            entity.getTitle(),
            entity.getMessage(),
            entity.getType(),
            entity.getLink(),
            entity.getRead(),
            entity.getUser() != null ? entity.getUser().getId() : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    private void mapRequestToEntity(NotificationRequest request, Notification entity) {
        entity.setTitle(request.title());
        entity.setMessage(request.message());
        entity.setType(request.type());
        entity.setLink(request.link());
        entity.setRead(request.isRead());
        if (request.userId() != null) {
            User user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));
            entity.setUser(user);
        }
    }
}
