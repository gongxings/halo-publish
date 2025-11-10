package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 知乎专栏发布服务
 * 支持通过 Cookie 调用知乎网页版接口发布文章。
 * （知乎未开放标准 API，仅支持模拟登录后请求）
 *
 * @author liusu
 */
@Service("zhihuSyncService")
public class ZhihuSyncService implements SyncService {

    private static final Logger logger = LoggerFactory.getLogger(ZhihuSyncService.class);
    private final PlatformConfig config;
    private final RestTemplate restTemplate;

    public ZhihuSyncService(PlatformConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void publish(ArticleDTO article) throws Exception {
        logger.info("[Zhihu] 正在尝试发布文章：《{}》", article.getTitle());

        // 1️⃣ 检查知乎 Cookie 是否配置
        if (config.getZhihuCookie() == null || config.getZhihuCookie().isEmpty()) {
            throw new IllegalStateException(
                "知乎发布失败：未配置 Cookie。请在后台配置页面填写知乎 Cookie。");
        }

        // 2️⃣ 构建请求 URL
        String url = "https://zhuanlan.zhihu.com/api/articles";

        // 3️⃣ 构建请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("title", article.getTitle());
        payload.put("content", article.getContentHtml());
        payload.put("topics", article.getTags()); // 可选标签
        payload.put("author", article.getAuthorName());
        payload.put("published", true); // true 表示直接发布，false 表示仅保存草稿

        // 4️⃣ 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("cookie", config.getZhihuCookie());
        headers.set("x-requested-with", "fetch");
        headers.set("referer", "https://zhuanlan.zhihu.com/write");
        headers.set("user-agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/118 Safari/537.36");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            // 5️⃣ 发起 POST 请求
            ResponseEntity<String> response = restTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(url).toUriString(),
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [Zhihu] 发布成功：{}", response.getBody());
            } else {
                logger.warn("⚠️ [Zhihu] 发布失败：状态码={}，响应={}", response.getStatusCode(),
                    response.getBody());
            }
        } catch (Exception e) {
            logger.error("❌ [Zhihu] 发布异常：{}", e.getMessage(), e);
            throw e;
        }
    }
}
