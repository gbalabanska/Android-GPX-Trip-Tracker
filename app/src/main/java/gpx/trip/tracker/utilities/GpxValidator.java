package gpx.trip.tracker.utilities;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class GpxValidator {

    public static boolean validateGpx(Uri uri, Context context) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(context.getContentResolver().openInputStream(uri));

            Element rootElement = document.getDocumentElement();

            NodeList rteList = rootElement.getElementsByTagName("rte");
            if (rteList.getLength() != 1) {
                return false;
            }

            Element rteElement = (Element) rteList.item(0);

            int markerPointCount = 0;
            int regularRteptCount = 0;

            NodeList rteptList = rteElement.getElementsByTagName("rtept");
            for (int i = 0; i < rteptList.getLength(); i++) {
                Element rteptElement = (Element) rteptList.item(i);

                if (!rteptElement.hasAttribute("lat") || !rteptElement.hasAttribute("lon")) {
                    return false;
                }

                String latValue = rteptElement.getAttribute("lat").trim();
                String lonValue = rteptElement.getAttribute("lon").trim();
                Log.d("validate", "lat=" + latValue);
                if (latValue.isEmpty() || lonValue.isEmpty()) {
                    return false;
                }

                NodeList termList = rteptElement.getElementsByTagName("term");
                NodeList refList = rteptElement.getElementsByTagName("ref");

                if (termList.getLength() != 1 || refList.getLength() != 1) {
                    return false;
                }

                Element termElement = (Element) termList.item(0);
                String termValue = termElement.getTextContent().trim();

                if (termValue.isEmpty()) {
                    return false;
                }


                Element refElement = (Element) refList.item(0);

                String refText = refElement.getTextContent().trim();

                String[] refIds = refText.split(",");
                //refIds contains empty string if refText is empty
                if (!refText.equals("")) {
                    if (refIds.length != 0 && refIds.length != 13) {
                        return false;
                    }
                }
                for (String refId : refIds) {
                    try {
                        if (!refId.equals("")) {
                            int id = Integer.parseInt(refId);
                            if (id > i) {
                                return false;
                            }
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }

                NodeList nameList = rteptElement.getElementsByTagName("name");
                if (nameList.getLength() == 1) {
                    Element nameElement = (Element) nameList.item(0);
                    String nameValue = nameElement.getTextContent().trim();

                    if (!nameValue.isEmpty()) {
                        if (i != 0 && regularRteptCount < 9) {
                            return false;
                        }
                        markerPointCount++;
                        regularRteptCount = 0;
                    }
                } else {regularRteptCount++;}

            }

            //check if there are at least 2 marker points
            if (markerPointCount < 2) {
                return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("validate", "e" + e.toString());
            return false;
        }
    }
}
