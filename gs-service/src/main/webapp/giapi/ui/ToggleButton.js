
import { GIAPI } from '../core/GIAPI.js';

/**
 * A toggle button with e possible <a href="#method_state">states</a>. When the button switches to 'on', the target element <code>options.targetId</code> becomes visible, otherwise it is hide.<br>
 * <li> Button 'off' (initial state):<br>
 * <img style="border: none;" src="../assets/img/toggle-off.png" />
	<li> Button 'on':<br>
 * <img style="border: none;" src="../assets/img/toggle-on.png" />
 * <li> Button 'disabled':<br>
 * <img style="border: none;" src="../assets/img/toggle-disabled-off.png" />
 * 
 * For CSS personalization, see the <code>toggle-button</code> class of the <a href="https://api.geodab.eu/docs/assets/css/giapi.css">API CSS</a> file
 *
 * @class ToggleButton
 * 
 * @constructor
 * 
 * @param {Object} options
 * @param {String} options.targetId id of the element which toggles from visible to not visible
 * @param {String} options.targetSelector selector for the element which toggles from visible to not visible

 * @param {String} options.onLabel label of the button in 'on' state
 * @param {String} options.offLabel  label of the button in 'off' state
 * 
 * @param {Function} [options.beforeStart] callback function to call just before the animation is started

 * @param {Function} [options.onComplete] callback function to call once the animation is complete
 * @param {Function} [options.onComplete.state] the button state
 * 
 * @param {Integer} [options.width=135] button div width
 *
 * @param {Integer} [options.duration=800] number expressed in milliseconds determining how long the animation will run
 * @param {String} [options.id=GIAPI.random()] id of the button
 * 
 */
GIAPI.ToggleButton = function(options){
	  
      var button = {};
      
      var buttonId = options.id;
      if(!buttonId){
    	  buttonId = GIAPI.random();
      }
      
      var onLabel = options.onLabel;
      var offLabel = options.offLabel;
      
      var labelId = buttonId+'Label';
      var iconId = buttonId+'Icon';

      if(!options.onComplete){
    	  options.onComplete = function(){};
      }
      
      if(!options.duration){
    	  options.duration = 800;
      }
      
      if(!options.width){
    	  options.width = 135;
      }
      
      // initially off
      button._state = 'off';
      
      var divStyle = 'width:'+options.width+'px;';
      var iconStyle = '';
      var labelStyle = '';
      
      var stateIcon = {
    		'on': 'fa-angle-double-up',
    		'off': 'fa-angle-double-down'
      };
            
//      button.div_  = '<div id="'+buttonId+'" style="width:'+options.width+'px" class="toggle-button toggle-button-off" >';
//	  button.div_  += '<i id="'+iconId+'"class="toggle-button-icon fa fa-angle-double-down" aria-hidden="true"></i>';	  	  
//	  button.div_  += '<label id="'+labelId+'" class="toggle-button-icon-label">'+offLabel+'</label></div>';
	  
	  
      /**
       *  Set the button state icon. Default values are:<ul>
       *  <li>on: 'fa-angle-double-up'</li>
       *  <li>off: 'fa-angle-double-down'</li>
       *  </ul>
       * 
       * @method stateIcon
       */
      button.stateIcon = function(state, icon){
    	  
    	  stateIcon[state] = icon;
      };

      /**
  	   * Set the button state. Possible values are:<ul><li>'off'</li><li>'on'</li><li>'disabled'</li></ul>
  	   * @param {String} state
       *
  	   * @method state
  	   */
  	  button.state = function(state){
  		    		  
  		  switch(state){
  		  case 'on':
  			  if(button._state === 'disabled'){
  	  		  	  addListener();
  			  }
  			  jQuery('#'+buttonId).attr('class','toggle-button toggle-button-on');
  			  jQuery('#'+iconId).attr('class','toggle-button-icon fa '+stateIcon.on);	  	  
  			  jQuery('#'+labelId).attr('class','toggle-button-icon-label');
  			  jQuery('#'+labelId).html(onLabel);
  			  break;
  		  case 'off':
  			  if(button._state === 'disabled'){
 	  		  	  addListener();
 			  }
  			  jQuery('#'+buttonId).attr('class','toggle-button toggle-button-off');
  			  jQuery('#'+iconId).attr('class','toggle-button-icon fa '+stateIcon.off);	  	  
  			  jQuery('#'+labelId).attr('class','toggle-button-icon-label');
  			  jQuery('#'+labelId).html(offLabel);
  			  break;
  		  case 'disabled':
  			  // removes the listener
  			  jQuery(document).off('click','#'+buttonId);
  			  
  			  jQuery('#'+buttonId).attr('class','toggle-button-disabled');
	    	  jQuery('#'+labelId).attr('class','toggle-button-icon-label-disabled');
	    	  jQuery('#'+labelId).html(offLabel);
	    	  break;
  		  }
  		  
  		  if(state){
  	  		  button._state = state;
  		  }
  		  return button._state;
  	  };
  	  
  	  /**
  	   * Set a particular <code>label</code> for the given <code>state</code>
  	   * 
	   * @param {String} state possible values: 'on','off','disabled'
  	   * @param {String} label
  	   * 
  	   * @method setLabel
  	   */
  	  button.setLabel = function(state,label){
  		  
  		  if(state === 'on'){
  			  onLabel = label;
  		  }else{
  			  offLabel = label;
  		  }
		  jQuery('#'+labelId).html(label);
  	  };
  	  
  	  /**
  	   * Set a CSS <code>property</code> and <code>value</code> for the given <code>target</code>
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
  	   * Returns this button &lt;div&gt;
  	   * 
  	   * @method div
  	   */
  	  button.div = function(){
  		  
  		  var div_  = '<div id="'+buttonId+'" style="'+divStyle+'" class="toggle-button toggle-button-off"';
	      if(options && options.attr){
			 options.attr.forEach(function(attr){				 
				 div_ += attr.name+'="'+attr.value+'" ';
			 });
		  }
	      div_ += '>';
 	      div_ += '<i id="'+iconId+'"class="toggle-button-icon fa '+stateIcon.off+'" style="'+iconStyle+'" aria-hidden="true"></i>';	  	  
	      div_ += '<label id="'+labelId+'" style="'+labelStyle+'"  class="toggle-button-icon-label">'+offLabel+'</label></div>';
  	  	   		
  		  return div_;
  	  };
           
  	  var addListener = function(){
  		  
	  	  jQuery(document).on('click','#'+buttonId, function(){
	  		  
	  		  switch(button._state){
	  		  case 'on':  button.state('off'); break;
	  		  case 'off': button.state('on'); break;
	  		  }
	  		  
	  		  var selector = options.targetSelector ? options.targetSelector : '#'+options.targetId;
	  		  
	  		  if(options.beforeStart){
	  			  options.beforeStart();
	  		  }
	  		  	  			
  			  jQuery(selector).slideToggle({
 	     		 duration: options.duration,	     		 
 	     		 complete: function(){ options.onComplete(button._state)}
 	     	  }); 	  		  
	  		   
	  	  });
  	  };
  	  
  	  // adds the listener
  	  addListener();
  	  
  	  return button;	
};