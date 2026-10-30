package vn.io.huangnosimp.nautilusserver.user.services;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;
import vn.io.huangnosimp.nautilusserver.user.UserData;
import vn.io.huangnosimp.nautilusserver.user.UserUtils;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserUtilsImpl implements UserUtils {
    private final UserRepository userRepository;
    private final StorageUtils storageUtils;

    @Override
    public Optional<UserData> getUserDataByAuthUserId(String authUserId) {
        log.debug("Fetching user data for authUserId: {}", authUserId);
        var response =
                userRepository
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
        log.info("User data for authUserId: {} fetched successfully", authUserId);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(String authUserId, String username) {
        log.debug("Creating user with authUserId: {}, username: {}", authUserId, username);
        userRepository.save(
                User.builder().authUserId(authUserId).username(username).serverCount(0).build());
        log.info(
                "User with authUserId: {}, username: {} created successfully",
                authUserId,
                username);
    }
}
