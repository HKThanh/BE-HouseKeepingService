package iuh.house_keeping_service_be.dtos.Review;

public record ReviewDetailResponse(
        Integer criteriaId,
        String criteriaName,
        double rating
) {
}