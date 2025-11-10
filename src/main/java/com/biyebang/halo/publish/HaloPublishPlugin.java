package com.biyebang.halo.publish;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;
import reactor.core.publisher.Mono;

/**
 * @author liusu
 */
@Component
public class HaloPublishPlugin extends BasePlugin {

    private final DatabaseClient databaseClient;

    @Autowired
    public HaloPublishPlugin(PluginContext pluginContext, DatabaseClient databaseClient) {
        super(pluginContext);
        this.databaseClient = databaseClient;
    }

    @Override
    public void start() {
        System.out.println("halo-publish 插件启动成功！");
        initDatabaseTables().subscribe(); // 异步执行建表
    }

    @Override
    public void stop() {
        System.out.println("halo-publish 插件停止！");
    }

    /**
     * 初始化插件所需的数据表（仅在不存在时创建）
     */
    private Mono<Void> initDatabaseTables() {
        // 1️⃣ 检查表是否存在（根据你要存储的平台同步配置表）
        String checkSql = """
            SELECT table_name FROM information_schema.tables 
            WHERE table_name = 'halo_publish_config'
            """;

        return databaseClient.sql(checkSql)
            .fetch()
            .first()
            .flatMap(result -> {
                // 如果有结果，说明表已存在
                if (result != null && result.containsKey("table_name")) {
                    System.out.println("✅ 数据表 halo_publish_config 已存在，无需创建。");
                    return Mono.empty();
                }
                return createTables();
            })
            .switchIfEmpty(createTables());
    }

    /**
     * 创建数据表
     */
    private Mono<Void> createTables() {
        System.out.println("⚙️ 检测到表不存在，正在创建数据表...");

        String createConfigTable = """
            CREATE TABLE IF NOT EXISTS halo_publish_config (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                platform VARCHAR(50) NOT NULL,
                app_id VARCHAR(128),
                app_secret VARCHAR(128),
                access_token VARCHAR(256),
                refresh_token VARCHAR(256),
                extra JSON,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;

        String createLogTable = """
            CREATE TABLE IF NOT EXISTS halo_publish_log (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                article_id BIGINT NOT NULL,
                platform VARCHAR(50) NOT NULL,
                publish_status VARCHAR(32),
                response TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        return databaseClient.sql(createConfigTable)
            .then()
            .then(databaseClient.sql(createLogTable).then())
            .doOnSuccess(v -> System.out.println("✅ 数据表创建完成。"));
    }
}
