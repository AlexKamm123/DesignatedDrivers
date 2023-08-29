/*
 * Created by Osman Balci and Team 7 on 2023.4.28
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */
package edu.vt.controllers;

import edu.vt.EntityBeans.Request;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import org.primefaces.shaded.json.JSONArray;
import org.primefaces.shaded.json.JSONObject;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named("mapController")
@SessionScoped
public class MapController implements Serializable {

    /*
    ===============================
    Instance Variables (Properties)
    ===============================
    */

    private String locationLatString;
    private String locationLngString;
    private double locationLatDouble;
    private double locationLngDouble;
    private String startingLocationName;
    private String startingLocationAddress;
    private String destinationLocationName;
    private String destinationLocationAddress;
    private double startingLocationLat;
    private double startingLocationLng;
    private double destinationLocationLat;
    private double destinationLocationLng;
    private String travelMode;
    private String travelModeCapitalized;
    @Inject RequestController requestController;

    /*
    =========================
    Getter and Setter Methods
    =========================
     */

    public String getLocationLatString() {
        return locationLatString;
    }

    public void setLocationLatString(String locationLatString) {
        this.locationLatString = locationLatString;
    }

    public String getLocationLngString() {
        return locationLngString;
    }

    public void setLocationLngString(String locationLngString) {
        this.locationLngString = locationLngString;
    }

    public double getLocationLatDouble() {
        return locationLatDouble;
    }

    public void setLocationLatDouble(double locationLatDouble) {
        this.locationLatDouble = locationLatDouble;
    }

    public double getLocationLngDouble() {
        return locationLngDouble;
    }

    public void setLocationLngDouble(double locationLngDouble) {
        this.locationLngDouble = locationLngDouble;
    }

    public String getStartingLocationName() {
        return startingLocationName;
    }

    public void setStartingLocationName(String startingLocationName) {
        this.startingLocationName = startingLocationName;
    }

    public String getStartingLocationAddress() {
        return startingLocationAddress;
    }

    public void setStartingLocationAddress(String startingLocationAddress) {
        this.startingLocationAddress = startingLocationAddress;
    }

    public String getDestinationLocationName() {
        return destinationLocationName;
    }

    public void setDestinationLocationName(String destinationLocationName) {
        this.destinationLocationName = destinationLocationName;
    }

    public String getDestinationLocationAddress() {
        return destinationLocationAddress;
    }

    public void setDestinationLocationAddress(String destinationLocationAddress) {
        this.destinationLocationAddress = destinationLocationAddress;
    }

    public double getStartingLocationLat() {
        return startingLocationLat;
    }

    public void setStartingLocationLat(double startingLocationLat) {
        this.startingLocationLat = startingLocationLat;
    }

    public double getStartingLocationLng() {
        return startingLocationLng;
    }

    public void setStartingLocationLng(double startingLocationLng) {
        this.startingLocationLng = startingLocationLng;
    }

    public double getDestinationLocationLat() {
        return destinationLocationLat;
    }

    public void setDestinationLocationLat(double destinationLocationLat) {
        this.destinationLocationLat = destinationLocationLat;
    }

    public double getDestinationLocationLng() {
        return destinationLocationLng;
    }

    public void setDestinationLocationLng(double destinationLocationLng) {
        this.destinationLocationLng = destinationLocationLng;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public String getTravelModeCapitalized() {
        return travelModeCapitalized;
    }

    public void setTravelModeCapitalized(String travelModeCapitalized) {
        this.travelModeCapitalized = travelModeCapitalized;
    }

    /*
    ================
    Instance Methods
    ================
    */

    /*
    =================================================================
     INPUT: query
        Query can be a full address, “city, country”, a landmark name
        or “latitude, longitude” to show its location on Google map

     OUTPUT: Computed values of the following instance variables:
        private String locationLatString;
        private String locationLngString;
        private double locationLatDouble;
        private double locationLngDouble;
     ================================================================
     */
    public void geocode(String query) {

        String cleanedQuery = query.replaceAll(" ", "+");

        String geocodingUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                cleanedQuery + "&key=" + Constants.GOOGLE_API_KEY;

        Methods.preserveMessages();

        try {
            // Obtain the JSON file for geocodingUrl from Google Geocoding API
            String searchResultsJsonData = Methods.readUrlContent(geocodingUrl);

            /*
             {
             ✅"results" :
               [
                  {
                     "address_components" : [...],
                     "formatted_address" : "2001 Carroll Dr, Blacksburg, VA 24060, USA",
                   ✅"geometry" : {
                        "bounds" : {...},
                      ✅"location" : {
                         ✅"lat" : 37.25518630000001,
                         ✅"lng" : -80.4183295
                        },
                        "location_type" : "ROOFTOP",
                        "viewport" : {...}
                     },
                     "place_id" : "ChIJMfS-09a_TYgR_cWtDLRormY",
                     "types" : [ "premise" ]
                  }
               ],
               "status" : "OK"
             }
             */

            // Create a new JSON object from the returned file
            JSONObject searchResultsJsonObject = new JSONObject(searchResultsJsonData);
            JSONArray resultsJsonArray = searchResultsJsonObject.getJSONArray("results");
            JSONObject aJsonObject = resultsJsonArray.getJSONObject(0);
            JSONObject geometryJsonObject = aJsonObject.getJSONObject("geometry");
            JSONObject locationJsonObject = geometryJsonObject.getJSONObject("location");

            // setting lats and longs
            locationLatDouble = locationJsonObject.optDouble("lat", 0.0);
            locationLngDouble = locationJsonObject.optDouble("lng", 0.0);

            locationLatString = String.valueOf(locationLatDouble);
            locationLngString = String.valueOf(locationLngDouble);

        } catch (Exception ex) {
            Methods.showMessage("Information", "Unable to Geocode!",
                    "Google Geocoding API was unable to geocode the given query!");
            return;
        }
    }

    /*
    ***************************************
    Set travel mode and determine start and
    destination geolocations for directions
    ***************************************
     */
    public void createStartAndDestinationPoints(Request request) {
        Methods.preserveMessages();

        travelMode = "DRIVING";
        travelModeCapitalized = "Driving";

        startingLocationName = requestController.startingLocationName(request);
        startingLocationAddress = requestController.startingLocation(request);

        destinationLocationName = requestController.destinationLocationName(request);
        destinationLocationAddress = requestController.destinationLocation(request);

        /*
        ------------------------------------------------------------------------
        Starting building geolocation determination for directions to START with
        ------------------------------------------------------------------------
         */
        geocode(startingLocationAddress);

        startingLocationLat = locationLatDouble;
        startingLocationLng = locationLngDouble;

        /*
        -------------------------------------------------------------------------
        Destination building geolocation determination for directions to END with
        -------------------------------------------------------------------------
         */
        geocode(destinationLocationAddress);

        destinationLocationLat = locationLatDouble;
        destinationLocationLng = locationLngDouble;

        return;
    }
}
