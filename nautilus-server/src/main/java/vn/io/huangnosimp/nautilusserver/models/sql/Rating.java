package vn.io.huangnosimp.nautilusserver.models.sql;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.constant.Star;

@Entity
@Table(
        name = "rating",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "project_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(name = "star", nullable = false, length = 5)
    private Star star;
}
