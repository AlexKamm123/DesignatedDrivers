/*
 * Created by Osman Balci and Team 7 on 2023.5.1
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */
package edu.vt.controllers;

import edu.vt.EntityBeans.Event;
import edu.vt.EntityBeans.User;
import java.lang.Math;
import edu.vt.EntityBeans.UserPhoto;
import edu.vt.FacadeBeans.CarFacade;
import edu.vt.FacadeBeans.EventFacade;
import edu.vt.FacadeBeans.UserFacade;
import edu.vt.FacadeBeans.UserPhotoFacade;
import edu.vt.controllers.util.JsfUtil;
import edu.vt.controllers.util.JsfUtil.PersistAction;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.shaded.json.JSONArray;
import org.primefaces.shaded.json.JSONObject;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


@SessionScoped
@Named("eventController")
public class EventController implements Serializable {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */
    // all the events
    private List<Event> listOfPublicEvents = null;
    // the events that belong to organizer
    private List<Event> listOfMyEvents = null;
    private Event selected;
    private String userPasswordEntered = "";
    private String driverPasswordEntered = "";
    // helper dates
    private java.util.Date startDate;
    private java.util.Date endDate;
    private User user;

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    eventFacade bean into the instance variable 'eventFacade' after it is instantiated at runtime.
     */
    @EJB
    private EventFacade eventFacade;
    @Inject
    private UserController userController;
    @Inject
    private CarController carController;
    @EJB
    private CarFacade carFacade;
    /*
    =========================
    Getter and Setter Methods
    =========================
     */

    public List<Event> getListOfPublicEvents() {
        listOfPublicEvents = eventFacade.findPublicEvents();
        return listOfPublicEvents;
    }

    public List<Event> getListOfMyEvents() {
        listOfMyEvents = eventFacade.findMyEvents(userController.getSignedInUser().getId());
        return listOfMyEvents;
    }

    public void setListOfPublicEvents(List<Event> listOfPublicEvents) {
        this.listOfPublicEvents = listOfPublicEvents;
    }

    public void setListOfMyEvents(List<Event> listOfMyEvents) {
        this.listOfMyEvents = listOfMyEvents;
    }

    public EventFacade getEventFacade() {
        return eventFacade;
    }

    public void setEventFacade(EventFacade eventFacade) {
        this.eventFacade = eventFacade;
    }

    public UserController getUserController() {
        return userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public Event getSelected() {
        return selected;
    }

    public void setSelected(Event selected) {
        this.selected = selected;
    }

    public String getUserPasswordEntered() { return userPasswordEntered; }

    public void setUserPasswordEntered(String userPasswordEntered) { this.userPasswordEntered = userPasswordEntered; }

    public String getDriverPasswordEntered() { return driverPasswordEntered; }

    public void setDriverPasswordEntered(String driverPasswordEntered) { this.driverPasswordEntered = driverPasswordEntered; }

    /*
     * Converts SQL TimeStamp to java date to be displayed
     */
    public Date getStartDate() {
        if(selected != null){
            java.util.Date javaTime = new java.util.Date(selected.getStartTime().getTime());
            return javaTime;
        }
        return new Date();
    }

    /*
     sets the SQL timestamp to the passed in date
     */
    public void setStartDate(Date startDate) {
        if(selected != null){
            this.startDate = startDate;
            java.sql.Timestamp sqlTime = new java.sql.Timestamp(startDate.getTime());
            this.selected.setStartTime(sqlTime);
        }
    }

    /*
     * Converts SQL TimeStamp to java date to be displayed
     */
    public Date getEndDate() {
        if(selected != null){
            java.util.Date javaTime = new java.util.Date(selected.getEndTime().getTime());
            return javaTime;
        }
        return new Date();
    }

    /*
     sets the SQL timestamp to the passed in date
     */
    public void setEndDate(Date endDate) {
        if(selected != null){
            this.endDate = endDate;
            java.sql.Timestamp sqlTime = new java.sql.Timestamp(endDate.getTime());
            this.selected.setEndTime(sqlTime);
        }
    }

    /*
    ================
    Instance Methods
    ================
    */

    public User getUser(Event event) {
        return event.getUserId();
    }

    //Creates a random driver code, checking for existing codes and repeating until unique
    public String createDriverCode() {
        int upperBound = 999999;
        int lowerBound = 100000;
        int range = (upperBound - lowerBound) + 1;
        int random = (int)(Math.random() * range);
        String possible = String.valueOf(random);
        while (eventFacade.findByDriverCode(possible) != null) {
            random = (int)(Math.random() * range);
            possible = String.valueOf(random);
        }
        return possible;
    }

    //Creates a random passenger code, checking for existing codes and repeating until unique
    public String createPassengerCode() {
        int upperBound = 999999;
        int lowerBound = 100000;
        int range = (upperBound - lowerBound) + 1;
        int random = (int)(Math.random() * range);
        String possible = String.valueOf(random);
        while (eventFacade.findByPassengerCode(possible) != null) {
            random = (int)(Math.random() * range);
            possible = String.valueOf(random);
        }
        return possible;
    }

    // returns event image from a passed in event
    public String getImage(Event event) {
        if (event == null || event.getPhoto() == null) {
            return null;
        }
        return Constants.FILES_URI + event.getPhoto();
    }

    /*
     **************************************
     *   Unselect Selected Event Object   *
     **************************************
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
        // Unselect previously selected Event object if any
        selected = null;
        return "/index";
    }

    private Boolean eventNotStarted() {
        return  selected.getStartTime().compareTo(new Timestamp(System.currentTimeMillis())) > 0;
    }

    private Boolean eventEnded() {
        return selected.getEndTime().compareTo(new Timestamp(System.currentTimeMillis())) < 0;
    }

    /*
     ******************************************
     * Checks entered pass with user passwords
     * ****************************************
     */
    public String checkUserPassword() {
        Methods.preserveMessages();

        if (userPasswordEntered.isEmpty()) {
            Methods.showMessage("Error", "Enter your event password!", "");
            return "";
        }
        selected = eventFacade.findByPassengerCode(userPasswordEntered);
        if (selected == null) {
            Methods.showMessage("Error", "Incorrect event password!", "");
            return "";
        }

        if (eventNotStarted()) {
            Methods.showMessage("Error", "Event has not started yet!", "");
            return "";
        }

        if (eventEnded()) {
            Methods.showMessage("Error", "Event ended!", "");
            return "";
        }

        return "/passenger/RequestRide?faces-redirect=true";
    }

    /*
     * called when user clicks the request ride button for an event
     */
    public String requestRide() {
        // if the event hasn't started yet
        if (eventNotStarted()) {
            Methods.showMessage("Error", "Event has not started yet!", "");
            return "";
        }
        // if the event has ended already
        if (eventEnded()) {
            Methods.showMessage("Error", "Event ended!", "");
            return "";
        }
        // redirect
        return "/passenger/RequestRide?faces-redirect=true";
    }

    /*
     ********************************************
     *  checks entered pass with driver passwords
     * *******************************************
     */
    public String checkDriverPassword(){
        Methods.preserveMessages();

        if (driverPasswordEntered.isEmpty()) {
            Methods.showMessage("Error", "Enter your event password!", "");
            return "";
        }
        selected = eventFacade.findByDriverCode(driverPasswordEntered);
        if (selected == null) {
            Methods.showMessage("Error", "Incorrect event password!", "");
            return "";
        }
        if (carFacade.findByUser(Methods.getSignedInUser().getId()) == null) {
            Methods.showMessage("Information", "You must create a car before driving for an event.", "Redirecting you now!");
            return carController.editCar();
        }

        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        User signedInUser = (User) sessionMap.get("user");
        signedInUser.setEventId(selected);

        return "/driver/List?faces-redirect=true";
    }



    /*
    *****************************
    Prepare to Create a New Event
    *****************************
    */
    public void prepareCreate() {
        /*
        Instantiate a new Event object and store its object reference into
        instance variable 'selected'. The Event class is defined in Event.java
         */
        selected = new Event();
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        User signedInUser = (User) sessionMap.get("user");
        // set values of selected
        selected.setUserId(signedInUser);
        selected.setStartTime(new Timestamp(System.currentTimeMillis()));
        selected.setEndTime(new Timestamp(System.currentTimeMillis() + 3600000));
        selected.setAddressId(" ");
        selected.setDriverCode(createDriverCode());
        selected.setPassengerCode(createPassengerCode());
    }

    /*
    An enum is a special Java type used to define a group of constants.
    The constants CREATE, DELETE and UPDATE are defined as follows in JsfUtil.java

            public enum PersistAction {
                CREATE,
                DELETE,
                UPDATE
            }
     */

    /*
    **********************************
    CREATE a New Event in the Database
    **********************************
     */
    public void create() {
        Methods.preserveMessages();

        persist(PersistAction.CREATE,"Event was Successfully Created!");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The CREATE operation is successfully performed.
            selected = null;        // Remove selection
            listOfPublicEvents = null;    // Invalidate listOfPublicEvents to trigger re-query.
        }
    }

    /*
    *************************************
    UPDATE Selected Event in the Database
    *************************************
     */
    public void update() {
        Methods.preserveMessages();

        persist(PersistAction.UPDATE,"Event was Successfully Updated!");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The UPDATE operation is successfully performed.
            selected = null;        // Remove selection
            listOfPublicEvents = null;    // Invalidate listOfPublicEvents to trigger re-query.
        }
    }

    /*
    ***************************************
    DELETE Selected Event from the Database
    ***************************************
     */
    public void destroy() {
        Methods.preserveMessages();

        persist(PersistAction.DELETE,"Event was Successfully Deleted!");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The DELETE operation is successfully performed.
            selected = null;        // Remove selection
            listOfPublicEvents = null;    // Invalidate listOfPublicEvents to trigger re-query.
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
                    
                     eventFacade inherits the edit(selected) method from the AbstractFacade class.
                     */
                    eventFacade.edit(selected);
                } else {
                    /*
                     -----------------------------------------
                     Perform DELETE operation in the database.
                     -----------------------------------------
                     The remove(selected) method performs the DELETE operation of the "selected"
                     object in the database.
                    
                     eventFacade inherits the remove(selected) method from the AbstractFacade class.
                     */
                    eventFacade.remove(selected);
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
