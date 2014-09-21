/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallySoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.api;

import de.rallye.annotations.KnownUserAuth;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.ChatPictureLink;
import de.rallye.model.structures.Picture;
import de.rallye.model.structures.PictureSize;
import de.rallye.model.structures.SubmissionPictureLink;
import de.rallye.push.PushService;
import de.rallye.util.HttpCacheHandling;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import java.awt.image.BufferedImage;
import java.io.File;

@Path("resources/pics")
@Produces({"application/x-jackson-smile;qs=0.8", "application/xml;qs=0.9", "application/json;qs=1"})
public class Pics {
	
	private static final Logger logger =  LogManager.getLogger(Groups.class);

	@Inject	IDataAdapter data;
	@Inject java.util.Map<String, ChatPictureLink> chatPictureMap;
	@Inject java.util.Map<String, SubmissionPictureLink> submissionPictureMap;
	@Inject	ImageRepository imageRepository;
	@Inject PushService push;
	
	@PUT
	@Path("{hash}")
	@KnownUserAuth
	@Consumes("image/jpeg")
	public Picture uploadPictureWithHash(File img, @PathParam("hash") String hash, @Context SecurityContext sec) throws DataException {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		Picture pic = savePicture(img, p);
		if (pic != null) {
			ChatPictureLink.getLink(chatPictureMap, hash, data).setPicture(pic.pictureID);
			SubmissionPictureLink.getLink(submissionPictureMap, hash, data).setPicture(pic.pictureID);
		}

		logger.debug("Picture {} has hash {}", pic.pictureID, hash);
		
		return logger.exit(pic);
	}
	
	@PUT
	@KnownUserAuth
	@Consumes("image/jpeg")
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
	public BufferedImage getPicture(@PathParam("pictureID") int pictureID, @Context Request request) {
		logger.entry();

		HttpCacheHandling.checkModifiedSince(request, imageRepository.getLastModified(pictureID));
		
		BufferedImage res = imageRepository.get(pictureID, PictureSize.Standard);
		return logger.exit(res);
	}
	
	@GET
	@Path("{pictureID}/{size}")
	@Produces("image/jpeg")
	public BufferedImage getPicture(@PathParam("pictureID") int pictureID, @PathParam("size") PictureSize.PictureSizeString size, @Context Request request) {
		logger.entry();

		HttpCacheHandling.checkModifiedSince(request, imageRepository.getLastModified(pictureID));
		
		BufferedImage res = imageRepository.get(pictureID, size.size);
		return logger.exit(res);
	}
}
