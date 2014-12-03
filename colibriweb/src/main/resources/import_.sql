-- initialize quartz scheduler schema
drop table if exists qrtz_job_listeners;
drop table if exists qrtz_trigger_listeners;
drop table if exists qrtz_fired_triggers;
drop table if exists qrtz_paused_trigger_grps;
drop table if exists qrtz_scheduler_state;
drop table if exists qrtz_locks;
drop table if exists qrtz_simple_triggers;
drop table if exists qrtz_cron_triggers;
drop table if exists qrtz_blob_triggers;
drop table if exists qrtz_triggers;
drop table if exists qrtz_job_details;
drop table if exists qrtz_calendars;

CREATE TABLE qrtz_job_details
  (
    JOB_NAME  VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    JOB_CLASS_NAME   VARCHAR(250) NOT NULL, 
    IS_DURABLE BOOL NOT NULL,
    IS_VOLATILE BOOL NOT NULL,
    IS_STATEFUL BOOL NOT NULL,
    REQUESTS_RECOVERY BOOL NOT NULL,
    JOB_DATA BYTEA NULL,
    PRIMARY KEY (JOB_NAME,JOB_GROUP)
);

CREATE TABLE qrtz_job_listeners
  (
    JOB_NAME  VARCHAR(200) NOT NULL, 
    JOB_GROUP VARCHAR(200) NOT NULL,
    JOB_LISTENER VARCHAR(200) NOT NULL,
    PRIMARY KEY (JOB_NAME,JOB_GROUP,JOB_LISTENER),
    FOREIGN KEY (JOB_NAME,JOB_GROUP) 
	REFERENCES QRTZ_JOB_DETAILS(JOB_NAME,JOB_GROUP) 
);

CREATE TABLE qrtz_triggers
  (
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL, 
    JOB_GROUP VARCHAR(200) NOT NULL,
    IS_VOLATILE BOOL NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT NULL,
    PREV_FIRE_TIME BIGINT NULL,
    PRIORITY INTEGER NULL,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT NOT NULL,
    END_TIME BIGINT NULL,
    CALENDAR_NAME VARCHAR(200) NULL,
    MISFIRE_INSTR SMALLINT NULL,
    JOB_DATA BYTEA NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (JOB_NAME,JOB_GROUP) 
	REFERENCES QRTZ_JOB_DETAILS(JOB_NAME,JOB_GROUP) 
);

CREATE TABLE qrtz_simple_triggers
  (
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT NOT NULL,
    REPEAT_INTERVAL BIGINT NOT NULL,
    TIMES_TRIGGERED BIGINT NOT NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_cron_triggers
  (
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_blob_triggers
  (
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BYTEA NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP) 
        REFERENCES QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_trigger_listeners
  (
    TRIGGER_NAME  VARCHAR(200) NOT NULL, 
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    TRIGGER_LISTENER VARCHAR(200) NOT NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_LISTENER),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);


CREATE TABLE qrtz_calendars
  (
    CALENDAR_NAME  VARCHAR(200) NOT NULL, 
    CALENDAR BYTEA NOT NULL,
    PRIMARY KEY (CALENDAR_NAME)
);


CREATE TABLE qrtz_paused_trigger_grps
  (
    TRIGGER_GROUP  VARCHAR(200) NOT NULL, 
    PRIMARY KEY (TRIGGER_GROUP)
);

CREATE TABLE qrtz_fired_triggers 
  (
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    IS_VOLATILE BOOL NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200) NULL,
    JOB_GROUP VARCHAR(200) NULL,
    IS_STATEFUL BOOL NULL,
    REQUESTS_RECOVERY BOOL NULL,
    PRIMARY KEY (ENTRY_ID)
);

CREATE TABLE qrtz_scheduler_state 
  (
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT NOT NULL,
    CHECKIN_INTERVAL BIGINT NOT NULL,
    PRIMARY KEY (INSTANCE_NAME)
);

CREATE TABLE qrtz_locks
  (
    LOCK_NAME  VARCHAR(40) NOT NULL, 
    PRIMARY KEY (LOCK_NAME)
);


INSERT INTO qrtz_locks values('TRIGGER_ACCESS');
INSERT INTO qrtz_locks values('JOB_ACCESS');
INSERT INTO qrtz_locks values('CALENDAR_ACCESS');
INSERT INTO qrtz_locks values('STATE_ACCESS');
INSERT INTO qrtz_locks values('MISFIRE_ACCESS');

insert into ColibriRole (id, name, label, description, immutable, active) values ((select nextval ('hibernate_sequence')), 'user', 'Benutzer', 'user',true, true);
insert into ColibriRole (id, name, label, description, immutable, active) values ((select nextval ('hibernate_sequence')), 'admin', 'Administrator', 'administration',true, true);

insert into ColibriUser (id, username, password, enabled, title, firstname, lastname, email, active) values ((select nextval ('hibernate_sequence')), 'admin', 'Eyox7xbNQ09MkIfRyH+rjg==', true,null,'Adi','Manus','admin@proclos.com', true); -- admin
insert into ColibriUser_ColibriRole (ColibriUser_id, roles_id) values ((select id from ColibriUser where username like 'admin'), (select id from ColibriRole where name like 'user'));
insert into ColibriUser_ColibriRole (ColibriUser_id, roles_id) values ((select id from ColibriUser where username like 'admin'), (select id from ColibriRole where name like 'admin'));

insert into FetchInterval (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), '0 */1 * * * ?', 'Jede Minute', 1, false, true);
insert into FetchInterval (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), '0 */5 * * * ?', 'Alle 05 Minuten', 2, false, true);
insert into FetchInterval (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), '0 */30 * * * ?', 'Alle 30 Minuten', 3, false, true);
insert into FetchInterval (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), '0 0 * * * ?', 'Stündlich', 4, false, true);
insert into FetchInterval (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), '0 0 0-23/3 * * ?', 'Alle 3 Stunden', 5, false, true);
insert into FetchInterval (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), '0 0 0-23/6 * * ?', 'Alle 6 Stunden', 6, false, true);
insert into FetchInterval (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), '0 0 0-23/12 * * ?', 'Alle 12 Stunden', 5, false, true);
insert into FetchInterval (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), '0 0 0 * * ?', 'Täglich', 6, false, true);

insert into ProjectType (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), 'Developement', 'Developement', 1, true, true);
insert into ProjectType (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), 'Staging', 'Staging', 2, true, true);
insert into ProjectType (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), 'Production', 'Production', 3, true, true);
insert into ProjectType (id, name, label, sortorder, immutable, active) values ((select nextval ('hibernate_sequence')), 'Debug', 'Debug', 4, true, true);


-- versions
UPDATE colibrirole SET version = 0;
UPDATE colibriuser SET version = 0;
UPDATE permission SET version = 0;
UPDATE fetchinterval SET version = 0;
UPDATE projecttype SET version = 0;