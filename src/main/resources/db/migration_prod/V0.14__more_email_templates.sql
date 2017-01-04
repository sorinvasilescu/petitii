INSERT INTO `petitions`.`email_templates` (`name`, `category`, `content`)
VALUES ('Începe lucrul standard', 'start_work',
        REPLACE('<img alt="" src="http://ithub.gov.ro/wp-content/uploads/2016/08/govithub000.png"><br>Stimate [[ ~{pet.petitioner.title} ]] [[ ~{pet.petitioner.getFullName()} ]],<br><br>Petiția dumneavoastră cu numărul [[ ~{pet.regNo.number}]] a fost înregistrată cu success la data de [[ ~{#dates.format(pet.receivedDate, \'dd.MM.yyyy\')} ]].<br><br>Termenul de soluționare este [[ ~{#dates.format(pet.deadline, \'dd.MM.yyyy\')} ]]<br><br>Cu stimă,<br>Echipa Gov IT Hub<br><code></code>', '~{', '${'));

INSERT INTO `petitions`.`email_templates` (`name`, `category`, `content`)
VALUES ('Redirecționare standard', 'forward',
        REPLACE('<img alt="" src="http://ithub.gov.ro/wp-content/uploads/2016/08/govithub000.png"><br>Redirecționare standard,<br><br>Petiția numărul [[ ~{pet.regNo.number} ]]<br><br><b>-- Redirecționare standard</b><br><b><br></b>Cu respect,<br>Echipa Gov IT HUB<br>', '~{', '${'));