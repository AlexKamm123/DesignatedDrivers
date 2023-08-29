/*
 * Created by Osman Balci and Team 7 on 2023.4.26
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */
package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.Event;
import edu.vt.EntityBeans.Request;
import edu.vt.EntityBeans.Ride;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.io.PushbackInputStream;
import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall not be maintained.
@Stateless
public class RideFacade extends AbstractFacade<Ride> {
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
    public RideFacade() {
        super(Ride.class);
    }

    /*
     * Gets the most recently created ride
     */
    public Ride findLatest() {
        List<Ride> rides = entityManager.createNamedQuery("Ride.findAll")
                .getResultList();

        return rides.size() > 0 ? rides.get(rides.size() - 1) : null;
    }

    /*
     * Finds the most recently created ride form a driver key
     */
    public Ride findLatestByDriver(Integer primaryKey) {
        List<Ride> rides = entityManager.createNamedQuery("Ride.findByDriver")
                .setParameter("driver", primaryKey)
                .getResultList();

        return rides.size() > 0 ? rides.get(rides.size() - 1) : null;
    }

    /*
     * Finds a ride that has a user involved
     */
    public Ride findLatestByUsername(String username) {
        username = "%" + username + "%";
        List<Ride> rides = entityManager.createNamedQuery("Ride.findByUsername")
                .setParameter("passenger", username)
                .getResultList();

        return rides.size() > 0 ? rides.get(rides.size() - 1) : null;
    }

}
