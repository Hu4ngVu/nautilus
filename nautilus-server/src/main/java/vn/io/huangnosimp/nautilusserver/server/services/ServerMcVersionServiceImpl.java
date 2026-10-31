package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.auth.AuthUserUtils;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidMcVersion;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.exceptions.UserNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.constants.McVersion;
import vn.io.huangnosimp.nautilusserver.server.dto.data.VersionData;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddBedrockVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddJavaVersionRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.BedrockVersionResponse;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.JavaVersionResponse;
import vn.io.huangnosimp.nautilusserver.server.models.BedrockMcVersion;
import vn.io.huangnosimp.nautilusserver.server.models.JavaMcVersion;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.repositories.BedrockMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.JavaMcVersionRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;

@Service
@RequiredArgsConstructor
public class ServerMcVersionServiceImpl implements ServerMcVersionService {
    private final AuthUserUtils authUserUtils;
    private final ServerRepository serverRepository;
    private final JavaMcVersionRepository javaMcVersionRepository;
    private final BedrockMcVersionRepository bedrockMcVersionRepository;

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public JavaVersionResponse getAllJavaVersion(String username, String servername) {
        Server server = findServer(username, servername);
        VersionData versionData =
                buildVersionData(
                        javaMcVersionRepository.findAllByServer(server),
                        JavaMcVersion::getMcVersion,
                        JavaMcVersion::getIsPrimaryMcVersion);
        return new JavaVersionResponse(versionData.versions(), versionData.primaryVersion());
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public BedrockVersionResponse getAllBedrockVersion(String username, String servername) {
        Server server = findServer(username, servername);
        VersionData versionData =
                buildVersionData(
                        bedrockMcVersionRepository.findAllByServer(server),
                        BedrockMcVersion::getMcVersion,
                        BedrockMcVersion::getIsPrimaryMcVersion);
        return new BedrockVersionResponse(versionData.versions(), versionData.primaryVersion());
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void addJavaVersion(String username, String servername, AddJavaVersionRequest body) {
        Server server = findServer(username, servername);
        if (body == null) {
            throw new InvalidMcVersion("Version payload is invalid");
        }
        validateVersions(body.javaVersions(), body.primaryJavaVersion());
        javaMcVersionRepository.deleteAll(javaMcVersionRepository.findAllByServer(server));
        javaMcVersionRepository.saveAll(
                body.javaVersions().values().stream()
                        .flatMap(Set::stream)
                        .map(
                                version ->
                                        new JavaMcVersion(
                                                server,
                                                version,
                                                version.equals(body.primaryJavaVersion())))
                        .toList());
    }

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void addBedrockVersion(
            String username, String servername, AddBedrockVersionRequest body) {
        Server server = findServer(username, servername);
        if (body == null) {
            throw new InvalidMcVersion("Version payload is invalid");
        }
        validateVersions(body.bedrockVersions(), body.primaryBedrockVersion());
        bedrockMcVersionRepository.deleteAll(bedrockMcVersionRepository.findAllByServer(server));
        bedrockMcVersionRepository.saveAll(
                body.bedrockVersions().values().stream()
                        .flatMap(Set::stream)
                        .map(
                                version ->
                                        new BedrockMcVersion(
                                                server,
                                                version,
                                                version.equals(body.primaryBedrockVersion())))
                        .toList());
    }

    private @NonNull Server findServer(String username, String servername) {
        String userId =
                authUserUtils
                        .getAuthUserIdByUsername(username)
                        .orElseThrow(() -> new UserNotFound("User not found"));
        return serverRepository
                .findByUserIdAndServername(userId, servername)
                .orElseThrow(() -> new ServerNotFound("Server not found"));
    }

    private void validateVersions(Map<String, Set<String>> versions, String primaryVersion) {
        if (versions == null || primaryVersion == null && !versions.isEmpty()) {
            throw new InvalidMcVersion("Version payload is invalid");
        }
        for (Map.Entry<String, Set<String>> entry : versions.entrySet()) {
            Set<String> allowedVersions = McVersion.VERSIONS.get(entry.getKey());
            if (allowedVersions == null
                    || entry.getValue() == null
                    || !allowedVersions.containsAll(entry.getValue())) {
                throw new InvalidMcVersion("Invalid Minecraft version group: " + entry.getKey());
            }
        }
        Set<String> selectedVersions =
                versions.values().stream()
                        .flatMap(Set::stream)
                        .collect(java.util.stream.Collectors.toSet());
        if (primaryVersion != null && !selectedVersions.contains(primaryVersion)) {
            throw new InvalidMcVersion("Primary version must be selected");
        }
    }

    private String getMajorVersion(String version) {
        int secondDot = version.indexOf('.', version.indexOf('.') + 1);
        return secondDot == -1 ? version : version.substring(0, secondDot);
    }

    private <T> VersionData buildVersionData(
            List<T> serverVersions,
            Function<T, String> versionExtractor,
            Function<T, Boolean> primaryExtractor) {
        Map<String, Set<String>> versions =
                serverVersions.stream()
                        .collect(
                                Collectors.groupingBy(
                                        version -> getMajorVersion(versionExtractor.apply(version)),
                                        LinkedHashMap::new,
                                        Collectors.mapping(
                                                versionExtractor,
                                                Collectors.toCollection(LinkedHashSet::new))));
        String primaryVersion =
                serverVersions.stream()
                        .filter(version -> Boolean.TRUE.equals(primaryExtractor.apply(version)))
                        .map(versionExtractor)
                        .findFirst()
                        .orElse(null);
        return new VersionData(versions, primaryVersion);
    }
}
