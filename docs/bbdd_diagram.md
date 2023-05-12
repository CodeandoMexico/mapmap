classDiagram
direction BT
class agency {
   varchar(255) color
   varchar(255) gtfsagencyid
   varchar(255) lang
   varchar(255) name
   varchar(255) phone
   boolean systemmap
   varchar(255) timezone
   varchar(255) url
   bigint id
}
class geography_columns {
   name f_table_catalog
   name f_table_schema
   name f_table_name
   name f_geography_column
   integer coord_dimension
   integer srid
   text type
}
class geometry_columns {
   varchar(256) f_table_catalog
   name f_table_schema
   name f_table_name
   name f_geometry_column
   integer coord_dimension
   integer srid
   varchar(30) type
}
class gtfssnapshot {
   timestamp creationdate
   varchar(255) description
   varchar(255) source
   bigint id
}
class gtfssnapshotexport {
   varchar(255) calendars
   varchar(255) description
   timestamp mergecomplete
   timestamp mergestarted
   varchar(255) source
   varchar(255) status
   bigint id
}
class gtfssnapshotexport_agency {
   bigint gtfssnapshotexport_id
   bigint agencies_id
}
class gtfssnapshotmerge {
   varchar(255) description
   timestamp mergecomplete
   timestamp mergestarted
   varchar(255) status
   bigint snapshot_id
   bigint id
}
class gtfssnapshotmergetask {
   varchar(255) description
   varchar(255) status
   timestamp taskcompleted
   timestamp taskstarted
   bigint merge_id
   bigint id
}
class gtfssnapshotvalidation {
   varchar(255) status
   varchar(255) validationdesciption
   bigint snapshot_id
   bigint id
}
class phone {
   varchar(255) imei
   timestamp registeredon
   varchar(255) unitid
   varchar(255) username
   bigint id
}
class raster_columns {
   name r_table_catalog
   name r_table_schema
   name r_table_name
   name r_raster_column
   integer srid
   double precision scale_x
   double precision scale_y
   integer blocksize_x
   integer blocksize_y
   boolean same_alignment
   boolean regular_blocking
   integer num_bands
   text[] pixel_types
   double precision[] nodata_values
   boolean[] out_db
   geometry extent
   boolean spatial_index
}
class raster_overviews {
   name o_table_catalog
   name o_table_schema
   name o_table_name
   name o_raster_column
   name r_table_catalog
   name r_table_schema
   name r_table_name
   name r_raster_column
   integer overview_factor
}
class route {
   boolean aircon
   timestamp capturetime
   varchar(255) comments
   varchar(255) gtfsrouteid
   varchar(255) routecolor
   varchar(255) routedesc
   varchar(255) routelongname
   varchar(255) routenotes
   varchar(255) routeshortname
   varchar(255) routetextcolor
   varchar(255) routetype
   varchar(255) routeurl
   boolean saturday
   boolean sunday
   varchar(255) vehiclecapacity
   varchar(255) vehicletype
   boolean weekday
   bigint agency_id
   bigint phone_id
   bigint id
}
class routepoint {
   double precision lat
   double precision lon
   integer sequence
   integer timeoffset
   bigint route_id
   bigint id
}
class servicecalendar {
   varchar(255) description
   timestamp enddate
   boolean friday
   varchar(255) gtfsserviceid
   boolean monday
   boolean saturday
   timestamp startdate
   boolean sunday
   boolean thursday
   boolean tuesday
   boolean wednesday
   bigint agency_id
   bigint id
}
class servicecalendardate {
   timestamp date
   varchar(255) description
   varchar(255) exceptiontype
   varchar(255) gtfsserviceid
   bigint calendar_id
   bigint id
}
class spatial_ref_sys {
   varchar(256) auth_name
   integer auth_srid
   varchar(2048) srtext
   varchar(2048) proj4text
   integer srid
}
class stop {
   integer alight
   integer board
   varchar(255) gtfsstopid
   geometry location
   varchar(255) locationtype
   boolean majorstop
   varchar(255) parentstation
   varchar(255) stopcode
   varchar(255) stopdesc
   varchar(255) stopname
   varchar(255) stopurl
   varchar(255) zoneid
   bigint agency_id
   bigint id
}
class stoptime {
   integer arrivaltime
   integer departuretime
   varchar(255) dropofftype
   varchar(255) pickuptype
   double precision shapedisttraveled
   varchar(255) stopheadsign
   integer stopsequence
   bigint stop_id
   bigint trip_id
   bigint id
}
class trip {
   varchar(255) blockid
   varchar(255) gtfstripid
   varchar(255) tripdirection
   varchar(255) tripheadsign
   varchar(255) tripshortname
   bigint pattern_id
   bigint route_id
   bigint servicecalendar_id
   bigint servicecalendardate_id
   bigint shape_id
   bigint id
}
class trippattern {
   integer endtime
   varchar(255) headsign
   integer headway
   boolean longest
   varchar(255) name
   boolean saturday
   integer starttime
   boolean sunday
   boolean usefrequency
   boolean weekday
   bigint route_id
   bigint shape_id
   bigint id
}
class trippattern_trippatternstop {
   bigint trippattern_id
   bigint patternstops_id
}
class trippatternstop {
   integer alight
   integer board
   double precision defaultdistance
   integer defaultdwelltime
   integer defaulttraveltime
   integer stopsequence
   bigint pattern_id
   bigint stop_id
   bigint id
}
class tripshape {
   double precision describeddistance
   varchar(255) description
   varchar(255) gtfsshapeid
   geometry shape
   geometry simpleshape
   bigint agency_id
   bigint id
}
class us_gaz {
   integer seq
   text word
   text stdword
   integer token
   boolean is_custom
   integer id
}
class us_lex {
   integer seq
   text word
   text stdword
   integer token
   boolean is_custom
   integer id
}
class us_rules {
   text rule
   boolean is_custom
   integer id
}

gtfssnapshotexport_agency  -->  agency : agencies_id:id
gtfssnapshotexport_agency  -->  gtfssnapshotexport : gtfssnapshotexport_id:id
gtfssnapshotmerge  -->  gtfssnapshot : snapshot_id:id
gtfssnapshotmergetask  -->  gtfssnapshotmerge : merge_id:id
gtfssnapshotvalidation  -->  gtfssnapshot : snapshot_id:id
route  -->  agency : agency_id:id
route  -->  phone : phone_id:id
routepoint  -->  route : route_id:id
servicecalendar  -->  agency : agency_id:id
servicecalendardate  -->  servicecalendar : calendar_id:id
stop  -->  agency : agency_id:id
stoptime  -->  stop : stop_id:id
stoptime  -->  trip : trip_id:id
trip  -->  route : route_id:id
trip  -->  servicecalendar : servicecalendar_id:id
trip  -->  servicecalendardate : servicecalendardate_id:id
trip  -->  trippattern : pattern_id:id
trip  -->  tripshape : shape_id:id
trippattern  -->  route : route_id:id
trippattern  -->  tripshape : shape_id:id
trippattern_trippatternstop  -->  trippattern : trippattern_id:id
trippattern_trippatternstop  -->  trippatternstop : patternstops_id:id
trippatternstop  -->  stop : stop_id:id
trippatternstop  -->  trippattern : pattern_id:id
tripshape  -->  agency : agency_id:id
