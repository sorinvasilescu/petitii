-- sample users for testing purposes
-- do not run when in prod
insert into users (email,password,first_name,last_name,role) values ('a@a.com',convert('$2a$04$.ZXSw4VOyG0RF/nTjIg5VeE6oafbNvFT9cuAFZreVviIs13AKIR1G',BINARY),'AF','AL','USER');
insert into users (email,password,first_name,last_name,role) values ('b@b.com',convert('$2a$04$7ryGNF7IGmbgfJPBXfvOn.TWnDveU93mt89kn.ZNxgP9IwHubOYzq',BINARY),'BF','BL','ADMIN');