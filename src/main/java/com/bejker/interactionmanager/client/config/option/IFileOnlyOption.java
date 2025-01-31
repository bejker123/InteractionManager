package com.bejker.interactionmanager.client.config.option;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
// Config option annotated with this won't be displayed in the in game config screen
public @interface IFileOnlyOption {
}
