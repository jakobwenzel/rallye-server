package de.rallye.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import de.rallye.annotations.AdminAuth;
import de.rallye.annotations.KnownUserAuth;
import de.rallye.annotations.NewUserAuth;
import de.rallye.auth.GroupPrincipal;
import de.rallye.auth.RallyePrincipal;
import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.model.structures.Group;
import de.rallye.model.structures.LoginInfo;
import de.rallye.model.structures.PushConfig;
import de.rallye.model.structures.User;
import de.rallye.model.structures.UserAuth;

@Path("rallye/groups")
public class Groups {
	
	private Logger logger =  LogManager.getLogger(Groups.class);

	@Inject	IDataAdapter data;
	@Inject	RallyeConfig config;


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Group> getGroups() throws DataException {
		logger.entry();
	
		List<Group> res = data.getGroups();
		return logger.exit(res);
	}
	
	@GET
	@Path("{groupID}")
	@KnownUserAuth
	@Produces(MediaType.APPLICATION_JSON)
	public List<? extends User> getMembers(@PathParam("groupID") int groupID) throws DataException {
		logger.entry();
	
		List<? extends User> res = data.getMembers(groupID);
		return logger.exit(res);
	}
	
	@GET
	@Path("{groupID}/avatar")
	@Produces("image/jpeg")
	public File getGroupAvatar(@PathParam("groupID") int groupID) {
		File f = new File(config.getDataDirectory()+"game/"+ groupID +"/avatar.jpg");
		
		return f;
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
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserInfo(@PathParam("groupID") int groupID, @PathParam("userID") int userID) {
		throw new UnsupportedOperationException();
	}
	
	@GET
	@Path("{groupID}/{userID}/pushSettings")
	@KnownUserAuth
	@Produces(MediaType.APPLICATION_JSON)
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
	@Produces(MediaType.APPLICATION_JSON)
	public UserAuth login(@PathParam("groupID") int groupID, LoginInfo info, @Context SecurityContext sec) throws DataException, InputException {
		logger.entry();
		GroupPrincipal p = (GroupPrincipal) sec.getUserPrincipal();
		
		int authGroup = p.getGroupID();
		
		p.ensureGroupMatches(groupID);
		
		logger.info("New User: {}", info);
		
		UserAuth login = data.login(authGroup, info);
		return login;
		
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
	//@Produces(MediaType.APPLICATION_JSON)
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
