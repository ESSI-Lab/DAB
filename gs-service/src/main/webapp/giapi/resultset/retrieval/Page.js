/**
 * @module ResultSet
 * @submodule Retrieval
 **/

/**
 * A {{#crossLink "ResultSet"}}result set{{/crossLink}} page of {{#crossLink "GINode"}}nodes{{/crossLink}}
 *
 * @class Page
 **/

import { GIAPI } from '../../core/GIAPI.js';

GIAPI.Page = function(nodes, size) {

    if (nodes && !Array.isArray(nodes)) {
        throw 'nodes is not an array';
    }

    var index = 0;
    return {
        /**
         * Retrieves the next {{#crossLink "GINode"}}node{{/crossLink}} from this {{#crossLink "Page"}}page{{/crossLink}}
         *
         * @method next
         * @return {GINode}
         *
         * The next {{#crossLink "GINode"}}node{{/crossLink}} from this {{#crossLink "Page"}}page{{/crossLink}} or <code>null</code> if
         *  the last has already been returned (see also methods {{#crossLink "Page/hasNext:method"}}{{/crossLink}}
         * and {{#crossLink "Page/reset:method"}}{{/crossLink}})
         */
        next : function() {

            if (index === nodes.length) {
                return null;
            }

            return nodes[index++];
        },

        /**
         * Tests if one or more {{#crossLink "GINode"}}node{{/crossLink}} is available
         * @method hasNext
         *
         * @return {Boolean}
         * <code>true</code> if the next call of the {{#crossLink "Page/next:method"}}next method{{/crossLink}} will
         * return a {{#crossLink "GINode"}}{{/crossLink}}, <code>false</code> otherwise (see also {{#crossLink "Page/reset:method"}}reset method{{/crossLink}}))
         */
        hasNext : function() {

            return index < nodes.length;
        },

        /**
         * Retrieves the {{#crossLink "GINode"}}nodes{{/crossLink}} of this {{#crossLink "Page"}}page{{/crossLink}}
         * @method nodes
         *
         * @return {Array}
         *
         * Array of {{#crossLink "GINode"}}{{/crossLink}} of this {{#crossLink "Page"}}page{{/crossLink}} (an empty
         * array for an empty {{#crossLink "Page"}}page{{/crossLink}})
         */
        nodes : function() {

            return nodes;
        },

        /**
         * Retrieves the maximum number of {{#crossLink "GINode"}}nodes{{/crossLink}} that this {{#crossLink "Page"}}page{{/crossLink}} can contain.<br>
         * See also <a style="cursor: pointer" onclick="window.open('../classes/DAB.html#d_pageSize','_blank')">discover pageSize option</a>.<br>
         * See also <a style="cursor: pointer" onclick="window.open('../classes/GINode.html#pageSize','_blank')">expand pageSize option</a>
         * @method size
         *
         * @return {Integer}
         * An integer with the maximum number of {{#crossLink "GINode"}}nodes{{/crossLink}} that this {{#crossLink "Page"}}page{{/crossLink}} can contain
         */
        size : function() {

            return size || '10';
        },

        /**
         * Retrieves the number of {{#crossLink "GINode"}}nodes{{/crossLink}} of this {{#crossLink "Page"}}page{{/crossLink}}
         * @method count
         *
         * @return {Integer}
         * An integer with the number of {{#crossLink "GINode"}}nodes{{/crossLink}} contained in this {{#crossLink "Page"}}page{{/crossLink}}
         */
        count : function() {

            return nodes.length;
        },

        /**
         * Reset this page to its original state, as if the {{#crossLink "Page/next:method"}}next method{{/crossLink}} had never been called
         *
         * @method reset
         *
         */
        reset : function() {

            index = 0;
        }
    };
};
