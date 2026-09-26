package vn.io.huangnosimp.nautilusserver.models.sql;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "project")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Project extends  BaseEntity {
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "visibility", nullable = false, columnDefinition = "BOOLEAN")
    private boolean visibility;

    @Column(name = "is_free", nullable = false, columnDefinition = "BOOLEAN")
    private boolean isFree;

    @Column(name = "image_url", unique = true)
    private String image_url;

    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;
}
