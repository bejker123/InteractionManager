package com.bejker.interactionmanager.client.gui;

import com.bejker.interactionmanager.InteractionManager;
import com.bejker.interactionmanager.client.InteractionManagerClient;
import com.bejker.interactionmanager.config.InteractionManagerConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.struct.MemberRef;

import java.lang.reflect.Field;
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

        try {
            adder.add(
                    this.createButton("Shovel Paths","ALLOW_SHOVEL_CREATE_PATHS")
            );

            adder.add(
                    this.createButton("Axe Strips","ALLOW_AXE_STRIP_BLOCKS")
            );

            adder.add(
                    this.createButton("Fireworks Work On Blocks","ALLOW_USE_FIREWORK_ON_BLOCK")
            );

        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

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

    private ButtonWidget createButton(String name, String field_name) throws IllegalAccessException, NoSuchFieldException {
        Field field = InteractionManagerConfig.class.getDeclaredField(field_name);
        return ButtonWidget.builder(getBooleanText(name, field.getBoolean(InteractionManagerConfig.getInstance())),(button)->{
            try {
                boolean new_value = !field.getBoolean(InteractionManagerConfig.getInstance());
                field.setBoolean(InteractionManagerConfig.getInstance(),new_value);
                button.setMessage(getBooleanText(name,new_value));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }).build();
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
