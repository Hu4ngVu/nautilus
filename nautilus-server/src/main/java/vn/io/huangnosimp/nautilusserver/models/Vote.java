package vn.io.huangnosimp.nautilusserver.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "vote",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_feedback_id_user_id",
                        columnNames = {"feedback_id", "user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch =  FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Column(name = "vote_type", nullable = false)
    private Boolean voteType;   // true for upvote, false for downvote
}
