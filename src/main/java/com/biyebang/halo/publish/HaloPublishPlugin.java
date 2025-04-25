package com.biyebang.halo.publish;

import org.springframework.stereotype.Component;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;


/**
 * @author liusu
 */
@Component
public class HaloPublishPlugin extends BasePlugin {

    public HaloPublishPlugin(PluginContext pluginContext) {
        super(pluginContext);
    }

    @Override
    public void start() {
        System.out.println("halo-publish插件启动成功！");
    }

    @Override
    public void stop() {
        System.out.println("halo-publish插件停止！");
    }
}
