package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "gallery",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_server_id_image_key",
                        columnNames = {"server_id", "image_key"}
                )
        },
        indexes = @Index(
                name = "idx_server_id",
                columnList = "server_id"
        )
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

    @Column(name = "image_key", nullable = false)
    private String imageKey;

    @Column(name = "title")
    private String title;

    @Column(name = "summary")
    private String summary;

    @Column(name = "order_index", columnDefinition = "INTEGER DEFAULT 0")
    private Integer orderIndex;
}
