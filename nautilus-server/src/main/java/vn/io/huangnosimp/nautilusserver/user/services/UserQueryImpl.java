package vn.io.huangnosimp.nautilusserver.user.services;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.api.UserQuery;
import vn.io.huangnosimp.nautilusserver.events.UserData;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserQueryImpl implements UserQuery {
    private final UserRepository userRepository;
    private final ResourceStorage resourceStorage;

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public Optional<UserData> getUserDataByAuthUserId(String authUserId) {
        return userRepository.findByAuthUserId(authUserId).map(this::toUserData);
    }

    @Override
    @Logging(level = Level.DEBUG)
    public Map<String, UserData> getUserDataByAuthUserIds(Collection<String> authUserIds) {
        if (authUserIds.isEmpty()) return Map.of();
        return userRepository.findAllByAuthUserIdIn(Set.copyOf(authUserIds)).stream()
                .map(this::toUserData)
                .collect(Collectors.toMap(UserData::authUserId, Function.identity()));
    }

    private UserData toUserData(User user) {
        return UserData.builder()
                .authUserId(user.getAuthUserId())
                .username(user.getUsername())
                .avatarUrl(resourceStorage.getPublicUrl(user.getAvatarId()))
                .build();
    }
}
