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
 * 今日头条（头条号）内容同步服务
 * 使用开放平台 API 发布文章
 * <p>
 * 参考文档：https://open.snssdk.com/
 *
 * @author liusu
 */
@Service("toutiaoSyncService")
public class ToutiaoSyncService implements SyncService {

    private static final Logger logger = LoggerFactory.getLogger(ToutiaoSyncService.class);
    private final PlatformConfig config;
    private final RestTemplate restTemplate;

    public ToutiaoSyncService(PlatformConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void publish(ArticleDTO article) throws Exception {
        logger.info("[Toutiao] 正在发布文章：《{}》", article.getTitle());

        if (config.getToutiaoAccessToken() == null || config.getToutiaoAccessToken().isEmpty()) {
            throw new IllegalStateException(
                "今日头条发布失败：未配置 AccessToken。请在后台配置页面填写。");
        }

        // 1️⃣ 上传文章封面（可选）
        String coverUrl = uploadCoverImage(article.getCoverImageUrl());

        // 2️⃣ 发布图文内容
        String url =
            UriComponentsBuilder.fromHttpUrl("https://open.snssdk.com/toutiao/v1/content_publish/")
                .queryParam("access_token", config.getToutiaoAccessToken())
                .toUriString();

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", article.getTitle());
        payload.put("content", article.getContentHtml());
        if (coverUrl != null) {
            payload.put("cover_uri", coverUrl);
        }
        payload.put("article_type", "article"); // article 或 micro_article

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [Toutiao] 发布成功：{}", response.getBody());
            } else {
                logger.warn("⚠️ [Toutiao] 发布失败：状态码={}，响应={}", response.getStatusCode(),
                    response.getBody());
            }

        } catch (Exception e) {
            logger.error("❌ [Toutiao] 发布异常：{}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 上传封面图片到头条素材库
     */
    private String uploadCoverImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            String uploadUrl = UriComponentsBuilder
                .fromHttpUrl("https://open.snssdk.com/toutiao/image/upload/")
                .queryParam("access_token", config.getToutiaoAccessToken())
                .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // 这里只示例逻辑，实际可通过 FileSystemResource 或网络下载临时文件
            Map<String, Object> body = new HashMap<>();
            body.put("image", imageUrl);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof Map) {
                    String uri = (String) ((Map<?, ?>) data).get("uri");
                    logger.info("🖼️ [Toutiao] 封面上传成功，URI={}", uri);
                    return uri;
                }
            }
        } catch (Exception e) {
            logger.warn("⚠️ [Toutiao] 封面上传失败：{}", e.getMessage());
        }

        return null;
    }
}
