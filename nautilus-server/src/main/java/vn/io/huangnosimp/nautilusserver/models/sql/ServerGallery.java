package vn.io.huangnosimp.nautilusserver.models.sql;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "server_gallery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerGallery extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false, updatable = false)
    private Server server;

    @Column(name = "image_url", nullable = false, unique = true)
    private String imageUrl;
}
