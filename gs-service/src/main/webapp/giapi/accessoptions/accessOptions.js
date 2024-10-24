/**
 * This module provides a set of objects related to the <a href="../classes/GINode.html#accessOptions" class="crosslink">options</a> 
 * of the {{#crossLink "GINode/accessLink:method"}}GINode <code>accessLink</code> method{{/crossLink}}.
 * See also {{#crossLink "GINode/accessOptions:method"}}GINode <code>accessOptions</code> method{{/crossLink}} 
 * 
 * @module AccessOptions
 * @main AccessOptions
 **/

/**
 * This object provides a set of parameters to use as <a href="../classes/GINode.html#accessOptions" class="crosslink">options</a> 
 * with the {{#crossLink "GINode/accessLink:method"}}GINode <code>accessLink</code> method{{/crossLink}}. E.g.:
 * 
 * <pre><code> var accessOptions1 = {
	     "CRS": "EPSG:4326",
	     "format": "IMAGE_PNG",
	     "firstAxisSize": {
	        "label": "Lat",
	        "value": 300
	     },
	     "secondAxisSize": {
	        "label": "Lon",
	        "value": 300
	     },
	     "spatialSubset": {
	         "south": -60.0,
	         "west": -180.0,
	         "north": 90.000007823,
	         "east": 180.000018775
	     },
	     "temporalSubset": {
	         "from" : "2000-01-01T00:00:00Z",
	         "to": "2013-01-01"        
	     }
   };<br>
   var accessOptions2 = {
         "CRS": "EPSG:3857",
     	 "format": "IMAGE_JPEG",
     	 "firstAxisSize": {
        	"label": "X",
        	"value": 455
     	 },
	     "secondAxisSize": {
	        "label": "Y",
	        "value": 300
	     },
	     "spatialSubset": {
	         "south": -60.0,
	         "west": -180.0,
	         "north": 90.000007823,
	         "east": 180.000018775
	     },
	     "temporalSubset": {
	         "from" : "2000-01-01T00:00:00Z",
	         "to": "2013-01-01"        
	     }
   };</pre></code>
 * 
 * @class AccessOptions
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * A valid data output Coordinate Reference System expressed with the 
 * <a href="http://www.epsg-registry.org/">EPSG Geodetic Parameter Registry</a> (e.g.: "EPSG:4326").<br>
 *
 * @property CRS
 * @type {String}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * A valid data output format (e.g.: "NetCDF").<br>
 *  
 * @property format
 * @type {String}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * The size of the first axis of the data.<br>
 *  
 * @property firstAxisSize
 * @type {AxisSize}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * The size of the second axis of the data.<br>
 *  
 * @property secondAxisSize
 * @type {AxisSize}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * The data output spatial subset.<br>
 *
 * @property spatialSubset
 * @type {Bbox}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * The data output temporal subset.<br>
 *
 * @property temporalSubset
 * @type {TimePeriod}
 *
 **/
