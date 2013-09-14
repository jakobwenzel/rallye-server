package de.rallye.api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

import de.rallye.db.IDataAdapter;
import de.rallye.exceptions.DataException;
import de.rallye.model.structures.UserInternal;

@Path("rallye/debug")
public class Debug {

	private Logger logger = LogManager.getLogger(Debug.class);

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
