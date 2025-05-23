package me.icodetits.customCrates.commands.manager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CrateCommandInfo {

	String description();

	String usage();

	String[] aliases();

	String permission();
}