/**
 * @module Concept 
 */

/**
 * This class provides possible {{#crossLink "Relation"}}relations{{/crossLink}} which can be used to <a href="../classes/DAB.html#extend">extend a discover</a>
 * or to {{#crossLink "Concept/extend:method"}}extend a concept{{/crossLink}}
 *
 * @class Relation
 * @static
 **/

import { GIAPI } from '../core/GIAPI.js';


GIAPI.Relation = {
    /**
     *
     * No Relation.
     *
     * @property {Object} NONE
     * @property {String} NONE.name Human readable value of this {{#crossLink "Relation"}}relation{{/crossLink}}
     * @property {String} NONE.value URI representation value of this {{#crossLink "Relation"}}relation{{/crossLink}}, this value is URL-encoded
     **/
    "NONE" : {
        "name" : "None",
        "value" : "none"
    },

    /**
     *
     * More specific relation between two {{#crossLink "Concept"}}concepts{{/crossLink}} belonging to the same Controlled Vocabulary.
     * A formal definition of this relation can be found <a href="http://www.w3.org/2009/08/skos-reference/skos.html#narrower"  target="_blank">here</a>
     *
     * @property {Object} NARROWER
     * @property {String} NARROWER.name Human readable value of this {{#crossLink "Relation"}}relation{{/crossLink}}
     * @property {String} NARROWER.value URI representation value of this {{#crossLink "Relation"}}relation{{/crossLink}}, this value is URL-encoded
     **/
    "NARROWER" : {
        "name" : "Narrower",
        "value" : "skos%3Anarrower"
    },

    /**
     *
     * More general relation between two {{#crossLink "Concept"}}concepts{{/crossLink}} belonging to the same Controlled Vocabulary.
     * A formal definition of this relation can be found <a href="http://www.w3.org/2009/08/skos-reference/skos.html#broader"  target="_blank">here</a>
     *
     * @property {Object} BROADER
     * @property {String} BROADER.name Human readable value of this {{#crossLink "Relation"}}relation{{/crossLink}}
     * @property {String} BROADER.value URI representation value of this {{#crossLink "Relation"}}relation{{/crossLink}}, this value is URL-encoded
     **/
    "BROADER" : {
        "name" : "Broader",
        "value" : "skos%3Abroader"
    },

    /**
     *
     * More specific relation between two {{#crossLink "Concept"}}concepts{{/crossLink}} belonging to different Controlled Vocabularies.
     * A formal definition of this relation can be found <a href="http://www.w3.org/2009/08/skos-reference/skos.html#narrowMatch"  target="_blank">here</a>
     *
     * @property {Object} NARROW_MATCH
     * @property {String} NARROW_MATCH.name Human readable value of this {{#crossLink "Relation"}}relation{{/crossLink}}
     * @property {String} NARROW_MATCH.value URI representation value of this {{#crossLink "Relation"}}relation{{/crossLink}}, this value is URL-encoded
     **/
    "NARROW_MATCH" : {
        "name" : "Narrow match",
        "value" : "skos%3AnarrowMatch"
    },

    /**
     *
     * More general relation between two {{#crossLink "Concept"}}concepts{{/crossLink}} belonging to different Controlled Vocabularies.
     * A formal definition of this relation can be found <a href="http://www.w3.org/2009/08/skos-reference/skos.html#broadMatch"  target="_blank">here</a>
     *
     * @property {Object} BROAD_MATCH
     * @property {String} BROAD_MATCH.name Human readable value of this {{#crossLink "Relation"}}relation{{/crossLink}}
     * @property {String} BROAD_MATCH.value URI representation value of this {{#crossLink "Relation"}}relation{{/crossLink}}, this value is URL-encoded
     **/
    "BROAD_MATCH" : {
        "name" : "Broad match",
        "value" : "skos%3AbroadMatch",
    },
    
    
    /**
     *
     * This relation indicates that two {{#crossLink "Concept"}}concepts{{/crossLink}} belonging to different Controlled Vocabularies are similar and, in some applications,they can be used interchangeably.
     * A formal definition of this relation can be found <a href="http://www.w3.org/2009/08/skos-reference/skos.html#closeMatch"  target="_blank">here</a>
     *
     * @property {Object} CLOSE_MATCH
     * @property {String} CLOSE_MATCH.name Human readable value of this {{#crossLink "Relation"}}relation{{/crossLink}}
     * @property {String} CLOSE_MATCH.value URI representation value of this {{#crossLink "Relation"}}relation{{/crossLink}}, this value is URL-encoded
     **/
    "CLOSE_MATCH" : {
        "name" : "Close match",
        "value" : "skos%3AcloseMatch"
    },


    /**
     *
     * This relation is used to assert an associative link between two {{#crossLink "Concept"}}concepts{{/crossLink}} belonging to the same Controlled Vocabulary.
     * A formal definition of this relation can be found <a href="http://www.w3.org/2009/08/skos-reference/skos.html#related"  target="_blank">here</a>
     *
     * @property {Object} RELATED
     * @property {String} RELATED.name Human readable value of this {{#crossLink "Relation"}}relation{{/crossLink}}
     * @property {String} RELATED.value URI representation value of this {{#crossLink "Relation"}}relation{{/crossLink}}, this value is URL-encoded
     **/
    "RELATED" : {
        "name" : "Related",
        "value" : "skos%3Arelated",
    },


    /**
     *
     * This relation is used to assert an associative link between two {{#crossLink "Concept"}}concepts{{/crossLink}} belonging to different Controlled Vocabularies.
     * A formal definition of this relation can be found <a href="http://www.w3.org/2009/08/skos-reference/skos.html#relatedMatch"  target="_blank">here</a>
     *
     * @property {Object} RELATED_MATCH
     * @property {String} RELATED_MATCH.name Human readable value of this {{#crossLink "Relation"}}relation{{/crossLink}}
     * @property {String} RELATED_MATCH.value URI representation value of this {{#crossLink "Relation"}}relation{{/crossLink}}, this value is URL-encoded
     **/
    "RELATED_MATCH" : {
        "name" : "Related match",
        "value" : "skos%3ArelatedMatch"
    },


    /**
     *
     * This relation is used is used to link two {{#crossLink "Concept"}}concepts{{/crossLink}}, indicating a high degree of confidence that these can be used interchangeably across a wide range of information retrieval applications.
     * A formal definition of this relation can be found <a href="http://www.w3.org/2009/08/skos-reference/skos.html#exactMatch"  target="_blank">here</a>
     *
     * @property {Object} EXACT_MATCH
     * @property {String} EXACT_MATCH.name Human readable value of this {{#crossLink "Relation"}}relation{{/crossLink}}
     * @property {String} EXACT_MATCH.value URI representation value of this {{#crossLink "Relation"}}relation{{/crossLink}}, this value is URL-encoded
     **/
    "EXACT_MATCH" : {
        "name" : "Exact match",
        "value" : "skos%3AexactMatch",
    },
    
    /**
     * Returns the {{#crossLink "Relation"}}{{/crossLink}} instance having the provided <code>value</code>.<br>
     * An <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown if <code>value</code> 
     * is unknown
     * 
     * @param {String} value the {{#crossLink "Relation"}}{{/crossLink}} value
     * @return the <a href="Relation.html" target=_blank>Relation</a> instance having the provided <code>value</code>
     * @method decode
     */
    decode: function(value){
        
        switch(value){
            case this.NONE.value: return this.NONE;
            case this.NARROWER.value: return this.NARROWER;
            case this.BROADER.value: return this.BROADER;
            case this.NARROW_MATCH.value: return this.NARROW_MATCH;
            case this.BROAD_MATCH.value: return this.BROAD_MATCH;
            case this.CLOSE_MATCH.value: return this.CLOSE_MATCH;
            case this.RELATED.value: return this.RELATED;
            case this.RELATED_MATCH.value: return this.RELATED_MATCH;
            case this.EXACT_MATCH.value: return this.EXACT_MATCH;
        }
        
        throw "Unable to decode "+value;
    }
};