package de.rallye.api;

import de.rallye.annotations.KnownUserAuth;
import de.rallye.auth.RallyePrincipal;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.model.structures.Picture;
import de.rallye.model.structures.PictureSize;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.awt.image.BufferedImage;
import java.io.File;

@Path("rallye/pics")
public class Pics {
	
	private Logger logger =  LogManager.getLogger(Groups.class);

	@Inject	DataAdapter data;
	@Inject java.util.Map<String, ChatPictureLink> chatPictureMap;
	@Inject	ImageRepository imageRepository;
	
	@PUT
	@Path("{hash}")
	@KnownUserAuth
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Picture uploadPictureWithHash(File img, @PathParam("hash") String hash, @Context SecurityContext sec) {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		Picture pic = savePicture(img, p);
		if (pic != null) {
			ChatPictureLink.getLink(chatPictureMap, hash, data).setPicture(pic.pictureID);
		}
		
		return logger.exit(pic);
	}
	
	@PUT
	@KnownUserAuth
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Picture uploadPicture(File img, @Context SecurityContext sec) {
		logger.entry();
		return logger.exit(savePicture(img, (RallyePrincipal) sec.getUserPrincipal()));
	}
	
	private Picture savePicture(File img, RallyePrincipal p) {
		logger.entry();
		
		int pictureID;
		
		try {
			pictureID = data.assignNewPictureID(p.getUserID());
			imageRepository.put(pictureID, img);
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
		
		BufferedImage res = imageRepository.get(pictureID, PictureSize.Standard);
		return logger.exit(res);
	}
	
	@GET
	@Path("{pictureID}/{size}")
	@Produces("image/jpeg")
	public BufferedImage getPicture(@PathParam("pictureID") int pictureID, @PathParam("size") PictureSize.PictureSizeString size) {
		logger.entry();
		
		BufferedImage res = imageRepository.get(pictureID, size.size);
		return logger.exit(res);
	}
}
