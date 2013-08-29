package de.rallye;

import java.io.IOException;
import java.util.Properties;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

@JsonWriteNullProperties(true)
public class GitRepositoryState {

	public String getDescribe() {
		return describe;
	}

	public String getBuildTime() {
		return buildTime;
	}
	private final String describe; // =${git.commit.id.describe}
	private final String buildTime; // =${git.build.time}

	public GitRepositoryState(Properties properties) {
		this.describe = properties.get("git.commit.id.describe").toString();
		this.buildTime = properties.get("git.build.time").toString();
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