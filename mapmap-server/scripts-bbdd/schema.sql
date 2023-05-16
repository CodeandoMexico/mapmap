--
-- PostgreSQL database dump
--

-- Dumped from database version 15.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: agency; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.agency (
                               id bigint NOT NULL,
                               color character varying(255),
                               gtfsagencyid character varying(255),
                               lang character varying(255),
                               name character varying(255),
                               phone character varying(255),
                               systemmap boolean,
                               timezone character varying(255),
                               url character varying(255)
);


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: phone; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.phone (
                              id bigint NOT NULL,
                              imei character varying(255),
                              registeredon timestamp(6) without time zone,
                              unitid character varying(255),
                              username character varying(255)
);


--
-- Name: route; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.route (
                              id bigint NOT NULL,
                              aircon boolean,
                              capturetime timestamp(6) without time zone,
                              comments character varying(255),
                              gtfsrouteid character varying(255),
                              routecolor character varying(255),
                              routedesc character varying(255),
                              routelongname character varying(255),
                              routenotes character varying(255),
                              routeshortname character varying(255),
                              routetextcolor character varying(255),
                              routetype character varying(255),
                              routeurl character varying(255),
                              saturday boolean,
                              sunday boolean,
                              vehiclecapacity character varying(255),
                              vehicletype character varying(255),
                              weekday boolean,
                              agency_id bigint,
                              phone_id bigint
);


--
-- Name: routepoint; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.routepoint (
                                   id bigint NOT NULL,
                                   lat double precision,
                                   lon double precision,
                                   sequence integer,
                                   timeoffset integer,
                                   route_id bigint
);


--
-- Name: stop; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.stop (
                             id bigint NOT NULL,
                             alight integer,
                             board integer,
                             gtfsstopid character varying(255),
                             location public.geometry,
                             locationtype character varying(255),
                             majorstop boolean,
                             parentstation character varying(255),
                             stopcode character varying(255),
                             stopdesc character varying(255),
                             stopname character varying(255),
                             stopurl character varying(255),
                             zoneid character varying(255),
                             agency_id bigint
);


--
-- Name: trippattern; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.trippattern (
                                    id bigint NOT NULL,
                                    endtime integer,
                                    headsign character varying(255),
                                    headway integer,
                                    longest boolean,
                                    name character varying(255),
                                    saturday boolean,
                                    starttime integer,
                                    sunday boolean,
                                    usefrequency boolean,
                                    weekday boolean,
                                    route_id bigint,
                                    shape_id bigint
);


--
-- Name: trippatternstop; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.trippatternstop (
                                        id bigint NOT NULL,
                                        alight integer,
                                        board integer,
                                        defaultdistance double precision,
                                        defaultdwelltime integer,
                                        defaulttraveltime integer,
                                        stopsequence integer,
                                        stop_id bigint,
                                        pattern_id bigint
);


--
-- Name: tripshape; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tripshape (
                                  id bigint NOT NULL,
                                  describeddistance double precision,
                                  description character varying(255),
                                  gtfsshapeid character varying(255),
                                  shape public.geometry,
                                  simpleshape public.geometry
);


--
-- Name: agency agency_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.agency
    ADD CONSTRAINT agency_pkey PRIMARY KEY (id);


--
-- Name: phone phone_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.phone
    ADD CONSTRAINT phone_pkey PRIMARY KEY (id);


--
-- Name: route route_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.route
    ADD CONSTRAINT route_pkey PRIMARY KEY (id);


--
-- Name: routepoint routepoint_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routepoint
    ADD CONSTRAINT routepoint_pkey PRIMARY KEY (id);


--
-- Name: stop stop_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stop
    ADD CONSTRAINT stop_pkey PRIMARY KEY (id);


--
-- Name: trippattern trippattern_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trippattern
    ADD CONSTRAINT trippattern_pkey PRIMARY KEY (id);


--
-- Name: trippatternstop trippatternstop_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trippatternstop
    ADD CONSTRAINT trippatternstop_pkey PRIMARY KEY (id);


--
-- Name: tripshape tripshape_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tripshape
    ADD CONSTRAINT tripshape_pkey PRIMARY KEY (id);


--
-- Name: trippatternstop fk230bhkq28aljg0j97jjm7ixqm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trippatternstop
    ADD CONSTRAINT fk230bhkq28aljg0j97jjm7ixqm FOREIGN KEY (stop_id) REFERENCES public.stop(id);


--
-- Name: stop fk4rgjey2anj29bxsu6dny8d385; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.stop
    ADD CONSTRAINT fk4rgjey2anj29bxsu6dny8d385 FOREIGN KEY (agency_id) REFERENCES public.agency(id);


--
-- Name: routepoint fk6969b72l2da71ttwiv83mrmka; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.routepoint
    ADD CONSTRAINT fk6969b72l2da71ttwiv83mrmka FOREIGN KEY (route_id) REFERENCES public.route(id);


--
-- Name: trippattern fkcfqxrkra9ffeffprxosm935pb; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trippattern
    ADD CONSTRAINT fkcfqxrkra9ffeffprxosm935pb FOREIGN KEY (route_id) REFERENCES public.route(id);


--
-- Name: trippattern fklgmv3mwk1kd3jb93rvm1ncq7o; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trippattern
    ADD CONSTRAINT fklgmv3mwk1kd3jb93rvm1ncq7o FOREIGN KEY (shape_id) REFERENCES public.tripshape(id);


--
-- Name: trippatternstop fkn5dq2uf2a2mmj1dhjl0ed4d70; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trippatternstop
    ADD CONSTRAINT fkn5dq2uf2a2mmj1dhjl0ed4d70 FOREIGN KEY (pattern_id) REFERENCES public.trippattern(id);


--
-- Name: route fkn8x168hmc9ajuq4i2rohnmmpn; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.route
    ADD CONSTRAINT fkn8x168hmc9ajuq4i2rohnmmpn FOREIGN KEY (phone_id) REFERENCES public.phone(id);


--
-- Name: route fkpjyc40ihe8p6u2y5t0pwo17o3; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.route
    ADD CONSTRAINT fkpjyc40ihe8p6u2y5t0pwo17o3 FOREIGN KEY (agency_id) REFERENCES public.agency(id);


--
-- PostgreSQL database dump complete
--
