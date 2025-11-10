package com.biyebang.halo.publish.service;

import com.biyebang.halo.publish.dto.ArticleDTO;

public interface SyncService {
    void publish(ArticleDTO article) throws Exception;
}
