/*
 * Copyright (c) 2014 Jakob Wenzel, Ramon Wirsch.
 *
 * This file is part of RallyeSoft.
 *
 * RallyeSoft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RallyeSoft. If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GitRepositoryState {
	private final Logger logger = LogManager.getLogger(GitRepositoryState.class);

	private final String branch; // =$(git.commit.branch)
	private final String revision; // =${git.commit.id.revision}
	private final String buildTime; // =${git.build.time}

	private GitRepositoryState() throws IOException {
		Properties properties = new Properties();
		InputStream str = GitRepositoryState.class.getClassLoader().getResourceAsStream("META-INF/buildInfo.properties");
		if (str !=null) {
			logger.trace("Found buildInfo.properties");
			properties.load(str);

			Object branch = properties.get("git.commit.branch");
			this.branch = (branch == null)? "" : branch.toString();

			Object desc = properties.get("git.commit.id.revision");
			this.revision = (desc == null)? "" : desc.toString();
	
			Object time = properties.get("git.build.time");
			this.buildTime = time.toString();
		} else {
			logger.trace("No buildInfo.properties");
			this.branch="Unknown branch";
			this.revision="Unknown build";
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

	public String getBranch() {
		return branch;
	}

	public String getRevision() {
		return revision;
	}

	public String getBuildTime() {
		return buildTime;
	}

	@Override
	public String toString() {
		return "Git: "+ branch +":"+ revision +" @ "+ buildTime;
	}
}