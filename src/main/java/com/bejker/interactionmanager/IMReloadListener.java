package com.bejker.interactionmanager;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import com.bejker.interactionmanager.search.SearchUtil;
import net.minecraft.util.profiler.Profiler;

public class IMReloadListener implements IdentifiableResourceReloadListener {
    private static final ResourceManagerHelper helper = ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES);

    public void init() {
        helper.registerReloadListener(this);
    }

    /**
     * @see IMReloadListener#reload(Synchronizer, ResourceManager, Executor, Executor)
     */
    private static void onReload(){
        InteractionManager.LOGGER.info("Reloading resources");
        SearchUtil.init(true);
    }

    @Override
    public Identifier getFabricId() {
        return InteractionManager.id("search_utils_resource_reload_listener");
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
        // We don't need to prepare any data
        CompletableFuture<Void> prepData = CompletableFuture.runAsync(() -> {}, prepareExecutor);

        CompletableFuture<Void> applyStart = prepData.thenComposeAsync(synchronizer::whenPrepared);

        return applyStart.thenRunAsync(IMReloadListener::onReload, applyExecutor);
    }
}
