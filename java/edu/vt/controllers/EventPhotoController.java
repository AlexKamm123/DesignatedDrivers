/*
 * Created by Osman Balci and Team 7 on 2023.5.1
 * Copyright © 2023 Osman Balci and Team 7. All rights reserved.
 */
package edu.vt.controllers;

import edu.vt.EntityBeans.User;
import edu.vt.globals.Constants;
import edu.vt.globals.Methods;
import org.primefaces.model.file.UploadedFile;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.util.Map;
import java.util.UUID;

@Named("eventPhotoController")
// this bean is sessionScoped to store data for entire user session
@SessionScoped
public class EventPhotoController implements Serializable {

    @Inject
    private EventController eventController;

    // stores the uploaded file
    private UploadedFile uploadedFile;

    /*
    ************************
    Handle Event Photo Upload
    ************************
     */
    public String upload() {

        // preserving messages
        Methods.preserveMessages();

        // checking for null uploaded file
        if(uploadedFile == null){
            Methods.showMessage("Information", "No File Selected!",
                    "You need to choose a file first before clicking Upload.");
            return "";
        }

        String file = uploadedFile.getFileName();



        // Check if a file is selected
        if (file.length() <= 1) {
            Methods.showMessage("Information", "No File Selected!",
                    "You need to choose a file first before clicking Upload.");
            return "";
        }

        String fileExtensionInCaps = file.substring(file.lastIndexOf(".") + 1, file.length()).toUpperCase();


        switch (fileExtensionInCaps) {
            case "JPG":
            case "JPEG":
            case "PNG":
            case "GIF":
                // File is an acceptable image type
                break;
            default:
                Methods.showMessage("Fatal Error", "Unrecognized File Type!",
                        "Selected file type is not a JPG, JPEG, PNG, or GIF!");
                return "";
        }

        storePhotoFile(uploadedFile);

        // Redirect to show the Create page
        return "/organizer/Create?faces-redirect=true";
    }

    /*
    *******************************************************
    Stores a Event photo
    *******************************************************
     */
    public void storePhotoFile(UploadedFile file) {

        // preserving messages
        Methods.preserveMessages();

        try {
            // Obtain the object reference of the signed-in User object
            Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            User signedInUser = (User) sessionMap.get("user");

            // Obtain the uploaded file's MIME file type
            String mimeFileType = file.getContentType();

            // If it is an image file, obtain its file extension; otherwise, set JPEG as the file extension anyway.
            String fileExtension = mimeFileType.startsWith("image/") ? mimeFileType.subSequence(6, mimeFileType.length()).toString() : "jpeg";

            // Generate a random UUID string as the filename
            String photoFullFilename = UUID.randomUUID().toString() + "." + fileExtension;
            eventController.getSelected().setPhoto(photoFullFilename);
            /*
            InputStream is an abstract class, which is the superclass of all classes representing
            an input stream of bytes. It is imported as: import java.io.InputStream;
            Convert the uploaded file into an input stream of bytes.
             */
            InputStream inputStream = file.getInputStream();

            // Write the uploaded file's input stream of bytes under the photo object's
            // filename using the inputStreamToFile method given below
            File uploadedFile = inputStreamToFile(inputStream, photoFullFilename);

            Methods.showMessage("Information", "Success!",
                    "Event Photo File is Successfully Uploaded!");

        } catch (IOException ex) {
            Methods.showMessage("Fatal Error",
                    "Something went wrong while storing the Event photo file!",
                    "See: " + ex.getMessage());
        }

    }

    /*
    ***************************************************
    Write Given InputStream into a File with Given Name
    ***************************************************
     */
    /**
     * @param inputStream of bytes to be written into file with name targetFilename
     * @return the created file targetFile
     */
    private File inputStreamToFile(InputStream inputStream, String targetFilename) throws IOException {

        File targetFile = null;

        try {
            /*
            inputStream.available() returns an estimate of the number of bytes that can be read from
            the inputStream without blocking by the next invocation of a method for this input stream.
            A memory buffer of bytes is created with the size of estimated number of bytes.
             */
            byte[] buffer = new byte[inputStream.available()];

            // Read the bytes of data from the inputStream into the created memory buffer.
            inputStream.read(buffer);

            // Create a new empty file with the given name targetFilename in the DesignatedDriver directory
            targetFile = new File(Constants.FILES_ABSOLUTE_PATH, targetFilename);

            // A file OutputStream is an output stream for writing data to a file
            OutputStream outStream;

            /*
            FileOutputStream is intended for writing streams of raw bytes such as image data.
            Create a new FileOutputStream for writing to the empty targetFile
             */
            outStream = new FileOutputStream(targetFile);

            // Create the targetFile in the UserPhotoStorage directory with the inputStream given
            outStream.write(buffer);

            // Close the output stream and release any system resources associated with it.
            outStream.close();

        } catch (IOException ex) {
            Methods.showMessage("Fatal Error",
                    "Something went wrong in input stream to file!",
                    "See: " + ex.getMessage());
        }
        // Return the created targetFile
        return targetFile;
    }

    public UploadedFile getUploadedFile() { return uploadedFile; }

    public void setUploadedFile(UploadedFile uploadedFile) { this.uploadedFile = uploadedFile; }
}
