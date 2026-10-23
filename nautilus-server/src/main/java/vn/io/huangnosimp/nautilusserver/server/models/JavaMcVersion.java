package vn.io.huangnosimp.nautilusserver.server.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "java_mc_version",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_server_id_mc_version",
                        columnNames = {"server_id", "mc_version"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JavaMcVersion extends BaseEntity {
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(name = "mc_version", nullable = false, length = 7)
    private String mcVersion;

    @Column(name = "is_primary_mc_version", nullable = false, columnDefinition = "BOOLEAN")
    private Boolean isPrimaryMcVersion;
}
