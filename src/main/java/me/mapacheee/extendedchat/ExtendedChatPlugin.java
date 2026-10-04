package me.mapacheee.extendedchat;

import com.thewinterframework.paper.PaperWinterPlugin;
import com.thewinterframework.plugin.WinterBootPlugin;

@WinterBootPlugin
public final class ExtendedChatPlugin extends PaperWinterPlugin {

    private static volatile ExtendedChatPlugin instance;
    private static volatile boolean loading = false;

    public static ExtendedChatPlugin getInstance() {
        return instance;
    }

    public static <T> T getService(Class<T> type) {
        ExtendedChatPlugin current = instance;
        if (current == null || loading || current.getInjector() == null) {
            throw new IllegalStateException("ExtendedChat plugin is not loaded yet");
        }
        return current.getInjector().getInstance(type);
    }

    @Override
    public void onPluginLoad() {
        loading = true;
        try {
            super.onPluginLoad();
            instance = this;
        } finally {
            loading = false;
        }
    }

    @Override
    public void onPluginDisable() {
        instance = null;
        super.onPluginDisable();
    }

}
