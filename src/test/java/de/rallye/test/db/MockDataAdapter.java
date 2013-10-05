package de.rallye.test.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.exceptions.InputException;
import de.rallye.exceptions.UnauthorizedException;
import de.rallye.filter.auth.GroupPrincipal;
import de.rallye.filter.auth.RallyePrincipal;
import de.rallye.model.structures.ChatEntry;
import de.rallye.model.structures.Chatroom;
import de.rallye.model.structures.Edge;
import de.rallye.model.structures.Group;
import de.rallye.model.structures.GroupUser;
import de.rallye.model.structures.LoginInfo;
import de.rallye.model.structures.Node;
import de.rallye.model.structures.PushConfig;
import de.rallye.model.structures.PushMode;
import de.rallye.model.structures.RallyeGameState;
import de.rallye.model.structures.SimpleChatEntry;
import de.rallye.model.structures.SimpleSubmission;
import de.rallye.model.structures.Submission;
import de.rallye.model.structures.SubmissionScore;
import de.rallye.model.structures.Task;
import de.rallye.model.structures.TaskSubmissions;
import de.rallye.model.structures.UserAuth;
import de.rallye.model.structures.UserInternal;

public class MockDataAdapter implements IDataAdapter {

	private MockDataAdapter() {
	}
	private static MockDataAdapter instance;
	public static MockDataAdapter getInstance() {
		if (instance==null) instance = new MockDataAdapter();
		return instance;
	}

	@Override
	public List<Group> getGroups() throws DataException {
		List<Group> result = new ArrayList<Group>();
		result.add(new Group(1, "Gruppe 1", "Beschreibung Gruppe 1"));
		result.add(new Group(2, "Gruppe 2", "Beschreibung Gruppe 2"));
		return result;
	}

	@Override
	public List<Task> getTasks(Integer groupID) throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public List<Submission> getSubmissions(int taskID, int groupID)
			throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public List<TaskSubmissions> getAllSubmissions(int groupID, boolean includeRatings)
			throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public Submission submit(int taskID, int groupID, int userID,
			SimpleSubmission submission) throws DataException, InputException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public Map<Integer, Node> getNodes() {
		fail("Method not implemented");
		return null;
	}

	@Override
	public List<Edge> getEdges() {
		fail("Method not implemented");
		return null;
	}

	@Override
	public RallyePrincipal getKnownUserAuthorization(int groupID, int userID,
			String password) throws DataException, UnauthorizedException,
			InputException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public GroupPrincipal getNewUserAuthorization(int groupID, String password)
			throws DataException, UnauthorizedException {
		if (groupID==1 && password.equals("test")) {
			return new GroupPrincipal(groupID, "test");
		} else throw new UnauthorizedException();
	}

	public static String validLogin = "{\"pushID\":\"42\",\"uniqueID\":\"e858af70351adc2f\",\"pushMode\":\"gcm\",\"name\":\"test\"}"; 
	@Override
	public UserAuth login(int groupID, LoginInfo info) throws DataException,
			InputException {
		ObjectMapper mapper = new ObjectMapper();
		LoginInfo ref;
		try {
			ref = mapper.readValue(validLogin, LoginInfo.class);
			assertEquals("Login info should match",ref,info);
			
			
		} catch (JsonParseException e) {
			e.printStackTrace();
			fail("Exception: "+e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			fail("Exception: "+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Exception: "+e);
		}
		return new UserAuth(1337, "133742");
	}

	@Override
	public boolean logout(int groupID, int userID) throws DataException {
		fail("Method not implemented");
		return false;
	}

	@Override
	public boolean hasRightsForChatroom(int groupID, int roomID)
			throws DataException {
		fail("Method not implemented");
		return false;
	}

	@Override
	public List<Chatroom> getChatrooms(int groupID) throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public List<ChatEntry> getChats(int roomID, long timestamp, int groupID)
			throws DataException, UnauthorizedException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public ChatEntry addChat(SimpleChatEntry chat, int roomID, int groupID,
			int userID) throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public void setPushConfig(int groupID, int userID, PushConfig push)
			throws DataException {
		fail("Method not implemented");

	}

	@Override
	public PushConfig getPushConfig(int groupID, int userID)
			throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public List<PushMode> getPushModes() throws DataException {
		fail("Method not implemented");
		return new ArrayList<PushMode>();
	}

	@Override
	public List<GroupUser> getAllUsers() throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public List<UserInternal> getMembers(int groupID) throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public List<UserInternal> getChatroomMembers(int roomID)
			throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public int assignNewPictureID(int userID) throws DataException {
		fail("Method not implemented");
		return 0;
	}

	@Override
	public void editChatAddPicture(int chatID, int pictureID)
			throws DataException {
		fail("Method not implemented");

	}

	@Override
	public void updatePushIds(HashMap<String, String> changes)
			throws DataException {
		fail("Method not implemented");

	}

	@Override
	public int addGroup(Group group) throws DataException {
		fail("Method not implemented");
		return 0;
	}

	@Override
	public void editGroup(Group group) throws DataException {
		fail("Method not implemented");
		
	}

	@Override
	public void scoreSubmissions(SubmissionScore[] scores) throws DataException {
		fail("Method not implemented");
	}

	@Override
	public RallyeGameState loadGameState() throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public void saveGameState(RallyeGameState gameState) throws DataException {
		fail("Method not implemented");
	}

	@Override
	public List<TaskSubmissions> getSubmissionsByTask(int taskID, boolean includeRatings)
			throws DataException {
		fail("Method not implemented");
		return null;
	}

	@Override
	public List<TaskSubmissions> getUnratedSubmissions(boolean includeRatings) throws DataException {
		fail("Method not implemented");
		return null;
	}

}
