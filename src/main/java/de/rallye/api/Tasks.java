package de.rallye.api;

import de.rallye.annotations.KnownUserAuth;
import de.rallye.auth.RallyePrincipal;
import de.rallye.db.DataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.model.structures.SimpleSubmission;
import de.rallye.model.structures.Submission;
import de.rallye.model.structures.Task;
import de.rallye.model.structures.TaskSubmissions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("rallye/tasks")
public class Tasks {

	public static final int API_VERSION = 4;
	public static final String API_NAME = "ist_rallye";

	private Logger logger =  LogManager.getLogger(Tasks.class);

	@Inject	DataAdapter data;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Task> getTasks() {
		logger.entry();
		
		try {
			List<Task> res = data.getTasks();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getTasks failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("{taskID}")
	@Produces(MediaType.APPLICATION_JSON)
	@KnownUserAuth
	public List<Submission> getSubmissions(@PathParam("taskID") int taskID, @Context SecurityContext sec) {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		try {
			List<Submission> res = data.getSubmissions(taskID, p.getGroupID());
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getSubmissions failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("all/{groupID}")
	@Produces(MediaType.APPLICATION_JSON)
	@KnownUserAuth
	public List<TaskSubmissions> getAllSubmissions(@PathParam("groupID") int groupID, @Context SecurityContext sec) {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
//		if (!p.hasRightsForTaskScoring()) {
//			logger.warn("admin {} has no access rights taskScoring", p.getAdminID());
//			throw new WebApplicationException(Response.Status.FORBIDDEN);
//		}
		p.ensureGroupMatches(groupID);
		
		try {
			List<TaskSubmissions> res = data.getAllSubmissions(groupID);
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getAllSubmissions failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PUT
	@Path("{taskID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@KnownUserAuth
	public Submission submit(SimpleSubmission submission, @PathParam("taskID") int taskID, @Context SecurityContext sec) {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		try {
			Submission res = data.submit(taskID, p.getGroupID(), p.getUserID(), submission);
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("submit failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		} catch (InputException e) {
			logger.error("Not a valid taskID", e);
			throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
		}
	}
}