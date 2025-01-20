/**
 * @module UI
 **/
import { GIAPI } from '../core/GIAPI.js';

/**
 *  This widget allows to select all the available <a href="#method_constraints" class="crosslink">constraints</a> of 
 *  the <a href="../classes/DAB.html#method_discover" class="crosslink">discover</a>; it also allows to set some <a href="#method_options" class="crosslink">options</a>.<br>
 *  The following CSS is required:<pre><code>
&lt;!-- Font Awesome CSS --&gt;        
&lt;link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.6.1/css/font-awesome.min.css" /&gt;<br>
&lt;!-- Google Maps --&gt;
&lt;script type="text/javascript" src="http://maps.google.com/maps/api/js?" /&gt;<br>
&lt;!-- Gmap.js --&gt;
&lt;script type="text/javascript" src="https://api.geodab.eu/download/giapi/jquery/gmaps.js" /&gt;<br>
&lt;!-- API CSS --&gt;
&lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
</code></pre>
 *  <pre><code>
 *    // creates the widget
   var constWidget = GIAPI.ConstraintsWidget({
	   'keyDownAction': function(){ // something to do when the enter key is pressed }
   });<br>
   // adds the basic constraints controls
   constWidget.whereConstraint('add',{'resultsMapWidget': resMapWidget});<br>   
   constWidget.whatConstraint('add',{'showOptDialog':false});<br>
   constWidget.whenConstraint('add','from',{'minDate': new Date('2010-01-01'), 'maxDate':new Date()});
   constWidget.whenConstraint('add','to');<br>
   // appends the widget to the div for the basic constraints
   var basicConstDivId = 'basicId';
   constWidget.append(basicConstDivId);<br>
   // adds some advanced constraints controls
   constWidget.numericConstraint('add','maxmag',{'minValue': 0, 'step': 0.1});
   constWidget.numericConstraint('add','minmag',{'minValue': 0, 'step': 0.1});<br>
   constWidget.textConstraint('add','magt',[ 
		{'label':'ML'},{'label':'Mw'}], false);<br>
   constWidget.booleanConstraint('add','gdc');<br>
   constWidget.evtOrdConstraint('add');<br>
   // appends the widget to the div for the advanced constraints
   var advConstDivId = 'advId';
   constWidget.append(advConstDivId);
   </pre></code>
   
 *  For additional personalization of the widget, see the <code>cnst-widget</code> class in the <a href="https://api.geodab.eu/docs/assets/css/giapi.css">API CSS</a> file
 *
 *  @param {Object} [options]
 *  @param {Integer} [options.fieldsWidth=205]
 *  @param {Function} [options.keyDownAction] function to be called when the enter key is pressed in a input field 
 *  (for example the function could start the <a href="../classes/DAB.html#method_discover" class="crosslink">discover</a>)
 *
 *  @constructor 
 *  @class ConstraintsWidget
 */
GIAPI.ConstraintsWidget = function(dabNode, options) {

	var widget = {};
	var initOptions;
	var whatOptionsDialogVisible = false;

	if (!options) {
		initOptions = {};
	} else {
		initOptions = options;
	}

	if (!initOptions.fieldsWidth) {
		initOptions.fieldsWidth = 205;
	}

	var widgetId = GIAPI.random();
	var whatOptDialogId = 'what-options-dialog';
	var taxonDialogId = GIAPI.random();

	var keyDownEnabled = initOptions.keyDownAction ? true : false;
	var fixedHeight = 18;

	jQuery('head').append('<div id="' + whatOptDialogId + '"/>');
	jQuery('head').append('<div id="' + taxonDialogId + '"/>');

	var out = '<div class="cnst-widget-div"><table cellpadding="0" class="cnst-widget-table">';

	var timeObjects = [];
	var whereWidget;
	var whereOpt;
	var whatOpt;
	var whatOptDialog;
	var taxonDialog;

	// identifiers
	var ids = {};

	var _getId = function(constraint, id) {

		if (!ids[constraint]) {
			ids[constraint] = id ? id : GIAPI.random();
		}
		return ids[constraint];
	};

	/**
	 * 
	 */
	widget.getId = function(constraint) {

		return _getId(constraint);
	};

	// ------------------------------------
	// set the where input control listener
	//
	jQuery(document).on('click', '#' + _getId('where'), (
		function() {
			whereWidget.show();
		}
	)
	);

	// -------------------------------------------
	// set the from and to input control listeners
	//
	jQuery(document).on('click', '#' + _getId('from') + ',#' + _getId('to'), function() {

		if (jQuery('#' + this.id).attr('class').indexOf('hasDatepicker') === -1) {
			timeObjects.forEach(function(obj) {
				createDatePicker(obj);
			});
		}

		jQuery('#' + this.id).datepicker('show');
	});

	/**
	 * <pre><code>
	 * constWidget.advancedConstraints(
				'advConstDiv',
				'adv-search-div',
				[
					constWidget.textConstraint('get','instrumentTitle'),
					constWidget.textConstraint('get','attributeTitle'),
					constWidget.textConstraint('get','platformTitle')     			
				]
		  );
	 * </pre></code>
	 * 
	 * @param advConstDivId identifier of the <i>div</i> which contains the advanced <code>constraints</code>
	 * @param targetId identifier of the element to append the advanced search button
	 * @param constraints array of constraints
	 * @param options

	 */
	widget.advancedSearch = function(advConstDivId, targetId, constraints, options) {

		if (!options) {
			options = {};
		}

		if (!options.searchButtonBckColor) {

			options.searchButtonBckColor = 'rgb(221, 221, 221)';
		}

		if (!options.searchButtonLabelColor) {

			options.searchButtonLabelColor = 'black';
		}

		if (!options.advConstDivBckColor) {

			options.advConstDivBckColor = 'lightgray';
		}

		var advConstDiv = '<div id="' + advConstDivId + '">';
		constraints.forEach((con) => advConstDiv += con);

		// advanced constraints button    	    
		var advConstButton = GIAPI.FontAwesomeButton({
			'width': 250,
			'label': 'Advanced',
			'icon': ' fa-bars',
			'handler': function() {

				if (jQuery("#" + advConstDivId).css("display") === 'block') {

					jQuery("#" + advConstDivId).css("display", "none");

				} else {

					jQuery("#" + advConstDivId).css("display", "block");
				}
			}
		});

		advConstButton.css('div', 'background', options.searchButtonBckColor);
		advConstButton.css('div', 'color', options.searchButtonLabelColor);

		// advanced search table
		var advSearchTable = '<table>';
		advSearchTable += '<tr><td>' + advConstButton.div() + '</td></tr>';
		advSearchTable += '<tr><td>' + advConstDiv + '</td></tr>';
		advSearchTable += '</table>';

		jQuery("#" + targetId).append(advSearchTable);

		jQuery("#" + advConstDivId).css("position", "relative");
		jQuery("#" + advConstDivId).css("display", "none");
		jQuery("#" + advConstDivId).css("background-color", options.advConstDivBckColor);
		jQuery("#" + advConstDivId).css("padding", "10px");
		jQuery("#" + advConstDivId).css("padding-right", "3px");
	};

	/**
	 * Adds or get the control for the <a href="../classes/DAB.html#what" class="crosslink"><code>what</code></a> constraint 
	 * 
	 * <img style="border: none;" src="../assets/img/search-terms.png" />
	 * 
	 * The image above shows the control with the options dialog disabled.
	 * 
	 * <img style="border: none;" src="../assets/img/search-terms-2.png" />
	 * 
	 * The image above shows the control with the options dialog enabled and opened
	 * 
	 * @param {String} op operation to perform. Possible values are:<ul>
	 * <li>"add": the control is added in cascade to the previously added constraints</li><li>"get": the HTML markup of the constraint is returned</li>
	 * </ul>
	 * @param {Object} [options]
	 * 
	 * @param {String/Integer} [options.value=] the initial value. Must be a string value (empty by default) if <code>options.values</code> is not set, 
	 * otherwise an integer which specifies the array index
	 * 
	 * @param {String} [options.label='Search terms']
	 * @param {String} [options.help='The keyword(s) to search for [...]']
	 * @param {Boolean} [options.showHelpIcon=true]
	 * @param {Boolean} [options.resizable=false]

	 * @param {Object[]} [options.values] array of objects having 'label' and 'value' properties. The labels are used to create
	 * an optionally editable (see <code>options.readOnlyValues</code> option) dropdown menu with autocompletion. If the value is equals to the label, the <code>value</code> property can be omitted.
	 * E.g: <pre><code>&#91;{'label':'','value':''},{'label':'Label and value','value':'Value different from label'},{'label':'Only label (value equals to label)'}&#93;</pre></code>
	 * 
	 * @param {Boolean} [options.readOnlyValues=false] if set to <code>true</code> the input field cannot be edited and only the values included in the <code>values</code> array can be submitted
	 * @param {Boolean} [options.showOptDialog=true] if <code>true</code> shows a dialog which allows to set the 
	 * <a href="../classes/DAB.html#searchFields" class="crosslink">search fields</a> and/or the
	 * <a href="../classes/DAB.html#extend" class="crosslink">result set extension</a> options
	 * 
	 * @param {Boolean} [options.showResultSetExtensionOpt=true] 
	 * @param {String} [options.optDialogPosition='right'] position of the options dialog relative to the control. 
	 * Possible values: "left","right","top","bottom"
	 * 
	 * @return the control HTML markup if <code>op</code> is set to "get" or <code>null</code> otherwise
	 * 
	 * @method whatConstraint
	 */
	// 
	// @param {Boolean} [options.showTaxonDialog=false] TOADD TOADD TOADD
	//
	widget.whatConstraint = function(op, options) {

		if (!options) {
			whatOpt = {};
		} else {
			whatOpt = options;
		}

		if (!whatOpt.label) {
			whatOpt.label = 'Search terms';
		}

		if (!whatOpt.help) {
			whatOpt.help = 'The keyword(s) to search for.<br> Multiple keywords must be separated by " OR " or " AND " token (e.g.: "water OR fire", "sea AND sun")';
		}

		if (whatOpt.showHelpIcon === undefined) {
			options.showHelpIcon = true;
		}

		if (whatOpt.showOptDialog === undefined) {
			whatOpt.showOptDialog = true;
		}

		if (!whatOpt.optDialogPosition) {
			whatOpt.optDialogPosition = 'right';
		}

		if (whatOpt.showTaxonDialog === undefined) {
			whatOpt.showTaxonDialog = false;
		}

		if (whatOpt.showResultSetExtensionOpt === undefined) {
			whatOpt.showResultSetExtensionOpt = true;
		}

		if (!whatOpt.value) {
			if (whatOpt.values) {
				whatOpt.value = 0;
			} else {
				whatOpt.value = '';
			}
		}

		var pos = GIAPI.position(whatOpt.optDialogPosition);

		jQuery('#' + whatOptDialogId).dialog({

			width: whatOpt.showResultSetExtensionOpt ? 345 : 160,
			autoOpen: false,
			resizable: false,
			//dialogClass: "no-titlebar",
			dialogClass: "no-titlebar cnst-widget-what-opt-dialog",

			position: {
				of: '#' + _getId('what'),
				my: "left-103 top+5",
				at: pos.at,
				collision: 'none'
			}
		});

		var tr = createTextField(
			_getId('what'),
			whatOpt.label,
			whatOpt.help,
			whatOpt.values,
			false,
			null,
			null,
			whatOpt.readOnlyValues,
			whatOpt.showHelpIcon,
			options.helpIconImage,
			whatOpt.value,
			whatOpt.resizable);

		switch (op) {
			case 'add':
				out += tr;
				return null;
			case 'get':
				return '<table>' + tr + '</table>';
		}
	};

	/**
	   * Adds or get the control for the <a href="../classes/DAB.html#where" class="crosslink"><code>where</code></a> constraint 
	 *
	 * <img style="border: none;" src="../assets/img/where.png" />
	 * 
	 * The image above shows the area selection widget.
	 * 
	 * <img style="border: none;" src="../assets/img/where-2.png" />
	 * 
	 * The image above shows the input field after the selection
	 * 
	 * @param {String} op operation to perform. Possible values are:<ul>
	 * <li>"add": the control is added in cascade to the previously added constraints</li><li>"get": the HTML markup of the constraint is returned</li>
	 * </ul>
	 * @param {Object} [options] all the available <a href="https://developers.google.com/maps/documentation/javascript/reference#MapOptions" target=_blank>map options</a> are also allowed
	 * 
	 * @param {ResultsMapWidget} [options.resultsMapWidget] when the area selection is applied, the selection is drawn also in the 
	 * given <a href="../classes/ResultsMapWidget.html" class="crosslink">ResultsMapWidget</a> map
	 * 
	 * @param {String} [options.label='Bounding box']
	 * @param {String} [options.help='Rectangular area [...]']
	   * @param {Boolean} [options.showHelpIcon=true]
	 *  
	 * @param {Integer} [options.widgetWidth=550]
	 * @param {Integer} [options.widgetHeight=460]
	 * @param {String} [options.widgetPosition='right'] position of the selection widget dialog relative to the control. 
	 * Possible values: "left","right","top","bottom"
	 * @param {String} [options.widgetModal=false] set the modal option to the widget dialog 
	 * 
	 * @param {Bbox} [options.value={'west':'-30', 'south':'-30', 'east':'30', 'north':'30'}] the initial user selection. Unless <code>options.applyValue</code> 
	 * is set to <code>true</code>, the selection rectangle is drawn on the map but it is not applied
	 * @param {Boolean} [options.applyValue=false] applies the <code>options.value</code>

	 * @param {String} [options.selectionColor='#0000FF']
	 * 
	 * @param {Integer} [options.zoom=6]
	 * @param {Number} [options.lat=0]
	 * @param {Number} [options.lng=0]
	 * 	  
	   * @return the control HTML markup if <code>op</code> is set to "get" or <code>null</code> otherwise
	 * 
	 * @method whereConstraint
	 */
	widget.whereConstraint = function(op, options) {

		if (!options) {
			whereOpt = {};
		} else {
			whereOpt = options;
		}

		if (!options) {
			options = {};
		}

		if (!options.label) {
			options.label = 'Bounding box';
		}

		if (!options.help) {
			options.help = 'Rectangular area (bounding box) where the searched reasults must be contained or overlapped';
		}

		if (options.showHelpIcon === undefined) {
			options.showHelpIcon = true;
		}

		if (!whereOpt.widgetWidth) {
			whereOpt.widgetWidth = 570;
		}

		if (!whereOpt.widgetHeight) {
			whereOpt.widgetHeight = 470;
		}

		if (!whereOpt.widgetPosition) {
			whereOpt.widgetPosition = 'right';
		}

		if (!whereOpt.zoom) {
			whereOpt.zoom = 2;
		}
		if (!whereOpt.lat) {
			whereOpt.lat = 0;
		}
		if (!whereOpt.lng) {
			whereOpt.lng = 0;
		}
		if (whereOpt.showSpatialRelationControl === undefined || whereOpt.showSpatialRelationControl === true) {
			whereOpt.showSpatialRelationControl = true;
		}
		if (whereOpt.scrollwheel === undefined) {
			whereOpt.scrollwheel = true;
		}
		if (whereOpt.panControl === undefined) {
			whereOpt.panControl = true;
		}
		if (whereOpt.panControlOptions === undefined) {
			whereOpt.panControlOptions = true;
		}
		if (whereOpt.streetViewControl === undefined) {
			whereOpt.streetViewControl = false;
		}

		if (whereOpt.overviewMapControl === undefined) {
			whereOpt.overviewMapControl = true;
		}

		if (whereOpt.mapTypeControl === undefined) {
			whereOpt.mapTypeControl = false;
		}

		if (whereOpt.navigationControl === undefined) {
			whereOpt.navigationControl = false;
		}

		if (whereOpt.fullscreenControl === undefined) {
			whereOpt.fullscreenControl = true;
		}

		if (!whereOpt.value) {
			whereOpt.value = { 'south': -30, 'west': -30, 'north': 30, 'east': 30 };
		}

		var value = whereOpt.applyValue ? whereOpt.value.south + ',' + whereOpt.value.west + ',' + whereOpt.value.north + ',' + whereOpt.value.east : '';

		if (whereOpt.applyValue) {
			whereOpt.applyValue = true;
		}

		if (whereOpt.resultsMapWidget && whereOpt.applyValue) {

			whereOpt.resultsMapWidget.select({
				south: whereOpt.value.south,
				west: whereOpt.value.west,
				north: whereOpt.value.north,
				east: whereOpt.value.east
			});
		}

		if (!whereOpt.selectionColor) {
			whereOpt.selectionColor = '#0000FF';
		}

		if (!whereOpt.showLocationControl) {
			whereOpt.showLocationControl = true;
		}

		whereWidget = GIAPI._whereWidget(dabNode, whereOpt, _getId('where'));

		var tr = createTextField(
			_getId('where'),
			options.label,
			options.help,
			null,
			null,
			null,
			null,
			null,
			options.showHelpIcon,
			options.helpIconImage,
			value);

		switch (op) {
			case 'add':
				out += tr;
				return null
			case 'get':
				return '<table>' + tr + '</table>';
		}
	};

	/**
		 * Adds or get the control for the <a href="../classes/DAB.html#when" class="crosslink"><code>when</code></a> constraint
		 * 
		 * <img style="border: none;" src="../assets/img/time.png" />
	 *  
	 * @param {String} op operation to perform. Possible values are:<ul>
	 * <li>"add": the control is added in cascade to the previously added constraints</li><li>"get": the HTML markup of the constraint is returned</li>
	 * </ul>
	 * @param {String} property the <code>when</code> property to control. Possible values are "from" and "to"  
	 * 	
	 * @param {Object} [options]
	   * @param {String} [options.value=] the initial date (unset by default)
	   * 
	 * @param {String} [options.label='Start time'/'End time']
	 * @param {String} [options.help='Start time of [...]'/'End time of [...]']
	 * @param {Boolean} [options.showHelpIcon=true]
	 * 
	 * @param {String} [options.minDate]
	 * @param {String} [options.maxDate]
	   *
	   * @return the control HTML markup if <code>op</code> is set to "get" or <code>null</code> otherwise
	 *
	 * @method whenConstraint
	 */
	widget.whenConstraint = function(op, property, options) {

		if (!options) {
			options = {};
		}

		if (!options.label) {
			options.label = property === 'from' ? 'Start time' : 'End time'
		}

		if (!options.help) {
			options.help = property === 'from' ? 'Start time of the period covered by the content of the dataset' : 'End time of the period covered by the content of the dataset';
		}

		if (options.showHelpIcon === undefined) {
			options.showHelpIcon = true;
		}

		if (!options.value) {
			options.value = '';
		}

		var id = property === 'from' ? _getId('from') : _getId('to');

		var tr = createTextField(
			id,
			options.label,
			options.help,
			null,
			true,
			options.minDate,
			options.maxDate,
			null,
			options.showHelpIcon,
			options.helpIconImage,
			options.value);

		switch (op) {
			case 'add':
				out += tr;
				return null;
			case 'get':
				return '<table>' + tr + '</table>';
		}
	};

	/**
	 * Adds or get an input field for a specified <code>constraint</code> which accepts only numeric values. The available constraints are:<ul>
	   <li><code>'minmag'</code>: minimum magnitude of the searched event. This constraint affects only the "IRIS Event" source</li>
	   <li><code>'maxmag'</code>: maximum magnitude of the searched event. This constraint affects only the "IRIS Event" source</li>
	   <li><code>'mind'</code>: minimum depth (in kilometers) of the searched event depth. This constraint affects only the "IRIS Event" source</li>
	   <li><code>'maxd'</code>: maximum depth (in kilometers) of the searched event depth. This constraint affects only the "IRIS Event" source</li>
	   <li><code>'bandwl'</code>: histogram mean</li>
	   <li><code>'proclev'</code>: processing level</li>
	   <li><code>'illazan'</code>: illumination azimuth angle</li>
	   <li><code>'illzean'</code>: illumination zenith angle value</li>
	   <li><code>'cloudcp'</code>: cloud cover percentage</li>
	   <li><code>'sensorResolution'</code>: sensor resolution</li>
	   <li><code>'sensorResolutionMax'</code>: maximum sensor resolution</li>
	   <li><code>'sensorResolutionMin'</code>: minimum sensor resolution</li></ul>
	  
	 * @param {String} op operation to perform. Possible values are:<ul>
	 * <li>"add": the control is added in cascade to the previously added constraints</li><li>"get": the HTML markup of the constraint is returned</li>
	 * </ul>

	   * @param {String} constraint 
	   * @param {Object} [options]
	 * @param {Number/Integer} [options.value=] the initial value. Must be a number value (not specified by default) if <code>options.values</code> is not set, 
	 * otherwise an integer which specifies the array index
	 *  
	   * @param {String} [options.label=depends on the constraint]
	 * @param {String} [options.help=depends on the constraint] 
	   * @param {Boolean} [options.showHelpIcon=true]
	 * 
	 * @param {Number} [options.minValue]
	 * @param {Number} [options.maxValue]
	 * @param {Number} [options.step]
	 * 
	 * @param {Object[]} [options.values] array of objects having 'label' and 'value' properties (the value must be a number). The labels are used to create
	 * an optionally editable (see <code>options.readOnlyValues</code> option) dropdown menu with autocompletion. If the value is equals to the label, the <code>value</code> property can be omitted.
	 * E.g: <pre><code>&#91;{'label':'','value':''},{'label':'Five','value': 5 },{'label': 1 }&#93;</pre></code>
	 * @param {Boolean} [options.readOnlyValues=false] if set to <code>true</code> the input field cannot be edited and only the values included in the <code>values</code> array can be submitted
	   * 
	   * @return the control HTML markup if <code>op</code> is set to "get" or <code>null</code> otherwise 
	 * 
	 * @method numericConstraint
	 */
	widget.numericConstraint = function(op, constraint, options) {

		var help;
		var label;
		switch (constraint) {
			case 'minmag':
				help = 'The minimum magnitude of the searched event. This constraint affects only the "IRIS Event" source';
				label = 'Min mag.';
				break;
			case 'maxmag':
				help = 'The maximum magnitude of the searched event. This constraint affects only the "IRIS Event" source';
				label = 'Max mag.';
				break;
			case 'mind':
				help = 'The minimum depth (in kilometers) of the searched event depth. This constraint affects only the "IRIS Event" source';
				label = 'Min depth';
				break;
			case 'maxd':
				help = 'The maximum depth (in kilometers) of the searched event depth. This constraint affects only the "IRIS Event" source';
				label = 'Max depth';
				break;
			case 'bandwl':
				help = 'Histogram mean';
				label = 'Hist. mean';
				break;
			case 'proclev':
				help = 'Processing level';
				label = 'Proc. level';
				break;
			case 'illazan':
				help = 'Illumination azimuth angle ';
				label = 'Ill. az. angle ';
				break;
			case 'illzean':
				help = 'Illumination zenith angle value ';
				label = 'Ill. zen. a. val.';
				break;
			case 'cloudcp':
				help = 'Cloud cover percentage ';
				label = 'Cloud co. per.';
				break;
			case 'sensorResolution':
				help = 'Sensor resolution';
				label = 'Sensor res.';
				break;
			case 'sensorResolutionMax':
				help = 'Maximum sensor resolution';
				label = 'Max. sensor res.';
				break;
			case 'sensorResolutionMin':
				help = 'Minimum sensor resolution';
				label = 'Min. sensor res.';
				break;
			default: throw 'Invalid constraint: ' + constraint;
		}

		if (!options) {
			options = {};
		}

		if (!options.label) {
			options.label = label;
		}

		if (!options.help) {
			options.help = help;
		}

		if (options.showHelpIcon === undefined) {
			options.showHelpIcon = true;
		}

		if (!options.value) {
			if (options.values) {
				options.value = 0;
			} else {
				options.value = '';
			}
		}

		var tr = createNumericField(
			_getId(constraint),
			options.label,
			options.help,
			options.minValue,
			options.maxValue,
			options.step,
			options.values,
			options.readOnlyValues,
			options.showHelpIcon,
			options.value);

		switch (op) {
			case 'add':
				out += tr;
				return null;
			case 'get':
				return '<table>' + tr + '</table>';
		}
	};

	/**
	 * Adds or get an input field for a specified <code>constraint</code>. The available constraints are:<ul>
	   <li><code>'loc'</code>: name of a location where to constraint the search. E.g: "italy", "u.s.a", "africa"</li>
	   <li><code>'sba'</code>: "GEO Societal Benefit Area". E.g.:"agriculture", "climate", "disasters"</li>
	   <li><code>'prot'</code>: protocol used to access the data linked to the searched results. E.g: "HTTP", "urn:ogc:serviceType:WebMapService:1.1.1:HTTP", "OGC:WMS-1.1.1-http-get-map"</li>
	   <li><code>'frmt'</code>: format of the data linked to the discovered nodes. E.g.: "image/gif", "application/zip"</li>
	   <li><code>'kwd'</code>: keyword which describes the searched result</li>
	   <li><code>'uselim'</code>: limitation applied on the use of the data linked to the searched results</li>
	   <li><code>'magt'</code>: magnitude type of the searched event. This constraint affects only the "IRIS Event" source</li>
	   <li><code>'inpe-sat-name'</code>: satellite name (string). E.g.: "AQUA"</li>
	   <li><code>'inpe-instr-name'</code>: instrument name (string). E.g.: "MODIS"</li>
	   <li><code>'sta'</code>: station description</li>
	   <li><code>'sensor'</code>: sensor description</li>
	   <li><code>'sarPolCh'</code>: polarisation channels</li>
	   <li><code>'sarPolMd'</code>: polarisation mode</li></ul>

	 * @param {String} op operation to perform. Possible values are:<ul>
	 * <li>"add": the control is added in cascade to the previously added constraints</li><li>"get": the HTML markup of the constraint is returned</li>
	 * </ul>
	 * @param {String} op operation to perform. Possible values are:<ul>
	 * <li>"add": the control is added in cascade to the previously added constraints</li>
	 * <li>"get": the HTML markup of the constraint is returned</li>
	 * </ul>

	 * @param {String} constraint
	   * @param {Object} [options]
	 * @param {String/Integer} [options.value=] the initial value. Must be a string value (empty by default) if <code>options.values</code> is not set, 
	 * otherwise an integer which specifies the array index
	 * 
	   * @param {String} [options.label=depends on the constraint]
	 * @param {String} [options.help=depends on the constraint] 
	   * @param {Boolean} [options.showHelpIcon=true] 	
	 *
	 * @param {Object[]} [options.values] array of objects having 'label' and 'value' properties. The labels are used to create
	 * an optionally editable (see <code>options.readOnlyValues</code> option) dropdown menu with autocompletion. If the value is equals to the label, the <code>value</code> property can be omitted.
	 * E.g: <pre><code>&#91;{'label':'','value':''},{'label':'Label and value','value':'Value different from label'},{'label':'Only label (value equals to label)'}&#93;</pre></code>
	 * @param {Boolean} [options.readOnlyValues]
	   * 
	   * @return the control HTML markup if <code>op</code> is set to "get" or <code>null</code> otherwise	
	 * 
	 * @method textConstraint
	 */
	widget.textConstraint = function(op, constraint, options) {

		var help;
		var label;
		switch (constraint) {
			case 'loc':
				help = 'Name of a location where to constraint the search. E.g: "italy", "u.s.a", "africa"';
				label = 'Location';
				break;
			case 'sba':
				help = '"GEO Societal Benefit Area". E.g.:"agriculture", "climate", "disasters"';
				label = 'GEO S.B.A.';
				break;
			case 'prot':
				help = 'Protocol used to access the data linked to the searched results. E.g: "HTTP", "urn:ogc:serviceType:WebMapService:1.1.1:HTTP", "OGC:WMS-1.1.1-http-get-map"';
				label = 'Protocol';
				break;
			case 'format':
				help = 'Format of the data linked to the discovered nodes. E.g.: "image/gif", "application/zip"';
				label = 'Format';
				break;
			case 'kwd':
				help = 'Keyword which describes the searched result';
				label = 'Keyword';
				break;
			case 'uselim':
				help = 'Limitation applied on the use of the data linked to the searched results';
				label = 'Use lim.';
				break;
			case 'magt':
				help = 'The magnitude type of the searched event. This constraint affects only the "IRIS Event" source';
				label = 'Mag. type';
				break;
			case 'inpe-sat-name':
				help = 'Satellite name (string). E.g.: "AQUA"';
				label = 'Sat. name';
				break;
			case 'inpe-instr-name':
				help = 'Instrument name (string). E.g.: "MODIS"';
				label = 'Instr. name';
				break;
			case 'sta':
				help = 'Station description';
				label = 'Station desc.';
				break;
			case 'sensor':
				help = 'Sensor description';
				label = 'Sensor desc.';
				break;
			case 'sarPolCh':
				help = 'Polarisation channels';
				label = 'Pol. channels';
				break;
			case 'sarPolMd':
				help = 'Polarisation mode';
				label = 'Pol. mode';
				break;
			case 'origOrgId':
				help = 'Organisation which created the resource';
				label = 'Originator organisation';
				break;
			case 'instrumentId':
				help = 'Identifier of the measuring instruments used to acquire the data';
				label = 'Instrument id';
				break;
			case 'instrumentTitle':
				help = 'Name of the measuring instruments used to acquire the data';
				label = 'Instrument name';
				break;
			case 'platformId':
				help = 'Identifier of the platform from which the data were taken';
				label = 'Platform id';
				break;
			case 'platformTitle':
				help = 'Name of the platform from which the data were taken';
				label = 'Platform name';
				break;
			case 'attributeId':
				help = 'Id of the parameter described by the measurement value';
				label = 'Parameter id';
				break;
			case 'attributeTitle':
				help = 'Name of the parameter described by the measurement value';
				label = 'Parameter name';
				break;
			case 'riverName':
				help = 'Name of the river object of measurement';
				label = 'River name';
				break;
			default: throw 'Invalid constraint: ' + constraint;
		}

		if (!options) {
			options = {};
		}

		if (!options.label) {
			options.label = label;
		}

		if (!options.help) {
			options.help = help;
		}

		if (options.showHelpIcon === undefined) {
			options.showHelpIcon = true;
		}

		if (!options.value) {
			if (options.values) {
				options.value = 0;
			} else {
				options.value = '';
			}
		}

		options.id = _getId(constraint, options.id);

		var tr = createTextField(
			options.id,
			options.label,
			options.help,
			options.values,
			false,
			null,
			null,
			options.readOnlyValues,
			options.showHelpIcon,
			options.helpIconImage,
			options.value);

		switch (op) {
			case 'add':
				out += tr;
				return null;
			case 'get':
				return '<table>' + tr + '</table>';
		}
	};

	/**
	 * Adds or get an input field for a specified <code>constraint</code> which accepts only boolean values. The available constraints are:<ul>
	   <li><code>'gdc'</code>: includes only or excludes all the "GEOSS Data Core" records from the search</li>
	   <li><code>'lac'</code>: includes only or excludes all the records having some legal access constraints</li>
	   <li><code>'luc'</code>: includes only or excludes all the records having some legal use constraints</li>

	 * <img style="border: none;" src="../assets/img/gdc.png" />
	 * 
	 * The image above shows the control for the <code>gdc</code> costraint

	 * @param {String} op operation to perform. Possible values are:<ul>
	 * <li>"add": the control is added in cascade to the previously added constraints</li><li>"get": the HTML markup of the constraint is returned</li>
	 * </ul>
	 * 
	 * @param {String} constraint
	   * @param {Object} [options]
	 * @param {Integer} [options.value=0] the initial value. Possible values are: 0 (default), 1, 2

	   * @param {String} [options.label=depends on the constraint]
	 * @param {String} [options.help=depends on the constraint] 
	   * @param {Boolean} [options.showHelpIcon=true] 
	   * 
	   * @return the control HTML markup if <code>op</code> is set to "get" or <code>null</code> otherwise
	 * 
	 * @method booleanConstraint
	 */
	widget.booleanConstraint = function(op, constraint, options) {

		var help;
		var label;
		var values; // = [ {'label':'','value':''}, {'label':'Yes','value':'true'}, {'label':'No','value':'false'} ];
		switch (constraint) {
			case 'gdc':
				values = [{ 'label': 'Search all records', 'value': '' }, { 'label': 'Only GEOSS Data Core records', 'value': 'true' }, { 'label': 'Only Non GEOSS Data Core records', 'value': 'false' }];
				help = 'Includes only or excludes all the "GEOSS Data Core" records from the search';
				label = 'GEOSS D.C.';
				break;
			case 'lac':
				values = [{ 'label': 'Search all records', 'value': '' }, { 'label': 'Only records with legal access constraints', 'value': 'true' }, { 'label': 'Only records without legal access constraints', 'value': 'false' }];
				help = 'Includes only or excludes all the records having some legal access constraints';
				label = 'Legal acc. const.';
				break;
			case 'luc':
				values = [{ 'label': 'Search all records', 'value': '' },
				{ 'label': 'Only records with legal use constraints', 'value': 'true' },
				{ 'label': 'Only records without legal use constraints', 'value': 'false' }];
				label = 'Legal use const.';
				break;
			case 'rosetta':
				values = [{ 'label': 'Basic search by exact term', 'value': '' }, 
				{ 'label': 'Advanced search w/ Rosetta Stone translations', 'value': 'true' }, 
				{ 'label': 'Advanced search w/ Rosetta Stone narrow matches', 'value': 'narrow' },
				 { 'label': 'Advanced search w/ Rosetta Stone broad matches', 'value': 'broad' }];
				label = 'Use ODIP Rosetta Stone service.';
				break;
			case 'semantics':
				values = [{ 'label': 'Basic search by exact term', 'value': '' }, 
				{ 'label': 'Advanced search w/ translations', 'value': 'sameas' }, 
				{ 'label': 'Advanced search w/ translations and narrow matches', 'value': 'narrow' }];
				label = 'Use ODIP Rosetta Stone service.';
				break;
			case 'isValidated':
				values = [{ 'label': 'Search all records', 'value': '' },
				{ 'label': 'Search validated records', 'value': 'true' }];
				help = 'Optionally limits the search to validated records only';
				label = 'Validated records';
				break;
			default: throw 'Invalid constraint: ' + constraint;
		}

		if (!options) {
			options = {};
		}

		if (!options.label) {
			options.label = label;
		}

		if (!options.help) {
			options.help = help;
		}

		if (options.showHelpIcon === undefined) {
			options.showHelpIcon = true;
		}

		if (!options.value) {
			options.value = 0;
		}

		var tr = createTextField(
			_getId(constraint),
			options.label,
			options.help,
			values,
			false,
			null,
			null,
			true,
			options.showHelpIcon,
			options.helpIconImage,
			options.value);

		switch (op) {
			case 'add':
				out += tr;
				return null;
			case 'get':
				return '<table>' + tr + '</table>';
		}
	};

	/**
	 * Adds or get an input field for a the <code>hl</code> constraint
	 *
	 * <img style="border: none;" src="../assets/img/hl.png" />
	 *     
	 * @param {String} op operation to perform. Possible values are:<ul>
	 * <li>"add": the control is added in cascade to the previously added constraints</li><li>"get": the HTML markup of the constraint is returned</li>
	 * </ul>
	   * @param {Object} [options]
	 * @param {Integer} [options.value=0] the initial value. Possible values are: 0 (default), 1, 2

	   * @param {String} [options.label='Result type']
	 * @param {String} [options.help='Type of records to search'] 
	   * @param {Boolean} [options.showHelpIcon=true] 
	   * 
	   * @return the control HTML markup if <code>op</code> is set to "get" or <code>null</code> otherwise
	 *
	 * @method hlConstraint
	 */
	widget.hlConstraint = function(op, options) {

		if (!options) {
			options = {};
		}

		if (!options.label) {
			options.label = 'Result type';
		}

		if (!options.help) {
			options.help = 'Type of records to search';
		}

		if (options.showHelpIcon === undefined) {
			options.showHelpIcon = true;
		}

		if (!options.value) {
			options.value = 0;
		}

		var values = [{ 'label': 'Search all records', 'value': '' }, { 'label': 'Only Dataset', 'value': 'dataset' }, { 'label': 'Only Collections', 'value': 'series' }];

		var tr = createTextField(
			_getId('hl'),
			options.label,
			options.help,
			values,
			false,
			null,
			null,
			true,
			options.showHelpIcon,
			options.helpIconImage,
			options.value);

		switch (op) {
			case 'add':
				out += tr;
				return null;
			case 'get':
				return '<table>' + tr + '</table>';
		}
	};

	/**
	 * 	Adds or get an input field for a the <code>evtOrd</code> constraint. This constraint affects only the "IRIS Event" source
	 * 
	 * <img style="border: none;" src="../assets/img/evtord.png" />
	 *
	 * @param {String} op operation to perform. Possible values are:<ul>
	 * <li>"add": the control is added in cascade to the previously added constraints</li><li>"get": the HTML markup of the constraint is returned</li>
	 * </ul>
	   * @param {Object} [options]
	   * @param {String} [options.value='time'] the initial value. Possible values are: "time", "magnitude"
	   * 
	   * @param {String} [options.label='Event ord.']
	 * @param {String} [options.help='Ordering of the results. This constraint affects only the "IRIS Event" source'] 
	   * @param {Boolean} [options.showHelpIcon=true] 
	   * 
	   * @return the control HTML markup if <code>op</code> is set to "get" or <code>null</code> otherwise
	 *
	 * @method evtOrdConstraint
	 */
	widget.evtOrdConstraint = function(op, options) {

		if (!options) {
			options = {};
		}

		if (!options.label) {
			options.label = 'Event ord.';
		}

		if (!options.help) {
			options.help = 'Ordering of the results. This constraint affects only the "IRIS Event" source';
		}

		if (options.showHelpIcon === undefined) {
			options.showHelpIcon = true;
		}

		if (!options.value) {
			options.value = 0;
		}

		var values = [{ 'label': 'Order by Time', 'value': 'time' }, { 'label': 'Order by Magnitude', 'value': 'magnitude' }];

		var tr = createTextField(
			_getId('evtOrd'),
			options.label,
			options.help,
			values,
			false,
			null,
			null,
			true,
			options.showHelpIcon,
			options.helpIconImage,
			options.value);

		switch (op) {
			case 'add':
				out += tr;
				return null;
			case 'get':
				return '<table>' + tr + '</table>';
		}
	};

	/**
	 * Appends the widget to the element (tipically a <code>&lt;div&gt;</code>) with the specified <code>id</code>; 
	 * this method must be called only if one or more controls have been added in cascade (see <code>op</code> option).<br>
	 * 
	 * @param {String} id
	 * 
	 * @method append
	 */
	widget.append = function(id) {

		out += '</table></div>';
		jQuery('#' + id).append(out);

		out = '<div class="cnst-widget-div"><table cellpadding="0" class="cnst-widget-table">';
	};

	/**
	 * Returns an object with the constraints selected by the user with this widget.<br> 
	 * See also <a href="../classes/DAB.html#constraints" class="crosslink">discover constraints</a>
	 * 
	 * @return the <a href="../classes/DAB.html#constraints" class="crosslink">constraints</a> 
	 * object for the <a href="../classes/DAB.html#method_discover" class="crosslink">discover</a> operation
	 * 
	 * @method constraints
	 */
	widget.constraints = function() {

		var constraints = {};

		var what = jQuery('#' + _getId('what')).val();
		if (what) {
			constraints.what = what;
		}

		var where = jQuery('#' + _getId('where')).val();
		if (where) {
			constraints.where = {
				"south": parseFloat(where.split(',')[0]),
				"west": parseFloat(where.split(',')[1]),
				"north": parseFloat(where.split(',')[2]),
				"east": parseFloat(where.split(',')[3]),
			};
		}

		var from = jQuery('#' + _getId('from')).val();
		var to = jQuery('#' + _getId('to')).val();
		if (from || to) {
			constraints.when = {};
			constraints.when.from = from;
			constraints.when.to = to;
		}

		var keys = [
			'maxmag',
			'minmag',
			'magType',
			'evtOrd',
			'mind',
			'maxd',
			'bandwl',
			'proclev',
			'illazan',
			'illzean',
			'cloudcp',
			'sensorResolution',
			'sensorResolutionMax',
			'sensorResolutionMin',
			'loc',
			'sba',
			'prot',
			'format',
			'kwd',
			'uselim',
			'magt',
			'inpe-sat-name',
			'inpe-instr-name',
			'sta',
			'sensor',
			'sarPolCh',
			'sarPolMd',
			'hl',
			'gdc',
			'lac',
			'luc',
			'rosetta',
			'instrumentId',
			'instrumentTitle',
			'platformId',
			'platformTitle',
			'origOrgId',
			'attributeId',
			'attributeTitle',
			'isValidated',
			'riverName'
		];

		constraints.kvp = [];

		keys.forEach(function(key) {

			var val = jQuery('#' + _getId(key)).val();
			if (val) {
				constraints.kvp.push({ key: key, value: val });
			}
		});

		return constraints;
	};

	/**
	 * Returns an object with the options selected by the user with this widget.<br>
	 * The selectebale options are:<ul>
	 * <li> <a href="../classes/DAB.html#sprel" class="crosslink">spatial relation</a>: see <a href="#method_whereConstraint" class="crosslink">whereConstraint</a> method</li><li> <a href="../classes/DAB.html#searchFields" class="crosslink">search fields</a>: see <a href="#method_whatConstraint" class="crosslink">whatConstraint</a> method</li><li> <a href="../classes/DAB.html#extension" class="crosslink">result set extension</a>: see <a href="#method_whatConstraint" class="crosslink">whatConstraint</a> method</li>
	 * </ul>
	 *
	 * @return the <a href="../classes/DAB.html#options" class="crosslink">options</a> 
	 * object for the <a href="../classes/DAB.html#method_discover" class="crosslink">discover</a> operation
	 * 
	 * @method options
	 */
	widget.options = function() {

		var options = {};

		// spatial relation
		options.spatialRelation =
			whereWidget && whereWidget.inputControl() ? whereWidget.inputControl().spatialRelation() : 'CONTAINS';

		// search fields
		var searchFields = [];
		var anyText = jQuery('#' + _getId('anyTextCheck')).prop('checked') || (jQuery('#' + _getId('anyTextCheck')) === undefined);
		var title = jQuery('#' + _getId('titleCheck')).prop('checked') || (jQuery('#' + _getId('titleCheck')) === undefined);
		var description = jQuery('#' + _getId('descCheck')).prop('checked') || (jQuery('#' + _getId('descCheck')) === undefined);
		var keywords = jQuery('#' + _getId('kwdCheck')).prop('checked') || (jQuery('#' + _getId('kwdCheck')) === undefined);

		if (anyText) {
			searchFields.push('anytext');
		}
		if (title) {
			searchFields.push('title');
		}
		if (description) {
			searchFields.push('description');
		}
		if (keywords) {
			searchFields.push('keywords');
		}
		if (searchFields.length > 0) {
			options.searchFields = searchFields.toString();
		}

		// result set extension
		var ext = jQuery('#' + _getId('rsExtCheck')).prop('checked');
		var rel = jQuery('#' + _getId('moreGenRadio')).prop('checked') ?
			GIAPI.Relation.BROADER : jQuery('#' + _getId('moreSpecRadio')).prop('checked') ?
				GIAPI.Relation.NARROWER :
				GIAPI.Relation.RELATED;

		var what = jQuery('#' + _getId('what')).val();

		if (ext && what) {
			options.extension = {};
			options.extension.relation = rel;
			options.extension.keyword = what;
		}

		return options;
	};

	var createNumericField = function(id, label, help, min, max, step, values, readOnlyValues, helpIcon, initValue) {

		if (keyDownEnabled) {
			jQuery(document).on('keydown', '#' + id, (function() {
				GIAPI.UI_Utils.enterKeyDown(initOptions.keyDownAction);
			}));
		}

		var clearId = id + '-clear';
		jQuery(document).on('click', '#' + clearId, (function() {
			var id = clearId.replace('-clear', '');
			clear(id);
		}));

		var textInput = '<td style="width: ' + initOptions.fieldsWidth + 'px;"  >';
		textInput += '<div style="height:' + fixedHeight + 'px">';
		textInput += '<input style="width: ' + initOptions.fieldsWidth + 'px;" placeholder="' + label + '" class="cnst-widget-input" id="' + id + '" max="' + max + '" min="' + min + '" step="' + step + '" type="number"/></div>';
		textInput += '</td>';

		var input = values ? createSelect(id, label, values, readOnlyValues, min, max, step, initValue) : textInput;
		var clearButton = createClearButton(values, id, clearId, true);

		var tr = '<tr>';
		var _helpTd = '';
		if (helpIcon) {
			var helpId = GIAPI.random();
			_helpTd = helpTd(label, help, helpId);
			var trId = GIAPI.random();
			tr = '<tr id="' + trId + '">';
			//			setRowListener(trId,helpId);
		};

		return tr +
			'<td>' +
			'<table>' +
			'<tr>' + input +
			'<td>' +
			clearButton +
			'</td>' + _helpTd +
			'</tr>' +
			'</table>' +
			'</td></tr>';
	};

	var createTextField = function(
		id,
		label,
		help,
	    values,
		time,
		minDate,
		maxDate,
		ro,
        showHelpIcon,
		helpIconImage,
		initValue,
		whatResizable) {

		var taxonFieldWidth = (id === _getId('what') && whatOpt && whatOpt.showTaxonDialog) ? 'width: ' + (initOptions.fieldsWidth - 28) + 'px;' : '';

		var taxonDiv = (id === _getId('what') && whatOpt.showTaxonDialog) ? createTaxonDiv() : '';

		var cursor = ((whatOpt && id === _getId('what') && whatOpt.showTaxonDialog) || id === _getId('where') || id === _getId('from') || id === _getId('to')) ? 'pointer' : values ? 'default' : 'text';

		var readOnly = ro || (whatOpt && whatOpt.showTaxonDialog && id === _getId('what')) ? 'readonly' : '';

		var sel = (id === _getId('where') && whereOpt.applyValue && whereOpt.value) ? whereOpt.value.south + ',' + whereOpt.value.west + ',' + whereOpt.value.north + ',' + whereOpt.value.east : '';

		var whatStyle = (id === _getId('what') && whatResizable) ? ' min-width:' + initOptions.fieldsWidth + 'px; padding-right:10px;display:flex;resize: horizontal; overflow-x: auto; overflow-y: hidden;' : '';

		var textInput = (id === _getId('what')) ?
			'<td style="width: ' + initOptions.fieldsWidth + 'px;' + whatStyle + '" >' :
			'<td style="width: ' + initOptions.fieldsWidth + 'px;">';

		if (id != _getId('what')) {
			textInput += '<div style="height:' + fixedHeight + 'px;">';
		}

		var width = (id === _getId('what') && whatResizable) ? '100%' : initOptions.fieldsWidth + 'px';

		textInput += '<input value="' + initValue + '" style="width: ' + width + '; cursor: ' + cursor + ';' + taxonFieldWidth + '" placeholder="' + label + '" value="' + sel + '" class="cnst-widget-input" id="' + id + '" type="text" ' + readOnly + '/>' + taxonDiv;

		if (id != _getId('what')) {
			textInput += '</div>';
		}

		textInput += '</td>';

		var input = values ? createSelect(id, label, values, readOnly, null, null, null, initValue) : textInput;

		if (keyDownEnabled) {
			jQuery(document).on('keydown', '#' + id, (function() {
				GIAPI.UI_Utils.enterKeyDown(initOptions.keyDownAction);
			}));
		}

		var clearId = id + '-clear';
		jQuery(document).on('click', '#' + clearId, (function() {
			var id = clearId.replace('-clear', '');
			clear(id);
			if (id === _getId('where')) {

				if (whereOpt.resultsMapWidget) {

					whereOpt.resultsMapWidget.select();
				}
			}
		}));

		if (time) {
			timeObjects.push({
				'id': id,
				'minDate': minDate,
				'maxDate': maxDate
			}
			);
		}

		var options = (id === _getId('what') && whatOpt && !whatOpt.showTaxonDialog && whatOpt.showOptDialog) ? createWhatOptButton() : '';

		var clearButton = createClearButton(values, id, clearId, false, options);
		if (whatOpt && whatOpt.showTaxonDialog && id === _getId('what')) {
			clearButton = '';
		}

		var tr = '<tr>';
		var _helpTd = '';
		if (showHelpIcon && !(whatOpt && whatOpt.showTaxonDialog && id === _getId('what'))) {
			var helpId = GIAPI.random();
			_helpTd = helpTd(label, help, helpId, helpIconImage);
			var trId = GIAPI.random();
			tr = '<tr id="' + trId + '">';
			//			setRowListener(trId,helpId);
		}

		return tr +
			'<td>' +
			'<table>' +
			'<tr>' + input +
			'<td>' +
			options + clearButton +
			'</td>' + _helpTd +
			'</tr>' +
			'</table>' +
			'</td></tr>';
	};

	var setRowListener = function(trId, helpId) {

		jQuery(document).on('mouseover', '#' + trId, function() {
			jQuery('#' + helpId + ' i').css('display', '');
		});
		jQuery(document).on('mouseout', '#' + trId, function() {
			jQuery('#' + helpId + ' i').css('display', 'none');
		});

	};

	var createClearButton = function(values, id, clearId, num, settings) {

		var l = num ? 'margin-left: -40px' : settings ? 'margin-left:-35px' : '';
		var cl = !values ?
			'<i title="Clear" id="' + clearId + '" style="display:none; ' + l + '" class="cnst-widget-clear-button fa fa-times" aria-hidden="true"></i>' : '';

		if (cl) {
			jQuery(document).on('mouseover', '#' + id, function() {
				jQuery('#' + clearId).css('display', '');
			});
			jQuery(document).on('mouseover', '#' + clearId, function() {
				jQuery('#' + clearId).css('display', '');
			});
			jQuery(document).on('mouseout', '#' + id, function() {
				jQuery('#' + clearId).css('display', 'none');
			});
		}

		return cl;
	};

	var createWhatOptButton = function(id) {

		var id = GIAPI.random();
		var i = '<i title="Options" id="' + id + '" class="cnst-widget-what-opt-button fa fa-cog" aria-hidden="true"></i>';

		jQuery(document).on('click', '#' + id, (function() { showWhatOptionsDialog() }));

		return i;
	};

	var createSelect = function(id, label, values, readOnly, min, max, step, initValue) {

		var sel = '<td style="width: ' + initOptions.fieldsWidth + 'px;"  >';
		var n = '';
		if (min || max || step) {
			if (min) {
				n = 'min="' + min + '" ';
			}
			if (max) {
				n += 'max="' + max + '" ';
			}
			if (step) {
				n += 'step="' + step + '" ';
			}
			n += 'type="number"';
		}

		if (!readOnly) {
			sel += '<div style="height:' + fixedHeight + 'px">';
			sel += '<input style="width: ' + initOptions.fieldsWidth + 'px;" class="cnst-widget-input" id="' + id + '" list="' + id + '-list"/ ' + n + ' >';
			sel += '</div>';
			// an input associated to a datalist is editable 
			sel += '<datalist id="' + id + '-list">';
		} else {
			// the select is readonly
			sel += '<div style="height:' + fixedHeight + 'px">';
			sel += '<select style="width: ' + (initOptions.fieldsWidth + 10) + 'px;" class="cnst-widget-select-input cnst-widget-input" id="' + id + '" ' + n + '>';
		}

		values.forEach(function(v, index) {
			var selected = index === initValue ? ' selected' : '';
			sel += '<option value="' + (v.value !== undefined ? v.value : v.label) + '" ' + selected + '>' + v.label + '</option>';
		});

		sel += !readOnly ? '</datalist>' : ('</select></div>');
		sel += '</td>';

		return sel;
	};

	var createTaxonDiv = function() {

		var div = '<div title="Click to edit the field" id="editField" class="cnst-widget-button-img cnst-widget-edit-img"/>';
		jQuery(document).on('click', '#' + _getId('what'), (function() { showTaxonDialog() }));

		jQuery(document).on('click', '#editField', (function() {

			jQuery(document).off('click', '#' + _getId('what'));

			jQuery('#' + _getId('what')).css('cursor', 'text');
			jQuery('#' + _getId('what')).removeAttr('readonly');
			jQuery('#' + _getId('what')).focus();
		}));

		jQuery(document).on('blur', '#' + _getId('what'), (function() {

			jQuery(document).on('click', '#' + _getId('what'), (function() { showTaxonDialog() }));

			jQuery('#' + _getId('what')).css('cursor', 'pointer');
			jQuery('#' + _getId('what')).attr('readonly', 'readonly');
		}));

		return div;
	};

	var createDatePicker = function(obj) {

		var id = obj.id;

		jQuery('#' + id).datepicker(

			{
				dateFormat: "yy-mm-dd",
				changeMonth: true,
				changeYear: true,

				minDate: obj.minDate,
				maxDate: obj.maxDate
			});
	};

	var createWhatOptDialog = function() {

		var table = '<table style="border-spacing: 0px; display: inline-block;  width: 130px;">';

		// search fields
		var title = 'SEARCH FIELDS';
		var help = 'Set the metadata fields where the text search is performed.<br>';
		help += 'As default the text search is performed on the "title" and "keyword" fields.<br>';
		help += 'If you want to perform the text search in to the whole textual content of the dataset, select "Any text"<br><br>';

		var sf = '<td style="width:300px; padding-left: 5px; padding-top: 5px; padding-bottom:15px"><label id="searchFieldsLabel" style="font-weight: bold;">SEARCH FIELDS</label></td><td>' + GIAPI.UI_Utils.helpImage(title, help, 'margin-top:5px;margin-left:2px'); +'</td>';
		table += '<tr>' + sf + '</tr>';

		var title = '<td style="padding-left: 5px;padding-top:5px"><label>Title</label></td><td style="padding-right: 5px;"><input id="' + _getId('titleCheck') + '" type="checkbox" checked/></td>';
		table += '<tr>' + title + '</tr>';

		var kwd = '<td style="padding-left: 5px;padding-top:5px"><label>Keyword</label></td><td style="padding-right: 5px;"><input id="' + _getId('kwdCheck') + '" type="checkbox" checked/></td>';
		table += '<tr>' + kwd + '</tr>';

		var desc = '<td style="padding-left: 5px;padding-top:5px"><label>Description</label></td><td style="padding-right: 5px;"><input id="' + _getId('descCheck') + '" type="checkbox"/></td>';
		table += '<tr>' + desc + '</tr>';

		var anyText = '<td style="padding-left: 5px;padding-top:5px"><label>Any text</label></td><td style="padding-right: 5px;padding-bottom:15px"><input id="' + _getId('anyTextCheck') + '" type="checkbox"/></td>';
		table += '<tr>' + anyText + '</tr>';

		table += '</table>';

		var preventEmptySelection = function(check) {

			var anyText = jQuery('#' + _getId('anyTextCheck')).prop('checked');
			var title = jQuery('#' + _getId('titleCheck')).prop('checked');
			var description = jQuery('#' + _getId('descCheck')).prop('checked');
			var keywords = jQuery('#' + _getId('kwdCheck')).prop('checked');

			if (check.id === _getId('anyTextCheck') && anyText) {
				jQuery('#' + _getId('anyTextCheck')).prop('checked', true);
				jQuery('#' + _getId('titleCheck')).prop('checked', true);
				jQuery('#' + _getId('descCheck')).prop('checked', true);
				jQuery('#' + _getId('kwdCheck')).prop('checked', true);
			}

			if (!anyText && !title && !description && !keywords) {
				jQuery(check).prop('checked', true);
			}
		};

		jQuery(document).on('click', '#' + _getId('anyTextCheck'), function(e) {
			preventEmptySelection(this);
		});

		jQuery(document).on('click', '#' + _getId('titleCheck'), function(e) {
			preventEmptySelection(this);
		});

		jQuery(document).on('click', '#' + _getId('descCheck'), function(e) {
			preventEmptySelection(this);
		});

		jQuery(document).on('click', '#' + _getId('kwdCheck'), function(e) {
			preventEmptySelection(this);
		});

		// result set extension
		var extTable = '<table style="border-spacing: 0px; display: inline-block; width: 180px;height: 133px;">';

		var title = 'RESULT SET EXTENSION';
		var help = 'Normally the response of a search is a single result set. With this option <b>you can get more than one result set</b>';
		help += ' according to an interaction with a service called "semantic engine". This service is able to identify terms "semantically related" ';
		help += ' to the term specified by the "Search terms" constraint. The relation between terms can be "more general" or "more specific". The terms identified by the "semantic engine" ';
		help += ' are used to build additional queries to the DAB, and a result set is created for each query having at least one result.<br>';
		help += ' If the extension provides more than one result set, you will find in the response pane a tab for each result set. The label of each tab ';
		help += ' provides the term identified by the "semantic engine" used to generate the correspondent result set.<br><br>';
		help += ' Note that because of the interaction with the "semantic engine" and the possible generation of additional queries, <i>the response time can be longer than usual</i><br><br>';

		var sf = '<td style="width:300px; padding-left: 5px; padding-top: 5px; padding-bottom:15px"><label style="font-weight: bold;">RESULT SET EXTENSION</label></td>';
		sf += '<td>' + GIAPI.UI_Utils.helpImage(title, help, 'margin-top:5px;margin-left:2px'); +'</td>';
		extTable += '<tr>' + sf + '</tr>';

		var title = '<td style="padding-left: 5px;padding-top:5px"><label>Enabled</label></td><td style="padding-right: 5px;"><input id="' + _getId('rsExtCheck') + '" type="checkbox"/></td>';
		extTable += '<tr>' + title + '</tr>';

		var moreGen = '<td style="padding-left: 5px;padding-top:5px"><label id="' + _getId('moreGenLabel') + '" style="margin-left:10px">- More general</label></td>';
		moreGen += '<td style="padding-right: 5px;"><input id="' + _getId('moreGenRadio') + '" type="radio" name="rsExtRadio" checked/></td>';
		extTable += '<tr>' + moreGen + '</tr>';

		var moreSpec = '<td style="padding-left: 5px;padding-top:5px"><label id="' + _getId('moreSpecLabel') + '" style="margin-left:10px">- More specific</label></td>';
		moreSpec += '<td style="padding-right: 5px;"><input id="' + _getId('moreSpecRadio') + '" type="radio" name="rsExtRadio"/></td>';
		extTable += '<tr>' + moreSpec + '</tr>';

		var related = '<td style="padding-left: 5px;padding-top:5px"><label id="' + _getId('relLabel') + '" style="margin-left:10px">- Related</label></td>';
		related += '<td style="padding-right: 5px;"><input id="' + _getId('relRadio') + '" type="radio" name="rsExtRadio"/></td>';
		extTable += '<tr>' + related + '</tr>';

		extTable += '</table>';

		if (!whatOpt.showResultSetExtensionOpt) {
			extTable = '';
		}

		var widget = '<div class="cnst-widget-what-opt-div">' + table + ' ' + extTable + ' </div>';

		var updateOnCheck = function(obj) {
			if (obj && obj.checked) {

				jQuery('#' + _getId('moreGenLabel')).css('color', 'black');
				jQuery('#' + _getId('moreSpecLabel')).css('color', 'black');
				jQuery('#' + _getId('relLabel')).css('color', 'black');

				jQuery('#' + _getId('moreGenRadio')).removeAttr('disabled');
				jQuery('#' + _getId('moreSpecRadio')).removeAttr('disabled');
				jQuery('#' + _getId('relRadio')).removeAttr('disabled');
			} else {
				jQuery('#' + _getId('moreGenLabel')).css('color', '#999999');
				jQuery('#' + _getId('moreSpecLabel')).css('color', '#999999');
				jQuery('#' + _getId('relLabel')).css('color', '#999999');

				jQuery('#' + _getId('moreGenRadio')).attr('disabled', 'disabled');
				jQuery('#' + _getId('moreSpecRadio')).attr('disabled', 'disabled');
				jQuery('#' + _getId('relRadio')).attr('disabled', 'disabled');
			}
		};

		jQuery(document).on('click', '#' + _getId('rsExtCheck'), (function() {
			updateOnCheck(this);
		}));

		updateOnCheck();

		return widget;
	};

	var showWhatOptionsDialog = function() {

		if (!whatOptDialog) {

			whatOptDialog = createWhatOptDialog();

			jQuery('#' + whatOptDialogId).append(whatOptDialog);
		}

		if (whatOptionsDialogVisible) {

			jQuery('#' + whatOptDialogId).dialog('close');

		} else {

			jQuery('#' + whatOptDialogId).dialog('open');
		}

		whatOptionsDialogVisible = !whatOptionsDialogVisible;
	};


	var createTaxonDialog = function() {

		// search mode table
		var sModeTable = '<table style="padding-bottom: 10px; background: white; width: 590px;">';

		var helpTitle = 'Search mode';
		var helpText = '';

		var ts = '<td style=" padding-left: 5px; padding-top: 5px; padding-bottom:10px">';
		ts += '<label style="font-weight: bold;">Search mode</label></td><td></td>';
		sModeTable += '<tr>' + ts + '</tr>';

		var verNameEx = '<td style=""><input id="verNameExact" type="radio" name="srcMode" checked/>';
		verNameEx += '<label style="vertical-align: top">&nbsp;Vernacular name exact</label></td>';

		var sciNameEx = '<td style=""><input id="sciNameExact" type="radio" name="srcMode"/>';
		sciNameEx += '<label style="vertical-align: top">&nbsp;Scientific name exact</label></td>';

		var identifier = '<td style=""><input id="identifier" type="radio" name="srcMode"/>';
		identifier += '<label style="vertical-align: top">&nbsp;Find by identifier</label></td>';

		sModeTable += '<tr>' + verNameEx + sciNameEx + identifier + '</tr>';

		var verNameLk = '<td style=""><input id="verNameLike" type="radio" name="srcMode"/>';
		verNameLk += '<label style="vertical-align: top">&nbsp;Vernacular name like</label></td>';

		var sciNameLk = '<td style=""><input id="sciNameLike" type="radio" name="srcMode"/>';
		sciNameLk += '<label style="vertical-align: top">&nbsp;Scientific name like</label></td>';

		sModeTable += '<tr>' + verNameLk + sciNameLk + '</tr>';

		sModeTable += '</table>';

		// vernacular name
		helpTitle = 'Vernacular name';
		helpText = '';

		var verNameLabel = '<label style="margin-left:5px; margin-top: 10px; display: inline:block"><b>Name/identifier:</b></label>';
		var verNameField = '<input title="Press enter to start searching" id="taxonName" type="text" style="padding: 3px;border:none; margin-top: 10px;margin-left: 5px; width: 364px; display: inline:block"></input>';

		var verNameSearch = GIAPI.FontAwesomeButton({
			'id': 'taxonSearch',
			'label': 'SEARCH',
			'icon': 'fa-search'
		});
		verNameSearch.css('div', 'padding', '3px');
		verNameSearch.css('div', 'padding-right', '15px');
		verNameSearch.css('div', 'margin-left', '5px');

		var verNameDiv = '<div style="margin-top:5px">' + verNameLabel + verNameField + verNameSearch.div() + '</div>';

		var search = function() {

			// removes the listener from the search button and set the disabled style
			jQuery(document).off('click', '#taxonSearch', search);
			GIAPI.UI_Utils.setGlassPane('taxonSearch', { background: true });

			// disables the input
			jQuery('#taxonName').attr('disabled', 'disabled');

			// appends an info message
			jQuery('#responseDiv').html('');
			jQuery('#responseDiv').append('<br><label style="color:blue">&nbsp;&nbsp;&nbsp;Performing request, please wait...</label>');

			// retrieves name and mode
			var name = jQuery('#taxonName').val();
			var mode = jQuery('#verNameExact').prop('checked') === true ? 'vernacularNameExact' :
				jQuery('#verNameLike').prop('checked') === true ? 'vernacularNameLike' :
					jQuery('#sciNameExact').prop('checked') === true ? 'scientificNameExact' :
						jQuery('#sciNameLike').prop('checked') === true ? 'scientificNameLike' :
							'findByIdentifier';

			var endpoint = dabNode.endpoint();
			endpoint = endpoint.endsWith('/') ? endpoint : endpoint + '/';

			var servicePath = dabNode.servicePath();

			var query = endpoint + servicePath + '/opensearch?reqID=66w&taxonSearch&name=' + name + '&mode=' + mode;

			// listener to retrieve the scientific name when clicking on a table row
			jQuery(document).on('click', '#cnst-widget-taxon-table tr', function() {

				jQuery(this).addClass('cnst-widget-taxon-resp-table-td-selected').siblings().removeClass('cnst-widget-taxon-resp-table-td-selected');
				var value = jQuery(this).find('td:first').text();

				jQuery('#' + _getId('what')).val(value);
				jQuery('#' + taxonDialogId).dialog('close');
			});

			jQuery.ajax({

				type: 'GET',
				url: query,
				crossDomain: true,
				dataType: 'jsonp',

				success: function(data, status, jqXHR) {

					jQuery('#responseDiv').html('');

					// enables search button
					jQuery(document).on('click', '#taxonSearch', search);
					GIAPI.UI_Utils.removeGlassPane('taxonSearch');

					// enables input field
					jQuery('#taxonName').removeAttr('disabled');

					// get the query response
					var query = data.response.query;
					if (!query) {
						jQuery('#responseDiv').append('<br><label style="color:red">&nbsp;&nbsp;&nbsp;Error occurred, EU BON UTIS not available.<br>&nbsp;&nbsp;&nbsp;Please try later</label>');
						return;
					}

					// creates the response table
					var table = '<table id="cnst-widget-taxon-table" style="text-align: left;width: 100%;padding:3px; background: white;">';
					table += '<tr style="font-size:90%"><th>Scientific Name</th><th>Genus</th><th>Family</th><th>Order</th><th>Class</th><th>Phylum</th><th>Kingdom</th></tr>';

					query[0].response.forEach(function(r) {

						var checkList = r.checklist.toLowerCase();
						if (checkList === 'pesi') {

							var taxon = r.taxon;
							var sName = taxon.taxonName.scientificName;

							var hClass = taxon.higherClassification;
							var values = [];
							values.push(sName);
							// 0: sc name
							// 1: genus
							// 2: family
							// 3: order
							// 4: class
							// 5: phylum
							// 6: kingdom
							if (hClass.length === 0) {
								for (var i = 1; i < 7; i++) {
									values[i] = '-';
								}
							}
							hClass.forEach(function(cl) {

								var rank = cl.rank;
								var name = cl.scientificName;
								var index;
								switch (rank) {
									case 'Genus': index = 1; break;
									case 'Family': index = 2; break;
									case 'Order': index = 3; break;
									case 'Class': index = 4; break;
									case 'Phylum': index = 5; break;
									case 'Kingdom': index = 6; break;
								}
								values[index] = name;
							});

							for (var i = 0; i < values.length; i++) {
								var value = values[i];
								if (!value) {
									value = '-';
								}
								if (i === 0) {
									table += '<tr class="cnst-widget-taxon-resp-table-tr" title="Click to select Scientific Name">';
									table += '<td class="cnst-widget-taxon-resp-sc-name-td">' + value + '</td>'
								} else if (i === values.length) {
									table += '</tr>';
								} else {
									table += '<td style=" font-size:80%;">' + value + '</td>';
								}
							}
						}
					});

					table += '</table>'
					jQuery('#responseDiv').append(table);
				}
			});

		};

		jQuery(document).on('click', '#taxonSearch', search);
		jQuery(document).on('keydown', '#taxonName', (function() { GIAPI.UI_Utils.enterKeyDown(search) }));

		var applyButton = GIAPI.FontAwesomeButton({
			'label': 'CLOSE',
			'icon': 'fa-times',
			'handler': function() {
				jQuery('#' + taxonDialogId).dialog('close');
				return false;
			}
		});
		applyButton.css('div', 'padding-right', '15px');
		applyButton.css('div', 'margin-top', '10px');

		var responseDiv = '<div style="background:white;width:800px;height:400px;margin-top:10px;overflow-y:auto" id="responseDiv"></div>';

		var widget = '<div style="margin-left:-9px">' + sModeTable + ' ' + verNameDiv + ' ' + responseDiv + ' ' + applyButton.div() + '</div>';

		jQuery('#' + taxonDialogId).append(widget);

		return widget;
	};

	var showTaxonDialog = function() {

		var pos = GIAPI.position(whatOpt.taxonDialogPosition);

		jQuery('#' + taxonDialogId).dialog({
			title: 'EU BON UTIS Taxonomic Search',
			height: 650,
			width: 810,
			modal: true,
			resizable: false,
			autoOpen: false
		});

		jQuery('#' + taxonDialogId).dialog('open');

		if (!taxonDialog) {

			taxonDialog = createTaxonDialog();
		}
	};

	var helpTd = function(title, help, id, imageId_) {

		var td = '';
		if (help) {
			
			var imageId = 'fa-question-circle-o';
			
			if (title.toLowerCase().includes('instrument')) {
				imageId = 'fa-thermometer-full';
			}
			
			if (title.toLowerCase().includes('platform')) {
				imageId = 'fa-ship';
			}
			
			if (title.toLowerCase().includes('originator')) {
				imageId = 'fa-users';
			}
			
			if (title.toLowerCase().includes('attribute') || title.toLowerCase().includes('parameter')) {
				imageId = 'fa-bar-chart';
			}
			
			if (title.toLowerCase().includes('validated')) {
				imageId = 'fa-check-square-o';
			}
			
			imageId = imageId_ || imageId;
			
			td = '<td style="vertical-align: middle;height: ' + (fixedHeight + 1) + 'px;" id="' + id + '">' +
				GIAPI.UI_Utils.helpImage(title, help, 'margin-left: -4px;', imageId, 'odip-help') +
				'</td>';
		}
		return td;
	};

	var clear = function(id) {

		jQuery('#' + id).val('');
	};

	return widget;
};
