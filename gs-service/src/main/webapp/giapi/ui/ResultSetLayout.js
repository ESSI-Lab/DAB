/**
 * This module provides widget and components which allow to easily create Graphical User interfaces  
 *
 * @module UI
 * @main UI 
 **/

 /**
  *  A <code>ResultSetLayout</code> displays the {{#crossLink "UINode/isRenderable:method"}}renderable nodes{{/crossLink}} of the 
  *  current {{#crossLink "Page"}}page{{/crossLink}} in the <code>{{#crossLink "ResultSet"}}{{/crossLink}}</code> arranging them in 
  *  a <code>&lt;table&gt;</code>; each column contains a {{#crossLink "UINode"}}node{{/crossLink}}. 
  *  By default each <code>&lt;table&gt;</code> row displays a single column; the maximum number of column in a row can be set with the <code><a href="columnCount">columnCount</a></code> option.<br>
  *  If the <a href="https://api.geodab.eu/docs/classes/DAB.html#extend">ResultSet extension</a> provides more than one {{#crossLink "ResultSet"}}result set{{/crossLink}},
  *   this tool creates a tab for each {{#crossLink "ResultSet"}}result set{{/crossLink}} <code>&lt;table&gt;</code>. The label of each tab provides the term identified by the "semantic engine" used to generate the correspondent {{#crossLink "ResultSet"}}result set{{/crossLink}}.
  *  
  *  In order to be {{#crossLink "UINode/render:method"}}rendered{{/crossLink}}, the {{#crossLink "UINode"}}{{/crossLink}} class must be <i>registered</i> by adding its reference 
  *  to the <code><a href="uiNodes">uiNodes</a></code> array. For details and examples about registration and {{#crossLink "UINode/render:method"}}rendering of the nodes{{/crossLink}}, 
  *  see <a href="../classes/UINode.html#renderableNodes">this section</a>.<br>

  *  If the optional <code>options.mapWidget</code>, <code>options.pagWidget</code> and <code>options.tfWidget</code> instances are provided, their <code>update</code> method 
  *  is called during the layout {{#crossLink "ResultSetLayout/update:method"}}updating{{/crossLink}}.
  *   
  *  As well as the documented properties, all the additional <code><a href="#resSetLayOptions">options</a></code> property are passed as argument to each 
  *  {{#crossLink "UINode/isRenderable:method"}}renderable node{{/crossLink}} constructor.
  *  
  *  For CSS personalization, see the <code>resultset-layout-table</code> class of the <a href="https://api.geodab.eu/docs/assets/css/giapi.css">API CSS</a> file

  *  @param {String} id id of an existent HTML container (typically <code>&lt;div&gt;</code> element) in which the layout is inserted

  *  @param {Object} [options] <a name="resSetLayOptions">as well as the following properties</a>, all the additional properties of this object are passed as argument 
  *  to each {{#crossLink "UINode/isRenderable:method"}}renderable node{{/crossLink}} constructor. For example, this allows a <code>{{#crossLink "UINode"}}{{/crossLink}}</code> to 
  *  {{#crossLink "UINode/render:method"}}{{/crossLink}} itself in a compact way in case the <a href="../classes/ResultSetLayout.html#columnCount">column count</a> is greater than one. 
  *    
  *  @param {UINode[]} [options.uiNodes] <a name="uiNodes">array</a> of registered <code>{{#crossLink "UINode"}}{{/crossLink}}</code>
  *   classes to instantiate during the <a href="../classes/UINode.html#renderableNodes">search</a> for a {{#crossLink "UINode/isRenderable:method"}}renderable node{{/crossLink}} </code> 
  *  
  *  @param {UINode} [options.commonUINode=Common_UINode] <a name="commonUINode">the UI node</a> subclass to instantiate when the <a href="../classes/UINode.html#renderableNodes">search</a> for 
  *  {{#crossLink "UINode/isRenderable:method"}}renderable nodes{{/crossLink}} fails. By the default a <code>{{#crossLink "Common_UINode"}}{{/crossLink}}</code> instance is 
  *  {{#crossLink "UINode/render:method"}}rendered{{/crossLink}}
  *  
  *  @param {ResultsMapWidget} [options.mapWidget] an instance of <code>{{#crossLink "ResultsMapWidget"}}{{/crossLink}}</code> to <code>{{#crossLink "ResultsMapWidget/update:method"}}{{/crossLink}}</code>
  *  @param {PaginatorWidget} [options.pagWidget] an instance of <code>{{#crossLink "PaginatorWidget"}}{{/crossLink}}</code> to <code>{{#crossLink "PaginatorWidget/update:method"}}{{/crossLink}}</code>
  *  @param {TermFrequencyWidget} [options.tfWidget] an instance of <code>{{#crossLink "TermFrequencyWidget"}}{{/crossLink}}</code> to <code>{{#crossLink "TermFrequencyWidget/update:method"}}{{/crossLink}}</code>
  *  
  *  @param {Integer} [options.columnCount=1] <a name="columnCount">maximum number of columns</a> of each <code>&lt;table&gt;</code> row; this determines the maximum number of {{#crossLink "UINode"}}nodes{{/crossLink}} 
  *  to display in a single row 
  *  
  *  @param {Integer} [options.maxHeight=0]  
  *
  *  @param {Function} [options.onUpdateReady] <a name="onUpdateReady">callback function</a> called when the layout updating is ready
  *  @param {UINode[]} [options.onUpdateReady.renderedNodes] array with the <a href="../classes/UINode.html#method_render">rendered nodes</a> 
  *  
  *  @param {Boolean} [options.browseCollection] if set, enables the browsing of collections
  *  @param {String} [options.browseCollectionMapType] 

  *  @param {Function} [options.onDiscoverResponse] required if <code>browseCollection</code> is set. It is called by the refiner methods
  *  @param {Function} [options.onBrowsingReady] <a name="onBrowsingReady">callback function</a> called when the browsing of collection/s is ready
  *
  *  @param {Boolean} [options.preserve]
  *  @param {Function} [options.sortingFunction]
  *
  *  @constructor 
  *  @class ResultSetLayout
  */
GIAPI.ResultSetLayout = function(id, options) {

	var layout = {};
	var resultSets = [];
	var preservedNodes = [];
	var extensions = [];
    var renderedNodes = [];
    var refButton;
    var whereWidg;
    var inputControl;
    var browser;
    
	if(!options){
		options = {};
	}
	
	if(options.preserve === null || options.preserve === undefined){
		options.preserve = false;
	}
			
	if(!options.commonUINode){
		options.commonUINode = GIAPI.Common_UINode;
	}
	
	if(!options.columnCount){
		options.columnCount = 1;
	}
	
	if(!options.maxHeight){
		options.maxHeight = 0;
	}
	
	/**
	 * Updates the layout according to the provided <a href="../classes/DAB.html#response">discover response</a>
	 * 
	 * @param {Object} response
	 * 
	 * @method update
	 */
	layout.update = function(response){
				
		// reset the rendered nodes array
		renderedNodes = [];
    	
    	// extended result set
    	if(response[0].extension){  	
    		
    		// -------------------------------------------	
    		// response of a discover request (length > 1)
    		//
	        if(response._origin === 'dab'){
	        		        	
	       	     // if a tab already exists it must be destroyed
		   	   	 // before to create a new one
		   	   	 if(jQuery('#'+id).tabs('instance')){ 
		   	   		 jQuery('#'+id).tabs('destroy');
		   	   	 }
		   	     jQuery('#'+id).empty();
	        	 
	          	 // the resultSets array is reset
	        	 resultSets = [];
	        	 
	        	 // builds the tabs
	        	 var ul = '<ul class="resultset-layout-ul">';
	        	 response.forEach(function(rs,index){
	        		 
	        		 resultSets.push(rs);
	        		 
	        		 var label = GIAPI.formatWords(rs.extension.label);
	        		 ul += '<li><a href="#resSet-'+index+'">'+label+'</a></li>';       		 
	        	 });
	        	 
	        	 ul += '</ul>';
	        	 jQuery('#'+id).append(ul);
	        	 jQuery('#'+id).tabs();
	        	 
	        	 // when a tab is selected, the widgets are updated with the
	        	 // correspondent result set
	        	 jQuery('#'+id).tabs({
	        		 activate: function( event, ui ) { 
	        			 var selector = ui.newPanel.selector;
	        			 var index = selector.substring(selector.indexOf('-')+1, selector.length);
	        			 
	        			 updateWidgets(resultSets[index],response[0].termFrequency);		             			 
	        		 }
	        	 });
	         }else{
	        	// ---------------------------------------------	
	     		// response of a paginator request (length === 1)
	     		// the tab and the resultSets array already exist
	        	// 
	        	var ext = response[0].extension;
	        	var index = ext.resultSetIndex;
	        	
	        	// updates the resultSets array with the current result set
	        	resultSets[index] = response[0];
	        	
	        	updateWidgets(resultSets[index], response[0].termFrequency);		
	         }
    	 }else{
 			 jQuery('#'+id).empty();	  
    	 }
    	
    	 // a table for each result set
         response.forEach(function(rs,index){
         
	         var resultSet = rs;
	         var paginator = resultSet.paginator;	         
	         var refiner = resultSet.refiner;	         
	         var page = paginator.page();
	          
	         
	         // set the response to the options
	         // so it can be reused by the node ui nodes
	         options.response = response;
	         
	         var tableId = GIAPI.random();
	         var headerDivId = GIAPI.random();
	         var collectionsDiv = '<div  id="'+headerDivId+'"></div>';	         
	         var content = collectionsDiv;
	         
	         if(!options.height){
	        	 options.height = '';
	         }else{
	        	 options.height = 'height:'+options.height+'px';
	         }
	         
	         if(options.maxHeight > 0){
		         content += '<div class="resultset-layout-table-div" style="max-height:'+options.maxHeight+'px; overflow-y:auto">';
		         content += '<table style="'+options.height+'" class="resultset-layout-table"><tbody id="' + tableId + '"/></table></div>';
	         }else{
		         content += '<table style="'+options.height+'" class="resultset-layout-table"><tbody id="' + tableId + '"/></table>';
	         }
	         
        	 var tabId  = 'resSet-0';
	         if(resultSet.extension){
		         
	        	 tabId  = 'resSet-'+index;        	 
	        	 // response of a paginator request 
		         if(response._origin === 'paginator'){
		        	
		        	// the tab to update depends on the current extension
	        		var ext = resultSet.extension;
	 	        	index = ext.resultSetIndex;
	 	        	tabId = 'resSet-'+index;
	 	        	
	 	        	// removes the previous tab content
	 	        	jQuery('#'+tabId).remove();
	        	 }
	        	 	        	
	        	 content = '<div id="'+tabId+'">'+content+'</div>';	 	
	         }
	         
	         // inserts the result set content           
	         jQuery('#'+id).append(content);
	         
	         if(refiner && options.browseCollection){  	        	 
	     	    // ---------------------------------------
	     		// creates the browser
	     		//
	         	browser = GIAPI.SeriesBrowserWidget(options); 	         	
	         	browser.init(headerDivId,refiner);
	        	 	     			     		 
//	     		layout._updateBrowser();    		  
	         }	        	         
	                                                   
	         var rowNumber = 0;
	         
	         var nodes = [];
	         var tmp = [];
	         while (page.hasNext()) {
	        	 var n = page.next();
	        	 nodes.push(n);
	        	 tmp.push(n);
	         }
	         
	         if(options.preserve){
	        	 
	        	 // new nodes are stored before the old ones
	        	 for(var i = 0; i < preservedNodes.length; i++){
		        	 tmp.push(preservedNodes[i]);
		         }
		         
		         preservedNodes = tmp;
	        	 nodes = preservedNodes;
	        	 
	        	 if(options.sortingFunction){
	        		 nodes.sort(options.sortingFunction);
	        	 }	        	                 
	         }
	            
	         // resets the page in order to make it reusable in case
	         // the widgets are updated when a tab is selected
	         page.reset();
	         	               
	         // updates the widgets with the only result set,
	         // or with the first (index === 0) in case of extension since the first tab is selected
	         if(response.length === 1 || index === 0){
	        	 
	        	 if(options.preserve){
	        		 
	        		 resultSet.size = nodes.length;
	        		 resultSet.pageSize = nodes.length;

	                 paginator._page = GIAPI.Page(nodes, resultSet.pageSize);
	                 resultSet.paginator = paginator;
	        	 }
	        	 
	        	 updateWidgets(resultSet, response[0].termFrequency);		         
	         }    
	        
	         var nodeIndex = 0;
	         for(var rowNumber = 0;  nodeIndex < nodes.length ;rowNumber ++){
	        	 
	        	 var rowId = tabId+'_row_' + rowNumber;
	        	 
	        	 // appends the row
	             jQuery('#' + tableId).append('<tr id=' + rowId + '/>');
	        	         	 
		         for(var colNumber = 0; colNumber < options.columnCount && nodeIndex < nodes.length; colNumber++){
		    	        
	   	        	  var currentNode = nodes[nodeIndex];
	   	        	  
	   	           	  // reset the options because the UI nodes can alter them
		   	          var opt = {};
		   	          // set a reference to this layout
		   	          opt.layout = layout;
		   	     	  for(property in options){
		   	     		  opt[property] = options[property];
		   	    	  }
	   	        
	  	        	  // creates the common UI node
	   	        	  var renderedNode = opt.commonUINode( opt );
	   	        	  
	   	        	  var sectionDom = opt.sectionDom;
	   	        	  var asideDom = opt.asideDom;
	   	        	  var onSectionReady = opt.onSectionReady;
	   	        	  var onAsideReady = opt.onAsideReady;
	
	   	        	  // tries to find another type of node to render
	   	        	  // in case of more renderable nodes, the list priority
	   	        	  // determines which node will be rendered
	   	        	  if(opt.uiNodes){
	   	        		  for(var i = opt.uiNodes.length-1; i >=0; i--){
	   	        			  var uiNodeClass = opt.uiNodes[i];
	   	        			  var uiNodeInstance = uiNodeClass( opt );
	   	        			  if(uiNodeInstance.isRenderable( currentNode )){
	   	        				  renderedNode = uiNodeInstance;
	   	        				  break;
	   	        			  }else{
	   	        				  // when a UI node is created it can overrides one or
	   	        				  // both the rendering functions; so if the node created above is 
	   	        				  // not the candidate to be rendered, 
	   	        				  // the common UI node functions are restore
	   	        				  opt.sectionDom = sectionDom;
	   	        				  opt.asideDom = asideDom;
	   	        				  opt.onSectionReady = onSectionReady;
	   	        	        	  opt.onAsideReady = onAsideReady;
	   	        			  }
	   	        		  }      		 
	   	        	  }
	   	        	  	   	        	 	                  	   	        	     	        	  
	   	        	  var colId = 'cell-' + currentNode.uiId;
	           	  
	   	        	  // open the current cell
	                  jQuery('#' + rowId).append('<td class="resultset-layout-td" id="'+colId+'">');
	                        		  
	            	  // renders the node
	           	      renderedNode.render(currentNode, rowNumber, rowId, colNumber, colId);
	           	      
	           	      // insert the rendered node in the list for the onUpdateReady function
	           	      renderedNodes.push(renderedNode);          	      
	           	      
	                  // closes the current cell
	                  jQuery('#' + rowId).append('</td>');
		        	     	        	   
		        	  nodeIndex++;
		         }
	         }

	         // refreshes the tab
	         if(jQuery('#'+id).tabs('instance')){ 
	    		 jQuery('#'+id).tabs('refresh');
			 }
	         
	         if(options.onUpdateReady){
	        	 options.onUpdateReady.apply(this,[renderedNodes]);
	         }
         });
	};
	
	/**
	 * 
	 */
	layout.empty = function(){
		
		preservedNodes = [];
		layout.update(GIAPI.emptyResponse());		
	};
	
	layout.browser = function(){
		
		return browser;
	};
	
 	var updateWidgets = function(resultSet, tf){
		
		if(options.mapWidget){
	       	 options.mapWidget.update(resultSet);
        }
        
        if(options.pagWidget){
	       	 options.pagWidget.update(resultSet);
        }
        
        if(tf && options.tfWidget){
	       	 options.tfWidget.update(resultSet);
        }     
	}
	 	
	return layout;
};