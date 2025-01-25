package com.bejker.interactionmanager.client.gui;

import com.bejker.interactionmanager.config.InteractionManagerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class InteractionManagerOptionsScreen extends Screen {
    private final Screen parent;
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this, 61, 33);
    public InteractionManagerOptionsScreen(Text title,Screen parent) {
        super(title);
        this.parent = parent;
    }

    private static Text getBooleanText(String name,boolean bool){
        if(bool) {
            name += ": ON";
        }else{
            name += ": OFF";
        }
        return Text.of(name);
    }

    @Override
    public void init(){
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().marginX(4).marginBottom(4).alignHorizontalCenter().alignVerticalCenter();
        GridWidget.Adder adder = gridWidget.createAdder(2);

        adder.add(ButtonWidget.builder(getBooleanText("Shovel Paths",InteractionManagerConfig.getInstance().ALLOW_SHOVEL_CREATE_PATHS),(button)->{
            InteractionManagerConfig.getInstance().ALLOW_SHOVEL_CREATE_PATHS = !InteractionManagerConfig.getInstance().ALLOW_SHOVEL_CREATE_PATHS;
            button.setMessage(getBooleanText("Shovel Paths",InteractionManagerConfig.getInstance().ALLOW_SHOVEL_CREATE_PATHS));
        }).build());

        adder.add(ButtonWidget.builder(getBooleanText("Axe Strips",InteractionManagerConfig.getInstance().ALLOW_AXE_STRIP_BLOCKS),(button)->{
            InteractionManagerConfig.getInstance().ALLOW_AXE_STRIP_BLOCKS = !InteractionManagerConfig.getInstance().ALLOW_AXE_STRIP_BLOCKS;
            button.setMessage(getBooleanText("Shovel Path",InteractionManagerConfig.getInstance().ALLOW_AXE_STRIP_BLOCKS));
        }).build());

        this.layout.addHeader(this.title, MinecraftClient.getInstance().textRenderer);
        this.layout.addBody(gridWidget);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.close();
        }).width(200).build());
        this.layout.forEachChild(this::addDrawableChild);
        this.refreshWidgetPositions();
    }

    @Override
    protected void refreshWidgetPositions() {
        this.layout.refreshPositions();
    }

    private ButtonWidget createGoToScreenButton(Text text, Supplier<Screen> screenSupplier){
        return ButtonWidget.builder(text, (button) -> {
            this.client.setScreen((Screen)screenSupplier.get());
        }).width(98).build();
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void removed() {
        InteractionManagerConfig.getInstance().saveConfig();
    }
}
