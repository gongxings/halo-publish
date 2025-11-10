package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.config.PlatformConfig;
import com.biyebang.halo.publish.domain.PlatformType;
import com.biyebang.halo.publish.domain.SyncJob;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.repository.SyncJobRepository;
import com.biyebang.halo.publish.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyncManagerService {
    private static final Logger logger = LoggerFactory.getLogger(SyncManagerService.class);

    private final Map<PlatformType, SyncService> serviceMap;
    private final SyncJobRepository jobRepository;

    public SyncManagerService(List<SyncService> services, SyncJobRepository jobRepository) {
        this.jobRepository = jobRepository;
        this.serviceMap = services.stream().collect(Collectors.toMap(s -> {
            String name = s.getClass().getSimpleName().toUpperCase();
            if (name.contains("WECHAT")) {
                return PlatformType.WECHAT;
            }
            if (name.contains("XHS")) {
                return PlatformType.XHS;
            }
            if (name.contains("CSDN")) {
                return PlatformType.CSDN;
            }
            if (name.contains("JUEJIN")) {
                return PlatformType.JUEJIN;
            }
            if (name.contains("ZHIHU")) {
                return PlatformType.ZHIHU;
            }
            if (name.contains("WEIBO")) {
                return PlatformType.WEIBO;
            }
            if (name.contains("TOUTIAO")) {
                return PlatformType.TOUTIAO;
            }
            return null;
        }, s -> s));
    }

    public void publishTo(ArticleDTO article, PlatformType platform) {
        SyncJob job = new SyncJob();
        job.setArticleId(article.getId());
        job.setPlatform(platform);
        job.setStatus("PENDING");
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        SyncService service = serviceMap.get(platform);
        if (service == null) {
            job.setStatus("FAILED");
            job.setMessage("No service for platform");
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
            return;
        }

        try {
            job.setStatus("RUNNING");
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
            service.publish(article);
            job.setStatus("SUCCESS");
            job.setMessage("ok");
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
        } catch (Exception e) {
            logger.error("publish failed", e);
            job.setStatus("FAILED");
            job.setMessage(e.getMessage());
            job.setUpdatedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }

    public void publishToAll(ArticleDTO article) {
        for (PlatformType p : PlatformType.values()) {
            publishTo(article, p);
        }
    }
}
