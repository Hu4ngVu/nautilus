package vn.io.huangnosimp.nautilusserver.services;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
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
import java.util.Optional;
import java.util.UUID;

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
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UserNotFound(username);
        }
        return new UserInfoResponse(
                user.get().getUsername(),
                user.get().getEmail(),
                user.get().getServerCount(),
                user.get().getCollectionCount(),
                user.get().getBio(),
                user.get().getAvatarUrl()
        );
    }

    @Override
    public void updateUserInfo(String username, UpdateUserInfoRequest updateUserInfoRequest) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        User user = optionalUser.orElseThrow(() -> new UserNotFound(username));
        if (updateUserInfoRequest.bio() != null) {
            user.setBio(updateUserInfoRequest.bio());
        }
        MultipartFile avatarFile = updateUserInfoRequest.avatar();
        if (avatarFile != null) {
            String extension = FileNameUtil.getExtension(avatarFile.getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException("Invalid file type. Only PNG, JPG, JPEG, and WEBP are allowed.");
            }
            String key = "avatars/" + user.getUsername() + "-" + UUID.randomUUID() + extension;
            try {
                if (user.getAvatarUrl() != null) {
                    String s3Url = user.getAvatarUrl();
                    s3Template.deleteObject(s3Url);
                }
                S3Resource s3Resource = s3Template.upload(bucketName, key, avatarFile.getInputStream());
                user.setAvatarUrl(s3Resource.getURL().toString());
            } catch (IOException e) {
                throw new CannotUpload("Failed to upload avatar");
            }
        }
        userRepository.save(user);
    }

    private boolean isExtensionInvalid(String extension) {
        return extension == null || !extension.equalsIgnoreCase("png") && !extension.equalsIgnoreCase("jpg") && !extension.equalsIgnoreCase("jpeg") && !extension.equalsIgnoreCase("webp");
    }

}
