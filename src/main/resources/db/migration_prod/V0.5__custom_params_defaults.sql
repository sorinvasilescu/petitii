--
-- Entity type
--
INSERT INTO `petitions`.`petition_custom_params` (`id`, `param`, `friendly_name`, `default_value`, `required`)
VALUES (1, 'entity', 'Tipul entității', NULL, TRUE);

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 1, 'Persoană Fizică', 'PF');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 1, 'Persoană Juridică', 'PJ');
--
-- Information type
--
INSERT INTO `petitions`.`petition_custom_params` (`id`, `param`, `friendly_name`, `default_value`, `required`)
VALUES (2, 'information', 'Tipul relației', NULL, TRUE);

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 2, 'Audiență', 'audienta');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 2, 'Scrisoare', 'scrisoare');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 2, 'Fax', 'fax');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 2, 'E-mail', 'e-mail');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 2, 'Online', 'online');
--
-- Problem type
--
INSERT INTO `petitions`.`petition_custom_params` (`id`, `param`, `friendly_name`, `default_value`, `required`)
VALUES (3, 'problem', 'Tip problematică', NULL, FALSE);

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 3, 'Solicitare privată', 'solicitare');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 3, 'Plângere', 'plangere');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 3, 'Întrebare', 'intrebare');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 3, 'Cerere audiență', 'cerere_audienta');
--
-- Domain type
--
INSERT INTO `petitions`.`petition_custom_params` (`id`, `param`, `friendly_name`, `default_value`, `required`)
VALUES (4, 'domain', 'Domenii', NULL, FALSE);

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Administrație publică centrală', 'APC');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Administrație publică locală', 'APL');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Amenajare teritoriu', 'AT');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Cetățenie', 'CT');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Culte', 'CU');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Drepturi persoane', 'DP');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Restituire proprietăți', 'RP');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Despăgubiri', 'DE');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Fond funciar', 'FF');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Sesizări abuzuri', 'SA');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Bănci', 'BK');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Mediu', 'MD');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 4, 'Dezvoltare regională', 'DR');
--
-- title type
--
INSERT INTO `petitions`.`petition_custom_params` (`id`, `param`, `friendly_name`, `default_value`, `required`)
VALUES (5, 'title', 'Adresare', NULL, TRUE);

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 5, 'Domnul', 'dl');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 5, 'Doamna', 'dna');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 5, 'Alta', 'alt');

INSERT INTO `petitions`.`petition_custom_params_values` (`id`, `cp_id`, `label`, `value`)
VALUES (NULL, 5, 'Societatea', 'soc');