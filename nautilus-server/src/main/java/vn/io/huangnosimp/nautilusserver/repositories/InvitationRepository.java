package vn.io.huangnosimp.nautilusserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.models.Invitation;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, String> {
    @Query("SELECT i FROM Invitation i WHERE i.sender.username = :senderName AND i.receiver.username = :receiverName AND i.server.servername = :servername AND i.status = PENDING")
    Optional<Invitation> findBySenderNameReceiverNameServerName(@Param("senderName") String senderName, @Param("receiverName") String receiverName, @Param("servername") String servername);

    @Query("SELECT EXISTS (SELECT 1 FROM Invitation si WHERE si.sender.username = :senderName AND si.receiver.username = :receiverName AND si.server.servername = :servername AND si.status = PENDING)")
    boolean existsPendingInvitation(@Param("senderName") String senderName, @Param("receiverName") String receiverName, @Param("servername") String servername);
}
