package vn.io.huangnosimp.nautilusserver.server.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "image",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uc_server_id_image_key",
                    columnNames = {"server_id", "image_key"})
        },
        indexes = @Index(name = "idx_server_id", columnList = "server_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
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
