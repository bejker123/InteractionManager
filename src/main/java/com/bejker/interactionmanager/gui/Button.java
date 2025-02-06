package com.bejker.interactionmanager.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class Button extends ButtonWidget {

    private final static int DEFAULT_WIDTH = 150;
    private final static int DEFAULT_HEIGHT = 20;

    private final static Text DEFAULT_MESSAGE = Text.literal("btn");

    protected Button(int x, int y, int width, int height, Text message, PressAction onPress, ButtonWidget.NarrationSupplier narrationSupplier) {
        super(x, y, width, height, message,onPress,narrationSupplier);
    }

    public Button(int x, int y, int width, int height,PressAction onPress) {
        super(x, y, width, height, DEFAULT_MESSAGE,onPress,DEFAULT_NARRATION_SUPPLIER);
    }

    public Button(PressAction onPress) {
        this(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT,onPress);
    }

    private int getBackgroundColor(){
        if(this.isHovered()){
            return 0xA0_10_10_10;
        }

        return 0xF0_10_10_10;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        context.fill(this.getX(),this.getY(),this.getX() + this.getWidth(),this.getY() + this.getHeight(), getBackgroundColor());
        context.drawBorder(this.getX(),this.getY(),this.getWidth(),this.getHeight(),0x2F_01_01_01);
        int i = this.active ? 16777215 : 10526880;
        this.drawMessage(context, minecraftClient.textRenderer, i | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }
}
