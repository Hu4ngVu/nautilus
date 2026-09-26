package vn.io.huangnosimp.nautilusserver.models.sql;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "version",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"project_id", "version"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Version extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "addon_url", nullable = false)
    private String addonUrl;
}
