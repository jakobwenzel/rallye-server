package de.rallye.api;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.jersey.spi.container.ResourceFilters;

import de.rallye.RallyeResources;
import de.rallye.RallyeServer;
import de.rallye.auth.KnownUserAuth;
import de.rallye.auth.RallyePrincipal;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.model.structures.Picture;
import de.rallye.model.structures.PictureSize;

@Path("rallye/pics")
public class Pics {
	
	private Logger logger =  LogManager.getLogger(Groups.class);
	
	private RallyeResources R = RallyeServer.getResources();
	
	@PUT
	@Path("{hash}")
	@ResourceFilters(KnownUserAuth.class)
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Picture uploadPictureWithHash(BufferedImage img, @PathParam("hash") String hash, @Context SecurityContext sec) {
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		Picture pic = savePicture(img, p);
		if (pic != null) {
			ChatPictureLink.getLink(R.hashMap, hash, R.data).setPicture(pic.pictureID);
		}
		
		return pic;
	}
	
	@PUT
	@ResourceFilters(KnownUserAuth.class)
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Picture uploadPicture(BufferedImage img, @Context SecurityContext sec) {//TODO: keep unedited original including EXIF
		return savePicture(img, (RallyePrincipal) sec.getUserPrincipal());
	}
	
	private Picture savePicture(BufferedImage img, RallyePrincipal p) {
		logger.entry();
		
		int pictureID;
		
		try {
			pictureID = R.data.assignNewPictureID(p.getUserID());
			R.imgRepo.put(pictureID, img);
		} catch (DataException e) {
			logger.error("could not assign new pictureID", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	
		return logger.exit(new Picture(pictureID));
	}
	
	@GET
	@Path("{pictureID}")
	@Produces("image/jpeg")
	public BufferedImage getPicture(@PathParam("pictureID") int pictureID) {
		logger.entry();
		
		BufferedImage res = R.imgRepo.get(pictureID, PictureSize.Standard);
		return logger.exit(res);
	}
	
	@GET
	@Path("{pictureID}/{size}")
	@Produces("image/jpeg")
	public BufferedImage getPicture(@PathParam("pictureID") int pictureID, @PathParam("size") PictureSize.PictureSizeString size) {
		logger.entry();
		
		BufferedImage res = R.imgRepo.get(pictureID, size.size);
		return logger.exit(res);
	}
}
