package de.rallye.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GitRepositoryState {

	private final String description; // =${git.commit.id.description}
	private final String buildTime; // =${git.build.time}

	private GitRepositoryState() throws IOException {
		Properties properties = new Properties();
		InputStream str = GitRepositoryState.class.getClassLoader().getResourceAsStream("git.properties");
		if (str !=null) {
			properties.load(str);
	
			Object desc = properties.get("git.commit.id.description");
			this.description = (desc == null)? "" : desc.toString();
	
			Object time = properties.get("git.build.time");
			this.buildTime = time.toString();
		} else {
			this.description="Unknown build";
			this.buildTime="Unknown";
		}
	}

	public static GitRepositoryState getState() {
		try {
			return new GitRepositoryState();
		} catch (IOException e) {
			return null;
		}
	}

	public String getDescription() {
		return description;
	}

	public String getBuildTime() {
		return buildTime;
	}
}