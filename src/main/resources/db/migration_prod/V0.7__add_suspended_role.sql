ALTER TABLE `petitions`.`users`
  MODIFY COLUMN `role` ENUM ('ADMIN', 'USER', 'SUSPENDED');