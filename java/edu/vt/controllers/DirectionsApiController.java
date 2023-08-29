/*
 * Created by and Team 7 on 2023.5.1
 * Copyright © 2023 and Team 7. All rights reserved.
 */
package edu.vt.controllers;

import edu.vt.EntityBeans.Ride;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import edu.vt.pojo.PlacesApi;
import org.primefaces.shaded.json.JSONArray;
import org.primefaces.shaded.json.JSONObject;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SessionScoped
@Named("directionsApiController")
public class DirectionsApiController implements Serializable {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */
    @Inject
    private PlacesApiController placesApiController;

    private long currentTime;
    private List<Integer> times = null;
    private List<String> computedTimes = null;
    private List<Integer> orders = null;

    //https://www.google.com/maps/dir/?api=1&origin=Paris%2CFrance&destination=Cherbourg%2CFrance&travelmode=driving&waypoints=Versailles%2CFrance%7CChartres%2CFrance%7CLe+Mans%2CFrance%7CCaen%2CFrance
    private String googleUrl = null;

    // the direction of the ride (to or from)
    private String rideDirection;

    /*
     * computes time for etas
     */
    public List<String> getComputedTimes() {
        computedTimes = new ArrayList<String>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");
        long elapsedTime = 0;
        for (int i = 0; i < times.size(); i++) {
            elapsedTime += times.get(i);
            long eta = currentTime + elapsedTime;
            Date date = new Date(eta);
            String time = simpleDateFormat.format(date);
            computedTimes.add(time);
        }
        return computedTimes;
    }

    /*
     * Returns a string of the computed times
     */
    public String getTimesString(){
        getComputedTimes();
        String listedTimes = "";
        for(int i = 0; i < computedTimes.size(); i++){
            if(i == 0){
                listedTimes += computedTimes.get(i);
            }
            else{
                listedTimes += "," + computedTimes.get(i);
            }
        }
        return listedTimes;
    }

    public List<Integer> getTimes() {
        return times;
    }

    public String getOrdersString() {
        String toReturn = "";
        for(int i = 0; i < orders.size(); i++){
            if(i == 0) {
                toReturn += orders.get(i);
            }
            else{
                toReturn += "," + orders.get(i);
            }
        }
        return toReturn;
    }

    public String getGoogleUrl() {
        return googleUrl;
    }

    private Timestamp eta;

    public PlacesApiController getPlacesApiController() {
        return placesApiController;
    }

    public void setPlacesApiController(PlacesApiController placesApiController) {
        this.placesApiController = placesApiController;
    }

    public void setTimes(List<Integer> times) {
        this.times = times;
    }

    public void setOrders(List<Integer> orders) {
        this.orders = orders;
    }

    public void setGoogleUrl(String googleUrl) {
        this.googleUrl = googleUrl;
    }

    public Timestamp getEta() {
        return eta;
    }

    public void setEta(Timestamp eta) {
        this.eta = eta;
    }

    public String getRideDirection() { return rideDirection; }

    public void setRideDirection(String rideDirection) {
        this.rideDirection = rideDirection;
    }

    /*
     * Method used to get the estimated destination arrival time for the rider
     */
    public String distance(String startingAddress, String destinationAddress) {

        startingAddress = startingAddress.replaceAll(" ", "%20").replaceAll(",", "%2C");
        destinationAddress = destinationAddress.replaceAll(" ", "+").replaceAll(",", "%2C");

        String directionsUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?destinations=" +
                destinationAddress + "&origins=" + startingAddress + "&units=imperial&key=" + Constants.GOOGLE_API_KEY;

        String distance = "";
        String duration = "";

        String searchResultsJsonData = "";

        try {
            searchResultsJsonData = Methods.readUrlContent(directionsUrl);

            JSONObject searchResultsJsonObject = new JSONObject(searchResultsJsonData);

            if (!searchResultsJsonObject.optString("status").equals("OK")) {
                return "Response not OK";
            }

            JSONArray rowsJsonArray = new JSONArray(searchResultsJsonObject.optJSONArray("rows"));

            distance = ((JSONObject) ((JSONObject) ((JSONArray) ((JSONObject) rowsJsonArray.get(0)).get("elements")).get(0)).get("distance")).get("text").toString();
            duration = ((JSONObject) ((JSONObject) ((JSONArray) ((JSONObject) rowsJsonArray.get(0)).get("elements")).get(0)).get("duration")).get("text").toString();

        } catch (Exception ex) {
            return "API Failed";
        }

        return (distance.isEmpty() || duration.isEmpty()) ? "Unavailable" : distance + ", " + duration;
    }

    /*
     * Sets the google url to be clicked by driver
     */
    public String directions(String currentAddress, Ride ride) {
        currentTime = System.currentTimeMillis();
        times = new ArrayList<Integer>();
        orders = new ArrayList<Integer>();
        eta = null;
        googleUrl = "";

        placesApiController.places(currentAddress);
        PlacesApi start = placesApiController.getListOfPlacesFound().get(0);
        String start_id = start.getId();
        String start_name = start.getPlaceName();

        placesApiController.places(ride.getDestinationAddress());
        PlacesApi end = placesApiController.getListOfPlacesFound().get(0);
        String end_id = end.getId();
        String end_name = end.getPlaceName();

        // cleaning the values for the url
        String cleanedStartName = start_name.replaceAll(" ", "+").replaceAll(",", "%2C");
        String cleanedEndName = end_name.replaceAll(" ", "+").replaceAll(",", "%2C");

        String[] waypoints = ride.getWaypoints().split("#");

        // getting last wapoint in case this needs to be used as destination for "From Event"
        String cleanedWaypointEnd = waypoints[waypoints.length - 1].replaceAll(" ", "+").replaceAll(",", "%2C");

        // if going the ride is going to the evnet
        if(rideDirection.equalsIgnoreCase("To Event")){
            // destination is the event
            googleUrl = "https://www.google.com/maps/dir/?api=1&destination=" + cleanedEndName;
            if(waypoints.length > 0){
                googleUrl += "&waypoints=";
            }
            // waypoints are on the way
            for(int i = 0; i < waypoints.length; i++){
                String cleanedWaypoint = waypoints[i].replaceAll(" ", "+").replaceAll(",", "%2C");
                googleUrl += cleanedWaypoint;
                if(i+1 != waypoints.length){
                    googleUrl += "%7C";
                }
            }

        }
        // ride is picking up from event
        else{
            // the last waypoint is now the destination
            googleUrl = "https://www.google.com/maps/dir/?api=1&destination=" + cleanedWaypointEnd;
            // the first actual waypoint is the event becasue the driver needs to go there first
            googleUrl += "&waypoints=" + cleanedEndName;
            if(waypoints.length > 1){
                googleUrl += "%7C";
            }
            // adding rest of waypoints
            for(int i = 0; i < waypoints.length - 1; i++){
                String cleanedWaypoint = waypoints[i].replaceAll(" ", "+").replaceAll(",", "%2C");
                googleUrl += cleanedWaypoint;
                if(i+1 != waypoints.length){
                    googleUrl += "%7C";
                }
            }
        }




       // example request https://maps.googleapis.com/maps/api/directions/json?destination=place_id%3AChIJgdL4flSKrYcRnTpP0XQSojM&origin=place_id%3AChIJ7cv00DwsDogRAMDACa2m4K8&waypoints=place_id%3AChIJ69Pk6jdlyIcRDqM1KDY3Fpg%7Cplace_id%3AChIJgdL4flSKrYcRnTpP0XQSojM&key=AIzaSyD8m7QPBba3NZWS5WpfO97GbqsVj_CdDRQ
        String directionsUrl = "https://maps.googleapis.com/maps/api/directions/json?destination=place_id%3A" +
                end_id + "&origin=place_id%3A" + start_id;

        String[] ids = ride.getAddressIds().split(",");
        if(ids.length > 0){
            directionsUrl += "&waypoints=place_id%3A";
        }
        for(int i = 0; i < ids.length; i++){
            directionsUrl += ids[i];
            if(i+1 != ids.length){
                directionsUrl += "%7Cplace_id%3A";
            }
        }
        directionsUrl += "&key=" + Constants.GOOGLE_API_KEY;
        System.out.println(directionsUrl);
        Methods.preserveMessages();

        try {
            // Obtain the JSON file for directionsUrl from Google Geocoding API
            String searchResultsJsonData = Methods.readUrlContent(directionsUrl);

            // Create a new JSON object from the returned file
            JSONObject searchResultsJsonObject = new JSONObject(searchResultsJsonData);

            JSONArray routesJsonArray = searchResultsJsonObject.getJSONArray("routes");

            JSONObject onlyObject = routesJsonArray.getJSONObject(0);

            JSONArray legsJsonArray = onlyObject.getJSONArray("legs");

            int index = 0;
            while(legsJsonArray.length() > index) {
                JSONObject leg = legsJsonArray.getJSONObject(index);

                JSONObject duration = leg.getJSONObject("duration");

                Integer time = duration.optInt("value", -1);


                times.add(time);
                index++;
            }

            JSONObject overviewObject = onlyObject.getJSONObject("overview_polyline");
            JSONArray orderJsonArray = onlyObject.getJSONArray("waypoint_order");
            index = 0;
            while(orderJsonArray.length() > index) {
                Integer order = orderJsonArray.getInt(index);
                orders.add(order);
                index++;
            }

        } catch (Exception ex) {
            Methods.showMessage("Information", "Unable to Get Directions!",
                    "Google Directions API was unable to get directions!");
            return "fail";
        }
        eta = getETA();
        System.out.println(eta);
        return getTimesString() + "&" + getOrdersString();
    }

    /*
     * Gets the eta
     */
    public Timestamp getETA() {
        if (times == null || times.get(0) == null) {
            return new Timestamp(System.currentTimeMillis());
        }
        Integer duration = 0;
        int index = 0;
        while (times.size() > index) {
            duration += times.get(0);
            index++;
        }
        Timestamp date = new Timestamp(System.currentTimeMillis() + (duration*1000));

        return date;

    }

}
