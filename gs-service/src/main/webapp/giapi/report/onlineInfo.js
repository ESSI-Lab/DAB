/**
 * @module Report 
 **/

/**
 * This object provides info about an online resource. E.g.:
 * 
 * <pre><code>
 *   var onlineInfo  = {
 *      "url": "http://example.com",
 *      "name": "some name",
 *      "description": "some description",
 *      "function": "download",
 *      "protocol": "urn:ogc:serviceType:WebMapService:1.1.1:HTTP"
 *      "accessType": "direct"
 *   }
 * </pre></code>
 * 
 * See also {{#crossLink "report"}}{{/crossLink}}
 * 
 * @class OnlineInfo
 *
 **/

/**
 *
 * @property url
 * @type {String}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * @property name
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
 * @property function
 * @type {String}
 *
 **/

/**
 * 
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * The online access <a name="onlprot">protocol/s</a> of the data linked to the node.
 * 
 * 
 * @property protocol
 * @type {String}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * Defines how the <code>url</code> can be accessed; possible values are:<ul>
 * <li><i>unknown</i>: the access type is not recognized</li>
 * <li><i>noaccess</i>: <code>url</code> can't be used to download the data. It could be a link to an information site or a report about the node</li>
 * <li><i>service</i>: <code>url</code> refers to a WEB service</li>
 * <li><i>simple</i>: <span class="flag deprecated">!</span>Replaced by <code>direct</code><span class="flag deprecated">!</span>
 * <li><i>direct</i>: <code>url</code> refers to a resource that can be directly accessed</li>
 * <li><i>complex</i>: <code>url</code> is the "base URL" of the service providing the data</li>
 * </ul>
 * @property accessType
 * @type {String}
 *
 **/