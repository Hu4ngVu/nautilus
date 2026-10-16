package vn.io.huangnosimp.nautilusserver.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.dto.request.CreateServerRequest;
import vn.io.huangnosimp.nautilusserver.dto.response.ServerInfoResponse;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.models.Server;
import vn.io.huangnosimp.nautilusserver.repositories.ServerRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {
    private final ServerRepository serverRepository;
    
    @Override
    public ServerInfoResponse getServerInfo(String username, String servername) throws ServerNotFound {
        Optional<Server> optionalServer = serverRepository.findByNameAndUserUsername(servername, username);
        Server server = optionalServer.orElseThrow(() -> new ServerNotFound(servername));
        return ServerInfoResponse.builder()
                .servername(server.getName())
                .visibility(server.getVisibility())
                .feedbackCount(server.getFeedbackCount())
                .favoriteCount(server.getFavoriteCount())
                .playerCount(server.getPlayerCount())
                .linkCount(server.getLinkCount())
                .requiresPremium(server.getRequiresPremium())
                .javaAddress(server.getJavaAddress())
                .bedrockAddress(server.getBedrockAddress())
                .build();
    }

    @Override
    public void createServer(String username, CreateServerRequest body) {
        Server server = Server.builder()
                .name(body.servername())
                .visibility(body.visibility())
                .type(body.type())
                .summary(body.summary())
                .build();
        serverRepository.save(server);

    }

    @Override
    public void deleteServer(String username, String servername) {
        serverRepository.deleteByNameAndUserUsername(servername, username);
    }
}
