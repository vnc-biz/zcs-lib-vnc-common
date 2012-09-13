USE zimbra;

--
-- a sane, SQL standard-conformant way would be simply using type 'text'
-- but mysql usually doens't understand them right, and even insists on
-- varchar being artifially limited, while also unable to index columns
-- that can grow longer than 1000 bytes ... someone please give these
-- jerks a mercyful headshot ;-o
--

CREATE TABLE IF NOT EXISTS zcs_zimlet_user_config
(
	`mtime`		timestamp,
	`zimlet`	varchar(111),	-- mysql is too stupid to support longer keys ...
	`username`	varchar(111),
	`property`	varchar(111),
	`value`		varchar(4096)
);

CREATE INDEX _zcs_zimlet_user_config_si1 ON zcs_zimlet_user_config(zimlet,username);
CREATE UNIQUE INDEX _zcs_zimlet_user_config_ui1 ON zcs_zimlet_user_config(zimlet,username,property);
