/*
 * Created by Osman Balci and Team 7 on 2023.4.26
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */
package edu.vt.pojo;

// This class provides a Plain Old Java Object (POJO) representing a Place returned from the API
public class PlacesApi {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */

    // unique id of pojo
    private String id = null;
    private String placeName;


    /*
        ==================
        Constructor Method
        ==================
         */

    public PlacesApi(String id, String placeName) {
        this.id = id;
        this.placeName = placeName;
    }

    /*
    =========================
    Getter and Setter Methods
    =========================
     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
}
