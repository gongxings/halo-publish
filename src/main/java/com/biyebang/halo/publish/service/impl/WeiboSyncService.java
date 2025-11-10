package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微博发布服务实现
 * 支持文字、图片发布。
 * <p>
 * 官方文档：https://open.weibo.com/wiki/API
 * 关键接口：
 * - statuses/update ：发布纯文本
 * - statuses/upload ：发布图文
 *
 * @author liusu
 */
@Service("weiboSyncService")
public class WeiboSyncService implements SyncService {

    private static final Logger logger = LoggerFactory.getLogger(WeiboSyncService.class);
    private final PlatformConfig config;
    private final RestTemplate restTemplate;

    public WeiboSyncService(PlatformConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void publish(ArticleDTO article) throws Exception {
        logger.info("[Weibo] 正在发布微博：《{}》", article.getTitle());

        String token = config.getWeiboAccessToken();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException(
                "微博发布失败：未配置 AccessToken，请在后台配置页面填写。");
        }

        // 微博接口限制文字长度为 2000 字以内
        String text = article.getTitle() + "\n\n" + stripHtml(article.getContentHtml());
        if (text.length() > 2000) {
            text = text.substring(0, 1990) + "…";
        }

        if (article.getCoverImageUrl() != null && !article.getCoverImageUrl().isEmpty()) {
            publishImageWeibo(token, text, article.getCoverImageUrl());
        } else {
            publishTextWeibo(token, text);
        }
    }

    /**
     * 发布纯文本微博
     */
    private void publishTextWeibo(String accessToken, String content) {
        String url = "https://api.weibo.com/2/statuses/update.json";
        Map<String, String> params = new HashMap<>();
        params.put("access_token", accessToken);
        params.put("status", content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [Weibo] 文字发布成功：{}", response.getBody());
            } else {
                logger.warn("⚠️ [Weibo] 发布失败：状态码={}，响应={}", response.getStatusCode(),
                    response.getBody());
            }
        } catch (Exception e) {
            logger.error("❌ [Weibo] 发布异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 发布图文微博
     */
    private void publishImageWeibo(String accessToken, String content, String imageUrl) {
        String url = "https://api.weibo.com/2/statuses/share.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        Map<String, Object> form = new HashMap<>();
        form.put("access_token", accessToken);
        form.put("status", content);

        try {
            // 从 URL 下载图片
            File tmp = File.createTempFile("weibo_img_", ".jpg");
            Files.copy(new java.net.URL(imageUrl).openStream(), tmp.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            org.springframework.core.io.FileSystemResource resource =
                new org.springframework.core.io.FileSystemResource(tmp);
            form.put("pic", resource);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(form, headers);
            ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("✅ [Weibo] 图文发布成功：{}", response.getBody());
            } else {
                logger.warn("⚠️ [Weibo] 图文发布失败：{}", response.getBody());
            }

            tmp.delete();
        } catch (Exception e) {
            logger.error("❌ [Weibo] 图文发布异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 移除 HTML 标签
     */
    private String stripHtml(String html) {
        return html.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
    }
}
