package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "feedback",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_server_id_user_id",
                        columnNames = {"server_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback extends  BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "upvote_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer upvoteCount;

    @Column(name = "downvote_count", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer downvoteCount;
}
