package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * CSDN adapter - CSDN does not publish a stable public API for article creation;
 * common approaches: use OAuth + web endpoints or use authorized developer APIs if granted.
 */
@Service("csdnSyncService")
public class CsdnSyncService implements SyncService {
    private static final Logger logger = LoggerFactory.getLogger(CsdnSyncService.class);
    private final PlatformConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public CsdnSyncService(PlatformConfig config) {
        this.config = config;
    }

    @Override
    public void publish(ArticleDTO article) throws Exception {
        logger.info("[CSDN] publish simulated - implement via OAuth or web endpoints");
        String url = "https://api.csdn.net/blog/add";

        Map<String, Object> body = new HashMap<>();
        body.put("title", article.getTitle());
        body.put("content", article.getContentHtml());
        body.put("tags", "同步,AI");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(config.getCsdnToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
    }
}
