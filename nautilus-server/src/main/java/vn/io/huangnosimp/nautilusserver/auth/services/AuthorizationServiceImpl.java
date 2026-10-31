package vn.io.huangnosimp.nautilusserver.auth.services;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.auth.AuthorizationUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.CannotCreateRelation;
import vn.io.huangnosimp.nautilusserver.exceptions.CannotDeleteRelation;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.ServerUtils;

@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationUtils, AuthorizationService {
    private final OpenFgaClient openFgaClient;
    private final AuthUserUtils authUserUtils;
    private final ServerUtils serverUtils;

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    public void createOwnerRelation(String userId, String serverId) {
        postTuple(userId, serverId, "owner");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    public void deleteOwnerRelation(String userId, String serverId) {
        deleteTuple(userId, serverId, "owner");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    public boolean canEdit(String authenticationName, String username, String servername) {
        Result result = checkInput(authenticationName, username, servername);
        if (!result.isAllowed) {
            return false;
        }
        return checkTuple(result.userId, result.serverId, "editor");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    public boolean canView(String authenticationName, String username, String servername) {
        Result result = checkInput(authenticationName, username, servername);
        if (!result.isAllowed) {
            return false;
        }
        return checkTuple(result.userId, result.serverId, "viewer");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    public boolean canDelete(String authenticationName, String username, String servername) {
        Result result = checkInput(authenticationName, username, servername);
        if (!result.isAllowed) {
            return false;
        }
        return checkTuple(result.userId, result.serverId, "owner");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
    public void createPublicRelation(String serverId) {
        postTuple("*", serverId, "is_public");
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Override
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

    // use for checking in @PreAuthorize annotation, return false if any error occurs
    private boolean checkTuple(String userId, String serverId, String relation) {
        ClientCheckRequest checkRequest =
                new ClientCheckRequest()
                        .user("user:" + userId)
                        .relation(relation)
                        ._object("server:" + serverId);
        try {
            var response = openFgaClient.check(checkRequest).join();
            return Objects.equals(response.getAllowed(), Boolean.TRUE);
        } catch (Exception e) {
            return false;
        }
    }

    private record Result(boolean isAllowed, String userId, String serverId) {}

    private Result checkInput(String authenticationName, String username, String servername) {
        var optionalUserId = authUserUtils.getAuthUserIdByUsername(authenticationName);
        var optionalOwnerId = authUserUtils.getAuthUserIdByUsername(username);
        if (optionalUserId.isEmpty()) {
            return new Result(false, null, null);
        }
        if (optionalOwnerId.isEmpty()) {
            return new Result(false, null, null);
        }
        var optionalServerId = serverUtils.getServerId(optionalOwnerId.get(), servername);
        return optionalServerId
                .map(s -> new Result(true, optionalUserId.get(), s))
                .orElseGet(() -> new Result(false, null, null));
    }
}
