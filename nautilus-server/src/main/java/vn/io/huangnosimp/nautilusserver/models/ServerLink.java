package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "server_link")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerLink extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "link", nullable = false)
    private String link;
}
