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

import de.rallye.annotations.AdminAuth;
import de.rallye.annotations.KnownUserAuth;
import de.rallye.annotations.NewUserAuth;
import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.filter.auth.GroupPrincipal;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.model.structures.*;
import de.rallye.util.HttpCacheHandling;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("groups")
@Produces({"application/x-jackson-smile;qs=0.8", "application/xml;qs=0.9", "application/json;qs=1"})
public class Groups {
	
	private static final Logger logger =  LogManager.getLogger(Groups.class);

	@Inject	IDataAdapter data;
	@Inject	RallyeConfig config;


	@GET
	public List<Group> getGroups() throws DataException {
		logger.entry();
	
		List<Group> res = data.getGroups(false);
		return logger.exit(res);
	}

	@GET
	@AdminAuth
	@Path("admin")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Group> getGroupsAdmin() throws DataException {
		logger.entry();

		List<Group> res = data.getGroups(true);
		return logger.exit(res);
	}

	@GET
	@Path("{groupID}")
	@KnownUserAuth
	public List<? extends User> getMembers(@PathParam("groupID") int groupID) throws DataException {
		logger.entry();
	
		List<? extends User> res = data.getMembers(groupID);
		return logger.exit(res);
	}
	
	@GET
	@Path("{groupID}/avatar")
	@Produces("image/jpeg")
	public File getGroupAvatar(@PathParam("groupID") int groupID, @Context Request request) {
		File avatar = new File(config.getDataDirectory()+"game/"+ groupID +"/avatar.jpg");

		HttpCacheHandling.checkModifiedSince(request, avatar.lastModified());

		return avatar;
	}
	

	@POST
	@Path("{groupID}/avatar")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
	@AdminAuth
	public Response setGroupAvatar(@PathParam("groupID") int groupID, @FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws DataException{
		logger.entry();
		File f = new File(config.getDataDirectory()+"game/"+ groupID +"/avatar.jpg");
		
		try {
			//TODO: Resize image
			org.apache.commons.io.FileUtils.copyInputStreamToFile(uploadedInputStream, f);
		} catch (IOException e) {
			logger.error(e);
			throw new DataException(e);
		}
		
		return logger.exit(Response.ok().build());
	}
	
	@DELETE
	@Path("{groupID}/avatar")
	@AdminAuth
	public Response deleteGroupAvatar(@PathParam("groupID") int groupID) throws DataException{
		logger.entry();
		File f = new File(config.getDataDirectory()+"game/"+ groupID +"/avatar.jpg");
		
		f.delete();
		
		return logger.exit(Response.ok().build());
	}
	
	
	@GET
	@Path("{groupID}/{userID}")
	@KnownUserAuth
	public Response getUserInfo(@PathParam("groupID") int groupID, @PathParam("userID") int userID) {
		throw new UnsupportedOperationException();
	}
	
	@GET
	@Path("{groupID}/{userID}/pushSettings")
	@KnownUserAuth
	public PushConfig getPushSettings(@PathParam("groupID") int groupID, @PathParam("userID") int userID, @Context SecurityContext sec) throws DataException {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		p.ensureBothMatch(userID, groupID);
		
		PushConfig res = data.getPushConfig(groupID, userID);
		return logger.exit(res);
	}
	
	@POST
	@Path("{groupID}/{userID}/pushSettings")
	@KnownUserAuth
	@Consumes(MediaType.APPLICATION_JSON)
	public Response setPushConfig(@PathParam("groupID") int groupID, @PathParam("userID") int userID, PushConfig push, @Context SecurityContext sec) throws DataException {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		p.ensureBothMatch(userID, groupID);
		
		data.setPushConfig(groupID, userID, push);
		return Response.ok().build();
	}
	
	@PUT
	@Path("{groupID}")
	@NewUserAuth
	@Consumes(MediaType.APPLICATION_JSON)
	public UserAuth login(@PathParam("groupID") int groupID, LoginInfo info, @Context SecurityContext sec) throws DataException, InputException {
		logger.entry();
		GroupPrincipal p = (GroupPrincipal) sec.getUserPrincipal();
		
		int authGroup = p.getGroupID();
		
		p.ensureGroupMatches(groupID);
		
		logger.info("New User: {}", info);
		
		return data.login(authGroup, info);
	}
	
	@DELETE
	@Path("{groupID}/{userID}")
	@KnownUserAuth
	public Response logout(@PathParam("groupID") int groupID, @PathParam("userID") int userID, @Context SecurityContext sec) throws DataException {
		logger.entry();
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		p.ensureBothMatch(userID, groupID);
	
		data.logout(groupID, userID);
		return Response.ok().build();
	}
	

	@PUT
	@AdminAuth
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("text/plan")
	public int addGroup(Group group, @Context SecurityContext sec) throws DataException {
		logger.entry();

		int id = data.addGroup(group);
		
		return logger.exit(id);
		
	}
	

	@POST
	@Path("{groupID}")
	@AdminAuth
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editGroup(@PathParam("groupID") int groupID, Group group, @Context SecurityContext sec) throws DataException, InputException {
		
		logger.entry();
		
		//We don't expect the id in the json data but in the url, so change the 
		if (group.groupID!=groupID) {
			throw new InputException("Group ID in json data and url must be equal.");
		}

		data.editGroup(group);
		
		return logger.exit(Response.ok().build());
		
	}
	
	
	
	
}
