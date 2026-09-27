package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "gallery",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_server_id_image_url",
                        columnNames = {"server_id", "image_url"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gallery extends BaseEntity {
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;
}
