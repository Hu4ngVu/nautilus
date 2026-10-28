package vn.io.huangnosimp.nautilusserver.star.controllers;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.io.huangnosimp.nautilusserver.star.dto.ServerStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.dto.UserStaredInfo;
import vn.io.huangnosimp.nautilusserver.star.services.StarService;

@RestController
@RequestMapping("/stars")
@RequiredArgsConstructor
public class StarController {
    private final StarService starService;

    @GetMapping("/{username}/{servername}")
    public ResponseEntity<Page<UserStaredInfo>> getUserStaredInfo(
            @PathVariable String username,
            @PathVariable String servername,
            @PageableDefault(
                            size = 20,
                            sort = {"createdAt"})
                    Pageable pageable) {
        return ResponseEntity.ok(starService.getUserStaredInfo(username, servername, pageable));
    }

    @GetMapping("/{username}")
    public ResponseEntity<Page<ServerStaredInfo>> getServerStaredInfo(
            @PathVariable String username,
            @PageableDefault(
                            size = 20,
                            sort = {"createdAt"})
                    Pageable pageable) {
        return ResponseEntity.ok(starService.getServerStaredInfo(username, pageable));
    }

    @PutMapping("/{username}/{servername}")
    public ResponseEntity<?> addStar(
            @PathVariable String username, @PathVariable String servername) {
        starService.addStar(getAuthentication().getName(), username, servername);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{username}/{servername}")
    public ResponseEntity<?> deleteStar(
            @PathVariable String username, @PathVariable String servername) {
        starService.deleteStar(getAuthentication().getName(), username, servername);
        return ResponseEntity.noContent().build();
    }

    private Authentication getAuthentication() {
        return Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
