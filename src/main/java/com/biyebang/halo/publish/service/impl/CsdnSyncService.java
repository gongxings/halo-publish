package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("csdnSyncService")
@RequiredArgsConstructor
public class CsdnSyncService implements SyncService {
    private final PlatformConfig config;
    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Void> publish(ArticleDTO article) {
        log.info("[CSDN] publishing article: {}", article.getTitle());

        Map<String, Object> body = new HashMap<>();
        body.put("title", article.getTitle());
        body.put("content", article.getContentHtml());
        body.put("tags", "同步,AI");

        return webClient.post()
                .uri("https://api.csdn.net/blog/add")
                .header("Authorization", "Bearer " + config.getCsdnToken())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(resp -> log.info("[CSDN] publish success: {}", resp))
                .then();
    }
}