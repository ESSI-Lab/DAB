/**
 * @module UI
 **/
import { GIAPI } from '../core/GIAPI.js';

/**
 *
 * @class PageSizeWidget
 * 
 * @constructor
 * 
 * @param {String} id
 *
 * @param {Object} options
 *
 * @param {Integer} [options.width=380]
 * @param {Integer} [options.min=10]
 * @param {Integer} [options.max=50]
 * @param {Integer} [options.step=10]
 * @param {Integer} [options.value=10]
 * @param {String} [options.label="- Max results"]
 * @param {String} [options.help=...]
 */
GIAPI.PageSizeWidget = function(id, options) {
    
	var widget = {};
	
	if(!options){
		options = {};
	}
	
	if(!options.width){
		options.width = 380;
	}
	var sliderWidth = options.width - 135;
	
	if(!options.label){
		options.label = '- Max results';
	}
	
	if(!options.min){
		options.min = 10;
	}
	
	if(!options.max){
		options.max = 50;
	}
	
	if(!options.step){
		options.step = 10;
	}
	
	if(!options.value){
		options.value = 10;
	}
	
	if(!options.help){
		options.help = 'Set the maximum number of results to show (with a limit of '+options.max+').<br> Please note that incrementing this value' +
	 	' the response time and the amount of necessary browser memory can significantly grow<br>';
	}
	
	var divId = GIAPI.random();
	var sliderId = GIAPI.random();
	var labelId = GIAPI.random();
	var helpDivId = GIAPI.random();

	jQuery(document).on('change','#'+sliderId, function(){
         
		jQuery('#'+labelId).text(this.value);
         
         GIAPI.Common_UINode.pageSize = this.value;
    });
	
	jQuery(document).on('mouseover','#'+divId,function(){
		jQuery('#'+helpDivId+' i').css('display','inline-block');
	});

	jQuery(document).on('mouseout','#'+divId,function(){
		jQuery('#'+helpDivId+' i').css('display','none');
	});
				
	var div = '<div id="'+divId+'" style="width: '+(options.width+10)+'px; display:inline-block; margin-top: 10px;margin-left: 5px;">';
  	div += '<label style="vertical-align: super;">'+options.label+'</label>';
  	div += '<input id="'+sliderId+'" style="margin-left:5px;width:'+sliderWidth+'px" value="'+options.value+'" type="range" step="'+options.step+'" min="'+options.min+'" max="'+options.max+'"/>';
  	div += '<label style="margin-left:5px;vertical-align: super;" id="'+labelId+'">'+options.value+'</label>';
  	div += '<div style="display:inline-block" id="'+helpDivId+'">'+GIAPI.UI_Utils.helpImage('Max results',options.help,'vertical-align: super;margin-left: 5px; display:none')+'</div></div>';
  	
  	jQuery('#'+id).append(div);
	
  	/**
  	 * @method value
  	 */
	widget.value = function(){
		
		return jQuery('#'+sliderId).val();
	};
	 		    
    return widget;
	 
};
