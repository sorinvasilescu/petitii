ALTER TABLE `petitions`.`petitions`
  ADD COLUMN `deadline` DATE NULL AFTER `received_date`;

UPDATE `petitions`.`petitions` SET `deadline`= DATE_ADD(`received_date`, INTERVAL 30 day);