package de.rallye;

import java.io.IOException;
import java.util.Properties;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@JsonWriteNullProperties(true)
public class GitRepositoryState {
	public String getBranch() {
		return branch;
	}

	public String getDescribe() {
		return describe;
	}

	public String getCommitId() {
		return commitId;
	}

	public String getBuildUserName() {
		return buildUserName;
	}

	public String getBuildUserEmail() {
		return buildUserEmail;
	}

	public String getBuildTime() {
		return buildTime;
	}

	public String getCommitUserName() {
		return commitUserName;
	}

	public String getCommitUserEmail() {
		return commitUserEmail;
	}

	public String getCommitMessageFull() {
		return commitMessageFull;
	}

	public String getCommitMessageShort() {
		return commitMessageShort;
	}

	public String getCommitTime() {
		return commitTime;
	}
	public String getCommitIdAbbrev() {
		return commitIdAbbrev;
	}

	public String getRemote() {
		return remote;
	}


	private final String branch; // =${git.branch}
	private final String describe; // =${git.commit.id.describe}
	private final String commitId; // =${git.commit.id}
	private final String commitIdAbbrev; // =${git.commit.id.abbrev}
	private final String buildUserName; // =${git.build.user.name}
	private final String buildUserEmail; // =${git.build.user.email}
	private final String buildTime; // =${git.build.time}
	private final String commitUserName; // =${git.commit.user.name}
	private final String commitUserEmail; // =${git.commit.user.email}
	private final String commitMessageFull; // =${git.commit.message.full}
	private final String commitMessageShort; // =${git.commit.message.short}
	private final String commitTime; // =${git.commit.time}
	private final String remote; //=${git.remote.origin.url}

	public GitRepositoryState(Properties properties) {
		this.branch = properties.get("git.branch").toString();
		this.describe = properties.get("git.commit.id.describe").toString();
		this.commitId = properties.get("git.commit.id").toString();
		this.buildUserName = properties.get("git.build.user.name").toString();
		this.buildUserEmail = properties.get("git.build.user.email").toString();
		this.buildTime = properties.get("git.build.time").toString();
		this.commitUserName = properties.get("git.commit.user.name").toString();
		this.commitUserEmail = properties.get("git.commit.user.email")
				.toString();
		this.commitMessageShort = properties.get("git.commit.message.short")
				.toString();
		this.commitMessageFull = properties.get("git.commit.message.full")
				.toString();
		this.commitTime = properties.get("git.commit.time").toString();
		this.commitIdAbbrev = properties.get("git.commit.id.abbrev").toString();
		this.remote = properties.getProperty("git.remote.origin.url").toString();
	}

	private static GitRepositoryState gitRepositoryState;

	public static GitRepositoryState getGitRepositoryState() throws IOException {
		if (gitRepositoryState == null) {
			Properties properties = new Properties();
			properties.load(GitRepositoryState.class.getClassLoader()
					.getResourceAsStream("git.properties"));

			gitRepositoryState = new GitRepositoryState(properties);
		}
		return gitRepositoryState;
	}
}