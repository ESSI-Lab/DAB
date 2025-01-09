/**
 * @module Core
 **/

/**
 * A {{#crossLink "GINode"}}{{/crossLink}} is a <i>Geo Information node</i> representing an abstract
 * geospatial resource, a "dataset" or a "service", available as result of a query to the DAB.
 *
 * {{#crossLink "GINode"}}Nodes{{/crossLink}} properties such as {{#crossLink "Report/title:property"}}title{{/crossLink}} and {{#crossLink "Report/title:property"}}abstract{{/crossLink}}
 * are described by a {{#crossLink "GINode/report:method"}}report{{/crossLink}}.
 * A particular {{#crossLink "Report"}}{{/crossLink}} property attribute is {{#crossLink "Report/type:property"}}type{{/crossLink}}, which
 * defines which kind of interactions are available with the {{#crossLink "GINode"}}node{{/crossLink}}.
 * When a {{#crossLink "GINode"}}node{{/crossLink}} represents a "service" such as WMS, WCS, etc, the {{#crossLink "Report"}}report{{/crossLink}}
 * has an additional {{#crossLink "Report/service:property"}}service{{/crossLink}} property.
 * 
 * <h4>Node accessibility</h4>
 * 
 * Usually a node is <i>linked</i> to one or more data. If the linked data is accessible by means of a set of {{#crossLink "AccessOptions"}}options{{/crossLink}}, the node is 
 * <a name="accessible"><i>accessible</i></a> otherwise it is <a name="directlyAccessible"><i>directly accessible</i></a>; a node can be both <i>accessible</i> and <i>directly accessible</i>.<br>
 *
 * <ul><li><i>accessible</i> nodes provide one or more links that can be retrieved by means of the 
 * {{#crossLink "GINode/accessLink:method"}}{{/crossLink}} method. To test if this node is <i>accessible</i> use the {{#crossLink "GINode/isAccessible:method"}}{{/crossLink}} method</li>
 * <li><i>directly accessible</i> nodes provide one or more links that can retrieved
 * by means of the {{#crossLink "GINode/directAccessLinks:method"}}{{/crossLink}} method. To test if this node is <i>directly accessible</i> use the 
 * {{#crossLink "GINode/isDirectlyAccessible:method"}}{{/crossLink}} method</li></ul>
 *
 * This API provides two other kinds of nodes: {{#crossLink "DAB"}}{{/crossLink}} and {{#crossLink "DABSource"}}DAB sources{{/crossLink}}
 *
 * @class GINode
 **/
GIAPI.GINode = function() {

    var EXTRA_SMALL_SIZE = 48;
    var SMALL_SIZE = 96;
    var MEDIUM_SIZE = 144;
    var LARGE_SIZE = 288;
    var DEFAULT_DATA_SIZE = 96;

    var random = GIAPI.random();
    var resultSet;
    var expandCallback;
    var giNode = {};
    var report = arguments[0];
    var dabNode = arguments[1];
    var servicePath = arguments[2];
    var openSearchPath = arguments[3];

    if (!report) {
        report = {
            id : '' + random,
            type : 'simple',
            title : 'Simple node ' + random
        };
    } else {

        if (report.topic) {
            report.topic = report.topic.distinctValues();
        }

        if (report.geossCategory) {
            report.geossCategory = report.geossCategory.distinctValues();
        }

        if (report.keyword) {
            report.keyword = report.keyword.distinctValues();
        }
    }

    // used by the GITreeView
    giNode._parent = null;
    // useful for debugging
    giNode._id = report.id;
    giNode._title = report.title;
    
    /**
     * Identifier created with {{#crossLink "GIAPI/random:method"}}{{/crossLink}} method which contains 
     * only alphanumeric characters and can be used inside the attributes of <code>DOM</code> elements
     * 
     * @property {String} uiId
     */
    giNode.uiId = GIAPI.random();

    /**
     *
     * If this node is {{#crossLink "Report/type:property"}}composed{{/crossLink}}, the result of the expansion is a
     *  {{#crossLink "ResultSet"}}result set{{/crossLink}} split in {{#crossLink "Page"}}pages{{/crossLink}}
     *  (possibly one if the matched nodes are minor than the {{#crossLink "ResultSet"}}result set{{/crossLink}} {{#crossLink "Page/size:method"}}page size{{/crossLink}}).<br>
     * The {{#crossLink "ResultSet"}}result set{{/crossLink}} contains the underlying first level nodes (this node children). If this
     * node is a {{#crossLink "DAB"}}{{/crossLink}} instance, than the first call of this method
     * returns the correspondent {{#crossLink "DABSource"}}sources{{/crossLink}}.<br>
     * If the {{#crossLink "ResultSet"}}result set{{/crossLink}} is split in more than one {{#crossLink "Page"}}page{{/crossLink}}
     *  use the {{#crossLink "GINode/expandNext:method"}}expandNext method{{/crossLink}} to retrieve the next ones.
     *
     * If this is a  {{#crossLink "Report/type:property"}}simple node{{/crossLink}}, the {{#crossLink "ResultSet"}}result set{{/crossLink}} is empty
     *
     * @method expand
     * @async
     *
     * @param {Function} onResponse <a name="onResponse">onResponse</a> Callback function for receiving asynchronous query response
     *
     * @param {ResultSet} onResponse.resultSet The expansion {{#crossLink "ResultSet"}}result set{{/crossLink}} set. In case of error,
     * this argument contains an empty {{#crossLink "ResultSet"}}result set{{/crossLink}}
     * with the {{#crossLink "ResultSet/error:property"}}error{{/crossLink}} property describing the problem occurred
     *
     * @param {Integer} [pageSize=10] <a name="pageSize">The size</a> of the {{#crossLink "ResultSet"}}result set{{/crossLink}} {{#crossLink "Page"}}pages{{/crossLink}}
     **/
    giNode.expand = function(onResponse, pageSize, viewId) {

        if (report.type === 'simple') {

            onResponse.apply(giNode, [GIAPI.Page([])], GIAPI.emptyResultSet());
        } else {

            var start = 1;
            if (arguments.length === 4) {
                // invoked from expandNext
                start = arguments[3];
            } else {
                // resetting resulSet
                resultSet = null;
            }

            var options = {};
            options.start = start;
            options.pageSize = pageSize;

            var query = GIAPI.query(dabEndpoint(), null, options, report.id, null, null, 'expand_'+GIAPI.random(), viewId, servicePath, openSearchPath);
            // callback reused in expandNext
            expandCallback = onResponse;

            jQuery.ajax({

                type : 'GET',
                url : query,
                crossDomain : true,
                dataType : 'jsonp',

                success : function(data, status, jqXHR) {

                    resultSet = data.resultSet;

                    if (resultSet.error) {

                        GIAPI.logger.log('expand error: ' + resultSet.error, 'error');
                        onResponse.apply(dabNode, [GIAPI.Page(), resultSet]);

                        return;
                    }

                    var nodes = [];
                    for (var i = 0; i < data.reports.length; i++) {
                        var report = data.reports[i];
                        var n;
                        if (report.service && report.service.source === true) {
                            n = GIAPI.DABSource(report, giNode);
                        } else {
                            n = GIAPI.GINode(report, dabNode, servicePath, openSearchPath);
                        }

                        n._parent = giNode;
                        nodes.push(n);
                    }
                    
                    nodes.sort(function(s1,s2){
                        
                        var t1 = s1._title;
                        var t2 = s2._title;
                        
                        return t1.localeCompare(t2);                    
                    });
                    
                    resultSet.page = GIAPI.Page(nodes);
                    
                    // the page is a result set property
                    onResponse.apply(giNode, [resultSet]);
                },

                complete : function(jqXHR, status) {

                    GIAPI.logger.log('expand complete status: ' + status);
                },

                error : function(jqXHR, error, exception) {

                    error = error + (exception.message ? ', exception -> ' + exception.message : '');

                    GIAPI.logger.log('expand error: ' + error, 'error');
                    onResponse.apply(giNode, [GIAPI.emptyResultSet('Error occurred: ' + error)]);
                }
            });
        }
    };

    /**
     * If this node has already been {{#crossLink "GINode/expand:method"}}expanded{{/crossLink}}, this method
     * tests if other {{#crossLink "Page"}}pages{{/crossLink}} in the {{#crossLink "ResultSet"}}result set{{/crossLink}}
     * are available. If the test is positive and the <code>execute</code> argument is <code>true</code>, retrieves the next
     * {{#crossLink "ResultSet"}}result set{{/crossLink}} {{#crossLink "Page"}}page{{/crossLink}}.
     * If this node is a  {{#crossLink "Report/type:property"}}composed node{{/crossLink}} but it has never been expanded, an <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown.<br>
     * If this is a  {{#crossLink "Report/type:property"}}simple node{{/crossLink}}, this method returns <code>false</code>
     *
     * @method expandNext
     * @async
     * @param {Function} onResponse onResponse Callback function for receiving asynchronous query response. See <a href="../classes/GINode.html#onResponse">onResponse</a> for more info
     * @param {Boolean} [execute] If omitted or set to <code>false</code>, this method tests if other pages in the
     * result set are available and returns the test result. If set to <code>true</code> and the test is positive,
     * this method retrieves the next result set page and returns <code>true</code>.
     * If set to <code>true</code> and the test is negative this method returns <code>false</code>
     *
     * @return {Boolean}
     * <code>true</code> if other pages in the result set are available, <code>false</code> otherwise
     **/
    giNode.expandNext = function(onResponse, execute) {

        if (report.type === 'simple') {
            return false;
        }

        if (!resultSet) {
            throw "node not expanded";
        }

        var size = resultSet.size;
        var start = resultSet.start;
        var pageSize = resultSet.pageSize;

        if (start + pageSize > size) {
            return false;
        }

        if (execute) {
            var callback = onResponse ? onResponse : expandCallback;
            giNode.expand(expandCallback, null, pageSize, start + pageSize);
        }

        return true;
    };

    // @formatter:off
    /**
     * If this {{#crossLink "GINode"}}node{{/crossLink}} is <a href="GINode.html#accessible" class="crosslink">accessible</a>, retrieves one or more
     * objects containing a list of {{#crossLink "AccessOptions"}}validOptions{{/crossLink}}, the {{#crossLink "AccessOptions"}}defaultOption{{/crossLink}} and the 
     * {{#crossLink "AccessOptions"}}reducedOption{{/crossLink}} to use
     * as <a href="#accessOptions" class="crosslink">options</a> for the {{#crossLink "GINode/accessLink:method"}}{{/crossLink}} method.<br>
     * If this {{#crossLink "GINode"}}node{{/crossLink}} is not <a href="GINode.html#accessible" class="crosslink">accessible</a>, this method does nothing.<br><br>
     *  Each returned object contains a <code>validOptions</code> array, and a <code>defaultOption</code> and a <code>reducedOption</code>. E.g.:
     * 
     * <pre><code>var options = {
     "validOptions": [
     	  {
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
     	  },
     	  {
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
     	  }],
     "defaultOption": 
         {
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
     	  },
     "reducedOption": 
         {
	         "CRS": "EPSG:4326",
	     	 "format": "IMAGE_PNG",
	     	 "firstAxisSize": {
	        	"label": "Lat",
	        	"value": 395
	     	 },
		     "secondAxisSize": {
		        "label": "Lon",
		        "value": 600
		     },
		     "spatialSubset": {
		         "south": -60.0,
		         "west": -180.0,
		         "north": 90.000007823,
		         "east": 180.000018775
		     },
		     "temporalSubset": {
		         "from" : "2013-01-01",
		         "to": "2013-01-01"        
		     }
     	  }
     }
     * </code></pre>
     *
     * @method accessOptions
     * @async
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {Object} onResponse.options the access options; in case of error this argument is set to <code>null</code>
     * @param {String} [onResponse.error] In case of error this argument contains a message which describes the problem occurred
     *
     *
     **/
    giNode.accessOptions = function(onResponse) {
    	
    	 if(!giNode.isAccessible()){
    		 return;
    	 }
    	
    	 var endpoint = dabEndpoint();
    	 if(!endpoint.endsWith('/')){
    		 endpoint += '/';
    	 }
    	     	
    	 var optionsLink = endpoint+servicePath+'/api-rest/datasets/'+giNode.report().id+'/content/options';

		 jQuery.ajax({

	        type : 'GET',
	        url : optionsLink,
	        dataType : 'json',

	        success : function(data, status, jqXHR) {
	        	
	           var createTemporalSubset = function(options){
	        	   
	        	   if(options.temporalBegin || options.temporalEnd){
	        		   options.temporalSubset = {};
	        		   if(options.temporalBegin){
	        			   options.temporalSubset.from = options.temporalBegin;
	        			   delete options.temporalBegin;
	        		   } 
	        		   if(options.temporalEnd){
	        			   options.temporalSubset.to = options.temporalEnd;
	        			   delete options.temporalEnd;
	        		   }
	        	   }
	           };	
	           
	           var createSpatialSubset = function(options){
	        	   
	        	   if(options.subset){
	        		   options.spatialSubset = {};
	        		   options.spatialSubset.south = parseFloat(parseFloat(options.subset.split(',')[0]).toFixed(3));
	        		   options.spatialSubset.west = parseFloat(parseFloat(options.subset.split(',')[1]).toFixed(3));
	        		   options.spatialSubset.north = parseFloat(parseFloat(options.subset.split(',')[2]).toFixed(3));
	        		   options.spatialSubset.east = parseFloat(parseFloat(options.subset.split(',')[3]).toFixed(3));
	        		   
	        		   if(options.subsetCRS){
	        			   options.spatialSubset.CRS = options.subsetCRS;
	        			   delete options.subsetCRS;
	        		   }
	        		   
	        		   delete options.subset;
	        	   }
	           };	
	        	
	           if(data.defaultOption){
	        	   createTemporalSubset(data.defaultOption);	  
	        	   createSpatialSubset(data.defaultOption);	        	  
	           } 
	           
	           if(data.reducedOption){
	        	   createTemporalSubset(data.reducedOption);	
	        	   createSpatialSubset(data.reducedOption);	  
	           }
	           
	           for(var index in data.availableOptions){	        	   
	        	   createTemporalSubset(data.availableOptions[index]);	
	        	   createSpatialSubset(data.availableOptions[index]);	
	           }
	        	
	           onResponse.apply(giNode, [data]);
	        },

	        complete : function(jqXHR, status) {       	
	        },

	        error : function(jqXHR, msg, exception) {
	        	
	        	var error = msg + (exception.message ? ', exception -> ' + exception.message : '');

                GIAPI.logger.log('accessoptions error: ' + error, 'error');
                
                onResponse.apply(giNode, [null,error]);        	
	        }
	    });
    };

    /**
     * If this {{#crossLink "GINode"}}node{{/crossLink}} is <a href="GINode.html#accessible" class="crosslink">accessible</a>, retrieves the
     * link to access the data linked to this {{#crossLink "GINode"}}node{{/crossLink}}.<br>
     * If this {{#crossLink "GINode"}}node{{/crossLink}} is not <a href="GINode.html#accessible" class="crosslink">accessible</a>, this method does nothing.<br> 
     * The data is accessed using a set of {{#crossLink "AccessOptions"}}options{{/crossLink}} which allow to specify
     * several output parameters such as {{#crossLink "AccessOptions/CRS:property"}}Coordinate Reference System{{/crossLink}}. Not all the possible combinations of 
     * {{#crossLink "AccessOptions"}}access options{{/crossLink}} parameters are valid for a given data; the valid set of parameters to access the data of this
     * node can be retrieved with the {{#crossLink "GINode/accessOptions:method"}}<code>accessOptions</code> method{{/crossLink}}.<br>
     * If the <code>options</code></a> parameter is omitted,
     *  the data link is created using the available {{#crossLink "AccessOptions"}}reducedOption{{/crossLink}} that can
     *  be retrieved with the {{#crossLink "GINode/accessOptions:method"}}<code>accessOptions</code> method{{/crossLink}}
     *
     * @method accessLink
     * @async
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {String} onResponse.link The access link; in case of error this argument is set to <code>null</code> 
     * @param {String} [onResponse.error] In case of error this argument contains a message which describes the problem occurred
     *
     * @param {AccessOptions}[options] The access {{#crossLink "AccessOptions"}}options{{/crossLink}} which allows to specify the data output parameters. If omitted,
     *  the data link is created using the available {{#crossLink "AccessOptions"}}reducedOption{{/crossLink}}
     * (see {{#crossLink "GINode/accessOptions:method"}}<code>accessOptions</code> method{{/crossLink}})
     */
	giNode.accessLink = function(onResponse, option) {
		
		 if(!giNode.isAccessible()){
    		 return;
    	 }
    	
		 var createLink = function(option){
			 
			 var endpoint = dabEndpoint();
	    	 if(!endpoint.endsWith('/')){
	    		 endpoint += '/';
	    	 }
	    		
			 var contentLink = endpoint+servicePath+'/api-rest/datasets/'+giNode.report().id+'/content?';
	
			 contentLink += 'format='+option.format+'&';
		   	 
		   	 if(option.spatialSubset){
		   		 contentLink += 'subset='+option.spatialSubset.south+','+option.spatialSubset.west+','+option.spatialSubset.north+','+option.spatialSubset.east+'&';
		   	 }	   	 
		   	 contentLink += 'subsetCRS=EPSG:4326&';
		   	 
		   	 if(option.temporalSubset && option.temporalSubset.from){
		   		 contentLink += 'temporalBegin='+option.temporalSubset.from+'&';
		   	 }
		   	 if(option.temporalSubset && option.temporalSubset.to){
		   		 contentLink += 'temporalEnd='+option.temporalSubset.to+'&';
		   	 }
		   	 
		   	 var size = null;
		   	 if(option.firstAxisSize  && option.secondAxisSize){
		   		 size = option.firstAxisSize.value+','+option.secondAxisSize.value;
			   	 contentLink += 'size='+size+'&';
		   	 }
		   	 
		   	 contentLink += 'CRS='+option.CRS;
		   	 
		   	 return contentLink;
		 };
		 
		 if(option){
			 onResponse.apply(giNode, [createLink(option)]); 
		 }else{
			giNode.accessOptions(function(options,error){
				
				if(!error){					
					 onResponse.apply(giNode, [createLink(options.reducedOption)]);
				}else{
					 onResponse.apply(giNode, [null,error]);					
				}				
			});
		 }
	}, 

    /**
     * If this {{#crossLink "GINode"}}node{{/crossLink}} has an
     *  {{#crossLink "Report/overview:property"}}overview{{/crossLink}},
     * creates an &lt;img&gt; having as source the {{#crossLink "Report/overview:property"}}overview{{/crossLink}}
     *  link at the given <code>options.index</code> (0 if not specified) and
     *  appends &lt;img&gt; to the element having the given <code>id</code>.<br>
     * The {{#crossLink "Report/overview:property"}}overview{{/crossLink}} property can be available also if this {{#crossLink "GINode"}}node{{/crossLink}}
     * is not <a href="GINode.html#accessible" class="crosslink">accessible</a>.<br><br>
     * The method throws an <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> if <code>id</code> does not correspond to an element
     *  or if <code>options.index</code> does not respect the following statement: <code>index</code> >= 0 && <code>index</code> <= {{#crossLink "Report/overview:property"}}overview{{/crossLink}}.<code>length - 1</code>
     *
     * @method overview
     * @async
     * @param {String} id the id of an element in which to append the &lt;img&gt; element of the selected overview
     * @param {Function} onResponse Callback function for receiving asynchronous request status
     * @param {String} onResponse.status possible status are 'success' and 'error'
     * @param {String} [onResponse.message] in case of 'error' status this argument contains the error message
     * @param {String} [onResponse.image] the loaded <code>image</code> element or <code>null</code> in case of error
     * @param {Object} [options]
     * @param {Number} [options.index=0] index of this node {{#crossLink "Report/overview:property"}}overview{{/crossLink}} array
     * @param {String} [options.size="medium"] The overview size (aspect ration is preserved). Possible values are:<ul>
     * <li>"extra small": 48x48 px</li>
     * <li>"small": 96x96 px</li>
     * <li>"medium": 144x144 px</li>
     * <li>"large": 288x288 px</li>
     * <li>"original": the original size of the target overview image</li>
     * </ul>
     * @param {Boolean} [options.force=false] If set to <code>true</code> a "no overview" image is used in case of error or in case
     * an overview is not available
     * @param {String} [options.backgroundURL] URL of an additional image to load as background of the overview image 
     * (use this only if the overview image is transparent)
     * @return {Boolean}
     * <code>true</code> if this {{#crossLink "GINode"}}node{{/crossLink}} has an
     *  {{#crossLink "Report/overview:property"}}overview{{/crossLink}}, <code>false</code> otherwise
     *
     */
    giNode.overview = function(id, onResponse, options) {

        var overview = report.overview;
        
        if(!options){
        	options = {};
        }
        
        if(!options.size){
        	options.size = 'medium'; 
        }
        
        if(!options.index){
        	options.index = 0; 
        }

        if (overview && (options.index < 0 || options.index > overview.length - 1)) {
            throw Error("index out of bounds, must be >= 0 and <= " + (overview.length - 1));
        }

        if (!jQuery('#' + id).length === 0) {
            throw Error("element not found");
        }
        
        var size = null;
        switch(options.size) {
            case 'extra small':
                size = EXTRA_SMALL_SIZE;
                break;
            case 'small':
                size = SMALL_SIZE;
                break;
            case 'large':
                size = LARGE_SIZE;
                break;
            case 'original':
                size = null;
                break;
            case 'medium':
            default:
                size = MEDIUM_SIZE;
                break;
        }
       var overviewLink
        if(overview){
        	overviewLink = overview[options.index];
        }else if(options.force){
        	overviewLink = 'https://api.geodab.eu/docs/assets/img/no_overview_576.png';
        }else{
        	return false;
        }
             
        // creates the overview image
        var ovrImg = new Image();
        
        // creates the background image
        var backgroundImg = new Image();
        
        if(!options.backgroundURL){
        	
        	var onLoad = function(event){     
        		         		
        		onLoadImage(ovrImg,id,size,onResponse,giNode);
        		
        		if(this.error){
        			// the error image has been loaded
                    onResponse.apply(giNode, ['error', 'Image loading aborted'],ovrImg);
                    return;
        		}
                onResponse.apply(giNode, ['success','Image loaded',ovrImg]);   	
        	};
        	
        	ovrImg.onload = onLoad;
        }else{
        	
            // set the link as property to be asynchronously loaded after the background image
            ovrImg.overviewLink = overviewLink;
        	
        	var onLoad = function(event) {

        		onLoadImage(backgroundImg,id,size,onResponse,giNode);
        		
        		// now loads the overview image
        		ovrImg.src = ovrImg.overviewLink;
        		ovrImg.onload = function(event){
        			
            		onLoadImage(ovrImg,id,size,onResponse,giNode);
                  
                	jQuery(ovrImg).css('position', 'relative');
                    jQuery(ovrImg).css('top', '-15px');
                    jQuery(ovrImg).css('margin-top', '-'+ovrImg.height+'px');
                    
                    onResponse.apply(giNode, ['success','Image loaded',ovrImg]);   	
            	}       		
            };
        	
        	backgroundImg.onload = onLoad;
        }
        
        var onError = function(event) {       	
        	if(options.force){
	        	// loads the no image URL
	            try {
	                ovrImg.src = 'https://api.geodab.eu/docs/assets/img/no_overview_576.png';
	            } catch(e) {
	                GIAPI.logger.log('unable to load no preview image: ' + e.message, 'error');
	                onResponse.apply(giNode, ['error', 'Unable to load no preview image: ' + e.message],null);
	                
	                // this is required in order to avoid loops
	                ovrImg.onerror = null;
	                return;
	            }
            }
        	
        	// set an error attribute before the no image URL is loaded
            ovrImg.error = true;
        	
            GIAPI.logger.log('image loading error');
            onResponse.apply(giNode, ['error', 'Unable to load image, error occurred'],ovrImg);
        };       
        ovrImg.onerror = onError;
        
        var onAbort = function(event) {

            GIAPI.logger.log('image loading aborted');
            onResponse.apply(giNode, ['error', 'Image loading aborted'],null);
        };
        ovrImg.onabort = onAbort;

        // loads image URL
        try {
            
        	if(!options.backgroundURL){
            	ovrImg.src = overviewLink;
        	}else{
        		// the overview image is loaded after the background image
            	backgroundImg.src = options.backgroundURL;
            }
        } catch(e) {
            GIAPI.logger.log('unable to load image: ' + e.message, 'error');
            onResponse.apply(giNode, ['error', 'Unable to load image: ' + e.message],null);
        }

        return overview;
    };
    
    var onLoadImage = function(img,id,size,onResponse,giNode){   

    	if(size){ // set to null for original size
    		if(img.width > img.height){
    			 
    		     img.height = (img.height / img.width) * size;
    		     img.width = size;
    		}else if(img.height > img.width){
    			 
    			 img.width = (img.width / img.height) * size;
    			 img.height = size;
    		}else {

    			 img.width = size;
    			 img.height = size;
            }
    	}
		 
	    jQuery('#' + id).attr('width',  img.width + 'px');
        jQuery('#' + id).attr('height', img.height + 'px');
        jQuery('#' + id).css('width',  img.width + 'px');
        jQuery('#' + id).css('height', img.height + 'px');
                           	
        jQuery('#' + id).append(img); 
    };

    /**
     * Retrieves an array of <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a>;
     * if the current {{#crossLink "GINode"}}node{{/crossLink}} does not provide any <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">layer</a>
     * an empty array is returned.<br>
     * As explained in the <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a>
     * constructor documentation, the returned <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">layers</a> can be optionally created
     * with additional parameters and options for the <i>GetMap</i> request (see <code>initOptions</code> argument). E.g.:
     *
     * <pre><code>var initOptions = {};
     * initOptions.params = {transparent: true}; // corresponds to the OpenLayers.Layer.WMS <code>params</code> argument
     * initOptions.options = {opacity: 0.5}; // corresponds to the OpenLayers.Layer.WMS <code>options</code> argument
     *
     * var layers = giNode.olWMS_Layer(initOptions);
     * </code></pre>
     *
     * @param {Object} [initOptions] object literal of options passed the <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a>
     * constructor
     * @param {Object} [initOptions.params] this argument corresponds to the <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a> <code>params</code> argument
     *  <b>Note: the <code>layers</code> property is provided by the API</b>
     * @param {Object} [initOptions.options] this argument corresponds to the <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a> <code>options</code> argument
     *
     * @method olWMS_Layer
     * @return {<a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS">OpenLayers.Layer.WMS</a>[[]]} Array of
     * <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a> possible empty if this
     * {{#crossLink "GINode"}}node{{/crossLink}} does not provide any
     */
    giNode.olWMS_Layer = function(initOptions) {
    	
    	return GIAPI.LayersFactory.ol_Layer(report.online, 'urn:ogc:serviceType:WebMapService:', initOptions);
    };
    
    /**
     * Retrieves an array of <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">google.maps.ImageMapType</a>;
     * if the current {{#crossLink "GINode"}}node{{/crossLink}} does not provide any <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">layer</a>
     * an empty array is returned.<br>
     * All the available <a href="https://developers.google.com/maps/documentation/javascript/reference#ImageMapTypeOptions">ImageMapTypeOptions</a> are allowed; this method
     * implements the <code>getTileUrl</code> function in order to build the correct <i>WMS GetMap</i> request.
     * 
     * @param {<a href="https://developers.google.com/maps/documentation/javascript/reference#Map">google.maps.Map</a>} map instance
     *  required for the implementation of the <code>getTileUrl</code> function 
     * @param [initOptions] See <a href="https://developers.google.com/maps/documentation/javascript/reference#ImageMapTypeOptions">ImageMapTypeOptions</a> for more info
     * @method googleImageMapType
     * @return {<a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">google.maps.ImageMapType</a>[[]]} array (possible empty)
     * of <a href="../classes/GINode.html#method_googleImageMapType" class="crosslink">google.maps.ImageMapType</a>
     */
    giNode.googleImageMapType = function(map,initOptions) {
		
		return GIAPI.LayersFactory.googleImageMapType(map, report.online, initOptions);	
    };
    
    /**
     * Retrieves an array of <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a> (TiledMapService type);
     * if the current {{#crossLink "GINode"}}node{{/crossLink}} does not provide any <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">layer</a>
     * an empty array is returned.<br>
     * As explained in the <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a>
     * constructor documentation, the returned <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">layers</a> can be optionally created
     * with additional parameters and options for the <i>GetMap</i> request (see <code>initOptions</code> argument). E.g.:
     *
     * <pre><code>var initOptions = {};
     * initOptions.params = {transparent: true}; // corresponds to the OpenLayers.Layer.WMS <code>params</code> argument
     * initOptions.options = {opacity: 0.5}; // corresponds to the OpenLayers.Layer.WMS <code>options</code> argument
     *
     * var layers = giNode.olTiles_Layer(initOptions);
     * </code></pre>
     *
     * @param {Object} [initOptions] object literal of options passed the <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a>
     * constructor
     * @param {Object} [initOptions.params] this argument corresponds to the <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a> <code>params</code> argument
     *  <b>Note: the <code>layers</code> property is provided by the API</b>
     * @param {Object} [initOptions.options] this argument corresponds to the <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a> <code>options</code> argument
     *
     * @method olTiles_Layer
     * @return {<a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS">OpenLayers.Layer.WMS</a>[[]]} Array of
     * <a href="http://dev.openlayers.org/docs/files/OpenLayers/Layer/WMS-js.html#OpenLayers.Layer.WMS" target="_blank">OpenLayers.Layer.WMS</a> possible empty if this
     * {{#crossLink "GINode"}}node{{/crossLink}} does not provide any
     */
    giNode.olTiles_Layer = function(initOptions) {
    	
    	return GIAPI.LayersFactory.ol_Layer(report.online, 'urn:essi:serviceType:TiledMapService:', initOptions);
    };
    
    /**
	 * This method checks if the {{#crossLink "GINode"}}node{{/crossLink}} provides one or more layers
	 * of type <a href="http://dev.openlayers.org/apidocs/files/OpenLayers/Layer/Vector-js.html#OpenLayers.Layer.Vector.OpenLayers.Layer.Vector" target="_blank">OpenLayers.Layer.Vector</a>.
	 * The method returns an array of <code>String</code>s, each string is the name of an available vector <a href="http://dev.openlayers.org/apidocs/files/OpenLayers/Layer/Vector-js.html#OpenLayers.Layer.Vector.OpenLayers.Layer.Vector" target="_blank">layer</a>.
	 *
	 * @method has_olVector_Layer
	 * 
	 * @return {String[]} the Array of names of available <a href="http://dev.openlayers.org/apidocs/files/OpenLayers/Layer/Vector-js.html#OpenLayers.Layer.Vector.OpenLayers.Layer.Vector" target="_blank">OpenLayers.Layer.Vector</a> layers.
	 */    
	giNode.has_olVector_Layer = function() {

		var online = report.online;
		var out = [];
		var found = false;

		if (online) {

			for (var i = 0; i < online.length; i++) {
				var on = online[i];

				var name = on.name;
				var url = on.url;

				var protocol = on.protocol;


				if (protocol && protocol.indexOf('urn:essi:serviceType:Trajectory') != -1) {
					if (name) {
						out.push(name);
					} else {
						
						out.push('Unknown Vector Name');
					}
				}
			}
		}
		
		return out;
	}; 

	/**
	 * Retrieves an array of <a href="http://dev.openlayers.org/apidocs/files/OpenLayers/Layer/Vector-js.html#OpenLayers.Layer.Vector.OpenLayers.Layer.Vector" target="_blank">OpenLayers.Layer.Vector</a>;
	 * if the current {{#crossLink "GINode"}}node{{/crossLink}} does not provide any <a href="http://dev.openlayers.org/apidocs/files/OpenLayers/Layer/Vector-js.html#OpenLayers.Layer.Vector.OpenLayers.Layer.Vector" target="_blank">layer</a>
	 * an empty array is returned.<br>
	 * Besides, client applications are required (at the moment) to handle the cross-origin nature of the request which is executed (GML is an XML encoding)
	 *
	 * @method olVector_Layer
	 * @async
	 * @param {Function} onResponse Callback function for receiving asynchronous result
	 * @param {Object/Array} onResponse.result the Array of available 
	 * <a href="http://dev.openlayers.org/apidocs/files/OpenLayers/Layer/Vector-js.html#OpenLayers.Layer.Vector.OpenLayers.Layer.Vector" target="_blank">OpenLayers.Layer.Vector</a> objects
	 */    
	giNode.olVector_Layer = function(onResponse) {

		var online = report.online;
		var out = [];
		var found = false;

		if (online) {

			for (var i = 0; i < online.length; i++) {
				var on = online[i];

				var name = on.name;
				var url = on.url;

				var protocol = on.protocol;

				if (protocol && protocol.indexOf('urn:essi:serviceType:Trajectory') != -1) {

					found = true;

					var cBack = function(response) {

						response.defaultOptions.rasterFormat = "GML 3.1.1";

						var onDataLinkResponse = function(result, error) {

							if (error) {
								alert(error + ": " + JSON.stringify(result));
								return;
							}

							var vector = new OpenLayers.Layer.Vector(name, {

								protocol : new OpenLayers.Protocol.Script({
									url : result,
									format : new OpenLayers.Format.GML(),
									parseFeatures : function(data) {
										return this.format.read(data.results);
									}
								}),
								strategies : [new OpenLayers.Strategy.Fixed()]
							});

							out.push(vector);
							onResponse.apply(giNode, [out]);

						};

						giNode.accessLink(onDataLinkResponse, response.defaultOptions);
					};

					giNode.accessOptions(cBack);

				}
			};
		}

		if (found === false) {
			onResponse.apply(giNode, [[]]);
		}
	};

	/**
     * Returns an array containing all the available <a href="GINode.html#simplyAccessible" class="crosslink"><i>direct access links</i></a>. 
     * 
     * See also {{#crossLink "OnlineInfo/accessType:property"}}OnlineInfo.accessType{{/crossLink}} property.<br>
     * See also {{#crossLink "GINode/isDirectlyAccessible:method"}}{{/crossLink}} method
     *
     * @return {String[]} An array containing all the available <a href="GINode.html#simplyAccessible" class="crosslink"><i>direct access links</i></a> or an empty array if none are available
     *
     * @method directAccessLinks
     * 
     */
    giNode.directAccessLinks = function() {

    	var out = [];

        if (report.online) {
            for (var i = 0; i < report.online.length; i++) {
                var onlineInfo = report.online[i];
                if (onlineInfo.accessType && 
                		(onlineInfo.accessType === 'direct' || onlineInfo.accessType === 'simple')&& 
                		onlineInfo.url) {
                	
                    out.push(onlineInfo.url);
                }
            }         
        }

        return out;
    };
    
    /**
     * Tests if this node is <a href="GINode.html#directlyAccessible" class="crosslink">directly accessible</a>.
     * 
     * See also {{#crossLink "GINode/directAccessLinks:method"}}{{/crossLink}} method.<br>
     * See also {{#crossLink "OnlineInfo/accessType:property"}}OnlineInfo.accessType{{/crossLink}} property
     * 
     * @return <code>true</code> if this node is linked to one or more data and it has one or more <a href="GINode.html#directlyAccessible" class="crosslink"><i>direct access links</i></a>, 
     * <code>false</code> otherwise.<br>
     *
     * @method isDirectlyAccessible
     */
    giNode.isDirectlyAccessible = function() {

    	return giNode.directAccessLinks().length > 0;
    };
    
    /**
     * Tests if this node is <a href="GINode.html#accessible" class="crosslink">accessible</a>.<br>
     * See also {{#crossLink "GINode/accessOptions:method"}}{{/crossLink}} and {{#crossLink "GINode/accessLink:method"}}{{/crossLink}} methods
     *
     * @return <code>true</code> if this node is linked to one or more data and it is asynchronously accessible,
     *  <code>false</code> otherwise
     *
     * @method isAccessible
     */
    giNode.isAccessible = function() {

        return axeOnline().length > 0; // || giNode.simpleAccessLinks().length > 0;
    };

    /**
     * Retrieves and optionally set a new {{#crossLink "Report"}}report{{/crossLink}}
     *
     * @method report
     *
     * @param {report} [newReport] The new {{#crossLink "Report"}}report{{/crossLink}}
     * @return {report} The node {{#crossLink "Report"}}report{{/crossLink}}
     **/
    giNode.report = function(newReport) {

        if (newReport) {
            report = newReport;
        };

        return report;
    };
    
    /**
     * Retrieves an array of <a href="http://openlayers.org/en/v3.15.1/apidoc/ol.source.TileWMS.html" target="_blank">ol.source.TileWMS </a>;
     * if the current {{#crossLink "GINode"}}node{{/crossLink}} does not provide any <a href="http://openlayers.org/en/v3.15.1/apidoc/ol.source.TileWMS.html" target="_blank">layer</a>
     * an empty array is returned.<br>
     * As explained in the <a href="http://openlayers.org/en/v3.15.1/apidoc/ol.source.TileWMS.html" target="_blank">ol.source.TileWMS </a>
     * constructor documentation, the returned <a href="http://openlayers.org/en/v3.15.1/apidoc/ol.source.TileWMS.html" target="_blank">layers</a> can be optionally created
     * with additional parameters and options for the <i>GetMap</i> request (see <code>opt_options</code> argument). E.g.:
     *
     * <pre><code>var opt_options = {};
     * opt_options.params = {LAYERS: name, TRANSPARENT: true}; //WMS request parameters. At least a LAYERS param is required.
     * opt_options.attributions = new ol.Attribution({
                                        html: 'All maps © ' + '<a href="http://www.example.org/">ExampleMap</a>'
                                  }); 
     *
     * var layers = giNode.olMWS_Layer(opt_options);
     * </code></pre>
     *
     * @param {Object} [opt_options] See Tile WMS options in the <a href="http://openlayers.org/en/v3.15.1/apidoc/ol.source.TileWMS.html" target="_blank">ol.source.TileWMS </a>
     * constructor
     *
     * @method olMWS_Layer
     * @return {<a href="http://openlayers.org/en/v3.15.1/apidoc/ol.source.TileWMS.html" target="_blank">ol.source.TileWMS </a>[[]]} Array of
     * <a href="http://openlayers.org/en/v3.15.1/apidoc/ol.source.TileWMS.html" target="_blank">ol.source.TileWMS </a> possible empty if this
     * {{#crossLink "GINode"}}node{{/crossLink}} does not provide any
     */
    giNode.olMWS_Layer = function(opt_options) {
        
        return GIAPI.LayersFactory.layers(report.online, 'urn:ogc:serviceType:WebMapService:', opt_options);
    };
    
    
    /**
     TODO: add documentation
     */
    giNode.WFS_Service = function(opt_options) {
        
        return wfs_Service('urn:ogc:serviceType:WebFeatureService:', opt_options);
    };
         
    var wfs_Service = function(protocol, opt_options) {
      
        var online = report.online;
        var out = [];

        if (online) {

            for (var i = 0; i < online.length; i++) {
                var on = online[i];
                var prot = on.protocol;
                
                if (prot && prot.indexOf(protocol) != -1) {
                    //var name = on.name;
                    //var url = on.url;
                    out.push(on);
                    //var layerWFS0 = new ol.layer.Vector({title: '-989751316',source: new ol.source.Vector({loader: function (extent) {$.ajax('http://roncella.essi-lab.eu:8084/gi-axe/services/wfs', {type: 'GET',data: {service: 'WFS',version: '1.0.0',request: 'GetFeature',typename: '-989751316',srsname: 'EPSG:4326',outputFormat: 'application/vnd.geo+json',bbox: extent.join(',') + ',EPSG:4326'}}).done(function (response) {layerWFS0.getSource().addFeatures(new ol.format.GeoJSON().readFeatures(response));});},strategy: ol.loadingstrategy.bbox,projection: 'EPSG:4326'})});
                    // var query = url + "service=WFS&version=1.0.0&request=GetFeature&typename=" + name + "&srsname=EPSG:4326&outputFormat=application%2Fvnd.geo%2Bjson";                 
                   // jQuery.ajax({
 
                     // type : 'GET',
                     // url : query,
                     // crossDomain : true,
                     // dataType : 'jsonp',
 
                     // success : function(data, status, jqXHR) {
 
                    // var featurecollection = data;
                    // var geojson_format = new OpenLayers.Format.GeoJSON();
                    // geojson_format.ignoreExtraDims=true;
                    // var vector_layer = new ol.layer.Vector(name); 
                     
                    //vector_layer.addFeatures(geojson_format.read(featurecollection));
                    // vector_layer.getSource().addFeatures(new ol.format.GeoJSON().readFeatures(featurecollection));    
                    // out.push(vector_layer);
                
                // },

                // complete : function(jqXHR, status) {
                
                // },
 
                // error : function(jqXHR, msg, exception) {
                 
                // }
            // });
                
                }
            };
        }
        
        return out;
    };

    var axeOnline = function() {
    	
    	var out = [];
        var names = {};
        
        if (report.online) {
            for (var i = 0; i < report.online.length; i++) {
                var onlineInfo = report.online[i];
                if (onlineInfo.protocol && 
                		onlineInfo.protocol.toLowerCase() === 'gi-axe' && 
                		onlineInfo.name && 
                		!names[onlineInfo.name] ) {
                    
                    names[onlineInfo.name] = onlineInfo.name;
                    out.push(onlineInfo);
                }
            }         
        }

        return out;
    };

    var dabEndpoint = function() {

        // dabNode is a string when node is created by DAB
        return typeof dabNode === 'string' ? dabNode : dabNode.endpoint();
    };
    
    return giNode;
};
