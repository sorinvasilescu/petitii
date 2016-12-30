ALTER TABLE `petitions`.`attachments`
  DROP FOREIGN KEY `email`;
ALTER TABLE `petitions`.`attachments`
  CHANGE COLUMN `email_id` `email_id` INT(10) UNSIGNED NULL ;
ALTER TABLE `petitions`.`attachments`
  ADD CONSTRAINT `email`
FOREIGN KEY (`email_id`)
REFERENCES `petitions`.`emails` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;