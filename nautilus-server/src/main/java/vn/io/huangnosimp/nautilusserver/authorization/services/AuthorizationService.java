package vn.io.huangnosimp.nautilusserver.authorization.services;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.exceptions.CannotCreateRelation;
import vn.io.huangnosimp.nautilusserver.exceptions.CannotDeleteRelation;
import vn.io.huangnosimp.nautilusserver.logging.Logging;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final OpenFgaClient openFgaClient;

    @Logging(level = Level.DEBUG, logArguments = true)
    public void createOwnerRelation(String userId, String serverId) {
        postTuple(userId, serverId, "owner");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    public void deleteOwnerRelation(String userId, String serverId) {
        deleteTuple(userId, serverId, "owner");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    public void createPublicRelation(String serverId) {
        postTuple("*", serverId, "is_public");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    public void deletePublicRelation(String serverId) {
        deleteTuple("*", serverId, "is_public");
    }

    private void postTuple(String userId, String serverId, String relation) {
        // Check if the tuple already exists
        try {
            if (checkTupleAlreadyExists(userId, serverId, relation)) {
                return;
            }
        } catch (FgaInvalidParameterException e) {
            throw new CannotCreateRelation(
                    "Failed to check if tuple already exists for user: "
                            + userId
                            + ", server: "
                            + serverId
                            + ". Error: "
                            + e.getMessage());
        }
        ClientTupleKey tupleKey =
                new ClientTupleKey()
                        .user("user:" + userId)
                        .relation(relation)
                        ._object("server:" + serverId);
        try {
            var response = openFgaClient.writeTuples(List.of(tupleKey)).join();
            // OpenFga only returns 200 for successful write
            if (response.getStatusCode() != 200) {
                throw new CannotCreateRelation(
                        "Failed to create user-{}-server relation for user: "
                                + userId
                                + ", server: "
                                + serverId
                                + ". Status code: "
                                + response.getStatusCode());
            }
        } catch (FgaInvalidParameterException e) {
            throw new CannotCreateRelation(
                    "Failed to create user-{}-server relation for user: "
                            + userId
                            + ", server: "
                            + serverId
                            + ". Error: "
                            + e.getMessage());
        }
    }

    private void deleteTuple(String userId, String serverId, String relation) {
        try {
            if (!checkTupleAlreadyExists(userId, serverId, relation)) {
                return;
            }
        } catch (FgaInvalidParameterException e) {
            throw new CannotDeleteRelation(
                    "Failed to check if tuple exists for user: "
                            + userId
                            + ", server: "
                            + serverId
                            + ". Error: "
                            + e.getMessage());
        }
        ClientTupleKey tupleKey =
                new ClientTupleKey()
                        .user("user:" + userId)
                        .relation(relation)
                        ._object("server:" + serverId);
        try {
            var response = openFgaClient.deleteTuples(List.of(tupleKey)).join();
            // OpenFga only returns 200 for successful delete
            if (response.getStatusCode() != 200) {
                throw new CannotDeleteRelation(
                        "Failed to delete user-{}-server relation for user: "
                                + userId
                                + ", server: "
                                + serverId
                                + ". Status code: "
                                + response.getStatusCode());
            }
        } catch (FgaInvalidParameterException e) {
            throw new CannotDeleteRelation(
                    "Failed to delete user-{}-server relation for user: "
                            + userId
                            + ", server: "
                            + serverId
                            + ". Error: "
                            + e.getMessage());
        }
    }

    // use for checking tuple existence before creating or deleting, throw exception if any error
    // occurs
    private boolean checkTupleAlreadyExists(String userId, String serverId, String relation)
            throws FgaInvalidParameterException {
        ClientCheckRequest checkRequest =
                new ClientCheckRequest()
                        .user("user:" + userId)
                        .relation(relation)
                        ._object("server:" + serverId);
        var response = openFgaClient.check(checkRequest).join();
        return Objects.equals(response.getAllowed(), Boolean.TRUE);
    }
}
