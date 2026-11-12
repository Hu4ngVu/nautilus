package vn.io.huangnosimp.nautilusserver.user.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "auth_user_id", nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private String authUserId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Min(0)
    @Column(name = "server_count", nullable = false, columnDefinition = "INTEGER")
    private Integer serverCount;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_id", columnDefinition = "CHAR(36)")
    private String avatarId;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User other = (User) o;

        return authUserId != null && authUserId.equals(other.authUserId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public String getId() {
        return this.authUserId;
    }

    public void setId(String id) {
        this.authUserId = id;
    }
}
