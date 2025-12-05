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
@Service("juejinSyncService")
@RequiredArgsConstructor
public class JuejinSyncService implements SyncService {
    private final PlatformConfig config;
    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Void> publish(ArticleDTO article) {
        log.info("[Juejin] publishing article: {}", article.getTitle());

        Map<String, Object> payload = new HashMap<>();
        payload.put("article_id", 0);
        payload.put("title", article.getTitle());
        payload.put("content", article.getContentHtml());

        return webClient.post()
                .uri("https://api.juejin.cn/content_api/v1/article/publish")
                .header("Cookie", config.getJuejinCookie() == null ? "" : config.getJuejinCookie())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(resp -> {
                    log.info("[Juejin] response: {}", resp);
                    return Mono.empty();
                });
    }
}