package iuh.house_keeping_service_be.services.AdminService.impl;

import iuh.house_keeping_service_be.models.AdminProfile;
import iuh.house_keeping_service_be.repositories.AdminProfileRepository;
import iuh.house_keeping_service_be.services.AdminService.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminProfileRepository adminProfileRepository;

    @Override
    public AdminProfile findByAccountId(String accountId) {
        return adminProfileRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new RuntimeException("Admin not found with account ID: " + accountId));
    }
}
