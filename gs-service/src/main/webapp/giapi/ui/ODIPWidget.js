/**
 * @module UI
 **/
import { GIAPI } from '../core/GIAPI.js';


GIAPI.ODIPWidget = function(dialogId, relativeId, dabNode) {

    var widget = {};
     
	var termLabelStyle = '.termLabel{ font-size: 80%}';
	termLabelStyle += '.termLabel:hover{  font-weight: bold; padding-top:4px; padding-bottom: 4px; background-color: #97b0d9; cursor:pointer}';
	
	GIAPI.UI_Utils.appendStyle(termLabelStyle);
    
    var profilesDiv = '<input id="suggestionInput" style="width: 397px; margin-left: -4px" placeholder="Suggestion" type="text">';
    	   	
    profilesDiv +=	'<div style="margin-left: -8px;border: none;background: transparent;" id="profile-tab">';   
    profilesDiv +=     '<ul style="width:394px">';
    profilesDiv +=        '<li style="margin-left:100px"><a style="background: transparent" href="#cdi-tab">CDI</a></li>';
    profilesDiv +=        '<li><a style="border: transparent" href="#mcp-tab">MCP</a></li>';
    profilesDiv +=        '<li><a  style="background: transparent" href="#nodc-tab">NODC</a></li>';
    profilesDiv +=	   '</ul>';		    						               
    profilesDiv +=	   '<div class="tabs-element" id="cdi-tab">';                 
    profilesDiv +=	   '</div>';		    
    profilesDiv +=	    '<div class="tabs-element" id="mcp-tab">';
    profilesDiv +=	    '</div>';		                  	                 
    profilesDiv +=	    '<div class="tabs-element" id="nodc-tab">';	                    
    profilesDiv +=	    '</div>';        
    profilesDiv += '</div>';                       
          
    var pos = GIAPI.position('bottom');
    pos.at = pos.at+'+5';

    jQuery("#"+dialogId).append(profilesDiv);  
    
    jQuery('#suggestionInput').keyup(function(){
    	
    	var active = jQuery('#profile-tab').tabs('option', 'active');
    	var target = null;
    	switch(active){
	    	case 0: target = 'CDI'; break;
	    	case 1: target = 'MCP'; break;
	    	case 2: target = 'NODC'; break;
    	}
    	
    	var suggestion = jQuery('#suggestionInput').val();
    	updateLabels(null, 'labelsDiv', target, null, suggestion); 
    });
     
 	jQuery('#profile-tab').tabs({
		  activate: function( event, ui ) {   
			  
			  jQuery('#suggestionInput').val('');
			  
			  switch(ui.newPanel.selector){
			  case '#cdi-tab':
				   updateLabels(null,'labelsDiv','CDI',null);
		           break;
			  case '#nodc-tab':
	               updateLabels(null,'labelsDiv','NODC',null);
	               break;
			  case '#mcp-tab':
				  updateLabels(null,'labelsDiv','MCP',null);
	           	  break;
			  }    		            			 
		  }
	});

    jQuery('#'+dialogId).dialog({
	   autoOpen: false,
       height : 330,
       width : 425,
       resizable: false,
       modal : false,
       position : {
           of : '#'+relativeId,
           my : pos.my,
           at : pos.at,
           collision : 'none'
       }
    });

    widget.showDialog = function(textFieldId, target){
    	
    	widget.target = target;
    	widget.textFieldId = textFieldId;
    	
    	var title = '';
    	switch(target){
    	case 'INSTRUMENTS':
    		title = 'INSTRUMENTS';
    		break;
    	case 'PLATFORMS':
    		title = 'PLATFORMS';
    		break;
    	case 'ORIG_ORGANIZATIONS':
    		title = 'ORIGINATOR ORGANISATIONS';
    		break;
    	case 'PARAMETERS':
    		title = 'MEASURED ATTRIBUTE';
    		break;
    	}
         
        jQuery('#'+dialogId).dialog('option','title',title);
        
        if(!jQuery('#'+dialogId).dialog('isOpen')){
        	jQuery('#'+dialogId).dialog('open');
        }
        
        jQuery('#profile-tab').tabs({ active: 0 });

        updateLabels(textFieldId,'labelsDiv','CDI',target);
    };
    
    var updateLabels = function(textFieldId,divIdentifier, profile, target, suggestion){
    	
    	if(!target){
    		target = widget.target;
    	}
    	
    	if(!textFieldId){
    		textFieldId = widget.textFieldId;
    	}
    	
    	if(!suggestion){
    		suggestion = '';
    	}
    	
    	var endpoint = dabNode.endpoint().endsWith('/') ? dabNode.endpoint() : dabNode.endpoint() + '/';
         
        endpoint = endpoint +'services/support/odip?profile='+profile+'&target='+target+'&suggestion='+suggestion;
        	
        jQuery.ajax({

                type : 'GET',
                url : endpoint,
                crossDomain : true,
                dataType : 'jsonp',

                success : function(data, status, jqXHR) {

                	console.log('updatelabels: '+ data);  

                	jQuery('#'+divIdentifier).remove();
                	
                    var labelsDiv = '<div style="height:200px; overflow: auto" id="labelsDiv">';

                	var labels = data.labels;
                	if(labels.length === 0){
                		labelsDiv += '<label style="font-size: 80%">No labels found</label><br>';
	                	
                	}else{               	
	                	for(var i = 0; i < labels.length; i++){
	                		var id = GIAPI.random();
	                		labelsDiv += '<label id="'+id+'" class="termLabel">'+labels[i]+'</label><br>';
	                    	jQuery(document).on('click','#'+id, function(){
	                    		
	                    		setTerm(textFieldId,this, profile, target);
	                    	});
	                	}    
	                	labelsDiv += '</div>';
                	}
                	
                	jQuery('#'+dialogId).append(labelsDiv);
                },

                complete : function(jqXHR, status) {
                },

                error : function(jqXHR, msg, exception) {
                }
        });	
    }
      
    var setTerm = function(textFieldId,label, profile, target){
    	
    	console.log('START setTerm');  
  	
    	var labelId = label.id;
    	var label = jQuery('#'+labelId).text();
    	label = encodeURIComponent(label);
    	
    	var endpoint = dabNode.endpoint().endsWith('/') ? dabNode.endpoint() : dabNode.endpoint() + '/';
       
    	endpoint = endpoint +'services/support/odip?profile='+profile+'&target='+target+'&label='+label;

        jQuery.ajax({

            type : 'GET',
            url : endpoint,
            crossDomain : true,
            dataType : 'jsonp',

            success : function(data, status, jqXHR) {

            	 console.log('END setTerm: '+ data);  

            	 jQuery('#'+textFieldId).val(data.term);
            },

            complete : function(jqXHR, status) {
            },

            error : function(jqXHR, msg, exception) {
            }
        });	
    } 
    
    return widget;

};