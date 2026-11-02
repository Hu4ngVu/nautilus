package vn.io.huangnosimp.nautilusserver.server.services;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.huangnosimp.nautilusserver.exceptions.InvalidServerTag;
import vn.io.huangnosimp.nautilusserver.exceptions.ServerNotFound;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.server.constants.Tag;
import vn.io.huangnosimp.nautilusserver.server.constants.TagCategory;
import vn.io.huangnosimp.nautilusserver.server.dto.requests.AddTagRequest;
import vn.io.huangnosimp.nautilusserver.server.dto.responses.TagResponse;
import vn.io.huangnosimp.nautilusserver.server.models.Server;
import vn.io.huangnosimp.nautilusserver.server.models.ServerTag;
import vn.io.huangnosimp.nautilusserver.server.repositories.ServerRepository;
import vn.io.huangnosimp.nautilusserver.server.repositories.TagRepository;

@Service
@RequiredArgsConstructor
public class ServerTagService {
    private final ServerRepository serverRepository;
    private final TagRepository tagRepository;

    @Logging(level = Level.DEBUG, logArguments = true)
    public TagResponse getAllServerTag(String serverId) {
        Server server = getServer(serverId);
        Map<TagCategory, Set<String>> tagMap =
                tagRepository.findAllByServer(server).stream()
                        .sorted(
                                Comparator.comparing((ServerTag tag) -> tag.getCategory().name())
                                        .thenComparing(ServerTag::getTag))
                        .collect(
                                Collectors.groupingBy(
                                        ServerTag::getCategory,
                                        LinkedHashMap::new,
                                        Collectors.mapping(
                                                ServerTag::getTag,
                                                Collectors.toCollection(LinkedHashSet::new))));
        return new TagResponse(tagMap);
    }

    @Logging(level = Level.DEBUG, logArguments = true)
    @Transactional(rollbackFor = Exception.class)
    public void addServerTag(String serverId, AddTagRequest body) {
        Server server = getServer(serverId);
        if (body == null || body.tags() == null) {
            throw new InvalidServerTag("Tags must not be null");
        }
        validateTags(body.tags());
        tagRepository.deleteAllByServerId(server.getId());
        List<ServerTag> tags =
                body.tags().entrySet().stream()
                        .flatMap(
                                entry ->
                                        entry.getValue().stream()
                                                .map(
                                                        tag ->
                                                                new ServerTag(
                                                                        server,
                                                                        tag,
                                                                        entry.getKey())))
                        .collect(Collectors.toList());
        tagRepository.saveAll(tags);
    }

    private Server getServer(String serverId) {
        return serverRepository
                .findById(serverId)
                .orElseThrow(() -> new ServerNotFound("Server not found"));
    }

    private void validateTags(Map<TagCategory, Set<String>> tags) {
        if (tags == null) {
            throw new InvalidServerTag("Tags must not be null");
        }
        if (tags.entrySet().stream()
                .anyMatch(entry -> entry.getKey() == null || entry.getValue() == null)) {
            throw new InvalidServerTag("Tag categories and values must not be null");
        }

        int tagCount = tags.values().stream().mapToInt(Set::size).sum();
        if (tagCount > 20) {
            throw new InvalidServerTag("A server cannot have more than 20 tags");
        }

        Map<TagCategory, Set<String>> allowedTags = Tag.getTagMap();
        tags.forEach(
                (category, categoryTags) -> {
                    Set<String> allowedCategoryTags = allowedTags.getOrDefault(category, Set.of());
                    categoryTags.forEach(
                            tag -> {
                                if (!allowedCategoryTags.contains(tag)) {
                                    throw new InvalidServerTag(
                                            "Invalid tag for category " + category + ": " + tag);
                                }
                            });
                });
    }
}
