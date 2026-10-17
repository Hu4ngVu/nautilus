package vn.io.huangnosimp.nautilusserver.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.io.huangnosimp.nautilusserver.models.Transfer;

import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, String> {
    @Query("SELECT t FROM Transfer t WHERE t.sender.username = :senderName AND t.receiver.username = :receiverName AND t.server.servername = :servername")
    Optional<Transfer> findBySenderNameReceiverNameServerName(@Param("senderName") String senderName, @Param("receiverName") String receiverName, @Param("servername") String servername);

    @Query("SELECT EXISTS (SELECT 1 FROM Transfer t WHERE t.sender.username = :senderName AND t.server.servername = :servername AND t.status = PENDING)")
    boolean existsPendingTransfer(@Param("senderName") String senderName, @Param("servername") String servername);

    @Query("SELECT EXISTS (SELECT 1 FROM Transfer t WHERE t.sender.username = :senderName AND t.receiver.username = :receiverName AND t.server.servername = :servername AND t.status = PENDING)")
    boolean existsPendingTransferWithReceiverName(String senderName, String receiverName, String servername);
}
