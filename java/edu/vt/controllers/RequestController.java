/*
 * Created by Team 7 on 2023.4.28
 * Copyright © 2023 Team 7. All rights reserved.
 */
package edu.vt.controllers;

import edu.vt.EntityBeans.Event;
import edu.vt.EntityBeans.Request;
import edu.vt.EntityBeans.User;
import edu.vt.FacadeBeans.RequestFacade;
import edu.vt.controllers.util.JsfUtil;
import edu.vt.controllers.util.JsfUtil.PersistAction;
import edu.vt.globals.Methods;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@SessionScoped
@Named("requestController")
public class RequestController implements Serializable {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */
    private List<Request> listOfRequests = null;
    private Request selected;

    @EJB
    private RequestFacade requestFacade;

    @Inject
    private EventController eventController;

    @Inject
    private UserController userController;

    @Inject
    private DirectionsApiController directionsApiController;

    /*
    =========================
    Getter and Setter Methods
    =========================
     */

    public List<Request> getListOfRequests() {
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        User signedInUser = (User) sessionMap.get("user");
        if(signedInUser.getEventId() == null){
            return null;
        }
        Integer eventKey = signedInUser.getEventId().getId();
        listOfRequests = requestFacade.findByEvent(eventKey);
        return listOfRequests;
    }

    public int getNumRequests() {
        return getListOfRequestsByEvent(eventController.getSelected()).size();
    }

    public List<Request> getListOfRequestsByEvent(Event event) {
        return requestFacade.findByEvent(event.getId());
    }

    public Request getSelected() {
        return selected;
    }

    public void setSelected(Request selected) {
        this.selected = selected;
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
     *   Cancel and Display Index.xhtml   *
     *************************************
     */
    public String cancel() {
        // Unselect previously selected Request object
        selected = null;
        return "/index";
    }

    /*
     * Correctly display the names of a request with the username
     */
    public String displayNames(Request request) {
        return request.getUserId().getFirstName() + " " + request.getUserId().getLastName()
                + " (" + request.getUserId().getUsername() + ")";
    }

    public String startingLocation(Request request) {
        return request.getDirection().equals("To Event") ? request.getAddress() : request.getEventId().getAddress();
    }

    public String destinationLocation(Request request) {
        return request.getDirection().equals("From Event") ? request.getAddress() : request.getEventId().getAddress();
    }

    public String passengerPhoneNumber(Request request) {
        return request.getUserId().getCellPhoneNumber().isEmpty() ? "Phone Number Unavailable" : request.getUserId().getCellPhoneNumber();
    }

    public String startingLocationName(Request request) {
        return request.getDirection().equals("To Event") ? "Starting Location" : request.getEventId().getName();
    }

    public String destinationLocationName(Request request) {
        return request.getDirection().equals("From Event") ? "Destination Location" : request.getEventId().getName();
    }

    /*
     * Get the distance from event for request
     */
    public String distance(Request request) {
        return directionsApiController.distance(startingLocation(request), destinationLocation(request));
    }

    /*
     * Creates a new ride then redirects
     */
    public String requestRide() {
        Methods.preserveMessages();
        create();
        return "/passenger/RideRequested?faces-redirect=true";
    }

    /*
    *******************************
    Prepare to Create a New Request
    *******************************
    */
    public void prepareCreate() {
        selected = new Request();
        selected.setNumPassengers(1);
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        User signedInUser = (User) sessionMap.get("user");
        selected.setUserId(signedInUser);
        selected.setDirection("To Event");
        selected.setAddress(userController.getLocation(selected.getUserId()));
    }

    /*
    ************************************
    CREATE a New Request in the Database
    ************************************
     */
    public void create() {
        Methods.preserveMessages();

        selected.setTime(new Timestamp(System.currentTimeMillis()));
        selected.setEventId(eventController.getSelected());

        System.out.println(distance(selected));

        persist(PersistAction.CREATE,"Ride request submitted!");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The CREATE operation is successfully performed.
            selected = null;        // Remove selection
            listOfRequests = null;    // Invalidate listOfRequests to trigger re-query.
        }
    }

    /*
    ***************************************
    UPDATE Selected Request in the Database
    ***************************************
     */
    public void update() {
        Methods.preserveMessages();

        persist(PersistAction.UPDATE,"Ride Updated!");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The UPDATE operation is successfully performed.
            selected = null;        // Remove selection
            listOfRequests = null;    // Invalidate listOfRequests to trigger re-query.
        }
    }

    /*
    *****************************************
    DELETE Selected Request from the Database
    *****************************************
     */
    public void destroy() {
        Methods.preserveMessages();

        persist(PersistAction.DELETE,"Request successfully deleted");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The DELETE operation is successfully performed.
            selected = null;        // Remove selection
            listOfRequests = null;    // Invalidate listOfRequests to trigger re-query.
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
                    
                     requestFacade inherits the edit(selected) method from the AbstractFacade class.
                     */
                    requestFacade.edit(selected);
                } else {
                    /*
                     -----------------------------------------
                     Perform DELETE operation in the database.
                     -----------------------------------------
                     The remove(selected) method performs the DELETE operation of the "selected"
                     object in the database.
                    
                     requestFacade inherits the remove(selected) method from the AbstractFacade class.
                     */
                    requestFacade.remove(selected);
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
