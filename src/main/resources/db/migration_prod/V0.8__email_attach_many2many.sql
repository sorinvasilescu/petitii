-- change attachment index
ALTER TABLE `petitions`.`attachments`
  CHANGE COLUMN `id` `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT;

-- intersection table
CREATE TABLE IF NOT EXISTS `petitions`.`emails_attachments` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `email_id` INT(10) UNSIGNED NULL DEFAULT NULL,
  `attachment_id` INT(10) UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `email_idx` (`email_id` ASC),
  INDEX `attachment_idx` (`attachment_id` ASC),
  CONSTRAINT `emails_attachments_email`
  FOREIGN KEY (`email_id`)
  REFERENCES `petitions`.`emails` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `emails_attachments_attachment`
  FOREIGN KEY (`attachment_id`)
  REFERENCES `petitions`.`attachments` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;

-- migrate data
INSERT INTO `petitions`.`emails_attachments` (`email_id`,`attachment_id`)
  SELECT `email_id`,`id`
  FROM `petitions`.`attachments`;

-- remove emai id from attachment
ALTER TABLE `petitions`.`attachments`
  DROP FOREIGN KEY `email`,
  DROP COLUMN `email_id`,
  DROP INDEX `email_idx`