package com.bejker.interactionmanager.config.option.interfaces;

import net.minecraft.client.option.SimpleOption;

public interface IOptionConvertable {
    SimpleOption<?> asOption();
}
