/**
 * @module UI
 **/


 /**
  *  This class extends <code>{{#crossLink "UINode"}}{{/crossLink}}</code> in order to provide a specific graphical representation of 
  *  <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> representing <i>events</i> originating from {{#crossLink "DABSource"}}sources{{/crossLink}} 
  *  of tyle <code>IRIS_EVENT</code>.
  *  The following CSS is required:<pre><code>
 &lt;!-- API CSS --&gt; 
 &lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
</code></pre>

<img style="width: 1000px;border: none;" src="../assets/img/iris-event-ui-node.png" /><br>
<i>The image above shows a <code>IRISEvent_UINode</code></i>

The code snippet below shows how to register this class to the <code>{{#crossLink "ResultSetLayout"}}{{/crossLink}}</code>:
	
<pre><code>
var resultSetLayout = GIAPI.ResultSetLayout(id,{
	 // registers this class to the layout
    	'uiNodes': [GIAPI.IRISEvent_UINode],
 
     	// ...other default ResultSetLayout properties 
    });
</code></pre>

  *
  *  @constructor 
  *  @class IRISEvent_UINode
  *  @extends Common_UINode
  */
GIAPI.IrisEvent_UINode = function(options) {
		
	var uiNode = GIAPI.Common_UINode(options);	 	
 				
	options.sectionDom = function(node,options,sectionId){
        
		var report = node.report();
		var id = report.id;
		var type = report.type;
        var title = report.title;
        var desc = report.description === 'none' || !report.description ? 'No descripion available':report.description;
        var ovr = report.overview;
        var type = report.type;
        var keyword = report.keyword;
        
        var rowDivId = GIAPI.random();
        var datasetDivId = GIAPI.random();

		var sectionDivId = 'section-div-'+node.uiId;
        var div = '<div id="'+sectionDivId+'" class="common-ui-node-report-div">';
       
        id = id.length > 100 ? (id.substr(0,100)+'...') : id;
        
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
        
        // inserts the event table
        div += createEventTable(report.QuakeML); 
 
        // inserts the quake ml button     
        if(node.directAccessLinks().length > 0){
			 var link = node.directAccessLinks()[0].replace('quakeml:','http://');		 
	   		 var label = 'FULL QuakeML';
	   			   	 	 
	   	 	 var width = GIAPI.UI_Utils.getTextWidth(label,"normal 1.1em 'Helvetica Neue', Helvetica, Arial, sans-serif");	  
    		 width += 60; // for the icon
    	     var button  = GIAPI.FontAwesomeButton({   
			    'width': width,
			    'label':label,
		        'icon':'fa-external-link',
		        'attr':[{'name':'link','value':link}],
		        'handler': function(){
		        	
	   		       	 var link = jQuery(this).attr('link');
	   		       	 window.open(link);         	 	 
		             return false;
		        }
	   	     });
    	     button.css('div','margin-top','10px');
	   	 	 
	   	 	 div += button.div();
        }
          
        return div;        
    };
        
	/**
	 * Return <code>true</code> if this <code>{{#crossLink "GINode"}}{{/crossLink}}</code> 
	 * {{#crossLink "Report}}report{{/crossLink}} has the "QuakeML" extension property
	 * 
	 * @method isRenderable
	 * @return <code>true</code> if this <code><a href="../classes/GINode.html">GINode</a></code> 
	 * {{#crossLink "Report}}report{{/crossLink}} has the "QuakeML" extension property
	 */
	uiNode.isRenderable = function(node){
		
		return node.report().QuakeML ? true : false;
	};
	 
    // -----------------------------------------------------------
    // private methods -------------------------------------------
    // -----------------------------------------------------------
	          
    var createEventTable = function(quakeML){
    	
     	var table = '<table class="classification-table" id="occTable">';
     	
     	var title = GIAPI.formatWords(quakeML.type);
     	var region = GIAPI.formatWords(quakeML.region);
     	if(region === 'Unknown'){
     		region = 'Unknown Region';
     	}
     	title += ' localized in '+region;
     	
		table += '<tr><th colspan="5">'+title+'</th></tr>';
  		table += '<tr><td colspan="5">'+GIAPI.UI_Utils.separator(' width:100%; vertical-align: top;')+'</td></tr>';
  		
  		//
		// overview
		//
  		table += '<tr><th>Magnitude value</th><th>Depth</th><th>Lat</th><th>Lon</th><th>Time</th></tr>';
				
		// 0: Magnitude value
		table += '<tr>';
		table += '<td style="font-size:90%;">'+normalize(quakeML.magnValue)+'</td>';
		
		// 1: Depth
		table += '<td style="font-size:90%;">'+normalize(quakeML.depth)+'</td>';
		
		// 2: Lat
		table += '<td style="font-size:90%;">'+normalize(quakeML.lat)+'</td>';
		
		// 3: Lon
		table += '<td style="font-size:90%;">'+normalize(quakeML.lon)+'</td>';

		// 4: Time
		table += '<td style="font-size:90%;">'+normalize(quakeML.time)+'</td>';
					
		table += '<tr><td colspan="5"></td></tr>';
		table += '<tr><td colspan="5"></td></tr>';

		//
		// other info
		//
		table += '<tr><th>Region</th><th>Magnitude type</th><th>Event type</th><th>Author</th><th>Contributor</th></tr>';

		table += '<tr>';

		// 5: Region
		table += '<td style="font-size:90%;">'+normalize(region)+'</td>';
		
		// 6: Magnitude type
		table += '<td style="font-size:90%;">'+normalize(GIAPI.formatWords(quakeML.magnType))+'</td>';
		
		// 7: Event type
		table += '<td style="font-size:90%;">'+normalize(GIAPI.formatWords(quakeML.type))+'</td>';
						
		// 8: Author
		table += '<td style="font-size:90%;">'+normalize(GIAPI.formatWords(quakeML.author))+'</td>';
		
		// 9: Contributor
		table += '<td style="font-size:90%;">'+normalize(GIAPI.formatWords(quakeML.contributor))+'</td>';
		
	  	table += '</tr></table>';
    	return table;
    };
    
    var normalize = function(string){
    	
    	return (''+string).toLowerCase() === 'unknown' ? '-' : string;
    }
	      
	return uiNode;
};
	