/**
 * @module UI
 **/

/**
 * This control is based on the <a href="http://developer.yahoo.com/yui/treeview/" target="blank">YUI TreeView Control</a> version 2.9.0 (scripts are included in the minified version of the API) 
 * and provides a hierarchical representation of the {{#crossLink "DAB/concept:method"}}DAB concept method{{/crossLink}}.<br>
 *
 * <ul style="margin-left:-25px;"><li> Required CSS:</li></ul>
 * <pre><code>
 &lt;link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.9.0/build/fonts/fonts-min.css" /&gt;
 &lt;link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.9.0/build/treeview/assets/skins/sam/treeview.css" /&gt;<br>
 * </code></pre>
 *
 * When a <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> is expanded, the method
 *  {{#crossLink "Concept/extend:method"}}{{/crossLink}} is called on the correspondent {{#crossLink "Concept"}}{{/crossLink}} (available in the <code>data</code> property).
 *
 * <img src="../assets/img/conceptree-example.png"></img>
 *
 * As depicted in the image above (the test page is available <a href="http://development.eurogeoss-broker.eu/gi-api-demo/concept-tree-test.html" target=_blank>here</a>) each {{#crossLink "Concept"}}{{/crossLink}} is rendered using its
 * {{#crossLink "Concept/uri:method"}}{{/crossLink}}; rendering customization (e.g. icons visualization besides the {{#crossLink "Concept/uri:method"}}{{/crossLink}})
 *  can be done with the <code>options.onCreateNode</code> and <code>options.onLoadComplete</code> functions (see also {{#crossLink "ConceptTree/onCreateNode:method"}}{{/crossLink}} and
 * {{#crossLink "ConceptTree/onLoadComplete:method"}}{{/crossLink}} methods)
 *
 * @class ConceptTree
 * @constructor
 * @param {DAB} dabNode the {{#crossLink "DAB"}}DAB node{{/crossLink}} on which the {{#crossLink "DAB/concept:method"}}concept method{{/crossLink}} is called
 * in order to generate {{#crossLink "Concept"}}concepts{{/crossLink}} related to the given <code>keyword</code>
 * @param {String} id id of an existent HTML container (typically <code>&lt;div&gt;</code> element) in which the tree is inserted
 *
 * @param {Object} [options] object literal of optional properties
 *
 * @param {Function} [options.onCreateNode] callback function called when a new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> is created; this function
 * can be used to customize the new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> aspect and/or functionalities.
 * See <a href="ConceptTree.html#method_onCreateNode">here</a> for the default implementation
 * @param {<a href="Concept.html" target="blank">Concept</a> \ <a href="DAB.html" target="blank">DAB</a>} options.onCreateNode.element the element to render:
 * <a href="DAB.html" target="blank">DAB node</a> if the <code>isRoot</code> parameter is <code>true</code>, a <a href="Concept.html" target="blank">Concept</a> otherwise
 * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a>} options.onCreateNode.parentTreeNode the parent <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a>
 * @param {boolean} options.onCreateNode.isRoot if <code>true</code> the <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">node</a> to create is the root of the
 * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.GITreeView.html" target="blank">tree</a> (the <code>element</code> parameter is the <a href="DAB.html" target="blank">DAB node</a>), <code>false</code>
 * otherwise (the <code>element</code> parameter is a <a href="Concept.html#method_onCreateNode">Concept</a>)
 *  <div style="margin-left: -25px"><br> <b>Returns:</b> a string or object containing the data that will be used to render the new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a></div>
 *
 * @param {String} [options.keyword] the keyword by which {{#crossLink "DAB/concept:method"}}generate{{/crossLink}} the related {{#crossLink "Concept"}}concepts{{/crossLink}}. If omitted
 * it can be set with the {{#crossLink "ConceptTree/conceptKeyword:method"}}{{/crossLink}} method
 * @param {Function} [options.onLoadComplete] callback function called when all the expanded <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">node</a>
 *  children have been rendered. If omitted it can be set with the {{#crossLink "ConceptTree/onLoadComplete:method"}}{{/crossLink}} method

 * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a> [[]]} options.onLoadComplete.treeNodes
 * array of rendered <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree nodes</a>. Each
 * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> provides the correspondent <a href="Concept.html" target="blank">Concept</a>
 *  in the <code>data</code> property

 * @param {Function} [options.onError] this function is called if an error occurs during the execution of the <a href="Concept.html#method_extend" target="blank">extend</a> method on the target
 * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a>
 *
 * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a>} options.onError.treeNode the
 *  target <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a>; the related <a href="Concept.html" target="blank">Concept</a> is available
 *  in the <code>data</code> property
 * @param {String} options.onError.error the error message
 *
 **/
GIAPI.ConceptTree = function(dabNode, id, options) {

    var conceptKWD = options && options.keyword;
    var _onLoadComplete = options && options.onLoadComplete;
    var _onCreateNode = options && options.onCreateNode ? options.onCreateNode : function(element, parentTreeNode, isRoot) {

        var oData = null;
        if (isRoot) {
            // element is the DAB node
            oData = element.report().title;
        } else {
            // element is a Concept
            oData = element.uri();
        }
        return oData;
    };

    var loadNodeData = function(prtTreeNode, fnLoadComplete) {

        var callback = function(concepts, error) {

            var treeNodes = [];
            for (var i = 0; i < concepts.length; i++) {

                var curConcept = concepts[i];
                var uri = curConcept.uri();

                var oData = _onCreateNode(curConcept, prtTreeNode, false);
                var tmpTreeNode = new YAHOO.widget.HTMLNode(oData, prtTreeNode, false, true);

                tmpTreeNode.data = curConcept;
                tmpTreeNode.isLeaf = false;
                treeNodes.push(tmpTreeNode);
            }

            if (error) {
                onError.apply(this, [prtTreeNode, error]);
            }

            prtTreeNode.loadComplete();

            if (_onLoadComplete) {
                _onLoadComplete.apply(this, [treeNodes]);
            }
        };

        var data = prtTreeNode.data;
        if (data.extend) {
            if (prtTreeNode.relation) {
                data.extend(callback, prtTreeNode.relation);
            } else {
                data.extend(callback);
            }
        } else {
            data.concept(conceptKWD, callback);
        }
    };

    if (options && options.onError) {
        onError = options.onError;
    } else {
        onError = function(errorNode, error) {

            errorNode.label = errorNode.label + ' (' + error + ')';
            conceptTree.tree.draw();
        };
    }

    var conceptTree = {};

    /**
     * The <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.TreeView.html" target="blank">tree</a> which renders the
     *  {{#crossLink "DAB/concept:method"}}DAB concept method{{/crossLink}} hierarchical structure
     *
     * @property tree
     * @type {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.TreeView.html" target="blank">YAHOO.widget.TreeView</a>}
     */
    conceptTree.tree = new YAHOO.widget.TreeView(id);
    conceptTree.tree.setDynamicLoad(loadNodeData);

    /**
     * The root of the <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.TreeView.html" target="blank">tree</a> with the {{#crossLink "DAB"}}DAB node{{/crossLink}}
     * in the <code>data</code> property
     *
     * @property treeRoot
     * @type {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a>}
     */
    var oData = _onCreateNode(dabNode, conceptTree.tree.getRoot(), true);
    conceptTree.treeRoot = new YAHOO.widget.HTMLNode(oData, conceptTree.tree.getRoot(), false, true);
    conceptTree.treeRoot.data = dabNode;

    conceptTree.tree.draw();

    /**
     * Get and optionally set the keyword by which to {{#crossLink "DAB/concept:method"}}generate{{/crossLink}}
     *  the related {{#crossLink "Concept"}}concepts{{/crossLink}}; the {{#crossLink "Concept"}}concepts{{/crossLink}} will be
     * rendered as <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree nodes</a>
     *  when the root <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> is expanded
     *
     * @method conceptKeyword
     * @param {String} keyword the keyword to use to {{#crossLink "DAB/concept:method"}}generates{{/crossLink}}
     *  the related {{#crossLink "Concept"}}concepts{{/crossLink}}
     * @return the keyword currently used to <a href="DAB.html#method_concept" target="blank">generate</a> the related <a href="Concept.html" target=_blank>concept</a>
     */
    conceptTree.conceptKeyword = function(keyword) {

        if (keyword) {
            conceptKWD = keyword;
        }
        return conceptKWD;
    };

    /**
     * Get and optionally set the callback function called when a new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> is created; this function
     * can be used to customize the new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> aspect and/or functionalities. The default implementation
     * is as follows:<pre><code>function(element, parentTreeNode, isRoot){
     var oData = null;
     if(isRoot){
     // element is the DAB node
     oData = element.report().title;
     }else{
     // element is a Concept
     oData = element.uri();
     }
     return oData;
     }
     * </code></pre>
     *
     * @method onCreateNode
     * @param {Function} [callback] callback function called when a new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> is created
     * @param {<a href="Concept.html" target="blank">Concept</a> \ <a href="DAB.html" target="blank">DAB</a>} callback.element the element to render:
     * <a href="DAB.html" target="blank">DAB node</a> if the <code>isRoot</code> parameter is <code>true</code>, a <a href="Concept.html" target="blank">Concept</a> otherwise
     * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a>} callback.parentTreeNode the parent <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a>
     * @param {boolean} callback.isRoot if <code>true</code> the <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">node</a> to create is the root of the
     * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.GITreeView.html" target="blank">tree</a> (the <code>element</code> parameter is the <a href="DAB.html" target="blank">DAB node</a>), <code>false</code>
     * otherwise (the <code>element</code> parameter is a <a href="Concept.html#method_onCreateNode">Concept</a>)
     *  <div style="margin-left: -25px"> <b>Returns:</b> a string or object containing the data that will be used to render the new <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a></div>
     * @return the current <code>onCreateNode</code> function
     */
    conceptTree.onCreateNode = function(callback) {

        if (callback) {
            _onCreateNode = callback;
        }
        return _onCreateNode;
    };

    /**
     * Get and optionally set the function called when all the expanded
     * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">node</a> children have been rendered
     *
     * @method onLoadComplete
     * @param {Function} [callback] callback function called when all the expanded <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">node</a> children have been rendered
     * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a> [[]]} callback.treeNodes
     * array of rendered <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree nodes</a>. Each
     * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a> provides the
     * correspondent {{#crossLink "Concept"}}{{/crossLink}} in the <code>data</code> property
     *
     * @return the current <code>onLoadComplete</code> function
     */
    conceptTree.onLoadComplete = function(callback) {

        _onLoadComplete = callback;
        return _onLoadComplete;
    };

    /**
     * Get and optionally set the {{#crossLink "Relation"}}relations{{/crossLink}} by which to {{#crossLink "Concept/extend:method"}}{{/crossLink}}
     * the <code>treeNode</code> related {{#crossLink "Concept"}}concept{{/crossLink}}.
     * An example of use of this method can be found <a href="http://development.eurogeoss-broker.eu/gi-api-demo/concept-tree-test.html" target=_blank>here</a>
     * where this method is used to set a single {{#crossLink "Relation"}}relation{{/crossLink}} selected by the user (see also the image above).<br>
     * See also {{#crossLink "Concept/extend:method"}}Concept extend method{{/crossLink}}
     *
     * @method relationExtension
     * @param {<a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">YAHOO.widget.HTMLNode</a>} treeNode the target
     * <a href="http://developer.yahoo.com/yui/docs/YAHOO.widget.HTMLNode.html" target="blank">tree node</a>
     * @param {<a href="Relation.html" target=_blank>Relation</a>/String [[]]} An array of
     * {{#crossLink "Relation"}}relations{{/crossLink}} or {{#crossLink "Relation"}}relations{{/crossLink}} values used
     * to extend the <code>treeNode</code> related {{#crossLink "Concept"}}concept{{/crossLink}}
     * @return the <a href="Relation.html" target=_blank>relation</a> currently used to extend the <code>treeNode</code> related <a href="Concept.html" target=_blank>concept</a>
     */
    conceptTree.relationExtension = function(treeNode, relation) {

        if (relation) {
            treeNode.relation = relation;
        }
        return treeNode.relation;
    };

    return conceptTree;
}; 