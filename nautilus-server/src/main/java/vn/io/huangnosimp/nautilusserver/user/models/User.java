package vn.io.huangnosimp.nautilusserver.user.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    @Column(name = "auth_user_id", nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private String authUserId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "server_count", nullable = false, columnDefinition = "INTEGER")
    private Integer serverCount;

    @Column(name = "avatar_key")
    private String avatarKey;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
}
