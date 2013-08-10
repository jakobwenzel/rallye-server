package de.rallye.api;

import java.util.List;

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
import de.rallye.auth.AdminAuth;
import de.rallye.auth.AdminPrincipal;
import de.rallye.auth.KnownUserAuth;
import de.rallye.auth.RallyePrincipal;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.model.structures.SimpleSubmission;
import de.rallye.model.structures.Submission;
import de.rallye.model.structures.Task;
import de.rallye.model.structures.TaskSubmissions;

@Path("rallye/tasks")
public class Tasks {
	
	private Logger logger =  LogManager.getLogger(Tasks.class);

	private RallyeResources R = RallyeServer.getResources();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Task> getTasks() {
		logger.entry();
		
		try {
			List<Task> res = R.data.getTasks();
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getTasks failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("{taskID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ResourceFilters(KnownUserAuth.class)
	public List<Submission> getSubmissions(@PathParam("taskID") int taskID, @Context SecurityContext sec) {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		try {
			List<Submission> res = R.data.getSubmissions(taskID, p.getGroupID());
			return logger.exit(res);
		} catch (DataException e) {
			logger.error("getSubmissions failed", e);
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("all/{groupID}")
	@Produces(MediaType.APPLICATION_JSON)
	@ResourceFilters(AdminAuth.class)
	public List<TaskSubmissions> getAllSubmissions(@PathParam("groupID") int groupID, @Context SecurityContext sec) {
		logger.entry();
		
		AdminPrincipal p = (AdminPrincipal) sec.getUserPrincipal();
		
		if (!p.hasRightsForTaskScoring()) {
			logger.warn("admin {} has no access rights taskScoring", p.getAdminID());
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
		
		try {
			List<TaskSubmissions> res = R.data.getAllSubmissions(groupID);
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
	@ResourceFilters(KnownUserAuth.class)
	public Submission submit(SimpleSubmission submission, @PathParam("taskID") int taskID, @Context SecurityContext sec) {
		logger.entry();
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
		
		try {
			Submission res = R.data.submit(taskID, p.getGroupID(), p.getUserID(), submission);
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