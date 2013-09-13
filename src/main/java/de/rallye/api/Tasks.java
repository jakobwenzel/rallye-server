package de.rallye.api;

import java.util.List;

import javax.inject.Inject;
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

import de.rallye.annotations.KnownUserAuth;
import de.rallye.auth.RallyePrincipal;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.model.structures.SimpleSubmission;
import de.rallye.model.structures.Submission;
import de.rallye.model.structures.Task;
import de.rallye.model.structures.TaskSubmissions;

@Path("rallye/tasks")
public class Tasks {

	public static final int API_VERSION = 4;
	public static final String API_NAME = "ist_rallye";

	private Logger logger =  LogManager.getLogger(Tasks.class);

	@Inject	IDataAdapter data;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Task> getTasks() throws DataException {
		logger.entry();
	
		List<Task> res = data.getTasks();
		return logger.exit(res);
	}
	
	@GET
	@Path("{taskID}")
	@Produces(MediaType.APPLICATION_JSON)
	@KnownUserAuth
	public List<Submission> getSubmissions(@PathParam("taskID") int taskID, @Context SecurityContext sec) throws DataException {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		List<Submission> res = data.getSubmissions(taskID, p.getGroupID());
		return logger.exit(res);
	}
	
	@GET
	@Path("all/{groupID}")
	@Produces(MediaType.APPLICATION_JSON)
	//TODO Enable auth again!
	//@KnownUserAuth
	public List<TaskSubmissions> getAllSubmissions(@PathParam("groupID") int groupID, @Context SecurityContext sec) throws DataException {
		logger.entry();
		
		//RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
//		if (!p.hasRightsForTaskScoring()) {
//			logger.warn("admin {} has no access rights taskScoring", p.getAdminID());
//			throw new WebApplicationException(Response.Status.FORBIDDEN);
//		}
		//p.ensureGroupMatches(groupID);
		
		List<TaskSubmissions> res = data.getAllSubmissions(groupID);
		return logger.exit(res);
	}
	
	@PUT
	@Path("{taskID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@KnownUserAuth
	public Submission submit(SimpleSubmission submission, @PathParam("taskID") int taskID, @Context SecurityContext sec) throws DataException, InputException {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		Submission res = data.submit(taskID, p.getGroupID(), p.getUserID(), submission);
		return logger.exit(res);
	}
}