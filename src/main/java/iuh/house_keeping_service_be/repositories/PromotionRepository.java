package iuh.house_keeping_service_be.repositories;

import iuh.house_keeping_service_be.models.Promotion;
import iuh.house_keeping_service_be.enums.DiscountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    
    // Find by promo code
    Optional<Promotion> findByPromoCode(String promoCode);
    
    // Find active promotions
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true " +
           "AND p.startDate <= :currentTime AND p.endDate >= :currentTime")
    List<Promotion> findActivePromotions(@Param("currentTime") LocalDateTime currentTime);
    
    // Check if promo code is available for customer
    @Query("SELECT p FROM Promotion p WHERE p.promoCode = :promoCode " +
           "AND p.isActive = true " +
           "AND p.startDate <= :currentTime AND p.endDate >= :currentTime " +
           "AND (p.usageLimit IS NULL OR " +
           "     (SELECT COUNT(b) FROM Booking b WHERE b.promotion.promotionId = p.promotionId) < p.usageLimit)")
    Optional<Promotion> findAvailablePromotion(@Param("promoCode") String promoCode,
                                              @Param("currentTime") LocalDateTime currentTime);
    
    // Check promo code usage by customer
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.promotion.promoCode = :promoCode " +
           "AND b.customer.customerId = :customerId")
    long countPromoCodeUsageByCustomer(@Param("promoCode") String promoCode,
                                      @Param("customerId") String customerId);
    
    // Find by discount type
    List<Promotion> findByDiscountTypeAndIsActiveTrue(DiscountType discountType);
    
    // Check if promo code exists
    boolean existsByPromoCode(String promoCode);
    
    // Get total usage count for promotion
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.promotion.promotionId = :promotionId")
    long getTotalUsageCount(@Param("promotionId") Integer promotionId);
}