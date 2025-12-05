package com.biyebang.halo.publish.service.impl;

import com.biyebang.halo.publish.domain.PlatformType;
import com.biyebang.halo.publish.domain.SyncJob;
import com.biyebang.halo.publish.dto.ArticleDTO;
import com.biyebang.halo.publish.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SyncManagerService {

    private final ReactiveExtensionClient client;
    private final Map<PlatformType, SyncService> serviceMap;

    public SyncManagerService(ReactiveExtensionClient client, List<SyncService> services) {
        this.client = client;
        this.serviceMap = services.stream().collect(Collectors.toMap(s -> {
            String name = s.getClass().getSimpleName().toUpperCase();
            if (name.contains("WECHAT")) return PlatformType.WECHAT;
            if (name.contains("XHS")) return PlatformType.XHS;
            if (name.contains("CSDN")) return PlatformType.CSDN;
            if (name.contains("JUEJIN")) return PlatformType.JUEJIN;
            if (name.contains("ZHIHU")) return PlatformType.ZHIHU;
            if (name.contains("WEIBO")) return PlatformType.WEIBO;
            if (name.contains("TOUTIAO")) return PlatformType.TOUTIAO;
            return null;
        }, s -> s));
    }

    public Mono<SyncJob> publishTo(ArticleDTO article, PlatformType platform) {
        return createSyncJob(article, platform)
                .flatMap(job -> {
                    SyncService service = serviceMap.get(platform);
                    if (service == null) {
                        return updateJobStatus(job, "FAILED", "No service for platform: " + platform);
                    }
                    return updateJobStatus(job, "RUNNING", null)
                            .flatMap(runningJob -> service.publish(article)
                                    .then(updateJobStatus(runningJob, "SUCCESS", "ok"))
                                    .onErrorResume(e -> {
                                        log.error("Publish to {} failed", platform, e);
                                        return updateJobStatus(runningJob, "FAILED", e.getMessage());
                                    }));
                });
    }

    public Flux<SyncJob> publishToAll(ArticleDTO article) {
        return Flux.fromArray(PlatformType.values())
                .flatMap(platform -> publishTo(article, platform));
    }

    private Mono<SyncJob> createSyncJob(ArticleDTO article, PlatformType platform) {
        SyncJob job = new SyncJob();
        Metadata metadata = new Metadata();
        metadata.setName("syncjob-" + UUID.randomUUID().toString().substring(0, 8));
        job.setMetadata(metadata);

        SyncJob.SyncJobSpec spec = new SyncJob.SyncJobSpec();
        spec.setArticleName(article.getTitle());
        spec.setPlatform(platform);
        job.setSpec(spec);

        SyncJob.SyncJobStatus status = new SyncJob.SyncJobStatus();
        status.setState("PENDING");
        status.setStartedAt(Instant.now());
        job.setStatus(status);

        return client.create(job);
    }

    private Mono<SyncJob> updateJobStatus(SyncJob job, String state, String message) {
        return client.get(SyncJob.class, job.getMetadata().getName())
                .flatMap(latest -> {
                    if (latest.getStatus() == null) {
                        latest.setStatus(new SyncJob.SyncJobStatus());
                    }
                    latest.getStatus().setState(state);
                    latest.getStatus().setMessage(message);
                    if ("SUCCESS".equals(state) || "FAILED".equals(state)) {
                        latest.getStatus().setCompletedAt(Instant.now());
                    }
                    return client.update(latest);
                });
    }
}