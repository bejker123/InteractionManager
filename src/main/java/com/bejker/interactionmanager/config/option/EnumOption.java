package com.bejker.interactionmanager.config.option;

import com.bejker.interactionmanager.util.Util;
import com.mojang.serialization.Codec;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.util.Arrays;
import java.util.Locale;

public class EnumOption<E extends Enum<E>> implements IOptionConvertable{

    private final String key;
    private final String translation_key;
    private final String tooltip_key;

    private final Class<E> enum_class;
    private final E default_value;
    private static final String TRANSLATION_KEY_TYPE = "option";
    public EnumOption(String key,E default_value){
        this.key = key;
        this.translation_key = Util.translationKeyOf(TRANSLATION_KEY_TYPE,key);
        this.tooltip_key = Util.getTooltipTranslationKey(TRANSLATION_KEY_TYPE,key);
        this.enum_class = default_value.getDeclaringClass();
        this.default_value = default_value;

        setValue(default_value);
    }
    public String getKey(){
        return key;
    }
    public E getValue(){
        return OptionStorage.getEnum(key,enum_class);
    }

    public void setValue(E value){
        OptionStorage.setEnum(key,value);
    }

    public void cycleValue(int amount){
        OptionStorage.cycleEnumValue(key,enum_class,amount);
    }

    public void nextValue(){
        cycleValue(1);
    }

    public E getDefaultValue(){
        return default_value;
    }
    private static <E extends Enum<E>> Text getValueText(EnumOption<E> option, E value) {
        return Text.translatable(option.translation_key + "." + value.name().toLowerCase(Locale.ROOT));
    }

    public Text getButtonText() {
        return ScreenTexts.composeGenericOptionText(Text.translatable(translation_key), getValueText(this, getValue()));
    }
    @Override
    public SimpleOption<?> asOption() {
        return new SimpleOption<>(translation_key,
                    Texts.hasTranslation(Text.translatable(tooltip_key)) ?
                    SimpleOption.constantTooltip(Text.translatable(tooltip_key)) : SimpleOption.emptyTooltip(),
                    (text,value) -> getValueText(this,value),
                    new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(enum_class.getEnumConstants()),
                            Codec.STRING.xmap(string -> Arrays.stream(enum_class.getEnumConstants())
                                    .filter(e -> e.name().toLowerCase().equals(string))
                                    .findAny()
                                    .orElse(null), newValue -> newValue.name().toLowerCase())
                    ),
                    getValue(),
                    new_value -> OptionStorage.setEnum(key,new_value)
                );
    }

}
