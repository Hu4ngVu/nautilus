package vn.io.huangnosimp.nautilusserver.star.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.star.StarUtils;
import vn.io.huangnosimp.nautilusserver.star.repositories.StarRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class StarUtilsImpl implements StarUtils {
    private final StarRepository starRepository;

    @Override
    public void deleteAllStar(String serverId) {
        log.debug("Deleting all stars for server: {}", serverId);
        starRepository.deleteAllByServerId(serverId);
        log.info("All stars for server: {} deleted successfully", serverId);
    }
}
