package com.biyebang.halo.publish.controller;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.domain.PlatformType;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.impl.SyncManagerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plugins/sync")
public class SyncController {
    private final SyncManagerService manager;
    private final PlatformConfig config;

    public SyncController(SyncManagerService manager, PlatformConfig config) {
        this.manager = manager;
        this.config = config;
    }

    @PostMapping("/publish/{articleId}")
    public ResponseEntity<String> publish(@PathVariable Long articleId,
        @RequestParam(defaultValue = "ALL") String platform) {
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setId(articleId);
        articleDTO.setTitle("Sample " + articleId);
        articleDTO.setContentHtml("<p>content</p>");

        if ("ALL".equalsIgnoreCase(platform)) {
            manager.publishToAll(articleDTO);
            return ResponseEntity.ok("enqueued:all");
        }
        PlatformType platformType = PlatformType.valueOf(platform);
        manager.publishTo(articleDTO, platformType);
        return ResponseEntity.ok("enqueued:" + platformType.name());
    }
}
