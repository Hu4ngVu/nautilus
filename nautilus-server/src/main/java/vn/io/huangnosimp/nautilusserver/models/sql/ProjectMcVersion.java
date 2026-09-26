package vn.io.huangnosimp.nautilusserver.models.sql;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "project_mc_version",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"version_id", "mc_version"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMcVersion extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false, updatable = false)
    private Version version;

    @Column(name = "mc_version", nullable = false, length = 7)
    private String mcVersion;
}
