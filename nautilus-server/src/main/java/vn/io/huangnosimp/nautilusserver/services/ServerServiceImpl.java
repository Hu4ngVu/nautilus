package vn.io.huangnosimp.nautilusserver.services;

import io.awspring.cloud.s3.S3Template;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.dto.request.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.dto.request.UpdateServerInfoRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.exceptions.CannotUpload;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.UserNotFound;
import vn.io.huangnosimp.nautilusserver.models.Owner;
import vn.io.huangnosimp.nautilusserver.models.Server;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.repositories.*;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final S3Template s3Template;

    @Value("${app.s3.bucket-name}")
    private String bucketName;
    
    @Override
    public ServerInfoResponse getServerInfo(String username, String servername) throws ServerNotFound {
        log.debug("Fetching server info for user: {}, server: {}", username, servername);
        Server server = serverRepository.findByUsernameAndServername(username, servername).orElseThrow(() -> new ServerNotFound(servername));
        var response = ServerInfoResponse.builder()
                .servername(server.getServername())
                .visibility(server.getVisibility())
                .requiresPremium(server.getRequiresPremium())
                .javaAddress(server.getJavaAddress())
                .bedrockAddress(server.getBedrockAddress())
                .imageUrl(server.getImageKey() != null ? s3Template.createSignedGetURL(bucketName, server.getImageKey(), Duration.ofMinutes(1)).toString() : null)
                .bannerUrl(server.getBannerKey() != null ? s3Template.createSignedGetURL(bucketName, server.getBannerKey(), Duration.ofMinutes(1)).toString() : null)
                .build();
        log.info("Server info for user: {}, server: {} fetched successfully", username, servername);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createServer(String username, CreateServerRequest body) {
        log.debug("Creating server for user: {}, servername: {}", username, body.servername());
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFound(username));
        Server server = Server.builder()
                .user(user)
                .servername(body.servername())
                .requiresPremium(body.requiresPremium())
                .visibility(body.visibility())
                .type(body.type())
                .summary(body.summary())
                .build();
        serverRepository.save(server);
        Owner owner = Owner.builder()
                .user(user)
                .server(server)
                .build();
        ownerRepository.save(owner);
        user.setServerCount(user.getServerCount() + 1);
        userRepository.save(user);
        log.info("Server for user: {}, servername: {} created successfully", username, body.servername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServer(String username, String servername) {
        log.debug("Deleting server for user: {}, server: {}", username, servername);
        serverRepository.deleteByServernameAndUsername(username, servername);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ServerNotFound(servername));
        user.setServerCount(user.getServerCount() - 1);
        userRepository.save(user);
        log.info("Server for user: {}, servername: {} deleted successfully", username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateServerInfo(String username, String servername, UpdateServerInfoRequest body) {
        log.debug("Updating server info for user: {}, server: {}", username, servername);
        if (allFieldIsNull(body)) {
            return;
        }
        Server server = serverRepository.findByUsernameAndServername(username, servername).orElseThrow(() -> new ServerNotFound(servername));
        if (body.banner() != null) {
            String extension = FileNameUtil.getExtension(body.banner().getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException("Invalid file type. Only PNG, JPG, JPEG, WEBP, and GIF are allowed.");
            }
            String bannerKey = "banners/" + server.getUser().getUsername() + "/" + server.getServername() + "-" + UUID.randomUUID() + "." + extension;
            try {
                String oldKey = server.getBannerKey();
                s3Template.upload(bucketName, bannerKey, body.banner().getInputStream());
                server.setBannerKey(bannerKey);
                if (oldKey != null) {
                    s3Template.deleteObject(bucketName, oldKey);
                }
            } catch (IOException e) {
                throw new CannotUpload("Fail to upload banner for server");
            }
        }
        if (body.image() != null) {
            String extension = FileNameUtil.getExtension(body.image().getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException("Invalid file type. Only PNG, JPG, JPEG, WEBP, and GIF are allowed.");
            }
            String imageKey = "images/" + server.getUser().getUsername() + "/" + server.getServername() + "-" + UUID.randomUUID() + "." + extension;
            try {
                String oldKey = server.getImageKey();
                s3Template.upload(bucketName, imageKey, body.image().getInputStream());
                server.setImageKey(imageKey);
                if (oldKey != null) {
                    s3Template.deleteObject(bucketName, oldKey);
                }
            } catch (IOException e) {
                throw new CannotUpload("Fail to upload image for server");
            }
        }
        if (body.javaAddress() != null) {
            server.setJavaAddress(body.javaAddress());
        }
        if (body.bedrockAddress() != null) {
            server.setBedrockAddress(body.bedrockAddress());
        }
        if (body.bio() != null) {
            server.setBio(body.bio());
        }
        serverRepository.save(server);
        log.info("Server info for user: {}, server: {} updated successfully", username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServerImage(String leaderName, String servername) {
        log.debug("Deleting server image for user: {}, server: {}", leaderName, servername);
        Server server = serverRepository.findByUsernameAndServername(leaderName, servername)
                .orElseThrow(() -> new ServerNotFound(servername));
        String oldKey = server.getImageKey();
        if (oldKey != null) {
            s3Template.deleteObject(bucketName, oldKey);
            server.setImageKey(null);
            serverRepository.save(server);
        }
        log.info("Server image for user: {}, server: {} deleted successfully", leaderName, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServerBanner(String leaderName, String servername) {
        log.debug("Deleting server banner for user: {}, server: {}", leaderName, servername);
        Server server = serverRepository.findByUsernameAndServername(leaderName, servername)
                .orElseThrow(() -> new ServerNotFound(servername));
        String oldKey = server.getBannerKey();
        if (oldKey != null) {
            s3Template.deleteObject(bucketName, oldKey);
            server.setBannerKey(null);
            serverRepository.save(server);
        }
        log.info("Server banner for user: {}, server: {} deleted successfully", leaderName, servername);
    }

    private boolean isExtensionInvalid(String extension) {
        return extension == null
                || !extension.equalsIgnoreCase("png")
                && !extension.equalsIgnoreCase("jpg")
                && !extension.equalsIgnoreCase("jpeg")
                && !extension.equalsIgnoreCase("webp")
                && !extension.equalsIgnoreCase("gif");
    }

    private boolean allFieldIsNull(UpdateServerInfoRequest body) {
        return body.banner() == null
                && body.image() == null
                && body.javaAddress() == null
                && body.bedrockAddress() == null
                && body.bio() == null;
    }
}
