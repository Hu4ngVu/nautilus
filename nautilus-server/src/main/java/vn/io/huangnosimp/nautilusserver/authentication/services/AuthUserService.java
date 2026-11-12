package vn.io.huangnosimp.nautilusserver.authentication.services;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.slf4j.event.Level;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class AuthUserService implements UserDetailsService {
    private final AuthUserRepository authUserRepository;

    @Override
    @NullMarked
    @Logging(level = Level.DEBUG)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Accepts either a username or an email; usernames may contain '@', so try username first
        return authUserRepository
                .findByUsername(username)
                .or(() -> authUserRepository.findByEmail(username))
                .orElseThrow(
                        () ->
                                new UsernameNotFoundException(
                                        "User not found with userId: " + username));
    }
}
