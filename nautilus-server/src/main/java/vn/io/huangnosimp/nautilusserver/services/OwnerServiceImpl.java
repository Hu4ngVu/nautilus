package vn.io.huangnosimp.nautilusserver.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.constants.InvitationStatus;
import vn.io.huangnosimp.nautilusserver.exceptions.DuplicateInvitation;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidInvitationStatus;
import vn.io.huangnosimp.nautilusserver.exceptions.InvitationNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.UserNotFound;
import vn.io.huangnosimp.nautilusserver.models.Server;
import vn.io.huangnosimp.nautilusserver.models.ServerInvitation;
import vn.io.huangnosimp.nautilusserver.models.User;
import vn.io.huangnosimp.nautilusserver.repositories.ServerInvitationRepository;
import vn.io.huangnosimp.nautilusserver.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.repositories.UserRepository;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements  OwnerService {
    private final ServerInvitationRepository serverInvitationRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;

    @Value("${app.invite-expiration-ms}")
    private long inviteExpirationMs;

    @Override
    public void sendInvite(String leaderName, String servername, String username) {
        Optional<User> optionalSender = userRepository.findByUsername(leaderName);
        User sender = optionalSender.orElseThrow(() -> new UserNotFound(leaderName));
        Optional<User> optionalReceiver = userRepository.findByUsername(username);
        User receiver = optionalReceiver.orElseThrow(() -> new UserNotFound(leaderName));
        Optional<Server> optionalServer = serverRepository.findByUsernameAndServername(leaderName, servername);
        Server server = optionalServer.orElseThrow(() -> new ServerNotFound(servername));
        if (serverInvitationRepository.existsPendingInvitation(leaderName, username, servername)) {
            throw new DuplicateInvitation("pending invitation already exists");
        }
        ServerInvitation invitation = ServerInvitation.builder()
                .sender(sender)
                .receiver(receiver)
                .server(server)
                .expiresAt(Instant.now().plusMillis(inviteExpirationMs))
                .status(InvitationStatus.PENDING)
                .build();
        serverInvitationRepository.save(invitation);
    }

    @Override
    public void cancelInvite(String leaderName, String servername, String username) {
        Optional<ServerInvitation> optionalServerInvitation = serverInvitationRepository.findBySenderNameReceiverNameServerName(leaderName, username, servername);
        ServerInvitation invitation = optionalServerInvitation.orElseThrow(() -> new InvitationNotFound("invitation not found"));
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidInvitationStatus("invitation is not pending");
        }
        invitation.setStatus(InvitationStatus.CANCELED);
        serverInvitationRepository.save(invitation);
    }

    @Override
    public void acceptInvite(String leaderName, String servername, String receiverName) {
        Optional<ServerInvitation> optionalServerInvitation = serverInvitationRepository.findBySenderNameReceiverNameServerName(leaderName, receiverName, servername);
        ServerInvitation invitation = optionalServerInvitation.orElseThrow(() -> new InvitationNotFound("invitation not found"));
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidInvitationStatus("invitation is not pending");
        }
        invitation.setStatus(InvitationStatus.ACCEPTED);
        serverInvitationRepository.save(invitation);
    }

    @Override
    public void declineInvite(String leaderName, String servername, String username) {
        Optional<ServerInvitation> optionalServerInvitation = serverInvitationRepository.findBySenderNameReceiverNameServerName(leaderName, username, servername);
        ServerInvitation invitation = optionalServerInvitation.orElseThrow(() -> new InvitationNotFound("invitation not found"));
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidInvitationStatus("invitation is not pending");
        }
        invitation.setStatus(InvitationStatus.DECLINED);
        serverInvitationRepository.save(invitation);
    }
}
