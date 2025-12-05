package com.biyebang.halo.publish.service;

import com.biyebang.halo.publish.dto.ArticleDTO;
import reactor.core.publisher.Mono;

public interface SyncService {
    Mono<Void> publish(ArticleDTO article);
}