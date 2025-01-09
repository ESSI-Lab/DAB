/**
 * @module ResultSet
 * @submodule Refinement
 **/
import { GIAPI } from '../../core/GIAPI.js';

/**
 * This object is <a href="../classes/ResultSet.html#resSetRef">optionally provided</a> as 
 * <a href="../classes/ResultSet.html#termFrequency">property of a result set</a>.<br>
 * A new instance is created every time a {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} is performed, and it is maintained with its 
 * {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}, until a new {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} is performed.<br>
 * 
 * The <code>Refiner</code> is initialized as follows:<ol>
 * <li>the <code>{{#crossLink "Refiner/cursor:method"}}{{/crossLink}}</code> value is 0</li><li>the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}<code>.length</code> is 1 and contains the <a href="../classes/DAB.html#constraints">constraints</a> and the 
 * <a href="../classes/DAB.html#options">options</a> of the origin {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}</li>
 * </ol>
 * 
 * The <code>Refiner</code> provides the following main features:<ul>
 * <li>allows to {{#crossLink "Refiner/refine:method"}}refine{{/crossLink}} the related
 * {{#crossLink "ResultSet"}}result set{{/crossLink}} by {{#crossLink "GIAPI/mergeConstraints:method"}}merging{{/crossLink}}
 *  the given constraints with the constraints of the origin {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} (see point 2 above)</li><li>keeps track of the {{#crossLink "Refiner/refine:method"}}refinements{{/crossLink}} (both <a href="../classes/DAB.html#constraints">constraints</a> and 
 * <a href="../classes/DAB.html#options">options</a>) in a {{#crossLink "Refiner/chronology:method"}}{{/crossLink}} that can be scrolled 
 * {{#crossLink "Refiner/rewind:method"}}back{{/crossLink}} and {{#crossLink "Refiner/forward:method"}}ahead{{/crossLink}}</li>
 * </ul>
 *  
 * 
 * @class Refiner
 **/
GIAPI.Refiner = function(dabNode, cnstr, _options, onStatus, paginator, reset) {
    
    var ref = {};
    ref._id = 'refiner_' + GIAPI.random();
    
    if(!GIAPI.refHelper || reset){
    	
    	GIAPI.refHelper = {};
    	
    	GIAPI.refHelper._cursor = 0;
        
    	// chronology of the options
        GIAPI.refHelper._optionsChrono = [];
        
        // pushes a clone of the initial options (always available at index 0)
        GIAPI.refHelper._optionsChrono.push(GIAPI.clone(_options));

    	// chronology of the constraints
    	GIAPI.refHelper._chronology = [];
    	
    	// pushes a clone of the initial constraints (always available at index 0)
        GIAPI.refHelper._chronology.push(GIAPI.clone(cnstr));
    }
            
    /**
     * Refines the related {{#crossLink "ResultSet"}}result set{{/crossLink}}:<ol>
     * <li>{{#crossLink "GIAPI/mergeConstraints:method"}}merges{{/crossLink}} the given <code>constraints</code> 
     * with the original {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} constraints</li>
     * <ul>
     * 	<li>if {{#crossLink "Refiner/cursor:method"}}cursor{{/crossLink}} is at the end of the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}} (its value is 
     * {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}<code>.length - 1</code>), it appends the {{#crossLink "GIAPI/mergeConstraints:method"}}merged{{/crossLink}} 
     * <code>constraints</code> to the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}</li>
     * 	<li>if {{#crossLink "Refiner/cursor:method"}}cursor{{/crossLink}} is less than {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}<code>.length - 1</code> 
     * (due to one or more calls to the {{#crossLink "Refiner/rewind:method"}}{{/crossLink}}/{{#crossLink "Refiner/forward:method"}}{{/crossLink}} methods),
     * it overwrites the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}} at index <code>{{#crossLink "Refiner/cursor:method"}}cursor{{/crossLink}} + 1</code> 
     * with the {{#crossLink "GIAPI/mergeConstraints:method"}}merged{{/crossLink}} <code>constraints</code> and clears the remaining part of the 
     * {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}</li>
     * </ul>
     * <li>increases the <code>{{#crossLink "Refiner/cursor:method"}}cursor{{/crossLink}}</code> by 1</li><li>refines the related {{#crossLink "ResultSet"}}result set{{/crossLink}} with the 
     * {{#crossLink "GIAPI/mergeConstraints:method"}}merged{{/crossLink}} <code>constraints</code> and the given <code>options</code></li></ol> 
     *
     * Only <a href="../classes/DAB.html#kvp_list">basic constraints</a> are supported. See also {{#crossLink "GIAPI/mergeConstraints:method"}}{{/crossLink}}
     * 
     * @method refine
     * @async
     * 
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {Object} constraints the <a href="../classes/DAB.html#constraints" target=_blank>constraints</a> to refine the 
     * {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}. Only <a href="../classes/DAB.html#kvp_list">basic constraints</a> are supported
     * 
     * @param {Object} [options] all the <a href="../classes/DAB.html#options">options</a> are allowed except <code>start</code>, 
     * <code>termFrequency</code> and <code>extension</code>  
     */
    ref.refine = function(onResponse, constraints, options){
    	
    	var _targetCnst = null;
    	// the hidden merge option is used by the browse method
    	if(!options || options.merge === undefined || options.merge){    		
    		_targetCnst = GIAPI.mergeConstraints(GIAPI.refHelper._chronology[0], constraints);
    	}else{
    		_targetCnst = GIAPI.clone(constraints);
    	}
    	
    	// prepares the options
    	if(!options){
    		options = {};
    	}
    	
    	// the start index can be different from the starting one
    	// due to some paginations, so it is updated
    	GIAPI.refHelper._optionsChrono[GIAPI.refHelper._cursor].start = _options.start;
    	
 		options.start = 1; // reset the start index
		options.termFrequency =  GIAPI.refHelper._optionsChrono[0].termFrequency; // term frequency cannot change
		delete options.extension; // extension cannot be set
    	    	
    	if(GIAPI.refHelper._cursor === GIAPI.refHelper._chronology.length - 1){
    		// the refinement is appended to the chronology since it is
    		// performed when the cursor is at the end 
    		// (that is that no rewind and/or forward has been called)
    		GIAPI.refHelper._chronology.push(_targetCnst);       	
        	GIAPI.refHelper._optionsChrono.push( options );
     	}else{
    		// the refinement is performed when the cursor has an intermediate
    		// value between 0 and GIAPI.refHelper._chronology.length - 2,
    		// that is that no rewind and/or forward has been called.
    		// the chronology from the cursor index is than cleared
        	GIAPI.refHelper._chronology[GIAPI.refHelper._cursor + 1] = _targetCnst;           	
        	GIAPI.refHelper._optionsChrono[GIAPI.refHelper._cursor + 1] = options;
        	
        	// clears the remaining chronology
        	GIAPI.refHelper._chronology.length = GIAPI.refHelper._cursor + 2;
        	GIAPI.refHelper._optionsChrono.length  = GIAPI.refHelper._cursor + 2;
    	}
        
    	// the cursor is incremented by one
    	GIAPI.refHelper._cursor++;
    	
    	// updates the paginator constraints and start index
     	paginator._cnstr(_targetCnst);  	
    	paginator._index(1);
    	   	   	   	
    	onResponse._origin = 'refiner';    	
        dabNode._paginator(onResponse, _targetCnst, options, onStatus, paginator);
        
//        console.log(JSON.stringify(GIAPI.refHelper._chronology));
//        console.log(_targetCnst);
    };
    
    /**
     * Browses the content of the {{#crossLink "Report/type:property"}}composed{{/crossLink}} {{#crossLink "GINode"}}node{{/crossLink}} 
     * having the given <code>who</code> identifier.<br>
     * The correspondent {{#crossLink "ResultSet"}}result set{{/crossLink}} is the same that could be 
     * retrieved by calling the {{#crossLink "GINode/expand:method"}}{{/crossLink}} method on the 
     * {{#crossLink "Report/type:property"}}composed{{/crossLink}} {{#crossLink "GINode"}}node{{/crossLink}} having the given <code>who</code> identifier. 
     * However the <code>Refiner</code> allows to take advantage of the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}.<br> 
     * If <code>who</code> corresponds to a non existent {{#crossLink "GINode"}}node{{/crossLink}} or to a 
     * {{#crossLink "Report/simple:property"}}{{/crossLink}} {{#crossLink "GINode"}}node{{/crossLink}}, the 
     * correspondent {{#crossLink "ResultSet"}}result set{{/crossLink}} is empty.
     *       
     * @param {Function} onResponse callback function for receiving asynchronous query response
     * @param {String} who identifier of the {{#crossLink "Report/type:property"}}composed{{/crossLink}} {{#crossLink "GINode"}}node{{/crossLink}}
     * @param {Object} [options] all the <a href="../classes/DAB.html#options">options</a> are allowed except <code>start</code>, 
     * <code>termFrequency</code> and <code>extension</code>  
     * 
     * @method browse
     * @async
     * 
     */
    ref.browse = function(onResponse, who, options){
    	
    	var constraints = {'who': who};
    	if(!options){
    		options = {};
    	}
    	options.merge = false;
    	
    	ref.refine(onResponse,constraints,options);
    };
    
    /**
     * Refines the the related {{#crossLink "ResultSet"}}result set{{/crossLink}} by restoring the <a href="../classes/DAB.html#constraints">constraints</a> and the 
      <a href="../classes/DAB.html#options">options</a> in the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}} at the specified <code>cursor</code>.<br>
     * The method is executed only if 
     * <code> cursor &gt;= 0 && cursor &lt;= {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}.length </code> and <code>onResponse</code> is provided
     * 
     * @method restore
     * @async
     * 
     * @param {Function} [onResponse] callback function for receiving asynchronous query response; if omitted the method is only tested but not executed
     * @param {Integer} cursor an integer value indicating the constraints in the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}} to restore
     * @return {Boolean} <code>true</code> if <code> cursor &gt;= 0 && cursor &lt;= {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}.length </code>,
     *  <code>false</code> otherwise
     */
    ref.restore = function(onResponse, cursor, _execute){
    	
     	cursor = parseInt(cursor);
    	
    	if(cursor < 0 || cursor > GIAPI.refHelper._chronology.length - 1){
    		return false;
    	}
    	
    	// hidden param used by forward and rewind
    	if(_execute ===  undefined){
    		_execute = onResponse ? true:false;
    	}
    	
    	if (!_execute) {
            return true;
        }
    	        
    	// set the current start value to the current options
    	GIAPI.refHelper._optionsChrono[GIAPI.refHelper._cursor].start = _options.start;

    	// updates the cursor
     	GIAPI.refHelper._cursor = cursor;
     	
    	var options = GIAPI.refHelper._optionsChrono[cursor];
    	
    	// updates the paginator constraints and start index
    	paginator._cnstr(GIAPI.refHelper._chronology[cursor]);
    	paginator._index(options.start);    	
    	
    	onResponse._origin = 'refiner';
        dabNode._paginator(onResponse, GIAPI.refHelper._chronology[cursor], options, onStatus, paginator);
        
//        console.log(JSON.stringify(GIAPI.refHelper._chronology));
//        console.log(JSON.stringify(GIAPI.refHelper._chronology[GIAPI.refHelper._cursor]));

    	return true;   
    };
    
    /**
     * Resets the <code>Refiner</code> to its initial state:<ol><li>the {{#crossLink "Refiner/cursor:method"}}{{/crossLink}} value is set to 0</li><li>the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}<code>.length</code> is set to 1 and contains the <a href="../classes/DAB.html#constraints">constraints</a> and the 
      <a href="../classes/DAB.html#options">options</a> of the origin {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}</li></ol>
     * @method reset
     */
    ref.reset = function(){
    	
    	// reset the cursor
    	GIAPI.refHelper._cursor = 0;
    	
    	// reset the chronology
    	GIAPI.refHelper._chronology.length = 1;
        
    	// updates the paginator
        paginator._cnstr(GIAPI.refHelper._chronology[0]);
    };
    
    /**
     * Scrolls back the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}:<ol>
     * <li>decreases the {{#crossLink "Refiner/cursor:method"}}{{/crossLink}} by 1</li><li>{{#crossLink "Refiner/restore:method"}}restores{{/crossLink}} the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}} 
     * to the current {{#crossLink "Refiner/cursor:method"}}{{/crossLink}} value</li>
     * </ol>
     * The method is executed only if {{#crossLink "Refiner/cursor:method"}}cursor{{/crossLink}} is greater than 0 and <code>onResponse</code> is provided
     * 
     * @method rewind
     * @async
     * 
     * @param {Function} [onResponse] callback function for receiving asynchronous query response; if omitted the method is only tested but not executed
     * @return {Boolean} <code>true</code> if the {{#crossLink "Refiner/cursor:method"}}cursor{{/crossLink}} is greater than 0,
     *  <code>false</code> otherwise
     */
    ref.rewind = function(onResponse){
    	
    	var _execute = onResponse ? true:false;
    	
    	return ref.restore(onResponse, GIAPI.refHelper._cursor - 1, _execute);
    };
    
    /**
     * Scrolls ahead the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}:<ol>
     * <li>increases the {{#crossLink "Refiner/cursor:method"}}{{/crossLink}} by one</li><li>{{#crossLink "Refiner/restore:method"}}restores{{/crossLink}} the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}} 
     * to the current {{#crossLink "Refiner/cursor:method"}}{{/crossLink}} value</li>
     * </ol>
     * The method is executed only if {{#crossLink "Refiner/cursor:method"}}cursor{{/crossLink}} is less than {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}<code>.length</code> 
     * and <code>onResponse</code> is provided
     *     
     * @method forward
     * @async
     * 
     * @param {Function} [onResponse] callback function for receiving asynchronous query response; if omitted the method is only tested but not executed
     * @return {Boolean} <code>true</code> if the {{#crossLink "Refiner/cursor:method"}}cursor{{/crossLink}} is less than {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}<code>.length</code>,
     *  <code>false</code> otherwise
     */
    ref.forward = function(onResponse){

    	var _execute = onResponse ? true:false;

    	return ref.restore(onResponse, GIAPI.refHelper._cursor + 1, _execute);
    };
   
    /**
     * Returns the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}} cursor, an integer value indicating the current constraints of the 
     * {{#crossLink "Refiner/chronology:method"}}{{/crossLink}}.<br>
     * The value satisfies the following statement: 
     * <code> cursor &gt;= 0 && cursor &lt;= {{#crossLink "Refiner/cursor:method"}}history{{/crossLink}}.length </code>
     * @method cursor
     * @return {Integer} an integer value indicating the current constraint of the {{#crossLink "Refiner/chronology:method"}}{{/crossLink}} 
     */
    ref.cursor = function(){
    	
    	return GIAPI.refHelper._cursor;
    };
    
    /**
     * Returns the chronology of the constraints used to {{#crossLink "Refiner/refine:method"}}{{/crossLink}} 
     * the related {{#crossLink "ResultSet"}}result set{{/crossLink}}
     *
     * @method chronology
     * @return {Object[]} array of <a href="../classes/DAB.html#kvp_list" target=_blank>basic constraints</a> 
     */
    ref.chronology = function(){
    	
    	return GIAPI.refHelper._chronology;
    };
   
    return ref;
};
