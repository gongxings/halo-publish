package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Realistic WeChat implementation (public account):
 * - get access_token: /cgi-bin/token
 * - upload image: /cgi-bin/media/uploadimg
 * - add_news: /cgi-bin/material/add_news
 * <p>
 * See WeChat docs: https://developers.weixin.qq
 * .com/doc/offiaccount/Basic_Information/Access_Overview.html
 */
@Service("wechatSyncService")
public class WechatSyncService implements SyncService {
    private static final Logger logger = LoggerFactory.getLogger(WechatSyncService.class);
    private final PlatformConfig config;
    private final RestTemplate rest = new RestTemplate();

    public WechatSyncService(PlatformConfig config) {
        this.config = config;
    }

    @Override
    public void publish(ArticleDTO article) throws Exception {
        // 1) get access token
        String tokenUrl = String.format(
            "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret"
                    + "=%s",
            config.getWechatAppId(), config.getWechatAppSecret());
        Map resp = rest.getForObject(tokenUrl, Map.class);
        if (resp == null || resp.get("access_token") == null) {
            throw new RuntimeException("failed to get wechat access_token: " + resp);
        }
        String accessToken = (String) resp.get("access_token");
        logger.info("wechat access_token={}", accessToken);

        // 2) upload image (if cover exists) - using uploadimg which returns URL
        String thumbUrl = article.getCoverImageUrl();
        String uploadedUrl = thumbUrl;
        if (thumbUrl != null && !thumbUrl.isEmpty()) {
            // We just pass through here; to actually upload you would POST multipart/form-data to:
            // https://api.weixin.qq.com/cgi-bin/media/uploadimg?access_token=ACCESS_TOKEN
            // For brevity we reuse original URL
            uploadedUrl = thumbUrl;
        }

        // 3) create news JSON and call add_news
        Map<String, Object> item = new HashMap<>();
        item.put("title", article.getTitle());
        item.put("thumb_media_id",""); // for production, you should upload as permanent material to get media_id
        item.put("author", "");
        item.put("digest", article.getSummary() == null ? "" : article.getSummary());
        item.put("show_cover_pic", 1);
        item.put("content", article.getContentHtml());
        Map<String, Object> body = new HashMap<>();
        body.put("articles", new Object[] {item});

        String addNewsUrl =
            "https://api.weixin.qq.com/cgi-bin/material/add_news?access_token=" + accessToken;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        Map addResp = rest.postForObject(addNewsUrl, entity, Map.class);
        logger.info("wechat add_news resp={}", addResp);
        if (addResp == null || addResp.get("media_id") == null) {
            throw new RuntimeException("wechat add_news failed: " + addResp);
        }
    }
}
