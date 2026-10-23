package vn.io.huangnosimp.nautilusserver.server.models;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.server.constants.Loader;

import java.math.BigInteger;

@Entity
@Table(
        name = "modpack_version",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_server_id_version_loader",
                        columnNames = {"server_id", "version", "loader"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModpackVersion extends BaseEntity {
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "loader", nullable = false, length = 20)
    private Loader loader;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_size", nullable = false, columnDefinition = "BIGINT")
    private BigInteger fileSize;
}
