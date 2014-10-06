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

package de.rallye.db;

import com.drew.metadata.Metadata;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;
import de.rallye.filter.auth.AdminPrincipal;
import de.rallye.filter.auth.GroupPrincipal;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.images.ImageRepository;
import de.rallye.model.structures.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IDataAdapter {

	long getNodesLastModified();

	long getEdgesLastModified();

	void purgeCache();

	public abstract List<Group> getGroups(boolean includePasswords) throws DataException;
	
	/**
	 * Add a new group
	 * @param group The group to be added. The included groupID is ignored.
	 * @return ID of the newly created group.
	 * @throws DataException
	 */
	public abstract int addGroup(Group group) throws DataException;

	/**
	 * Edit a group
	 * @param group The group to be edited. It is identified by the ID.
	 * @throws DataException
	 */
	public abstract void editGroup(Group group) throws DataException;

	/**
	 * Get all tasks. If groupID is not null, include the ratings of the specified group
	 */
	public abstract List<Task> getTasks(Integer groupID) throws DataException;

	public abstract List<Submission> getSubmissions(int taskID, int groupID)
			throws DataException;

	/* (non-Javadoc)
	 * @see de.rallye.db.IDataAdapter#getAllSubmissions(int)
	 */
	List<TaskSubmissions> getAllSubmissions(int groupID, boolean includeRating) throws DataException;

	List<TaskSubmissions> getSubmissionsByTask(int taskID, boolean includeRating)
			throws DataException;

	List<TaskSubmissions> getUnratedSubmissions(boolean includeRating) throws DataException;

	public abstract Submission submit(int taskID, int groupID, int userID,
									  PostSubmission submission, ImageRepository.Picture picture) throws DataException, InputException;

	public abstract Map<Integer, Node> getNodes();

	public abstract List<Edge> getEdges();

	public abstract RallyePrincipal getKnownUserAuthorization(int groupID,
			int userID, String password) throws DataException,
			UnauthorizedException, InputException;

	public abstract GroupPrincipal getNewUserAuthorization(int groupID,
			String password) throws DataException, UnauthorizedException;

	public abstract UserAuth login(int groupID, LoginInfo info)
			throws DataException, InputException;

	public abstract boolean logout(int groupID, int userID)
			throws DataException;

	public abstract boolean hasRightsForChatroom(int groupID, int roomID)
			throws DataException;

	public abstract List<Chatroom> getChatrooms(int groupID)
			throws DataException;

	public abstract List<ChatEntry> getChats(int roomID, long timestamp,
			int groupID) throws DataException, UnauthorizedException;

	public abstract ChatEntry addChat(PostChat chat, ImageRepository.Picture picture, int roomID,
									  int groupID, int userID) throws DataException;

	public abstract void setPushConfig(int groupID, int userID, PushConfig push)
			throws DataException;

	public abstract PushConfig getPushConfig(int groupID, int userID)
			throws DataException;

	public abstract List<PushMode> getPushModes() throws DataException;

	public abstract List<GroupUser> getAllUsers() throws DataException;

	public abstract List<UserInternal> getMembers(int groupID)
			throws DataException;

	public abstract List<UserInternal> getChatroomMembers(int roomID)
			throws DataException;

	public abstract void editChatAddPicture(int chatID, int pictureID)
			throws DataException;

	public abstract void updatePushIds(HashMap<String, String> changes)
			throws DataException;

	public abstract void scoreSubmissions(SubmissionScore[] scores) throws DataException;

	public abstract RallyeGameState loadGameState() throws DataException;

	public abstract void saveGameState(RallyeGameState gameState) throws DataException;

	public abstract AdminPrincipal getAdminPrincipal(String username, String password) throws DataException, InputException;

	void editSubmissionAddPicture(int submissionID, int pictureID)  throws DataException;

	long getTasksLastModified();

	int addPicture(int userID, String pictureHash, Metadata meta) throws DataException;
}