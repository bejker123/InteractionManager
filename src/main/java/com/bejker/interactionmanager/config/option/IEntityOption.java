package com.bejker.interactionmanager.config.option;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
// Options annotated with this will only be displayed in the entity options screen
public @interface IEntityOption {
}
