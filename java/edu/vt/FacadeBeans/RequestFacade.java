/*
 * Created by Osman Balci and Team 7 on 2023.4.26
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */
package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.Request;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall not be maintained.
@Stateless
public class RequestFacade extends AbstractFacade<Request> {
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
    public RequestFacade() {
        super(Request.class);
    }

    /*
     * Finds all the requests of an event
     */
    public List<Request> findByEvent(Integer eventKey) {
        return entityManager.createNamedQuery("Request.findByEvent")
                .setParameter("eventId", eventKey)
                .getResultList();
    }

}
