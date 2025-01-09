
import { GIAPI } from '../core/GIAPI.js';

GIAPI.DownloadWidget = function(options) {
	
	var widget = {};
	
	var endpoint = GIAPI.Common_UINode.dabNode.endpoint();
	if(!endpoint.endsWith('/')){
		 endpoint += '/';
	}
	
	var contentOptions;	
    var formatSelectId = GIAPI.random();    
    var formatTdId = GIAPI.random(); 
    var gridCRSSelectId = GIAPI.random();
    var gridCRSTdId = GIAPI.random();     
    var firstResLabelId = GIAPI.random();
    var firstResSelectId = GIAPI.random();
    var firstResTdId = GIAPI.random();
    var secondResLabelId = GIAPI.random();
    var secondResSelectId = GIAPI.random();     
    var secondResTdId = GIAPI.random();
    var rightTableId = GIAPI.random();
    var whereDivId = GIAPI.random();
    var downButtonTdId = GIAPI.random();
    var infoLabelId = GIAPI.random();
    
    //-------------------------------------
    // appends a temporary div
    //
    var tmpDivLabelId = GIAPI.random();
    var tmpDiv = '<div style="margin-left: 3px"><label id="'+tmpDivLabelId+'" style="font-size:11px; font-weight: bold;height:50px">Loading data, please wait...</label></div>';
    jQuery('#'+options.divId).append(tmpDiv);


	//-------------------------------------
    // executes the content options request 
    //  
    if(! options.node.isAccessible()){
    	 jQuery('#'+tmpDivLabelId).text('No download options available');
		 return;
	}
    options.node.accessOptions(function(options,error){
    	 
    	 if(!options){
    		 jQuery('#'+tmpDivLabelId).text('No download options available');
    	 }else{
        	 update(options);	  
    	 }
    });
    
	//----------------------------------------------------------
    // set the change listener for the format and grid CRS input 
    //
	jQuery(document).on('change','#' + formatSelectId, function() {
	    	
	    var selectedFormat = this.value;	
	    var avOptions = contentOptions.availableOptions;
	    var compliantOptions = findCompliantOptions(avOptions,'format',selectedFormat);
	    
	    var gridCRSSet = createSet('gridCRS',compliantOptions);
	    	    	    	    
	    jQuery('#'+gridCRSSelectId).empty();
	    var sel = true;
	    for(var gridCRS in gridCRSSet){
			var selected = sel ? ' selected' : '';
			sel = false;
			jQuery('#'+gridCRSSelectId).append('<option value="'+gridCRS+'" '+selected+'>'+gridCRS+'</option>');
		}
	    
 		setNotAvailbleOption('format', !jQuery('#'+gridCRSSelectId+' option').length);
	        
	});
    
	//-----------------------------------------------
    // set the change listener for the grid CRS input 
    //
	jQuery(document).on('change','#' + gridCRSSelectId, function() {
    	
	 	var selectedCRS = this.value;	
	 	
	    var avOptions = contentOptions.availableOptions;
	    var compliantOptions = findCompliantOptions(avOptions,'gridCRS',selectedCRS);
	    	    
		var formatSet = createSet('format',compliantOptions);
			    
	    jQuery('#'+formatSelectId).empty();
	    var sel = true;
	    for(var format in formatSet){
			var selected = sel ? ' selected' : '';
			sel = false;
			jQuery('#'+formatSelectId).append('<option value="'+format+'" '+selected+'>'+format+'</option>');
		}
	    
		setNotAvailbleOption('gridCRS', !jQuery('#'+formatSelectId+' option').length);
	    
    });
	
	//-----------------------------------------------------------------------
	// find the options compliant with the selected value (format or grid CRS
    // and with the EPSG:4326 bbox CRS
	//
	var findCompliantOptions = function(avOptions, target, selectedValue){
		
		var compliantOptions = [];
	    for( var i = 0; i < avOptions.length; i++){
	    	
	    	var option = avOptions[i];    	
	    	var subsetCRS = option.spatialSubset.CRS;	    	
	    	
	    	var format = option.format;
	    	var gridCRS = option.CRS;
	    	
	    	var value = target === 'format' ? format : gridCRS;

	    	if(value === selectedValue && subsetCRS === 'EPSG:4326'){
	    		compliantOptions.push(option);
	    	}
	    }
	    
	    return compliantOptions;
	};
	
	//----------------------------------------------------------------
	// creates a set of formats or grid CRS from the compliant options
	//
	var createSet = function(target, compliantOptions){
		
		var set = {};
	    for(var i = 0; i < compliantOptions.length; i++){
	    	
	    	var option = compliantOptions[i];	   
	    	
	    	var format = option.format;
	    	var gridCRS = option.CRS;	    	
	    	
	    	var value = target === 'format' ? format : gridCRS;

	    	set[value] = value;
	    }
	    
	    return set;
	};
	
	var setNotAvailbleOption = function(target, set){
		
		if(set){
			
			GIAPI.UI_Utils.setGlassPane(firstResTdId);
			GIAPI.UI_Utils.setGlassPane(secondResTdId);			
			GIAPI.UI_Utils.setGlassPane(rightTableId);

			if(target === 'format'){
				GIAPI.UI_Utils.setGlassPane(gridCRSTdId);
			}else{
				GIAPI.UI_Utils.setGlassPane(formatTdId);
			}
			
			GIAPI.UI_Utils.setGlassPane(downButtonTdId);
			
			if(target === 'gridCRS'){
				target = 'CRS';
			}
			jQuery('#'+infoLabelId).text('The selected option is not availble. Please select another '+target);

		}else{
			
			GIAPI.UI_Utils.removeGlassPane(firstResTdId);
			GIAPI.UI_Utils.removeGlassPane(secondResTdId);			
			GIAPI.UI_Utils.removeGlassPane(rightTableId); 			
			GIAPI.UI_Utils.removeGlassPane(gridCRSTdId);
			GIAPI.UI_Utils.removeGlassPane(formatTdId);
			GIAPI.UI_Utils.removeGlassPane(downButtonTdId);
			
			jQuery('#'+infoLabelId).text('');
		}
	};
    
	//----------------------------------------------------------
    // updates the fields values
    //
	var update = function(data){
		
		contentOptions = data;
		
		//----------------------------------
	    // Format
	    //
	    var leftTable = '<table style="width: 200px">';
	    
	    var formatLabel = '<label class="common-ui-node-download-widget-label">Format</label>';
	    var formatSelect = '<select style="width: 198px; margin-left:3px; height: 18px" class="common-ui-node-download-widget-input"  id="'+formatSelectId+'"></select>';
	    
	    leftTable += '<tr><td id="'+formatTdId+'">'+formatLabel+formatSelect+'</td></tr>';
	      
	    //----------------------------------
	    // Grid info
	    //
	    var gridInfoLabel = '<label style="margin-top:3px" class="common-ui-node-download-widget-header">Grid info</label>';
	    leftTable += '<tr><td><div style="margin-top:5px" >'+gridInfoLabel+'</div></td></tr>';
	    
	    var gridCRSLabel = '<label class="common-ui-node-download-widget-label">CRS</label>';
	    var gridCRSSelect = '<select class="common-ui-node-download-widget-input" style="margin-left:3px;width: 210px; height: 18px" id="'+gridCRSSelectId+'"></select>';
	    
	    leftTable += '<tr><td id="'+gridCRSTdId+'">'+gridCRSLabel+gridCRSSelect+'</td></tr>';
	    
	    var firstResLabel = '<label id="'+firstResLabelId+'" class="common-ui-node-download-widget-label"></label>';
	    var firstResInput = '<input class="cnst-widget-input common-ui-node-download-widget-input" style="margin-left:-3px;width:100%;" type="number" id="'+firstResSelectId+'"></input>';
	   
	    var labelDiv = '<div style="display: table-cell;width:auto">'+firstResLabel+'</div>';
	    var inputDiv = '<div style="display: table-cell;width:60%">'+firstResInput+'</div>';
	    var firstDiv = '<div style="display:table; width: 100%">'+labelDiv+inputDiv+'</div>';

	    leftTable += '<tr><td id="'+firstResTdId+'">'+firstDiv+'</td></tr>';
	    
	    var secondResLabel = '<label id="'+secondResLabelId+'" class="common-ui-node-download-widget-label"></label>';
	    var secondResInput = '<input class="cnst-widget-input common-ui-node-download-widget-input" style="margin-left:-3px;width: 100%" type="number" id="'+secondResSelectId+'"></input>';
	    
	    var labelDiv = '<div style="display: table-cell;width:auto">'+secondResLabel+'</div>';
	    var inputDiv = '<div style="display: table-cell;width:60%">'+secondResInput+'</div>';
	    var secondDiv = '<div style="display:table; width: 100%">'+labelDiv+inputDiv+'</div>';
	    
	    leftTable += '<tr><td id="'+secondResTdId+'">'+secondDiv+'</td></tr>';
	    leftTable += '</table>';
	    
	    //----------------------------------
	    // Spatial and temporal subset
	    //
	    var rightTable = '<table id="'+rightTableId+'" style="width: 230px">';
	    
	    var subsetLabel = '<label class="common-ui-node-download-widget-header">Spatial subset</label>';
	    rightTable += '<tr><td><div style="margin-top: -3px;" >'+subsetLabel+'</div></td></tr>';
	     
//	    var subsetCRSLabel = '<label class="common-ui-node-download-widget-label">CRS</label>';
//	    var subsetCRSSelectId = GIAPI.random();
//	    var subsetCRSSelect = '<select class="common-ui-node-download-widget-input" style="margin-left:3px;width: 185px; height: 18px" id="'+subsetCRSSelectId+'"></select>';
//	    rightTable += '<tr><td><div>'+subsetCRSLabel+subsetCRSSelect+'</div></td></tr>';
//	        
	    var constWidget = GIAPI.ConstraintsWidget(options.dabNode, {
			'fieldsWidth': 172             	
	    });
	            
 	    var bboxLabel = '<label class="common-ui-node-download-widget-label">Bbox</label>';

	    rightTable += '<tr><td><div style="margin-top:-1px"><div style="display:inline-block">'+bboxLabel+'</div><div id="'+whereDivId+'" style="vertical-align: middle;display:inline-block"></div></div></td></tr>';

	    var tempSubLabel = '<label class="common-ui-node-download-widget-header">Temporal subset</label>';
	    rightTable += '<tr><td><div style="margin-top: -3px;">'+tempSubLabel+'</div></td></tr>';
	    
	    // finds the min and the max time
		var availOptions = contentOptions.availableOptions;
		var minTemp = '3000-01-01T00:00:00Z';
		var maxTemp = '0000-01-01T00:00:00Z';
        for(var index in availOptions){
        	var option = availOptions[index];
        	var temporalBegin = option.temporalSubset && option.temporalSubset.from;
        	if(temporalBegin){
        		temporalBegin = temporalBegin.substring(0,temporalBegin.indexOf('T'));
        	}
        	var temporalEnd = option.temporalSubset && option.temporalSubset.to;
        	if(temporalEnd){
        		temporalEnd = temporalEnd.substring(0,temporalEnd.indexOf('T'));
        	}
        	if(temporalBegin < minTemp){
        		minTemp = temporalBegin;
        	}
        	if(temporalEnd > maxTemp){
        		maxTemp = temporalEnd;
        	}
        }
        
        var minDate = new Date(minTemp);
        var maxDate = new Date(maxTemp);
      
//        console.log(JSON.stringify(availOptions));
//        console.log(minDate);
//        console.log(maxDate);

	    var from = constWidget.whenConstraint('get','from',{
	    			showHelpIcon: false,
	    			minDate: minDate,
	    			maxDate: maxDate
	    		}
	    );
	    var labelDiv = '<div style="vertical-align: middle;display: table-cell;width:auto"><label class="common-ui-node-download-widget-label">Start</label></div>';
	    var inputDiv = '<div style="display: table-cell;width:90%">'+from+'</div>';
	    var fromDiv = '<div style="margin-top: -5px;display:table; width: 100%">'+labelDiv+inputDiv+'</div>';
	    rightTable += '<tr><td>'+fromDiv+'</td></tr>';
	    
	    var to = constWidget.whenConstraint('get','to',{
    				showHelpIcon: false,
    				minDate: minDate,
	    			maxDate: maxDate
    			}
	    );
	    var labelDiv = '<div style="vertical-align: middle;display: table-cell;width:auto"><label class="common-ui-node-download-widget-label">End</label></div>';
	    var inputDiv = '<div style="display: table-cell;width:90%">'+to+'</div>';
	    var toDiv = '<div style="margin-top:-12px;display:table; width: 100%">'+labelDiv+inputDiv+'</div>';
	    rightTable += '<tr><td>'+toDiv+'</td></tr>';
	       
	    rightTable += '</table>';
	    
	    //-------------------------------------------------
	    // Main table with download button and error message
	    // 
	    var mainTable = '<table>';        
	    mainTable += '<tr><td>'+leftTable+'</td><td>'+rightTable+'</td></tr>';
	    
	    var downloadButton  = GIAPI.FontAwesomeButton({   
		    'width': 120,
		    'label':'Start download',
	        'icon':'fa-download',
	        'handler': function(){
	        	 
	        	 var option = {};
	        	 
	        	 option.format = jQuery('#'+formatSelectId).val();
	        	 option.CRS = jQuery('#'+gridCRSSelectId).val();
	        	 
	        	 if(jQuery('#'+firstResSelectId).val() && jQuery('#'+secondResSelectId).val()){
	        		 option.firstAxisSize = {value: parseFloat(jQuery('#'+firstResSelectId).val())};
	        		 option.secondAxisSize = {value: parseFloat(jQuery('#'+secondResSelectId).val())};
	        	 }
	        	 
	        	 if(constWidget.constraints().where){
	        		 option.spatialSubset = constWidget.constraints().where;
	        	 }
	        	 
	        	 var temporalBegin = constWidget.constraints().when && constWidget.constraints().when.from;
	        	 var temporalEnd = constWidget.constraints().when && constWidget.constraints().when.to;	        
	        	 if(temporalBegin && temporalEnd){
	        		 option.temporalSubset = {from: temporalBegin, to: temporalEnd};
	        	 }
	        	 
	        	 options.node.accessLink(function(link, error){	        		 
	        		 if(!error){
	    	        	 window.open(link);
	        		 }
	        		 
	        	 },option);
	       	 	 
	             return false;
	        }
		});
	    
	    downloadButton.css('div','margin-left','3px');
	    downloadButton.css('icon','vertical-align','middle');
	    downloadButton.css('div','font-size','12px');
	    downloadButton.css('div','margin-top','5px');
	    
	    var infoDiv = '<div style="margin-top:13px; margin-left:-105px"><label class="common-ui-node-download-widget-label" style="font-weight: bold;color:red;" id="'+infoLabelId+'"></label></div>';
	    mainTable += '<tr><td id="'+downButtonTdId+'">'+downloadButton.div()+'</td><td>'+infoDiv+'</td></tr>';
	    mainTable += '</table>';
	    
	    //----------------------------
	    // Widget div
	    //
	    var widgetDiv = '<div>';	  
	    var line = GIAPI.UI_Utils.separator('display:inline-block; width:100%;vertical-align: middle');
	    widgetDiv += line;
	    widgetDiv += mainTable;
	    widgetDiv += '</div>';
	    
	    //----------------------------
	    // Appends the updated div
	    // 
	    jQuery('#'+options.divId).empty();
	    jQuery('#'+options.divId).append(widgetDiv);
	    
	    //---------------------------------
	    // changes some ContraintWidget css
	    //
		jQuery('#'+constWidget.getId('from')).addClass('common-ui-node-download-widget-input');
		jQuery('#'+constWidget.getId('to')).addClass('common-ui-node-download-widget-input');	 
		jQuery('#'+constWidget.getId('from')).parent('table').css('display','inline-block');
		jQuery('#'+constWidget.getId('to')).parent('table').css('display','inline-block');
		jQuery('#'+constWidget.getId('from')).parent('div').parent('td').css('width','97px');
		jQuery('#'+constWidget.getId('to')).parent('div').parent('td').css('width','97px');
		jQuery('#'+constWidget.getId('from')).css('width','97px');
		jQuery('#'+constWidget.getId('to')).css('width','97px');	
		jQuery('#'+constWidget.getId('from')).css('height','15px');
		jQuery('#'+constWidget.getId('to')).css('height','15px'); 
	    			
		//----------------------------------------------------------------------
		// set the resolution, time and bbox init values from the reduced option
		//				
		var reducedOption = contentOptions.reducedOption;
		
		var firstSize = reducedOption.firstAxisSize && reducedOption.firstAxisSize.value
		var secondSize = reducedOption.secondAxisSize && reducedOption.secondAxisSize.value

		if(firstSize && secondSize){
			jQuery('#'+firstResSelectId).val(firstSize);
			jQuery('#'+secondResSelectId).val(secondSize);
			
			jQuery('#'+firstResLabelId).text(reducedOption.firstAxisSize.label+' resolution');
			jQuery('#'+secondResLabelId).text(reducedOption.secondAxisSize.label+' resolution');
		}else{
			jQuery('#'+firstResSelectId).remove();
			jQuery('#'+secondResSelectId).remove();
		}
		
		var tempSubset = reducedOption.temporalSubset;
		var fromVal = tempSubset && tempSubset.from && tempSubset.from.substring(0,tempSubset.from.indexOf('T'));
		var toVal = tempSubset && tempSubset.to && tempSubset.to.substring(0,tempSubset.to.indexOf('T'));

		if(fromVal){
			jQuery('#'+constWidget.getId('from')).val(fromVal);
		}else{
			jQuery('#'+constWidget.getId('from')).attr('disabled','disabled');
		}
		if(toVal){
			jQuery('#'+constWidget.getId('to')).val(toVal);
		}else{
			jQuery('#'+constWidget.getId('to')).attr('disabled','disabled');
		}	
		
		// creates now the where widget in order to initializes it with reducedOption.spatialSubset		
		if(reducedOption.spatialSubset){
			delete reducedOption.spatialSubset.CRS;
		}
		var where = constWidget.whereConstraint('get',{
	    	widgetPosition: 'right',
	    	'showHelpIcon':false,
	    	'value': reducedOption.spatialSubset,
	    	'applyValue': reducedOption.spatialSubset ? true: false,
	    	'showSpatialRelationControl': false
	    });
		
		jQuery('#'+whereDivId).append(where);
		
		jQuery('#'+constWidget.getId('where')).addClass('common-ui-node-download-widget-input');
	    jQuery('#'+constWidget.getId('where')).css('height','15px');
	    
	    if(!reducedOption.spatialSubset){
	    	jQuery('#'+constWidget.getId('where')).attr('disabled','disabled');
	    }

		//--------------------------------------------------------
		// fill all the remaining values from the available option
		// and select the values from the reduced option
		//		
		var formatMap = {};
		var gridCRSMap = {};
		var subsetCRSMap = {};

		for(var i=0; i < availOptions.length; i++){
			
			var opt = availOptions[i];
			
 			formatMap[opt.format] = opt.format;
 			gridCRSMap[opt.CRS] = opt.CRS;
 			subsetCRSMap[opt.CRS] = opt.subsetCRS;
 		}
		
		for(var format in formatMap){
			var selected = reducedOption.format === format ? ' selected' : '';
			jQuery('#'+formatSelectId).append('<option value="'+format+'" '+selected+'>'+format+'</option>');
		}
		
		for(var gridCRS in gridCRSMap){
			var selected = reducedOption.CRS === gridCRS ? ' selected' : '';
			jQuery('#'+gridCRSSelectId).append('<option value="'+gridCRS+'" '+selected+'>'+gridCRS+'</option>');
		}
		
//		for(var subsetCRS in subsetCRSMap){
//			var selected = reducedOption.subsetCRS === subsetCRS ? ' selected' : '';
//			jQuery('#'+subsetCRSSelectId).append('<option value="'+subsetCRS+'" '+selected+'>'+subsetCRS+'</option>');
//		}
		
	};
    	
	return widget;
};
