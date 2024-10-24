/**
 * @module ResultSet
 * @submodule Refinement
 **/

/**
 * This object provides the {{#crossLink "TermFrequencyItem/freq:property"}}frequency{{/crossLink}} of a 
 * {{#crossLink "TermFrequencyItem/term:property"}}term{{/crossLink}} derived from a <a href="../classes/DAB.html#termfreq">term frequency target</a>. E.g.:
 * 
 * <pre><code>  // term derived from the term frequency target "keyword"
 *   var kwdTerm  = {
 *      "term": "N1+%28ENVISAT%2",
 *      "decodedTerm": "N1 (ENVISAT)",
 *      "freq": 461,
 *   }<br><br>  // term derived from the term frequency target "source"
 *   var srcTerm  = {
 *      "term": "Nasa SVS Image Server",
 *      "decodedTerm": "Nasa SVS Image Server",
 *      "freq": 765,
 *   }
 * </pre></code>
 * 
 * In the example above the <code>term</code> derived from the <a href="../classes/DAB.html#termfreq">term frequency target</a> <i>keyword</i> appears <b>461</b> times in the {{#crossLink "Report"}}nodes report{{/crossLink}} 
 * of the {{#crossLink "ResultSet"}}result set{{/crossLink}} while
 * the <code>term</code> derived from the <a href="../classes/DAB.html#termfreq">term frequency target</a> <i>source</i> indicates that <b>765</b>
 *    {{#crossLink "GINode"}}nodes{{/crossLink}} of the {{#crossLink "ResultSet"}}result set{{/crossLink}} own to the {{#crossLink "DABSource"}}source{{/crossLink}} "Nasa SVS Image Server".<br>
 * 
 * See also <a href="../classes/DAB.html#termfreq">termFrequency option</a><br>
 * See also {{#crossLink "TermFrequency"}}{{/crossLink}}<br>
 * See also {{#crossLink "TermFrequencyWidget"}}{{/crossLink}}<br>
 * 
 * @class TermFrequencyItem
 *
 **/

/**
 * The term of the {{#crossLink "TermFrequencyItem/term:property"}}term{{/crossLink}} of a <a href="../classes/DAB.html#termfreq">term frequency target</a>
 *
 * @property term
 * @type {String}
 *
 **/

/**
 * The term (decoded) of the {{#crossLink "TermFrequencyItem/term:property"}}term{{/crossLink}} of a <a href="../classes/DAB.html#termfreq">term frequency target</a>
 * 
 * @property decodedTerm
 * @type {String}
 *
 **/

/**
 * The frequency at which <code>term</code> appears in the {{#crossLink "Report"}}nodes report{{/crossLink}}
 *  
 * @property freq
 * @type {String}
 *
 **/