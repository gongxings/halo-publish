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
@Service("wechatSyncService")
@RequiredArgsConstructor
public class WechatSyncService implements SyncService {
    private final PlatformConfig config;
    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Void> publish(ArticleDTO article) {
        String tokenUrl = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                config.getWechatAppId(), config.getWechatAppSecret());

        return webClient.get()
                .uri(tokenUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(resp -> {
                    if (resp == null || resp.get("access_token") == null) {
                        return Mono.error(new RuntimeException("Failed to get wechat access_token: " + resp));
                    }
                    String accessToken = (String) resp.get("access_token");
                    log.info("[WeChat] access_token obtained");
                    return addNews(accessToken, article);
                });
    }

    private Mono<Void> addNews(String accessToken, ArticleDTO article) {
        Map<String, Object> item = new HashMap<>();
        item.put("title", article.getTitle());
        item.put("thumb_media_id", "");
        item.put("author", "");
        item.put("digest", article.getSummary() == null ? "" : article.getSummary());
        item.put("show_cover_pic", 1);
        item.put("content", article.getContentHtml());

        Map<String, Object> body = new HashMap<>();
        body.put("articles", new Object[]{item});

        String addNewsUrl = "https://api.weixin.qq.com/cgi-bin/material/add_news?access_token=" + accessToken;

        return webClient.post()
                .uri(addNewsUrl)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(resp -> {
                    log.info("[WeChat] add_news response: {}", resp);
                    if (resp == null || resp.get("media_id") == null) {
                        return Mono.error(new RuntimeException("WeChat add_news failed: " + resp));
                    }
                    return Mono.empty();
                });
    }
}