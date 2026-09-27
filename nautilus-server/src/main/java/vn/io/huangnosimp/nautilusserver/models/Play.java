package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "play",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_user_id_server_id",
                        columnNames = {"user_id", "server_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Play extends BaseEntity {
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;
}
