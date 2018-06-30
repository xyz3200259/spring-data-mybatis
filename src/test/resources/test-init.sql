CREATE TABLE DS_USER (
  ID            VARCHAR(64)      NOT NULL,
  FIRST_NAME     VARCHAR(32),
  LASTNAME      VARCHAR(32),
  AGE           INT(3),
  ACTIVE        INT(1),
  CREATED_AT    TIMESTAMP,
  EMAIL_ADDRESS VARCHAR(128),
  MANAGER_ID    INT(11),
  BINARY_DATA   BLOB,
  DATE_OF_BIRTH DATE,
  country       VARCHAR(64),
  city          VARCHAR(64),
  street_name   VARCHAR(64),
  street_no     VARCHAR(64),
  PRIMARY KEY (ID)
);