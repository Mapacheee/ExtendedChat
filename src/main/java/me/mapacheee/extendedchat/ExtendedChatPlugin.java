package me.mapacheee.extendedchat;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import com.thewinterframework.paper.PaperWinterPlugin;
import com.thewinterframework.plugin.WinterBootPlugin;
import com.thewinterframework.service.annotation.Service;

@WinterBootPlugin
public final class ExtendedChatPlugin extends PaperWinterPlugin {

    private static volatile ExtendedChatPlugin instance;
    private static volatile boolean loading = false;

    public static ExtendedChatPlugin getInstance() {
        return instance;
    }

    public static <T> T getService(Class<T> type) {
        ExtendedChatPlugin current = instance;
        if (current == null || loading) {
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

    @Override
    public void configure(Binder binder) {
        binder.bindScope(Service.class, Scopes.SINGLETON);
    }
}