SDMX DataStore
==============


Overview
--------

Just a proof of concept


Requirements
------------

SDMX ReST 2.1


Functionality
-------------

TBD


Test
----

TBD

MSTP='TOT' and "
        + "AGE='TOT' and " + "STATE='1' and " + "REGIONTYPE='STE' and "
        + "REGION in ('1','2','3','4') and " + "FREQUENCY='A'
        

curl -XGET "http://geoserverarcgis/geoserver/wfs?request=GetFeature&typeName=aurin:ABS_CENSUS2011_T04&version=1.1.0\
&cql_filter=MEASURE=%271%27%20and%20MSTP=%27TOT%27%20and%20AGE=%27TOT%27%20and%20STATE=%271%27%20and%20REGIONTYPE=%27STE%27%20and%20REGION%20in%20(%271%27,%272%27,%273%27,%274%27)%20and%20FREQUENCY=%27A%27"

http://geoserverarcgis/geoserver/wfs?request=GetFeature&typeName=aurin:ABS_CENSUS2011_T04&version=1.1.0\
&cql_filter=MEASURE=%271%27%20and%20MSTP=%27TOT%27%20and%20AGE=%27TOT%27%20and%20STATE=%271%27%20and%20REGIONTYPE=%27STE%27%20and%20REGION%20in%20(%271%27,%272%27,%273%27,%274%27)%20and%20FREQUENCY=%27A%27        