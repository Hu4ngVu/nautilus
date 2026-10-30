package vn.io.huangnosimp.nautilusserver.user.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.user.UserData;
import vn.io.huangnosimp.nautilusserver.user.UserUtils;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserUtilsImpl implements UserUtils {
    private final UserRepository userRepository;

    @Override
    public Optional<UserData> getUserDataByAuthUserId(String authUserId) {
        return userRepository
                .findByAuthUserId(authUserId)
                .map(
                        user ->
                                UserData.builder()
                                        .authUserId(user.getAuthUserId())
                                        .username(user.getUsername())
                                        .avatarKey(user.getAvatarKey())
                                        .build());
    }
}
