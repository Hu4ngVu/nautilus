package vn.io.huangnosimp.nautilusserver.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.api.ResourceStorage;
import vn.io.huangnosimp.nautilusserver.constraints.CacheName;
import vn.io.huangnosimp.nautilusserver.events.UserAvatarDeletedEvent;
import vn.io.huangnosimp.nautilusserver.events.UserAvatarUpdatedEvent;
import vn.io.huangnosimp.nautilusserver.events.UsernameUpdatedEvent;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateUsername;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidResourceId;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.ChangeUsernameRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.requests.UpdateUserInfoRequest;
import vn.io.huangnosimp.nautilusserver.user.dto.responses.UserInfoResponse;
import vn.io.huangnosimp.nautilusserver.user.models.User;
import vn.io.huangnosimp.nautilusserver.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ResourceStorage resourceStorage;
    private final ApplicationEventPublisher eventPublisher;

    @Logging(logArguments = true)
    @Cacheable(cacheNames = CacheName.USER_INFO_CACHE, key = "#authUserId")
    public UserInfoResponse getUserInfo(String authUserId) {
        User user =
                userRepository
                        .findByAuthUserId(authUserId)
                        .orElseThrow(() -> new UserNotFound(authUserId));
        String avatarUrl = null;
        if (user.getAvatarId() != null) {
            avatarUrl = resourceStorage.createSignedGetUrl(user.getAvatarId());
        }
        return UserInfoResponse.builder()
                .username(user.getUsername())
                .serverCount(user.getServerCount())
                .bio(user.getBio())
                .avatarUrl(avatarUrl)
                .build();
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheName.USER_INFO_CACHE, key = "#authUserId")
    public void updateUserInfo(String authUserId, UpdateUserInfoRequest updateUserInfoRequest) {
        User user =
                userRepository
                        .findByAuthUserId(authUserId)
                        .orElseThrow(() -> new UserNotFound(authUserId));

        if (updateUserInfoRequest.resourceId().isPresent()) {
            if (user.getAvatarId() != null) {
                eventPublisher.publishEvent(new UserAvatarDeletedEvent(user.getAvatarId()));
            }
            if (updateUserInfoRequest.resourceId().isNull()) {
                user.setAvatarId(null);
            } else {
                if (!resourceStorage.isResourceIdExist(updateUserInfoRequest.resourceId().get())) {
                    throw new InvalidResourceId(
                            "Resource ID does not exist: "
                                    + updateUserInfoRequest.resourceId().get());
                }
                eventPublisher.publishEvent(
                        new UserAvatarUpdatedEvent(updateUserInfoRequest.resourceId().get()));
                user.setAvatarId(updateUserInfoRequest.resourceId().get());
            }
        }
        updateUserInfoRequest.bio().ifPresent(user::setBio);
        userRepository.save(user);
    }

    @Logging(logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = CacheName.USER_INFO_CACHE, key = "#authUserId")
    public void changeUsername(String authUserId, ChangeUsernameRequest request) {
        var user =
                userRepository
                        .findByAuthUserId(authUserId)
                        .orElseThrow(() -> new UserNotFound(authUserId));
        if (userRepository.existsByUsername(request.newUsername())) {
            throw new DuplicateUsername("Username already exists: " + request.newUsername());
        }
        String oldUsername = user.getUsername();
        user.setUsername(request.newUsername());
        userRepository.save(user);
        eventPublisher.publishEvent(
                new UsernameUpdatedEvent(authUserId, oldUsername, request.newUsername()));
    }
}
