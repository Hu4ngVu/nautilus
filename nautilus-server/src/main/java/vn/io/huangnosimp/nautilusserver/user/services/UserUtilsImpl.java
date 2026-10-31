package vn.io.huangnosimp.nautilusserver.user.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;
import vn.io.huangnosimp.nautilusserver.user.UserData;
import vn.io.huangnosimp.nautilusserver.user.UserUtils;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserUtilsImpl implements UserUtils {
    private final UserRepository userRepository;
    private final StorageUtils storageUtils;

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
                                                storageUtils.createSignedGetUrl(
                                                        user.getAuthUserId(),
                                                        StorageType.USER_AVATAR))
                                        .build());
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void createUser(String authUserId, String username) {
        userRepository.save(
                User.builder().authUserId(authUserId).username(username).serverCount(0).build());
    }
}
