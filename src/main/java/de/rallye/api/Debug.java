package de.rallye.api;

import de.rallye.db.DataAdapter;
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
import javax.ws.rs.core.Response.Status;
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

	private Logger logger = LogManager.getLogger(Debug.class);

	@Inject	DataAdapter data;

	@GET
	@Path("members/{groupID}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UserInternal> getMembers(@PathParam("groupID") int groupID) {
		try {
			return data.getMembers(groupID);
		} catch (DataException e) {
			logger.error("Failed to get members", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("chatroomMembers/{roomID}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UserInternal> getChatroomMembers(@PathParam("roomID") int roomID) {
		try {
			return data.getChatroomMembers(roomID);
		} catch (DataException e) {
			logger.error("Failed to get chatroom members", e);
			throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	@Path("pic")
	@Consumes("image/jpeg")
	@Produces("text/plain")
	public String convert(File img) throws FileNotFoundException, IOException {
		ImageInputStream iis = ImageIO.createImageInputStream(new BufferedInputStream(new FileInputStream(img)));
		Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType("image/jpeg");
		IIOImage image = null;
		if (readers.hasNext()) {
			ImageReader reader = readers.next();
			reader.setInput(iis, true);
			try {
				image = reader.readAll(0, null);
			} catch (javax.imageio.IIOException e) {
				throw e;
			}

			IIOMetadata metadata = image.getMetadata();
			String[] names = metadata.getMetadataFormatNames();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < names.length; i++) {
				sb.append("Format name: " + names[i]);
				sb.append(displayMetadata(metadata.getAsTree(names[i])));
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
