package org.allurefw.report;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 26.02.16
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Plugin {

    String name();

    PluginScope scope() default PluginScope.PROCESS;

    long priority() default 0;

}
