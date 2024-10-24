/**
 * @module Report
 **/

/**
 * This object provides info about services operation. E.g.:
 * <pre><code>
 *      var serviceOperation = {
 *           "name": "operation name",
 *           "binding": [
 *                "HTTP-GET"
 *           ],
 *           "online": [
 *                {
 *                    "url": "http://example.com",
 *                    "name": "some name",
 *                    "description": "some description",
 *                    "function": "download"
 *                }
 *            ]
 *       }
 * </pre></code>
 * 
 * See also {{#crossLink "ServiceInfo"}}{{/crossLink}}.<br>
 * See also {{#crossLink "OnlineInfo"}}{{/crossLink}}.<br>
 * See also {{#crossLink "report"}}{{/crossLink}}
 * 
 * @class ServiceOperation
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
 * @property binding
 * @type {String[]}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * @property operations
 * @type {OnlineInfo[]}
 **/