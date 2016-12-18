ALTER TABLE `petitions`.`petitions`
  ADD COLUMN `status` varchar(16) COLLATE utf8mb4_unicode_ci NOT NULL AFTER `problem_type`;