package com.biyebang.halo.publish.controller;

import com.biyebang.halo.publish.domain.PlatformType;
import com.biyebang.halo.publish.domain.SyncJob;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.impl.SyncManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ApiVersion;

@ApiVersion("publish.halo.run/v1alpha1")
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncController {
    private final SyncManagerService manager;

    @PostMapping("/publish/{articleId}")
    public Mono<SyncJob> publish(@PathVariable Long articleId,
                                 @RequestParam(defaultValue = "WECHAT") String platform) {
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setId(articleId);
        articleDTO.setTitle("Sample " + articleId);
        articleDTO.setContentHtml("<p>content</p>");

        PlatformType platformType = PlatformType.valueOf(platform);
        return manager.publishTo(articleDTO, platformType);
    }

    @PostMapping("/publish-all/{articleId}")
    public Flux<SyncJob> publishAll(@PathVariable Long articleId) {
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setId(articleId);
        articleDTO.setTitle("Sample " + articleId);
        articleDTO.setContentHtml("<p>content</p>");

        return manager.publishToAll(articleDTO);
    }
}