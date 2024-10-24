/**
 * @module UI
 **/

 /**
  *  This class extends <code>{{#crossLink "UINode"}}{{/crossLink}}</code> in order to provide a common graphical representation of the 
  *  <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> as depicted in the below snapshots.<br>
  *  The following CSS is required:<pre><code>
 &lt;!-- API CSS --&gt;
 &lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
</code></pre>
  *  The <code>&lt;section&gt;</code> element on the left contains the following subsections:<ul>
<li><code>{{#crossLink "Report/title:property"}}report title{{/crossLink}}</code></li><li>abbreviated <code>{{#crossLink "Report/description:property"}}report description{{/crossLink}}</code></li><li>a maximum of 5 clickable {{#crossLink "GINode/overview:method"}}overviews{{/crossLink}} loaded with the <code>{{#crossLink "UI_Utils/loadOverview:method"}}{{/crossLink}}</code> utility</li><li>a button to add the {{#crossLink "GINode/googleImageMapType:method"}}layers{{/crossLink}} to the <code>{{#crossLink "ResultsMapWidget"}}{{/crossLink}}</code> (see <code>{{#crossLink "ResultSetLayout"}}{{/crossLink}}</code> options for more info) and a maximum of 5 buttons for the {{#crossLink "GINode/directAccessLinks:method"}}direct access links{{/crossLink}}</li><li>a "MORE INFO" {{#crossLink "ButtonsFactory/onOffSwitchButton:method"}}button{{/crossLink}} which shows/hide a panel with additional {{#crossLink "Report"}}report{{/crossLink}} information</li></ul>
     The <code>&lt;aside&gt;</code> element contains a <code>{{#crossLink "NodeMapWidget"}}{{/crossLink}}</code> that is appended to the <code>&lt;aside&gt;</code> 
     by the <code>options.onAsideReady</code> callback
     
  *  <img style="width: 1000px;border: none;" src="../assets/img/common-ui-node-1.png" /><br>
	 <i>The image above shows a <code>Common_UINode</code> with the additional <code>{{#crossLink "Report"}}report{{/crossLink}}</code> 
  *  information hide</i> 

  *  <img style="width: 1000px;border: none;" src="../assets/img/common-ui-node-2.png" /><br>
 	 <i>The image above shows a <code>Common_UINode</code> with the additional <code>{{#crossLink "Report"}}report{{/crossLink}}</code> 
  *  information visible</i> 
  *  
  *  See also the <code>common-ui-node</code> class in the <a href="https://api.geodab.eu/docs/assets/css/giapi.css">API CSS</a> file
  *  
  *  @param {Object} [options]
  *  
  *  @param {Function} [options.sectionDom] returns the DOM of the <code>&lt;section&gt;</code> element
  *  @param {Function} [options.asideDom] returns the DOM of the <code>&lt;aside&gt;</code> element
  *  
  *  @param {Function} [options.onSectionReady] callback function called when <code>&lt;section&gt;</code> element is ready
  *  @param {Function} [options.onAsideReady] callback function called when <code>&lt;aside&gt;</code> element is ready 
  *  Appends the  <code>{{#crossLink "NodeMapWidget"}}{{/crossLink}}</code> to the <code>&lt;aside&gt;</code> element
  *  
  *  @param {DAB} [options.dabNode] Reference to the <a href="../classes/DAB.html">DAB</a> object; if not set, the "browse collection" button is not provided
  *    
  *  @param {Function} [options.onDiscoverResponse] a reference to the <a href="../classes/DAB.html#onResponse">onResponse</a> discover callback; this function is called when 
  *  the "browse collection" button is clicked. If not set, the "browse collection" button is not provided
  *
  *  @param {Integer} [options.pageSize=10] Set the default <a href="../classes/DAB.html#property_d_pageSize">page size</a> used for to "browse collection"
  *  
  *  @constructor 
  *  @class Common_UINode
  *  @extends UINode
  */
GIAPI.Common_UINode = function(options) {
	
	var uiNode = GIAPI.UINode(options);	 
    var nodeMapId = GIAPI.random();
    var layout = options.layout;	
		        
    /**
	 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
	 * 
     * 
     * @static
	 * @property {String} [GIAPI.Common_UINode.mapType=]
	 */
    GIAPI.Common_UINode.mapType;
    
    /**
	 * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
	 * 
     * 
     * @static
	 * @property {String} [GIAPI.Common_UINode.mapTypeId=]
	 */
    GIAPI.Common_UINode.mapTypeId;
    
    /**
     * <span style="margin-left:-1px;" class="flag optional" title="This parameter is optional.">optional</span>
     * 
     * 
     * @static
     * @property {String} [GIAPI.Common_UINode.mapMode=]
     */
    GIAPI.Common_UINode.mapMode;
 			
 			
	/**
	 * Creates a row of a table with the <code>report.author</code> information
	 * 
	 * @static
	 * @method GIAPI.Common_UINode.authorRow
	 * @param {Report} report
	 */
	GIAPI.Common_UINode.authorRow = function(report){
		
		var out = '';
		
	 	// author
	   	if(report.author && report.author.length > 0){
	   		
		   	out += '<tr><td colspan="2" class="common-ui-node-report-content-table-left-td"><label style="margin-top:5px;margin-bottom: 5px;" class="common-ui-node-report-content-table-left">Contacts</label></td></tr>';
   			var first = true;	   		   		 	
	   		report.author.forEach(function(auth){
	   			
	   			if(!first){
	   				out += '<tr><td colspan="2">'+GIAPI.UI_Utils.separator('margin-left:40px; width: 110px;')+'</td></tr>';
	   			}
   				first = false;
	   				   			
	   			var indName = auth.individualName;
	   			var posName = auth.positionName;
	   			var orgName = auth.organizationName;
	   			var email = auth.email;
	   			var city = auth.city;
	   			var url = null;
	   			if(auth.online && auth.online.length > 0){
	   				url = auth.online[0].url;
	   			}
	   			
	   			if(indName){
	   			   	out += '<tr><td class="common-ui-node-report-content-table-left-td"><label style="margin-left:40px">Name</label></td>';
	   			   	out += '<td class="common-ui-node-report-content-table-right-td"><label>'+indName+'</label></td></tr>';
 	   			}
	   			
	   			if(posName){
	   			   	out += '<tr><td class="common-ui-node-report-content-table-left-td"><label style="margin-left:40px">Position</label></td>';
	   			   	out += '<td class="common-ui-node-report-content-table-right-td"><label>'+posName+'</label></td></tr>';
 	   			}
	   			
	   			if(orgName){
	   			   	out += '<tr><td style="width: 150px;" class="common-ui-node-report-content-table-left-td"><label style="margin-left:40px">Organization</label></td>';
	   			   	out += '<td class="common-ui-node-report-content-table-right-td"><label>'+orgName+'</label></td></tr>';
 	   			}
	   			
	   			if(city){
	   			   	out += '<tr><td class="common-ui-node-report-content-table-left-td"><label style="margin-left:40px">City</label></td>';
	   			 	out += '<td class="common-ui-node-report-content-table-right-td"><label>'+city+'</label></td></tr>';
 	   			}	
	   			
	   			if(url){
	   				if(url.endsWith('/')){
	   					url = url.substring(0,url.length-1);
	   				}
	   			   	out += '<tr><td style="width: 150px;" class="common-ui-node-report-content-table-left-td"><label style="margin-left:40px">Home page</label></td>';
	   			   	out += '<td class="common-ui-node-report-content-table-right-td"><a style="color:blue;cursor:pointer;text-decoration: underline;" target=_blank href='+url+'>'+url+'</a></td></tr>';
 	   			}
	   			
	   			if(email){
	   			   	out += '<tr><td class="common-ui-node-report-content-table-left-td"><label style="margin-left:40px">Email</label></td>';
	   			   	out += '<td class="common-ui-node-report-content-table-right-td"><a style="color:blue;cursor:pointer;text-decoration: underline;" target=_blank href=mailto:'+email+'>'+email+'</a></td></tr>';
 	   			}	   			
	   		});	   		
	   	}
	   	
	   	return out;
	};
	
	/**
	 * 
	 */
	GIAPI.Common_UINode.createdRow = function(report){
		
		var out = '';
		
		// created
	   	if(report.created){
	   		
	   		var createdId = GIAPI.random();
	   		out += '<tr><td></td></tr>';
	   		out += '<tr><td class="common-ui-node-report-content-table-left-td"><label class="common-ui-node-report-content-table-left">Created</td>';
	   		out += '<td id="'+createdId+'" class="common-ui-node-report-content-table-right-td">';
	   		out += '<div style="margin-top:2px"><label style="cursor: pointer;">'+report.created+'</label></div></td></tr>';
	   		
	   		jQuery(document).on('click','#'+createdId, function(event){
   				
   				var date = report.created.substring(0,10);
	   			var time = report.created.substring(11);
	   			
	   			jQuery('#'+createdId).datepicker('dialog',date,null,{
	                 dateFormat : "yy-mm-dd",			               
	                 minDate: date,
	                 maxDate: date,
	            },[event.pageX, event.pageY-100]);
	   			
	   		    jQuery('#ui-datepicker-div').append('<div style="border: 1px solid rgba(0, 0, 0, 0.21);padding:5px"><label style="font-weight: bold;;margin-left:56px">'+time+'</label></div>');
		   });
	   	}
	   	
	   	return out;
	};
	
	/**
	 * Creates a row of a table with the <code>report.when</code> information
	 * 
	 * @static
	 * @method GIAPI.Common_UINode.whenRow
	 * @param {Report} report
	 */
	GIAPI.Common_UINode.whenRow = function(report){
		
		var out = '';
		
		// when
	   	if(report.when && report.when.length > 0){   
	   		
			var toId = GIAPI.random();  
	   		 
	   		out += '<tr><td colspan=2><table style="margin-top:5px; width: 450px"><tbody><tr>';
	   		 
	   		if(report.when[0].start || report.when[0].from){	   			   				 
  	   		     out += '<td style="width:200px">'+GIAPI.Common_UINode.whenDiv('from',report)+'</td>'; 
  	   		}
	   		 
	   		if(report.when[0].end || report.when[0].to){	   			 
  	   			 out += '<td style="width:200px">'+GIAPI.Common_UINode.whenDiv('to',report)+'</td>'; 
	   		}
	   		 
	   		out += '</tr></tbody></table></td></tr>';
	   	}
	   	
	   	return out;
	};
	
	/**
	 * Creates a &lt;div&gt; with the given date value which can be clicked to show the calendar 
	 * 
	 * @static
	 * @method GIAPI.Common_UINode.whenDiv
	 * @param {String} time
	 */
	GIAPI.Common_UINode.whenDiv = function(time,report){
		
   	   	 var id = GIAPI.random();
   	   	 
   	   	 var value = '';
   	   	 var label = '';
   	   	 switch(time){
   	   	 case 'from':
   	   		 label = 'Start time';
   	   		 value = report.when[0].start || report.when[0].from;
   	   		 break;
   	   	 case 'to':
   	   		 label = 'End time';
   	   		 value = report.when[0].end || report.when[0].to;
	   		 break;
   	   	 }
   	   	 
   	   	 // removes eventual other values such as seconds and/or timezone (e.g.:000+02:00) 
   	   	 value = value.substring(0,19);
   	   	 
   	   	 var width = GIAPI.UI_Utils.getTextWidth(value,"normal 1.1em 'Helvetica Neue', Helvetica, Arial, sans-serif");	  
		 width += 30;
   	   	 
 		 var out = '<div style="width:150px">';
	     out += '<label class="common-ui-node-report-content-table-left">'+label+'</label></div>';
	     var timeLabel = value.replace('T',' ');
		 out += '<label class="common-ui-node-time-label" id='+id+'>'+timeLabel+'</label>';
		 out += '</div>'; 
		 
		 jQuery(document).on('click','#'+id, function(event){
		 
			 var date = value.substring(0,10);
		     var time = value.substring(11);
		        		     
	       	 jQuery('#'+id).datepicker('dialog',date, null,{
	             dateFormat : "yy-mm-dd",
	             minDate: date,
	             maxDate: date
	             
	         },[event.pageX, event.pageY-100]);
	       	 
	   		 jQuery('#ui-datepicker-div').append('<div style="border: 1px solid rgba(0, 0, 0, 0.21);padding:5px"><label style="font-weight: bold;;margin-left:56px">'+time+'</label></div>');
			
		 });
		 
		 return out;
	};
	
	options.sectionDom = function(node,options,sectionId){
		
        // creates the div
		var sectionDivId = 'section-div-'+node.uiId;
        var sectionDiv = '<div id="'+sectionDivId+'" class="common-ui-node-report-div">';
        
        jQuery(document).on('mouseover','#'+sectionDivId, function(){
        	if(options.mapWidget){
        		options.mapWidget.markerIcon(node,{url: 'https://api.geodab.eu/docs/assets/img/circle-yellow-marker.png'});
        	}
        });
        jQuery(document).on('mouseout','#'+sectionDivId, function(){
        	if(options.mapWidget){
        		options.mapWidget.markerIcon(node,{color:'red'});
        	}
        });
      
        // creates the first subsection                   	 
        sectionDiv += subSection1(node,sectionId);
     	
        var reportDivId = GIAPI.random();
        var measurementDivId = GIAPI.random();
        var downloadDivId = GIAPI.random();

        // creates the second subsection
        sectionDiv += buttonsDiv(node,sectionId,measurementDivId,reportDivId,downloadDivId);
               
     	// creates the report content div
     	sectionDiv += reportContentDiv(node, reportDivId); 
     	
    	// creates the measurement content div
     	sectionDiv += measurementContentDiv(node, measurementDivId); 
     	
     	sectionDiv += '<div style="display:none" id="'+downloadDivId+'"></div>';
     	
     	return sectionDiv;
	};
	
	options.asideDom = function(node, options, asideId){
			    	
    	var out = null;
    	if(node.report().where){
            out  = '<div id='+nodeMapId+' class="common-ui-node-node-map-div"></div>';            
        }
    	
    	return out;
	};
	
	options.onAsideReady = function(aside,node){
		
		// appends the NodeMapWidget
        var nodeMapWidget = GIAPI.NodeMapWidget(
       		nodeMapId, 
       		node,
       		{	'mapType': GIAPI.Common_UINode.mapType || GIAPI.ui.mapType || 'google',
       			'mapTypeId': GIAPI.Common_UINode.mapTypeId || GIAPI.ui.mapTypeId || google.maps.MapTypeId.ROADMAP,
       			'mode':GIAPI.Common_UINode.mapMode || 'al',
       			'zoom': 9,
       			'height': 140
   			});
         nodeMapWidget.init();    
	};
	  
    // -----------------------------------------------------------
    // private methods -------------------------------------------
    // -----------------------------------------------------------
	
	// overview, title and description
    var subSection1 = function(node){
	   
	    var desc = node.report().description;
	    if(!desc || desc === 'none'){
	    	desc = 'No description available';
	    }
	    var title = node.report().title;
	   
        // this anchor is used to localize this ui node when clicking on a marker of the result map
        var out = '<a name="node-'+node.uiId+'"/>';
               
        var divId = 'overview_div_' + node.uiId;
        out += '<table><tr>';
	 	out += '<td><div class="no-overview-div" style="width:96px;height:96px" id="'+divId+'"></div></td>';
       
  	    GIAPI.UI_Utils.loadOverview(node, divId,  {'force':true, 'selector':true, 'maintainSize':true });

  	    out += '<td style="width:100%">';
        if(!options.noTitle){
        	out += '<label class="common-ui-node-report-title">'+title+'</label><br><br>';
        } 
        
        out += '<textarea rows="4" class="common-ui-node-report-desc" readonly>'+desc+'</textarea></td>';   
        
        var fromDiv = '';
        var toDiv = '';
        
        if(node.report().when){
        	
	    	if(node.report().when[0].start || node.report().when[0].from){	   	
	    		var style = 'margin-bottom: 15px;';
	    		fromDiv = '<div style="'+style+'">'+GIAPI.Common_UINode.whenDiv('from',node.report())+'</div>'; 
	   		}
		 
	  		if(node.report().when[0].end || node.report().when[0].to){	   			 
	  			toDiv = GIAPI.Common_UINode.whenDiv('to',node.report());
	  		}
  		}

        out += '<td>'+fromDiv+toDiv+'</td>';
        
        out += '</tr></table>';
               
        return out;
    };
    
    // collections buttons, layers and download buttons
    var buttonsDiv = function(node,sectionId,measurementDivId,reportDivId,downloadDivId){
	   
       var out = '<div class="common-ui-node-button-div" style="margin-bottom:5px; margin-top:5px; display:inline-block">';
       
       // -------------------------------------------------------
       // measurement info button
       // 
       var minfoButtonId = 'minfo-button-'+node.uiId;       
       var mtoggleButton = GIAPI.ToggleButton({
			'id': minfoButtonId,
			'width': 18,
			'targetId': measurementDivId,
			'offLabel':'',
			'onLabel':'',
			'attr':[{ name:'title',value:'Measurement info'  }]
       });
       mtoggleButton.stateIcon('on','fa-bar-chart');
       mtoggleButton.stateIcon('off','fa-bar-chart');
       mtoggleButton.css('div','padding','4px');
       mtoggleButton.css('div','background','#009688');

       mtoggleButton.css('icon','margin-left','2px');
       mtoggleButton.css('icon','font-size','15px');
       mtoggleButton.css('icon','vertical-align','middle');

       out += '<div style="margin-left:3px; display: inline-block;">'+mtoggleButton.div()+'</div>';
       
       // -------------------------------------------------------
       // more info button
       // 
       var infoButtonId = 'info-button-'+node.uiId;       
       var toggleButton = GIAPI.ToggleButton({
			'id': infoButtonId,
			'width': 18,
			'targetId': reportDivId,
			'offLabel':'',
			'onLabel':'',
			'attr':[{ name:'title',value:'More info'  }]
       });
       toggleButton.stateIcon('on','fa-info-circle');
       toggleButton.stateIcon('off','fa-info-circle');
       toggleButton.css('div','padding','4px');
       toggleButton.css('div','background','#005aef');

       toggleButton.css('icon','margin-left','2px');
       toggleButton.css('icon','font-size','15px');
       toggleButton.css('icon','vertical-align','middle');

       out += '<div style="margin-left:3px; display: inline-block;">'+toggleButton.div()+'</div>';
                   
       // -------------------------------------------------------
       // full xml info button
       // 
       if(GIAPI.Common_UINode.dabNode || options.dabNode){
    	   var dabNode = GIAPI.Common_UINode.dabNode || options.dabNode;
	       var xmlButton  = GIAPI.FontAwesomeButton({   
			    'width': 22,
			    'label':'',
		        'icon':'fa-file-text-o',
		        'attr':[{'name':'nodeid','value':node.report().id},{'name':'title','value':'Full metadata'}],
		        'handler': function(){
		        	
		        	 GIAPI.UI_Utils.openNodeInfoPage(dabNode, jQuery(this).attr('nodeid'));		        		       	 	 
		             return false;
		        }
	  	   });
	       xmlButton.css('div','margin-left','3px');
	       xmlButton.css('icon','font-size','13px');
	       xmlButton.css('div','padding','4px');
	       xmlButton.css('div','background','#005aef');
	
	       out += '<div style="margin-left:3px; display: inline-block;">'+xmlButton.div()+'</div>';
       }
       
       var insertCR = false;
       
       // -------------------------------------------------------
       // for composed nodes, if a refiner is available inserts
       // the browse content buttons
       // 
       var resultSet = options.response && options.response[0];  
       
       if(node.report().type === 'composed' 
    	   && resultSet && resultSet.refiner
    	   && (GIAPI.Common_UINode.onDiscoverResponse || options.onDiscoverResponse)){
    	      
    	   var buttonId = GIAPI.random();  	   
      	   var iconId = GIAPI.random();
    	   
    	   out += '<input nodeid="'+ node.report().id +'" id="'+buttonId+'" class="common-ui-node-browse-collection-button" value="" type="button"/>';
    	   out += '<i title="Browse collection content" nodeid="'+ node.report().id +'" id="'+iconId+'" class="common-ui-node-browse-collection-icon fa fa-folder-open-o" aria-hidden="true"/>';
    	   
    	   var handler = function(){
   		    
   		    	GIAPI.UI_Utils.discoverDialog('open');
   		    	
   		    	resultSet.refiner.browse(
	    			GIAPI.Common_UINode.onDiscoverResponse || options.onDiscoverResponse,
	    			jQuery(this).attr('nodeid'),{
 						'pageSize': GIAPI.Common_UINode.pageSize || options.pageSize
 					}
    			);	   		    
	   	   };
    	   
    	   jQuery(document).on('click','#'+buttonId, handler);
    	   jQuery(document).on('click','#'+iconId, handler);
    	   
    	   // -------------------------
    	   // adds the collection check
    	   //
    	   if(options.browseCollection){
    		   out += createCollectionCheck(resultSet.refiner,node);
    	   }
       }

       // -------------------------
	   // layers button
	   //
       if(options.mapWidget){
	        var button = options.mapWidget.addLayersButton(node);

	        if(button){
	        	out += '<div style="margin-left: 5px; display:inline-block">'+button.div()+'</div>';
	        }
       }
              
       //--------------------------
       // axe download button
	   //
//       if(GIAPI.Common_UINode.dabNode || options.dabNode){
//    	   
//	       var axeButtonId = 'axe-button-'+node.uiId;   
//	       
//	       var divAppended = false;
//	       var beforeStart = function(){
//	    	   
//	            if(!divAppended){
//		  		  
//		    	    var opt = {};
//		      	    opt.node = node;
//		      	    opt.dabNode = GIAPI.Common_UINode.dabNode || options.dabNode;
//		      	    opt.divId = downloadDivId;
//		      		 
//		      	    var widget = GIAPI.DownloadWidget(opt);
//	            
//	            	divAppended = true;
//	             	            	
//	            	jQuery('#'+downloadDivId).css('display','none');
//	            }
//	  	   };
//	       
//	       var axeButton = GIAPI.ToggleButton({
//				'id': axeButtonId,
//				'width': 18,
//				'targetId': downloadDivId,
//				'offLabel':'',
//				'onLabel':'',
//				'attr':[{ name:'title', value:'Download options'}],
//				
//				'beforeStart': beforeStart
//	       });
//	       
//	       axeButton.stateIcon('on','fa-download');
//	       axeButton.stateIcon('off','fa-download');
//	       axeButton.css('div','padding','4px');
//	       axeButton.css('div','background','#4CAF50');
//	       axeButton.css('div','color','#FAFAFA');
//	       axeButton.css('div','margin-left','5px');
//	
//	       axeButton.css('icon','margin-left','2px');
//	       axeButton.css('icon','font-size','15px');
//	       axeButton.css('icon','vertical-align','middle');      
//	
//	  	   out += axeButton.div();	
//  	   }
       
       // -------------------------
	   // direct download buttons
	   //
       var links = node.directAccessLinks();
       
       if(links && links.length > 0){
    	     var stop = Math.min(links.length,5);
    	     insertCR = true;
	       	 for(var i=0; i < stop; i++){
	       		 
	       		 var link = links[i];
	       		 
	       		 var label = '';
	       		 if(link.indexOf('.tar.gz') > -1 || link.indexOf('.gz') > -1){
	       			 // DLR TAR GZ
	       			 label = 'TAR GZ';
	       		 }else if(link.indexOf('anonymous') > -1 || // DLR PNG
      					 ( link.indexOf('earthexplorer') > -1 && link.indexOf('sceneid') > -1)) // LANDSAT 8 PNG
      			 {
	       			 label = 'PNG';
	       		 }else if(link.indexOf('.avi') > -1 || link.indexOf('/video/') > -1){
	       			 // termocamere video
	       			 label = 'AVI Directory';
	       		 }else if(link.indexOf('rinex') > -1 || link.indexOf('gsacapi/file/search') > -1){
	       			 // GSAC rinex
	       			 label = 'RINEX Directory';               		 
	       		 }else if(link.indexOf('kml') > -1){
	       			 // MEDSUV TerraDue KML
	       			 continue;
	       		 }else if(link.indexOf('atom') > -1){
	       			 // MEDSUV TerraDue ATOM
	       			 label = 'ATOM';
	       		 }else if(link.indexOf('.N1') > -1){
	       			 // MEDSUV TerraDue N1
	       			 label = 'N1';
	       		 }else if(link.indexOf('dataselect') > -1){
	       			 // IRIS Station MiniSEED
	       			 label = 'MiniSEED';
	       		 }else if(link.indexOf('position') > -1){
	       			 // UNAVCO Position service
	       			 label = 'Position';
	       		 }else if(link.indexOf('velocity') > -1){
	       			 // UNAVCO Velocity service
	       			 label = 'Velocity';
	       		 }else if(link.indexOf('event/1/query')  > -1){
	       			 // IRIS Event
	       			 label = 'QuakeML';
	       		 }
 	       		 
	       	     var button  = GIAPI.FontAwesomeButton({   
	   			    'label':label,
	   		        'icon':'fa-external-link',
	   		        'attr':[  {'name':'link','value':link}, {'name':'title','value':'Download '+label}   ],
	   		        'handler': function(){
	   		        	
		   		       	 var link = jQuery(this).attr('link');
	            	 	 window.open(link);
	            	 	 
	   		             return false;
	   		        }
		   	     });
	       	     button.css('div','margin-left','5px');
	       	     //button.css('div','padding-right','10px');

	       	     out += button.div();	       	     
        	 } 
       };
       
       var wfslinks = node.WFS_Service();
       if(wfslinks && wfslinks.length > 0){
           
            var stop = Math.min(wfslinks.length,5);
            insertCR = true;
            for(var j=0; j < stop; j++){
                 
                var on = wfslinks[j];           
                var name = on.name;
                var url = on.url;
                var prot = on.protocol;
                
                if((url.indexOf('energic-od.eu') > -1 || url.indexOf('essi-lab.eu') > -1) && (url.indexOf('protocol=urn:ogc:serviceType:WebFeatureService') > -1)){
                                 
                    var query = url + "service=WFS&version=1.0.0&request=GetFeature&typename=" + name + "&outputFormat=application%2Fgeo%2Bjson";
                    var width = GIAPI.UI_Utils.getTextWidth(label,"normal 1.1em 'Helvetica Neue', Helvetica, Arial, sans-serif");     
                    width += 60; // for the icon
                    var button  = GIAPI.FontAwesomeButton({   
                        'width': width,
                        'label':'GeoJSON',
                        'icon':'fa-download',
                        'attr':[{'name':'link','value':query}],
                        'handler': function(){
                        
                             var link = jQuery(this).attr('link');
                             window.open(link);
                         
                             return false;
                        }
                    });
                    button.css('div','margin-left','3px');
                    out += button.div();
                }
           }   
                
       }
       
       // closes the buttons div
       out += '</div>';
       
       return out;
   };
   
   // info button and (hidden) report content
   var measurementContentDiv = function(node,measurementDivId){

        var div = '';       
        var report = node.report();     	
	   	var table = '<table class="common-ui-node-report-content-table"><tbody>';
		var content = '';
		
	    var line = GIAPI.UI_Utils.separator('margin-left:10px; display:inline-block; width:495px;vertical-align: middle');
	    	
	   	// instrument title or description
	    if(report.instrumentTitle && report.instrumentTitle.length){
	   		
		   	content += '<tr><td colspan=2>'+line+'</td></tr>';
	   		
	   		content += '<tr><td style="width: 10px;"><i style="background: white;text-align: center; width: 15px;margin-left: 10px; margin-right: 10px; border: 1px solid gray; padding: 5px" title="Measuring instruments used to acquire the data" class="fa fa-thermometer-full" aria-hidden="true"></i></td>';
	   			   		
	   		content += '<td class="common-ui-node-report-content-table-right-td">';   			
 	   		for(var i = 0; i < report.instrumentTitle.length; i++){
	   			var value = report.instrumentTitle[i];
	   			content +=  '<label class="common-ui-node-report-content-table-right">'+value+'</label></br>';
	   		}
	   		content += '</td>';   
	   		
 	   		content += '</tr>';
	   	}else if(report.instrumentDescription && report.instrumentDescription.length){
	   		
		   	content += '<tr><td colspan=2>'+line+'</td></tr>';
	   		
	   		content += '<tr><td style="width: 10px;"><i style="background: white;text-align: center; width: 15px;margin-left: 10px; margin-right: 10px; border: 1px solid gray; padding: 5px" title="Measuring instruments used to acquire the data" class="fa fa-thermometer-full" aria-hidden="true"></i></td>';
	   			   		
	   		content += '<td class="common-ui-node-report-content-table-right-td">';   			
 	   		for(var i = 0; i < report.instrumentDescription.length; i++){
	   			var value = report.instrumentDescription[i];
	   			content +=  '<label class="common-ui-node-report-content-table-right">'+value+'</label></br>';
	   		}
	   		content += '</td>';   
	   		
 	   		content += '</tr>';
	   	}
		 	
	   	// platform title or description
	    
	    if(report.platformTitle && report.platformTitle.length){
	   		
		   	content += '<tr><td colspan=2>'+line+'</td></tr>';
	   		
	   		content += '<tr><td style="width: 10px;"><i style="background: white;margin-left: 10px; margin-right: 10px; border: 1px solid gray; padding: 5px" title="Platform from which the data were taken" class="fa fa-ship" aria-hidden="true"></i></td>';
   			   		
	   		content += '<td class="common-ui-node-report-content-table-right-td">';   			
 	   		for(var i = 0; i < report.platformTitle.length; i++){
	   			var value = report.platformTitle[i];
	   			content +=  '<label class="common-ui-node-report-content-table-right">'+value+'</label></br>';
	   		}
	   		content += '</td>';   
	   		
 	   		content += '</tr>';
	   	}else if(report.platformDescription && report.platformDescription.length){
	   		
		   	content += '<tr><td colspan=2>'+line+'</td></tr>';
	   		
	   		content += '<tr><td style="width: 10px;"><i style="background: white;margin-left: 10px; margin-right: 10px; border: 1px solid gray; padding: 5px" title="Platform from which the data were taken" class="fa fa-ship" aria-hidden="true"></i></td>';
   			   		
	   		content += '<td class="common-ui-node-report-content-table-right-td">';   			
 	   		for(var i = 0; i < report.platformDescription.length; i++){
	   			var value = report.platformDescription[i];
	   			content +=  '<label class="common-ui-node-report-content-table-right">'+value+'</label></br>';
	   		}
	   		content += '</td>';   
	   		
 	   		content += '</tr>';
	   	}
	   	   	
	   	// originator organization description
	   	if(report.origOrgDesc && report.origOrgDesc.length){
	   		
		   	content += '<tr><td colspan=2>'+line+'</td></tr>';

	   		content += '<tr><td style="width: 10px;"><i style="background: white;margin-left: 10px; margin-right: 10px; border: 1px solid gray; padding: 5px" title="Organisation which created the resource" class="fa fa-users" aria-hidden="true"></i></td>';
	   			   		
	   		content += '<td class="common-ui-node-report-content-table-right-td">';   			
 	   		for(var i = 0; i < report.origOrgDesc.length; i++){
	   			var value = report.origOrgDesc[i];
	   			content +=  '<label class="common-ui-node-report-content-table-right">'+value+'</label></br>';
	   		}
	   		content += '</td>';   
	   		
 	   		content += '</tr>';
	   	}
	   		   		   	      
        // attribute title or description
	   	if(report.attributeTitle && report.attributeTitle.length){ 
	   		
		   	content += '<tr><td colspan=2>'+line+'</td></tr>';
	   		
	   		content += '<tr><td style="width: 10px;"><i style="background: white;margin-left: 10px;  margin-right: 10px; border: 1px solid gray; padding: 5px" title="Attribute described by the measurement value" class="fa fa-bar-chart" aria-hidden="true"></i></td>';
	   			   		
	   		content += '<td class="common-ui-node-report-content-table-right-td">';   			
 	   		for(var i = 0; i < report.attributeTitle.length; i++){
	   			var value = report.attributeTitle[i];
	   			content +=  '<label class="common-ui-node-report-content-table-right">'+value+'</label></br>';
	   		}
	   		content += '</td>';   
	   		
 	   		content += '</tr>';
	   	} else if(report.attributeDescription && report.attributeDescription.length){ 
	   		
		   	content += '<tr><td colspan=2>'+line+'</td></tr>';
	   		
	   		content += '<tr><td style="width: 10px;"><i style="background: white;margin-left: 10px;  margin-right: 10px; border: 1px solid gray; padding: 5px" title="Attribute described by the measurement value" class="fa fa-bar-chart" aria-hidden="true"></i></td>';
	   			   		
	   		content += '<td class="common-ui-node-report-content-table-right-td">';   			
 	   		for(var i = 0; i < report.attributeDescription.length; i++){
	   			var value = report.attributeDescription[i];
	   			content +=  '<label class="common-ui-node-report-content-table-right">'+value+'</label></br>';
	   		}
	   		content += '</td>';   
	   		
 	   		content += '</tr>';
	   	}
	   	
		if(!content){
	   		table = '<div style="margin-left: 3px;"><label style="font-size:11px; font-weight: bold">No measurement info</label></div>';
	   	}else{	   	
	   		table += content+'</tbody></table><br>';
	   	}
	   		   	
	    div = '<div class="common-ui-node-report-content-div" id="'+measurementDivId+'">'+table+'</div>';
       
        return div;
    };
   
   // info button and (hidden) report content
   var reportContentDiv = function(node,reportDivId){

        var div = '';       
        var report = node.report();     	
	   	var table = '<table class="common-ui-node-report-content-table"><tbody>';
	   	
	    var line = GIAPI.UI_Utils.separator('margin-left:3px; display:inline-block; width:100%;vertical-align: middle');
	    table += '<tr><td colspan=2>'+line+'</td></tr>';
	 
	   	// author
	   	var content = GIAPI.Common_UINode.authorRow(report);
	   	
	   	// keywords
	   	if(report.keyword && report.keyword.length){
	   		content += '<tr><td class="common-ui-node-report-content-table-left-td"><label class="common-ui-node-report-content-table-left">Keywords</td>';
	   		content += '<td class="common-ui-node-report-content-table-right-td">';
	       	for(var i=0; i<report.keyword.length; i++){
	       		content += '<label class="common-ui-node-report-content-table-right">'+report.keyword[i]+'</label><br>';	
	       	}
	       	content += '</td></tr>';
	   	}
		
	   	// format
	   	if(report.format && report.format.length){
	   		content += '<tr><td class="common-ui-node-report-content-table-left-td"><label class="common-ui-node-report-content-table-left">Formats</td>';
	   		content += '<td class="common-ui-node-report-content-table-right-td">';
	       	for(var i=0; i<report.format.length; i++){
	       		content += '<label class="common-ui-node-report-content-table-right">'+report.format[i]+'</label><br>';	
	       	}
	       	content += '</td></tr>';
	   	}
		
	   	// content
	   	if(report.content && report.content.length){
	   		content += '<tr><td class="common-ui-node-report-content-table-left-td"><label class="common-ui-node-report-content-table-left">Contents</td>';
	   		content += '<td class="common-ui-node-report-content-table-right-td">';
	       	for(var i=0; i<report.content.length; i++){
	       		content += '<label class="common-ui-node-report-content-table-right">'+report.content[i]+'</label><br>';	
	       	}
	       	content += '</td></tr>';
	   	}
	   		   	
       if(report.parentId === 'INGVIE'){
      	
	       	var grbyidUrl = 'http://med-suv.essi-lab.eu/dab/services/cswisogeo?service=CSW&request=GetRecordById&id='+report.id+'&outputschema=http://quakeml.org/xmlns/bed/1.2&elementSetName=full';
	       	if(!report.online){
	       		report.online = [];
	       	}
	       	
	       	report.online.push({
	       		url:grbyidUrl,
	       		protocol: 'HTTP_GET',
	       		description: 'Formatted QuakeML metatada'              		
	       	});                	
       }
	   	
	   	// online
       if(report.online){
      	
	   		var links = [];
	       	
	       	for(var i=0; i < report.online.length; i++){
	       		
	       		links.push(report.online[i]);
	       	}
	       	
	       	if(links.length > 0){
	       		
	       		content += '<tr><td class="common-ui-node-report-content-table-left-td"><label class="common-ui-node-report-content-table-left">Links</td>';
	       		content += '<td class="common-ui-node-report-content-table-right-td">';
	           	
	           	for(var i=0; i < links.length; i++){
	           		
	           		content += '<a target="_blank" class="dont-break-out common-ui-node-report-content-table-right" style="color:blue;text-decoration: none;" href="'+links[i].url+'">"'+links[i].url+'"</a><br>';
	               	if(links[i].protocol){
	               		content += '<label class="common-ui-node-report-content-table-right">Protocol: "'+links[i].protocol+'"</label><br>';
	               	}
	               	if(links[i].description){
	               		content += '<label class="common-ui-node-report-content-table-right">Description: "'+links[i].description+'"</label><br>';
	               	}
	               	if(i < links.length -1){
	               		content += '<br>';
	               	}
	           	}
	           	
	           	content += '</td></tr>';
	       	}
       }
       
        // vertical extent
	   	if(report.verticalExtent && report.verticalExtent.length > 0){ 
	   		
	   		content += '<tr><td class="common-ui-node-report-content-table-left-td"><label class="common-ui-node-report-content-table-left">Elevation</td>';
	   		content += '<td class="common-ui-node-report-content-table-right-td"><label class="common-ui-node-report-content-table-right">Min: '+report.verticalExtent[0].min+'</label><br>';
	   		content += '<label class="common-ui-node-report-content-table-right">Max: '+report.verticalExtent[0].max+'</label>';
	   		content += '</td></tr>';
	   	}
	   	
		// created
	   	content += GIAPI.Common_UINode.createdRow(report);	   	
	   	
	   	if(!content){
	   		table = '<div style="margin-left: 3px;"><label style="font-size:11px; font-weight: bold">No additional info</label></div>';
	   	}else{	   	
	   		table += content+'</tbody></table><br>';
	   	}
       
        div += '<div class="common-ui-node-report-content-div" id="'+reportDivId+'">'+table+'</div>';
       
        return div;
    };
    
    // ----------------------------------------------------------------
    // static hidden method which deselects all the checked collections
    // it is called by the layout when the clear button is clicked
    // 
    GIAPI.Common_UINode._deselectAll = function(){
    	
    	  jQuery('[id^="ref-check-"]').attr('check','false');
				  
		  jQuery('[id^="ref-check-"]').removeClass('fa-minus');
		  jQuery('[id^="ref-check-"]').addClass('fa-plus');
		  			  			  
		  jQuery('[id^="ref-check-"]').css('background','transparent');  
		  jQuery('[id^="ref-check-"]').css('color','black');  
    };
    
	var createCollectionCheck = function(refiner,currentNode){
		    	  
   		  var title = 'Add collection to browse';
   		  
   		  var check = '';
 	  	  var checkId = 'ref-check-'+currentNode.uiId;		  

   		  var collections = layout.browser().collections();
   		  // set the correct icon 
   		  if(collections.indexOf(currentNode.report().id) > -1 ){
   			  check += '<i title="'+title+'" id="'+checkId+'" style="background: #666; color:white" check="true" node="'+currentNode.uiId+'" class="common-ui-node-collection-check fa fa-minus" aria-hidden="true"></i>'; 
   		  }else{
   			  check += '<i title="'+title+'" id="'+checkId+'" check="false" node="'+currentNode.uiId+'" class="common-ui-node-collection-check fa fa-plus" aria-hidden="true"></i>';	  
  		  }   		  
	  	   	  
	   	  var handler = function(check,node){
	   		  
	   		  switch(jQuery(check).attr('check')){
	   		  case 'true':
	   			  // from selected to deselected
	   			  jQuery(check).attr('check','false');
	   			  	   				  
	   			  jQuery(check).removeClass('fa-minus');
	   			  jQuery(check).addClass('fa-plus');
	   			  
	   			  jQuery('#'+checkId).css('background','transparent');  
	   			  jQuery('#'+checkId).css('color','black');  
	   			  
	   			  layout.browser().update('remove',node.report().id);
	   			  	   			     			  
	   			  break;
	   		  case 'false':
	   			  // from deselected to selected
	   			  jQuery(check).attr('check','true');
	   			  
	   			  jQuery(check).addClass('fa-minus');
	   			  jQuery(check).removeClass('fa-plus');
	   			  
	   			  jQuery('#'+checkId).css('background','#666');  
	   			  jQuery('#'+checkId).css('color','white');  

	   			  layout.browser().update('add',node.report().id);

	   			  break;
	   		  }
	   	  } 
	   	  
	   	  jQuery(document).on('click','#'+checkId, function(){ handler(this, currentNode) });	                	  
	   	  jQuery(document).on('click','#div-'+checkId,  function(){ handler(this, currentNode) });	 
	   	  
	   	  return check;
	};
	        
	return uiNode;
};
	