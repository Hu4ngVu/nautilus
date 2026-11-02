package vn.io.huangnosimp.nautilusserver.storage.models;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageStatus;
import vn.io.huangnosimp.nautilusserver.storage.constants.StorageType;

@Entity
@Table(name = "resource")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_status", nullable = false)
    private StorageStatus storageStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type")
    private StorageType storageType;
}
