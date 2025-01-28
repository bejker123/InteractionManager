package com.bejker.interactionmanager.client.config.option;

import net.minecraft.client.option.SimpleOption;

public interface IOptionConvertable {
    SimpleOption<?> asOption();
}
