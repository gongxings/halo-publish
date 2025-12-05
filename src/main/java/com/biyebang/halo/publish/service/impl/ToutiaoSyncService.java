package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("toutiaoSyncService")
@RequiredArgsConstructor
public class ToutiaoSyncService implements SyncService {
    private final PlatformConfig config;
    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Void> publish(ArticleDTO article) {
        log.info("[Toutiao] publishing article: {}", article.getTitle());

        if (config.getToutiaoAccessToken() == null || config.getToutiaoAccessToken().isEmpty()) {
            return Mono.error(new IllegalStateException("今日头条发布失败：未配置 AccessToken"));
        }

        String url = UriComponentsBuilder
                .fromHttpUrl("https://open.snssdk.com/toutiao/v1/content_publish/")
                .queryParam("access_token", config.getToutiaoAccessToken())
                .toUriString();

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", article.getTitle());
        payload.put("content", article.getContentHtml());
        payload.put("article_type", "article");

        return webClient.post()
                .uri(url)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(resp -> log.info("[Toutiao] publish success: {}", resp))
                .then();
    }
}