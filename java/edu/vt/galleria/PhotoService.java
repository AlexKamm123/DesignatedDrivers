/*
 * Created by Osman Balci and Conrad MacKethan on 2023.04.26
 * Copyright © 2023 Osman Balci and Conrad MacKethan. All rights reserved.
 */

package edu.vt.galleria;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named(value = "photoService")
@ApplicationScoped
public class PhotoService {
    /*
    ============================
    Instance Variable (Property)
    ============================
     */
    private List<Photo> listOfPhotos;

    /*
    The PostConstruct annotation is used on a method that needs to be executed after
    dependency injection is done to perform any initialization. The initialization
    method init() is the first method invoked before this class is put into service.
     */
    @PostConstruct
    public void init() {
        listOfPhotos = new ArrayList<>();

        listOfPhotos.add(new Photo("/resources/images/photos/photo1.jpg", "/resources/images/photos/photo1s.jpg",
                "Description for Photo 1", "Don't drink and drive!"));
        listOfPhotos.add(new Photo("/resources/images/photos/photo2.jpg", "/resources/images/photos/photo2s.jpg",
                "Description for Photo 2", "Don't drink and drive!"));
        listOfPhotos.add(new Photo("/resources/images/photos/photo3.jpg", "/resources/images/photos/photo3s.jpg",
                "Description for Photo 3", "Don't drink and drive!"));
        listOfPhotos.add(new Photo("/resources/images/photos/photo4.jpg", "/resources/images/photos/photo4s.jpg",
                "Description for Photo 4", "Don't drink and drive!"));
        listOfPhotos.add(new Photo("/resources/images/photos/photo5.jpg", "/resources/images/photos/photo5s.jpg",
                "Description for Photo 5", "Don't drink and drive!"));
        listOfPhotos.add(new Photo("/resources/images/photos/photo6.jpg", "/resources/images/photos/photo6s.jpg",
                "Description for Photo 6", "Don't drink and drive!"));
        }

    /*
    =============
    Getter Method
    =============
     */
    public List<Photo> getListOfPhotos() {
        return listOfPhotos;
    }
}
