package com.bejker.interactionmanager.config.option;

import net.minecraft.client.option.SimpleOption;

public interface IOptionConvertable {
    SimpleOption<?> asOption();
}
