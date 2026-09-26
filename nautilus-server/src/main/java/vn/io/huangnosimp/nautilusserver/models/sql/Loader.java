package vn.io.huangnosimp.nautilusserver.models.sql;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.constant.Loaders;


@Entity
@Table(
        name = "loader",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"version_id", "loader"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loader extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false, updatable = false)
    private Version version;

    @Enumerated(EnumType.STRING)
    @Column(name = "loader", nullable = false)
    private Loaders loader;
}
