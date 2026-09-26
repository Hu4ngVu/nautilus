package vn.io.huangnosimp.nautilusserver.models.sql;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "server_mc_version",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"server_id", "mc_version"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerMcVersion extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false, updatable = false)
    private Server server;

    @Column(name = "mc_version", nullable = false, length = 7)
    private String mcVersion;
}
