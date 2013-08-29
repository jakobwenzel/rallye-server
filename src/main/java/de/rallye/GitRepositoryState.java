package de.rallye;

import java.io.IOException;
import java.util.Properties;

import org.codehaus.jackson.annotate.JsonWriteNullProperties;

@JsonWriteNullProperties(true)
public class GitRepositoryState {

	public String getDescription() {
		return description;
	}

	public String getBuildTime() {
		return buildTime;
	}
	private final String description; // =${git.commit.id.description}
	private final String buildTime; // =${git.build.time}

	public GitRepositoryState(Properties properties) {
		Object desc = properties.get("git.commit.id.description");
		this.description = (desc == null)? "" : desc.toString();
		Object time = properties.get("git.build.time");
		this.buildTime = time.toString();
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