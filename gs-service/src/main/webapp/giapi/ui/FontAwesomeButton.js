/**
 * @module UI
 **/
import { GIAPI } from '../core/GIAPI.js';

/**
 * ---------------------------------
 * https://fontawesome.com/v4/icons/
 * ---------------------------------
 * 
 * @class FontAwesomeButton
 * 
 * @constructor
 * 
 * @param {Object} options
 * 
 * @param {String} options.icon the Font Awesome icon class name (e.g: 'fa-search')
 * @param {String} options.label the button label
 * @param {Function} options.handler callback function to call when the button is clicked
 *
 * @param {String} [options.id=GIAPI.random()] button &lt;div&gt; identifier
 * @param {Integer} [options.width=135] button &lt;div&gt; width
 * 
 * @param {Object[]} [options.attr] array of attributes defined by objects like this 
 *  <code>{'name':'<i>attribute-name</i>', 'value':'<i>attribute-value</i>'}</code> to be added to the button. They can be used to store particular values 
 *  to use in the 'click' function. E.g.:<pre><code>jQuery("#"+id).click(function(){
 *  	var attrValue = jQuery(this).attr('attribute-name');
 * 
 */
GIAPI.FontAwesomeButton = function(options){
	  
      var button = {};
      var enabled = true; 
            
      var buttonId = options.id;
      if(!buttonId){
    	  buttonId = GIAPI.random();
      }
      
      var _label = options.label;      
      var labelId = buttonId+'Label';
      var iconId = buttonId+'Icon';

      if(!options.handler){
    	  options.handler = function(){};
      }
      
//      if(!options.width){
//    	  options.width = 135;
//      }
      
      if(!options.iconSize){
    	  options.iconSize = '13';
      }
      
      var divStyle = options.width ? 'width:'+options.width+'px;' : '';
      var iconStyle = 'font-size:'+options.iconSize+'px;';
      var labelStyle = '';
      
      // adds the click listener
	  jQuery(document).on('click','#'+buttonId, function(){
		  
		  if(enabled){
			  options.handler.apply(this,[]);
		  }		  
	  });
	  
  	  /**
  	   * Set the given <code>label</code>
  	   * 
  	   * @param {String} label
  	   * 
  	   * @method label
  	   */
  	  button.label = function(state){
  		  
  	 	   jQuery('#'+labelId).html(label);
  	 	   _label = label;
  	  };
  	  
  	  /**
  	   * Set the CSS <code>property</code> and its <code>value</code> to the given <code>target</code>
  	   * 
  	   * @param {String} target possible values: 'div','label','icon'
  	   * @param {String} property
  	   * @param {String} value
  	   * 
  	   * @method css
  	   */
  	  button.css = function(target,property,value){
  		
  		  switch(target){
  		  case 'div': 			  			  
			  jQuery('#'+buttonId).css(property,value);
			  divStyle += property+':'+value+';'  			   
  			  break;
   		  case 'label':
    	      jQuery('#'+buttonId+'Label').css(property,value);
 			  labelStyle += property+':'+value+';'
    	      break;
  		  case 'icon':
	    	  jQuery('#'+buttonId+'Icon').css(property,value);
	    	  iconStyle += property+':'+value+';'
  		  	  break;
  		  }  		  
  	  };
  	  
  	  /**
  	   * Enable/disables the button handler (this method does not affect the CSS)
  	   * 
  	   * @param {Boolean} value 
  	   * @method enable
  	   */
  	  button.enable = function(value){
  		  
  		  enabled = value;
  	  };
  	  
  	  /**
  	   * Returns this button &lt;div&gt;
  	   * 
  	   * @method div
  	   */
  	  button.div = function(){
  		  
  	      var div_  = '<div id="'+buttonId+'" style="'+divStyle+'" class="font-awesome-button"';
  	      if(options && options.attr){
			 options.attr.forEach(function(attr){				 
				 div_ += attr.name+'="'+attr.value+'" ';
			 });
		  }
  	      div_ += '>';
  	      div_ += '<i id="'+iconId+'"class="font-awesome-button-icon fa '+options.icon+'" style="'+iconStyle+'" aria-hidden="true"></i>';	  	  
  	      div_ += '<label id="'+labelId+'" style="'+labelStyle+'"  class="font-awesome-button-label">'+_label+'</label></div>';
    	
  		  return div_;
  	  };
             	  
  	  return button;	
};