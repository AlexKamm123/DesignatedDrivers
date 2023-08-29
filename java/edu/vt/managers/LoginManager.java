/*
 * Created by Osman Balci on 2023.2.17
 * Copyright © 2023 Osman Balci. All rights reserved.
 */
package edu.vt.managers;

import edu.vt.EntityBeans.User;
import edu.vt.FacadeBeans.UserFacade;
import edu.vt.controllers.EmailController;
import edu.vt.controllers.TextMessageController;
import edu.vt.globals.Methods;
import edu.vt.globals.Password;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.MessagingException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Random;

@Named("loginManager")
@SessionScoped
public class LoginManager implements Serializable {
    /*
    ===============================
    Instance Variables (Properties)
    ===============================
     */
    private String username;
    private String password;

    private String generatedCodeFor2FA;
    private String userEnteredCodeFor2FA;

    /*
    The @EJB annotation directs the EJB Container Manager to inject (store) the object reference of the
    UserFacade bean into the instance variable 'userFacade' after it is instantiated at runtime.
     */
    @EJB
    private UserFacade userFacade;

    /*
    The @Inject annotation directs the CDI Container Manager to inject (store) the object reference of the
    CDI container-managed EmailController bean into the instance variable 'emailController' after it is instantiated at runtime.
     */
    @Inject
    private EmailController emailController;

    /*
    The @Inject annotation directs the CDI Container Manager to inject (store) the object reference of the
    CDI container-managed TextMessageController bean into the instance variable 'textMessageController' after it is instantiated at runtime.
     */
    @Inject
    private TextMessageController textMessageController;

    /*
    =========================
    Getter and Setter Methods
    =========================
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserEnteredCodeFor2FA() {
        return userEnteredCodeFor2FA;
    }

    public void setUserEnteredCodeFor2FA(String userEnteredCodeFor2FA) {
        this.userEnteredCodeFor2FA = userEnteredCodeFor2FA;
    }

    /*
    ================
    Instance Methods
    ================
    */

    // Generate a random integer number between 100000 and 999999
    public String getRandomCodeString() {
        Random random = new Random();
        int randomNumber = random.nextInt(899999) + 100000;
        return String.format("%06d", randomNumber);
    }

    // Authenticate user with the random code sent via email or SMS
    public void authenticateUserWithCode() {
        Methods.preserveMessages();
        if (userEnteredCodeFor2FA.equals(generatedCodeFor2FA)) {
            userEnteredCodeFor2FA = null;
            User user = userFacade.findByUsername(username);
            // Initialize the session map with user properties of interest in the method below
            initializeSessionMap(user);
            redirectToShowJSFpage("/userAccount/Profile.xhtml");
        } else {
            Methods.showMessage("Fatal Error", "Code Does Not Match!",
                    "Entered code " + userEnteredCodeFor2FA + " does not match!");
        }
    }

    /*
     ****************************
     *   SIGN IN (LOGIN) User   *
     ****************************
     */
    public void loginUser() {
        // Since we will redirect to show the Profile page, invoke preserveMessages()
        Methods.preserveMessages();

        // Obtain the object reference of the User object from the entered username
        User user = userFacade.findByUsername(username);
        if (user == null) {
            Methods.showMessage("Fatal Error", "Unknown Username!",
                    "Entered username " + username + " does not exist!");
        } else {
            String actualUsername = user.getUsername();
            if (actualUsername.equals(username)) {
                /*
                 Call the getter method to obtain the user's coded password stored in the database.
                 The coded password contains the following parts:
                    "algorithmName":"PBKDF2_ITERATIONS":"hashSize":"salt":"hash"
                 */
                String codedPassword = user.getPassword();

                // Call the getter method to get the password entered by the user
                String enteredPassword = getPassword();

                /*
                 Obtain the user's password String containing the following parts from the database
                      "algorithmName":"PBKDF2_ITERATIONS":"hashSize":"salt":"hash"
                 Extract the actual password from the parts and compare it with the password String
                 entered by the user by using Key Stretching to prevent brute-force attacks.
                 */
                try {
                    if (Password.verifyPassword(enteredPassword, codedPassword)) {
                        // Verification Successful: Entered password = User's actual password

                        switch (user.getTwoFactorAuthenticationStatus()) {
                            // 2FA is turned off
                            case 0:
                                // Initialize the session map with user properties of interest in the method below
                                initializeSessionMap(user);
                                redirectToShowJSFpage("/userAccount/Profile.xhtml");
                                break;
                            // Send 2FA Code via Email
                            case 1:
                                generatedCodeFor2FA = getRandomCodeString();

                                String emailAddress = user.getEmail();
                                emailController.setEmailTo(emailAddress);
                                emailController.setEmailSubject("Your Authentication Code");
                                emailController.setEmailBody("Your Authentication Code: " + generatedCodeFor2FA);
                                try {
                                    emailController.sendEmail();
                                } catch (MessagingException e) {
                                    Methods.showMessage("Fatal Error", "Unable to Send Email",
                                            "Unable to send authentication code to " + emailAddress);
                                }

                                redirectToShowJSFpage("/signIn/TwoFactorAuthentication.xhtml");
                                break;
                            // Send 2FA Code via SMS
                            case 2:
                                generatedCodeFor2FA = getRandomCodeString();

                                String cellPhoneNumber = user.getCellPhoneNumber();
                                textMessageController.setCellPhoneNumber(cellPhoneNumber);

                                String cellPhoneCarrier = user.getCellPhoneCarrier();
                                textMessageController.setCellPhoneCarrierDomain(cellPhoneCarrier);

                                textMessageController.setMmsTextMessage("Your Authentication Code: " + generatedCodeFor2FA);
                                try {
                                    textMessageController.sendTextMessage();
                                } catch (MessagingException e) {
                                    Methods.showMessage("Fatal Error", "Unable to Send SMS",
                                            "Unable to send authentication code to " + cellPhoneNumber);
                                }



                                redirectToShowJSFpage("/signIn/TwoFactorAuthentication.xhtml");
                                break;
                            default:
                                // Take no action
                        }
                    } else {
                        Methods.showMessage("Fatal Error", "Invalid Password!",
                                "Please Enter a Valid Password!");
                    }
                } catch (Password.CannotPerformOperationException | Password.InvalidHashException ex) {
                    Methods.showMessage("Fatal Error",
                            "Password Manager was unable to perform its operation!",
                            "See: " + ex.getMessage());
                }
            } else {
                Methods.showMessage("Fatal Error", "Invalid Username!",
                        "Entered Username is Unknown!");
            }
        }
    }

    /*
     **********************************************************************
     This method is used to show a JSF (XHTML) page when the page cannot be
     returned, e.g., return "/userAccount/Profile?faces-redirect=true";
     because the user's session is not yet established or is invalidated.
     **********************************************************************
     */
    public void redirectToShowJSFpage(String jsfPageUri) {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        /*
         getRequestContextPath() returns the URI of the webapp directory of the application.
         Obtain the URI of the JSF (XHTML) page to redirect to with respect to webapp directory.
         Example jsfPageUri: "/userAccount/Profile.xhtml"
         */
        String redirectPageURI = externalContext.getRequestContextPath() + jsfPageUri;

        // Execute the redirect to show the JSF (XHTML) page
        try {
            externalContext.redirect(redirectPageURI);
        } catch (IOException e) {
            Methods.showMessage("Fatal Error", "Unable to Navigate",
                    "External context redirect failed!");
        }
    }

    /*
    ******************************************************************
    Initialize the Session Map to Hold Session Attributes of Interests 
    ******************************************************************
     */
    public void initializeSessionMap(User user) {

        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();

        // Store the object reference of the signed-in user
        sessionMap.put("user", user);

        // Store the First Name of the signed-in user
        sessionMap.put("first_name", user.getFirstName());

        // Store the Last Name of the signed-in user
        sessionMap.put("last_name", user.getLastName());

        // Store the Username of the signed-in user
        sessionMap.put("username", username);

        // Store signed-in user's Primary Key in the database
        sessionMap.put("user_id", user.getId());
    }

}
