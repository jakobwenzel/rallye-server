package de.rallye.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import de.rallye.control.GameHandler;
import de.rallye.db.DataAdapter;

@Path("rallye/pics")
public class Pics {
	
	private Logger logger =  LogManager.getLogger(Groups.class);

	private DataAdapter data = GameHandler.data;//TODO: get it _NOT_ from gameHandler (perhaps inject using Guice??)
	
	@PUT
	@Path("{hash}")
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadPictureWithHash(byte[] pic, @PathParam("hash") String hash) {
		throw new NotImplementedException();//TODO
	}
	
	@PUT
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject uploadPicture(byte[] pic) {
		throw new NotImplementedException();//TODO
	}
	
	@GET
	@Path("{pictureID}/{size}")
	@Produces("image/jpeg")
	public Response getPicture(@PathParam("pictureID") int pictureID, @PathParam("size") String size) {
		return returnPic(pictureID, size.charAt(0));
	}
	
	@Deprecated
	private Response returnPic(int picID, char size) {
		Response r = null;
		logger.entry();
		byte[] blob = GameHandler.blobStore.get(getImageName(picID, size));
		r = Response.ok().entity(blob).type("image/jpeg").build();
		if (blob == null) {
			r = Response.status(400)
					.entity("picID not found or not available in this size")
					.build();
		}
		return logger.exit(r);
	}

	/**
	 * this method creates the name for the blob entries
	 * 
	 * @param picID
	 *            running number of the picture from database
	 * @param size
	 *            't','s','l' to add at the end
	 * @return a String with the created name
	 * @author Felix H�bner
	 */
	private String getImageName(int picID, char size) {
		return String.format("%08d_%c", picID, size);
	}
}
