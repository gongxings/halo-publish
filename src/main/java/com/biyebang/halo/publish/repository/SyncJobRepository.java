package com.biyebang.halo.publish.repository;

import com.biyebang.halo.publish.domain.PlatformType;
import com.biyebang.halo.publish.domain.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long> {
    List<SyncJob> findByStatus(String status);
    List<SyncJob> findByPlatformAndStatus(PlatformType platform, String status);
}
