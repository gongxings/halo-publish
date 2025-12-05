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
@Service("xhsSyncService")
@RequiredArgsConstructor
public class XhsSyncService implements SyncService {
    private final PlatformConfig config;
    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Void> publish(ArticleDTO article) {
        log.info("[XHS] publishing article: {}", article.getTitle());

        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("grant_type", "client_credentials");
        tokenParams.put("client_id", config.getXhsClientId());
        tokenParams.put("client_secret", config.getXhsClientSecret());

        return webClient.post()
                .uri("https://open.xiaohongshu.com/oauth2/access_token")
                .bodyValue(tokenParams)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(resp -> {
                    if (resp.get("access_token") == null) {
                        return Mono.error(new RuntimeException("获取小红书 access_token 失败"));
                    }
                    String token = (String) resp.get("access_token");
                    return createNote(token, article);
                });
    }

    private Mono<Void> createNote(String token, ArticleDTO article) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", article.getTitle());
        body.put("content", article.getSummary());
        body.put("image_urls", article.getImageUrls());

        return webClient.post()
                .uri("https://open.xiaohongshu.com/api/partner/note/create")
                .header("Authorization", "Bearer " + token)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(resp -> log.info("[XHS] publish success: {}", resp))
                .then();
    }
}