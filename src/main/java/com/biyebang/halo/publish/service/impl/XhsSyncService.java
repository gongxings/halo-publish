package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

/**
 * Xiaohongshu (小红书) adapter.
 * Xiaohongshu provides a partner/open API in sandbox modes; production APIs require application and credentials.
 * See sandbox docs: http://flssandbox.xiaohongshu.com/ark/open_api/ (community references)
 */
@Service("xhsSyncService")
public class XhsSyncService implements SyncService {
    private final PlatformConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    public XhsSyncService(PlatformConfig config) {
        this.config = config;
    }

    @Override
    public void publish(ArticleDTO article) throws Exception {
        String tokenUrl = "https://open.xiaohongshu.com/oauth2/access_token";
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");
        params.put("client_id", config.getXhsClientId());
        params.put("client_secret", config.getXhsClientSecret());

        ResponseEntity<Map> tokenResp = restTemplate.postForEntity(tokenUrl, params, Map.class);
        if (tokenResp.getBody().get("access_token") == null) {
            throw new RuntimeException("获取小红书 access_token 失败");
        }
        String token = (String) tokenResp.getBody().get("access_token");

        String noteUrl = "https://open.xiaohongshu.com/api/partner/note/create";
        Map<String, Object> body = new HashMap<>();
        body.put("title", article.getTitle());
        body.put("content", article.getSummary());
        body.put("image_urls", article.getImageUrls());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(noteUrl, new HttpEntity<>(body, headers), String.class);
    }
}
