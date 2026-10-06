package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "collection",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_user_id_name",
                        columnNames = {"user_id", "name"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "visibility", nullable = false, columnDefinition = "BOOLEAN")
    private Boolean visibility; // true for public, false for private

    @Column(name = "server_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer serverCount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
