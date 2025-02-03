package com.bejker.interactionmanager.config.option;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
// Options annotated with this won't be saved or displayed in the in game config screen.
public @interface IRuntimeInternalOnlyOption {
}
