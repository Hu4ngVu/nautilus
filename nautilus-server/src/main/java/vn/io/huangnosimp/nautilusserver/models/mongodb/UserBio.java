package vn.io.huangnosimp.nautilusserver.models.mongodb;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
public class UserBio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String bio;
}
