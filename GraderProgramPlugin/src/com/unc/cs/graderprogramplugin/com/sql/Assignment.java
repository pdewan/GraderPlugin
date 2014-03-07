package com.unc.cs.graderprogramplugin.com.sql;

/**
 * @author Andrew Vitkus
 *
 */
public @interface Assignment {
	String name() default "";
	int number() default -1;
}
