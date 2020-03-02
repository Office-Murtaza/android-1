ALTER TABLE identitypiecepersonalinfo ADD COLUMN ssn VARCHAR(11) DEFAULT NULL AFTER id;
ALTER TABLE identitypiecepersonalinfo MODIFY COLUMN createdBy_id BIGINT(20) DEFAULT 5;