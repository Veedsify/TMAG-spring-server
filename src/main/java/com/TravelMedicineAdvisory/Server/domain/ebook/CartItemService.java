package com.TravelMedicineAdvisory.Server.domain.ebook;

import com.TravelMedicineAdvisory.Server.domain.user.User;
import com.TravelMedicineAdvisory.Server.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final EbookVersionRepository versionRepository;
    private final UserRepository userRepository;

    public CartItemService(CartItemRepository cartItemRepository,
                           EbookVersionRepository versionRepository,
                           UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.versionRepository = versionRepository;
        this.userRepository = userRepository;
    }

    public List<EbookDto.CartItemResponse> getCart(Long userId) {
        return cartItemRepository.findByUserIdOrderByCreatedAtAsc(userId).stream()
                .map(EbookDto.CartItemResponse::from)
                .toList();
    }

    public List<EbookDto.CartItemResponse> addToCart(Long userId, Long ebookVersionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        EbookVersion version = versionRepository.findById(ebookVersionId)
                .orElseThrow(() -> new NoSuchElementException("Ebook version not found"));

        if (!Boolean.TRUE.equals(version.getIsActive())) {
            throw new IllegalStateException("This ebook version is not available");
        }

        if (cartItemRepository.existsByUserIdAndEbookVersionId(userId, ebookVersionId)) {
            return getCart(userId);
        }

        CartItem item = new CartItem();
        item.setUser(user);
        item.setEbook(version.getEbook());
        item.setEbookVersion(version);
        cartItemRepository.save(item);

        return getCart(userId);
    }

    public void removeFromCart(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("Cart item not found"));
        if (!item.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Cart item does not belong to this user");
        }
        cartItemRepository.delete(item);
    }

    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public List<EbookDto.CartItemResponse> syncCart(Long userId, List<EbookDto.CartSyncItem> items) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        cartItemRepository.deleteByUserId(userId);

        List<CartItem> newItems = new ArrayList<>();
        for (EbookDto.CartSyncItem syncItem : items) {
            if (cartItemRepository.existsByUserIdAndEbookVersionId(userId, syncItem.ebookVersionId())) {
                continue;
            }
            EbookVersion version = versionRepository.findById(syncItem.ebookVersionId()).orElse(null);
            if (version == null || !Boolean.TRUE.equals(version.getIsActive())) {
                continue;
            }
            CartItem item = new CartItem();
            item.setUser(user);
            item.setEbook(version.getEbook());
            item.setEbookVersion(version);
            newItems.add(item);
        }
        cartItemRepository.saveAll(newItems);

        return getCart(userId);
    }
}
