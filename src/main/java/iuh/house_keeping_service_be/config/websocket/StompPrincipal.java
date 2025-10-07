package iuh.house_keeping_service_be.config.websocket;

import lombok.Getter;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

@Getter
public class StompPrincipal implements Principal {

    private final String accountId;
    private final String username;
    private final Set<String> roles;

    public StompPrincipal(String accountId, String username, Set<String> roles) {
        this.accountId = accountId;
        this.username = username;
        this.roles = roles == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(Set.copyOf(roles));
    }

    @Override
    public String getName() {
        return accountId;
    }
}