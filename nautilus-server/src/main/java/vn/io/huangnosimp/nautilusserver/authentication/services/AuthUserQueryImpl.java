package vn.io.huangnosimp.nautilusserver.authentication.services;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.api.AuthUserQuery;
import vn.io.huangnosimp.nautilusserver.authentication.repositories.AuthUserRepository;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class AuthUserQueryImpl implements AuthUserQuery {
    private final AuthUserRepository authUserRepository;

    @Override
    @Logging(level = Level.DEBUG)
    public Optional<String> getAuthUserIdByUsername(String username) {
        return authUserRepository.findIdByUsername(username);
    }

    @Override
    @Logging(level = Level.DEBUG)
    public Map<String, String> getUsernamesByAuthUserIds(Collection<String> authUserIds) {
        if (authUserIds.isEmpty()) return Map.of();
        return authUserRepository.findIdAndUsernameByIdIn(Set.copyOf(authUserIds)).stream()
                .collect(
                        Collectors.toMap(
                                AuthUserRepository.IdUsername::getId,
                                AuthUserRepository.IdUsername::getUsername));
    }
}
