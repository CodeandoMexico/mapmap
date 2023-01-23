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

CREATE TABLE public.phone (
    id bigint NOT NULL,
    imei character varying(255),
    registeredon timestamp without time zone,
    unitid character varying(255),
    username character varying(255)
);