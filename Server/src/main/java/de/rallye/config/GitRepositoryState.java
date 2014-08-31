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