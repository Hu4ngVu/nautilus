package vn.io.huangnosimp.nautilusserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.io.huangnosimp.nautilusserver.models.ServerInvitation;

import java.util.Optional;

public interface ServerInvitationRepository extends JpaRepository<ServerInvitation, String> {
    @Query("SELECT si FROM ServerInvitation si WHERE si.sender.username = :senderName AND si.receiver.username = :receiverName AND si.server.servername = :servername")
    Optional<ServerInvitation> findBySenderNameReceiverNameServerName(@Param("senderName") String senderName, @Param("receiverName") String receiverName, @Param("servername") String servername);

    @Query("SELECT EXISTS (SELECT 1 FROM ServerInvitation si WHERE si.sender.username = :senderName AND si.receiver.username = :receiverName AND si.server.servername = :servername AND si.status = PENDING)")
    boolean existsPendingInvitation(String senderName, String receiverName, String servername);
}
