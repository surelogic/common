---------------------------------------------------------------------
-- The database schema definition for surelogic.com Services
--
-- This script is automatically processed, ensure that nothing can
-- generate a result set (i.e., no queries).
---------------------------------------------------------------------

CREATE TABLE SUPPORT_REQUEST (
    ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	REMOTE_IP VARCHAR(40),
	REMOTE_HOSTNAME VARCHAR(512),
	REQUEST_TYPE VARCHAR(512),
	HTTP_HEADERS VARCHAR(4096),
	BODY VARCHAR(32672),
	SENDER VARCHAR(1024),
	CLIENT_OS VARCHAR(1024),
	CLIENT_JAVA VARCHAR(1024),
	CLIENT_IDE VARCHAR(1024),
	LICENSE_ID VARCHAR(256),
	LICENSE_TOOL VARCHAR(1024),
	LICENSE_HOLDER VARCHAR(1024),
	LICENSE_EXPIRATION DATE
)
<<>>
