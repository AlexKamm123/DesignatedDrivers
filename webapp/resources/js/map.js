/* 
 * Created by Osman Balci and Team 7 on 2023.4.30
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */

/********************************************************************************************* 
 You must provide your Google Maps JavaScript API Developer Key to able to show maps and
 get directions. I specified mine in siteTemplate.xhtml as follows:
 
 <script src="https://maps.google.com/maps/api/js?key=AIzaSyA12ol8V1MBwjR_uTqL4oR7_7DQQE-ePro"
 type="text/javascript"></script>
 
 This is given in siteTemplate.xhtml so that each page can use Google Maps JavaScript API. 

 This file provides the JavaScript functions we need to display Google maps and get directions.
 *********************************************************************************************/

/* Global variables */

// Object for autocomplete
var autocomplete

var google;

// Object reference 'map' to point to a Google Map object
var map;

// Object reference 'currentMarker' to point to a location on map
var currentMarker = null;

/*
 You can obtain directions via driving, bicycling, bus, or walking by using the DirectionsService object.
 This object communicates with the Google Maps API Directions Service which receives direction requests
 and returns computed results. You can handle these directions results by using the DirectionsRenderer
 object to render these results. [https://developers.google.com/maps/documentation/javascript/directions]
 */

// Instantiate a DirectionsService object and store its object reference into directionsService.
var directionsService = new google.maps.DirectionsService();

// Instantiate a DirectionsRenderer object and store its object reference into directionsDisplay.
var directionsDisplay = new google.maps.DirectionsRenderer();

// handles autocomplete for addresses
function Autocomplete(id){

    // gits the input
    const input = document.getElementById(id);
    if(input != null){
        const options = {
            fields: ["formatted_address"], //only need this field
            strictBounds: false,
            types: ["address"], // only need to look at addresses
        };
        // create autocomplete object with input and options
        autocomplete = new google.maps.places.Autocomplete(input, options);

        // set new value after clicking on address
        autocomplete.addListener('place_changed', () => {
            var place = autocomplete.getPlace();
            document.getElementById(id).value = place.formatted_address;
        });
    }
}

// Create and display a Virginia Tech campus map
function initializeMap() {

    /*
     Instantiate a new Virginia Tech campus map object and set its properties.
     document.getElementById('map') --> Obtains the Google Map style definition
     from the div element with id="map" in LocationOnMap.xhtml
     */
    console.log(google.maps)
    map = new google.maps.Map(document.getElementById('map'), {
        zoom: 15,
        center: {lat: 37.227264, lng: -80.420745},
        mapTypeControl: true,
        mapTypeControlOptions: {
            style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
            position: google.maps.ControlPosition.BOTTOM_LEFT
        },
        mapTypeId: google.maps.MapTypeId.HYBRID
    });

    // Show the desired map using the map created above by calling the display() function.
    display();

}

/*
 The Virginia Tech campus map created in the initializeMap() function above is used to show:
 (1) directions from one building to another on campus,
 (2) the location of a single VT building, or
 (3) locations of VT buildings in a given building category.
 */
function display() {
    /*
     document.getElementById("destinationLocationName").value --> Obtains the name of the destination
     location (VT building) from the hidden input element with id="destinationLocationName" in LocationOnMap.xhtml
     */
    if (document.getElementById("destinationLocationName").value !== '') {
        /*
         If destinationLocationName has a value, the user asked for directions.
         Show directions on the VT campus map created in the initializeMap() function.
         */
        drawRoute();
    }
    /*
     document.getElementById("locationName").value --> Obtains the name of the selected
     location (VT building) from the hidden input element with id="locationName" in LocationOnMap.xhtml
     */
    else if (document.getElementById("locationName").value !== '') {
        /*
         If locationName has a value, only one location will be shown on the map.
         Show the location on the VT campus map created in the initializeMap() function.
         */
        displaySingleLocation();
    } else {
        /*
         Show the locations of VT buildings in a given building category on
         the VT campus map created in the initializeMap() function.
         */
        displayBuildingsByCategory();
    }

}


// Displays the geolocation of the selected VT building on the VT campus map created in the initializeMap() function.
function displaySingleLocation() {

    // Obtain the selected location's name from the hidden input element with id="locationName" in LocationOnMap.xhtml
    var locationName = document.getElementById("locationName").value;

    // Obtain the selected location's Latitude value from the hidden input element with id="locationLat" in LocationOnMap.xhtml
    var locationLatitude = document.getElementById("locationLat").value;

    // Obtain the selected location's Longitude value from the hidden input element with id="locationLong" in LocationOnMap.xhtml
    var locationLongitude = document.getElementById("locationLong").value;

    // Determine the geolocation of the selected location
    var locationLatLong = new google.maps.LatLng(locationLatitude, locationLongitude);

    // Set the center of the map to the geolocation coordinates of the selected location
    map.setCenter(locationLatLong);

    // Instantiate a new pin marker and dress it up with the selected location's properties
    currentMarker = new google.maps.Marker({
        title: locationName,
        position: locationLatLong,
        map: map
    });

    // Place the newly created pin marker on the VT campus map
    currentMarker.setMap(map);

    // Instantiate a new InfoWindow object to display the location's name when the pin marker is clicked
    var infoWindow = new google.maps.InfoWindow();

    // Attach an event handler to currentMarker to display the location's name when the pin marker is clicked
    google.maps.event.addListener(currentMarker, "click", function () {

        infoWindow.setContent(this.get('title'));  // Show the location's name

        infoWindow.open(map, this);   // Use the map created here (map is a global variable)
    });

}


// Displays the geolocations of all VT buildings in the given category
function displayBuildingsByCategory() {

    /*
     document.getElementById("jsonCategoryResult").value --> Obtains the JSON data for all VT buildings
     in a given category from the hidden input element with id="jsonCategoryResult" in LocationOnMap.xhtml
     
     For example, document.getElementById("jsonCategoryResult").value --> returns the following JSON data for the category 'Athletic':
     [
     {"abbreviation":"TC","category":"Athletic","descriptionUrl":"http://manta.cs.vt.edu/vt/buildings/tc/tc.txt","id":10,"imageUrl":"http://manta.cs.vt.edu/vt/buildings/tc/tc.jpg","latitude":37.2148423470,"longitude":-80.4193116325,"name":"Burrows-Burleson Tennis Center"},
     {"abbreviation":"COL","category":"Athletic","descriptionUrl":"http://manta.cs.vt.edu/vt/buildings/col/col.txt","id":14,"imageUrl":"http://manta.cs.vt.edu/vt/buildings/col/col.jpg","latitude":37.2225472975,"longitude":-80.4189774813,"name":"Cassell Coliseum"},
     {"abbreviation":"BBPF","category":"Athletic","descriptionUrl":"http://manta.cs.vt.edu/vt/buildings/bbpf/bbpf.txt","id":34,"imageUrl":"http://manta.cs.vt.edu/vt/buildings/bbpf/bbpf.jpg","latitude":37.2235306695,"longitude":-80.4182276484,"name":"Hahn Hurst Basketball Practice Center"},
     {"abbreviation":"JAMC","category":"Athletic","descriptionUrl":"http://manta.cs.vt.edu/vt/buildings/jamc/jamc.txt","id":44,"imageUrl":"http://manta.cs.vt.edu/vt/buildings/jamc/jamc.jpg","latitude":37.2221614070,"longitude":-80.4187417030,"name":"Jamerson Athletic Center"},
     {"abbreviation":"STAD","category":"Athletic","descriptionUrl":"http://manta.cs.vt.edu/vt/buildings/stad/stad.txt","id":49,"imageUrl":"http://manta.cs.vt.edu/vt/buildings/stad/stad.jpg","latitude":37.2200492457,"longitude":-80.4180559870,"name":"Lane Stadium / Worsham Field"},
     {"abbreviation":"MCCOM","category":"Athletic","descriptionUrl":"http://manta.cs.vt.edu/vt/buildings/mccom/mccom.txt","id":58,"imageUrl":"http://manta.cs.vt.edu/vt/buildings/mccom/mccom.jpg","latitude":37.2203073567,"longitude":-80.4224733516,"name":"McComas Hall"},
     {"abbreviation":"MRYMN","category":"Athletic","descriptionUrl":"http://manta.cs.vt.edu/vt/buildings/mrymn/mrymn.txt","id":61,"imageUrl":"http://manta.cs.vt.edu/vt/buildings/mrymn/mrymn.jpg","latitude":37.2215480711,"longitude":-80.4190727526,"name":"Merryman Athletic Facility"},
     {"abbreviation":"RFH","category":"Athletic","descriptionUrl":"http://manta.cs.vt.edu/vt/buildings/rfh/rfh.txt","id":85,"imageUrl":"http://manta.cs.vt.edu/vt/buildings/rfh/rfh.jpg","latitude":37.2189872178,"longitude":-80.4216880690,"name":"Rector Field House"}
     ]
     
     The JSON data comes as one Array containing JavaScript objects (also called dictionaries). The Array is represented by brackets [].
     Each JavaScript object is defined by key:value pairs within curly braces {}.
     So, the above JSON data contain 8 JavaScript objects (i.e., data for 8 VT buildings in the category 'Athletic').
     
     The JSON.parse() method parses the JSON data into an Array. Each element of the Array contains a JavaScript object (i.e., VT building).
     */
    var jsonData = JSON.parse(document.getElementById("jsonCategoryResult").value);

    // Obtain the number of VT buildings (JavaScript objects) in the given category
    var numberOfBuildings = jsonData.length;

    // Instantiate a new InfoWindow object to display the VT building's name when the pin marker is clicked
    var infoWindow = new google.maps.InfoWindow();

    j = 0;

    // Iterate for all VT buildings in the given category (e.g., Academic, Athletic, Research)
    while (j < numberOfBuildings) {

        var marker = null;

        // Instantiate a new pin marker and dress it up with the VT building's properties
        marker = new google.maps.Marker({
            position: new google.maps.LatLng(jsonData[j].latitude, jsonData[j].longitude),
            map: map,
            title: jsonData[j].name
        });

        // Place the newly created pin marker on the VT campus map
        marker.setMap(map);

        // Attach an event handler to the pin marker to display the VT building's name when the pin marker is clicked
        google.maps.event.addListener(marker, "click", function () {
            infoWindow.setContent(this.get('title'));
            infoWindow.open(map, this);
        });

        j++;
    }

}

// Draws the route on map showing directions to go from one VT building to another
function drawRoute() {

    // Identify the VT campus map as the Map to display Directions on
    directionsDisplay.setMap(map);

    // Since the DirectionsRequest object must be of type 'literal', we convert lat and long numbers to String type.

    /******************************* Start Geolocation Determination *******************************/

    // Obtain the starting Latitude as String from the hidden input element with id="startLocationLat" in LocationOnMap.xhtml
    var startingLatitudeAsString = document.getElementById("startLocationLat").value.toString();

    // Obtain the starting Longitude as String from the hidden input element with id="startLocationLong" in LocationOnMap.xhtml
    var startingLongitudeAsString = document.getElementById("startLocationLong").value.toString();

    // Instantiate the starting geolocation object for obtaining directions FROM
    var startGeolocation = new google.maps.LatLng(startingLatitudeAsString, startingLongitudeAsString);

    /**************************** Destination Geolocation Determination ****************************/

    // Obtain the destination Latitude as String from the hidden input element with id="destinationLocationLat" in LocationOnMap.xhtml
    var destinationLatitudeAsString = document.getElementById("destinationLocationLat").value.toString();

    // Obtain the destination Longitude as String from the hidden input element with id="destinationLocationLong" in LocationOnMap.xhtml
    var destinationLongitudeAsString = document.getElementById("destinationLocationLong").value.toString();

    // Instantiate the ending geolocation object for obtaining directions TO
    var endGeolocation = new google.maps.LatLng(destinationLatitudeAsString, destinationLongitudeAsString);

    /********************************** Travel Mode Determination **********************************/

    // Obtain the selected Travel Mode from the hidden input element with id="travelMode" in LocationOnMap.xhtml
    var selectedTravelMode = document.getElementById('travelMode').value;

    /***************************** Directions Request Object Creation ******************************/

    // Create a DirectionsRequest object named 'request' with the following properties as key:value pairs
    var request = {
        origin: startGeolocation,
        destination: endGeolocation,
        travelMode: google.maps.TravelMode[selectedTravelMode]
    };

    /***************************** Obtaining and Displaying Directions *****************************/

    /*
     "To use directions in the Google Maps JavaScript API, create an object of type DirectionsService
     and call DirectionsService.route() to initiate a request to the Directions service, passing it a 
     DirectionsRequest object literal containing the input terms and a callback method to execute upon
     receipt of the 'response'." [Google]
     
     Values of the 'response' and 'status' parameters of the callback method are returned from the
     Google Maps Directions API.
     
     status   --> must be okay if the directions can be computed by the Google Maps Directions API
     response --> contains the requested directions
     */
    directionsService.route(request, function (response, status) {

        // The operator === tests for equal value and equal type
        if (status === google.maps.DirectionsStatus.OK) {

            // If DirectionsStatus is okay, then display the route for the directions on map
            directionsDisplay.setDirections(response);
        }
    });
}
