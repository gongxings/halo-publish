package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Juejin (掘金) publish implementation (best-effort):
 * Uses web endpoints which historically exist (see community references).
 * For production, obtain valid cookies and CSRF tokens.
 *
 * Reference: https://github.com/chenzijia12300/juejin-api
 */
@Service("juejinSyncService")
public class JuejinSyncService implements SyncService {
    private static final Logger logger = LoggerFactory.getLogger(JuejinSyncService.class);
    private final PlatformConfig config;
    private final RestTemplate rest = new RestTemplate();

    public JuejinSyncService(PlatformConfig config) { this.config = config; }

    @Override
    public void publish(ArticleDTO article) throws Exception {
        String url = "https://api.juejin.cn/content_api/v1/article/publish"; // may require auth & CSRF
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", config.getJuejinCookie() == null ? "" : config.getJuejinCookie());
        Map<String, Object> payload = new HashMap<>();
        payload.put("article_id", 0);
        payload.put("title", article.getTitle());
        payload.put("content", article.getContentHtml());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> resp = rest.postForEntity(url, entity, Map.class);
        logger.info("juejin resp status={} body={}", resp.getStatusCode(), resp.getBody());
        if (!resp.getStatusCode().is2xxSuccessful()) throw new RuntimeException("juejin publish failed");
    }
}
