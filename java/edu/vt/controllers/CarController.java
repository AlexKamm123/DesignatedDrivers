/*
 * Created by Team 7 on 2023.4.30
 * Copyright © 2023 Team 7. All rights reserved.
 */
package edu.vt.controllers;

import edu.vt.EntityBeans.Car;
import edu.vt.EntityBeans.User;
import edu.vt.FacadeBeans.CarFacade;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


@SessionScoped
@Named("carController")
public class CarController implements Serializable {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */
    private Car selected;

    @EJB
    private CarFacade carFacade;

    @Inject
    private UserController userController;

    // keeps track of the current number of people in a ride
    private int currPeople;

    /*
    =========================
    Getter and Setter Methods
    =========================
     */

    public Car getSelected() {
        return selected;
    }

    public void setSelected(Car selected) {
        this.selected = selected;
    }

    public int getCurrPeople() { return currPeople; }

    public void setCurrPeople(int currPeople) { this.currPeople = currPeople; }

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

    public String editCar() {
        selected = carFacade.findByUser(Methods.getSignedInUser().getId());

        prepareCreate();

        return "/car/AddCar?faces-redirect=true";
    }

    /*
    ***************************
    Prepare to Create a New Car
    ***************************
    */
    public void prepareCreate() {
        if (selected != null) {
            return;
        }
        selected = new Car();
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        User signedInUser = (User) sessionMap.get("user");
        selected.setUserId(signedInUser);
    }

    /*
    ************************************
    CREATE a New Car in the Database
    ************************************
     */
    public String create() {
        Methods.preserveMessages();

        persist(PersistAction.CREATE,"Car Edited!");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The CREATE operation is successfully performed.
            selected = null;        // Remove selection
        }
        return "/userAccount/Profile";
    }

    /*
    ***************************************
    UPDATE Selected Car in the Database
    ***************************************
     */
    public void update() {
        Methods.preserveMessages();

        persist(PersistAction.UPDATE,"Car Updated!");

        if (!JsfUtil.isValidationFailed()) {
            // No JSF validation error. The UPDATE operation is successfully performed.
            selected = null;        // Remove selection
        }
    }

    /*
    *****************************************
    DELETE Selected Car from the Database
    *****************************************
     */
    public void destroy() {
        Methods.preserveMessages();

        persist(PersistAction.DELETE,"Car Deleted!");

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

                     carFacade inherits the edit(selected) method from the AbstractFacade class.
                     */
                    carFacade.edit(selected);
                } else {
                    /*
                     -----------------------------------------
                     Perform DELETE operation in the database.
                     -----------------------------------------
                     The remove(selected) method performs the DELETE operation of the "selected"
                     object in the database.

                     carFacade inherits the remove(selected) method from the AbstractFacade class.
                     */
                    carFacade.remove(selected);
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
