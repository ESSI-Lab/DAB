/**
 * @module ResultSet
 **/

/**
 * This object provides several info about the result of a <a href="../classes/DAB.html#extend">DAB discover extension</a>
 * 
 * @class ResultSetExtension
 **/

/**
 * The sum of the {{#crossLink "ResultSet/size:property"}}size{{/crossLink}} of all the produced {{#crossLink "ResultSet"}}result set{{/crossLink}} 
 * 
 * @property size
 * @type {Integer}
 **/

/**
 * The index of this {{#crossLink "ResultSet"}}result set{{/crossLink}} between 0 and {{#crossLink "ResultSetExtension/width:property"}}{{/crossLink}} - 1
 * 
 * @property resultSetIndex
 * @type {Integer}
 *
 **/

/**
 * Number of {{#crossLink "Paginator"}}paginators{{/crossLink}} produced by the <a href="../classes/DAB.html#extend">discover extension</a>
 * 
 * @property width
 * @type {Integer}
 *
 **/

/**
 * The {{#crossLink "Concept/label:method"}}label{{/crossLink}} of the {{#crossLink "Concept"}}concept{{/crossLink}} related to this 
 * {{#crossLink "ResultSet"}}result set{{/crossLink}} (see <a href="../classes/DAB.html#extend">discover extension</a> for more info)
 * 
 * @property label
 * @type {Array}
 **/

/**
 * The {{#crossLink "ResultSetExtension/label:property"}}label{{/crossLink}} langauge
 * 
 * @property language
 * @type {String}
 **/

/**
 * The {{#crossLink "Concept/uri:method"}}URI{{/crossLink}} of the {{#crossLink "Concept"}}concept{{/crossLink}} related to this 
 * {{#crossLink "ResultSet"}}result set{{/crossLink}} (see <a href="../classes/DAB.html#extend">discover extension</a> for more info)
 *
 * @property uri
 * @type {String}
 **/

/**
 * URI of the Controlled Vocabulary which contains the {{#crossLink "Concept"}}concept{{/crossLink}} related
 * to this {{#crossLink "ResultSet"}}result set{{/crossLink}}
 *
 * @property scheme
 * @type {String}
 **/