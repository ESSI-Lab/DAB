/**
 * This module contains the {{#crossLink "Report"}}{{/crossLink}} object and other objects that compose it
 * 
 * @module Report
 * @main Report
 **/

/**
 * This object provides several info about the associated {{#crossLink "GINode"}}node{{/crossLink}}.
 * The properties listed are not described in detail, they are mainly derived from <a href="http://www.iso.org/iso/catalogue_detail.htm?csnumber=26020">ISO 19115</a> specification
 * 
 * @class Report
 **/

/**
 * 
 * @property id
 * @type {String}
 *
 **/

/**
 *
 * @property title
 * @type {String}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * @property alternateTitle
 * @type {String}
 *
 **/

/**
 * This property determines which kind of interaction are possible with 
 * the reported {{#crossLink "GINode"}}node{{/crossLink}}. Possible values are:
 *
 *<ui>
 *<li><h4>composed</h4>
 *<ul><li>a composed {{#crossLink "GINode"}}node{{/crossLink}} can be connected to other 
 * {{#crossLink "GINode"}}nodes{{/crossLink}} in a hierarchically structure</li>
 * <li>this API cannot alter the hierarchically structure of the {{#crossLink "GINode"}}nodes{{/crossLink}} which is defined by the DAB {{#crossLink "DABSource"}}sources{{/crossLink}}</li>
 * <li>composed {{#crossLink "GINode"}}nodes{{/crossLink}} can be {{#crossLink "GINode/expand:method"}}expanded{{/crossLink}} or {{#crossLink "DAB/discover:method"}}discovered{{/crossLink}}</ul></li></li>
 *
 * <li><h4>simple</h4>
 * <ul><li>a simple node cannot have a content, and a call to the {{#crossLink "GINode/expand:method"}}{{/crossLink}} will result in an empty {{#crossLink "ResultSet"}}result set{{/crossLink}}</ul></li>
 * </ui>
 * 
 * @property type
 * @type {String}
 *  
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * @property parentId
 * @type {String}
 * 
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * @property description
 * @type {String}
 * 
 **/

/**
  * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
  *
  * Date expressed in ISO8601 format. E.g.
  * <pre><code>
  *    '2013-05-01';
  * </pre></code>
  * 
  * @property updated
  * @type {String}
  *
  **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 *  This field describes the <a name="repfrmt">format/s</a> of the resource (e.g.: NetCDF, png,etc) linked to the {{#crossLink "GINode"}}node{{/crossLink}}
 *
 * @property format
 * @type {String[]}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * @property rights
 * @type {String[]}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * Datetime expressed in ISO8601 format. E.g.
 * 
 * <pre><code>
 *   created = '2013-05-01T12:00:00Z';<br>
 * </pre></code>
 *   
 * @property created
 * @type {String}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * @property author
 * @type {ContactInfo[]}
 * 
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * @property contributor
 * @type {ContactInfo[]}
 * 
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * @property online
 * @type {OnlineInfo[]}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * @property where
 * @type {Bbox}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * @property when
 * @type {TimePeriod}
 * 
 * **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * Descriptive <a name="repkwd">keywords</a>. 
 *   
 * @property keyword
 * @type {String[]}
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * @property topic
 * @type {String[]}
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * Set of URL to a graphic overview of the linked {{#crossLink "GINode"}}node{{/crossLink}} data 
 * 
 * @property overview
 * @type {String[]}
 * 
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * @property geossCategory
 * @type {String[]}
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *  
 * @property service
 * @type {ServiceInfo}
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *  
 * @property spatialRepresentationType
 * @type {String}
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *  
 * @property dataAuthority
 * @type {String}
 **/


/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *  
 * @property dataIdentifiers
 * @type {String[]}
 **/


/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *  
 * @property coverageAttribute
 * @type {String[]}
 **/

