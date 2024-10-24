/**
 * @module Report
 **/

/**
 * This object provides info about a time period expressed in ISO8601 (the time part is optional). E.g.:
 * <pre><code>
 *    // complete time period
 *    var when = {
 *        "from" : "2000-01-01T00:00:00Z",
 *        "to": "2013-01-01"
 *    };
 *    
 *    // time period having only start time
 *    var when = {
 *        "from" : "2000-01-01T00:00:00Z"
 *    }
 * 
 *   // time period having only end time
 *    var when = {
 *        "to" : "2013-01-01"
 *    }
 * </code></pre>
 * 
 * See also <a href="../classes/DAB.html#when">DAB discover 'when constraint'</a>.<br>
 * See also {{#crossLink "report"}}{{/crossLink}}
 *
 * @class TimePeriod
 **/

/**            
 * 
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * If omitted the <code>to</code> property must be specified.<br>
 * Date or datetime expressed in ISO8601 format.
 *
 * @property from
 * @type {String}
 *
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * If omitted the <code>from</code> property must be specified.<br>
 * Date or datetime expressed in ISO8601 format.
 *
 * @property to
 * @type {String}
 *
 **/
