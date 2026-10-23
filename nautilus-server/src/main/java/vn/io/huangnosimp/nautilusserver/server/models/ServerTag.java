package vn.io.huangnosimp.nautilusserver.server.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import vn.io.huangnosimp.nautilusserver.server.constants.TagCategory;

@Entity
@Table(
        name = "server_tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_server_id_tag_category",
                        columnNames = {"server_id", "category", "tag"}
                )
        },
        indexes = {
                @Index(name = "idx_server_tag_tag", columnList = "tag"),
                @Index(name = "idx_server_tag_category", columnList = "category")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerTag extends BaseEntity {
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(name = "tag", nullable = false, length = 50)
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private TagCategory category;
}
