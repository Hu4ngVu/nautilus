package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.constants.ServerTagValue;
import vn.io.huangnosimp.nautilusserver.constants.TagCategory;

@Entity
@Table(
        name = "server_tag",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_server_id_tag_category",
                        columnNames = {"server_id", "tag", "category"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerTag extends BaseEntity {
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag", nullable = false, length = 50)
    private ServerTagValue tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private TagCategory category;
}
