-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema petitions
-- -----------------------------------------------------

ALTER DATABASE petitions CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

-- -----------------------------------------------------
-- Table `petitions`.`Registration_numbers`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`registration_numbers` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `number` VARCHAR(32) NOT NULL,
  `date` DATE NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Petitioners`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`petitioners` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `first_name` VARCHAR(64) NULL,
  `last_name` VARCHAR(64) NULL,
  `organization` VARCHAR(256) NULL,
  `entity_type` VARCHAR(45) NULL COMMENT 'Persoana fizica, Persoana juridica',
  `email` VARCHAR(128) NULL,
  `phone` VARCHAR(15) NULL,
  `country` VARCHAR(128) NULL,
  `county` VARCHAR(128) NULL,
  `city` VARCHAR(128) NULL,
  `title` VARCHAR(16) NULL COMMENT 'Domnule, Doamna',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Users`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`users` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(128) NOT NULL,
  `password` BINARY(60) NOT NULL,
  `first_name` VARCHAR(64) NULL,
  `last_name` VARCHAR(64) NULL,
  `role` VARCHAR(45) NOT NULL COMMENT 'admin, user',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Petitions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`petitions` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `registration_no` INT UNSIGNED NOT NULL,
  `received_date` DATE NOT NULL,
  `relation` VARCHAR(32) NULL,
  `petitioner_id` INT UNSIGNED NOT NULL,
  `origin` VARCHAR(255) NULL,
  `type` VARCHAR(255) NULL,
  `field` VARCHAR(255) NULL,
  `abstract` TEXT NULL,
  `description` TEXT NOT NULL,
  `responsible_id` INT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  INDEX `nr_inreg_idx` (`registration_no` ASC),
  INDEX `petent_idx` (`petitioner_id` ASC),
  INDEX `responsible_idx` (`responsible_id` ASC),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  CONSTRAINT `reg_no`
  FOREIGN KEY (`registration_no`)
  REFERENCES `petitions`.`registration_numbers` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `petitioner`
  FOREIGN KEY (`petitioner_id`)
  REFERENCES `petitions`.`petitioners` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `responsible`
  FOREIGN KEY (`responsible_id`)
  REFERENCES `petitions`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Petition_status`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`petition_status` (
  `id_petition` INT UNSIGNED NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `date` TIMESTAMP NULL,
  `user_id` INT UNSIGNED NULL ,
  INDEX `id_petitie_idx` (`id_petition` ASC),
  INDEX `user_idx` (`user_id` ASC),
  CONSTRAINT `petition`
  FOREIGN KEY (`id_petition`)
  REFERENCES `petitions`.`petitions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `user`
  FOREIGN KEY (`user_id`)
  REFERENCES `petitions`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Connections`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`connections` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `old_petition_id` INT UNSIGNED NOT NULL,
  `new_petition_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `petitie_veche_idx` (`old_petition_id` ASC),
  INDEX `petitie_noua_idx` (`new_petition_id` ASC),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  CONSTRAINT `old_petition`
  FOREIGN KEY (`old_petition_id`)
  REFERENCES `petitions`.`petitions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `new_petition`
  FOREIGN KEY (`new_petition_id`)
  REFERENCES `petitions`.`petitions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Petition_attachments`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`petition_attachments` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `petition_id` INT UNSIGNED NULL,
  `original_filename` VARCHAR(255) NULL,
  `filename` VARCHAR(255) NULL,
  `content_type` VARCHAR(128) NULL,
  PRIMARY KEY (`id`),
  INDEX `petition_idx` (`petition_id` ASC),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  CONSTRAINT `attach_petition`
  FOREIGN KEY (`petition_id`)
  REFERENCES `petitions`.`petitions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Comments`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`comments` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `petition_id` INT UNSIGNED NOT NULL,
  `comment` TEXT NOT NULL,
  `date` TIMESTAMP NULL,
  `user_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `user_idx` (`user_id` ASC),
  INDEX `petition_idx` (`petition_id` ASC),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  CONSTRAINT `comm_user`
  FOREIGN KEY (`user_id`)
  REFERENCES `petitions`.`users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `comm_petition`
  FOREIGN KEY (`petition_id`)
  REFERENCES `petitions`.`petitions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Email`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`emails` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `uid` BIGINT UNSIGNED,
  `sender` VARCHAR(128) NULL,
  `recipients` TEXT NULL,
  `cc` TEXT NULL,
  `bcc` TEXT NULL,
  `date` DATETIME NULL,
  `subject` TEXT NULL,
  `body` TEXT NULL,
  `size` FLOAT NULL,
  `type` ENUM('Inbox','Outbox','Spam') NULL,
  `petition_id` INT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `email_petition_idx` (`petition_id` ASC),
  CONSTRAINT `email_petition`
  FOREIGN KEY (`petition_id`)
  REFERENCES `petitions`.`petitions` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Email_attachments`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`email_attachments` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `email_id` INT UNSIGNED NOT NULL,
  `original_filename` VARCHAR(255) NULL,
  `filename` VARCHAR(255) NULL,
  `content_type` TEXT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `email_idx` (`email_id` ASC),
  CONSTRAINT `email`
  FOREIGN KEY (`email_id`)
  REFERENCES `petitions`.`emails` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `petitions`.`Contacts`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `petitions`.`contacts` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL,
  `email` VARCHAR(128) NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
  ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
