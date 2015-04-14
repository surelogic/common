CREATE TABLE LICENSE_BLACKLIST (
  LICENSE_ID CHAR(36) NOT NULL
)
<<>>
CREATE TABLE LICENSE_NETCHECK_LOG (
  DATE_TIME         TIMESTAMP   NOT NULL,
  REMOTE_IP         VARCHAR(47) NOT NULL,
  LICENSE_ID        CHAR(36)    NOT NULL,
  EVENT_DESCRIPTION VARCHAR(35) NOT NULL
)
<<>>
CREATE INDEX LICENSE_NETCHECK_LOG_INDEX ON LICENSE_NETCHECK_LOG (LICENSE_ID)
<<>>
CREATE TABLE LICENSE_NETCHECK_DESCRIPTION (
  LICENSE_ID                  CHAR(36)          NOT NULL PRIMARY KEY,
  LICENSE_PRODUCT             VARCHAR(30)       NOT NULL,
  LICENSE_HOLDER              VARCHAR(1024)     NOT NULL,
  LICENSE_DURATION_IN_DAYS    BIGINT            DEFAULT 365 NOT NULL CHECK (LICENSE_DURATION_IN_DAYS > 1),
  LICENSE_INSTALL_BEFORE_DATE TIMESTAMP         NOT NULL, 
  LICENSE_TYPE                VARCHAR(30)       NOT NULL,
  MAX_ACTIVE                  BIGINT  DEFAULT 2 NOT NULL CHECK (MAX_ACTIVE >= 1)
)
<<>>
CREATE TABLE LICENSE_NETCHECK_COUNTS (
  LICENSE_ID      CHAR(36)         NOT NULL PRIMARY KEY,
  INSTALL_COUNT   BIGINT DEFAULT 0 NOT NULL CHECK (INSTALL_COUNT >= 0),
  RENEWAL_COUNT   BIGINT DEFAULT 0 NOT NULL CHECK (RENEWAL_COUNT >= 0),
  REMOVAL_COUNT   BIGINT DEFAULT 0 NOT NULL CHECK (REMOVAL_COUNT >= 0),
  BLACKLIST_COUNT BIGINT DEFAULT 0 NOT NULL CHECK (BLACKLIST_COUNT >= 0),
  TOO_MANY_COUNT  BIGINT DEFAULT 0 NOT NULL CHECK (TOO_MANY_COUNT >= 0)
)
<<>>