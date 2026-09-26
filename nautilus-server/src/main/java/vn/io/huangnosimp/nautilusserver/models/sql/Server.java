package vn.io.huangnosimp.nautilusserver.models.sql;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.constant.Compatibility;

@Entity
@Table(name = "server")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Server extends BaseEntity {
    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "visibility", nullable = false)
    private boolean visibility;

    @Column(name = "java_address")
    private String javaAddress;

    @Column(name = "java_port")
    private short port;

    @Column(name = "bedrock_address")
    private String bedrockAddress;

    @Column(name = "bedrock_port")
    private short bedrockPort;

    @Column(name = "image_url", unique = true)
    private String imageUrl;

    @Column(name = "modpack_url", unique = true)
    private String modpackUrl;

    @Column(name = "recommend_mc_version")
    private String recommendMcVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "compatibility", length = 20)
    private Compatibility compatibility;
}
