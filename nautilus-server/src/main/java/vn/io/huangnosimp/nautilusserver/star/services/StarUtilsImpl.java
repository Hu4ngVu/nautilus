package vn.io.huangnosimp.nautilusserver.star.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import vn.io.huangnosimp.nautilusserver.logging.Logging;
import vn.io.huangnosimp.nautilusserver.star.StarUtils;
import vn.io.huangnosimp.nautilusserver.star.repositories.StarRepository;

@Service
@RequiredArgsConstructor
public class StarUtilsImpl implements StarUtils {
    private final StarRepository starRepository;

    @Override
    @Logging(level = Level.DEBUG, logArguments = true)
    public void deleteAllStar(String serverId) {
        starRepository.deleteAllByServerId(serverId);
    }
}
