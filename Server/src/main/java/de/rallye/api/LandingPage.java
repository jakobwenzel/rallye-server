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

import de.rallye.StadtRallye;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.mvc.Template;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;

import javax.ws.rs.*;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Response;
import java.io.*;

/**
 * A qr code to this page will be printed on the Student's Name Tags
 */
@Path("qrcode")
public class LandingPage {

    public static final Logger logger = LogManager.getLogger(LandingPage.class);


	public static class Context {
		public final String groupsCode;

        public Context(String groupsCode) {
            this.groupsCode = groupsCode;
        }
    }

	@GET
	@Produces({"text/html; charset=utf-8"})
	@Path("{groupsCode}/{studentId}")
	@Template(name = "/landingpage")
	public Context getLandingPage(@PathParam("groupsCode") String groupsCode, @PathParam("studentId") int studentId) {
		return new Context(groupsCode);
	}

    @GET
    @Produces("image/png")
    @Path("timetable")
    public StreamingOutput getTimeTable() throws JDOMException, TransformerException, IOException {
        return getTimeTable("");
    }

    @GET
    @Produces("image/png")
    @Path("timetable/{groupsCode}")
    public StreamingOutput getTimeTable(@PathParam("groupsCode") final String groupsCode)  {


        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException,
                    WebApplicationException {
                Document doc = null;
                try {
                    doc = new SAXBuilder().build(StadtRallye.class.getResourceAsStream("timetable/stundenplan.xml"));
                    doc.getRootElement().getChild("heading").addContent(groupsCode);
                    Source xmlFile = new JDOMSource(doc);
                    final JDOMResult htmlResult = new JDOMResult();
                    Transformer transformer =
                            TransformerFactory.newInstance().newTransformer(
                                    new StreamSource(StadtRallye.class.getResourceAsStream("timetable/stundenplan.xsl")));
                    transformer.transform(xmlFile, htmlResult);

                    PipedInputStream svgIn = new PipedInputStream();
                    final PipedOutputStream svgOut = new PipedOutputStream(svgIn);


                    new Thread(
                            new Runnable(){
                                public void run(){

                                    XMLOutputter xmlOutputter = new XMLOutputter();
                                    try {
                                        xmlOutputter.output(htmlResult.getDocument(), svgOut);
                                        svgOut.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                    ).start();


                    // Create a JPEG transcoder
                    PNGTranscoder t = new PNGTranscoder();


                    // Create the transcoder input.
                    //String svgURI = new File(args[0]).toURL().toString();
                    TranscoderInput input = new TranscoderInput(svgIn);

                    // Create the transcoder output.
                    TranscoderOutput output = new TranscoderOutput(os);

                    // Save the image.
                    t.transcode(input, output);
                    svgIn.close();

                    // Flush and close the stream.
                    os.flush();
                    os.close();

                } catch (JDOMException e) {
                    e.printStackTrace();
                } catch (TransformerConfigurationException e) {
                    e.printStackTrace();
                } catch (TransformerException e) {
                    e.printStackTrace();
                } catch (TranscoderException e) {
                    e.printStackTrace();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        return stream;

        //return StadtRallye.class.getResourceAsStream("timetable/stundenplan.xml");
    }


    @GET
    @Produces("text/xsl")
    @Path("stundenplan.xsl")
    public InputStream getStylesheel() {

        return StadtRallye.class.getResourceAsStream("timetable/stundenplan.xsl");
    }
}
