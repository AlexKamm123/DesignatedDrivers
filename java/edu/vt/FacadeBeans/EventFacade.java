/*
 * Created by Osman Balci and Team 7 on 2023.4.26
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */
package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.Event;
import edu.vt.EntityBeans.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Time;
import java.util.List;
import java.sql.Timestamp;

// @Stateless annotation implies that the conversational state with the client shall not be maintained.
@Stateless
public class EventFacade extends AbstractFacade<Event> {
    /*
    ---------------------------------------------------------------------------------------------
    The EntityManager is an API that enables database CRUD (Create Read Update Delete) operations
    and complex database searches. An EntityManager instance is created to manage entities
    that are defined by a persistence unit. The @PersistenceContext annotation below associates
    the entityManager instance with the persistence unitName identified below.
    ---------------------------------------------------------------------------------------------
     */
    @PersistenceContext(unitName = "DesignatedDriversPU")
    private EntityManager entityManager;

    // Obtain the object reference of the EntityManager instance in charge of
    // managing the entities in the persistence context identified above.
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /*
    This constructor method invokes its parent AbstractFacade's constructor method,
    which in turn initializes its entity class type T and entityClass instance variable.
     */
    public EventFacade() {
        super(Event.class);
    }

    /*
     * Gets list of public events that haven't expired yet
     */
    public List<Event> findPublicEvents() {
        return entityManager.createNamedQuery("Event.findPublicEvents")
                .setParameter("visibility", "public")
                .setParameter("currentTime", new Timestamp(System.currentTimeMillis()))
                .getResultList();
    }

    /*
     * gets the event of an organization
     */
    public List<Event> findMyEvents(Integer primaryKey) {
        return entityManager.createNamedQuery("Event.findByUserId")
                .setParameter("userId", primaryKey)
                .getResultList();
    }

    /*
     * Gets events based on search string
     */
    public List<Event> nameQuery(String searchString) {
        // Place the % wildcard before and after the search string to search for it anywhere in the Book Title
        searchString = "%" + searchString + "%";
        // Conduct the search in a case-insensitive manner and return the results in a list.
        return getEntityManager().createQuery(
                        "SELECT e FROM Event e WHERE e.name LIKE :searchString")
                .setParameter("searchString", searchString)
                .getResultList();
    }

    /*
     * finds event based on passenger code
     */
    public Event findByPassengerCode(String passengerCode) {


        if (entityManager.createNamedQuery("Event.findByPassengerCode")
                .setParameter("passengerCode", passengerCode)
                .getResultList().isEmpty()) {

            // Return null if no fav object exists by the name of name
            return null;

        } else {

            // Return the Object reference of the fav object whose name is fullName
            return (Event) (entityManager.createNamedQuery("Event.findByPassengerCode")
                    .setParameter("passengerCode", passengerCode)
                    .getSingleResult());
        }
    }
    /*
     * Finds event based on driver code
     */
    public Event findByDriverCode(String driverCode) {


        if (entityManager.createNamedQuery("Event.findByDriverCode")
                .setParameter("driverCode", driverCode)
                .getResultList().isEmpty()) {

            // Return null if no fav object exists by the name of name
            return null;

        } else {

            // Return the Object reference of the fav object whose name is fullName
            return (Event) (entityManager.createNamedQuery("Event.findByDriverCode")
                    .setParameter("driverCode", driverCode)
                    .getSingleResult());
        }
    }
}
