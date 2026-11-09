package vn.io.huangnosimp.nautilusserver.authorization.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import dev.openfga.sdk.api.client.ApiResponse;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientCheckResponse;
import dev.openfga.sdk.api.client.model.ClientWriteResponse;
import dev.openfga.sdk.api.model.CheckResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {
    @Mock OpenFgaClient openFgaClient;

    @Test
    void ownerRelationIsWrittenOnlyWhenItDoesNotAlreadyExist() throws Exception {
        when(openFgaClient.check(any(ClientCheckRequest.class)))
                .thenReturn(
                        CompletableFuture.completedFuture(checkResponse(true)),
                        CompletableFuture.completedFuture(checkResponse(false)));
        when(openFgaClient.writeTuples(anyList()))
                .thenReturn(CompletableFuture.completedFuture(writeResponse(200)));
        var service = new AuthorizationService(openFgaClient);

        service.createOwnerRelation("user-id", "server-id");
        service.createOwnerRelation("user-id", "server-id");

        verify(openFgaClient, times(2)).check(any(ClientCheckRequest.class));
        verify(openFgaClient, times(1)).writeTuples(anyList());
    }

    @Test
    void publicTupleUsesWildcardAndDeleteSkipsMissingRelations() throws Exception {
        when(openFgaClient.check(any(ClientCheckRequest.class)))
                .thenReturn(
                        CompletableFuture.completedFuture(checkResponse(false)),
                        CompletableFuture.completedFuture(checkResponse(false)));
        when(openFgaClient.writeTuples(anyList()))
                .thenReturn(CompletableFuture.completedFuture(writeResponse(200)));
        var service = new AuthorizationService(openFgaClient);

        service.createPublicRelation("server-id");
        service.deletePublicRelation("server-id");

        verify(openFgaClient)
                .writeTuples(
                        argThat(
                                tuples ->
                                        tuples.getFirst().getUser().equals("user:*")
                                                && tuples.getFirst()
                                                        .getRelation()
                                                        .equals("is_public")
                                                && tuples.getFirst()
                                                        .getObject()
                                                        .equals("server:server-id")));
        verify(openFgaClient, never()).deleteTuples(anyList());
    }

    @Test
    void checkResponsesFailClosedOnNonTrueAllowedValue() throws Exception {
        when(openFgaClient.check(any(ClientCheckRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(checkResponse(false)));
        var service =
                new PermissionCheckerImpl(
                        openFgaClient,
                        mock(vn.io.huangnosimp.nautilusserver.api.ServerQuery.class));

        assertFalse(service.isOwner("user-id", "server-id"));
    }

    private static ClientCheckResponse checkResponse(boolean allowed) {
        return new ClientCheckResponse(
                new ApiResponse<>(200, Map.of(), "", new CheckResponse().allowed(allowed)));
    }

    private static ClientWriteResponse writeResponse(int status) {
        return new ClientWriteResponse(new ApiResponse<>(status, Map.of(), "", new Object()));
    }
}
