package iuh.house_keeping_service_be.security;

import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.Role;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // For JWT filter - load by username only
        List<Account> accounts = accountRepository.findAccountsByUsername(username);

        if (accounts.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Use the first active account found
        Account account = accounts.stream()
                .filter(acc -> acc.getStatus() == AccountStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("No active account found for: " + username));

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities("ROLE_" + account.getRole().name())
                .disabled(account.getStatus() != AccountStatus.ACTIVE)
                .build();
    }

    // Method specifically for login with role validation
    public UserDetails loadUserByUsernameAndRole(String username, Role role) throws UsernameNotFoundException {
        List<Account> accounts = accountRepository.findAccountsByUsernameAndRole(username, role);

        if (accounts.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username + " and role: " + role);
        }

        Account account = accounts.get(0); // Take the first one as per your existing logic

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new UsernameNotFoundException("Account is not active: " + username);
        }

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities("ROLE_" + account.getRole().name())
                .disabled(false)
                .build();
    }
}
