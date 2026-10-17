package vn.io.huangnosimp.nautilusserver.services;

import io.awspring.cloud.s3.S3Template;
import jodd.io.FileNameUtil;
import lombok.RequiredArgsConstructor;
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
import vn.io.huangnosimp.nautilusserver.repositories.OwnerRepository;
import vn.io.huangnosimp.nautilusserver.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.repositories.UserRepository;

import java.io.IOException;
import java.util.Optional;

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
        Optional<Server> optionalServer = serverRepository.findByUsernameAndServername(username, servername);
        Server server = optionalServer.orElseThrow(() -> new ServerNotFound(servername));
        return ServerInfoResponse.builder()
                .servername(server.getServername())
                .visibility(server.getVisibility())
                .linkCount(server.getLinkCount())
                .requiresPremium(server.getRequiresPremium())
                .javaAddress(server.getJavaAddress())
                .bedrockAddress(server.getBedrockAddress())
                .build();
    }

    @Override
    @Transactional
    public void createServer(String username, CreateServerRequest body) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        User user = optionalUser.orElseThrow(() -> new UserNotFound(username));
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
    }

    @Override
    public void deleteServer(String username, String servername) {
        serverRepository.deleteByServernameAndUsername(username, servername);
    }

    @Override
    public void updateServerInfo(String username, String servername, UpdateServerInfoRequest body) {
        Optional<Server> optionalServer = serverRepository.findByUsernameAndServername(username, servername);
        Server server = optionalServer.orElseThrow(() -> new ServerNotFound(servername));
        if (body.banner() != null) {
            String extension = FileNameUtil.getExtension(body.banner().getOriginalFilename());
            if (isExtensionInvalid(extension)) {
                throw new IllegalArgumentException("Invalid file type. Only PNG, JPG, JPEG, WEBP, and GIF are allowed.");
            }
            String bannerKey = "banners/" + server.getServername() + "-" + server.getId() + "." + extension;
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
            String imageKey = "images/" + server.getServername() + "-" + server.getId() + "." + extension;
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
            server.setSummary(body.bio());
        }
        serverRepository.save(server);
    }

    private boolean isExtensionInvalid(String extension) {
        return extension == null
                || !extension.equalsIgnoreCase("png")
                && !extension.equalsIgnoreCase("jpg")
                && !extension.equalsIgnoreCase("jpeg")
                && !extension.equalsIgnoreCase("webp")
                && !extension.equalsIgnoreCase("gif");
    }
}
