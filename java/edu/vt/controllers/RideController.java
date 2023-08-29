/*
 * Created by Osman Balci and Team 7 on 2023.4.28
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */
package edu.vt.controllers;

import edu.vt.EntityBeans.Request;
import edu.vt.EntityBeans.Ride;
import edu.vt.EntityBeans.User;
import edu.vt.FacadeBeans.CarFacade;
import edu.vt.FacadeBeans.RequestFacade;
import edu.vt.FacadeBeans.RideFacade;
import edu.vt.FacadeBeans.UserFacade;
import edu.vt.controllers.util.JsfUtil;
import edu.vt.controllers.util.JsfUtil.PersistAction;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import edu.vt.managers.LoginManager;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@SessionScoped
@Named("rideController")
public class RideController implements Serializable {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */
    private Ride selected;
    private ArrayList<Request> requests;
    private Ride rideForUser;
    @EJB
    private RideFacade rideFacade;
    @EJB
    private CarFacade carFacade;
    @EJB
    private UserFacade userFacade;

    @EJB
    RequestFacade requestFacade;
    @Inject
    private UserController userController;
    @Inject
    private RequestController requestController;
    @Inject
    private EventController eventController;
    @Inject
    private TextMessageController textMessageController;

    @Inject
    private PlacesApiController placesApiController;
    @Inject
    private CarController carController;

    @Inject
    private DirectionsApiController directionsApiController;

    // this is set to recently created ride to be deleted after requesting
    private Ride rideToBeDeleted;

    /*
    =========================
    Getter and Setter Methods
    =========================
     */

    public Ride getSelected() {
        return selected;
    }

    public void setSelected(Ride selected) {
        this.selected = selected;
    }

    public ArrayList<Request> getRequests() {
        return requests;
    }

    public void setRequests(ArrayList<Request> requests) {
        this.requests = requests;
    }

    public Ride getRideForUser() {
        rideForUser = rideFacade.findLatestByUsername(userController.getSignedInUser().getUsername());
        return rideForUser;
    }

    public void setRideForUser(Ride rideForUser) {
        this.rideForUser = rideForUser;
    }

    /*
    ================
    Instance Methods
    ================
    */

    /*
     ********************************
     *   Unselect Selected Object   *
     ********************************
     */
    public void unselect() {
        selected = null;
    }

    /*
     *************************************
     *   Cancel and Display List.xhtml   *
     *************************************
     */
    public String cancel() {
        // Unselect previously selected object if any
        selected = null;
        return "/index";
    }

    /*
     * Adds a request to the list of requests
     */
    public void addRequest(Request request) throws MessagingException {
        Methods.preserveMessages();
        // do nothing if the ride already has the request
        if (!requests.contains(request)) {
            // when the car cant hold that many people
            if(carController.getCurrPeople() + request.getNumPassengers() > selected.getCarId().getMaxOccupants()){
                Methods.showMessage("Warning",
                        "Your Car is Full!",
                        "Please dont exceed the max occupants of your vehicle. This can be changed" +
                                "in your user profile if needed.");
                return;
            }

            // add any request when size is zero
            if(requests.size() == 0){
                // increment number of ppl in car
                carController.setCurrPeople(carController.getCurrPeople() + request.getNumPassengers());
                requests.add(request);
            }
            // make sure the directions match
            else if(requests.get(0).getDirection().equals(request.getDirection())){
                carController.setCurrPeople(carController.getCurrPeople() + request.getNumPassengers());
                requests.add(request);
            }
            // throw message if the directions dont match
            else{
                Methods.showMessage("Warning",
                        "Please only add requests of the same type (to event or from event)!",
                        "Only add to requests to your trip that are going" +
                                " in the same direction (to the event or from the event)");
            }

        }
    }

    /*
     * Removes a request from the list of requests
     */
    public void removeRequest(Request request) {
        boolean happened = requests.remove(request);
        // if the request was actually removed, then decrement the number of people in car
        if(happened){
            carController.setCurrPeople(carController.getCurrPeople() - request.getNumPassengers());
        }
    }

    /*
     * When a driver clicks the start trip button
     */
    public String startTrip() {
        Methods.preserveMessages();
        // direction is used for the url
        directionsApiController.setRideDirection(requests.get(0).getDirection());
        // creates ride
        create();
        // this ride will need to be deleted when finished
        rideToBeDeleted = rideFacade.findLatest();
        // redirect
        return "/driver/TripStarted?faces-redirect=true";
    }

    public Boolean requestAdded(Request request) {
        return requests.contains(request);
    }

    public String requestAddedImage(Request request) {
        return requestAdded(request) ? Constants.PHOTOS_URI + "green_checkmark.png" : Constants.PHOTOS_URI + "x_circle.png";
    }

    public Ride findByDriver(Integer driverId) {
        return rideFacade.findLatestByDriver(driverId);
    }

    /*
     * When a driver has completed a trip
     * It will null values of user and then redirect driver
     */
    public String tripCompleted(){
        // reset ids of user
        for (int i = 0; i < requests.size(); i++){
            try {
                requests.get(i).getUserId().setRideId(null);
                requests.get(i).getUserId().setEventId(null);
                userFacade.edit(requests.get(i).getUserId());
            }
            catch (EJBException e){
                Methods.showMessage("Fatal Error",
                        "Something went wrong while updating user's profile!",
                        "See: " + e.getMessage());
            }
        }
        requests.clear(); // clear out the current requests
        rideFacade.remove(rideToBeDeleted); // deletes ride from database
        // need to get ready for next ride
        prepareCreate();
        return "/driver/List?faces-redirect=true";
    }

    /*
    *******************************
    Prepare to Create a New Ride
    *******************************
    */
    public void prepareCreate() {
        // sets number of people in car to 0
        carController.setCurrPeople(0);
        selected = new Ride();
        requests = new ArrayList<>();
        User signedInUser = Methods.getSignedInUser();
        selected.setCarId(carFacade.findByUser(signedInUser.getId()));
    }

    /*
     * when the user clicks the refresh button
     */
    public void refresh() throws IOException {
        Methods.preserveMessages();



        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        /*
         getRequestContextPath() returns the URI of the webapp directory of the application.
         Obtain the URI of the JSF (XHTML) page to redirect to with respect to webapp directory.
         Example jsfPageUri: "/userAccount/Profile.xhtml"
         */
        String redirectPageURI = externalContext.getRequestContextPath() + "/passenger/RideRequested.xhtml";

        // Execute the redirect to show the JSF (XHTML) page

        externalContext.redirect(redirectPageURI);
    }

    /*
    ************************************
    CREATE a New Ride in the Database
    ************************************
     */
    public void create() {
        Methods.preserveMessages();

        selected.setTime(new Timestamp(System.currentTimeMillis()));
        selected.setNumOccupants(requests.size());
        selected.setEventId(requests.get(0).getEventId());

        // Set fields from array of requests
        StringBuilder sbRiders = new StringBuilder(); // Riders
        StringBuilder sbDA = new StringBuilder(); // Destination Address
        StringBuilder sbWaypoints = new StringBuilder(); // Waypoints
        StringBuilder sbAIds = new StringBuilder(); // Address Ids

        // Set text message content
        StringBuilder sbTM = new StringBuilder();
        sbTM.append(selected.getCarId().getUserId().getFirstName());
        sbTM.append(" has accepted your request!\n\n");
        sbTM.append("To view driver details and ETA, click the following link:\n");
        sbTM.append(Constants.BASE_URI);
        sbTM.append("passenger/RideRequested.xhtml");

        for (int i = 0; i < requests.size(); i++) {

            try {
                // Send text message
                textMessageController.setCellPhoneNumber(requests.get(i).getUserId().getCellPhoneNumber());
                textMessageController.setCellPhoneCarrierDomain(requests.get(i).getUserId().getCellPhoneCarrier());
                textMessageController.setMmsTextMessage(sbTM.toString());
                textMessageController.sendTextMessage();
            }
            catch (Exception e) {

            }

            // Add addresses, waypoints, etc.
            String address = requests.get(i).getAddress();
            placesApiController.places(address);
            String addressId = placesApiController.getListOfPlacesFound().get(0).getId();
            if (i == 0) {
                sbRiders.append(requests.get(i).getUserId().getUsername());
                sbWaypoints.append(address);
                sbAIds.append(addressId);
            } else {
                sbRiders.append("," + requests.get(i).getUserId().getUsername());
                sbWaypoints.append("#" + address);
                sbAIds.append("," + addressId);
            }
        }

        sbDA.append(selected.getEventId().getAddress());

        selected.setRiders(sbRiders.toString());
        selected.setDestinationAddress(sbDA.toString());
        selected.setWaypoints(sbWaypoints.toString());
        selected.setAddressIds(sbAIds.toString());

        persist(PersistAction.CREATE,"Ride was Successfully Created!");

        Ride latest = rideFacade.findLatest();

        for (int i = 0; i < requests.size(); i++){
            try {
                requests.get(i).getUserId().setRideId(latest);
                userFacade.edit(requests.get(i).getUserId());
            }
            catch (EJBException e){
                Methods.showMessage("Fatal Error",
                        "Something went wrong while updating user's profile!",
                        "See: " + e.getMessage());
            }
        }

        // We would like to get the user's current address here, but we don't have a valid HTTPS certificate
        // Setting currAddr to Torgerson Hall instead...
        String currAddr = "620 Drillfield Dr, Blacksburg, VA 24060";
        String buffer = directionsApiController.directions(currAddr, latest);
        String[] buffers = buffer.split("&");
        selected.setWaypoints(buffers[0]);
        selected.setAddressIds(buffers[1]);
        latest.setTime(directionsApiController.getEta());
        rideFacade.edit(latest);

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The CREATE operation is successfully performed.
            selected = null;        // Remove selection
        }

        for (int i = 0; i < requests.size(); i++) {
            requestFacade.remove(requests.get(i));
        }

    }

    /*
    ***************************************
    UPDATE Selected Ride in the Database
    ***************************************
     */
    public void update() {
        Methods.preserveMessages();

        persist(PersistAction.UPDATE,"Ride was Successfully Updated!");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The UPDATE operation is successfully performed.
            selected = null;        // Remove selection
        }
    }

    /*
    *****************************************
    DELETE Selected Ride from the Database
    *****************************************
     */
    public void destroy() {
        Methods.preserveMessages();

        persist(PersistAction.DELETE,"Ride was Successfully Completed!");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The DELETE operation is successfully performed.
            selected = null;        // Remove selection
        }
    }

    /*
     **********************************************************************************************
     *   Perform CREATE, UPDATE (EDIT), and DELETE (DESTROY, REMOVE) Operations in the Database   *
     **********************************************************************************************
     */
    /**
     * @param persistAction refers to CREATE, UPDATE (Edit) or DELETE action
     * @param successMessage displayed to inform the user about the result
     */
    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            try {
                if (persistAction != PersistAction.DELETE) {
                    /*
                     -------------------------------------------------
                     Perform CREATE or EDIT operation in the database.
                     -------------------------------------------------
                     The edit(selected) method performs the SAVE (STORE) operation of the "selected"
                     object in the database regardless of whether the object is a newly
                     created object (CREATE) or an edited (updated) object (EDIT or UPDATE).
                    
                     publicFavoriteFacade inherits the edit(selected) method from the AbstractFacade class.
                     */
                    rideFacade.edit(selected);
                } else {
                    /*
                     -----------------------------------------
                     Perform DELETE operation in the database.
                     -----------------------------------------
                     The remove(selected) method performs the DELETE operation of the "selected"
                     object in the database.
                    
                     BookFacade inherits the remove(selected) method from the AbstractFacade class.
                     */
                    rideFacade.remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex,"A persistence error occurred.");
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex,"A persistence error occurred.");
            }
        }
    }

}
