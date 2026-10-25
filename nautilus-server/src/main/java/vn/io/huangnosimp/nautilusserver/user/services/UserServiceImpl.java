package vn.io.huangnosimp.nautilusserver.user.services;

import java.util.UUID;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.io.huangnosimp.nautilusserver.storage.StorageService;
import vn.io.huangnosimp.nautilusserver.user.DeleteUserAvatar;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UserInfoResponse getUserInfo(String username) {
        log.debug("Fetching user info for {}", username);
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
        if (user.getAvatarKey() == null) {
            var response =
                    new UserInfoResponse(
                            user.getUsername(), user.getServerCount(), user.getBio(), null);
            log.info("User info for {} fetched successfully", username);
            return response;
        }
        String avatarUrl = storageService.createSignedGetUrl(user.getAvatarKey());
        var response =
                new UserInfoResponse(
                        user.getUsername(), user.getServerCount(), user.getBio(), avatarUrl);
        log.info("User info for {} fetched successfully", username);
        return response;
    }

    @Override
    public void updateUserInfo(String username, UpdateUserInfoRequest updateUserInfoRequest) {
        log.debug("Updating user info for {}", username);
        if (allFieldIsNull(updateUserInfoRequest)) {
            return;
        }
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));

        String oldKey = user.getAvatarKey();
        String newKey = null;
        boolean isNewAvatarUploaded = false;

        MultipartFile avatarFile = updateUserInfoRequest.avatar();

        if (avatarFile != null) {
            String extension = FileNameUtil.getExtension(avatarFile.getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException(
                        "Invalid file type. Only PNG, JPG, JPEG, and WEBP are allowed.");
            }

            newKey =
                    "user/avatar/" + user.getUsername() + "-" + UUID.randomUUID() + "." + extension;

            storageService.uploadImage(avatarFile, newKey);
            isNewAvatarUploaded = true;
        }

        try {
            if (updateUserInfoRequest.bio() != null) {
                user.setBio(updateUserInfoRequest.bio());
            }
            if (isNewAvatarUploaded) {
                user.setAvatarKey(newKey);
            }
            userRepository.save(user);
            log.info("Updated user info for {}", username);

        } catch (Exception e) {
            if (isNewAvatarUploaded) {
                eventPublisher.publishEvent(new DeleteUserAvatar(newKey));
            }
            throw e;
        }

        if (isNewAvatarUploaded && oldKey != null) {
            eventPublisher.publishEvent(new DeleteUserAvatar(oldKey));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserAvatar(String username) {
        log.debug("Deleting avatar for user {}", username);
        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() -> new UserNotFound(username));
        String oldKey = user.getAvatarKey();
        if (oldKey != null) {
            eventPublisher.publishEvent(new DeleteUserAvatar(oldKey));
        }
        user.setAvatarKey(null);
        userRepository.save(user);
        log.info("Deleted avatar for user {}", username);
    }

    private boolean isExtensionInvalid(String extension) {
        return extension == null
                || extension.isBlank()
                || !extension.equalsIgnoreCase("png")
                        && !extension.equalsIgnoreCase("jpg")
                        && !extension.equalsIgnoreCase("jpeg")
                        && !extension.equalsIgnoreCase("webp");
    }

    private boolean allFieldIsNull(UpdateUserInfoRequest body) {
        return body.avatar() == null && body.bio() == null;
    }
}
