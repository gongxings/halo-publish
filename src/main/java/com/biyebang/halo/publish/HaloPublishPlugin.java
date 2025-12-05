package com.biyebang.halo.publish;

import com.biyebang.halo.publish.domain.SyncJob;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

@Component
public class HaloPublishPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public HaloPublishPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        System.out.println("halo-publish 插件启动成功！");
        schemeManager.register(SyncJob.class);
    }

    @Override
    public void stop() {
        System.out.println("halo-publish 插件停止！");
        schemeManager.unregister(schemeManager.get(SyncJob.class));
    }
}