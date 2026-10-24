package vn.io.huangnosimp.nautilusserver.server;

import java.util.List;

public record DeleteImagesBatchEvent(
        List<String> keys
) {
}
