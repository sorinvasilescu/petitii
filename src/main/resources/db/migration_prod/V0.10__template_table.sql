-- -----------------------------------------------------
-- Table `petitions`.`email_templates`
-- -----------------------------------------------------

CREATE TABLE `petitions`.`email_templates` (
`id` bigint(20) NOT NULL AUTO_INCREMENT,
`name` VARCHAR(255) NULL,
`content` longtext,
PRIMARY KEY (`id`)
) ENGINE=InnoDB;