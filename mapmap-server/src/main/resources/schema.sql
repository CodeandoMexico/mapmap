--
-- PostgreSQL database dump
--

-- Dumped from database version 9.4.25
-- Dumped by pg_dump version 14.4

-- Started on 2023-01-17 21:29:18 CST

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

--
-- TOC entry 4632 (class 1262 OID 18789)
-- Name: mapmap; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE mapmap WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE = 'C.UTF-8';


ALTER DATABASE mapmap OWNER TO postgres;

\connect mapmap

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

--
-- TOC entry 4633 (class 0 OID 0)
-- Name: mapmap; Type: DATABASE PROPERTIES; Schema: -; Owner: postgres
--

ALTER DATABASE mapmap SET search_path TO '$user', 'public', 'topology';


\connect mapmap

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

--
-- TOC entry 14 (class 2615 OID 18790)
-- Name: tiger; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA tiger;


ALTER SCHEMA tiger OWNER TO postgres;

--
-- TOC entry 15 (class 2615 OID 18791)
-- Name: tiger_data; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA tiger_data;


ALTER SCHEMA tiger_data OWNER TO postgres;

--
-- TOC entry 16 (class 2615 OID 18792)
-- Name: topology; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA topology;


ALTER SCHEMA topology OWNER TO postgres;

--
-- TOC entry 4635 (class 0 OID 0)
-- Dependencies: 16
-- Name: SCHEMA topology; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA topology IS 'PostGIS Topology schema';


--
-- TOC entry 8 (class 3079 OID 18793)
-- Name: address_standardizer; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS address_standardizer WITH SCHEMA public;


--
-- TOC entry 4636 (class 0 OID 0)
-- Dependencies: 8
-- Name: EXTENSION address_standardizer; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION address_standardizer IS 'Used to parse an address into constituent elements. Generally used to support geocoding address normalization step.';


--
-- TOC entry 7 (class 3079 OID 18800)
-- Name: address_standardizer_data_us; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS address_standardizer_data_us WITH SCHEMA public;


--
-- TOC entry 4637 (class 0 OID 0)
-- Dependencies: 7
-- Name: EXTENSION address_standardizer_data_us; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION address_standardizer_data_us IS 'Address Standardizer US dataset example';


--
-- TOC entry 6 (class 3079 OID 18843)
-- Name: fuzzystrmatch; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS fuzzystrmatch WITH SCHEMA public;


--
-- TOC entry 4638 (class 0 OID 0)
-- Dependencies: 6
-- Name: EXTENSION fuzzystrmatch; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION fuzzystrmatch IS 'determine similarities and distance between strings';


--
-- TOC entry 5 (class 3079 OID 18854)
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- TOC entry 4639 (class 0 OID 0)
-- Dependencies: 5
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


--
-- TOC entry 4 (class 3079 OID 20297)
-- Name: postgis_sfcgal; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis_sfcgal WITH SCHEMA public;


--
-- TOC entry 4640 (class 0 OID 0)
-- Dependencies: 4
-- Name: EXTENSION postgis_sfcgal; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION postgis_sfcgal IS 'PostGIS SFCGAL functions';


--
-- TOC entry 2 (class 3079 OID 20315)
-- Name: postgis_tiger_geocoder; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis_tiger_geocoder WITH SCHEMA tiger;


--
-- TOC entry 4641 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION postgis_tiger_geocoder; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION postgis_tiger_geocoder IS 'PostGIS tiger geocoder and reverse geocoder';


--
-- TOC entry 3 (class 3079 OID 20749)
-- Name: postgis_topology; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis_topology WITH SCHEMA topology;


--
-- TOC entry 4642 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION postgis_topology; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION postgis_topology IS 'PostGIS topology spatial types and functions';


--
-- TOC entry 1642 (class 1255 OID 20890)
-- Name: _st_addfacesplit(character varying, integer, integer, boolean); Type: FUNCTION; Schema: topology; Owner: postgres
--

CREATE FUNCTION topology._st_addfacesplit(atopology character varying, anedge integer, oface integer, mbr_only boolean) RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
  fan RECORD;
  newface INTEGER;
  sql TEXT;
  isccw BOOLEAN;
  ishole BOOLEAN;

BEGIN

  IF oface = 0 AND mbr_only THEN
    RETURN NULL;
  END IF;

  SELECT null::int[] as newring_edges,
         null::geometry as shell
  INTO fan;

  SELECT array_agg(edge)
  FROM topology.getringedges(atopology, anedge)
  INTO STRICT fan.newring_edges;


  -- You can't get to the other side of an edge forming a ring
  IF fan.newring_edges @> ARRAY[-anedge] THEN
    RETURN 0;
  END IF;


  sql := 'WITH ids as ( select row_number() over () as seq, edge from unnest('
    || quote_literal(fan.newring_edges::text)
    || '::int[] ) u(edge) ), edges AS ( select CASE WHEN i.edge < 0 THEN ST_Reverse(e.geom) ELSE e.geom END as g FROM ids i left join '
    || quote_ident(atopology) || '.edge_data e ON(e.edge_id = abs(i.edge)) ORDER BY seq) SELECT ST_MakePolygon(ST_MakeLine(g.g)) FROM edges g;';
  EXECUTE sql INTO fan.shell;


  isccw := NOT ST_OrderingEquals(fan.shell, ST_ForceRHR(fan.shell));


  IF oface = 0 THEN
    IF NOT isccw THEN
      RETURN NULL;
    END IF;
  END IF;

  IF mbr_only AND oface != 0 THEN
    -- Update old face mbr (nothing to do if we're opening an hole)
    IF isccw THEN -- {
      sql := 'UPDATE '
        || quote_ident(atopology) || '.face SET mbr = '
        || quote_literal(ST_Envelope(fan.shell)::text)
        || '::geometry WHERE face_id = ' || oface;
    	EXECUTE sql;
    END IF; -- }
    RETURN NULL;
  END IF;

  IF oface != 0 AND NOT isccw THEN -- {
    -- Face created an hole in an outer face
    sql := 'INSERT INTO '
      || quote_ident(atopology) || '.face(mbr) SELECT mbr FROM '
      || quote_ident(atopology)
      || '.face WHERE face_id = ' || oface
      || ' RETURNING face_id';
  ELSE
    sql := 'INSERT INTO '
      || quote_ident(atopology) || '.face(mbr) VALUES ('
      || quote_literal(ST_Envelope(fan.shell)::text)
      || '::geometry) RETURNING face_id';
  END IF; -- }

  -- Insert new face
  EXECUTE sql INTO STRICT newface;

  -- Update forward edges
  sql := 'UPDATE '
    || quote_ident(atopology) || '.edge_data SET left_face = ' || newface
    || ' WHERE left_face = ' || oface || ' AND edge_id = ANY ('
    || quote_literal(array( select +(x) from unnest(fan.newring_edges) u(x) )::text)
    || ')';
  EXECUTE sql;

  -- Update backward edges
  sql := 'UPDATE '
    || quote_ident(atopology) || '.edge_data SET right_face = ' || newface
    || ' WHERE right_face = ' || oface || ' AND edge_id = ANY ('
    || quote_literal(array( select -(x) from unnest(fan.newring_edges) u(x) )::text)
    || ')';
  EXECUTE sql;

  IF oface != 0 AND NOT isccw THEN -- {
    -- face shrinked, must update all non-contained edges and nodes
    ishole := true;
  ELSE
    ishole := false;
  END IF; -- }

  -- Update edges bounding the old face
  sql := 'UPDATE '
    || quote_ident(atopology)
    || '.edge_data SET left_face = CASE WHEN left_face = '
    || oface || ' THEN ' || newface
    || ' ELSE left_face END, right_face = CASE WHEN right_face = '
    || oface || ' THEN ' || newface
    || ' ELSE right_face END WHERE ( left_face = ' || oface
    || ' OR right_face = ' || oface
    || ') AND NOT edge_id = ANY ('
    || quote_literal( array(
        select abs(x) from unnest(fan.newring_edges) u(x)
       )::text )
    || ') AND ';
  IF ishole THEN sql := sql || 'NOT '; END IF;
  sql := sql || '( ' || quote_literal(fan.shell::text)
    || ' && geom AND _ST_Contains(' || quote_literal(fan.shell::text)
    -- We only need to check a single point, but must not be an endpoint
    || '::geometry, ST_LineInterpolatePoint(geom, 0.2)) )';
  EXECUTE sql;

  -- Update isolated nodes in new new face
  sql := 'UPDATE '
    || quote_ident(atopology) || '.node SET containing_face = ' || newface
    || ' WHERE containing_face = ' || oface
    || ' AND ';
  IF ishole THEN sql := sql || 'NOT '; END IF;
  sql := sql || 'ST_Contains(' || quote_literal(fan.shell::text) || '::geometry, geom)';
  EXECUTE sql;

  RETURN newface;

END
$$;


ALTER FUNCTION topology._st_addfacesplit(atopology character varying, anedge integer, oface integer, mbr_only boolean) OWNER TO postgres;

--
-- TOC entry 1643 (class 1255 OID 20891)
-- Name: _st_remedgecheck(character varying, integer, integer, integer, integer); Type: FUNCTION; Schema: topology; Owner: postgres
--

CREATE FUNCTION topology._st_remedgecheck(tname character varying, tid integer, eid integer, lf integer, rf integer) RETURNS void
    LANGUAGE plpgsql
    AS $$
DECLARE
  sql text;
  fidary int[];
  rec RECORD;
BEGIN
  -- Check that no TopoGeometry references the edge being removed
  sql := 'SELECT r.topogeo_id, r.layer_id'
      || ', l.schema_name, l.table_name, l.feature_column '
      || 'FROM topology.layer l INNER JOIN '
      || quote_ident(tname)
      || '.relation r ON (l.layer_id = r.layer_id) '
      || 'WHERE l.level = 0 AND l.feature_type = 2 '
      || ' AND l.topology_id = ' || tid
      || ' AND abs(r.element_id) = ' || eid ;
  FOR rec IN EXECUTE sql LOOP
    RAISE EXCEPTION 'TopoGeom % in layer % (%.%.%) cannot be represented dropping edge %',
            rec.topogeo_id, rec.layer_id,
            rec.schema_name, rec.table_name, rec.feature_column,
            eid;
  END LOOP;

  IF lf != rf THEN -- {

    RAISE NOTICE 'Deletion of edge % joins faces % and %',
                    eid, lf, rf;

    -- check if any topo_geom is defined only by one of the
    -- joined faces. In such case there would be no way to adapt
    -- the definition in case of healing, so we'd have to bail out
    --
    fidary = ARRAY[lf, rf];
    sql := 'SELECT t.* from ('
      || 'SELECT r.topogeo_id, r.layer_id'
      || ', l.schema_name, l.table_name, l.feature_column'
      || ', array_agg(r.element_id) as elems '
      || 'FROM topology.layer l INNER JOIN '
      || quote_ident(tname)
      || '.relation r ON (l.layer_id = r.layer_id) '
      || 'WHERE l.level = 0 AND l.feature_type = 3 '
      || ' AND l.topology_id = ' || tid
      || ' AND r.element_id = ANY (' || quote_literal(fidary)
      || ') group by r.topogeo_id, r.layer_id, l.schema_name, l.table_name, '
      || ' l.feature_column ) t';

    -- No surface can be defined by universal face
    IF lf != 0 AND rf != 0 THEN -- {
      sql := sql || ' WHERE NOT t.elems @> ' || quote_literal(fidary);
    END IF; -- }


    FOR rec IN EXECUTE sql LOOP
      RAISE EXCEPTION 'TopoGeom % in layer % (%.%.%) cannot be represented healing faces % and %',
            rec.topogeo_id, rec.layer_id,
            rec.schema_name, rec.table_name, rec.feature_column,
            rf, lf;
    END LOOP;

  END IF; -- } two faces healed...
END
$$;


ALTER FUNCTION topology._st_remedgecheck(tname character varying, tid integer, eid integer, lf integer, rf integer) OWNER TO postgres;

SET default_tablespace = '';

--
-- TOC entry 261 (class 1259 OID 20892)
-- Name: agency; Type: TABLE; Schema: public; Owner: postgres
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


ALTER TABLE public.agency OWNER TO postgres;

--
-- TOC entry 262 (class 1259 OID 20900)
-- Name: gtfssnapshot; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gtfssnapshot (
    id bigint NOT NULL,
    creationdate timestamp without time zone,
    description character varying(255),
    source character varying(255)
);


ALTER TABLE public.gtfssnapshot OWNER TO postgres;

--
-- TOC entry 263 (class 1259 OID 20906)
-- Name: gtfssnapshotexport; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gtfssnapshotexport (
    id bigint NOT NULL,
    calendars character varying(255),
    description character varying(255),
    mergecomplete timestamp without time zone,
    mergestarted timestamp without time zone,
    source character varying(255),
    status character varying(255)
);


ALTER TABLE public.gtfssnapshotexport OWNER TO postgres;

--
-- TOC entry 264 (class 1259 OID 20912)
-- Name: gtfssnapshotexport_agency; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gtfssnapshotexport_agency (
    gtfssnapshotexport_id bigint NOT NULL,
    agencies_id bigint NOT NULL
);


ALTER TABLE public.gtfssnapshotexport_agency OWNER TO postgres;

--
-- TOC entry 265 (class 1259 OID 20915)
-- Name: gtfssnapshotmerge; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gtfssnapshotmerge (
    id bigint NOT NULL,
    description character varying(255),
    mergecomplete timestamp without time zone,
    mergestarted timestamp without time zone,
    status character varying(255),
    snapshot_id bigint
);


ALTER TABLE public.gtfssnapshotmerge OWNER TO postgres;

--
-- TOC entry 266 (class 1259 OID 20921)
-- Name: gtfssnapshotmergetask; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gtfssnapshotmergetask (
    id bigint NOT NULL,
    description character varying(255),
    status character varying(255),
    taskcompleted timestamp without time zone,
    taskstarted timestamp without time zone,
    merge_id bigint
);


ALTER TABLE public.gtfssnapshotmergetask OWNER TO postgres;

--
-- TOC entry 267 (class 1259 OID 20927)
-- Name: gtfssnapshotvalidation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gtfssnapshotvalidation (
    id bigint NOT NULL,
    status character varying(255),
    validationdesciption character varying(255),
    snapshot_id bigint
);


ALTER TABLE public.gtfssnapshotvalidation OWNER TO postgres;

--
-- TOC entry 268 (class 1259 OID 20933)
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO postgres;

--
-- TOC entry 269 (class 1259 OID 20935)
-- Name: phone; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.phone (
    id bigint NOT NULL,
    imei character varying(255),
    registeredon timestamp without time zone,
    unitid character varying(255),
    username character varying(255)
);


ALTER TABLE public.phone OWNER TO postgres;

--
-- TOC entry 270 (class 1259 OID 20941)
-- Name: route; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.route (
    id bigint NOT NULL,
    aircon boolean,
    capturetime timestamp without time zone,
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


ALTER TABLE public.route OWNER TO postgres;

--
-- TOC entry 271 (class 1259 OID 20947)
-- Name: routepoint; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.routepoint (
    id bigint NOT NULL,
    lat double precision,
    lon double precision,
    sequence integer,
    timeoffset integer,
    route_id bigint
);


ALTER TABLE public.routepoint OWNER TO postgres;

--
-- TOC entry 272 (class 1259 OID 20950)
-- Name: servicecalendar; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.servicecalendar (
    id bigint NOT NULL,
    description character varying(255),
    enddate timestamp without time zone,
    friday boolean,
    gtfsserviceid character varying(255),
    monday boolean,
    saturday boolean,
    startdate timestamp without time zone,
    sunday boolean,
    thursday boolean,
    tuesday boolean,
    wednesday boolean,
    agency_id bigint
);


ALTER TABLE public.servicecalendar OWNER TO postgres;

--
-- TOC entry 273 (class 1259 OID 20956)
-- Name: servicecalendardate; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.servicecalendardate (
    id bigint NOT NULL,
    date timestamp without time zone,
    description character varying(255),
    exceptiontype character varying(255),
    gtfsserviceid character varying(255),
    calendar_id bigint
);


ALTER TABLE public.servicecalendardate OWNER TO postgres;

--
-- TOC entry 274 (class 1259 OID 20962)
-- Name: stop; Type: TABLE; Schema: public; Owner: postgres
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


ALTER TABLE public.stop OWNER TO postgres;

--
-- TOC entry 275 (class 1259 OID 20968)
-- Name: stoptime; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.stoptime (
    id bigint NOT NULL,
    arrivaltime integer,
    departuretime integer,
    dropofftype character varying(255),
    pickuptype character varying(255),
    shapedisttraveled double precision,
    stopheadsign character varying(255),
    stopsequence integer,
    stop_id bigint,
    trip_id bigint
);


ALTER TABLE public.stoptime OWNER TO postgres;

--
-- TOC entry 276 (class 1259 OID 20974)
-- Name: trip; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.trip (
    id bigint NOT NULL,
    blockid character varying(255),
    gtfstripid character varying(255),
    tripdirection character varying(255),
    tripheadsign character varying(255),
    tripshortname character varying(255),
    pattern_id bigint,
    route_id bigint,
    servicecalendar_id bigint,
    servicecalendardate_id bigint,
    shape_id bigint
);


ALTER TABLE public.trip OWNER TO postgres;

--
-- TOC entry 277 (class 1259 OID 20980)
-- Name: trippattern; Type: TABLE; Schema: public; Owner: postgres
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


ALTER TABLE public.trippattern OWNER TO postgres;

--
-- TOC entry 278 (class 1259 OID 20986)
-- Name: trippattern_trippatternstop; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.trippattern_trippatternstop (
    trippattern_id bigint NOT NULL,
    patternstops_id bigint NOT NULL
);


ALTER TABLE public.trippattern_trippatternstop OWNER TO postgres;

--
-- TOC entry 279 (class 1259 OID 20989)
-- Name: trippatternstop; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.trippatternstop (
    id bigint NOT NULL,
    alight integer,
    board integer,
    defaultdistance double precision,
    defaultdwelltime integer,
    defaulttraveltime integer,
    stopsequence integer,
    pattern_id bigint,
    stop_id bigint
);


ALTER TABLE public.trippatternstop OWNER TO postgres;

--
-- TOC entry 280 (class 1259 OID 20992)
-- Name: tripshape; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.tripshape (
    id bigint NOT NULL,
    describeddistance double precision,
    description character varying(255),
    gtfsshapeid character varying(255),
    shape public.geometry,
    simpleshape public.geometry,
    agency_id bigint
);


ALTER TABLE public.tripshape OWNER TO postgres;

--
-- TOC entry 4451 (class 2606 OID 21005)
-- Name: agency agency_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.agency
    ADD CONSTRAINT agency_pkey PRIMARY KEY (id);


--
-- TOC entry 4453 (class 2606 OID 21007)
-- Name: gtfssnapshot gtfssnapshot_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshot
    ADD CONSTRAINT gtfssnapshot_pkey PRIMARY KEY (id);


--
-- TOC entry 4455 (class 2606 OID 21009)
-- Name: gtfssnapshotexport gtfssnapshotexport_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshotexport
    ADD CONSTRAINT gtfssnapshotexport_pkey PRIMARY KEY (id);


--
-- TOC entry 4457 (class 2606 OID 21011)
-- Name: gtfssnapshotmerge gtfssnapshotmerge_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshotmerge
    ADD CONSTRAINT gtfssnapshotmerge_pkey PRIMARY KEY (id);


--
-- TOC entry 4459 (class 2606 OID 21013)
-- Name: gtfssnapshotmergetask gtfssnapshotmergetask_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshotmergetask
    ADD CONSTRAINT gtfssnapshotmergetask_pkey PRIMARY KEY (id);


--
-- TOC entry 4461 (class 2606 OID 21015)
-- Name: gtfssnapshotvalidation gtfssnapshotvalidation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshotvalidation
    ADD CONSTRAINT gtfssnapshotvalidation_pkey PRIMARY KEY (id);


--
-- TOC entry 4463 (class 2606 OID 21017)
-- Name: phone phone_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.phone
    ADD CONSTRAINT phone_pkey PRIMARY KEY (id);


--
-- TOC entry 4465 (class 2606 OID 21019)
-- Name: route route_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.route
    ADD CONSTRAINT route_pkey PRIMARY KEY (id);


--
-- TOC entry 4467 (class 2606 OID 21021)
-- Name: routepoint routepoint_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.routepoint
    ADD CONSTRAINT routepoint_pkey PRIMARY KEY (id);


--
-- TOC entry 4469 (class 2606 OID 21023)
-- Name: servicecalendar servicecalendar_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.servicecalendar
    ADD CONSTRAINT servicecalendar_pkey PRIMARY KEY (id);


--
-- TOC entry 4471 (class 2606 OID 21025)
-- Name: servicecalendardate servicecalendardate_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.servicecalendardate
    ADD CONSTRAINT servicecalendardate_pkey PRIMARY KEY (id);


--
-- TOC entry 4473 (class 2606 OID 21027)
-- Name: stop stop_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stop
    ADD CONSTRAINT stop_pkey PRIMARY KEY (id);


--
-- TOC entry 4475 (class 2606 OID 21029)
-- Name: stoptime stoptime_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stoptime
    ADD CONSTRAINT stoptime_pkey PRIMARY KEY (id);


--
-- TOC entry 4477 (class 2606 OID 21031)
-- Name: trip trip_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trip
    ADD CONSTRAINT trip_pkey PRIMARY KEY (id);


--
-- TOC entry 4479 (class 2606 OID 21033)
-- Name: trippattern trippattern_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trippattern
    ADD CONSTRAINT trippattern_pkey PRIMARY KEY (id);


--
-- TOC entry 4481 (class 2606 OID 21035)
-- Name: trippattern_trippatternstop trippattern_trippatternstop_patternstops_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trippattern_trippatternstop
    ADD CONSTRAINT trippattern_trippatternstop_patternstops_id_key UNIQUE (patternstops_id);


--
-- TOC entry 4483 (class 2606 OID 21037)
-- Name: trippatternstop trippatternstop_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trippatternstop
    ADD CONSTRAINT trippatternstop_pkey PRIMARY KEY (id);


--
-- TOC entry 4485 (class 2606 OID 21039)
-- Name: tripshape tripshape_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tripshape
    ADD CONSTRAINT tripshape_pkey PRIMARY KEY (id);


--
-- TOC entry 4504 (class 2606 OID 21040)
-- Name: trippattern fk1a65b38b604a2ff6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trippattern
    ADD CONSTRAINT fk1a65b38b604a2ff6 FOREIGN KEY (shape_id) REFERENCES public.tripshape(id);


--
-- TOC entry 4505 (class 2606 OID 21045)
-- Name: trippattern fk1a65b38bea648c9b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trippattern
    ADD CONSTRAINT fk1a65b38bea648c9b FOREIGN KEY (route_id) REFERENCES public.route(id);


--
-- TOC entry 4489 (class 2606 OID 21050)
-- Name: gtfssnapshotmergetask fk1fcf8ff74c5206c; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshotmergetask
    ADD CONSTRAINT fk1fcf8ff74c5206c FOREIGN KEY (merge_id) REFERENCES public.gtfssnapshotmerge(id);


--
-- TOC entry 4496 (class 2606 OID 21055)
-- Name: stop fk277c22d46c7c39; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stop
    ADD CONSTRAINT fk277c22d46c7c39 FOREIGN KEY (agency_id) REFERENCES public.agency(id);


--
-- TOC entry 4499 (class 2606 OID 21060)
-- Name: trip fk27e845434c4b3b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trip
    ADD CONSTRAINT fk27e845434c4b3b FOREIGN KEY (servicecalendar_id) REFERENCES public.servicecalendar(id);


--
-- TOC entry 4500 (class 2606 OID 21065)
-- Name: trip fk27e845604a2ff6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trip
    ADD CONSTRAINT fk27e845604a2ff6 FOREIGN KEY (shape_id) REFERENCES public.tripshape(id);


--
-- TOC entry 4501 (class 2606 OID 21070)
-- Name: trip fk27e8456c88ee96; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trip
    ADD CONSTRAINT fk27e8456c88ee96 FOREIGN KEY (pattern_id) REFERENCES public.trippattern(id);


--
-- TOC entry 4502 (class 2606 OID 21075)
-- Name: trip fk27e845dd77d01b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trip
    ADD CONSTRAINT fk27e845dd77d01b FOREIGN KEY (servicecalendardate_id) REFERENCES public.servicecalendardate(id);


--
-- TOC entry 4503 (class 2606 OID 21080)
-- Name: trip fk27e845ea648c9b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trip
    ADD CONSTRAINT fk27e845ea648c9b FOREIGN KEY (route_id) REFERENCES public.route(id);


--
-- TOC entry 4494 (class 2606 OID 21085)
-- Name: servicecalendar fk34c58f3d46c7c39; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.servicecalendar
    ADD CONSTRAINT fk34c58f3d46c7c39 FOREIGN KEY (agency_id) REFERENCES public.agency(id);


--
-- TOC entry 4506 (class 2606 OID 21090)
-- Name: trippattern_trippatternstop fk3a1f0579474c387b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trippattern_trippatternstop
    ADD CONSTRAINT fk3a1f0579474c387b FOREIGN KEY (trippattern_id) REFERENCES public.trippattern(id);


--
-- TOC entry 4507 (class 2606 OID 21095)
-- Name: trippattern_trippatternstop fk3a1f0579992b8387; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trippattern_trippatternstop
    ADD CONSTRAINT fk3a1f0579992b8387 FOREIGN KEY (patternstops_id) REFERENCES public.trippatternstop(id);


--
-- TOC entry 4509 (class 2606 OID 21100)
-- Name: trippatternstop fk4222722d4cea3ad9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trippatternstop
    ADD CONSTRAINT fk4222722d4cea3ad9 FOREIGN KEY (stop_id) REFERENCES public.stop(id);


--
-- TOC entry 4508 (class 2606 OID 21105)
-- Name: trippatternstop fk4222722d6c88ee96; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.trippatternstop
    ADD CONSTRAINT fk4222722d6c88ee96 FOREIGN KEY (pattern_id) REFERENCES public.trippattern(id);


--
-- TOC entry 4491 (class 2606 OID 21110)
-- Name: route fk4b7c2295eb2c256; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.route
    ADD CONSTRAINT fk4b7c2295eb2c256 FOREIGN KEY (phone_id) REFERENCES public.phone(id);


--
-- TOC entry 4492 (class 2606 OID 21115)
-- Name: route fk4b7c229d46c7c39; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.route
    ADD CONSTRAINT fk4b7c229d46c7c39 FOREIGN KEY (agency_id) REFERENCES public.agency(id);


--
-- TOC entry 4488 (class 2606 OID 21120)
-- Name: gtfssnapshotmerge fk4f470c5ac9819064; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshotmerge
    ADD CONSTRAINT fk4f470c5ac9819064 FOREIGN KEY (snapshot_id) REFERENCES public.gtfssnapshot(id);


--
-- TOC entry 4510 (class 2606 OID 21125)
-- Name: tripshape fk5f09da5cd46c7c39; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.tripshape
    ADD CONSTRAINT fk5f09da5cd46c7c39 FOREIGN KEY (agency_id) REFERENCES public.agency(id);


--
-- TOC entry 4495 (class 2606 OID 21130)
-- Name: servicecalendardate fk62e10b415aede510; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.servicecalendardate
    ADD CONSTRAINT fk62e10b415aede510 FOREIGN KEY (calendar_id) REFERENCES public.servicecalendar(id);


--
-- TOC entry 4497 (class 2606 OID 21135)
-- Name: stoptime fk6a10620f4cea3ad9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stoptime
    ADD CONSTRAINT fk6a10620f4cea3ad9 FOREIGN KEY (stop_id) REFERENCES public.stop(id);


--
-- TOC entry 4498 (class 2606 OID 21140)
-- Name: stoptime fk6a10620f7e12a3f9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stoptime
    ADD CONSTRAINT fk6a10620f7e12a3f9 FOREIGN KEY (trip_id) REFERENCES public.trip(id);


--
-- TOC entry 4490 (class 2606 OID 21145)
-- Name: gtfssnapshotvalidation fk7cb5a597c9819064; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshotvalidation
    ADD CONSTRAINT fk7cb5a597c9819064 FOREIGN KEY (snapshot_id) REFERENCES public.gtfssnapshot(id);


--
-- TOC entry 4493 (class 2606 OID 21150)
-- Name: routepoint fkac6394e7ea648c9b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.routepoint
    ADD CONSTRAINT fkac6394e7ea648c9b FOREIGN KEY (route_id) REFERENCES public.route(id);


--
-- TOC entry 4486 (class 2606 OID 21155)
-- Name: gtfssnapshotexport_agency fkb3db45323e9f741b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshotexport_agency
    ADD CONSTRAINT fkb3db45323e9f741b FOREIGN KEY (agencies_id) REFERENCES public.agency(id);


--
-- TOC entry 4487 (class 2606 OID 21160)
-- Name: gtfssnapshotexport_agency fkb3db4532a15bb94a; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gtfssnapshotexport_agency
    ADD CONSTRAINT fkb3db4532a15bb94a FOREIGN KEY (gtfssnapshotexport_id) REFERENCES public.gtfssnapshotexport(id);


--
-- TOC entry 4634 (class 0 OID 0)
-- Dependencies: 17
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2023-01-17 21:29:42 CST

--
-- PostgreSQL database dump complete
--

