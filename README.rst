SDMX DataStore
==============


Overview
--------

Proof of concept of an SDMX datastore. SDMX is a web-service protocol for the exchange of staitstical 
data used by national statistics offices and multilateral organisations such as OCED, EuroStat, 
The World Bank.


Requirements
------------

Connection to an SDMX ReST 2.1 server.



Functionality
-------------

This datastore allow the query of SDMX dataflows with filtes (either WFS or CQL) that bound the 
dimensions and specify the measure to return (only one measure is returned).

When filters are bound to a an empty diemsion level (as in AGE=''), all levels of that dimension 
are returned. 

The list of available provides is:
1. ABS
2. ECB
3. Estat
4. ILO
5. IMF
6. Ineg
7. Istat
8. NBB
9. Oecd
10. UIS
11. WB
12. WITS


CQL Examples
------------

All measures bound
MEASURE= '1' and MSTP='TOT' and AGE='TOT' and STATE='1' and REGIONTYPE='STE' and 
REGION in ('1','2','3','4') and FREQUENCY='A'

All ages request
MEASURE= '1' and MSTP='TOT' and AGE = '' and STATE='1' and REGIONTYPE='STE' and 
REGION='1' and FREQUENCY='A'


WFS Exmaples
------------ 
(Change hostname as needed)

All measures bound
curl -XGET "http://geoserverarcgis/geoserver/wfs?request=GetFeature&typeName=aurin:ABS_CENSUS2011_T04&version=1.1.0\
&cql_filter=MEASURE=%271%27%20and%20MSTP=%27TOT%27%20and%20AGE=%27TOT%27%20and%20STATE=%271%27%20and%20REGIONTYPE=%27STE%27%20and%20REGION%20in%20(%271%27,%272%27,%273%27,%274%27)%20and%20FREQUENCY=%27A%27"

All ages request
curl -XGET "http://geoserverarcgis/geoserver/wfs?request=GetFeature&typeName=aurin:ABS_CENSUS2011_T04&version=1.1.0\
&cql_filter=MEASURE=%271%27%20and%20MSTP=%27TOT%27%20and%20STATE=%271%27%20and%20REGIONTYPE=%27STE%27%20and%20REGION=%271%27%20and%20FREQUENCY=%27A%27%20and%20AGE=%27%27"        