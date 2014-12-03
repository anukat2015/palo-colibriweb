--
-- PostgreSQL database dump
--

-- Dumped from database version 9.1.2
-- Dumped by pg_dump version 9.1.4
-- Started on 2013-09-10 17:26:17 CEST

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 187 (class 3079 OID 11907)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2324 (class 0 OID 0)
-- Dependencies: 187
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

CREATE USER colibri WITH PASSWORD 'irbiloc';
CREATE DATABASE colibri WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'de_AT.UTF-8' LC_CTYPE = 'de_AT.UTF-8';
ALTER DATABASE colibri OWNER TO colibri;

\connect colibri

--
-- TOC entry 162 (class 1259 OID 92252)
-- Dependencies: 5
-- Name: colibrirole; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE colibrirole (
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    description character varying(32768),
    immutable boolean NOT NULL,
    label character varying(255),
    name character varying(255) NOT NULL,
    sortorder integer,
    creator_id bigint,
    modificator_id bigint
);


ALTER TABLE public.colibrirole OWNER TO colibri;

--
-- TOC entry 163 (class 1259 OID 92262)
-- Dependencies: 5
-- Name: colibriuser; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE colibriuser (
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    completename character varying(255),
    email character varying(255),
    enabled boolean NOT NULL,
    firstname character varying(255),
    jobtitle character varying(255),
    lastname character varying(255),
    password character varying(255),
    signature character varying(255),
    title character varying(255),
    username character varying(255),
    creator_id bigint,
    modificator_id bigint
);


ALTER TABLE public.colibriuser OWNER TO colibri;

--
-- TOC entry 164 (class 1259 OID 92270)
-- Dependencies: 5
-- Name: colibriuser_colibrirole; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE colibriuser_colibrirole (
    colibriuser_id bigint NOT NULL,
    roles_id bigint NOT NULL
);


ALTER TABLE public.colibriuser_colibrirole OWNER TO colibri;

--
-- TOC entry 165 (class 1259 OID 92273)
-- Dependencies: 5
-- Name: component; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE component (
    dtype integer NOT NULL,
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    description text,
    name character varying(255),
    type character varying(255),
    xml text,
    handle bytea,
    "interval" bytea,
    ismanual boolean,
    creator_id bigint,
    modificator_id bigint,
    project_id bigint
);


ALTER TABLE public.component OWNER TO colibri;

--
-- TOC entry 188 (class 1259 OID 127898)
-- Dependencies: 5
-- Name: component_variable; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE component_variable (
    component_id bigint NOT NULL,
    variables_id bigint NOT NULL,
    "position" integer NOT NULL
);


ALTER TABLE public.component_variable OWNER TO colibri;

--
-- TOC entry 187 (class 1259 OID 122904)
-- Dependencies: 5
-- Name: componentoutput; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE componentoutput (
    id bigint NOT NULL,
    name character varying(255),
    type character varying(255),
    version integer,
    component_id bigint
);


ALTER TABLE public.componentoutput OWNER TO colibri;

--
-- TOC entry 186 (class 1259 OID 92913)
-- Dependencies: 5
-- Name: execution; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE execution (
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    errors integer NOT NULL,
    firsterrormessage character varying(1000),
    log text,
    startdate timestamp without time zone,
    status character varying(255),
    stopdate timestamp without time zone,
    type character varying(255),
    warnings integer NOT NULL,
    creator_id bigint,
    modificator_id bigint,
    component_id bigint,
    "position" integer,
    etlid bigint
);


ALTER TABLE public.execution OWNER TO colibri;

--
-- TOC entry 166 (class 1259 OID 92296)
-- Dependencies: 5
-- Name: fetchinterval; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE fetchinterval (
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    description character varying(32768),
    immutable boolean NOT NULL,
    label character varying(255),
    name character varying(255) NOT NULL,
    sortorder integer,
    creator_id bigint,
    modificator_id bigint
);


ALTER TABLE public.fetchinterval OWNER TO colibri;

--
-- TOC entry 161 (class 1259 OID 92249)
-- Dependencies: 5
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: colibri
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO colibri;

--
-- TOC entry 167 (class 1259 OID 92306)
-- Dependencies: 5
-- Name: lostpassword; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE lostpassword (
    id integer NOT NULL,
    "timestamp" timestamp without time zone,
    token character varying(255),
    user_id bigint
);


ALTER TABLE public.lostpassword OWNER TO colibri;

--
-- TOC entry 168 (class 1259 OID 92311)
-- Dependencies: 5
-- Name: notification; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE notification (
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    details text,
    origin character varying(255),
    read boolean,
    targetid bigint,
    targetmodule character varying(255),
    title text,
    visibledate timestamp without time zone,
    creator_id bigint,
    modificator_id bigint,
    targetuser_id bigint
);


ALTER TABLE public.notification OWNER TO colibri;

--
-- TOC entry 169 (class 1259 OID 92319)
-- Dependencies: 5
-- Name: permission; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE permission (
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    action character varying(255),
    discriminator character varying(255),
    recipient character varying(255),
    target character varying(255),
    creator_id bigint,
    modificator_id bigint
);


ALTER TABLE public.permission OWNER TO colibri;

--
-- TOC entry 170 (class 1259 OID 92327)
-- Dependencies: 5
-- Name: project; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE project (
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    contactonerror character varying(255),
    contactperson character varying(255),
    description text,
    name character varying(255),
    publicationstate integer,
    creator_id bigint,
    modificator_id bigint,
    type_id bigint
);


ALTER TABLE public.project OWNER TO colibri;

--
-- TOC entry 172 (class 1259 OID 92345)
-- Dependencies: 5
-- Name: project_variable; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE project_variable (
    project_id bigint NOT NULL,
    variables_id bigint NOT NULL,
    "position" integer NOT NULL
);


ALTER TABLE public.project_variable OWNER TO colibri;

--
-- TOC entry 171 (class 1259 OID 92335)
-- Dependencies: 5
-- Name: projecttype; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE projecttype (
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    description character varying(32768),
    immutable boolean NOT NULL,
    label character varying(255),
    name character varying(255) NOT NULL,
    sortorder integer,
    creator_id bigint,
    modificator_id bigint
);


ALTER TABLE public.projecttype OWNER TO colibri;

--
-- TOC entry 179 (class 1259 OID 92809)
-- Dependencies: 5
-- Name: qrtz_blob_triggers; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_blob_triggers (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);


ALTER TABLE public.qrtz_blob_triggers OWNER TO colibri;

--
-- TOC entry 181 (class 1259 OID 92835)
-- Dependencies: 5
-- Name: qrtz_calendars; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_calendars (
    calendar_name character varying(200) NOT NULL,
    calendar bytea NOT NULL
);


ALTER TABLE public.qrtz_calendars OWNER TO colibri;

--
-- TOC entry 178 (class 1259 OID 92796)
-- Dependencies: 5
-- Name: qrtz_cron_triggers; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_cron_triggers (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    cron_expression character varying(120) NOT NULL,
    time_zone_id character varying(80)
);


ALTER TABLE public.qrtz_cron_triggers OWNER TO colibri;

--
-- TOC entry 183 (class 1259 OID 92848)
-- Dependencies: 5
-- Name: qrtz_fired_triggers; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_fired_triggers (
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    is_volatile boolean NOT NULL,
    instance_name character varying(200) NOT NULL,
    fired_time bigint NOT NULL,
    priority integer NOT NULL,
    state character varying(16) NOT NULL,
    job_name character varying(200),
    job_group character varying(200),
    is_stateful boolean,
    requests_recovery boolean
);


ALTER TABLE public.qrtz_fired_triggers OWNER TO colibri;

--
-- TOC entry 174 (class 1259 OID 92752)
-- Dependencies: 5
-- Name: qrtz_job_details; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_job_details (
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    job_class_name character varying(250) NOT NULL,
    is_durable boolean NOT NULL,
    is_volatile boolean NOT NULL,
    is_stateful boolean NOT NULL,
    requests_recovery boolean NOT NULL,
    job_data bytea
);


ALTER TABLE public.qrtz_job_details OWNER TO colibri;

--
-- TOC entry 175 (class 1259 OID 92760)
-- Dependencies: 5
-- Name: qrtz_job_listeners; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_job_listeners (
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    job_listener character varying(200) NOT NULL
);


ALTER TABLE public.qrtz_job_listeners OWNER TO colibri;

--
-- TOC entry 185 (class 1259 OID 92861)
-- Dependencies: 5
-- Name: qrtz_locks; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_locks (
    lock_name character varying(40) NOT NULL
);


ALTER TABLE public.qrtz_locks OWNER TO colibri;

--
-- TOC entry 182 (class 1259 OID 92843)
-- Dependencies: 5
-- Name: qrtz_paused_trigger_grps; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_paused_trigger_grps (
    trigger_group character varying(200) NOT NULL
);


ALTER TABLE public.qrtz_paused_trigger_grps OWNER TO colibri;

--
-- TOC entry 184 (class 1259 OID 92856)
-- Dependencies: 5
-- Name: qrtz_scheduler_state; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_scheduler_state (
    instance_name character varying(200) NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL
);


ALTER TABLE public.qrtz_scheduler_state OWNER TO colibri;

--
-- TOC entry 177 (class 1259 OID 92786)
-- Dependencies: 5
-- Name: qrtz_simple_triggers; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_simple_triggers (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL
);


ALTER TABLE public.qrtz_simple_triggers OWNER TO colibri;

--
-- TOC entry 180 (class 1259 OID 92822)
-- Dependencies: 5
-- Name: qrtz_trigger_listeners; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_trigger_listeners (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    trigger_listener character varying(200) NOT NULL
);


ALTER TABLE public.qrtz_trigger_listeners OWNER TO colibri;

--
-- TOC entry 176 (class 1259 OID 92773)
-- Dependencies: 5
-- Name: qrtz_triggers; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE qrtz_triggers (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    is_volatile boolean NOT NULL,
    description character varying(250),
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state character varying(16) NOT NULL,
    trigger_type character varying(8) NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint,
    calendar_name character varying(200),
    misfire_instr smallint,
    job_data bytea
);


ALTER TABLE public.qrtz_triggers OWNER TO colibri;

--
-- TOC entry 173 (class 1259 OID 92360)
-- Dependencies: 5
-- Name: variable; Type: TABLE; Schema: public; Owner: colibri; Tablespace: 
--

CREATE TABLE variable (
    id bigint NOT NULL,
    active boolean,
    creationdate timestamp without time zone,
    modificationdate timestamp without time zone,
    version integer,
    name character varying(255),
    value character varying(255),
    creator_id bigint,
    modificator_id bigint,
    commment text,
    comment text
);


ALTER TABLE public.variable OWNER TO colibri;

--
-- TOC entry 2238 (class 2606 OID 92261)
-- Dependencies: 162 162
-- Name: colibrirole_name_key; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY colibrirole
    ADD CONSTRAINT colibrirole_name_key UNIQUE (name);


--
-- TOC entry 2240 (class 2606 OID 92259)
-- Dependencies: 162 162
-- Name: colibrirole_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY colibrirole
    ADD CONSTRAINT colibrirole_pkey PRIMARY KEY (id);


--
-- TOC entry 2242 (class 2606 OID 92269)
-- Dependencies: 163 163
-- Name: colibriuser_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY colibriuser
    ADD CONSTRAINT colibriuser_pkey PRIMARY KEY (id);


--
-- TOC entry 2244 (class 2606 OID 92280)
-- Dependencies: 165 165
-- Name: component_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY component
    ADD CONSTRAINT component_pkey PRIMARY KEY (id);


--
-- TOC entry 2296 (class 2606 OID 127902)
-- Dependencies: 188 188 188
-- Name: component_variable_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY component_variable
    ADD CONSTRAINT component_variable_pkey PRIMARY KEY (component_id, "position");


--
-- TOC entry 2294 (class 2606 OID 122911)
-- Dependencies: 187 187
-- Name: componentoutput_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY componentoutput
    ADD CONSTRAINT componentoutput_pkey PRIMARY KEY (id);


--
-- TOC entry 2292 (class 2606 OID 92920)
-- Dependencies: 186 186
-- Name: execution_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY execution
    ADD CONSTRAINT execution_pkey PRIMARY KEY (id);


--
-- TOC entry 2246 (class 2606 OID 92305)
-- Dependencies: 166 166
-- Name: fetchinterval_name_key; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY fetchinterval
    ADD CONSTRAINT fetchinterval_name_key UNIQUE (name);


--
-- TOC entry 2248 (class 2606 OID 92303)
-- Dependencies: 166 166
-- Name: fetchinterval_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY fetchinterval
    ADD CONSTRAINT fetchinterval_pkey PRIMARY KEY (id);


--
-- TOC entry 2250 (class 2606 OID 92310)
-- Dependencies: 167 167
-- Name: lostpassword_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY lostpassword
    ADD CONSTRAINT lostpassword_pkey PRIMARY KEY (id);


--
-- TOC entry 2252 (class 2606 OID 92318)
-- Dependencies: 168 168
-- Name: notification_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);


--
-- TOC entry 2254 (class 2606 OID 92326)
-- Dependencies: 169 169
-- Name: permission_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT permission_pkey PRIMARY KEY (id);


--
-- TOC entry 2256 (class 2606 OID 92334)
-- Dependencies: 170 170
-- Name: project_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_pkey PRIMARY KEY (id);


--
-- TOC entry 2262 (class 2606 OID 92349)
-- Dependencies: 172 172 172
-- Name: project_variable_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY project_variable
    ADD CONSTRAINT project_variable_pkey PRIMARY KEY (project_id, "position");


--
-- TOC entry 2264 (class 2606 OID 92351)
-- Dependencies: 172 172
-- Name: project_variable_variables_id_key; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY project_variable
    ADD CONSTRAINT project_variable_variables_id_key UNIQUE (variables_id);


--
-- TOC entry 2258 (class 2606 OID 92344)
-- Dependencies: 171 171
-- Name: projecttype_name_key; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY projecttype
    ADD CONSTRAINT projecttype_name_key UNIQUE (name);


--
-- TOC entry 2260 (class 2606 OID 92342)
-- Dependencies: 171 171
-- Name: projecttype_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY projecttype
    ADD CONSTRAINT projecttype_pkey PRIMARY KEY (id);


--
-- TOC entry 2278 (class 2606 OID 92816)
-- Dependencies: 179 179 179
-- Name: qrtz_blob_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);


--
-- TOC entry 2282 (class 2606 OID 92842)
-- Dependencies: 181 181
-- Name: qrtz_calendars_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_calendars
    ADD CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (calendar_name);


--
-- TOC entry 2276 (class 2606 OID 92803)
-- Dependencies: 178 178 178
-- Name: qrtz_cron_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);


--
-- TOC entry 2286 (class 2606 OID 92855)
-- Dependencies: 183 183
-- Name: qrtz_fired_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_fired_triggers
    ADD CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (entry_id);


--
-- TOC entry 2268 (class 2606 OID 92759)
-- Dependencies: 174 174 174
-- Name: qrtz_job_details_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_job_details
    ADD CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (job_name, job_group);


--
-- TOC entry 2270 (class 2606 OID 92767)
-- Dependencies: 175 175 175 175
-- Name: qrtz_job_listeners_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_job_listeners
    ADD CONSTRAINT qrtz_job_listeners_pkey PRIMARY KEY (job_name, job_group, job_listener);


--
-- TOC entry 2290 (class 2606 OID 92865)
-- Dependencies: 185 185
-- Name: qrtz_locks_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_locks
    ADD CONSTRAINT qrtz_locks_pkey PRIMARY KEY (lock_name);


--
-- TOC entry 2284 (class 2606 OID 92847)
-- Dependencies: 182 182
-- Name: qrtz_paused_trigger_grps_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_paused_trigger_grps
    ADD CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (trigger_group);


--
-- TOC entry 2288 (class 2606 OID 92860)
-- Dependencies: 184 184
-- Name: qrtz_scheduler_state_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_scheduler_state
    ADD CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (instance_name);


--
-- TOC entry 2274 (class 2606 OID 92790)
-- Dependencies: 177 177 177
-- Name: qrtz_simple_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);


--
-- TOC entry 2280 (class 2606 OID 92829)
-- Dependencies: 180 180 180 180
-- Name: qrtz_trigger_listeners_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_trigger_listeners
    ADD CONSTRAINT qrtz_trigger_listeners_pkey PRIMARY KEY (trigger_name, trigger_group, trigger_listener);


--
-- TOC entry 2272 (class 2606 OID 92780)
-- Dependencies: 176 176 176
-- Name: qrtz_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);


--
-- TOC entry 2266 (class 2606 OID 92367)
-- Dependencies: 173 173
-- Name: variable_pkey; Type: CONSTRAINT; Schema: public; Owner: colibri; Tablespace: 
--

ALTER TABLE ONLY variable
    ADD CONSTRAINT variable_pkey PRIMARY KEY (id);


--
-- TOC entry 2308 (class 2606 OID 92443)
-- Dependencies: 2241 163 167
-- Name: fk104ad5ff1db4dd09; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY lostpassword
    ADD CONSTRAINT fk104ad5ff1db4dd09 FOREIGN KEY (user_id) REFERENCES colibriuser(id);


--
-- TOC entry 2297 (class 2606 OID 117918)
-- Dependencies: 162 163 2241
-- Name: fk1486cf0678b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY colibrirole
    ADD CONSTRAINT fk1486cf0678b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2298 (class 2606 OID 117923)
-- Dependencies: 162 163 2241
-- Name: fk1486cf06f8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY colibrirole
    ADD CONSTRAINT fk1486cf06f8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2299 (class 2606 OID 117928)
-- Dependencies: 163 2241 163
-- Name: fk14883a5b78b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY colibriuser
    ADD CONSTRAINT fk14883a5b78b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2300 (class 2606 OID 117933)
-- Dependencies: 2241 163 163
-- Name: fk14883a5bf8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY colibriuser
    ADD CONSTRAINT fk14883a5bf8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2303 (class 2606 OID 117948)
-- Dependencies: 165 2255 170
-- Name: fk24013cdd4d644507; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY component
    ADD CONSTRAINT fk24013cdd4d644507 FOREIGN KEY (project_id) REFERENCES project(id);


--
-- TOC entry 2304 (class 2606 OID 117953)
-- Dependencies: 2241 163 165
-- Name: fk24013cdd78b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY component
    ADD CONSTRAINT fk24013cdd78b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2305 (class 2606 OID 117958)
-- Dependencies: 2241 163 165
-- Name: fk24013cddf8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY component
    ADD CONSTRAINT fk24013cddf8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2318 (class 2606 OID 92488)
-- Dependencies: 171 2241 163
-- Name: fk2b64c1d378b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY projecttype
    ADD CONSTRAINT fk2b64c1d378b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2317 (class 2606 OID 92483)
-- Dependencies: 163 2241 171
-- Name: fk2b64c1d3f8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY projecttype
    ADD CONSTRAINT fk2b64c1d3f8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2311 (class 2606 OID 92458)
-- Dependencies: 2241 163 168
-- Name: fk2d45dd0b43cfb658; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY notification
    ADD CONSTRAINT fk2d45dd0b43cfb658 FOREIGN KEY (targetuser_id) REFERENCES colibriuser(id);


--
-- TOC entry 2310 (class 2606 OID 92453)
-- Dependencies: 163 168 2241
-- Name: fk2d45dd0b78b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY notification
    ADD CONSTRAINT fk2d45dd0b78b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2309 (class 2606 OID 92448)
-- Dependencies: 2241 168 163
-- Name: fk2d45dd0bf8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY notification
    ADD CONSTRAINT fk2d45dd0bf8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2329 (class 2606 OID 92921)
-- Dependencies: 186 2243 165
-- Name: fk366b2af811908347; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY execution
    ADD CONSTRAINT fk366b2af811908347 FOREIGN KEY (component_id) REFERENCES component(id);


--
-- TOC entry 2331 (class 2606 OID 92931)
-- Dependencies: 186 163 2241
-- Name: fk366b2af878b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY execution
    ADD CONSTRAINT fk366b2af878b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2330 (class 2606 OID 92926)
-- Dependencies: 2241 186 163
-- Name: fk366b2af8f8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY execution
    ADD CONSTRAINT fk366b2af8f8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2320 (class 2606 OID 92498)
-- Dependencies: 172 2255 170
-- Name: fk4048cd224d644507; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY project_variable
    ADD CONSTRAINT fk4048cd224d644507 FOREIGN KEY (project_id) REFERENCES project(id);


--
-- TOC entry 2319 (class 2606 OID 92493)
-- Dependencies: 173 172 2265
-- Name: fk4048cd2272052712; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY project_variable
    ADD CONSTRAINT fk4048cd2272052712 FOREIGN KEY (variables_id) REFERENCES variable(id);


--
-- TOC entry 2314 (class 2606 OID 92872)
-- Dependencies: 170 2259 171
-- Name: fk50c8e2f96dba91a0; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY project
    ADD CONSTRAINT fk50c8e2f96dba91a0 FOREIGN KEY (type_id) REFERENCES projecttype(id);


--
-- TOC entry 2316 (class 2606 OID 92478)
-- Dependencies: 170 163 2241
-- Name: fk50c8e2f978b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY project
    ADD CONSTRAINT fk50c8e2f978b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2315 (class 2606 OID 92473)
-- Dependencies: 170 163 2241
-- Name: fk50c8e2f9f8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY project
    ADD CONSTRAINT fk50c8e2f9f8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2313 (class 2606 OID 92468)
-- Dependencies: 169 163 2241
-- Name: fk57f7a1ef78b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT fk57f7a1ef78b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2312 (class 2606 OID 92463)
-- Dependencies: 2241 169 163
-- Name: fk57f7a1eff8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY permission
    ADD CONSTRAINT fk57f7a1eff8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2332 (class 2606 OID 122912)
-- Dependencies: 165 187 2243
-- Name: fk8d5b683e11908347; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY componentoutput
    ADD CONSTRAINT fk8d5b683e11908347 FOREIGN KEY (component_id) REFERENCES component(id);


--
-- TOC entry 2307 (class 2606 OID 92438)
-- Dependencies: 163 2241 166
-- Name: fk9299257f78b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY fetchinterval
    ADD CONSTRAINT fk9299257f78b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2306 (class 2606 OID 92433)
-- Dependencies: 166 163 2241
-- Name: fk9299257ff8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY fetchinterval
    ADD CONSTRAINT fk9299257ff8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2334 (class 2606 OID 127908)
-- Dependencies: 165 2243 188
-- Name: fka5cd53be11908347; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY component_variable
    ADD CONSTRAINT fka5cd53be11908347 FOREIGN KEY (component_id) REFERENCES component(id);


--
-- TOC entry 2333 (class 2606 OID 127903)
-- Dependencies: 173 188 2265
-- Name: fka5cd53be72052712; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY component_variable
    ADD CONSTRAINT fka5cd53be72052712 FOREIGN KEY (variables_id) REFERENCES variable(id);


--
-- TOC entry 2322 (class 2606 OID 92518)
-- Dependencies: 2241 173 163
-- Name: fkb95f369c78b0e108; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY variable
    ADD CONSTRAINT fkb95f369c78b0e108 FOREIGN KEY (creator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2321 (class 2606 OID 92513)
-- Dependencies: 163 2241 173
-- Name: fkb95f369cf8d38885; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY variable
    ADD CONSTRAINT fkb95f369cf8d38885 FOREIGN KEY (modificator_id) REFERENCES colibriuser(id);


--
-- TOC entry 2301 (class 2606 OID 117938)
-- Dependencies: 164 2239 162
-- Name: fkf007b72216a29182; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY colibriuser_colibrirole
    ADD CONSTRAINT fkf007b72216a29182 FOREIGN KEY (roles_id) REFERENCES colibrirole(id);


--
-- TOC entry 2302 (class 2606 OID 117943)
-- Dependencies: 163 164 2241
-- Name: fkf007b72257fae079; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY colibriuser_colibrirole
    ADD CONSTRAINT fkf007b72257fae079 FOREIGN KEY (colibriuser_id) REFERENCES colibriuser(id);


--
-- TOC entry 2327 (class 2606 OID 92817)
-- Dependencies: 179 179 176 2271 176
-- Name: qrtz_blob_triggers_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 2326 (class 2606 OID 92804)
-- Dependencies: 178 176 2271 178 176
-- Name: qrtz_cron_triggers_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 2323 (class 2606 OID 92768)
-- Dependencies: 175 2267 174 175 174
-- Name: qrtz_job_listeners_job_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY qrtz_job_listeners
    ADD CONSTRAINT qrtz_job_listeners_job_name_fkey FOREIGN KEY (job_name, job_group) REFERENCES qrtz_job_details(job_name, job_group);


--
-- TOC entry 2325 (class 2606 OID 92791)
-- Dependencies: 176 177 177 176 2271
-- Name: qrtz_simple_triggers_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 2328 (class 2606 OID 92830)
-- Dependencies: 2271 180 180 176 176
-- Name: qrtz_trigger_listeners_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY qrtz_trigger_listeners
    ADD CONSTRAINT qrtz_trigger_listeners_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 2324 (class 2606 OID 92781)
-- Dependencies: 176 176 174 174 2267
-- Name: qrtz_triggers_job_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: colibri
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_job_name_fkey FOREIGN KEY (job_name, job_group) REFERENCES qrtz_job_details(job_name, job_group);


--
-- TOC entry 2339 (class 0 OID 0)
-- Dependencies: 5
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;



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


-- Completed on 2013-09-10 17:26:17 CEST

--
-- PostgreSQL database dump complete
--

