/**
 * @module Core
 **/

/**
 *
 * Enumeration of available Spatial Relations.
 *
 * See also <a href="../classes/DAB.html#where">DAB discover 'where constraint'</a>.<br>
 *
 * @class SpatialRelation
 * @static
 **/
import { GIAPI } from './GIAPI.js';

GIAPI.SpatialRelation = {

	/**
	 * The <code>CONTAINS</code> relation
	 *
	 * @property CONTAINS
	 * @type {String}
	 *
	 */
	CONTAINS : "contains",

	/**
	 * The <code>OVERLAPS</code> relation
	 *
	 * @property OVERLAPS
	 * @type {String}
	 *
	 */
	OVERLAPS : "overlaps",
	
	/**
	 * The <code>DISJOINT</code> relation
	 *
	 * @property DISJOINT
	 * @type {String}
	 *
	 */
	DISJOINT : "disjoint"

};

