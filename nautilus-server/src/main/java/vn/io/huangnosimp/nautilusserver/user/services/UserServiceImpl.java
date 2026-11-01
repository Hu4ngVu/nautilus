package vn.io.huangnosimp.nautilusserver.user.services;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.authentication.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateUsername;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.ChangeUsernameRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final StorageUtils storageUtils;
    private final AuthUserUtils authUserUtils;
    private final CacheManager cacheManager;

    private static final String USER_INFO_CACHE = "userInfoCache";

    @Override
    @Logging(logArguments = true)
    @Cacheable(cacheNames = USER_INFO_CACHE, key = "#authUserId")
    public UserInfoResponse getUserInfo(String authUserId) {
        User user =
                userRepository
                        .findByAuthUserId(authUserId)
                        .orElseThrow(() -> new UserNotFound(authUserId));
        String avatarUrl =
                storageUtils.createSignedGetUrl(user.getAuthUserId(), StorageType.USER_AVATAR);
        return UserInfoResponse.builder()
                .username(user.getUsername())
                .serverCount(user.getServerCount())
                .bio(user.getBio())
                .avatarUrl(avatarUrl)
                .build();
    }

    @Override
    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = USER_INFO_CACHE, key = "#authUserId")
    public void updateUserInfo(String authUserId, UpdateUserInfoRequest updateUserInfoRequest) {
        if (allFieldIsNull(updateUserInfoRequest)) {
            return;
        }
        User user =
                userRepository
                        .findByAuthUserId(authUserId)
                        .orElseThrow(() -> new UserNotFound(authUserId));

        if (updateUserInfoRequest.resourceId() != null) {
            storageUtils.setResourceDeleted(
                    user.getAuthUserId(), StorageType.USER_AVATAR, user.getAuthUserId());
            storageUtils.setResourceActive(
                    updateUserInfoRequest.resourceId(),
                    user.getAuthUserId(),
                    StorageType.USER_AVATAR,
                    user.getAuthUserId());
        }
        if (updateUserInfoRequest.bio() != null) {
            user.setBio(updateUserInfoRequest.bio());
        }
        userRepository.save(user);
    }

    @Override
    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = USER_INFO_CACHE, key = "#authUserId")
    public void deleteUserAvatar(String authUserId) {
        storageUtils.setResourceDeleted(authUserId, StorageType.USER_AVATAR, authUserId);
    }

    @Override
    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = USER_INFO_CACHE, key = "#authUserId")
    public void changeUsername(String authUserId, ChangeUsernameRequest request) {
        if (userRepository.existsByUsername(request.newUsername())) {
            throw new DuplicateUsername("Username already exists: " + request.newUsername());
        }
        var user =
                userRepository
                        .findByAuthUserId(authUserId)
                        .orElseThrow(() -> new UserNotFound(authUserId));
        Objects.requireNonNull(cacheManager.getCache("userIdCache")).evict(user.getUsername());
        authUserUtils.changeUsername(authUserId, request.newUsername());
        userRepository.updateUsername(authUserId, request.newUsername());
    }

    private boolean allFieldIsNull(UpdateUserInfoRequest body) {
        return body.resourceId() == null && body.bio() == null;
    }
}
