/**
 * @module UI
 **/
import { GIAPI } from '../core/GIAPI.js';

 /**
  *  A <code>UINode</code> is a {{#crossLink "UINode/render:method"}}graphical representation{{/crossLink}} of a <code>{{#crossLink "GINode"}}node{{/crossLink}}</code> which can be 
  *  displayed by a <code>{{#crossLink "ResultSetLayout"}}{{/crossLink}}</code>.<br>
  *  This class can be considered as an <i>abstract class</i> which provides methods and options that can be overridden by the subclasses 
  *  as shown in <a href='#extendUINode'>this section</a>.<br>
  *  The following CSS is required:<pre><code>
 &lt;!-- API CSS --&gt;
 &lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
</code></pre>
  *   
  *  <h4>Choosing the graphical representation of a node</h4>
  *  
  *  Each subclass can <code>{{#crossLink "UINode/render:method"}}render{{/crossLink}}</code> one or more different kind of 
  *  <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> basing on some criteria and/or characteristic. 
  *  In most part of the cases some particular fields and/or values of the <code>{{#crossLink "Report"}}node report{{/crossLink}}</code> can be choose  
  *  to determine the graphic representation.<br>
  *  
  *  For example, a subclass of <code>UINode</code> called <code>Green_UINode</code> could <code>{{#crossLink "UINode/render:method"}}render{{/crossLink}}</code> 
  *  all the <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> with the word "green" in the 
  *  <code>{{#crossLink "Report/title:property"}}report title{{/crossLink}}</code> using a green background, and 
  *  a subclass of <code>UINode</code> called <code>Sea_UINode</code> could <code>{{#crossLink "UINode/render:method"}}render{{/crossLink}}</code> 
  *  all the <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> with the word "sea" in the 
  *  <code>{{#crossLink "Report/description:property"}}report description{{/crossLink}}</code> using a an image 
  *  of the sea as background. Of course making a subclass of <code>UINode</code> which manage a single characteristic can lead to a class explosion; 
  *  often the best solution is to create a single subclass which is able to manage several different kind of cases 
  *  (in the example above the same class could <code>{{#crossLink "UINode/render:method"}}render{{/crossLink}}</code> both 
  *  <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> with the word "green" in the <code>{{#crossLink "Report/title:property"}}report title{{/crossLink}}</code> and 
  *   with the word "sea" in the <code>{{#crossLink "Report/description:property"}}report description{{/crossLink}}</code>).<br>
	  This API provides a {{#crossLink "Common_UINode"}}UINode implementation{{/crossLink}} which provides a common 
  *   graphical {{#crossLink "GINode"}}nodes{{/crossLink}} representation.<br>
  *   
  *   Another possible <code>{{#crossLink "UINode/render:method"}}rendering{{/crossLink}}</code> criterion can be the type of the <code>{{#crossLink "GINode"}}node{{/crossLink}}</code>
  *   <code>{{#crossLink "DABSource"}}source{{/crossLink}}</code>. Since the <code>{{#crossLink "DABSource"}}source{{/crossLink}}</code> type information is not 
  *   provided by the <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> nor by the <code>{{#crossLink "Report"}}report{{/crossLink}}</code>, the 
  *   <code>{{#crossLink "UINode/isRenderable:method"}}{{/crossLink}}</code> method must be somehow able to obtain it.<br>
  *   For example, this API provides the {{#crossLink "GBIF_UINode"}}{{/crossLink}} which renders in a particular way <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> provided 
  *   by the <code>{{#crossLink "DABSource"}}{{/crossLink}}</code> of type <i>GBIF</i>. In this case the {{#crossLink "GBIF_UINode"}}{{/crossLink}} 
  *   <code>{{#crossLink "UINode/isRenderable:method"}}is renderable{{/crossLink}}</code> if the <code>{{#crossLink "Report/id:property"}}report.id{{/crossLink}}</code> of the 
  *   {{#crossLink "GINode"}}node{{/crossLink}} contains the word "gbif"
  *   
  *   <h4><a style="color:#30418C" name="renderableNodes">Renderable nodes</a></h4>
  *
  *   The {{#crossLink "ResultSetLayout"}}{{/crossLink}} renders the <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> of the current 
  *    <code>{{#crossLink "Page"}}page{{/crossLink}}</code> scrolling the <a href='../classes/ResultSetLayout.html#uiNodes'>registered UI nodes</a>
  *   in search of a <code>{{#crossLink "UINode/isRenderable:method"}}renderable{{/crossLink}}</code> <code>UINode</code>.<br>
  *    
  *   To determine which <a href='../classes/ResultSetLayout.html#uiNodes'>registered UI node</a> class is {{#crossLink "UINode/isRenderable:method"}}able to render{{/crossLink}}
  *   the current <code>{{#crossLink "GINode"}}node{{/crossLink}}</code>, the {{#crossLink "ResultSetLayout"}}{{/crossLink}} applies these rules:<ol>
  *   <li>creates an <code>UINode</code> instance from the current <a href='../classes/ResultSetLayout.html#uiNodes'>registered class reference</a></li>
  *   <li>tests the <code>{{#crossLink "UINode/isRenderable:method"}}{{/crossLink}}</code> method with the current <code>{{#crossLink "GINode"}}node{{/crossLink}}</code></li><ul><li>if the test succeeds, the <code>UINode</code> is <code>{{#crossLink "UINode/render:method"}}rendered{{/crossLink}}</code> and inserted in the layout</li>
  *   <li>otherwise it proceeds with the next <code>UINode</code> class reference of the <a href='../classes/ResultSetLayout.html#uiNodes'>array</a></li></ul>
  *   <li>if the search fails, than the <a href='../classes/ResultSetLayout.html#commonUINode'>common UI node</a> is used</li>
  *   </ol>
  *   In the example of the <code>Green_UINode</code> and <code>Sea_UINode</code> the <code>{{#crossLink "UINode/isRenderable:method"}}{{/crossLink}}</code> method
  *   returns <code>true</code> if the <code>{{#crossLink "Report"}}node report{{/crossLink}}</code> has respectively  the word "green" in the 
  *  <code>{{#crossLink "Report/title:property"}}report title{{/crossLink}}</code> and the word "sea" in the 
  *  <code>{{#crossLink "Report/description:property"}}report description{{/crossLink}}</code>
  *  
  *  <h4><a style="color:#30418C" name="extendUINode">Extending the UINode</a></h4>
  *  
  *  A <code>UINode</code> includes by default a <code>&lt;section&gt;</code> and a <code>&lt;aside&gt;</code> elements; the functions
  *  <code>options.sectionDom</code> and <code>options.asideDom</code> return the DOM to insert in the correspondent elements. This class 
  *   provides an implementation which returns an empty <code>&lt;div&gt;</code>.<br> 
  *  This class also provides a default implementation of the <code>{{#crossLink "UINode/render:method"}}{{/crossLink}}</code> method which places the 
  *  <code>&lt;section&gt;</code> element on the left of the <code>&lt;aside&gt;</code> element with respectively the 85% and 15% of the 
  *  horizontal space, according to the <code>ui-node</code> class in the 
  *  <a href="https://api.geodab.eu/docs/assets/css/giapi.css">API CSS</a> file. If the <code>options.asideDom</code> function 
  *  returns an empty element, than all the horizontal space is assigned to the <code>&lt;section&gt;</code> element.<br>
  *  
  *  In the most part of the cases there is no need to overwrite the <code>{{#crossLink "UINode/render:method"}}{{/crossLink}}</code> method, 
  *  so providing an implementation of the <code>options.sectionDom</code>, <code>options.asideDom</code> and of the 
  *  <code>{{#crossLink "UINode/isRenderable:method"}}renderable{{/crossLink}}</code> method is enough.<br>
  *  The code snippet below shows how to use this approach to create the <code>Green_UINode</code> and <code>Sea_UINode</code> classes:
  *  <pre><code>// creates the Green_UINode class
var Green_UINode = function(options) {<br>
	// creates an instance of the UINode superclass
	var uiNode = GIAPI.UINode(options);<br>
	// provides an implementation of the options.sectionDom function
	// the options.asideDom function is not implemented so the section 
	// takes 100% of the horizontal space
	options.sectionDom = function(node,options,sectionId){<br>
	    // creates the div with a green background
          var sectionDiv = '&lt;div style="background: green"&gt;';
          // adds other content to the div                 	 
          sectionDiv += '...';
          // closes the div	 
          sectionDiv += '&lt;/div&gt;';<br>
          return sectionDiv;
	};<br>
	// overwrite the isRenderable method
	uiNode.isRenderable = function(node){<br>
	    // get the node report
		var report = node.report();
		// get the title
		var title = report.title();
		// return true if the title contains the word "green"
		return title.indexOf('green') >= 0;		
	};<br>
	// returns the reference to the extended UINode
	return uiNode;
}<br><br>// creates the Sea_UINode class
var Sea_UINode = function(options) {<br>
	// creates an instance of the UINode superclass
	var uiNode = GIAPI.UINode(options);<br>
	// provides an implementation of the options.sectionDom function
	options.sectionDom = function(node,options,sectionId){<br>
	    // creates the div
          var sectionDiv = '&lt;div&gt;';
          // adds other content to the div                 	 
          sectionDiv += '...';
          // closes the div	 
          sectionDiv += '&lt;/div&gt;';<br>
          return sectionDiv;
	};<br>
	// provides an implementation of the options.asideDom function
	options.asideDom = function(node,options,sectionId){<br>
	    // creates a div with an image of the sea as background
          var asideDiv = '&lt;div style="background-image: url("sea-image.png")" &gt;';         
          // closes the div	 
          asideDiv += '&lt;/div&gt;';<br>
          return asideDiv;
	};<br>
	// overwrite the isRenderable method
	uiNode.isRenderable = function(node){<br>
	    // get the node report
		var report = node.report();
		// get the description
		var title = report.description();
		// return true if the description contains the word "sea"
		return description && description.indexOf('sea') >= 0;		
	};<br>
	// returns the reference to the extended UINode
	return uiNode;
}
  *  </pre></code>
  *  
  *  If necessary, the {{#crossLink "UINode/render:method"}}{{/crossLink}} method can be extended in the same way as the {{#crossLink "UINode/isRenderable:method"}}{{/crossLink}} 
  *  method in the code snippet above
  *  
  *  <h4><a style="color:#30418C" name="cratesLayout">Registering the <code>UINode</code> classes to the {{#crossLink "ResultSetLayout"}}{{/crossLink}}</a></h4>
  *
  *  The next step is to create a {{#crossLink "ResultSetLayout"}}{{/crossLink}} and <a href='../classes/ResultSetLayout.html#uiNodes'>register</a> 
  *  the just created UI nodes. The following code snippet shows how to do it:
  *  
  *  <pre><code>var layoutId = GIAPI.random();
  *  // creates the layout
  *  var layout = GIAPI.ResultSetLayout(layoutId,{<br>
  *     // registers the classes reference to the layout
  *     'uiNodes': [ Green_UINode, Sea_UINode ]
  *  });
  *  </pre></code>
  *  
  *  @param {Object} [options] <a name="uiNodeOpt">as well as the following properties</a>, all the <a href="../classes/ResultSetLayout.html#resSetLayOptions">options</a> of the {{#crossLink "ResultSetLayout"}}{{/crossLink}} are provided. This allows for example 
  *  to {{#crossLink "UINode/render:method"}}{{/crossLink}} the node in a compact way in case the <a href="../classes/ResultSetLayout.html#columnCount">column count</a> is greater than one. 
  *  Furthermore a particular implementation of this class could require other properties that thus can be provided by setting them in the 
  *  <a href="../classes/ResultSetLayout.html#resSetLayOptions">ResultSetLayout options</a> (see <a href="../classes/GBIF_UINode.html#gbifNodeProp">GBIF_UINode properties</a> as example)     
  *  
  *  @param {Function} [options.sectionDom] returns the DOM of the <code>&lt;section&gt;</code> element
  *  @param {Function} [options.asideDom] returns the DOM of the <code>&lt;aside&gt;</code> element
  *  
  *  @param {Function} [options.onSectionReady] callback function called when <code>&lt;section&gt;</code> element is ready
  *  @param {Function} [options.onAsideReady] callback function called when <code>&lt;aside&gt;</code> element is ready
  *  
  *  @constructor 
  *  @class UINode
  */
GIAPI.UINode = function(options) {

	var uiNode = {};
	
	if(!options){
		options = {};
	}
	
	options.sectionDom = function(node, options, sectionId){
         	
		return '<div></div>';
 	};

	options.asideDom = function(node, options, asideId){
         	
		return '<div></div>';
 	};
 	
 	options.onAsideReady = function(aside,node){	  
	};
	
	options.onSectionReady = function(aside,node){	  
	};
			
	/**
	 * Renders this <code>UINode</code> with the given <code>{{#crossLink "GINode"}}node{{/crossLink}}</code>.<br>
	 * This implementation creates a component constituted by a <code>&lt;section&gt;</code> and a <code>&lt;aside&gt;</code> 
	 * elements according to the <code>options.sectionDom</code> and <code>options.asideDom</code>.<br>
	 * When an element is appended to the DOM, the correspondent event function (<code>options.sectionDom</code> or <code>options.asideDom</code>) is called.<br>
	 * In most part of the cases, there is no need to override this method since the implementation of the <code>options.sectionDom</code> 
	 * and <code>options.asideDom</code> functions is enough.<br>
	 * For more info see <a href="#extendUINode">this section</a>.
	 * 
	 * @param {GINode} node the {{#crossLink "GINode"}}node{{/crossLink}} to use for rendering
     * @param {Integer} rowNumber the number of the current row of the {{#crossLink "ResultSetLayout"}}{{/crossLink}}
     * @param {String} rowId the identifier of the current row of the {{#crossLink "ResultSetLayout"}}{{/crossLink}}
     * @param {Integer} colNumber the number of the current column of the {{#crossLink "ResultSetLayout"}}{{/crossLink}}
     * @param {String} colId the identifier of the current column of the {{#crossLink "ResultSetLayout"}}{{/crossLink}}; the 
     * <code>&lt;section&gt;</code> and a <code>&lt;aside&gt;</code> elements are appended to this column
     * 
	 * @method render
	 */
	uiNode.render = function(node, rowNumber, rowId, colNumber, colId){
		
		var sectionId = 'section-'+node.uiId;
		var section = '<section class="ui-node-section" id="'+sectionId+'"></section>';
				
		// inserts the section and the aside
		jQuery('#'+colId).append(section);
	      
     	// creates the section dom
   	    var sectionDom = options.sectionDom(node,options,sectionId);
       	      
        // inserts the section dom
        jQuery('#' + sectionId).append(sectionDom);
        
        if(options.onSectionReady){
            // notifies the event
        	options.onSectionReady.apply(this,[jQuery('#' + sectionId),node ]);
        }
                                                                                          
        // creates the aside dom
        var asideDom = options.asideDom(node,options,asideId);
              
        if(!asideDom){
        	 // if not aside dom the section takes the 100% width 
            jQuery('#' + sectionId).css('width','100%');
        }else{        
        	
    		var asideId = 'aside-'+node.uiId;
    		var aside = '<aside class="ui-node-aside" id="'+asideId+'"></aside>';

    		jQuery('#'+colId).append(aside);
        	
	        // inserts the aside
	        jQuery('#' + asideId).append(asideDom);
	        
	        if(options.onAsideReady){
	            // notifies the event
	        	options.onAsideReady.apply(this,[ jQuery('#' + asideId), node ]);
	        }
        }
	};
	
	/**
	 * Return <code>true</code> if the given <code>{{#crossLink "GINode"}}node{{/crossLink}}</code> is renderable by this <code>UINode</code> instance, 
	 * <code>false</code> otherwise.<br> 
	 * This implementation returns always <code>true</code>.
	 * For more info see <a href="#renderableNodes">this section</a>.
	 * 
	 * @method isRenderable
	 * @param {GINode} node the {{#crossLink "GINode"}}node{{/crossLink}} to use for rendering
	 * 
	 * @return returns <code>true</code>
	 */
	uiNode.isRenderable = function(node){	
		
		return true;
	};
	         
	return uiNode;
};
	