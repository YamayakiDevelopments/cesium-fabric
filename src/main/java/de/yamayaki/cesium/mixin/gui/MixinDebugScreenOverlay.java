package de.yamayaki.cesium.mixin.gui;

import de.yamayaki.cesium.CesiumMod;
import de.yamayaki.cesium.accessor.DatabaseSource;
import de.yamayaki.cesium.common.db.LMDBInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.lmdbjava.Stat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class MixinDebugScreenOverlay {
    @Inject(method = "getSystemInformation", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void cesium$addDebugInfo(CallbackInfoReturnable<List<String>> cir, long l, long m, long n, long o, List<String> list) {
        if(!CesiumMod.config().getClient().showDebug()) {
            return;
        }

        final MinecraftServer minecraftServer = Minecraft.getInstance().getSingleplayerServer();
        if (minecraftServer == null || Minecraft.getInstance().level == null) {
            return;
        }

        final ResourceKey<Level> playerLevel = Minecraft
                .getInstance().level
                .dimension();

        final ServerLevel serverLevel = minecraftServer.getLevel(playerLevel);

        if (serverLevel == null) {
            return;
        }

        final LMDBInstance lmdbInstance = ((DatabaseSource) serverLevel)
                .cesium$getStorage();
        final List<Stat> stats = lmdbInstance.getStats();

        final int ms_depth = stats.stream().mapToInt(es -> es.depth).max().orElse(0);
        final long ms_branch_pages = stats.stream().mapToLong(es -> es.branchPages).sum();
        final long ms_leaf_pages = stats.stream().mapToLong(es -> es.leafPages).sum();
        final long ms_entries = stats.stream().mapToLong(es -> es.entries).sum();

        list.add("");
        list.add("Cesium level stats");

        list.add("ms_depth: " + ms_depth);
        list.add("ms_branch_pages: " + ms_branch_pages);
        list.add("ms_leaf_pages: " + ms_leaf_pages);
        list.add("ms_entries: " + ms_entries);
    }

}
