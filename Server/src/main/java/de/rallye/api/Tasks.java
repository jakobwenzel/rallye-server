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

import de.rallye.admin.AdminWebsocketApp;
import de.rallye.annotations.AdminAuth;
import de.rallye.annotations.KnownUserAuth;
import de.rallye.annotations.KnownUserOrAdminAuth;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.model.structures.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Arrays;
import java.util.List;

@Path("rallye/tasks")
public class Tasks {

	public static final int API_VERSION = 5;
	public static final String API_NAME = "ist_rallye";

	private static final Logger logger =  LogManager.getLogger(Tasks.class);

	@Inject	IDataAdapter data;
	@Inject RallyeGameState gameState;

	@Inject java.util.Map<String, SubmissionPictureLink> submissionPictureMap;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@KnownUserOrAdminAuth
	public List<Task> getTasks(@Context SecurityContext sec) throws DataException {
		logger.entry();

		//Group id is only passed to getTasks to include ratings
		Integer groupID = null;
		if (sec.getUserPrincipal() instanceof RallyePrincipal) {
			RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
			if (gameState.isShowRatingToUsers())
				groupID = p.getGroupID();
		}
		List<Task> res = data.getTasks(groupID);
		
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
	@KnownUserOrAdminAuth
	public List<TaskSubmissions> getAllSubmissions(@PathParam("groupID") int groupID, @Context SecurityContext sec) throws DataException {
		logger.entry();

		boolean includeRatings = true;
		if (sec.getUserPrincipal() instanceof RallyePrincipal) {
			RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();
			p.ensureGroupMatches(groupID);

			includeRatings = gameState.isShowRatingToUsers();
		} //else we are admin and don't need to check group match.
		
//		if (!p.hasRightsForTaskScoring()) {
//			logger.warn("admin {} has no access rights taskScoring", p.getAdminID());
//			throw new WebApplicationException(Response.Status.FORBIDDEN);
//		}
		
		List<TaskSubmissions> res = data.getAllSubmissions(groupID, includeRatings);
		return logger.exit(res);
	}
	
	@GET
	@Path("byTask/{taskID}")
	@Produces(MediaType.APPLICATION_JSON)
	@AdminAuth
	public List<TaskSubmissions> getAllByTask(@PathParam("taskID") int taskID) throws DataException {
		logger.entry();
		
		List<TaskSubmissions> res = data.getSubmissionsByTask(taskID, true);
		return logger.exit(res);
	}
	
	@GET
	@Path("unrated")
	@Produces(MediaType.APPLICATION_JSON)
	@AdminAuth
	public List<TaskSubmissions> getUnrated() throws DataException {
		logger.entry();
		
		List<TaskSubmissions> res = data.getUnratedSubmissions(true);
		return logger.exit(res);
	}
	
	@PUT
	@Path("{taskID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@KnownUserAuth
	public Submission submit(SimpleSubmissionWithPictureHash submission, @PathParam("taskID") int taskID, @Context SecurityContext sec) throws DataException, InputException {
		logger.entry();

		if (!gameState.isCanSubmit())
			throw new InputException("Submitting disabled.");
		
		RallyePrincipal p = (RallyePrincipal) sec.getUserPrincipal();

		Submission res;

		if (submission.pictureHash!=null) {
			logger.debug("Has Picture hash");
			SubmissionPictureLink link = SubmissionPictureLink.getLink(submissionPictureMap, submission.pictureHash, data);

			Integer picID = link.getPictureID();
			if (picID != null) {
				logger.debug("We have a pic id {}, has hash {} ",picID,submission.pictureHash);
				SimpleSubmission completeSubmission = new SimpleSubmission(submission.submitType,picID,submission.textSubmission);
				res = data.submit(taskID, p.getGroupID(), p.getUserID(), completeSubmission);
			} else {

				res = data.submit(taskID, p.getGroupID(), p.getUserID(), submission);
				link.setObject(res);

				logger.debug("submission {} has hash {}",res.submissionID,submission.pictureHash);

			}
		} else
			res = data.submit(taskID, p.getGroupID(), p.getUserID(), submission);

		AdminWebsocketApp.getInstance().newSubmission(p.getGroupID(),p.getUserID(),taskID,res);
		return logger.exit(res);
	}
	
	@POST
	@Path("score")
	@Consumes(MediaType.APPLICATION_JSON)
	@AdminAuth
	public Response setScores(SubmissionScore[] scores) throws DataException {
		logger.entry();
		
		logger.info("Writing scores: "+Arrays.toString(scores));
		
		data.scoreSubmissions(scores);
		
		return logger.exit(Response.ok().build());
	}
}