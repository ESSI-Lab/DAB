/**
 * @module UI
 **/

/**
 * This control is based on the <a href="http://developer.yahoo.com/yui/treeview/" target="blank">YUI TreeView Control</a> version 2.9.0 (scripts are included in the minified version of the API) 
 * and provides a hierarchical representation of a {{#crossLink "DAB"}}DAB node{{/crossLink}} (the root of the tree).<br>
 * 
 * <ul style="margin-left:-25px;"><li> Required CSS:</li></ul>
 * <pre><code>
  &lt;link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.9.0/build/fonts/fonts-min.css" /&gt;
  &lt;link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.9.0/build/treeview/assets/skins/sam/treeview.css" /&gt;<br> 
 * </code></pre> 
 * 
 * When a <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> is expanded, the method
 *  {{#crossLink "GINode/expand:method"}}{{/crossLink}} is called on the correspondent {{#crossLink "GINode"}}{{/crossLink}} available in the <code>data</code> property of the 
 *     expanded <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a>
 * (if the {{#crossLink "GINode"}}{{/crossLink}} is {{#crossLink "Report/type:property"}}simple{{/crossLink}} the <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> is not 
 * expanded). The maximum number of <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">nodes</a> that can be retrieve at each call of the {{#crossLink "GINode/expand:method"}}{{/crossLink}} method
 * can be set with <code>options.pageSize</code> or by means of the {{#crossLink "GINodeTree/pageSize:method"}}pageSize{{/crossLink}} method (the default is 10). If the expanded
 *  <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> has more than {{#crossLink "GINodeTree/pageSize:method"}}pageSize{{/crossLink}} children, <a name="expandNext">a special tree node</a>
 *  is created with the title "Click to get more results..."; by expanding this node the {{#crossLink "GINode/expandNext:method"}}{{/crossLink}} method 
 * is called on the correspondent parent {{#crossLink "GINode"}}{{/crossLink}}.     
 *  
 * <img src="../assets/img/ginodetree-example.png"></img>
 * 
 * As depicted in the image above (the test page is available <a href="http://development.eurogeoss-broker.eu/gi-api-demo/ginode-tree-test.html" target=_blank>here</a>) each {{#crossLink "GINode"}}{{/crossLink}} is rendered using its 
 * {{#crossLink "Report/title:property"}}report title{{/crossLink}}; rendering and/or functionalities customization (e.g. icons visualization besides the title)
 *  can be done with the <code>options.onCreateNode</code> and <code>options.onLoadComplete</code> functions (see also {{#crossLink "GINodeTree/onCreateNode:method"}}{{/crossLink}} and 
 * {{#crossLink "GINodeTree/onLoadComplete:method"}}{{/crossLink}} methods)
 * 
 * 
 * @class GINodeTree
 * @constructor
 * @param {DAB} dabNode the {{#crossLink "DAB"}}DAB node{{/crossLink}} which is the root of tree hierarchy
 * @param {String} divId the id of the &lt;div&gt; in which to insert the <a href="http://developer.yahoo.com/yui/treeview/" target="blank">tree</a>
 * 
 * @param {Object} [options] object literal of optional properties
 * 
 * @param {Function} [options.onCreateNode] callback function called to when a new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> is created; this function 
 * can be used to customize the new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> aspect and/or functionalities. 
 * See <a href="GINodeTree.html#method_onCreateNode">here</a> for the default implementation
 * @param {<a href="GINode.html" target="blank">GINode</a>} options.onCreateNode.giNode the <a href="GINode.html" target="blank">GINode</a> to render
 * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a>} options.onCreateNode.parentTreeNode the parent <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a>
 * @param {boolean} options.onCreateNode.expandNext if <code>true</code> the <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">node</a> to create is the 
 *  <a href="#expandNext">special node</a> which retrieves the <a href="GINode.html#method_expandNext" target="blank">next nodes</a> 
 * of the <a href="GINode.html" target="blank">GINode</a> related to <code>parentTreeNode</code> 
 * @param {boolean} options.onCreateNode.isRoot if <code>true</code> the <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">node</a> to create is the root of the 
 * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.GITreeView.html" target="blank">tree</a> <div style="margin-left: -25px"><br> <b>Returns:</b> 
 * a string or object containing the data that will be used to render the new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a></div>
 * 
 * @param {Function} [options.onLoadComplete] callback function called when all the expanded <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">node</a> children have been rendered 
 * (see also {{#crossLink "GINodeTree/onLoadComplete:method"}}{{/crossLink}} method)   
 * @param {Page} options.onLoadComplete.resultPage the <a href="Page.html" target="blank">page</a> resulting from the <a href="GINode.html#method_expand" target="blank">expansion</a> 
 * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a> [[]]} options.onLoadComplete.treeNodes 
 * array of rendered <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree nodes</a>. Each 
 * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> provides the correspondent <a href="GINode.html" target="blank">GINode</a> in the <code>data</code> property
 * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a>} options.onLoadComplete.moreTreeNode 
 * special <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> rendered with the title "Click to get more results..." which allows to 
 * retrieve the next page of nodes of the correspondent parent <a href="GINode.html" target="blank">GINode</a> that can be retrieved in the <code>data</code> property
 * 
 * @param {Function} [options.onError] callback function called if an error occurs during the execution of the <a href="GINode.html#method_expand" target="blank">expand</a> method on the target 
 * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a>
 * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a>} options.onError.treeNode the 
 *  target <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> related <a href="GINode.html#method_expand" target="blank">GINode</a>
 * @param {String} options.onError.error the error message
 * 
 * @param {Integer} [options.pageSize] the maximum size of the returned {{#crossLink "GINode/expand:method"}}expansion{{/crossLink}} {{#crossLink "Page"}}page{{/crossLink}}, default is 10
 * (see also {{#crossLink "GINodeTree/pageSize:method"}}{{/crossLink}} method)
 *
 **/
GIAPI.GINodeTree = function(dabNode, divId, options) {
	
	if(!options){
		options = {};
	}
	
	if(!options.expandRoot){
		options.expandRoot = false;
	}
    
    var _onLoadComplete = options.onLoadComplete;
    var createNodeData = function(id, giNode, parentTreeNode, expandNext, isRoot){
        
         var oData  = '<label expand="false" id="'+id+'" class="ygtvcontent-label">'+giNode.report().title+'</label>';
         if(expandNext){
            oData  = '<label expand="true" id="'+id+'" class="ygtvcontent-label">Click to get more results...</label>';    
         }else if(isRoot){
             // as normal node
         }
         return oData;
    };

    var _pageSize = options.pageSize ? options.pageSize : 10;
    
    var loadNodeData = function(prtTreeNode, fnLoadComplete) {
    	
        var callback = function(resultSet) {
        	         	
        	var page = resultSet.page;
            var nodes = resultSet.error ? [] : page.nodes();
            var treeNodes = [];
            var moreTreeNode;
            
            for (var i = 0; nodes && i < nodes.length; i++) {

                var curNode = nodes[i];            
                var type = curNode.report().type;
                
                var id = GIAPI.random();
                var oData = createNodeData(id, curNode,prtTreeNode,false,false);  
                if(options.onCreateNode){
                	oData = options.onCreateNode.apply(this,[id, oData,curNode, prtTreeNode, false, false]);
                }
                 
                var tmpTreeNode = new YAHOO.widget.HTMLNode(oData, prtTreeNode, false, true);
                tmpTreeNode.data = curNode;
                tmpTreeNode.isLeaf = type === 'simple';
                treeNodes.push(tmpTreeNode);

                if (i === nodes.length - 1 && curNode._parent && curNode._parent.expandNext()) {
                	                    
                	var id = GIAPI.random();
                    var oData = createNodeData(id, curNode,prtTreeNode,true,false);  
                    if(options.onCreateNode){
                     	oData = options.onCreateNode.apply(this,[id, oData, null, prtTreeNode, true, false]);
                     }
                    
                    moreTreeNode = new YAHOO.widget.HTMLNode(oData, prtTreeNode, false, true);
                    moreTreeNode.data = curNode._parent;
                    moreTreeNode.isLeaf = false;
                    moreTreeNode._expandNext = true;                    
                }
            }
             
            if (resultSet.error && options.onError) {
               options.onError.apply(this, [prtTreeNode, resultSet.error]);
            }

            prtTreeNode.loadComplete();
            
            if (nodeTree.nodeToRemove) {
                nodeTree.tree.removeNode(nodeTree.nodeToRemove, true);
                nodeTree.nodeToRemove = null;
            }

            if (_onLoadComplete) {
                _onLoadComplete.apply(this, [page, treeNodes, moreTreeNode]);
            }
        };

        if (prtTreeNode._expandNext) {
            prtTreeNode.data.expandNext(callback,true);
            nodeTree.nodeToRemove = prtTreeNode;
                    
        } else {
            prtTreeNode.data.expand(callback,_pageSize);
        }
    };

    if (!options || !options.onError) {
        onError = function(errorNode, error) {

            errorNode.label = errorNode.label + ' (' + error + ')';
            nodeTree.tree.draw();
        };
    }
    
    var nodeTree = {};

    /**
     * The <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.TreeView.html" target="blank">tree</a> which renders the 
     *  {{#crossLink "DAB"}}DAB node{{/crossLink}} hierarchical structure
     * 
     * @property tree 
     * @type {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.TreeView.html" target="blank">YAHOO.widget.TreeView</a>}   
     */
    nodeTree.tree = new YAHOO.widget.TreeView(divId);
    nodeTree.tree.setDynamicLoad(loadNodeData);
    
    // disables the click event, so the click on a node do not expands it.
    // a node can be expanded only with the + and - buttons
    nodeTree.tree.subscribe('clickEvent', function(node) {

    	 return node.node._expandNext ? true:false;   
    });
    
    /**
     * The root of the <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.TreeView.html" target="blank">tree</a> with the {{#crossLink "DAB"}}DAB node{{/crossLink}} 
     * in the <code>data</code> property 
     *
     * @property treeRoot 
     * @type {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a>}  
     */
    var oData = createNodeData(GIAPI.random(),dabNode, nodeTree.tree.getRoot(), false, true); 
    nodeTree.treeRoot = new YAHOO.widget.HTMLNode(oData, nodeTree.tree.getRoot(), options.expandRoot, true);
    nodeTree.treeRoot.data = dabNode;

    nodeTree.tree.draw();
         
    return nodeTree;
};