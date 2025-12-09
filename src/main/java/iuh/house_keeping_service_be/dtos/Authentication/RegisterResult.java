package iuh.house_keeping_service_be.dtos.Authentication;

import iuh.house_keeping_service_be.models.Account;

public record RegisterResult(
    Account account,
    String addressId
) {}
