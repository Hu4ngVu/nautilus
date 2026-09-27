package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "favorite",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_server_id_user_id",
                        columnNames = {"server_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite extends BaseEntity {
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;
}
