/**
 * @module UI
 **/
import { GIAPI } from '../core/GIAPI.js';

/**
 * Collection of static UI utility methods.<br>
 * The following CSS is required:<pre><code>&lt;!-- API CSS --&gt;
 &lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
</code></pre>
 * 
 * @class UI_Utils
 */
GIAPI.UI_Utils = new function(){
	
	var obj = {};
	var progressBar;
	var progressLabel;
	
	obj.glassPaneMap = {};
		
	/**
	 * Appends the given CSS to the &lt;head&gt; in a &lt;style&gt; element having the default identifier 'api-injected-css'
	 * 
	 * @param {String} css the CSS to append
	 * @param {String} [id='api-injected-css'] id of the ;&ltstyle&gt element
	 */
	obj.appendStyle = function(css,id){
		
		if(!id){
			id = 'api-injected-file';
		}
    	
    	if(jQuery('#'+id).length === 0){
    		jQuery('head').append('<style type="text/css" id="'+id+'"/>');
    	}
    	
    	jQuery('#'+id).append(css);
    };
	
	if(jQuery('#dialog').length === 0){
		
 	    var discDialogDiv = '<div id="discoverDialog">';
		
	    discDialogDiv += '<div id="progressBar"></div>';

 	    discDialogDiv += '<div><label id="status-label" style="font-weight: bold;font-size:11px">Searching...</label></div>'; 		   
		discDialogDiv += '</div>'; 		   

		jQuery('head').append(discDialogDiv);		
 	    jQuery('head').append('<div id="dialog" ></div>');
 		jQuery('head').append('<div id="overviewDialog" title="Overview"><div id="overviewDialogImg"></div></div>');
 		
	    jQuery(document).on('dblclick','#overviewDialog',function(){
        	
        	jQuery('#overviewDialog-maximize-button').click();
        	return false;
        });
 	    
 	    jQuery(function(){
 	     	
 	    	// ----
 	    	// Defines the maximizeButton function for the jQuery UI dialog
 	    	// ----	    		
    		var css = '.maximize-button{'+
    		     'cursor:pointer;'+
    			 'position: absolute;'+
    			 'right: 26px;'+
    			 'top: 50%;'+
    			 'width: 20px;'+
    			 'margin: -10px 0 0 0;'+
    			 'padding: 1px;'+
    			 'height: 20px; '+
    			 'border:none;'+
    			 'background:url("https://api.geodab.eu/docs/assets/img/maximize.png") no-repeat center center;'+
    		 '}'+
    		
    		 '.maximize-button:active{'+	
    			 'background:url("https://api.geodab.eu/docs/assets/img/maximize-active.png") no-repeat center center;'+
    		 '}'+
    		
    		 '.restore-button{'+
    		    		
    		obj.appendStyle(css);
    			 'background:url("https://api.geodab.eu/docs/assets/img/restore.png") no-repeat center center;'+
    		 '}'+
    		
    		 '.restore-button:active{'+
    			 'background:url("https://api.geodab.eu/docs/assets/img/restore-active.png") no-repeat center center;'+
    		 '}';
    		
    		jQuery.fn.maximizeButton = function(set, handler){
    					
    			var id = jQuery(this).attr('id');
    			var buttonId = id+'-maximize-button';
    			var div = jQuery('[aria-describedby="'+id+'"] div[class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix ui-draggable-handle"]');
    			
    			jQuery('#'+id).attr('maximized','no');
    			
    			if(set){
    				
    				jQuery('#'+buttonId).remove();           			
    				div.append('<button id="'+buttonId+'" class="maximize-button"/>');     
    				
    				jQuery('#'+buttonId).click(function(){
    					           				
    					var maximized = jQuery('#'+id).attr('maximized') === 'yes';
    					var dim = null;

    					if(maximized){
    						
    						jQuery('#'+id).attr('maximized','no');
    						jQuery('#'+buttonId).removeClass('restore-button');
    						
    						dim = [jQuery('#'+id).attr('w'),jQuery('#'+id).attr('h')];          					
    						
    					}else{
    						
    						jQuery('#'+id).attr('maximized','yes');  					
    						jQuery('#'+buttonId).addClass('restore-button');

    						dim = handler.apply(jQuery('#'+id),[jQuery('#'+id)]);
    	 				}	

    	   				
    					var maxw = 	handler.apply(jQuery('#'+id),[jQuery('#'+id)])[0];
    					var maxh = 	handler.apply(jQuery('#'+id),[jQuery('#'+id)])[1];

    					jQuery('#'+id).dialog({
    	                    width : dim[0],
    	                    height : dim[1],
    	                    maxWidth: maxw,
    	                    maxHeight: maxh                  
    	                });
    					
    					return false;
    				});
    	 		}else{
    	 			jQuery('#'+buttonId).remove();
    			}
    		};
    		
    		//--------------------------------------------
    		// init the generic dialog
 	     	//    		    	    		    
	 	    jQuery("#dialog").dialog({
	 	    	 autoOpen: false,
//	 	    	 dialogClass : "no-close",
	             height : 'auto',
	             width : 500,
	             modal : true
	        });
	 	    
	 	    var css = 'color: black;';
			css += 'padding: 3px;';
			css += 'padding-top:15px;';
			css += 'padding-bottom: 15px;';
			
			var div = '<div >';
			var div = '<div style="'+css+'" id="dialog-message"/>';
			div += '</div>';
			
            jQuery("#dialog").append(div);
                
			//--------------------------------------------
    		// init the discover dialog
 	     	// 
            progressBar = jQuery('#progressBar');
            // progressBar.lastValue = 0;
            // progressLabel = jQuery('.progress-label');
               
            progressBar.progressbar({
                value: false 
            });
			            
	 	    jQuery("#discoverDialog").dialog({
	 		    autoOpen: false,
	 		    dialogClass : "no-titlebar",
	            height : 70,
	            width : 400,
	            modal : true
	        });
 	    });   
	}
	
	obj.markerIcon = function(color){
    	
    	switch(color) {
        case 'red':
            return 'https://api.geodab.eu/docs/assets/img/red-marker.png';
            break;
        case 'blue':
        	return 'https://api.geodab.eu/docs/assets/img/blue-marker.png';
            break;
        case 'green':
        	return 'https://api.geodab.eu/docs/assets/img/green-marker.png';
            break;
        case 'yellow':
        	return 'https://api.geodab.eu/docs/assets/img/yellow-marker.png';
            break;
        }
   };
    
   /**
    *  Opens a page with detailed info on the {{#crossLink "GINode"}}{{/crossLink}} with the given <code>targetNodeId</code> 
    *  
    *  @param dabNode
    *  @param targetNodeId
    *  @method openNodeInfoPage
    *  @static
    */
    obj.openNodeInfoPage = function(dabNode, targetNodeId){
    	
    	 var endpoint = dabNode.endpoint();
    	 if(!endpoint.endsWith('/')){
    		 endpoint += '/';
    	 }
    	 
         var servicePath = dabNode.servicePath();
         var cswPath = dabNode.cswPath();

       	 var link;
       	 
       	 if(GIAPI.nameSpace.nameSpaceType === 'BLUECLOUD'){
       		link = endpoint+servicePath+'/'+cswPath+'?service=CSW&version=2.0.2&request=GetRecordById&id='+targetNodeId+'&outputschema=https://www.blue-cloud.org/&elementSetName=full';
       	 } else {
       		link = endpoint+servicePath+'/'+cswPath+'?service=CSW&version=2.0.2&request=GetRecordById&id='+targetNodeId+'&outputschema=http://www.isotc211.org/2005/gmi&elementSetName=full';
       	 }
   	 
       	 window.open(link);
    };
	
	/**
	 *  Dialog to show during a <code>{{#crossLink "DAB/discover:method"}}{{/crossLink}}</code> 
	 *  
	 *  @param {String} action possible values are:<ul><li>"open"</li><li>"isOpen"</li><li>"close"</li></ul>
	 *  @method discoverDialog
	 *  @static
	 */
	obj.discoverDialog = function(action, status, options){
		
		switch(action){
		case 'open':
			//progressBar.lastValue = 0;
			
			if(options && options.progressBarText){
				// progressLabel.text(options.progressBarText);
			}else{
				// progressLabel.text( '0%' );
				// progressBar.progressbar( 'value', 0 );
			}
			
			if(options && options.progressBarTextLeft){
				// jQuery('.progress-label').css('left',options.progressBarTextLeft+'%');
			}
			
			if(options && options.statusLabelText){
				jQuery('#status-label').text(options.statusLabelText);

			}else{
				jQuery('#status-label').text(__t("search_text"));
			}
			
            jQuery("#discoverDialog").dialog("open");
			return;
		case 'update':
			var value = status[0].progress();
			
			var msg = status[0].message();
			jQuery('#status-label').text(msg);
			
         	if(value >= progressBar.lastValue){      		
         		// progressBar.lastValue = value;
    			// progressBar.progressbar( 'value', value );
        	}  			
			return;
		case 'isOpen':
			return jQuery("#discoverDialog").dialog('isOpen');
		case 'empty':
			 jQuery("#discoverDialog").empty();
			 return;
		case 'close':
			 //progressBar.lastValue = 0;
			 //progressLabel.text( '100%' );  
			 // progressBar.progressbar( 'value', 100 );
			 
			 jQuery('#status-label').text('Done');
			 
			 jQuery("#discoverDialog").dialog('close');
			 return;	
		}		
	};
	
	/**
	 *  Generic dialog
	 *  
	 *  @param {String} action possible values are:<ul><li>"open"</li><li>"isOpen"</li><li>"close"</li></ul>
	 *  @param {Object} [options] all the <a href="https://jqueryui.com/dialog/">jQuery UI Dialog</a> options are allowed plus the following
	 *  
	 *  @param {String} [options.message] the message to show
	 *  @param {Boolean} [options.maximize=false] if set to <code>true</code> a small maximize button is set beside the close button  
     *  @param {Integer[]} [options.maximize] Integer array &#91;width,height&#93;; if <code>maximize</code> is set to <code>true</code> set the maximum size of the maximized window 
     *   
	 *  @method dialog
	 *  @static
	 */
	obj.dialog = function(action,options){
		
		switch(action){
		case 'open':
			
			jQuery("#dialog-message").empty();               
			
			if(options.message){
                jQuery("#dialog-message").append(options.message);
            }
			
			var maximize = options.maximize;
			var maxSize = options.maxSize;

			delete options.maximize;
			delete options.maxSize;
			delete options.message;
			
			jQuery("#dialog").dialog("option", options);
			
            if(maximize){          	
            	if(!maxSize){           		
            		 maxSize = function(){
            		   	 
        		        var winW = jQuery(window).width() - ((jQuery(window).width() / 100) * 10);
        		        var winH = jQuery(window).height() - ((jQuery(window).height() / 100) * 10);       
        			    	 
        				return [winW,winH];
        		    };
            	}            	
            	jQuery("#dialog").dialog().maximizeButton( true, maxSize );                
            }else{
              	jQuery("#dialog").dialog().maximizeButton(false);
            }
            
	        jQuery("#dialog").dialog('open');
   
			return;
		case 'isOpen':
			return jQuery("#dialog").dialog('isOpen');
		case 'empty':
			 jQuery("#dialog").empty();
			 return;
		case 'close':
			 jQuery("#dialog").dialog('close');
			 return;	
		}		
	};
	
	/**
	 * Removes the borders from the <a href="https://jqueryui.com/tabs/">jQuery UI Dialog</a> with the specified <code>id</code>
	 * 
	 * @param id the dialog identifier
	 * @static
	 * @method removeDialogBorders
	 */
	obj.removeDialogBorders = function(id){
		
		jQuery('[aria-describedby="'+id+'"]').css('background','transparent');
		jQuery('[aria-describedby="'+id+'"]').css('border','none');	
	};
	
	/**
	 * Adds a close button on the right side of a <a href="https://jqueryui.com/tabs/">jQuery UI Tabs</a> tab with the given <code>id</code>. The button is appended to the 
	 * <code>&lt;ul&gt;</code> or <code>&lt;ol&gt;</code> element which defines the tabs
	 * 
	 * @param id the tab identifier
	 * @param onClose callback function called when the button is clicked
	 * @static
	 * @method addCloseButtonOnTab
	 */
	obj.addCloseButtonToTab = function(id,onClose){
		
		var css = ' position: relative; float: right; margin-right: 5px; top: 15px;';    
		var buttonId = GIAPI.random();
		var button = '<button id="'+buttonId+'" style="'+css+'" type="button" class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close" role="button" title="Close">';
			button += '<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span><span class="ui-button-text">Close</span></button>';
		
		if(jQuery('#'+id+'-ul').length > 0){
			jQuery('#'+id+'-ul').append(button);
		}else{
			jQuery('#'+id+'-ol').append(button);
		}
		
        jQuery('#'+buttonId).click(onClose);
	}
	
	/**
	 * Loads the given <code>node</code> <code>{{#crossLink "GINode/overview:method"}}{{/crossLink}}</code> in the HTML 
	 * container with the specified <code>id</code> using the "small" size of 96x96px and set a "click" listener to the container in order to show 
	 * the image (in its original size) in a maximizable <code>{{#crossLink "UI_Utils/dialog:method"}}{{/crossLink}}</code>
	 *  
	 * @param {GINode} node
	 * @param {String} id
	 * @param {Object} [options] all the <code>{{#crossLink "GINode/overview:method"}}{{/crossLink}}</code> options are allowed. If <code>selector</code> is set, <code>index</code> is ignored
	 * @param {Boolean} [options.selector] if set, in case of multiple overviews available, two arrow buttons "left" and "right" placed on bottom of the overview container allow
	 *  to select which overview to show
	 * @param {Boolean} [options.maintainSize] is set, the overview container maintains the size of the "small" overview size 
	 * of 96x96 px also if the the image is smaller than 96x96
	 *  
	 * @method loadOverview
	 * @static
	 */
	obj.loadOverview = function(node, id, options) {
			    	      
        var onOverviewResponse = function(status, msg, image) {
        	            
        	if (status === 'error') {                       

        		jQuery('#'+id).addClass('no-overview-div');      		
             
        	}else{
            	
            	if(!node.report().overview || !node.report().overview.length){
            		jQuery('#'+id).addClass('no-overview-div');
            		return;
            	}
            	
              	jQuery('#'+id).addClass('overview-div');
              	
              	
               	if(options.maintainSize){
             		 jQuery('#' + id).attr('width',  '96px');
                     jQuery('#' + id).attr('height','96px');
                     jQuery('#' + id).css('width', '96px');
                     jQuery('#' + id).css('height', '96px');
             	}
              	            	
              	var length = node.report().overview.length;
              	var addDivListener = false;
            
              	if(length > 1 && options.selector){
             		
             		if( jQuery('#'+id).parent().children().length === 1){
             			
             			addDivListener = true;
             			
             			jQuery('#'+id).attr('idx','0');
             			
	             		var css = 'width:100%; height: 20px;margin-top:2px'
	             		
	            		var selectDiv = '<div style="'+css+'">';
	
	             		var prevId = GIAPI.random();
	             		var nextId = GIAPI.random();
	             		
	             		selectDiv += '<i id="'+prevId+'" title="Previous image" style="color:gray" class="fa fa-chevron-circle-left" aria-hidden="true"></i>';
	                 	selectDiv += '<i id="'+nextId+'" title="Next image" style="cursor:pointer; margin-left:3px" class="fa fa-chevron-circle-right" aria-hidden="true"></i>';
	                	
	                 	selectDiv += '</div>';
	
	                	jQuery('#'+id).parent().append(selectDiv); 
	                	
	                	jQuery('#'+prevId).click(function(){
	                		
	                		 var idx = parseInt(jQuery('#'+id).attr('idx'));
	               		  	 var exec = false;
	
	                		 if( (idx-1) >= 1){
	                			 jQuery(this).css('color','black');
	                			 jQuery(this).css('cursor','pointer');
	                			 
	                			 jQuery('#'+nextId).css('color','gray');
	                			 jQuery('#'+nextId).css('cursor','default');
	                			 exec = true;
	                		 }else{
	                			 jQuery(this).css('color','gray');
	                			 jQuery(this).css('cursor','default');
	                			 
	                			 jQuery('#'+nextId).css('color','black');
	                			 jQuery('#'+nextId).css('cursor','pointer');              			 
	                		 }
	                		 
	                		 if((idx-1) >= 0){
		            			 idx-=1;
		            			 jQuery('#'+id).attr('idx',idx);		                     	
		             			 jQuery('#'+id).empty();
	
		             		  	 options.size = 'small';
		             		  	 options.index = idx;
		             	         node.overview(id, onOverviewResponse, options);  
	             			 }
	               		
	                	});
	                	
	                	jQuery('#'+nextId).click(function(){
	                		
	                		  var idx = parseInt(jQuery('#'+id).attr('idx'));
	
	                		  if( (idx+1) < length-1){
	                 			 jQuery(this).css('color','black');
	                 			 jQuery(this).css('cursor','pointer');
	                 			
	                 			 jQuery('#'+prevId).css('color','gray');
	               			     jQuery('#'+prevId).css('cursor','default');
	                 		 }else{
	                 			 jQuery(this).css('color','gray');
	                 			 jQuery(this).css('cursor','default');
	                 			 
	                 			 jQuery('#'+prevId).css('color','black');
	                			 jQuery('#'+prevId).css('cursor','pointer');
	                 		 }
	                		  
	                		 if((idx+1) < length){ 
		              			 idx+=1;
		              			 
		              			 jQuery('#'+id).attr('idx',idx);	
		             			 jQuery('#'+id).empty();
		             			 
		             			 options.size = 'small';
		             			 options.index = idx;
		             	         node.overview(id, onOverviewResponse, options);  
	             			 }	                		  
	                	});
                	}
            	}
           	
              	if(length === 1 || addDivListener || !options.selector){
	             	// div click listener: shows the overview dialog
	             	// with the overview in original size
	                jQuery('#'+id).click(function(){
	                	
	                	jQuery("#overviewDialogImg").empty();
	                	
	                	options.size = 'original';
	                	var idx = jQuery('#'+id).attr('idx');
	                	
	                	if(idx){
	                		options.index = parseInt(idx);
	                	}
	
	                	node.overview('overviewDialogImg', function(s,m,i){
	                		
	                		var title = node.report().title;
	                		if(title.length > 60){
	                			title = title.substring(0,60)+'...';
	                		}                		
	                		
	                		openOverviewDialog(i.width,i.height,title);
	                		
	                	}, options);
	                });
                }
            }
        };
        
        if(!options){
        	options = {};
        }
        
        options.size = 'small';
        node.overview(id, onOverviewResponse, options);               
    };
   
    /**
     *  Draws a thin light gray line; see the <code>line</code> class in the <a href="https://api.geodab.eu/docs/assets/css/giapi.css">API CSS</a> file
     *  
     *  @param {String} css css for the &lt;div&gt element of the icon; <b>at least a width must be set</b> 
     *  @method separator
 	 *  @static
     */
    obj.separator = function(css){
    	
    	return '<div style="'+css+'" class="line"/>';
    };
    
	/**
	 *  Exit from the full screen mode of the browser
	 * 
	 * @method exitFullScreen
	 * @method separator
 	 * @static
	 */
	obj.exitFullScreen = function(){
		
	   if(document.webkitExitFullscreen){
  		   document.webkitExitFullscreen();
  	   }else if(document.mozCancelFullscreen){
  		   document.mozCancelFullscreen();
  	   }else if(document.exitFullscreen){
  		   document.exitFullscreen();
  	   }	
	};
	
	/**
	 * @method isFullScreen
	 */
	obj.isFullScreen = function(){
		
		return document.fullscreenElement ||     
		document.mozFullScreenElement || 
		document.webkitFullscreenElement || 
		document.msFullscreenElement;
	};
	
	/**
	 * Creates a small help icon which shows a <code>{{#crossLink "UI_Utils/dialog:method"}}{{/crossLink}}</code> when clicked
	 * 
	 * @param {String} title title of the help dialog
	 * @param {String} helpMsg message of the help dialog
	 * @param {String} css css for the &lt;img&gt element of the icon
	 * @param {String} imgClass additional class name for the image (can be used to customize the help image) 
	 * 
	 * @method helpImage
	 */
	obj.helpImage = function(title, helpMsg, css, imageId, imgClass){
		
		var id = GIAPI.random();
		if(css){
			css = 'style="cursor:pointer; '+css+'"';
		}else{
			css = 'style="cursor:pointer"';
		}
		
		if (imageId == null){
			imageId = 'fa-question-circle-o';
		}
		
		imgClass = imgClass ? imgClass: '';
		
		var img = '<i '+css+' id="'+id+'" class="'+imgClass+' fa '+imageId+' " aria-hidden="true"></i>';
		
		jQuery( document ).on( 'click', '#'+id, (function(){
			
			obj.dialog('open',{ 
				height : 'auto',
	            width : 500,
	            title: title,
	            message: helpMsg,
	            resizable: false
	        
	        });			
		}));
				
		return img;
	};
	
	/**
	 * Set a callback which handles the enter key pression 
	 * 
	 * @param {Function} handler
	 * @method enterKeyDown
	 * @static
	 */
	obj.enterKeyDown = function(handler){
   	
       var keycode;
         
       if (window.event){
           keycode = window.event.keyCode;
       }else if (e){
           keycode = e.which;
       }
             
       if((keycode === 13)){   	  
    	   handler.apply(this,[]);
    	   return false;
       }
       return true;

    };
    
    /**
     * Set a glass pane over the HTML element with the given <code>targetId</code>.<br>
     * 
     * See also {{#crossLink "UI_Utils/remove:method"}}{{/crossLink}}
     * 
     * @param {String} targetId id of the HTML element where to apply the glass pane
     * @param {Object} [options] 
     * @param {Boolean} [options.background=false] if set, apply a background with diagonal ticks on the target element
     * @param {Number} [options.opacity=0.2] set the opacity of the target element
     * @param {Function} [options.onClick] callback function to call if the glass pane is clicked

     * @method setGlassPane
     * @static
     */
    obj.setGlassPane = function(targetId,options){
    	
    	if(!options){
    		options = {}; 		
    	}
    	
    	if(!options.background){
        	options.background = false;
    	}
		
    	if(!options.opacity){
        	options.opacity = 0.2;
    	}
    	
    	var pos = jQuery('#'+targetId).position();
	    var w = jQuery('#'+targetId).width();
	    var h = jQuery('#'+targetId).height();

 		var css = 'width: '+w+'px;';
 		css += 'height: '+h+'px;';
 		css += 'margin-top: -'+h+'px;';
 		
 		jQuery('#'+targetId).css('opacity',options.opacity);
 		 		
 		var glassPaneId = obj.glassPaneMap[targetId];
 		if(!glassPaneId){
 			glassPaneId = GIAPI.random();
 	 		obj.glassPaneMap[targetId] = glassPaneId;
 		}
 		
 		jQuery('#'+glassPaneId).remove();
 		
 		var bck = options.background ? 'glass-pane-background' : '';
 		jQuery('#'+targetId).append('<div id="'+glassPaneId+'" style="'+css+'" class="glass-pane '+bck+'"></div>');
 		
 		if(options.onClick){
 			jQuery('#'+glassPaneId).click(function(){
 				options.onClick();
 			});
 		}
    };
    
    /**
     * Removes the glass pane from the HTML element with the given <code>targetId</code> 
     * and set the opacity to 1. If a background was set, it is removed.<br>
     * 
     * See also {{#crossLink "UI_Utils/setGlassPane:method"}}{{/crossLink}}
     *
     * @method removeGlassPane
     * @static
     */
    obj.removeGlassPane = function(targetId){
    	
 		var glassPaneId = obj.glassPaneMap[targetId];
 		jQuery('#'+glassPaneId).remove();
 		jQuery('#'+targetId).css('opacity',1);
    };
    
    /**
     * Uses canvas.measureText to compute and return the width of the given text of given font in pixels.
     * 
     * @param text The text to be rendered.
     * @param {String} font The css font descriptor that text is to be rendered with (e.g. "14px verdana").
     * 
     * @see http://stackoverflow.com/questions/118241/calculate-text-width-with-javascript/21015393#21015393
     * @static
     *
     */
    obj.getTextWidth = function(text, font) {
        // if given, use cached canvas for better performance
        // else, create new canvas
        var canvas = obj.canvas || (obj.canvas = document.createElement("canvas"));
        var context = canvas.getContext("2d");
        context.font = font;
        var metrics = context.measureText(text);
        return metrics.width;
    };

    
    var openOverviewDialog = function(imagew,imageh,title) {

    	var w = imagew > 500 ? 500 : 'auto';
    	var h = imageh > 500 ? 500 : 'auto';
    	            	            	
        jQuery("#overviewDialog").dialog({
        	title: title,
        	modal : true,
            autoOpen: false,
            width : w,
            height : h,
            maxWidth: imagew+60,
            maxHeight: imageh+55,                   
        });
    	
		jQuery('#overviewDialog').attr('w', w);
		jQuery('#overviewDialog').attr('h', h);
		
		if(w === 'auto'){
			jQuery('#overviewDialog').css('overflow', 'hidden');		
		}else{
			jQuery('#overviewDialog').css('overflow', 'auto');		
		}
               
        var maxSize = function(id){
         	 
            var winW = jQuery(window).width() - ((jQuery(window).width() / 100) * 10);
            var winH = jQuery(window).height() - ((jQuery(window).height() / 100) * 10);       
    	    	 
            var w = parseInt(jQuery('#'+id).attr('width'))+35;
    	 	var h = parseInt(jQuery('#'+id).attr('height'))+60;
    	 	    	
    	 	return [Math.min(w,winW),Math.min(h,winH)];
        };
                
        if(w !== 'auto'){
        	jQuery("#overviewDialog").dialog().maximizeButton( true, 
        			function(){
        				return maxSize('overviewDialogImg');
        			});
        }else{
        	jQuery("#overviewDialog").dialog().maximizeButton(false);
        }
        
        jQuery("#overviewDialog").dialog('open');
    };
    
    obj.header = function(version){
    	
    	var header =  '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">'+
    	'<link rel="shortcut icon" type="image/png" href="https://api.geodab.eu/docs/assets/favicon.png">'+
    	
    	'<div>  '+  
    	    '<div>'+
    		    '<style>'+
    			   ' .api-header{'+
    			        'height: 110px;'+
    			        'width: 100%;'+
    			        'border:1px solid;  '+              
    			       ' -moz-box-shadow:inset 0px 1px 0px 0px #bbdaf7;'+
    			       ' -webkit-box-shadow:inset 0px 1px 0px 0px #bbdaf7;'+
    			       ' box-shadow:inset 0px 1px 0px 0px #bbdaf7;'+
    			       ' background:-webkit-gradient( linear, left top, left bottom, color-stop(0.05, #79bbff), color-stop(1, #378de5) );'+
    			       ' background:-moz-linear-gradient( center top, #79bbff 5%, #378de5 100% );'+
    			       ' filter:progid:DXImageTransform.Microsoft.gradient(startColorstr="#79bbff", endColorstr="#378de5");'+
    			       ' background-color:#79bbff;'+
    			       ' -moz-border-radius:6px;'+
    			       ' -webkit-border-radius:6px;'+
    			       ' border-radius:6px;'+
    			       ' border:1px solid #84bbf3;'+
    			       ' color:#ffffff;'+
    			       ' font-family:arial;'+
    			       ' font-size:15px;'+
    			       ' font-weight:bold;'+
    			       ' text-decoration:none;'+
    			       ' text-shadow:1px 1px 0px #528ecc;'+                                
    			       ' margin-top:30px;'+
    	               ' margin-bottom:5px;'+
    	               ' margin-left:-30px;'+
    	               ' margin-right:50px;'+
    			   ' }'+       		   
    		   '</style>'+
    					    
    		   '<a href="https://api.geodab.eu/index.html">'+
    			   ' <table style="width:100%; margin-top: -20px; margin-bottom: -30px"><tr>'+
    			       ' <td style="width:300px; height: 150px; background-color: white;"><img style="border: none;width: 350px" src="https://api.geodab.eu/docs/assets/img/api-logo.png" /></td>'+
    			       ' <td style="background-color: white;">'+
    			       ' <div class="api-header">'+
    			           
    			           ' <span style="position:absolute; margin-top: 15px; margin-left: 10px; font-size: 270%">The GEOSS Discovery And Access Broker APIs</span><br>'+
    			           ' <span style="position:absolute; margin-top: 45px; margin-left: 10px; font-size: 110%">Authors: Fabrizio Papeschi, Mattia Santoro, Stefano Nativi</span><br>'+
    			       ' </div></td>'+
    			   ' </tr></table>'+
    		   ' </a>'+
    		    
    		   ' <div style="width: 100%;margin-left: 10px;margin-bottom: 5px">'+
    			    '<a style="text-decoration: none" href="http://www.eurogeoss.eu" target="_blank">'+
    			       ' <img style="background-color: white;border: none" src="https://api.geodab.eu/docs/assets/img/eurogeoss.png" /> '+
    			    '</a>'+
    			    
    			   ' <a style="margin-left: 10px; text-decoration: none" href="http://www.iia.cnr.it/" target="_blank">'+
    			        '<img style="background-color: white;border: none" src="https://api.geodab.eu/docs/assets/img/iia.png" /> '+
    			    '</a>'+
    			     
    			    '<a style="margin-left: 10px; text-decoration: none" href="http://www.uos-firenze.essi-lab.eu/" target="_blank">'+
    			        '<img style="background-color: white;border: none" src="https://api.geodab.eu/docs/assets/img/essilab.png" /> '+
    			    '</a>'+
    			    
    			    '<a style="margin-left: 10px; text-decoration: none" href="http://ec.europa.eu/index_en.htm" target="_blank">'+
    			        '<img style="background-color: white;border: none" src="https://api.geodab.eu/docs/assets/img/jrc.png" /> '+
    			    '</a>'+
    			    
    			   ' <a style="margin-left: 50px; text-decoration: none" href="http://www.earthobservations.org/" target="_blank">'+
    			       ' <img style="background-color: white;width: 320px;border: none" src="https://api.geodab.eu/docs/assets/img/geo.png" /> '+
    			   ' </a> '+
    			    
    			    ' <a style="margin-left: 50px; text-decoration: none" href="https://creativecommons.org/licenses/by-nc/4.0/" target="_blank">'+
    			       ' <img style="background-color: white;float:right; margin-right: 65px; width: 120px;border: none" src="https://api.geodab.eu/docs/assets/img/ccbync.png" /> '+
    			    '</a> '+                     
    			'</div>'+
    		        
    			'<div  style="margin-left: 10px">'+
    			    '<em>API version: '+version+'</em>'+
    			'</div>'+
    			
    		'</div>'+
    	'</div>';
    	
    	jQuery('body').prepend(header);
    };
    	
	return obj;
};