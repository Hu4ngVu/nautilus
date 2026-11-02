package vn.io.huangnosimp.nautilusserver.user.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.api.UserQuery;
import vn.io.huangnosimp.nautilusserver.events.UserData;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserQueryImpl implements UserQuery {
    private final UserRepository userRepository;
    private final ResourceStorage resourceStorage;

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public Optional<UserData> getUserDataByAuthUserId(String authUserId) {
        return userRepository
                .findByAuthUserId(authUserId)
                .map(
                        user ->
                                UserData.builder()
                                        .authUserId(user.getAuthUserId())
                                        .username(user.getUsername())
                                        .avatarUrl(
                                                resourceStorage.createSignedGetUrl(
                                                        user.getAvatarId()))
                                        .build());
    }
}
