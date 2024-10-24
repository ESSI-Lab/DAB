/**
 * This module contains the {{#crossLink "ResultSet"}}{{/crossLink}} and {{#crossLink "ResultSetExtension"}}{{/crossLink}} objects, and also
 * contains objects to <a href="../modules/Retrieval.html">retrieve</a> and <a href="../modules/Refinement.html">refine</a> its content  
 * 
 * @module ResultSet
 * @main ResultSet
 **/

/**
 * This object is provided as result of a {{#crossLink "DAB/discover:method"}}{{/crossLink}} or {{#crossLink "GINode/expand:method"}}{{/crossLink}}/{{#crossLink "GINode/expandNext:method"}}{{/crossLink}} operation 
 * and has several information such as the number of the returned {{#crossLink "GINode"}}nodes{{/crossLink}} (the <a href="#property_size">size</a> of the result set) and the <a href="#property_pageCount">number of pages</a> with which the result set is split. 
 * 
 * <h4>Retrieving result set content</a></h4>
 * 
 * The result set {{#crossLink "GINode"}}nodes{{/crossLink}} can be 
 *  <a href="../modules/Retrieval.html" target=_blank>retrieved</a> depending on the executed operation by means of one of the following properties:<ul><li>{{#crossLink "DAB/discover:method"}}discover{{/crossLink}}: by means of the {{#crossLink "Paginator"}}{{/crossLink}} object in the <a href="#property_paginator" target=_blank>paginator</a> property</li><li>{{#crossLink "GINode/expand:method"}}{{/crossLink}}/{{#crossLink "GINode/expandNext:method"}}{{/crossLink}}: by means of the {{#crossLink "Page"}}{{/crossLink}} object in the <a href="#property_page" target=_blank>page</a> property</li></ul>
 * In case of error or <a href="../classes/DAB.html#timeout" target=_blank>timeout</a>, none of the above properties is provided 
 * (see also <a href="../classes/DAB.html#onResponseError">response.error</a> property)
 * 
 * <h4><a name="resSetRef">Refining the result</a> set content of a {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} operation</a></h4> 
 * 
 * The <a href="../modules/Refinement.html" target=_blank>refinement</a> allows 
 * to decrease the <a href="#property_size">size</a> of the result set adding constraints to the last performed {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} 
 * in a simplified way. A result set of a {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} can be <i>refined</i> by means of one
 * of the following properties:<ul><li><a href="#property_termFrequency">termFrequency</a> 
 * (see also <a href="../classes/DAB.html#termfreq" target=_blank>discover termFrequency</a> option)</li><li><a href="#property_refiner">refiner</a></li></ul>
 * 
 * The availability of the above optional properties, depends on the following result set features:
 * 
 * <table>
 * 	<tr><th></th><th>Result set content is totally or partially <a href="../classes/DABSource.html#harv">harvested</a></th><th>Result set content is totally <a href="../classes/DABSource.html#harv">distributed</a></th><th><a href="../classes/DAB.html#extend" target=_blank>Extended</a> result set</th><th>Discover <a href="../classes/DAB.html#onResponseError" target=_blank>error</a> or <a href="../classes/DAB.html#timeout" target=_blank>timeout</a></th></tr>
 *  <tr><th>TermFrequency</th><td>YES</td><td>-</td><td>-</td><td>-</td></tr>
 *  <tr><th>Refiner</th><td>YES</td><td>YES</td><td>-</td><td>-</td></tr>
 * </table><br>
 * 
 * @class ResultSet
 **/

/**
 * The start index of the current {{#crossLink "GINode"}}node{{/crossLink}} between 1 and {{#crossLink "ResultSet/size:property"}}{{/crossLink}}.<br>
 * After a {{#crossLink "DAB/discover:method"}}DAB discover query{{/crossLink}},
 * this index depends from the <a href="../classes/DAB.html#start">discover start option</a>. After a call of {{#crossLink "Paginator"}}{{/crossLink}}
 *  methods {{#crossLink "Paginator/next:method"}}{{/crossLink}} or {{#crossLink "Paginator/prev:method"}}{{/crossLink}} this index increments
 * or decrements of a value equals to {{#crossLink "ResultSet/pageSize:property"}}{{/crossLink}}
 * 
 * @property start
 * @type {Integer}
 **/

/**
 * Number of {{#crossLink "GINode"}}nodes{{/crossLink}} of this result set
 * 
 * @property size
 * @type {Integer}
 **/

/**
 * Size of this result set {{#crossLink "Page"}}pages{{/crossLink}}. This value depends on the <a href="../classes/DAB.html#pageSize">discover pageSize option</a>
 * 
 * @property pageSize
 * @type {Integer}
 **/

/**
 * Number of {{#crossLink "Page"}}pages{{/crossLink}} of this result set
 * 
 * @property pageCount
 * @type {Integer}
 **/

/**
 * Index of the {{#crossLink "Paginator/page:method"}}current page{{/crossLink}} between 1 and {{#crossLink "ResultSet/pageCount:property"}}{{/crossLink}}
 * 
 * @property pageIndex
 * @type {Integer}
 **/


/**
 * This property is set only if the <a href="../classes/DAB.html#extend" target=_blank>discover extension</a> has produced more than one result set
 * 
 * @property extension
 * @type {resultSetExtension}
 * 
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * The {{#crossLink "Paginator"}}{{/crossLink}} resulting from a {{#crossLink "DAB/discover:method"}}{{/crossLink}} operation.<br>
 * In case of error or <a href="../classes/DAB.html#timeout" target=_blank>timeout</a> this property is omitted (see also <a href="../classes/DAB.html#onResponseError">response.error</a> property)

 * @property paginator
 * @type {Paginator}
 * 
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>

 * The {{#crossLink "Refiner"}}{{/crossLink}} <a name="refiner">object</a>. See <a href="#resSetRef">this section</a> for more info

 * @property refiner
 * @type {Refiner}
 * 
 **/

/**
 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
 *
 * The {{#crossLink "TermFrequency"}}{{/crossLink}} <a name="termFrequency">object</a>. See <a href="#resSetRef">this section</a> for more info

 * @property termFrequency
 * @type {TermFrequency}
 * 
 * 
 **/