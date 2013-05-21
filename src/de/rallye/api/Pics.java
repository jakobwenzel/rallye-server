package de.rallye.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("pics")
public class Pics {
	
	@PUT
	@Path("{hash}")
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadPictureWithHash(byte[] pic, @PathParam("hash") String hash) {
		return null;
	}
	
	@PUT
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadPicture(byte[] pic) {
		return null;
	}
	
	@GET
	@Path("{pictureID}/{size}")
	@Produces("image/jpeg")
	public Response getPicture(@PathParam("pictureID") int pictureID, @PathParam("size") char size) {
		return null;
	}
}
