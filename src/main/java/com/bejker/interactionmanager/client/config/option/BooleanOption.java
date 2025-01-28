package com.bejker.interactionmanager.client.config.option;

import com.bejker.interactionmanager.util.Util;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

public class BooleanOption implements IOptionConvertable{
    private final String key;
    private final String translation_key;
    private final boolean default_value;
    private final Text enabled_text;
    private final Text disabled_text;
    private final String tooltip_key;
    private static final Text OPTION_ON_TEXT = Text.translatable("option.interactionmanager.on");
    private static final Text OPTION_OFF_TEXT = Text.translatable("option.interactionmanager.off");
    private static final String TRANSLATION_KEY_TYPE = "option";

   public BooleanOption(String key,boolean default_value,String enabled_key,String disabled_key) {
       this.key = key;
       this.translation_key = Util.translationKeyOf(TRANSLATION_KEY_TYPE,key);
       this.default_value = default_value;
       this.enabled_text = Text.translatable(this.translation_key + "." + enabled_key);
       this.disabled_text = Text.translatable(this.translation_key + "." + disabled_key);
       this.tooltip_key = Util.getTooltipTranslationKey(TRANSLATION_KEY_TYPE,key);

       ConfigStorage.setBoolean(key,default_value);
   }

    public BooleanOption(String key, boolean defaultValue) {
        this(key, defaultValue, "true", "false");
    }

    //Default value set to true
    public BooleanOption(String key) {
        this.key = key;
        this.translation_key = Util.translationKeyOf(TRANSLATION_KEY_TYPE,key);
        this.default_value = true;
        this.enabled_text = OPTION_ON_TEXT;
        this.disabled_text = OPTION_OFF_TEXT;
        this.tooltip_key = Util.getTooltipTranslationKey(TRANSLATION_KEY_TYPE,key);

        ConfigStorage.setBoolean(key,default_value);
    }

    public String getKey(){
       return key;
    }
    public boolean getValue(){
       return ConfigStorage.getBoolean(key);
    }

    public void setValue(boolean value){
        ConfigStorage.setBoolean(key,value);
    }

    public void toggleValue(){
        ConfigStorage.toggleBoolean(key);
    }

    public boolean getDefaultValue(){
       return default_value;
    }

    public Text getButtonText(){
       return ScreenTexts.composeGenericOptionText(
           Text.translatable(translation_key),
           getValue() ? enabled_text : disabled_text
       );
    }
    @Override
    public SimpleOption<?> asOption() {
       if(enabled_text == null||disabled_text == null){
           return SimpleOption.ofBoolean(translation_key,getValue(),(value) -> ConfigStorage.setBoolean(key,value));
       }
        return new SimpleOption<>(translation_key,
                Texts.hasTranslation(Text.translatable(tooltip_key)) ?
                        SimpleOption.constantTooltip(Text.translatable(tooltip_key)) : SimpleOption.emptyTooltip(),
                (text,value) -> value ? enabled_text : disabled_text,
                SimpleOption.BOOLEAN,
                getValue(),
                new_value -> ConfigStorage.setBoolean(key,new_value));
    }
}
