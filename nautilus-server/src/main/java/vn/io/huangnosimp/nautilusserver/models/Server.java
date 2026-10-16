package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.constants.ServerType;

@Entity
@Table(
        name = "server",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_server_name_per_user",
                columnNames = {"user_id", "name"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Server extends  BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "visibility", nullable = false)
    private Boolean visibility; // true for public, false for private

    @Column(name = "feedback_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer feedbackCount;

    @Column(name = "favorite_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer favoriteCount;

    @Column(name = "player_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer playerCount;

    @Column(name = "link_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer linkCount;

    @Column(name = "requires_premium", columnDefinition = "BOOLEAN DEFAULT 1")
    private Boolean requiresPremium;

    @Column(name = "java_address")
    private String javaAddress;

    @Column(name = "bedrock_address")
    private String bedrockAddress;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ServerType type;

    @Column(name = "banner_url")
    private String bannerUrl;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
}
