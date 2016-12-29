ALTER TABLE `petitions`.`emails_attachments`
  ADD UNIQUE INDEX `pair_unique` (`email_id` ASC, `attachment_id` ASC);