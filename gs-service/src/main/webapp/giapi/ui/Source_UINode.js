/**
 * @module UI
 **/

/**
 *  
 * @class Source_UINode
 * @constructor
 *
 * @param {DABSource} dabNode
 * @param {String} id
 *
 */
GIAPI.Source_UINode = function(source, id) {
	
	var widget = {};

	var report = source.report();
	var where = report.where && report.where[0];
	
	var nodeMapId = GIAPI.random();
	var css = 'border: 1px solid #BDBDBD;';
	css += 'padding: 3px;';
	
	var mainDiv = '<table style="'+css+'">';
	mainDiv += '<tr>'
		
	//--------------------
	// link
	//	
	mainDiv += '<tr>';
	mainDiv += '<td colspan=2>';
	mainDiv += '<div style="margin-top:2px; font-size: 11px"><label>Service URL: </label>';
	mainDiv += '<a target="_blank" class="dont-break-out common-ui-node-report-content-table-right" style="color:blue;text-decoration: none;" href="'+report.online[0].url+'">'+report.online[0].url+'</a>';	
	mainDiv += '</td>';
	mainDiv += '</tr>';
 		
	if(report.harvested){
		
		//--------------------
		// where
		//
		if(report.where){
			var width = report.when ? '375' : '450';
			mainDiv += '<td style="height: 140px; width: '+width+'px;">'
			var mapDiv = '<div style="margin-top:3px; height: 140px; width: 100%"  id="'+nodeMapId+'"></div>';
			mainDiv += mapDiv;	
			mainDiv += '</td>'
		}	
    
		//--------------------
		// when
		//	
		if(report.when){	
			mainDiv += '<td>'
			mainDiv += '<div>'+GIAPI.Common_UINode.whenDiv('from', report)+'</div>';
			mainDiv += '<div style="margin-top: 10px">'+GIAPI.Common_UINode.whenDiv('to', report)+'</div>';
			mainDiv += '</td>'
		}	
		mainDiv += '</tr>';
		
		//--------------------
		// keywords
		//
		if(report.keyword || report.thesaurusKeyword){
			mainDiv += '<tr>';
			mainDiv += '<td colspan=2>';
			mainDiv += '<div style="margin-top:3px; margin-bottom:3px; font-size: 11px"><label>Keywords: </label></div>';
			var css = 'margin-left: -27px;';
		    css += 'max-height: 200px;';
		    css += 'font-size: 11px;';
		    css += 'overflow: auto;';
			mainDiv += '<div style="'+css+'"><ul>';
			
			if(report.thesaurusKeyword){				
				for(var i = 0; i < report.thesaurusKeyword.length; i++){
					var keyword = report.thesaurusKeyword[i].keyword;
					for(var j = 0; j < keyword.length; j++){						
						mainDiv += '<li><label>'+keyword[j]+'</label></li>';
					}
				}
			}else{
				for(var i = 0; i < report.keyword.length; i++){					
					mainDiv += '<li><label>'+report.keyword[i]+'</label></li>';
				}
			}			
			
			mainDiv += '</ul></div>';	
		}
	
	//	mainDiv += '<textarea style="font-size:11px; width: 100%">'+report.online[0].url+'</textarea>';
		mainDiv += '</td>';
		mainDiv += '</tr>';
	}
	
	mainDiv += '</table>';	
	jQuery('#'+id).append(mainDiv);
		
	widget.updateMap = function(){
		
		if(where){
			var nodeMapWidget = GIAPI.NodeMapWidget(
	       		nodeMapId, 
	       		source,
	       		{	'mapType': GIAPI.Common_UINode.mapType || GIAPI.ui.mapType || 'google',
	       			'mapTypeId': google.maps.MapTypeId.ROADMAP,
	       			'mode':GIAPI.Common_UINode.mapMode || 'al',
	       			'zoom': 9,
	       			'height': 140,
	       			'coordinatesDialogPosition': 'right'
	   			});
	     	nodeMapWidget.init();     
		};   
	};
	
	widget.div = function(){
		
		return mainDiv;
	};
	
	return widget;
	
    
};
