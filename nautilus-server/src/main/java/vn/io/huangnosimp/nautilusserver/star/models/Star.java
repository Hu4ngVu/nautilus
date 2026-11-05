package vn.io.huangnosimp.nautilusserver.star.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "star",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uc_user_id_server_id",
                        columnNames = {"user_id", "server_id"}),
        indexes = {
            @Index(name = "idx_user_id_star", columnList = "user_id"),
            @Index(name = "idx_server_id_star", columnList = "server_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Star extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "server_id", nullable = false)
    private String serverId;
}
