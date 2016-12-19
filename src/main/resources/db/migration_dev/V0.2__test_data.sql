-- sample users for testing purposes
-- do not run when in prod
INSERT INTO `petitions`.`users` (email, password, first_name, last_name, role)
VALUES ('a@a.com', convert('$2a$04$.ZXSw4VOyG0RF/nTjIg5VeE6oafbNvFT9cuAFZreVviIs13AKIR1G', BINARY), 'AF', 'AL', 'USER');
INSERT INTO `petitions`.`users` (email, password, first_name, last_name, role)
VALUES ('b@b.com', convert('$2a$04$7ryGNF7IGmbgfJPBXfvOn.TWnDveU93mt89kn.ZNxgP9IwHubOYzq', BINARY), 'BF', 'BL', 'ADMIN');

INSERT INTO `petitions`.`contacts` (`id`, `name`, `email`, `phone`)
VALUES (1, "Tudor Nixon", "user@datatables.net", "0231000000");

INSERT INTO `petitions`.`contacts` (`id`, `name`, `email`, `phone`)
VALUES (2, "George Winters", "g.winters@datatables.net", "09852200000");

INSERT INTO `petitions`.`contacts` (`id`, `name`, `email`, `phone`)
VALUES (3, "Alex Cox", "a.cox@datatables.net", "098755454874554");