package iuh.house_keeping_service_be.services.PhoneVerificationService;

import iuh.house_keeping_service_be.dtos.Authentication.PhoneOtpRequest;
import iuh.house_keeping_service_be.dtos.Authentication.PhoneOtpSendResponse;
import iuh.house_keeping_service_be.dtos.Authentication.PhoneOtpVerifyRequest;
import iuh.house_keeping_service_be.dtos.Authentication.PhoneOtpVerifyResponse;

public interface PhoneVerificationService {
    PhoneOtpSendResponse sendOtp(PhoneOtpRequest request);

    PhoneOtpVerifyResponse verifyOtp(PhoneOtpVerifyRequest request);
}

