package vn.io.huangnosimp.nautilusserver.user.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateUsername;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.StorageUtils;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.ChangeUsernameRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final StorageUtils storageUtils;
    private final AuthUserUtils authUserUtils;

    @Override
    public UserInfoResponse getUserInfo(String username) {
        log.debug("Fetching user info for {}", username);
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
        String avatarUrl =
                storageUtils.createSignedGetUrl(user.getAuthUserId(), StorageType.USER_AVATAR);
        var response =
                UserInfoResponse.builder()
                        .username(user.getUsername())
                        .serverCount(user.getServerCount())
                        .bio(user.getBio())
                        .avatarUrl(avatarUrl)
                        .build();
        log.info("User info for {} fetched successfully", username);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(String username, UpdateUserInfoRequest updateUserInfoRequest) {
        log.debug("Updating user info for {}", username);
        if (allFieldIsNull(updateUserInfoRequest)) {
            return;
        }
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));

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
        log.info("User info for {} updated successfully", username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserAvatar(String username) {
        log.debug("Deleting avatar for user {}", username);
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
        storageUtils.setResourceDeleted(userId, StorageType.USER_AVATAR, userId);
        log.info("Deleted avatar for user {}", username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeUsername(String username, ChangeUsernameRequest request) {
        log.debug("Changing username from {} to {}", username, request.newUsername());
        userRepository.findByUsername(username).orElseThrow(() -> new UserNotFound(username));
        if (userRepository.existsByUsername(request.newUsername())) {
            throw new DuplicateUsername("Username already exists: " + request.newUsername());
        }
        userRepository.updateUsername(username, request.newUsername());
        log.info("Username changed from {} to {}", username, request.newUsername());
    }

    private boolean allFieldIsNull(UpdateUserInfoRequest body) {
        return body.resourceId() == null && body.bio() == null;
    }
}
