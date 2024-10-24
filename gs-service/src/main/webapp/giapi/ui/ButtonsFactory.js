/**
 * @module UI
 **/

/**
 * Factory class for creation of several kind of buttons. The following CSS is required:<pre><code>&lt;!-- API CSS --&gt;
 &lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
</code></pre>
 * 
 * @class ButtonsFactory
 */

GIAPI.ButtonsFactory = {
		
	/**
	 * Nice button based on <a target="_blank" href="http://www.cssbutton.me/calebogden/4fd7c9f23f2df02715000000">http://www.cssbutton.me/calebogden/4fd7c9f23f2df02715000000</a>.<br>
	 * 
     * For additional personalization, see the <code>calebogden-button</code> class of the <i>giapi.css</i> file
	 * 
	 * @param {String} label
	 * @param {Object} [options]
	 * 
	 * @param {String} [options.id] button identifier
 	 * @param {Function} [options.handler] button click handler
 	 * 
     * @param {String} [options.color="green"] available colors are:<br><table style="background-color: white">
     * <tr><td style="padding: 0px; width: 100px; background-color: white">- 'green'</td><td style="padding: 0px;background-color: white"> <img style="border: none;" src="../assets/img/cale-green.png" /></td></tr>
     * <tr><td style="padding: 0px; background-color: white">- 'blue'</td><td style="padding: 0px;background-color: white"> <img style="border: none;" src="../assets/img/cale-blue.png" /></td></tr>
     * <tr><td style="padding: 0px; background-color: white">- 'yellow'</td><td style="padding: 0px;background-color: white"> <img style="border: none;" src="../assets/img/cale-yellow.png" /></td></tr>
     * <tr><td style="padding: 0px; background-color: white">- 'red'</td><td style="padding: 0px;background-color: white"> <img style="border: none;" src="../assets/img/cale-red.png" /></td></tr></table>
	 * @param {String} [options.title] 
     * @param {Object[]} [options.attr] array of attributes defined by objects like this 
     * <code>{'name':'<i>attribute-name</i>', 'value':'<i>attribute-value</i>'}</code> to be added to the button. They can be used to store particular values 
     * to use in the 'click' function. E.g.:<pre><code>jQuery("#"+id).click(function(){
     *  	var attrValue = jQuery(this).attr('attribute-name');
     *  	..
  });</pre></code>
	 * 
	 * @static
	 * @return the button HTML markup
	 * @method caleButton
	 */
	caleButton: function(label, options) {
		
		 if(!options){
			 options = {};
		 } 
		 
		 if(!options.color){
			 options.color = 'green';
		 }
		 
		 if(!options.title){
			 options.title = '';
		 }
		 
		 if(!options.id){
			 options.id = GIAPI.random();
		 }
		 
		 var button = '<ul title="'+options.title+'" id="'+options.id+'" ';
		 if(options && options.attr){
			 options.attr.forEach(function(attr){				 
				 button += attr.name+'="'+attr.value+'" ';
			 });
		 }
		 button += ' class="calebogden-button">';		 
		 button += '<li><span class="'+options.color+'">&nbsp;</span><a style="text-decoration:none">'+label+'</a></li></ul>';
		 
		 if(options.handler){
			 jQuery( document ).on( 'click', '#'+options.id, options.handler);
		 }
		 
		 return '<div>'+button+'</div>';
	},
	
	/**
	 * On/off switch button based on <a href="https://proto.io/freebies/onoff/">https://proto.io/freebies/onoff/</a>.
	 * 
	 * Example with all the <code>options</code> set to the default values: <pre><code>
	 * GIAPI.ButtonsFactory.onOffSwitchButton('On label','Off label', {
	'borderColor':'rgba(55, 141, 229, 0.47)',
	'shadowColor':'#999',
	'onColor':'#323232',
	'offColor':'#999999',
	'onBckColor':'rgba(255, 255, 255, 1)',
	'offBckColor':'#EEEEEE'
	'switchColor':'rgba(121, 187, 255, 0.24)',
	'switchBorderColor':'rgba(55, 141, 229, 0.47)'
}
	 * </pre></code>
	 * 
	 * <li> Button in "on" modality:<br>
	 * <img style="border: none;" src="../assets/img/on-switch.png" />
	 * <li> Button in "off" modality:<br>
	 * <img style="border: none;" src="../assets/img/off-switch.png" />
	 * 
	 * For additional personalization, see the <code>onoffswitch</code> class of the <i>giapi.css</i> file
	 * 
     * @param {String} onLabel
     * @param {String} offLabel
     * @param {Object} [options]
     * 
     * @param {Object} [options.size]
     * @param {Object} [options.checked=true]

	 * @param {String} [options.id] button identifier
 	 * @param {Function} [options.handler] button click handler
     * 
     * @param {String} [options.shadowColor="transparent"]
     * @param {String} [options.borderColor="transparent"] 
     * 
     * @param {String} [options.onColor="white"]
     * @param {String} [options.offColor="#white"]
     * @param {String} [options.onBckColor="#467FC9"]
     * @param {String} [options.offBckColor="#2c3e50"]
     * @param {String} [options.switchColor="white"]
     * @param {String} [options.switchBorderColor="white"] 
     * @param {Object[]} [options.attr] array of attributes defined by objects like this 
     * <code>{'name':'<i>attribute-name</i>', 'value':'<i>attribute-value</i>'}</code> to be added to the button. They can be used to store particular values 
     * to use in the 'click' function. E.g.:<pre><code>jQuery("#"+id).click(function(){
     *  	var attrValue = jQuery(this).attr('attribute-name');
     *  	..
  });</pre></code>
     * 
     * @static
  	 * @return the button HTML markup
	 * @method onOffSwitchButton
	 */
	onOffSwitchButton: function(onLabel, offLabel, options){
		
		 if(!options){
			 options = {};
		 }
		 
		 if(!options.onBckColor){
			 options.onBckColor = '#467FC9';
		 }
		 
		 if(!options.offBckColor){
			 options.offBckColor = '#2c3e50';
		 }
		 
		 if(!options.onColor){
			 options.onColor = 'white';
		 }
		 
		 if(!options.offColor){
			 options.offColor = 'white';
		 }
		 
		 if(!options.id){
			 options.id = GIAPI.random();
		 }
		 		 
		 if(options.checked  || options.checked === undefined){
			 options.checked = 'checked';
		 }else{
			 options.checked = '';
		 }
		 
		 var size = '';
		 switch(options.size){
		 case 'small':
			 size = 'small-';
			 break;
		 case 'medium':
			 size = 'medium-';
			 break;
		case 'large':
			 size = 'large-';
			 break;
		 }
				 
		 var style = '<style type="text/css">';
		 
		 style += '#'+options.id+'-inner.'+size+'onoffswitch-inner:before{content: "'+onLabel+'"; background-color: '+ options.onBckColor+'; color: '+ options.onColor+';} ';
		 style += '#'+options.id+'-inner.'+size+'onoffswitch-inner:after{content: "'+offLabel+'"; background-color: '+ options.offBckColor+'; color: '+ options.offColor+';} ';
		 
		 style += '</style>';
		 		 		 		 
		 if(!options.borderColor){
			 options.borderColor = 'transparent';
		 }
		 
		 if(!options.shadowColor){
			 options.shadowColor = 'transparent';
		 }
		 		 				 
		 if(!options.switchColor){
			 options.switchColor = 'white';
		 }

		 if(!options.switchBorderColor){
			 options.switchBorderColor = 'white';
		 }
		 
		 var button = '<div class="'+size+'onoffswitch"  id="onoffswitch-div-'+options.id+'">'+style;
		 button += '<input type="checkbox" name="onoffswitch" class="'+size+'onoffswitch-checkbox" id="'+options.id+'" ';
		 if(options && options.attr){
			 options.attr.forEach(function(attr){				 
				 button += attr.name+'="'+attr.value+'" ';
			 });
		 }
		 button += options.checked+'>';
		 
		 var labelStyle = 'border: 2px solid '+options.borderColor+'; box-shadow: 4px 4px 3px 0 '+options.shadowColor+';';
		 button += '<label id="'+options.id+'-label" class="'+size+'onoffswitch-label" for="'+options.id+'" style="'+labelStyle+'" >';
		 
		 button += '<span id="'+options.id+'-inner" class="'+size+'onoffswitch-inner"></span>';
		 
		 var switchStyle = 'background: '+options.switchColor+'; border: 2px solid '+options.switchBorderColor+';';
		 button += '<span id="'+options.id+'-switch" class="'+size+'onoffswitch-switch" style="'+switchStyle+'"></span>';
		 button += '</label></div>';
		 
		 if(options.handler){
			 jQuery( document ).on( 'click', '#'+options.id, options.handler);
		 }
		 		 
		 return '<div>'+button+'</div>';
	},
	
	/**
	 * Rounded corner button with CSS round icon based on <a href="https://proto.io/freebies/onoff/">https://proto.io/freebies/onoff/</a>.
	 * 
	 * Example with all the <code>options</code> set to the default values: <pre><code>
	 * GIAPI.ButtonsFactory.roundCornerButton('Label',{
	'enabled':true,
	'iconRadius': 20,
	'borderRadius': 20,
	'width':120,
	'labelColor':'rgb(43, 44, 105)',
	'bckColor':'#F7F7F7',
	'hoverBckColor':'rgb(187, 218, 247)',
	'borderColor':'rgba(63, 81, 181, 0.42)',
	'iconX':95,
	'iconColor':'#79bbff',
	'iconBorderColor':'#79bbff',
	'shadowColor':'#999'}
);
	</pre></code>
	 * 
	 * <li> Button enabled:<br>
	 * <img style="border: none;" src="../assets/img/round-crn-button.png" />
	 * <li> Button disabled:<br>
	 * <img style="border: none;" src="../assets/img/round-crn-button-disabled.png" />
	 * 
	 * Example with darker colors and less rounded corners: <pre><code>
	 * GIAPI.ButtonsFactory.roundCornerButton('Label',{
	'labelColor':'#2C3E50',
	'borderRadius':8,
	'borderColor':'rgba(44, 62, 80, 0.62)',
	'iconColor':'#2C3E50'}
);
	</pre></code>
	 * 
	 * <li> Button enabled:<br>
	 * <img style="border: none;" src="../assets/img/round-crn-button-2.png" />
	 * <li> Button disabled:<br>
	 * <img style="border: none;" src="../assets/img/round-crn-button-2-disabled.png" />
	 *
	 * For additional personalization, see the <code>rounded-corner-button</code> class of the <i>giapi.css</i> file
	 * 
     * @param {String} label
     * @param {Object} [options]
     * 
	 * @param {String} [options.id] button identifier
 	 * @param {Function} [options.handler] button click handler
     * 
     * @param {Boolean} [options.enabled=true]
     * @param {Integer} [options.width=120] 
     * @param {String} [options.labelColor="rgb(43, 44, 105)"] 
     * @param {String} [options.bckColor="#F7F7F7"] 
     * @param {String} [options.hoverBckColor="rgb(187, 218, 247)"] 
     * @param {Integer} [options.borderRadius=20]
     * @param {String} [options.borderColor="rgba(63, 81, 181, 0.42)"]
     * @param {Integer} [options.iconX=95]
     * @param {Integer} [options.iconRadius=20]
     * @param {String} [options.iconColor="rgb(43, 44, 105)"]
     * @param {String} [options.iconBorderColor="rgb(43, 44, 105)"]
	 * @param {String} [options.shadowColor="#999"]
     * 
     * @static
     * @param {Object[]} [options.attr] array of attributes defined by objects like this 
     * <code>{'name':'<i>attribute-name</i>', 'value':'<i>attribute-value</i>'}</code> to be added to the button. They can be used to store particular values 
     * to use in the 'click' function. E.g.:<pre><code>jQuery("#"+id).click(function(){
     *  	var attrValue = jQuery(this).attr('attribute-name');
     *  	..
  });</pre></code>
     * 
     * @return the button HTML markup
	 * @method roundCornerButton
	 */
	roundedCornerButton: function(label, options){
		
		if(!options){
			options = {};
		}
		
		if(options.enabled === undefined){
			options.enabled = true;
		}
		
		if(!options.iconRadius){
			options.iconRadius = 20;
		}
		
		if(!options.borderRadius){
			options.borderRadius = 20;
		}
		
		if(!options.width){
			options.width = 120;
		}
		
		if(!options.iconX){
			options.iconX = options.width - 25;
		}
		
		if(!options.labelColor){
			options.labelColor = 'rgb(43, 44, 105)';
		}
		
		if(!options.bckColor){
			options.bckColor = '#F7F7F7';
		}
		
		if(!options.hoverBckColor){
			options.hoverBckColor = 'rgb(187, 218, 247)';
		}
		
		if(!options.borderColor){
			options.borderColor = 'rgba(63, 81, 181, 0.42)';
		}
		
		if(!options.iconColor){
			options.iconColor = 'rgb(43, 44, 105)';
		}
		
		if(!options.iconBorderColor){
			options.iconBorderColor = 'rgb(43, 44, 105)';
		}
		
		if(!options.shadowColor){
			options.shadowColor = '#999';
		}
		
	    if(!options.id){
			options.id = GIAPI.random();
		}	
		
		var disButtonProp = 'cursor: default;'+	
			'color: rgba(128, 128, 128, 0.43);'+
			'background-color: rgba(170, 170, 170, 0.72);'+			
			'border: 2px solid #c0c0c0;'+
			'box-shadow: none;';
			
		var enaButtonProp = 'cursor: pointer;'+	
			'color: '+options.labelColor+';'+ 
			'background-color: '+options.bckColor+';'+
			'border: 2px solid '+options.borderColor+';'+
			'box-shadow: 4px 4px 3px 0 '+options.shadowColor+';';	
//			'box-shadow: 0 0 20px '+options.shadowColor+';';	
		
		var buttonProp = options.enabled ? enaButtonProp : disButtonProp;
				
		var style = '';		 
		style += '#'+options.id+'.rounded-corner-button{'+buttonProp+' width: '+options.width+'px; border-radius: '+options.borderRadius+'px;}';
		if(options.enabled){
			style += '#'+options.id+'.rounded-corner-button:hover { background-color: '+options.hoverBckColor+'; }';
		}else{
			style += '#'+options.id+'.rounded-corner-button:hover { background-color: rgba(170, 170, 170, 0.72); }';
		}
		
		style += '#'+options.id+'-icon.rounded-corner-button-icon{border: 2px solid '+options.iconBorderColor+'; ';				
		style += 'background: '+options.iconColor+'; margin-left: '+options.iconX+'px;  border-radius: '+options.iconRadius+'px;}';		
		style += '#'+options.id+'-icon.rounded-corner-button-icon-disabled{background: rgba(50, 51, 50, 0.05); border: 2px solid rgba(50, 51, 50, 0); }';		

		GIAPI.UI_Utils.appendStyle(style,options.id);
		
		var enButton = options.enabled ? '':' disabled';
		var iconClass = options.enabled ? 'rounded-corner-button-icon':'rounded-corner-button-icon rounded-corner-button-icon-disabled';
				
		var button = '<div><button class="rounded-corner-button" id="'+options.id+'"';
		if(options && options.attr){
			 options.attr.forEach(function(attr){				 
				 button += attr.name+'="'+attr.value+'" ';
			 });
		}
		button += enButton+'>'+label+'</button>';		
		button += '<div id="'+options.id+'-icon" class="'+iconClass+'"/></div>';
		
		if(options.handler){
			jQuery( document ).on( 'click', '#'+options.id, options.handler);
		}
		
		return '<div>'+button+'</div>';
	}	
};
