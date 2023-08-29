/*
 * Created by Osman Balci and Team 7 on 2023.4.26
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */
package edu.vt.FacadeBeans;

import edu.vt.EntityBeans.Car;
import edu.vt.EntityBeans.Request;
import edu.vt.globals.Methods;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

// @Stateless annotation implies that the conversational state with the client shall not be maintained.
@Stateless
public class CarFacade extends AbstractFacade<Car> {
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
    public CarFacade() {
        super(Car.class);
    }

    /*
     * Finds a car from the user
     */
    public Car findByUser(Integer userKey) {

        List<Car> cars = (List<Car>) entityManager.createNamedQuery("Car.findByUserId")
                .setParameter("userId", userKey)
                .getResultList();
        return cars.size() > 0 ? cars.get(0) : null;
    }

}
