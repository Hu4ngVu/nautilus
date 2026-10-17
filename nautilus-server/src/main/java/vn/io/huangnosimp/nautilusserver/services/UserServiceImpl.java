package vn.io.huangnosimp.nautilusserver.services;

import io.awspring.cloud.s3.S3Template;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.io.huangnosimp.nautilusserver.dto.request.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.exceptions.CannotUpload;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.UserNotFound;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.models.UserDetailsWrapper;
import vn.io.huangnosimp.nautilusserver.repositories.UserRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
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
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw  new UsernameNotFoundException(username);
        }
        return new UserDetailsWrapper(user.get());
    }

    @Override
    public UserInfoResponse getUserInfo(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        User user = optionalUser.orElseThrow(() -> new UserNotFound(username));
        String avatarUrl = s3Template.createSignedGetURL(bucketName, user.getAvatarKey(), Duration.of(1, ChronoUnit.MINUTES)).toString();
        return new UserInfoResponse(
                user.getUsername(),
                user.getEmail(),
                user.getServerCount(),
                user.getBio(),
                avatarUrl
        );
    }

    @Override
    public void updateUserInfo(String username, UpdateUserInfoRequest updateUserInfoRequest) {
        log.info("Updating user info for {}", username);
        Optional<User> optionalUser = userRepository.findByUsername(username);
        User user = optionalUser.orElseThrow(() -> new UserNotFound(username));
        if (updateUserInfoRequest.bio() != null) {
            log.info("Updating bio for {}", username);
            user.setBio(updateUserInfoRequest.bio());
            log.info("Updated bio for {}", username);
        }
        MultipartFile avatarFile = updateUserInfoRequest.avatar();
        if (avatarFile != null) {
            log.info("Updating avatar for {}", username);
            String extension = FileNameUtil.getExtension(avatarFile.getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException("Invalid file type. Only PNG, JPG, JPEG, and WEBP are allowed.");
            }
            String key = "avatars/" + user.getUsername() + "-" + UUID.randomUUID() + "." + extension;
            try {
                String oldKey = user.getAvatarKey();
                log.info("uploading avatar for user {}", user.getUsername());
                s3Template.upload(bucketName, key, avatarFile.getInputStream());
                log.info("Uploaded avatar for user {}", user.getUsername());
                log.info("Setting new avatar key for user {}", user.getUsername());
                user.setAvatarKey(key);
                log.info("Set new avatar key for user {}", user.getUsername());
                if (oldKey != null) {
                    log.info("Deleting old avatar for user {}", user.getUsername());
                    s3Template.deleteObject(bucketName, oldKey);
                    log.info("deleted old avatar for user {}", user.getUsername());
                }
                log.info("updated new avatar for user {}", user.getUsername());
            } catch (IOException e) {
                log.error("Failed to upload avatar for user {}: {}", user.getUsername(), e.getMessage());
                throw new CannotUpload("Failed to upload avatar");
            }
        }
        userRepository.save(user);
        log.info("Updated user info for {}", username);
    }

    private boolean isExtensionInvalid(String extension) {
        return extension == null
                || !extension.equalsIgnoreCase("png")
                && !extension.equalsIgnoreCase("jpg")
                && !extension.equalsIgnoreCase("jpeg")
                && !extension.equalsIgnoreCase("webp");
    }

}
