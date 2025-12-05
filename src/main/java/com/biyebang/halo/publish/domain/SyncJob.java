package com.biyebang.halo.publish.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

import java.time.Instant;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "publish.halo.run", version = "v1alpha1", kind = "SyncJob",
        plural = "syncjobs", singular = "syncjob")
public class SyncJob extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private SyncJobSpec spec;

    private SyncJobStatus status;

    @Data
    public static class SyncJobSpec {
        @Schema(requiredMode = REQUIRED)
        private String articleName;

        @Schema(requiredMode = REQUIRED)
        private PlatformType platform;
    }

    @Data
    public static class SyncJobStatus {
        private String state; // PENDING, RUNNING, SUCCESS, FAILED
        private String message;
        private Instant startedAt;
        private Instant completedAt;
    }
}