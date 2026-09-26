package vn.io.huangnosimp.nautilusserver.models.sql;

import jakarta.persistence.*;
import lombok.*;
import vn.io.huangnosimp.nautilusserver.constant.Role;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends  BaseEntity {
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 10)
    private Role role;

    @Column(name = "avatar_url")
    private String avatarUrl;
}
