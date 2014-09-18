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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;
import de.rallye.config.RallyeConfig;
import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.LatLngAlt;
import de.rallye.model.structures.UserInternal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;

@Path("rallye/debug")
public class Debug {

	private final Logger logger = LogManager.getLogger(Debug.class);

	@Inject	IDataAdapter data;
	@Inject
	RallyeConfig config;

	@GET
	@Path("log")
	@Produces(MediaType.TEXT_HTML)
	public String getLog() throws FileNotFoundException {

		File f = new File("log/debug.log");

		BufferedReader r = new BufferedReader(new FileReader(f));

		@SuppressWarnings("serial")
		LinkedHashMap<Integer, String> list = new LinkedHashMap<Integer, String>() {
			protected boolean removeEldestEntry(java.util.Map.Entry<Integer,String> arg0) {
				return size() > 50;
			}
		};

		int i = 0;
		String line;


		try {
			while ((line = r.readLine()) != null) {
				list.put(i++, line);
			}
		} catch (IOException e) {
			logger.error("Could not read Log", e);
		}

		try {
			r.close();
		} catch (IOException e) {
			logger.error("Could not close Log", e);
		}

		StringBuilder sb = new StringBuilder("<html><head><title>Log</title></head><body>");
		for (String l: list.values()) {
			sb.append(l).append("<br />");
		}
		return sb.append("</body></html>").toString();
	}

	@GET
	@Path("ping")
	public String ping(@Context HttpHeaders headers) {
		if (headers.getHeaderString("blubbel") != null)
			return "OK (blubbel sent)";
		else
			return "OK (no blubbel)";
	}

	@GET
	@Path("picture")
	@Produces("image/jpeg")
	public File getPicture() throws FileNotFoundException {
		File picture = new File(config.getDataDirectory()+"game/picture.jpg");
		if (picture.exists())
			return picture;
		else throw new FileNotFoundException("Picture not found");
	}

	@GET
	@Path("members/{groupID}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UserInternal> getMembers(@PathParam("groupID") int groupID) throws DataException {
		return data.getMembers(groupID);
	}

	@GET
	@Path("chatroomMembers/{roomID}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UserInternal> getChatroomMembers(@PathParam("roomID") int roomID) throws DataException {
		return data.getChatroomMembers(roomID);
	}

	@PUT
	@Path("pic2gps")
	@Consumes("image/*")
	@Produces(MediaType.APPLICATION_JSON)
	public LatLngAlt extractGpsLocation(File img) throws ImageProcessingException, IOException {
		Metadata meta = ImageMetadataReader.readMetadata(img);

		return readGps(meta);
	}

	@PUT
	@Path("pic2makemodel")
	@Consumes("image/*")
	@Produces(MediaType.TEXT_PLAIN)
	public String extractMakeModel(File img) throws ImageProcessingException, IOException {
		Metadata meta = ImageMetadataReader.readMetadata(img);

		StringBuilder sb = new StringBuilder();

		ExifIFD0Directory exif = meta.getDirectory(ExifIFD0Directory.class);

		sb.append("Make: ").append(exif.getString(ExifIFD0Directory.TAG_MAKE)).append(", Model: ").append(exif.getString(ExifIFD0Directory.TAG_MODEL));

		return sb.toString();
	}

	@PUT
	@Path("pic")
	@Consumes("image/*")
	@Produces("text/plain")
	public String convert(File img) throws /*FileNotFoundException,*/ IOException, ImageProcessingException {
		Metadata meta = ImageMetadataReader.readMetadata(img);

		StringBuilder sb = new StringBuilder();

		for (Directory directory : meta.getDirectories()) {
			sb.append(directory.getName()).append(":\n");
			for (Tag tag : directory.getTags()) {
				sb.append(tag).append('\n');
			}
		}
		return sb.toString();

	}

	private LatLngAlt readGps(Metadata meta) {
		GpsDirectory gps = meta.getDirectory(GpsDirectory.class);

		int altitude;
		try {
			altitude = gps.getInt(GpsDirectory.TAG_GPS_ALTITUDE);
		} catch (MetadataException e) {
			e.printStackTrace();
			altitude = 0;
		}

		LatLngAlt location = null;
		try {
			location = new LatLngAlt(gps.getGeoLocation().getLatitude(), gps.getGeoLocation().getLongitude(), altitude);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}
}
