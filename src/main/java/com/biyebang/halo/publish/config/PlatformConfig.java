package com.biyebang.halo.publish.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "plugin.sync")
@Data
public class PlatformConfig {
    private String wechatAppId;
    private String wechatAppSecret;

    private String xhsClientId;
    private String xhsClientSecret;

    private String csdnToken;

    private String juejinCookie;

    private String zhihuCookie;

    private String weiboClientId;
    private String weiboClientSecret;
    private String weiboAccessToken;
    private String weiboRedirectUri;

    private String toutiaoClientId;
    private String toutiaoClientSecret;
    private String toutiaoAccessToken;
    private String toutiaoRefreshToken;
}
