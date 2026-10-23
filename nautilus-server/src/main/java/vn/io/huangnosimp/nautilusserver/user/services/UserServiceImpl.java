package vn.io.huangnosimp.nautilusserver.user.services;

import io.awspring.cloud.s3.S3Template;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.exceptions.CannotUploadAvatar;
import vn.io.huangnosimp.nautilusserver.user.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final S3Template s3Template;
    private final UserRepository userRepository;

    @Value("${app.s3.bucket-name}")
    private String bucketName;

    @Override
    public UserInfoResponse getUserInfo(String username) {
        log.debug("Fetching user info for {}", username);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFound(username));
        if (user.getAvatarKey() == null) {
            var response = new UserInfoResponse(
                    user.getUsername(),
                    user.getServerCount(),
                    user.getBio(),
                    null
            );
            log.info("User info for {} fetched successfully", username);
            return response;
        }
        String avatarUrl = s3Template.createSignedGetURL(bucketName, user.getAvatarKey(), Duration.ofMinutes(1)).toString();
        var response = new UserInfoResponse(
                user.getUsername(),
                user.getServerCount(),
                user.getBio(),
                avatarUrl
        );
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
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFound(username));
        if (updateUserInfoRequest.bio() != null) {
            user.setBio(updateUserInfoRequest.bio());
        }
        MultipartFile avatarFile = updateUserInfoRequest.avatar();
        if (avatarFile != null) {
            String extension = FileNameUtil.getExtension(avatarFile.getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException("Invalid file type. Only PNG, JPG, JPEG, and WEBP are allowed.");
            }
            String key = "user/avatar/" + user.getUsername() + "-" + UUID.randomUUID() + "." + extension;
            try {
                String oldKey = user.getAvatarKey();
                s3Template.upload(bucketName, key, avatarFile.getInputStream());
                user.setAvatarKey(key);
                if (oldKey != null) {
                    s3Template.deleteObject(bucketName, oldKey);
                }
            } catch (IOException e) {
                throw new CannotUploadAvatar("Failed to upload avatar");
            }
        }
        userRepository.save(user);
        log.info("Updated user info for {}", username);
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void deleteUserAvatar(String username) {
        log.debug("Deleting avatar for user {}", username);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFound(username));
        String oldKey = user.getAvatarKey();
        if (oldKey != null) {
            s3Template.deleteObject(bucketName, oldKey);
            user.setAvatarKey(null);
            userRepository.save(user);
        }
        log.info("Deleted avatar for user {}", username);
    }

    private boolean isExtensionInvalid(String extension) {
        return extension == null
                || !extension.equalsIgnoreCase("png")
                && !extension.equalsIgnoreCase("jpg")
                && !extension.equalsIgnoreCase("jpeg")
                && !extension.equalsIgnoreCase("webp");
    }

    private boolean allFieldIsNull(UpdateUserInfoRequest body) {
        return body.avatar() == null && body.bio() == null;
    }
}
