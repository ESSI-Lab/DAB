/**
 * @module Concept 
 */

/**
 * Enumeration of well known {{#crossLink "Property"}}concept property{{/crossLink}} names. This names can be 
 * used to retrieve {{#crossLink "Property"}}concept properties{{/crossLink}} with the 
 * {{#crossLink "Concept/property:method"}}Concept property{{/crossLink}} method
 * 
 * See also {{#crossLink "Property"}}{{/crossLink}}.<br>
 * See also {{#crossLink "Concept/property:method"}}Concept property{{/crossLink}} method
 * 
 * @class PropertyName
 * @static
 **/

import { GIAPI } from '../core/GIAPI.js';


GIAPI.PropertyName = {
    /**
     * The <code>SCOPE_NOTE</code> {{#crossLink "Property"}}property{{/crossLink}} name
     * 
     * @property SCOPE_NOTE
     * @type {String}
     * 
     */
    SCOPE_NOTE: "skos%3AscopeNote",
    
    /**
     * The <code>NOTE</code> {{#crossLink "Property"}}property{{/crossLink}} name
     * 
     * @property NOTE
     * @type {String}
     * 
     */
    NOTE: "skos%3Anote",
    
    /**
     * The <code>NOTATION</code> {{#crossLink "Property"}}property{{/crossLink}} name
     * 
     * @property NOTATION
     * @type {String}
     * 
     */
    NOTATION: "skos%3Anotation",
    
    /**
     * The <code>ALT_LABEL</code> {{#crossLink "Property"}}property{{/crossLink}} name
     * 
     * @property ALT_LABEL
     * @type {String}
     * 
     */
    ALT_LABEL: "skos%3AaltLabel",
    
    /**
     * The <code>CHANGE_NOTE</code> {{#crossLink "Property"}}property{{/crossLink}} name
     * 
     * @property CHANGE_NOTE
     * @type {String}
     * 
     */
    CHANGE_NOTE: "skos%3AchangeNote",
    
    /**
     * The <code>HIDDEN_LABEL</code> {{#crossLink "Property"}}property{{/crossLink}} name
     * 
     * @property HIDDEN_LABEL
     * @type {String}
     * 
     */
    HIDDEN_LABEL: "skos%3AhiddenLabel",
    
    /**
     * The <code>HISTORY_NOTE</code> {{#crossLink "Property"}}property{{/crossLink}} name
     * 
     * @property HISTORY_NOTE
     * @type {String}
     * 
     */
    HISTORY_NOTE: "skos%3AhistoryNote",
    
    /**
     * The <code>EDITORIAL_NOTE</code> {{#crossLink "Property"}}property{{/crossLink}} name
     * 
     * @property EDITORIAL_NOTE
     * @type {String}
     * 
     */
    EDITORIAL_NOTE: "skos%3AeditorialNote",
    
    /**
     * The <code>EXAMPLE</code> {{#crossLink "Property"}}property{{/crossLink}} name
     * 
     * @property EXAMPLE
     * @type {String}
     * 
     */
    EXAMPLE: "skos%3Aexample"
}; 