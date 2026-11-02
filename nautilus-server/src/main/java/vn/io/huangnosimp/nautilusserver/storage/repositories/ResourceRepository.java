package vn.io.huangnosimp.nautilusserver.storage.repositories;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.models.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {

    @Modifying
    @Transactional
    @Query(
            "UPDATE Resource r SET r.storageStatus = StorageStatus.ACTIVE, r.storageType ="
                    + " :storageType WHERE r.id = :id AND r.storageStatus = StorageStatus.UPLOADED")
    void setActiveById(@Param("id") String id, @Param("storageType") StorageType storageType);

    @Modifying
    @Transactional
    @Query(
            "UPDATE Resource r SET r.storageStatus = StorageStatus.DELETED WHERE r.id = :id AND"
                    + " r.storageStatus = StorageStatus.ACTIVE")
    void setDeletedById(@Param("id") String id);

    @Modifying
    @Transactional
    @Query(
            "UPDATE Resource r SET r.storageStatus = StorageStatus.DELETED WHERE r.id IN :ids AND"
                    + " r.storageStatus = StorageStatus.ACTIVE")
    void setDeletedByIdBatch(@Param("ids") List<String> ids);

    @Modifying
    @Transactional
    @Query(
            "UPDATE Resource r SET r.storageStatus = StorageStatus.UPLOADED WHERE r.id ="
                    + " :resourceId AND r.storageStatus = StorageStatus.PENDING")
    void setUploadedById(@Param("resourceId") String resourceId);

    @Query(
            "SELECT r.id FROM Resource r WHERE r.storageStatus = StorageStatus.DELETED OR"
                    + " (r.storageStatus IN (StorageStatus.PENDING, StorageStatus.UPLOADED) AND"
                    + " r.createdAt < :targetTime)")
    List<String> findAllTrashResourceId(@Param("targetTime") Instant targetTime);

    @Modifying
    @Transactional
    @Query("DELETE FROM Resource r WHERE r.id IN :ids")
    void deleteAllById(@Param("ids") List<String> ids);
}
