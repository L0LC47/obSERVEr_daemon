package observer_daemon;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GarminParser {

	NodeList nList;
	int pos;

	double lat_0;
	double lon_0;
	double lat_1;
	double lon_1;
	long time_0;
	long time_1;
	double speed;

	SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	String format = "(\'%s\', \'%s\', \'%s\', %.0f, \'%.6f,%.6f\', to_timestamp(%d)),";

	public GarminParser(String file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);

		// Normalize the XML Structure; It's just too important !!
		document.getDocumentElement().normalize();

		// Here comes the root node
		Element root = document.getDocumentElement();

		// Get all elements
		this.nList = document.getElementsByTagName("trkpt");
		this.init();
	}

	public boolean hasNext() {
		return pos < nList.getLength();
	}

	public void next() throws Exception {
		if (pos >= nList.getLength())
			throw new NoSuchElementException();

		Node node = nList.item(pos++);
		if (node.getNodeType() != Node.ELEMENT_NODE)
			next(); // brr!
		Element eElement = (Element) node;
		lat_0 = lat_1;
		lon_0 = lon_1;
		time_0 = time_1;

		lat_1 = Double.parseDouble(eElement.getAttribute("lat"));
		lon_1 = Double.parseDouble(eElement.getAttribute("lon"));
		String time = eElement.getElementsByTagName("time").item(0)
				.getTextContent();
		time_1 = sf.parse(time).getTime() / 1000L;
		speed = (distance() / (time_1 - time_0) * 3.6);

	}

	public String getValue() {
		return String.format(Locale.ENGLISH, format, "observer", "email",
				"targa", speed, lat_1, lon_1, time_1);
	}

	public void init() throws Exception {
		if (!hasNext())
			throw new NoSuchElementException();

		Node node = nList.item(pos++);
		if (node.getNodeType() != Node.ELEMENT_NODE)
			init(); // brr!
		Element eElement = (Element) node;

		lat_1 = Double.parseDouble(eElement.getAttribute("lat"));
		lon_1 = Double.parseDouble(eElement.getAttribute("lon"));
		String time = eElement.getElementsByTagName("time").item(0)
				.getTextContent();
		time_1 = sf.parse(time).getTime() / 1000L;
		speed = 0;
	}

	public String getPosizione() {
		return String.format(Locale.ENGLISH, "%.6f,%.6f", lat_1, lon_1);
	}

	public long getTime() {
		return time_1;
	}

	public int getVelocita() {
		return (int) speed;
	}

	public static void main(String[] args) throws Exception {

		GarminParser g = new GarminParser("examples/VERONA SOLFERINO.gpx.xml");
		while (g.hasNext()) {
			g.next();
			System.out.println(g.getValue());
		}
	}

	// http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
	public double distance() {

		final int R = 6371; // Radius of the earth

		double latDistance = Math.toRadians(lat_1 - lat_0);
		double lonDistance = Math.toRadians(lon_1 - lon_0);
		double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(Math.toRadians(lat_0))
				* Math.cos(Math.toRadians(lat_1)) * Math.sin(lonDistance / 2)
				* Math.sin(lonDistance / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = R * c * 1000; // convert to meters

		distance = Math.pow(distance, 2);

		return Math.sqrt(distance);
	}
}
