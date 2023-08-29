/*
 * Created by Team 7 on 2023.4.30
 * Copyright © 2023 Team 7. All rights reserved.
 */
package edu.vt.controllers;

import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import edu.vt.pojo.PlacesApi;
import org.primefaces.shaded.json.JSONArray;
import org.primefaces.shaded.json.JSONObject;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@SessionScoped
@Named("placesApiController")
public class PlacesApiController implements Serializable {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */

    private List<PlacesApi> listOfPlacesFound = null;


    public List<PlacesApi> getListOfPlacesFound() {
        return listOfPlacesFound;
    }

    public void setListOfPlacesFound(List<PlacesApi> listOfPlacesFound) {
        this.listOfPlacesFound = listOfPlacesFound;
    }

    public void places(String query) {
        listOfPlacesFound = new ArrayList<>();

        String cleanedQuery = query.replaceAll(" ", "+");

        String placesUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" +
                cleanedQuery + "&language=en&types=geocode&key=" + Constants.GOOGLE_API_KEY;

        Methods.preserveMessages();

        try {
            // Obtain the JSON file for placesUrl from Google Geocoding API
            String searchResultsJsonData = Methods.readUrlContent(placesUrl);

            // Create a new JSON object from the returned file
            JSONObject searchResultsJsonObject = new JSONObject(searchResultsJsonData);

            JSONArray predictionsJsonArray = searchResultsJsonObject.getJSONArray("predictions");

            int index = 0;

            while(predictionsJsonArray.length() > index) {
                JSONObject prediction = predictionsJsonArray.getJSONObject(index);

                String name = prediction.optString("description", "");
                if(name.equals("")){
                    index++;
                    continue;
                }

                String id = prediction.optString("place_id", "");
                if(id.equals("")){
                    index++;
                    continue;
                }

                PlacesApi placeFound = new PlacesApi(id, name);
                // Add the newly created PlacesApi object to the list of places found
                listOfPlacesFound.add(placeFound);
                index++;
            }

        } catch (Exception ex) {
            Methods.showMessage("Information", "Unable to Geocode!",
                    "Google Geocoding API was unable to geocode the given query!");
            return;
        }
    }

}
