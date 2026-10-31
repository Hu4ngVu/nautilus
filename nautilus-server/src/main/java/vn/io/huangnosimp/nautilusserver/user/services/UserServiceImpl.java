package vn.io.huangnosimp.nautilusserver.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
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

    @Override
    @Logging(logArguments = true)
    public UserInfoResponse getUserInfo(String username) {
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
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
    public void updateUserInfo(String username, UpdateUserInfoRequest updateUserInfoRequest) {
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
    }

    @Override
    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserAvatar(String username) {
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
        storageUtils.setResourceDeleted(userId, StorageType.USER_AVATAR, userId);
    }

    @Override
    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void changeUsername(String username, ChangeUsernameRequest request) {
        userRepository.findByUsername(username).orElseThrow(() -> new UserNotFound(username));
        if (userRepository.existsByUsername(request.newUsername())) {
            throw new DuplicateUsername("Username already exists: " + request.newUsername());
        }
        userRepository.updateUsername(username, request.newUsername());
    }

    private boolean allFieldIsNull(UpdateUserInfoRequest body) {
        return body.resourceId() == null && body.bio() == null;
    }
}
