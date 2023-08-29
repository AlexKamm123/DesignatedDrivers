/*
 * Created by Conrad MacKethan on 2023.4.30
 * Copyright © 2023 Conrad MacKethan. All rights reserved.
 */

/*
 * Created by Conrad MacKethan on 2023.4.26
 * Copyright © 2023 Conrad MacKethan. All rights reserved.
 */

# ---------------------------------------------------
# SQL script to create the DesignatedDrivers Database
# ---------------------------------------------------

DROP TABLE IF EXISTS Request, Ride, Event, Car, UserPhoto, User;

CREATE TABLE User
(
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(32) NOT NULL,
    middle_name VARCHAR(32),
    last_name VARCHAR(32) NOT NULL,
    address1 VARCHAR(128) NOT NULL,
    address2 VARCHAR(128),
    city VARCHAR(64) NOT NULL,
    state VARCHAR(2) NOT NULL,
    zipcode VARCHAR(10) NOT NULL,
    security_question VARCHAR(255) NOT NULL,
    security_answer VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL,
    two_fa_status INT NOT NULL,
    cell_phone_number VARCHAR(24),
    cell_phone_carrier VARCHAR(32),
    `user_type` varchar(32) NOT NULL,
    `ride_id` INT,
    `event_id` INT,
    PRIMARY KEY (id)

);

CREATE TABLE UserPhoto
(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT NOT NULL,
    extension ENUM('jpeg', 'jpg', 'png', 'gif') NOT NULL,
    user_id INT UNSIGNED,         /* Optional photo belongs to a User */
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

CREATE TABLE `Car` (
                       `id` INT NOT NULL AUTO_INCREMENT,
                       `make` varchar(32) NOT NULL,
                       `model` varchar(32) NOT NULL,
                       `license_plate` varchar(16) NOT NULL,
                       `max_occupants` INT NOT NULL,
                       `user_id` INT UNSIGNED NOT NULL,
                       PRIMARY KEY (`id`)
);

CREATE TABLE `Event` (
                         `id` INT NOT NULL AUTO_INCREMENT,
                         `name` varchar(128) NOT NULL,
                         `description` varchar(2048) NOT NULL,
                         `start_time` TIMESTAMP NOT NULL,
                         `end_time` TIMESTAMP NOT NULL,
                         `visibility` varchar(16) NOT NULL,
                         `driver_code` varchar(255) NOT NULL,
                         `passenger_code` varchar(255) NOT NULL,
                         `address` varchar(256) NOT NULL,
                         `user_id` INT UNSIGNED NOT NULL,
                         `address_id` varchar(128) NOT NULL,
                         `photo` varchar(256) NOT NULL,
                         PRIMARY KEY (`id`)
);

CREATE TABLE `Ride` (
                        `id` INT NOT NULL AUTO_INCREMENT,
                        `time` TIMESTAMP NOT NULL,
                        `riders` varchar(255) NOT NULL,
                        `destination_address` varchar(256) NOT NULL,
                        `waypoints` varchar(2048) NOT NULL,
                        `address_ids` varchar(2048) NOT NULL,
                        `num_occupants` INT NOT NULL,
                        `car_id` INT NOT NULL,
                        `event_id` INT NOT NULL,
                        PRIMARY KEY (`id`)
);

CREATE TABLE `Request` (
                           `id` INT NOT NULL AUTO_INCREMENT,
                           `time` TIMESTAMP NOT NULL,
                           `event_id` INT NOT NULL,
                           `user_id` INT UNSIGNED NOT NULL,
                           `direction` varchar(16) NOT NULL,
                           `address` varchar(256) NOT NULL,
                           `num_passengers` INT UNSIGNED NOT NULL,
                           PRIMARY KEY (`id`)
);

ALTER TABLE `User` ADD CONSTRAINT `User_fk0` FOREIGN KEY (`ride_id`) REFERENCES `Ride`(`id`);

ALTER TABLE `User` ADD CONSTRAINT `User_fk1` FOREIGN KEY (`event_id`) REFERENCES `Event`(`id`);

ALTER TABLE `UserPhoto` ADD CONSTRAINT `UserPhoto_fk0` FOREIGN KEY (`user_id`) REFERENCES `User`(`id`);

ALTER TABLE `Car` ADD CONSTRAINT `Car_fk0` FOREIGN KEY (`user_id`) REFERENCES `User`(`id`);

ALTER TABLE `Event` ADD CONSTRAINT `Event_fk0` FOREIGN KEY (`user_id`) REFERENCES `User`(`id`);

ALTER TABLE `Ride` ADD CONSTRAINT `Ride_fk0` FOREIGN KEY (`car_id`) REFERENCES `Car`(`id`);

ALTER TABLE `Ride` ADD CONSTRAINT `Ride_fk1` FOREIGN KEY (`event_id`) REFERENCES `Event`(`id`);

ALTER TABLE `Request` ADD CONSTRAINT `Request_fk0` FOREIGN KEY (`event_id`) REFERENCES `Event`(`id`);

ALTER TABLE `Request` ADD CONSTRAINT `Request_fk1` FOREIGN KEY (`user_id`) REFERENCES `User`(`id`);