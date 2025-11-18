package iuh.house_keeping_service_be.websocket.voice;

import java.security.Principal;
import java.util.Objects;

/**
 * Principal used for voice booking WebSocket sessions.
 */
public class VoiceBookingPrincipal implements Principal {

    private final String name;

    public VoiceBookingPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoiceBookingPrincipal that = (VoiceBookingPrincipal) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "VoiceBookingPrincipal{" +
                "name='" + name + '\'' +
                '}';
    }
}
