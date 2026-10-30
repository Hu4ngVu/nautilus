package vn.io.huangnosimp.nautilusserver.storage.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.models.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {
    @Query(
            "SELECT s.id FROM Resource s WHERE s.modelId = :modelId AND s.storageType ="
                    + " :storageType AND s.storageStatus = StorageStatus.ACTIVE")
    Optional<String> findId(
            @Param("modelId") String modelId, @Param("storageType") StorageType storageType);

    @Modifying
    @Transactional
    @Query(
            "UPDATE Resource r SET r.storageStatus = StorageStatus.ACTIVE, r.modelId = :modelId,"
                + " r.storageType = :storageType WHERE r.id = :resourceId AND r.userId = :userId"
                + " AND r.storageStatus = StorageStatus.UPLOADED")
    void setActiveById(
            @Param("resourceId") String resourceId,
            @Param("modelId") String modelId,
            @Param("storageType") StorageType storageType,
            @Param("userId") String userId);

    @Modifying
    @Transactional
    @Query(
            "UPDATE Resource r SET r.storageStatus = StorageStatus.DELETED WHERE r.modelId ="
                    + " :modelId AND r.storageType = :storageType AND r.userId = :userId AND"
                    + " r.storageStatus = StorageStatus.ACTIVE")
    void setDeletedById(
            @Param("modelId") String modelId,
            @Param("storageType") StorageType storageType,
            @Param("userId") String userId);

    @Modifying
    @Transactional
    @Query(
            "UPDATE Resource r SET r.storageStatus = StorageStatus.DELETED WHERE r.modelId IN"
                    + " :modelIds AND r.storageType = :storageType AND r.userId = :userId AND"
                    + " r.storageStatus = StorageStatus.ACTIVE")
    void setDeletedByIdBatch(
            @Param("modelIds") List<String> modelIds,
            @Param("storageType") StorageType storageType,
            @Param("userId") String userId);

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
