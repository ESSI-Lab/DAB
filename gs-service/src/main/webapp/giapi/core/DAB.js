/**
 * @module Core
 **/

/**
 * <b>The API entry point</b>. This object provides the ability to create a {{#crossLink
"GINode"}}node{{/crossLink}} connected to an existing
 * DAB server instance. DAB is a <i>composed</i> {{#crossLink "GINode"}}node{{/crossLink}} and it's
 * the root of the hierarchical structure defined by the DAB {{#crossLink "DAB/sources:method"}}brokered sources{{/crossLink}}.<br>
 * The main DAB operation allows to {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}
 * any nodes regardless of their level in the hierarchy.
 *
 * <h4>How to create a DAB instance</h4>
 * In order to minimize the use of global variables {{#crossLink "DAB"}}DAB
 * object{{/crossLink}} is defined as a member of {{#crossLink "GIAPI"}}{{/crossLink}}. E.g.:
 *
 * <pre><code>// remote endpoint
 * var remoteDab = GIAPI.DAB('https://api.geodab.eu/dab/');
 * 
 * // local endpoint 
 * var localDab = GIAPI.DAB('./');<br>
 * </pre></code>
 *
 * See also {{#crossLink "GIAPI"}}{{/crossLink}}.
 * 
 * <h4><a name='kvp_list'>Basic and advanced</a> constraints</h4>
 * 
 * The {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} supports <i>basic</i> and <i>advanced</i> constraints.
 * 
 * - <b>Basic constraints</b>:<br><br>
<ul>
<li><a href="../classes/DAB.html#what" target=_blank>what</a></li>
<li><a href="../classes/DAB.html#where" target=_blank>where</a></li>
<li><a href="../classes/DAB.html#when" target=_blank>when</a></li>
<li><a href="../classes/DAB.html#who" target=_blank>who</a></li>
</ul>

 * 
 * - <b>Advanced constraints</b>:<br>
 * 
 * The following parameters expressed as <a href="#kvp_params">key-value</a> pair allow to set advanced constraints.
 * 
 * 
<ul><li><i>Miscellaneous</i></li>
General purpose constraints. 
<ul><br><li><code>loc</code>: name of a location where to constraint the {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}. It can be used instead of the <code>where</code> constraint. E.g: "italy", "u.s.a", "africa"</li>
<li><code>gdc</code>: includes only or excludes all the "GEOSS Data Core" datasets in the/from the {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}. Possible values are 'true' or 'false'</li>
<li><code>sources</code>: comma separated list of {{#crossLink "DABSource"}}sources{{/crossLink}} identifiers; only the related {{#crossLink "DABSource"}}sources{{/crossLink}} are included in the  {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}. This is an alternative way to the {{#crossLink "DABSource/include:method"}}DABSource.include{{/crossLink}} method</li>
<li><code>sba</code>: a "GEO Societal Benefit Area". E.g.:"agriculture", "climate", "disasters"</li>
<li><code>hl</code>: the {{#crossLink "Report/type:property"}}type{{/crossLink}} of {{#crossLink "GINode"}}node{{/crossLink}} to {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}. Possible values are: "dataset" for simple {{#crossLink "GINode"}}nodes{{/crossLink}}, "series" for composed {{#crossLink "GINode"}}nodes{{/crossLink}}</li> 
<li><code>prot</code>: a protocol used to access the data linked to the {{#crossLink "DAB/discover:method"}}discovered{{/crossLink}} nodes. E.g: "HTTP", "urn:ogc:serviceType:WebMapService:1.1.1:HTTP", "OGC:WMS-1.1.1-http-get-map"</li>
<li><code>frmt</code>: a format of the data linked to the {{#crossLink "DAB/discover:method"}}discovered{{/crossLink}} nodes. E.g.: "image/gif", "application/zip"</li>
<li><code>kwd</code>: a keyword which describes the nodes to {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}</li>
</ul></ul>

<ul><li><i>Legal constraints</i></li>
These constraints can be used to filter the nodes basing on their legal constraints.
<ul><br><li><code>uselim</code>: limitation applied on the use of the data linked to the {{#crossLink "DAB/discover:method"}}discovered{{/crossLink}} {{#crossLink "GINode"}}nodes{{/crossLink}} (string)</li>
<li><code>lac</code>: includes only or excludes all the nodes having some legal access constraints. Possible values are 'true' or 'false'</li>
<li><code>luc</code>: includes only or excludes all the nodes having some legal use constraints. Possible values are 'true' or 'false'</li>
</ul></ul>

<ul><li><i>IRIS Event constraints</i></li>
These constraints affect only the <a href="http://service.iris.edu/fdsnws/event/1/" target=_blank>"IRIS Event"</a> {{#crossLink "DABSource"}}source{{/crossLink}} ({{#crossLink "ServiceInfo/type:property"}}type{{/crossLink}} = "IRIS_EVENT").  
<ul><br><li><code>minmag</code>: integer value of the minimum magnitude</li>
<li><code>maxmag</code>: integer value of the maximum magnitude</li>
<li><code>magt</code>: the type of magnitude (case insensitive). E.g.: "ML", "Ms", "mb", "Mw", "all", "preferred"</li>
<li><code>mind</code>: limit to events with depths equal to or greater than the specified depth. Double value expressed in kilometers</li>
<li><code>maxd</code>: limit to events with depths less than or equal to the specified depth. Double value expressed in kilometers</li>
<li><code>evtOrd</code>: ordering of  the results. Possible values are: "time" and "magnitude"</li>
</ul></ul>

<ul><li><i>INPE constraints</i></li>
These constraints affect only the <a href="http://www.dgi.inpe.br/CDSR/" target=_blank>"INPE CDSR"</a> {{#crossLink "DABSource"}}source{{/crossLink}} ({{#crossLink "ServiceInfo/type:property"}}type{{/crossLink}} = "INPE").  
<ul><br><li><code>inpe-sat-name</code>: satellite name (string). E.g.: "AQUA"</li>
<li><code>inpe-instr-name</code>: instrument name (string). E.g.: "MODIS"</li>
</ul></ul>

<ul><li><i>Earth Observation constraints</i></li>
Specific Earth Observation constraints.


<ul><br><li><code>sta</code>: station description (string)</li>
<li><code>sensor</code>: sensor description (string)</li>
<li><code>bandwl</code>: histogram mean (double)</li>
<li><code>proclev</code>: processing level (double)</li>
<li><code>illazan</code>: illumination azimuth angle (double)</li>
<li><code>illzean</code>: illumination zenith angle value (double)</li>
<li><code>cloudcp</code>: cloud cover percentage (double)</li>

<li><code>sarPolCh</code>: polarisation channels (string)</li>
<li><code>sarPolMd</code>: polarisation mode (string)</li>

<li><code>sensorResolution</code>: sensor resolution (double)</li>
<li><code>sensorResolutionMax</code>: maximum sensor resolution (double)</li>
<li><code>sensorResolutionMin</code>: minimum sensor resolution (double)</li>
</ul></ul>
 
 
 *
 * @class DAB
 * @constructor
 * @param {String} endpoint The endpoint of the DAB instance to connect
 * @param {String} [viewId] a "server-side" view identifier
 * @param {String} [servicePath] default: "services"
 * @param {String} [cswPath] default: "cswiso"
 * @param {String} [openSearchPath] default: "opensearch"

 * @extends GINode
 **/

import { GIAPI } from './GIAPI.js';

GIAPI.DAB = function(dabEndpoint, viewId, servicePath, cswPath, openSearchPath) {

    var report = {
        id : 'ROOT',
        type : 'composed',
        title : 'DAB'
    };
    
    if(!servicePath){
    	servicePath = 'services';
    }
    
    if(!cswPath){
    	cswPath = 'cswiso';
    }
    
    if(!openSearchPath){
    	openSearchPath = 'opensearch';
    }

    var dabNode = GIAPI.GINode(report, dabEndpoint, servicePath,openSearchPath);
    var timer;
    var sourcesCount;
    var view;
    
    GIAPI.tfHelper = {};
    // keeps the checked items between the discover requests
    GIAPI.tfHelper.checkedItems = null;
    
    var pubSubManager = GIAPI.PubSubManager(dabNode);
     
    dabNode._paginator = function(onResponse, constraints, options, onStatus) {

        var queryID = GIAPI.random();     
        var sources = dabNode.findSources(constraints);

        var query = GIAPI.query(
        		dabEndpoint,//
        		constraints,//
        		options,//
        		null,//
        		null,//
        		sources,//
        		queryID,//
        		viewId,//
        		servicePath,//
        		openSearchPath);

        var paginator = arguments[4];
        var extension = paginator._resultSet.extension;
        var extended = options && options.extension && options.extension.relation && (options.extension.keyword || options.extension.concepts);

        if (onStatus) {
            timer = createTimer(onStatus, extended, queryID);
        }

        jQuery.ajax({

            type : 'GET',
            url : query,
            crossDomain : true,
            dataType : 'jsonp',

            success : function(data, status, jqXHR) {

                var nodes = [];
                for (var i = 0; i < data.reports.length; i++) {
                    var report = data.reports[i];
                    report = JSON.parse(JSON.stringify(report));
                    
                    if (report.semantic) {
                        delete (report.semantic);
                    }
                    var n = GIAPI.GINode(report, dabNode, servicePath);
                    nodes.push(n);
                }

                paginator._page = GIAPI.Page(nodes, paginator.pageSize);
                                          
                // -- new implementation **
                data.resultSet.paginator = paginator;
                // _resultSet makes the paginator ready again! (maintained)
                paginator._resultSet = data.resultSet; 
                               
                if (extension) {
                	data.resultSet.extension = extension;
                }// else called from a paginator having a single result set
                // ** END of new implementation --
                
                createRefiners(
                		data.termFrequency,
                		onResponse,
                		constraints,
                		data.resultSet, 
                		options, 
                		onStatus,
                		dabNode,
                		paginator);
                
                var response = [data.resultSet];
                response._origin = 'paginator';
                
                onResponse.apply(dabNode, [response]);

                if (timer) {
                    timer.stop();
                }
            },

            complete : function(jqXHR, status) {

                GIAPI.logger.log('_paginator complete status: ' + status);

                if (timer) {
                    timer.stop();
                }
            },

            error : function(jqXHR, msg, exception) {

                error(msg, exception, onResponse);
            }
        });
    };
    
    /**
     * <h4>Defining a "client-side" <a name="view">view</a> on the {{#crossLink "ResultSet"}}result set{{/crossLink}}</h4>
	 * 
	 * A "client-side" <i>view</i> is a way to limit the {{#crossLink "ResultSet"}}result set{{/crossLink}} on one or more specific constraint like <a href="../classes/DAB.html#where" target=_blank>where constraint</a>, thus providing a "view" of the entire {{#crossLink "ResultSet"}}result set{{/crossLink}}.
	 * Additional constraints used in the {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} are merged with the view constraints in order to further refine the initial {{#crossLink "ResultSet"}}result set{{/crossLink}}.<br> 
	 * For example, a <a href="../../view-demo.html" target=_blank>web portal</a> can provide earthquakes info only on the west coast of south America, by defining a view with the 
	 * <a href="../classes/DAB.html#where" target=_blank>where constraint</a> covering that area.
	 * When the users make a search they can retrieve only results from the area defined in the view; furthermore they can add, for example, <a href="../classes/DAB.html#when" target=_blank>when constraint</a> 
	 * to retrieve results in a particular temporal period.<br><br> 
     * 
     * @method view
     * @param {String} [action] possible values are: "enable" to enable the view, "disable" to disable it. Can be omitted if this method is used only as "get" method
     * @param {Object} [constraints] the view constraints (unnecessary if the <code>action</code> is "disable" or if this method is used only as "get" method)
     * @return the current view constraints
     */
    dabNode.view = function(action, constraints) {
    	
    	switch(action){
    	case "enable":
    		view = constraints;
    		break;
    	case "disable":
    		view = null;
    		break;
    	}
    	
    	return view;
    };

    // @formatter:off
    /**
     * While the {{#crossLink "GINode/expand:method"}}{{/crossLink}} is limited to the node children,
     * the {{#crossLink "ResultSet"}}result set{{/crossLink}} of a discover query contains any {{#crossLink "GINode"}}{{/crossLink}}
     * with a {{#crossLink "Report"}}report{{/crossLink}} matching the given <code>constraints</code> (if any), regardless of their level in the
     * hierarchy.
     *
     * <h4><a name="extend">Extending a discover {{#crossLink "ResultSet"}}{{/crossLink}}</a></h4>
     *
     * Normally the result of this method is a single {{#crossLink "ResultSet"}}{{/crossLink}}. When the <code>extension</code> option is used,
     * the discover might generate more than one {{#crossLink "ResultSet"}}{{/crossLink}} according to the following rules:
     *
     * <ol>
     *     <li>generation of {{#crossLink "Concept"}}concepts{{/crossLink}} list</li>
     *     <li>execution of a specific query for each {{#crossLink "Concept"}}concept{{/crossLink}} in the list</li>
     *     <li>creation of a {{#crossLink "ResultSet"}}{{/crossLink}} for each query with at least one result</li>
     * </ol>
     * The {{#crossLink "ResultSet"}}{{/crossLink}} can be extended <i>by keyword</i> or by {{#crossLink "Concept"}}concepts{{/crossLink}}.
     * In the first case the list of {{#crossLink "Concept"}}concepts{{/crossLink}} is generated by
     * retrieving concepts related to the provided keyword (see also {{#crossLink "DAB/concept:method"}}concept method{{/crossLink}}). In the second case the list of
     * {{#crossLink "Concept"}}concepts{{/crossLink}} is the provided one.
     * Besides, if the provided <code>relation</code> is different from {{#crossLink "Relation/NONE:property"}}{{/crossLink}},
     * the generated list contains also {{#crossLink "Concept"}}concepts{{/crossLink}} satisfying the
     * provided <code>relation</code>.
     *
     * The method throws an <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> if the extension option is not properly configured (see
     * <code>extension</code> option for more info)
     *
     * @method discover
     * @async
     *
     * @param {Function} onResponse <a name="onResponse">onResponse</a> Callback function for receiving asynchronous query response
     *
     * @param {Array} onResponse.response <a name="response">Array</a> of {{#crossLink "ResultSet"}}{{/crossLink}} resulting
     * from the query
     * @param {String} onResponse.response.error in case of <a name="onResponseError">error</a> or timeout (see <code>options.timeout</code>), 
     * the response array contains an empty {{#crossLink "ResultSet"}}result set{{/crossLink}} and this property describes the problem occurred
     * 
     * @param {Object} [constraints] Only nodes matching the given <a name="constraints">constraints</a> is returned as
     * result of the {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}. The constraints argument object supports the following properties:
     *
     * @param {Bbox} [constraints.where] <a name="where">A</a> {{#crossLink "Bbox"}}bounding box{{/crossLink}} which includes the results. See also <a href="#sprel" class="crosslink">spatial relation option</a>
     * @param {TimePeriod} [constraints.when] <a name="when">A</a> {{#crossLink "TimePeriod"}}time period{{/crossLink}} which includes the results
     * 
     * @param {String/Array} [constraints.what] <a name="what">A</a> single search keyword or an array of search
     * keywords; in case of multiple keywords, unless specified with the <code>options.searchOperator</code>, the search terms are related by <i>OR</i> logic operator. E.g:<pre><code>     constraints.what = "sst";
     *      constraints.what = &#91;"sst","temperature"&#93;;
     *      options.searchOperator = "AND";
     * </pre></code>

     * As default the search is performed
     * on the {{#crossLink "Report/title:property"}}report title{{/crossLink}} and {{#crossLink "Report/keyword:property"}}report keyword{{/crossLink}} fields; the <code>options.searchFields</code> argument
     * can be used to specify different fields. E.g.:<pre><code>    options.searchFields = "title,abstract";
     * </pre></code>
     *
     * @param {String/Array} [constraints.who] <a name="who">A</a> single {{#crossLink "Report/id:property"}}report id{{/crossLink}} or
     * an array {{#crossLink "Report/id:property"}}report id{{/crossLink}}; only the content of the correspondent
     * {{#crossLink "GINode"}}nodes{{/crossLink}} (matching the above constraints if any) is discovered.
     * In case of {{#crossLink "Report/type:property"}}simple nodes{{/crossLink}} (which have no content), they are always added to the {{#crossLink "ResultSet"}}result set{{/crossLink}}
     * regardless of the above constraints (if any).E.g:<pre><code>     constraints.who = "id1";
     *      constraints.who = &#91;"id1,"id2,"id3"&#93;;
     * </pre></code>
     *
     * @param {Object/Array} [constraints.kvp] <a name="kvp_params">A single advanced constraint</a> or array of advanced constraints submitted in the key-value pair format. E.g.:<pre><code>	constraints.kvp = {"key":"minmag","value":"6"};
     * 	constraints.kvp = &#91;{"key": "minmag", "value": "6"},{{"key": "magt", "value": "MWW"}}&#93;;
     * </pre></code>
     * See <a href="#kvp_list">here</a> for a complete list of supported key-value pair parameters.
     * @param {Object} [options] This <a name="options">parameter</a> object supports the following properties:
     *
     * @param {Integer} [options.start=1] The <a name="start">start index</a> of the first {{#crossLink "ResultSet"}}result set{{/crossLink}} {{#crossLink "GINode"}}node{{/crossLink}}, starting from 1
     * @param {Integer} [options.pageSize=10] The <a name="d_pageSize">size</a> of the {{#crossLink "ResultSet"}}result set{{/crossLink}} {{#crossLink "Page"}}pages{{/crossLink}}
     * @param {Object} [options.extension] This option allows to <a href="../classes/DAB.html#extend">extend</a> the discover and has the following properties:
     *
     * @param {Relation} options.extension.relation <a name="relation">The</a> <a href="../classes/Relation.html" class="crosslink">relation</a> to be used for the extension. If
     * different from <a href="../classes/Relation.html#property_NONE" class="crosslink">NONE</a> and neither <code>keyword</code> nor <code>concepts</code> are set, an <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown
     * @param {String} [options.extension.keyword] The <a name="keyword">keyword</a> to used to generate the extension concepts. If the  <code>concepts</code> is also set, an <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown
     * @param {Array} [options.extension.concepts] <a name="concepts">The</a> array of <a href="../classes/Concept.html" class="crosslink">concepts</a> to use for the extension.
     * If  <code>keyword</code> is also set, an <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown
     *
     * @param {Integer} [options.timeout] set a request <a name="timeout">timeout</a> (in milliseconds)
     * @param {String} [options.searchFields="title,keyword"] A single <a name="searchFields">search field</a> or a comma separated list of search fields in which to perform the keyword search (see <code>constraints.what</code>). Possible search fields are:
     * <ul>
     *   <li><b>title</b> <u>(default</u>): the search is performed in the {{#crossLink "Report/title:property"}}report title{{/crossLink}} </li>
     *   <li><b>keyword</b> <u>(default)</u>: the search is performed in the {{#crossLink "Report/title:property"}}report keyword{{/crossLink}}  </li>
     *   <li><b>anytext</b>: the search is performed in the whole textual content of the {{#crossLink "GINode/report:method"}}{{/crossLink}} </li>
     *   <li><b>description</b>: the search is performed in the {{#crossLink "Report/title:property"}}report description{{/crossLink}} </li>
     * </ul>
     * @param {String} [options.searchOperator="OR"] <a name="searchOp">the logic operator</a> for multiple keywords in the <code>constraints.what</code> constraints. Possible values are: "OR" and "AND"
     * @param {SpatialRelation} [options.spatialRelation]  <a name="sprel">The</a> {{#crossLink "SpatialRelation"}}spatial relation{{/crossLink}} used to include the results in the
     *  {{#crossLink "Bbox"}}bounding box{{/crossLink}} selected with the <a href="#where" class="crosslink">where constraint</a>
     * @param {String} [options.termFrequency="keyword,format,protocol,source"] One or more comma separated list of <a name="termfreq"><i>term frequency target</i></a>; 
     * by default all the <i>term frequency targets</i> are selected.<br>
     *  Possible <i>term frequency targets</i> are:
     * <ul>
     *   <li><b>keyword</b> </li>
     *   <li><b>format</b> </li>
     *   <li><b>protocol</b> </li>
     *   <li><b>source</b> </li>
     * </ul><br>
     * See <a href="../classes/ResultSet.html#resSetRef">here</a> for more info
       
     * @param {Function} [onStatus] Callback function called every second for receiving asynchronous {{#crossLink "SourceStatus"}}source status info{{/crossLink}}
     * @param {Array} onStatus.status Every second, this argument is updated with an array of {{#crossLink "SourceStatus"}}{{/crossLink}}, one for each {{#crossLink
     * "DABSource"}}DAB source{{/crossLink}} plus another representing the {{#crossLink "DAB"}}{{/crossLink}} node with the status of the whole process and with
     * {{#crossLink "SourceStatus/title:method"}}title{{/crossLink}} "DAB"
     **/
    dabNode.discover = function(onResponse, constraints, options, onStatus) {
        // @formatter:on
    	
    	var con = isConstraints(constraints) || isConstraints(options) || isConstraints(onStatus) || {};
    	if(view){
    		con = GIAPI.mergeConstraints(con, view);
    	}
    	
        var opt = isOptions(constraints) || isOptions(options) || isOptions(onStatus) || {};
    	
        var ext = opt && opt.extension ? opt.extension : null;
        if (ext && ext.relation && (!ext.keyword && !ext.concepts)) {
            throw 'Unable to expand discover, only relation selected. Please select keyword or concepts';
        }
        if (ext && !ext.relation && (ext.keyword || ext.concepts)) {
            var msg = ext.keyword ? 'keyword selected' : 'concepts selected';
            throw 'Unable to expand discover, only ' + msg + '. Please select a relation';
        }
             
        // if the discover is started from a term frequency object, the 
        // start index is reset
        if(isTermFrequencyQuery(con)){
            opt.start = 1;
            arguments[2].start = 1;
        }else{
            // if the discover is not started from a term frequency object, the 
            // list of checked items is deleted       
            GIAPI.tfHelper.checkedItems = null;       
        }
        
        var queryID = GIAPI.random();   
     
        var sources = dabNode.findSources(con);
        var extended = opt && opt.extension && opt.extension.relation && (opt.extension.keyword || opt.extension.concepts);
        
        // no term frequency option with extended query
        if(extended && opt.termFrequency){
            delete(opt.termFrequency);
        }
        
        var query = GIAPI.query(dabEndpoint, con, opt, null, null, sources, queryID, viewId, servicePath,openSearchPath);

        var ons = isOnStatus(constraints) || isOnStatus(options) || isOnStatus(onStatus);
        if (ons) {
            timer = createTimer(ons, extended, queryID);
        }
        
        // this array organization is made for the paginator
        var discoverArgs = [];
        discoverArgs[1] = con;
        discoverArgs[2] = opt;
        discoverArgs[3] = ons;      
        discoverArgs[4] = dabNode;      

        var timeo = (opt && opt.timeout) ? opt.timeout : 0;
        
        jQuery.ajax({

            type : 'GET',
            url : query,
            crossDomain : true,
            dataType : 'jsonp',
            timeout : timeo,

            success : function(data, status, jqXHR) {

                if (data.resultSet && data.resultSet.error) {
                    GIAPI.logger.log('discover error: ' + data.resultSet.error, 'error');
                    
                    var response = [data.resultSet];
                    // error is a result set property
                    response.error = data.resultSet.error;
                    
                    delete data.resultSet.error;
                    
                    onResponse.apply(dabNode, [response]);

                    return;
                }

                var response = [];
                response._origin = 'dab';
                
                // ***
                // Extended query
                // ***
                if (discoverArgs[2] && discoverArgs[2].extension && data.reports.length > 0) {

                    var nodesMap = {};
                    var width = 0;

                    for (var i = 0; i < data.reports.length; i++) {

                        var report = data.reports[i];

                        report = JSON.parse(JSON.stringify(report));

                        var matchKeyword = getMatchKeyword(report.semantic.matchKeyword);

                        var val = nodesMap[matchKeyword];

                        // using notIn avoid to put nodes having the same
                        // matchKeyword with different case
                        if (!val && notIn(nodesMap, matchKeyword)) {
                            val = [];
                            nodesMap[matchKeyword] = val;
                            width++;
                        }

                        if (val) {
                            val.push(GIAPI.GINode(report, dabNode, servicePath));
                        }
                    }

                    for (var matchKeyword in nodesMap) {

                        var nodes = nodesMap[matchKeyword];
                        var sem = nodes[0].report().semantic;

                        // deleting semantic property avoiding redundant info already
                        // available in the paginator.extension property
                        delete (nodes[0].report().semantic);

                        var args = GIAPI.clone(discoverArgs);
                        args.length = discoverArgs.length;
                        args.callee = discoverArgs.callee;
                        //args[args.length] = dabNode;

                        // setting 'what' argument that will be used as what constraint
                        // by the paginators to execute the discover query
                        var split = matchKeyword.split(',');
                        if (split.length === 1) {
                            args[1].what = matchKeyword;
                        } else {
                            // if the matchKeyword has multiple terms (e.g. solar radation, radizione solare) 
                            // they are split using the OR operator
                            var w = '';
                            for (var k = 0; k < split.length; k++) {
                                w += split[k].trim();
                                if (k < split.length - 1) {
                                    w += ' OR ';
                                }
                            }
                            args[1].what = w;
                        }

                        args[1].subject = null;
                        args[1].relation = null;
                        args[2].extension = null;

                        var resSet = {};
                        resSet.size = sem.matchCount;
                        resSet.pageSize = parseInt(data.resultSet.pageSize);
                        resSet.start = 1;
                        resSet.pageCount = pageCount(resSet.size, resSet.pageSize);
                        resSet.pageIndex = parseInt(data.resultSet.pageIndex);

                        resSet.extension = {};
                        resSet.extension.size = data.resultSet.size;
                        resSet.extension.resultSetIndex = response.length;
                        resSet.extension.label = matchKeyword;
                        resSet.extension.uri = sem.term || 'none';
                        resSet.extension.scheme = sem.scheme || 'none';
                        resSet.extension.language = sem.label && sem.label.language ? sem.label.language : 'none';
                        resSet.extension.width = width;

                        var paginator = GIAPI.Paginator(args);                        
                        paginator._page = GIAPI.Page(nodes, paginator.pageSize);
                        
                        // -- new implementation: paginator is a result set property
                        paginator._resultSet = resSet;// must be maintained!                       
                        resSet.paginator = paginator;
                        
                        response.push(resSet);
                    }
                    
                    // Preserve bboxUnion from server response if present (for extended queries)
                    if (data.bboxUnion) {
                    	response.bboxUnion = data.bboxUnion;
                    }
                } else {
                    // ***
                    // Normal query
                    // ***
                    var nodes = [];

                    //discoverArgs[discoverArgs.length] = dabNode;

                    for (var i = 0; i < data.reports.length; i++) {

                        var report = data.reports[i];                        
                        report = JSON.parse(JSON.stringify(report));
                        
                        nodes.push(GIAPI.GINode(report, dabNode, servicePath));
                    }

                    var paginator = GIAPI.Paginator(discoverArgs);              
                    paginator._page = GIAPI.Page(nodes, paginator.pageSize);
                                      
                    // -- new implementation: paginator is a result set property
                    data.resultSet.paginator = paginator;
                    data.resultSet.paginator._resultSet = data.resultSet; // must be maintained !!!
                    
                    createRefiners(
                    		data.termFrequency,
                    		onResponse,
                    		con,
                    		data.resultSet, 
                    		options, 
                    		onStatus,
                    		dabNode,
                    		paginator);
                                       
                    response.push(data.resultSet);
                    
                    // Preserve bboxUnion from server response if present
                    if (data.bboxUnion) {
                    	response.bboxUnion = data.bboxUnion;
                    }
//                    console.log(JSON.stringify(data.reports));
                }
                                               
                onResponse.apply(dabNode, [response]);

                if (timer) {
                    timer.stop();
                }
            },

            complete : function(jqXHR, status) {

                GIAPI.logger.log('discover complete status: ' + status);

                if (timer) {
                    timer.stop();
                }
            },

            error : function(jqXHR, msg, exception) {

                error(msg, exception, onResponse);
            }
        });
    };
    
    /**
    *
    * Retrieves the {{#crossLink "GINode"}}node{{/crossLink}} with the given {{#crossLink "Report/id:property"}}report id{{/crossLink}}
    *
    * @method find
    * @async
    * @param {String} id The {{#crossLink "GINode"}}node{{/crossLink}} {{#crossLink "Report/id:property"}}report id{{/crossLink}}
    * @param {Function} onResponse Callback function for receiving asynchronous query response
    * @param {GINode} onResponse.result The requested {{#crossLink "GINode"}}node{{/crossLink}} object. If a {{#crossLink "GINode"}}node{{/crossLink}} with the given
    * {{#crossLink "Report/id:property"}}report id{{/crossLink}} does not exist or in case of error, this argument is <code>null</code>
    * @param {String} [onResponse.error] In case of error, this argument contains a message which describes the problem occurred
    **/
   dabNode.find = function(id, onResponse) {

       var query = GIAPI.query(
    		   dabEndpoint,
    		   null,
    		   null,
    		   null,
    		   id,
    		   null,
    		   null,
    		   null,
    		   servicePath,
    		   openSearchPath);
 
       jQuery.ajax({

           type : 'GET',
           url : query,
           crossDomain : true,
           dataType : 'jsonp',

           success : function(data, status, jqXHR) {

               if (data.resultSet && data.resultSet.error) {
                   GIAPI.logger.log('node error: ' + data.resultSet.error, 'error');
                   onResponse.apply(dabNode, [null, 'Error occurred:' + data.resultSet.error]);

                   return;
               }

               var node = null;
               if (data.reports.length) {

                   var report = data.reports[0];
                   node = GIAPI.GINode(report, dabNode, servicePath);
               }

               onResponse.apply(dabNode, [node]);
           },

           complete : function(jqXHR, status) {

               GIAPI.logger.log('node complete status: ' + status);
           },

           error : function(jqXHR, msg, exception) {

               msg = msg + (exception.message ? ', exception -> ' + exception.message : '');

               GIAPI.logger.log('node error: ' + msg, 'error');
               onResponse.apply(dabNode, [null, 'Error occurred: ' + msg]);
           }
       });
   };
   
    /**
     * Retrieves the nodes representing the {{#crossLink "DABSource"}}sources{{/crossLink}}
     * brokered by this DAB instance. DAB {{#crossLink "DABSource"}}sources{{/crossLink}} can also be retrieved as result of
     *  the first call of the {{#crossLink "GINode/expand:method"}}{{/crossLink}} method
     *
     * @method sources
     * @async
     *
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {Array} onResponse.result Array of {{#crossLink "DABSource"}}DAB sources{{/crossLink}}
     * In case of error, this argument contains an empty array
     * @param {String} [onResponse.error] In case of error, this argument contains a message
     * which describes the problem occurred
     * @param {String} [viewId]
     **/
    dabNode.sources = function(onResponse, viewId) {

        dabNode.expand(function(resultSet) {

            var sources = [];
            if (!resultSet.error) {

            	var page = resultSet.page;
                var nodes = page.nodes();
                sourcesCount = nodes.length;

                dabNode._includedSources = [];
                        
                for (var i = 0; i < nodes.length; i++) {

                    var curNode = nodes[i];
                    var report = curNode.report();
                    var source = GIAPI.DABSource(report, dabNode);
                    
                    sources.push(source);
                    dabNode._includedSources.push(report.id);
                }

                onResponse.apply(dabNode, [sources]);
            } else {
                onResponse.apply(dabNode, [sources, resultSet.error]);
            }
        }, 500, viewId); // to be sure to get all the sources!
    };

    /**
     * Retrieves {{#crossLink "Concept"}}concepts{{/crossLink}} related to the given <code>keyword</code>
     *
     * @method concept
     * @async
     *
     * @param {String} keyword The keyword by which generates the related {{#crossLink "Concept"}}concepts{{/crossLink}}
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {Array} onResponse.result Array of {{#crossLink "Concept"}}concepts{{/crossLink}}
     * In case of error, this argument contains an empty array
     * @param {String} [onResponse.error] In case of error, this argument contains a message
     * which describes the problem occurred
     * @param {Object} [options] Object literal of available options
     * @param {Integer} [options.start=1] The start index of the first returned {{#crossLink "Concept"}}concept{{/crossLink}}, starting from 1
     * @param {Integer} [options.count=10] The maximum number of  {{#crossLink "Concept"}}concepts{{/crossLink}} to return
     * @param {Boolean} [options.topLevel=false] If set to <code>true</code> only the <i>top level concepts</i> is retrieved
     *
     **/
    dabNode.concept = function(keyword, onResponse, options) {

        var query = GIAPI.concept(dabEndpoint, keyword, '', [], options, servicePath);

        jQuery.ajax({

            type : 'GET',
            url : query,
            crossDomain : true,
            dataType : 'jsonp',

            success : function(data, status, jqXHR) {

                if (data.resultSet && data.resultSet.error) {
                    GIAPI.logger.log('extend concept error: ' + data.resultSet.error, 'error');
                    onResponse.apply(dabNode, [[], 'Error occurred:' + data.resultSet.error]);

                    return;
                }

                var con = data.concept;
                var concepts = [];

                for (var i = 0; i < con.length; i++) {

                    var report = con[i];
                    var concept = GIAPI.Concept(dabEndpoint, report);
                    concepts.push(concept);
                }

                onResponse.apply(dabNode, [concepts]);
            },

            complete : function(jqXHR, status) {

                GIAPI.logger.log('concept complete status: ' + status);
            },

            error : function(jqXHR, msg, exception) {

                msg = msg + (exception.message ? ', exception -> ' + exception.message : '');

                GIAPI.logger.log('concept error: ' + msg, 'error');
                onResponse.apply(dabNode, [[], 'Error occurred: ' + msg]);
            }
        });
    };
    
    /**
     * Returns the {{#crossLink "PubSubManager"}}{{/crossLink}} instance.
     * 
     * @method pubSubManager
     * @return {PubSubManager} returns the {{#crossLink "PubSubManager"}}{{/crossLink}} instance
     */
    dabNode.pubSubManager = function(){
    	
		return pubSubManager;
    };
    
    /**
     * Retrieves the DAB endpoint
     *
     * @method endpoint
     * @return {String} The URL String of this DAB instance
     *
     **/
    dabNode.endpoint = function() {

        return dabEndpoint;
    };
    
    /**
     * Retrieves the service path
     *
     * @method servicePath
     * @return {String} the service path
     *
     **/
    dabNode.servicePath = function() {

        return servicePath;
    };
    
    /**
     * Retrieves the CSW path
     *
     * @method cswPath
     * @return {String} the service csw path
     *
     **/
    dabNode.cswPath = function() {

        return cswPath;
    };
    
     /**
     * Retrieves the OpenSearch path
     *
     * @method cswPath
     * @return {String} the service OpenSearch path
     *
     **/
    dabNode.openSearchPath = function() {

        return openSearchPath;
    };
    
    var createRefiners = function(termFrequency,onResponse,constraints,resultSet, options,onStatus,dabNode,paginator){
    	
    	// the constraints are passed to the refiner when the origin is a refiner or when a discover is performed
    	// (that is onResponse._origin is not set)
//    	var c = (!onResponse._origin || onResponse._origin === 'refiner') ?  constraints : {};

    	// the refiner is reset if onResponse has no origin, that is when a discover is performed
    	var resetRefiner = !onResponse._origin; 
        resultSet.refiner = GIAPI.Refiner(
        		dabNode,
        		constraints, 
        		options, 
        		onStatus,  
        		
        		paginator, 
        		resetRefiner);
        
        if(termFrequency){
        	// the constraints are passed to the termfrequency when the origin is a term frequency object 
        	// or when a discover is performed (that is onResponse._origin is not set)
        	// this way the constraints of the refiner and the tf are never mixed
//        	c = (!onResponse._origin || onResponse._origin === 'termFrequency')  ? constraints : {};                 	
        	
        	// the term frequency  if onResponse has no origin, that is when a discover is performed,
        	// or if the origin is a refiner
        	var resetTf = !onResponse._origin || onResponse._origin === 'refiner';
        	resultSet.termFrequency = GIAPI.TermFrequency(
        			dabNode, 
        			constraints,
        			options, 
        			onStatus,
        			
        			onResponse,         		
        			termFrequency,
        			resetTf);
        }
        
        // reset the _origin field
        onResponse._origin = null;
    }

    var createTimer = function(onStatus, extended, queryID) {

        var partialFunct = function(timerCallback, onStatus, queryID) {
            return function() {
                return timerCallback(onStatus, extended, queryID);
            };
        };

        var timer = jQuery.timer(partialFunct(timerCallback, onStatus, queryID));
        timer.set({
            time : 1000, // one second
            autostart : true
        });

        return timer;
    };

    var timerCallback = function(onStatus, extended, queryID) {

        var query = GIAPI.status(dabEndpoint, extended, queryID, servicePath);

        jQuery.ajax({

            type : 'GET',
            url : query,
            crossDomain : true,
            dataType : 'jsonp',

            success : function(data, status, jqXHR) {

                var statusArray = [];
                var dabStatus;
                for (var i = 0; i < data.discoverStatus.length; i++) {
                    var status = data.discoverStatus[i];

                    var s = GIAPI.SourceStatus(status);
                    if (s.title() === 'DAB') {
                        dabStatus = s;
                    }
                    statusArray.push(s);
                }

                onStatus.apply(this, [statusArray]);

                if (dabStatus && dabStatus.progress() === 100) {
                    timer.stop();
                }
            },

            complete : function(jqXHR, status) {
//                onStatus.apply(this, []);
            },

            error : function(jqXHR, msg, exception) {
                onStatus.apply(this, []);
                timer.stop();
            }
        });
    };

    var pageCount = function(matchCount, pageSize) {

        var ct = parseInt(matchCount);
        var ps = parseInt(pageSize);

        return parseInt(ct < ps ? '1' : Math.ceil(ct / ps));
    };

    var error = function(msg, exception, onResponse) {

        msg = msg + (exception.message ? ', exception -> ' + exception.message : '');

        GIAPI.logger.log('error occurred: ' + msg, 'error');
        if (timer) {
            timer.stop();
        }

        var paginator = GIAPI.Paginator();
        paginator._resultSet = GIAPI.emptyResultSet(msg);
        paginator._page = GIAPI.Page([]);

        onResponse.apply(dabNode, [[paginator]]);
    };

    var isOptions = function(object) {

        return (object && (object.start || object.pageSize || object.extension || object.termFrequency ||  object.searchFields ||  object.spatialRelation)) ? object : null;
    };

    var isConstraints = function(object) {

        return (object && (object.where || 
        		object.when || 
        		object.what || 
        		object.who || 
        		object.keyword || 
        		object.format || 
        		object.protocol || 
        		object.sources || 
        		object.source || 
        		object.from || 
        		object.until || 
        		object.kvp || 
        		object.platformId || 
        		object.instrumentId || 
        		object.origOrgId || 
        		object.attributeId ||
        		
        		object.instrumentTitle || 
        		object.organisationName || 
        		object.platformTitle || 
        		object.intendedObservationSpacing ||
        		object.timeInterpolation ||
        		object.aggregationDuration ||
        		object.attributeTitle 
        		
        )) ? object : null;
    };

    var isOnStatus = function(object) {

        return (object && ( typeof object === 'function')) ? object : null;
    };

    var getMatchKeyword = function(object) {

        var index = object.indexOf('----') + 4;
        return object.substring(index, object.length);
    };

    var notIn = function(map, target) {

        for (var key in map) {
            if (key.toLowerCase() === target.toLowerCase()) {
                return false;
            }
        }
        return true;
    };
    
    var isTermFrequencyQuery = function(constraints) {

        // this method at the moment works since these params are set
        // only by the term frequency object
        return constraints && (
                constraints.format || 
                constraints.keyword || 
                constraints.source || 
                constraints.protocol || 
                constraints.platformId || 
                constraints.instrumentId || 
                constraints.origOrgId || 
                constraints.attributeId || 
                constraints.score || 
                
                constraints.instrumentTitle || 
                constraints.platformTitle || 
                constraints.organisationName || 
                constraints.intendedObservationSpacing ||
                constraints.timeInterpolation ||
                constraints.aggregationDuration ||
                constraints.attributeTitle ||
                // include observedPropertyURI so TF selections on observed properties
                // are correctly recognized as term-frequency-driven queries
                constraints.observedPropertyURI
        );
        
        // this method is preferred but is disabled to allow the correct working of the new portal
        // return constraints.tfDiscover; 
    };
    
    dabNode.findSources = function(constraints){
    
        // all sources selected is equals to none selected, so better the second
        var sources = sourcesCount && sourcesCount === dabNode._includedSources.length ? null : dabNode._includedSources;
        
        // this is set by the term frequency object
        if(constraints && constraints.source){
            sources = constraints.source;
        }
        
        return sources;
    };

    return dabNode;
};
