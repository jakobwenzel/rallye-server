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
 * RallyeSoft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Rallyesoft.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.rallye.api;

import de.rallye.StadtRallye;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.server.mvc.Template;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMResult;
import org.jdom2.transform.JDOMSource;

import javax.ws.rs.*;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * A qr code to this page will be printed on the Student's Name Tags
 */
@Path("qrcode")
public class LandingPage {

    public static final Logger logger = LogManager.getLogger(LandingPage.class);


	static class RoomCode {
		/**
		 * The digit to look at to select the room
		 */
		final int digit;
		final List<String> rooms;

		RoomCode(int digit, List<String> rooms) {
			this.digit = digit;
			this.rooms = rooms;
		}
	}

	static java.util.Map<String,RoomCode> roomCodeMap = new HashMap<>();
	static {
		//Monday
		List<String> roomsMonday = new ArrayList<>(4);
		roomsMonday.add("S1|01 A5");
		roomsMonday.add("S3|20 5");
		roomsMonday.add("S3|06 321");
		roomsMonday.add("S1|01 A3");
		roomCodeMap.put("monday",new RoomCode(0,roomsMonday));

		//Tuesday A
		List<String> roomsTuesdayA = new ArrayList<>(4);
		roomsTuesdayA.add("S1|03 107");
		roomsTuesdayA.add("S1|14 169");
		roomsTuesdayA.add("S2|02 C-Pool");
		roomsTuesdayA.add("S2|02 C-Pool");
		roomCodeMap.put("tuesdayA",new RoomCode(1,roomsTuesdayA));

		//Tuesday B
		List<String> roomsTuesdayB = new ArrayList<>(4);
		roomsTuesdayB.add("S2|02 C-Pool");
		roomsTuesdayB.add("S2|02 C-Pool");
		roomsTuesdayB.add("S1|02 34");
		roomsTuesdayB.add("S1|14 169");
		roomCodeMap.put("tuesdayB",new RoomCode(1,roomsTuesdayB));
		
		//Thursday
		List<String> roomsThursday = new ArrayList<>(4);
		roomsThursday.add("S1|01 A5");
		roomsThursday.add("S2|07 109");
		roomsThursday.add("S2|14 024");
		roomsThursday.add("S1|03 226");
		roomCodeMap.put("thursday",new RoomCode(3,roomsThursday));

		List<String> roomsFriday = new ArrayList<>();
		roomsFriday.add("S1|01 A5");
		roomCodeMap.put("friday", new RoomCode(4, roomsFriday));
	}


	@GET
	@Produces({"text/html; charset=utf-8"})
	@Path("{groupsCode}/{studentId}")
	@Template(name = "/landingpage")
	public java.util.Map<String,String> getLandingPage(@PathParam("groupsCode") String groupsCode, @PathParam("studentId") int studentId) {
		java.util.Map<String,String> res = new HashMap<>();


		res.put("groupsCode",groupsCode);

		return res;

	}

    @GET
    @Produces("image/png")
    @Path("timetable")
    public StreamingOutput getTimeTable() throws JDOMException, TransformerException, IOException {
        return getTimeTable(null);
    }

    @GET
    @Produces("image/png")
    @Path("timetable/{groupsCode}")
    public StreamingOutput getTimeTable(@PathParam("groupsCode") final String groupsCode)  {


        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException,
                    WebApplicationException {
                try {
					Document doc = new SAXBuilder().build(StadtRallye.class.getResourceAsStream("timetable/stundenplan.xml"));
					replaceKgRooms(doc, groupsCode);
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


                    // Create a PNG transcoder
                    PNGTranscoder t = new PNGTranscoder();


                    // Create the transcoder input.
                    //String svgURI = new File(args[0]).toURL().toString();
                    TranscoderInput input = new TranscoderInput(svgIn);

                    // Create the transcoder output.
                    TranscoderOutput output = new TranscoderOutput(os);

					SVGConverter x;


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
    public InputStream getStylesheet() {

        return StadtRallye.class.getResourceAsStream("timetable/stundenplan.xsl");
    }

	static class Event {
		final String start;
		final String end;
		final String name;
		final String location;

		Event(String start, String end, String name, String location) {
			this.start = start;
			this.end = end;
			this.name = name;
			this.location = location;
		}
	}

	static class Time {
		final int hours;
		final int minutes;

		Time(int hours, int minutes) {
			while(minutes<0) {
				hours-=1;
				minutes+=60;
			}
			while(minutes>60) {
				hours+=1;
				minutes-=60;
			}
			this.hours = hours;
			this.minutes = minutes;
		}

		static Time fromString(String s) {
			int dot = s.indexOf(":");
			if (dot>0) {
				int hours = Integer.parseInt(s.substring(0, dot));
				int minutes = Integer.parseInt(s.substring(dot + 1));

				return new Time(hours, minutes);
			} else return null;
		}

		Time subtractMinutes(int diff) {
			return new Time(hours,minutes-diff);
		}
		Time addMinutes(int diff) {
			return new Time(hours,minutes+diff);
		}

		public String toString() {
			return (hours<10?"0":"") + hours +
					(minutes<10?"0":"") + minutes;
		}

		public int diffMin(Time t ) {
			return hours*60-t.hours*60+minutes-t.minutes;
		}
	}

	private void replaceKgRooms(Document doc, String groupsCode) {
		List<Element> days = doc.getRootElement().getChild("days").getChildren("day");
		for (Element day : days) {
			String dateStr = formatDate(day.getChildText("heading"));

			List<Element> events = day.getChild("events").getChildren("event");
			for (Element event : events) {
				if (event.getChildText("type").equals("kleingruppe")) {

					String locCode = event.getChildText("location");
					RoomCode code = roomCodeMap.get(locCode);
					if (code==null)
						continue;

					String room = "Kleingruppe";
					if (groupsCode!=null) {
						char ch = groupsCode.charAt(code.digit);
						int groupIdx = Character.getNumericValue(ch);

						room = code.rooms.get(groupIdx);
					}

					event.getChild("location").removeContent();
					event.getChild("location").addContent(room);
				}
			}
		}
	}

	@GET
	@Produces("text/calendar")
	@Path("timetable/{groupsCode}.ics")
	@Template(name = "/calendar")
	public List<Event> getCalendar(@PathParam("groupsCode") String groupsCode) throws JDOMException, IOException {
try {
	String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

	Document doc = new SAXBuilder().build(StadtRallye.class.getResourceAsStream("timetable/stundenplan.xml"));
	replaceKgRooms(doc, groupsCode);

	List<Event> result = new ArrayList<>();

	List<Element> days = doc.getRootElement().getChild("days").getChildren("day");
	for (Element day : days) {
		String dateStr = formatDate(day.getChildText("heading"));

		List<Element> events = day.getChild("events").getChildren("event");
		for (int i = 0; i < events.size(); i++) {
			Element event = events.get(i);

			if (event.getChildText("type").equals("spacer"))
				continue;

			Time startT = Time.fromString(event.getChildText("starttime"));
			String start = startT.toString();

			Time endT = new Time(20,200);
			if (i < events.size() - 1) {
				Element next = events.get(i + 1);
				String endStr = next.getChildText("starttime");
				endT = Time.fromString(endStr);

				endT = endT.subtractMinutes(10);
			} else {
				float dur = Float.parseFloat(event.getChildText("duration"))*55;
				endT = startT.addMinutes((int) dur);
			}


			String title = event.getChildText("title");
			String titleR = title.replace("&amp;","&");

			Event e = new Event(
					year + dateStr + "T" + start + "00",
					year + dateStr + "T" + endT.toString() + "00",
					titleR,
					event.getChildText("location")
			);
			result.add(e);
		}
	}

	return result;
}catch (RuntimeException e) {
	e.printStackTrace();
}
		return null;
	}

	/**
	 * Parses a string in the form "Montag, 6.10." to "1006"
	 * @param heading
	 * @return
	 */
	private String formatDate(String heading) {
		int space = heading.indexOf(" ");
		int dot = heading.indexOf(".", space);

		String day = heading.substring(space+1,dot);
		String month = heading.substring(dot+1,heading.length()-1);

		if (day.length()<2)
			day = "0"+day;

		if (month.length()<2)
			month = "0"+month;

		return month+day;
	}
}
