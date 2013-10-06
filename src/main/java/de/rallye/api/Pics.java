package de.rallye.api;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.rallye.annotations.KnownUserAuth;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.model.structures.Picture;
import de.rallye.model.structures.PictureSize;
import de.rallye.push.PushService;

@Path("rallye/pics")
public class Pics {
	
	private Logger logger =  LogManager.getLogger(Groups.class);

	@Inject	IDataAdapter data;
	@Inject java.util.Map<String, ChatPictureLink> chatPictureMap;
	@Inject	ImageRepository imageRepository;
	@Inject PushService push;
	
	@PUT
	@Path("{hash}")
	@KnownUserAuth
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Picture uploadPictureWithHash(File img, @PathParam("hash") String hash, @Context SecurityContext sec) throws DataException {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		Picture pic = savePicture(img, p);
		if (pic != null) {
			ChatPictureLink.getLink(chatPictureMap, hash, data).setPicture(pic.pictureID, push);
		}
		
		return logger.exit(pic);
	}
	
	@PUT
	@KnownUserAuth
	@Consumes("image/jpeg")
	@Produces(MediaType.APPLICATION_JSON)
	public Picture uploadPicture(File img, @Context SecurityContext sec) throws DataException {
		logger.entry();
		return logger.exit(savePicture(img, (RallyePrincipal) sec.getUserPrincipal()));
	}
	
	private Picture savePicture(File img, RallyePrincipal p) throws DataException {
		logger.entry();
		
		int pictureID;
		
		pictureID = data.assignNewPictureID(p.getUserID());
		imageRepository.put(pictureID, img);
	
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
