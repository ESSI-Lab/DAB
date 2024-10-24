/**
 * @module Report 
 **/

/**
 * This object provides info about a service. E.g:
 * <pre><code>
 *   var serviceInfo = {
 *      "title": "service title",
 *      "description": "service description",
 *      "type": "service type",
 *      "version": "service version",
 *      "source": false,
 *      "operation": [
 *          {
 *              "name": "operation name",
 *              "binding": [
 *                  "HTTP-GET"
 *              ],
 *              "online": [
 *                  {
 *                      "url": "http://example.com",
 *                      "name": "some name",
 *                      "description": "some description",
 *                      "function": "download"
 *                  }
 *              ]
 *          }
 *       ]
 *    }
 * </pre></code>
 * 
 * See also {{#crossLink "ServiceOperation"}}{{/crossLink}}<br>
 * See also {{#crossLink "report"}}{{/crossLink}}
 * 
 * @class ServiceInfo
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * @property title
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
 * @property type
 * @type {String}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * @property version
 * @type {String}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 * 
 * Indicates if the current {{#crossLink "GINode"}}node{{/crossLink}} represents a {{#crossLink "DABSource"}}source{{/crossLink}}
 * 
 * @property source
 * @type {Boolean}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * @property operation
 * @type {ServiceOperation[]}
 * 
 **/