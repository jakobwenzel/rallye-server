package de.rallye.api;

import javax.ws.rs.Path;

import com.sun.jersey.spi.container.ResourceFilters;

import de.rallye.auth.AuthFilter;

@ResourceFilters(AuthFilter.class)
@Path("/chats")
public class Chats {

}
