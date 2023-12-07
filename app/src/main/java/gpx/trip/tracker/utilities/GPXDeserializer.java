package gpx.trip.tracker.utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.InputStream;
import java.util.ArrayList;

import gpx.trip.tracker.dto.RoutePoint;

public class GPXDeserializer {
    public static ArrayList<RoutePoint> deserializeGPX(Context context, Uri uri) {
        ArrayList<RoutePoint> routePoints = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();

        try {
            InputStream inputStream = contentResolver.openInputStream(uri);

            if (inputStream != null) {
                //prepare the XML parser
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                //parse the GPX data from the InputStream and obtain the root element
                Document document = builder.parse(inputStream);
                Element root = document.getDocumentElement();

                //get a NodeList of all route point elements in the GPX file
                NodeList rteptNodes = root.getElementsByTagName("rtept");
                Log.d("MyTag", "Number of rtept elements: " + rteptNodes.getLength());

                //store the previous point's reaching time for calculating the current point's reaching time
                double previousReachingTime = 0;

                for (int i = 0; i < rteptNodes.getLength(); i++) {

                    try {
                        Element rteptElement = (Element) rteptNodes.item(i);
                        RoutePoint routePoint = new RoutePoint();

                        routePoint.setLat(Double.parseDouble(rteptElement.getAttribute("lat")));
                        routePoint.setLon(Double.parseDouble(rteptElement.getAttribute("lon")));

                        String termContent = rteptElement.getElementsByTagName("term").item(0).getTextContent();
                        double term = Double.parseDouble(termContent);
                        routePoint.setTerm(term);

                        NodeList nameNodes = rteptElement.getElementsByTagName("name");
                        if (nameNodes.getLength() > 0) {
                            String name = nameNodes.item(0).getTextContent();
                            routePoint.setName(name);
                        }

                        NodeList restNodes = rteptElement.getElementsByTagName("rest");
                        if (restNodes.getLength() > 0) {
                            String restContent = restNodes.item(0).getTextContent();
                            int rest = Integer.parseInt(restContent);
                            routePoint.setRest(rest);
                        }

                        NodeList refNodes = rteptElement.getElementsByTagName("ref");
                        if (refNodes.getLength() > 0) {
                            String refContent = refNodes.item(0).getTextContent().trim(); //trim whitespace
                            if (!refContent.isEmpty()) { //check if the content is not empty
                                String[] refArray = refContent.split(",");
                                int[] siblings = new int[refArray.length];
                                for (int j = 0; j < refArray.length; j++) {
                                    siblings[j] = Integer.parseInt(refArray[j]);
                                }
                                routePoint.setSiblings(siblings);
                            } else {
                                routePoint.setSiblings(new int[0]);
                                Log.d("MyTag", "Empty 'ref' element for point " + i);
                            }
                        }

                        //calculate reachingTime for the current point
                        double reachingTime = previousReachingTime + routePoint.getTerm() + routePoint.getRest();
                        routePoint.setReachingTime(reachingTime);
                        previousReachingTime = reachingTime;

                        routePoints.add(routePoint);
                    } catch (Exception e) {
                        Log.e("MyTag", "Error processing point " + i, e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routePoints;
    }
}
