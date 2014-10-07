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
import de.rallye.images.IPictureRepository;
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
import javax.ws.rs.core.Response;
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
	public ImageRepository.Picture uploadPictureWithHash(File img, @PathParam("hash") String hash, @Context SecurityContext sec) throws DataException {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		ImageRepository.Picture pic = savePicture(img, hash, p);
		if (pic != null) {
			ChatPictureLink.getLink(chatPictureMap, hash, data).setPicture(pic);
			SubmissionPictureLink.getLink(submissionPictureMap, hash, data).setPicture(pic);
		}

		logger.debug("Picture {} has hash {}", pic.pictureHash, hash);
		
		return logger.exit(pic);
	}

	@PUT
	@Path("{hash}/preview")
	@KnownUserAuth
	@Consumes("image/jpeg")
	public ImageRepository.Picture uploadPreviewWithHash(File img, @PathParam("hash") String hash, @Context SecurityContext sec) throws DataException {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();

		ImageRepository.Picture picture = imageRepository.putImagePreview(p.getUserID(), hash, img);
		if (picture != null) {
			ChatPictureLink.getLink(chatPictureMap, hash, data).setPicture(picture);
			SubmissionPictureLink.getLink(submissionPictureMap, hash, data).setPicture(picture);
		}

		logger.debug("Picture {} has hash {}", picture.pictureHash, hash);

		return logger.exit(picture);
	}
	
	@PUT
	@KnownUserAuth
	@Consumes("image/jpeg")
	public ImageRepository.Picture uploadPicture(File img, @Context SecurityContext sec) throws DataException {
		logger.entry();
		return logger.exit(savePicture(img, null, (RallyePrincipal) sec.getUserPrincipal()));
	}
	
	private ImageRepository.Picture savePicture(File img, String pictureHash, RallyePrincipal p) throws DataException {
		logger.entry();

		if (pictureHash == null) {
			pictureHash = Integer.toString(img.getAbsolutePath().hashCode());//TODO generate a real hashCode
		}
		ImageRepository.Picture picture = imageRepository.putImage(p.getUserID(), pictureHash, img);
	
		return logger.exit(picture);
	}
	
	@GET
	@Path("{pictureHash}")
	@Produces("image/jpeg")
	public Object getPicture(@PathParam("pictureHash") String pictureHash, @Context Request request) {
		return getPicture(pictureHash, (PictureSize)null, request);
	}
	
	@GET
	@Path("{pictureHash}/{size}")
	@Produces("image/jpeg")
	public Object getPicture(@PathParam("pictureHash") String pictureHash, @PathParam("size") PictureSize.PictureSizeString size, @Context Request request) {
		return getPicture(pictureHash, size.size, request);
	}

	private Object getPicture(String pictureHash, PictureSize size, Request request) {
		logger.entry();

		IPictureRepository.IPicture picture = imageRepository.getImage(pictureHash);

		HttpCacheHandling.checkModifiedSince(request, picture.lastModified());

		Object res;
        if (size==null) {
            res = picture.getUpToStdFile();
        } else {
            res = picture.getCached(size);
            if (res == null) {
                res = picture.getFile(size);
            }
        }

		return logger.exit(res);
	}
}
