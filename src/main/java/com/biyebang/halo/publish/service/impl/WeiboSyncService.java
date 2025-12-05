package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service("weiboSyncService")
@RequiredArgsConstructor
public class WeiboSyncService implements SyncService {
    private final PlatformConfig config;
    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Void> publish(ArticleDTO article) {
        log.info("[Weibo] publishing: {}", article.getTitle());

        String token = config.getWeiboAccessToken();
        if (token == null || token.isEmpty()) {
            return Mono.error(new IllegalStateException("微博发布失败：未配置 AccessToken"));
        }

        String text = article.getTitle() + "\n\n" + stripHtml(article.getContentHtml());
        if (text.length() > 2000) {
            text = text.substring(0, 1990) + "…";
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("access_token", token);
        formData.add("status", text);

        return webClient.post()
                .uri("https://api.weibo.com/2/statuses/update.json")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(resp -> log.info("[Weibo] publish success: {}", resp))
                .then();
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }
}