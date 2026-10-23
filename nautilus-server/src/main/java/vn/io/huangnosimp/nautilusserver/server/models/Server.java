package vn.io.huangnosimp.nautilusserver.server.models;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.server.constants.ServerType;

@Entity
@Table(
        name = "server",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_server_name_per_user",
                columnNames = {"user_id", "servername"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Server extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "servername", nullable = false)
    private String servername;

    @Column(name = "visibility", nullable = false, columnDefinition = "BOOLEAN")
    private Boolean visibility; // true for public, false for private

    @Column(name = "requires_premium", nullable = false, columnDefinition = "BOOLEAN")
    private Boolean requiresPremium;

    @Column(name = "java_address")
    private String javaAddress;

    @Column(name = "bedrock_address")
    private String bedrockAddress;

    @Column(name = "avatar_key")
    private String avatarKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ServerType type;

    @Column(name = "banner_key")
    private String bannerKey;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
}
