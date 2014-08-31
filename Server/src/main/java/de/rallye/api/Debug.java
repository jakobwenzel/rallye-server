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

import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.UserInternal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Iterator;
import java.util.List;

@Path("rallye/debug")
public class Debug {

	private final Logger logger = LogManager.getLogger(Debug.class);

	@Inject	IDataAdapter data;

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
	@Path("pic")
	@Consumes("image/jpeg")
	@Produces("text/plain")
	public String convert(File img) throws /*FileNotFoundException,*/ IOException {
		ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(img)));
		Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType("image/jpeg");
		IIOImage image = null;
		if (readers.hasNext()) {
			ImageReader reader = readers.next();
			reader.setInput(iis, true);

			image = reader.readAll(0, null);

			IIOMetadata metadata = image.getMetadata();
			String[] names = metadata.getMetadataFormatNames();

			StringBuilder sb = new StringBuilder();
			for (String name : names) {
				sb.append("Format name: " + name);
				sb.append(displayMetadata(metadata.getAsTree(name)));
			}
			return sb.toString();
		}
		return "";
	}
	
	private String displayMetadata(Node node) {
		StringWriter sw = new StringWriter();
		try {
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(sw));
		} catch (TransformerException e) {
			logger.error("nodeToString Transformer Exception", e);
		}
		return sw.toString();
	}

}
