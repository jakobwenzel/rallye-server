package de.rallye.annotations;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Forces NewUserAuthFilter.class to be applied to Request
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface NewUserAuth {
}
