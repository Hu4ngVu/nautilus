package vn.io.huangnosimp.nautilusserver.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.constants.InvitationStatus;
import vn.io.huangnosimp.nautilusserver.exceptions.*;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.UserNotFound;
import vn.io.huangnosimp.nautilusserver.models.*;
import vn.io.huangnosimp.nautilusserver.repositories.*;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements  OwnerService {
    private final InvitationRepository invitationRepository;
    private final TransferRepository transferRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;

    @Value("${app.invite-expiration-ms}")
    private long inviteExpirationMs;

    @Override
    public void createInvite(String leaderName, String servername, String username) {
        User sender = userRepository.findByUsername(leaderName).orElseThrow(() -> new UserNotFound(leaderName));
        User receiver = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFound(username));
        Server server = serverRepository.findByUsernameAndServername(leaderName, servername).orElseThrow(() -> new ServerNotFound(servername));
        if (invitationRepository.existsPendingInvitation(leaderName, username, servername)) {
            throw new DuplicateInvitation("pending invitation already exists");
        }
        Invitation invitation = Invitation.builder()
                .sender(sender)
                .receiver(receiver)
                .server(server)
                .expiresAt(Instant.now().plusMillis(inviteExpirationMs))
                .status(InvitationStatus.PENDING)
                .build();
        invitationRepository.save(invitation);
    }

    @Override
    public void cancelInvite(String leaderName, String servername, String username) {
        Invitation invitation = invitationRepository
                .findBySenderNameReceiverNameServerName(leaderName, username, servername)
                .orElseThrow(() -> new InvitationNotFound("invitation not found"));
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidInvitationStatus("invitation is not pending");
        }
        invitation.setStatus(InvitationStatus.CANCELED);
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional
    @CacheEvict(value = "server-owner", key = "#leaderName + ':' + #servername + ':' + #receiverName")
    public void acceptInvite(String leaderName, String servername, String receiverName) {
        Invitation invitation = invitationRepository
                .findBySenderNameReceiverNameServerName(leaderName, receiverName, servername)
                .orElseThrow(() -> new InvitationNotFound("invitation not found"));
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidInvitationStatus("invitation is not pending");
        }
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        User user = invitation.getReceiver();
        Server server = invitation.getServer();
        Owner owner = Owner.builder()
                .user(user)
                .server(server)
                .build();
        ownerRepository.save(owner);
    }

    @Override
    public void declineInvite(String leaderName, String servername, String username) {
        Invitation invitation = invitationRepository
                .findBySenderNameReceiverNameServerName(leaderName, username, servername)
                .orElseThrow(() -> new InvitationNotFound("invitation not found"));
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidInvitationStatus("invitation is not pending");
        }
        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }


    @Override
    public void createTransfer(String leaderName, String ownerName, String servername) {
        User sender = userRepository.findByUsername(leaderName).orElseThrow(() -> new UserNotFound(leaderName));
        User receiver = userRepository.findByUsername(ownerName).orElseThrow(() -> new UserNotFound(ownerName));
        Server server = serverRepository.findByUsernameAndServername(leaderName, servername).orElseThrow(() -> new ServerNotFound(servername));
        if (transferRepository.existsPendingTransfer(leaderName, servername)) {
            throw new DuplicateTransfer("pending transfer already exists");
        }
        Transfer transfer = Transfer.builder()
                .sender(sender)
                .receiver(receiver)
                .server(server)
                .expiresAt(Instant.now().plusMillis(inviteExpirationMs))
                .status(InvitationStatus.PENDING)
                .build();
        transferRepository.save(transfer);
    }

    @Override
    public void cancelTransfer(String leaderName, String ownerName, String servername) {
        Transfer transfer = transferRepository
                .findBySenderNameReceiverNameServerName(leaderName, ownerName, servername)
                .orElseThrow(() -> new TransferNotFound("transfer not found"));
        if (transfer.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidTransferStatus("transfer is not pending");
        }
        transfer.setStatus(InvitationStatus.CANCELED);
        transferRepository.save(transfer);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "server_leader")
    public void acceptTransfer(String leaderName, String ownerName, String servername) {
        Transfer transfer = transferRepository
                .findBySenderNameReceiverNameServerName(leaderName, ownerName, servername)
                .orElseThrow(() -> new TransferNotFound("transfer not found"));
        if (transfer.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidTransferStatus("transfer is not pending");
        }
        transfer.setStatus(InvitationStatus.ACCEPTED);
        transferRepository.save(transfer);
        User receiver = transfer.getReceiver();
        Server server = transfer.getServer();
        server.setUser(receiver);
        serverRepository.save(server);
    }

    @Override
    public void declineTransfer(String leaderName, String ownerName, String servername) {
        Transfer transfer = transferRepository
                .findBySenderNameReceiverNameServerName(leaderName, ownerName, servername)
                .orElseThrow(() -> new TransferNotFound("transfer not found"));
        if (transfer.getStatus() != InvitationStatus.PENDING) {
            throw new InvalidTransferStatus("transfer is not pending");
        }
        transfer.setStatus(InvitationStatus.DECLINED);
        transferRepository.save(transfer);
    }
}
