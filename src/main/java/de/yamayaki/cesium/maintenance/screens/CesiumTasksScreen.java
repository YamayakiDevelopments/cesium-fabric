package de.yamayaki.cesium.maintenance.screens;

import de.yamayaki.cesium.maintenance.AbstractTask;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Unique;

public class CesiumTasksScreen extends Screen {
    private final LevelStorageSource.LevelStorageAccess levelAccess;
    private final BooleanConsumer callback;

    public CesiumTasksScreen(final LevelStorageSource.LevelStorageAccess levelAccess, final BooleanConsumer callback) {
        super(Component.literal("Cesium Maintenance"));

        this.levelAccess = levelAccess;
        this.callback = callback;
    }

    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderDirtBackground(guiGraphics);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);

        super.render(guiGraphics, i, j, f);
    }

    @Override
    protected void init() {
        var layout = new LinearLayout(this.width / 2 - 100, 38, this.width, 120, LinearLayout.Orientation.VERTICAL);

        layout.addChild(this.taskButton("Anvil → Cesium", AbstractTask.Task.TO_CESIUM));
        layout.addChild(this.taskButton("Cesium → Anvil", AbstractTask.Task.TO_ANVIL));
        layout.addChild(this.taskButton("Compact Database", AbstractTask.Task.COMPACT));

        layout.addChild(new SpacerElement(200, 20));

        layout.addChild(Button.builder(CommonComponents.GUI_CANCEL, (buttonx) -> this.onClose()).width(200).build());

        layout.visitWidgets(this::addRenderableWidget);

        layout.arrangeElements();
    }

    @Unique
    private Button taskButton(final String text, final AbstractTask.Task task) {
        return Button.builder(
                Component.literal(text),
                action -> this.minecraft.setScreen(
                        new CesiumWorkScreen(task, this.levelAccess, this.createRegistry(), this.callback)
                )
        ).width(200).build();
    }

    /*
     * See vanilla code net.minecraft.client.gui.screens.worldselection.OptimizeWorldScreen.create(...);
     */
    @Unique
    private RegistryAccess.Frozen createRegistry() {
        assert this.minecraft != null;

        try {
            final var worldOpenFlows = this.minecraft.createWorldOpenFlows();

            try (final WorldStem worldStem = worldOpenFlows.loadWorldStem(levelAccess, false)) {
                final var worldData = worldStem.worldData();
                final var frozen = worldStem.registries().compositeAccess();

                this.levelAccess.saveDataTag(frozen, worldData);

                return frozen;
            }
        } catch (final Throwable t) {
            throw new RuntimeException("Failed to load datapacks, can't convert world", t);
        }
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }
}
