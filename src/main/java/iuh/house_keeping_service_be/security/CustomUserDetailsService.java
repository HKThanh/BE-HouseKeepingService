package iuh.house_keeping_service_be.security;

import iuh.house_keeping_service_be.enums.AccountStatus;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.Account;
import iuh.house_keeping_service_be.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true) // Đảm bảo session được giữ lại để load lazy collections
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Account> accounts = accountRepository.findAccountsByUsername(username);

        if (accounts.isEmpty()) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
        }

        // Use the first active account found
        Account account = accounts.stream()
                .filter(acc -> acc.getStatus() == AccountStatus.ACTIVE)
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản hoạt động cho: " + username));

        // Explicitly initialize the roles collection within transaction
        account.getRoles().size(); // Force lazy loading

        // Create authorities from all roles
        Collection<SimpleGrantedAuthority> authorities = account.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName().name()))
                .collect(Collectors.toList());

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities(authorities)
                .disabled(account.getStatus() != AccountStatus.ACTIVE)
                .build();
    }

    // Method specifically for login with role validation
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsernameAndRole(String username, RoleName roleName) throws UsernameNotFoundException {
        List<Account> accounts = accountRepository.findAccountsByUsernameAndRole(username, roleName);

        if (accounts.isEmpty()) {
            throw new UsernameNotFoundException("Không tìm thấy tài khoản với vai trò " + roleName + " cho: " + username);
        }

        // Use the first active account with the specified role
        Account account = accounts.stream()
                .filter(acc -> acc.getStatus() == AccountStatus.ACTIVE)
                .filter(acc -> acc.getRoles().stream()
                        .anyMatch(role -> role.getRoleName() == roleName))
                .findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản hoạt động với vai trò " + roleName + " cho: " + username));

        // Force lazy loading within transaction
        account.getRoles().size();

        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .authorities("ROLE_" + roleName.name())
                .disabled(account.getStatus() != AccountStatus.ACTIVE)
                .build();
    }
}