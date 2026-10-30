package vn.io.huangnosimp.nautilusserver.star.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
        name = "star",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uc_user_id_server_id",
                        columnNames = {"user_id", "server_id"}))
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
