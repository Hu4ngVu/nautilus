package vn.io.huangnosimp.nautilusserver.storage.models;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.storage.StorageType;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageStatus;

@Entity
@Table(name = "resource", indexes = @Index(name = "idx_model_id", columnList = "model_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {
    @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_status", nullable = false)
    private StorageStatus storageStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type")
    private StorageType storageType;

    @Column(name = "model_id", columnDefinition = "CHAR(36)")
    private String modelId;
}
