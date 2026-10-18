package vn.io.huangnosimp.nautilusserver.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.constants.Status;
import vn.io.huangnosimp.nautilusserver.exceptions.*;
import vn.io.huangnosimp.nautilusserver.exceptions.auth.UserNotFound;
import vn.io.huangnosimp.nautilusserver.models.*;
import vn.io.huangnosimp.nautilusserver.repositories.*;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements  OwnerService {
    private final InvitationRepository invitationRepository;
    private final TransferRepository transferRepository;
    private final ServerRepository serverRepository;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final SchedulingService schedulingService;

    @Value("${app.invite-expiration-ms}")
    private long inviteExpirationMs;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInvite(String leaderName, String servername, String username) {
        log.debug("Creating invitation from {} to {} for server {}", leaderName, username, servername);
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
                .status(Status.PENDING)
                .build();
        invitationRepository.save(invitation);
        schedulingService.scheduleInvitationExpiration(invitation);
        log.info("Invitation from {} to {} for server {} created successfully", leaderName, username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelInvite(String leaderName, String servername, String username) {
        log.debug("Cancelling invitation from {} to {} for server {}", leaderName, username, servername);
        Invitation invitation = invitationRepository
                .findBySenderNameReceiverNameServerName(leaderName, username, servername)
                .orElseThrow(() -> new InvitationNotFound("invitation not found"));
        if (invitation.getStatus() != Status.PENDING) {
            throw new InvalidInvitationStatus("invitation is not pending");
        }
        invitation.setStatus(Status.CANCELED);
        invitationRepository.save(invitation);
        schedulingService.cancelInvitationExpiration(invitation.getId());
        log.info("Invitation from {} to {} for server {} cancelled successfully", leaderName, username, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "server-owner", allEntries = true)
    public void acceptInvite(String leaderName, String servername, String receiverName) {
        log.debug("Accepting invitation from {} to {} for server {}", leaderName, receiverName, servername);
        Invitation invitation = invitationRepository
                .findBySenderNameReceiverNameServerName(leaderName, receiverName, servername)
                .orElseThrow(() -> new InvitationNotFound("invitation not found"));
        if (invitation.getStatus() != Status.PENDING) {
            throw new InvalidInvitationStatus("invitation is not pending");
        }
        invitation.setStatus(Status.ACCEPTED);
        invitationRepository.save(invitation);
        User user = invitation.getReceiver();
        Server server = invitation.getServer();
        Owner owner = Owner.builder()
                .user(user)
                .server(server)
                .build();
        ownerRepository.save(owner);
        schedulingService.cancelInvitationExpiration(invitation.getId());
        log.info("Invitation from {} to {} for server {} accepted successfully", leaderName, receiverName, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void declineInvite(String leaderName, String servername, String username) {
        log.debug("Declining invitation from {} to {} for server {}", leaderName, username, servername);
        Invitation invitation = invitationRepository
                .findBySenderNameReceiverNameServerName(leaderName, username, servername)
                .orElseThrow(() -> new InvitationNotFound("invitation not found"));
        if (invitation.getStatus() != Status.PENDING) {
            throw new InvalidInvitationStatus("invitation is not pending");
        }
        invitation.setStatus(Status.DECLINED);
        invitationRepository.save(invitation);
        schedulingService.cancelInvitationExpiration(invitation.getId());
        log.info("Invitation from {} to {} for server {} declined successfully", leaderName, username, servername);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createTransfer(String leaderName, String ownerName, String servername) {
        log.debug("Creating transfer from {} to {} for server {}", leaderName, ownerName, servername);
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
                .status(Status.PENDING)
                .build();
        transferRepository.save(transfer);
        schedulingService.scheduleTransferExpiration(transfer);
        log.info("Transfer from {} to {} for server {} created successfully", leaderName, ownerName, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTransfer(String leaderName, String ownerName, String servername) {
        log.debug("Cancelling transfer from {} to {} for server {}", leaderName, ownerName, servername);
        Transfer transfer = transferRepository
                .findBySenderNameReceiverNameServerName(leaderName, ownerName, servername)
                .orElseThrow(() -> new TransferNotFound("transfer not found"));
        if (transfer.getStatus() != Status.PENDING) {
            throw new InvalidTransferStatus("transfer is not pending");
        }
        transfer.setStatus(Status.CANCELED);
        transferRepository.save(transfer);
        schedulingService.cancelTransferExpiration(transfer.getId());
        log.info("Transfer from {} to {} for server {} cancelled successfully", leaderName, ownerName, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "server-leader", allEntries = true)
    public void acceptTransfer(String leaderName, String ownerName, String servername) {
        log.debug("Accepting transfer from {} to {} for server {}", leaderName, ownerName, servername);
        Transfer transfer = transferRepository
                .findBySenderNameReceiverNameServerName(leaderName, ownerName, servername)
                .orElseThrow(() -> new TransferNotFound("transfer not found"));
        if (transfer.getStatus() != Status.PENDING) {
            throw new InvalidTransferStatus("transfer is not pending");
        }
        transfer.setStatus(Status.ACCEPTED);
        transferRepository.save(transfer);
        schedulingService.cancelTransferExpiration(transfer.getId());
        User receiver = transfer.getReceiver();
        Server server = transfer.getServer();
        server.setUser(receiver);
        serverRepository.save(server);
        log.info("Transfer from {} to {} for server {} accepted successfully", leaderName, ownerName, servername);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void declineTransfer(String leaderName, String ownerName, String servername) {
        log.debug("Declining transfer from {} to {} for server {}", leaderName, ownerName, servername);
        Transfer transfer = transferRepository
                .findBySenderNameReceiverNameServerName(leaderName, ownerName, servername)
                .orElseThrow(() -> new TransferNotFound("transfer not found"));
        if (transfer.getStatus() != Status.PENDING) {
            throw new InvalidTransferStatus("transfer is not pending");
        }
        transfer.setStatus(Status.DECLINED);
        transferRepository.save(transfer);
        schedulingService.cancelTransferExpiration(transfer.getId());
        log.info("Transfer from {} to {} for server {} declined successfully", leaderName, ownerName, servername);
    }
}
