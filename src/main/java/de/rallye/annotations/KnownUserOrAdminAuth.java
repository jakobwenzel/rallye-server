package de.rallye.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.ws.rs.NameBinding;

/**
 * Forces KownUserOrAdminAuthFilter.class to be applied to Request
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface KnownUserOrAdminAuth {

}
