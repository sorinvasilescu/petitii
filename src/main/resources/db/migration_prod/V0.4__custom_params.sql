CREATE TABLE IF NOT EXISTS `petitions`.`petition_custom_params` (
  `id`           INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `param`        VARCHAR(251),
  `friendly_name` VARCHAR(251)
                  COLLATE utf8mb4_unicode_ci,
  `default_value` VARCHAR(251)
                 COLLATE utf8mb4_unicode_ci,
  `required`     BOOL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `petitions`.`petition_custom_params_values` (
  `id`    INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `cp_id` INT UNSIGNED NOT NULL,
  `label` VARCHAR(251)
          COLLATE utf8mb4_unicode_ci,
  `value` VARCHAR(251)
          COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `cp_id_idx` (`cp_id` ASC),
  CONSTRAINT `custom_params`
  FOREIGN KEY (`cp_id`)
  REFERENCES `petitions`.`petition_custom_params` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
) ENGINE = InnoDB;
