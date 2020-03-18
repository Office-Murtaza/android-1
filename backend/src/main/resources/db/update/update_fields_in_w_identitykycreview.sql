ALTER TABLE w_identitykycreview CHANGE file_name id_card_file_name VARCHAR(255) DEFAULT NULL;
ALTER TABLE w_identitykycreview CHANGE file_mimetype id_card_file_mimetype VARCHAR(50) DEFAULT NULL;
ALTER TABLE w_identitykycreview ADD COLUMN ssn_file_name VARCHAR(255) DEFAULT NULL;
ALTER TABLE w_identitykycreview ADD COLUMN ssn_file_mimetype VARCHAR(50) DEFAULT NULL;