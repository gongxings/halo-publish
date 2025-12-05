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
@Service("zhihuSyncService")
@RequiredArgsConstructor
public class ZhihuSyncService implements SyncService {
    private final PlatformConfig config;
    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Void> publish(ArticleDTO article) {
        log.info("[Zhihu] publishing article: {}", article.getTitle());

        if (config.getZhihuCookie() == null || config.getZhihuCookie().isEmpty()) {
            return Mono.error(new IllegalStateException("知乎发布失败：未配置 Cookie"));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", article.getTitle());
        payload.put("content", article.getContentHtml());
        payload.put("topics", article.getTags());
        payload.put("author", article.getAuthorName());
        payload.put("published", true);

        return webClient.post()
                .uri("https://zhuanlan.zhihu.com/api/articles")
                .header("Cookie", config.getZhihuCookie())
                .header("x-requested-with", "fetch")
                .header("referer", "https://zhuanlan.zhihu.com/write")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/118 Safari/537.36")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(resp -> log.info("[Zhihu] publish success: {}", resp))
                .then();
    }
}