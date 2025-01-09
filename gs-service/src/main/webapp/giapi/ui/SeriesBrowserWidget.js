
import { GIAPI } from '../core/GIAPI.js';

GIAPI.SeriesBrowserWidget = function(options) {

	var widget = {};

	if (!GIAPI.ui.collections) {
		GIAPI.ui.collections = [];
	};
	var constWidget;
	var toggleButton;
	var browseButton;
	var browseButtonId = GIAPI.random();
	var toggleButtonId = GIAPI.random();
	var infoLabelId = GIAPI.random();

	var rwdButtonId = GIAPI.random();
	var fwdButtonId = GIAPI.random();
	var clearId = GIAPI.random();
	var browserDivId = GIAPI.random();

	widget.init = function(headerDivId, refiner) {

		createHeader(browserDivId, headerDivId, refiner);
		createDiv(browserDivId, refiner);

		widget.update();
	};

	/**
	 * Updates the widget
	 */
	widget.update = function(action, id) {

		switch (action) {
			case 'remove':
				Array.prototype.remove(GIAPI.ui.collections, id);
				break;
			case 'add':
				GIAPI.ui.collections.push(id);
				break;
		}

		if (GIAPI.ui.collections.length > 0) {

			toggleButton.state('off');
			enableBrowseButton(true);

			if (jQuery('#' + browserDivId).css('display') === 'none') {
				jQuery('#' + toggleButtonId).click();
			}

		} else {

			if (toggleButton.state() !== 'disabled') {
				jQuery('#' + toggleButtonId).click();
			}

			toggleButton.state('disabled');
			enableBrowseButton(false);
		}

		var label;
		var textLabel;
		switch (GIAPI.ui.collections.length) {
			case 0:
				label = 'Add collections to browse with optional filters';
				textLabel = label;
				break;
			case 1:
				label = 'You have <span style="color:blue; font-weight:bold; font-style:italic">' + GIAPI.ui.collections.length + ' collection</span> to browse';
				textLabel = 'You have ' + GIAPI.ui.collections.length + ' collection to browse';
				jQuery('#' + toggleButtonId).fadeOut(300);
				jQuery('#' + toggleButtonId).fadeIn(300);
				jQuery('#' + toggleButtonId).fadeOut(300);
				jQuery('#' + toggleButtonId).fadeIn(300);

				break;
			default:
				label = 'You have <span style="color:blue; font-weight:bold; font-style:italic">' + GIAPI.ui.collections.length + ' collections</span> to browse';
				textLabel = 'You have ' + GIAPI.ui.collections.length + ' collections to browse';
				jQuery('#' + toggleButtonId).fadeOut(300);
				jQuery('#' + toggleButtonId).fadeIn(300);
				jQuery('#' + toggleButtonId).fadeOut(300);
				jQuery('#' + toggleButtonId).fadeIn(300);

				break;
		}

		jQuery('#' + infoLabelId).html(label);
	};

	widget.collections = function() {

		return GIAPI.ui.collections;
	};

	var enableBrowseButton = function(enable) {

		browseButton.enable(enable);

		if (enable) {

			browseButton.css('div', 'background', '#2C3E50');
			browseButton.css('div', 'cursor', 'pointer');
			browseButton.css('label', 'cursor', 'pointer');
			browseButton.css('label', 'color', 'white');
			browseButton.css('icon', 'color', 'white');


			jQuery('#' + browseButtonId).hover(
				function() {
					jQuery('#' + browseButtonId).css('box-shadow', '0px 0px 6px 4px rgba(0,0,0,0.3)');

				},
				function() {
					jQuery('#' + browseButtonId).css('box-shadow', 'none');

				}
			);

			browseButton.css('icon', 'font-size', '13px');
			browseButton.css('label', ' font-size', '12px');

		} else {

			jQuery('#' + browseButtonId).hover(
				function() {
					jQuery('#' + browseButtonId).css('box-shadow', 'none');

				},
				function() {
					jQuery('#' + browseButtonId).css('box-shadow', 'none');

				}
			);

			browseButton.css('div', 'background', 'rgba(128, 128, 128, 0.28)');
			browseButton.css('div', 'cursor', 'default');
			browseButton.css('label', 'cursor', 'default');
			browseButton.css('label', 'color', 'lightgray');
			browseButton.css('icon', 'color', 'lightgray');

			browseButton.css('icon', 'font-size', '13px');
			browseButton.css('label', ' font-size', '12px');
		}

	};

	var createHeader = function(browserDivId, headerDivId, refiner) {

		var table = '<table class="series-browser-widget-table">';

		toggleButton = GIAPI.ToggleButton({
			'id': toggleButtonId,
			'targetId': browserDivId,
			'offLabel': 'Show browsing collection filters',
			'onLabel': 'Hide browsing collection filters',
			'attr': [{ name: 'type', value: 'series-browser-widget-toggle' }],
			'duration': 400,
			'width': 300
		});

		// ----------------------------------------------
		// clear button
		//
		var clearCSS = 'margin-right: 5px;';
		clearCSS += 'margin-left: 5px;';
		clearCSS += 'font-size: 1em;';

		if (refiner.chronology().length > 1) {
			clearCSS += 'color: black;';
			clearCSS += 'cursor: pointer;';
		} else {
			clearCSS += 'color: #666;';
			clearCSS += 'cursor: #default;';
		}

		var clear = '<i title="Clear browsing history" id="' + clearId + '" style="' + clearCSS + '" class="fa fa-times-circle" aria-hidden="true"></i>';
		jQuery(document).on('click', '#' + clearId, function() {

			GIAPI.ui.collections = [];
			refiner.reset();

			jQuery('#collDiv').html('');

			if (toggleButton.state() === 'on') {
				jQuery('#' + toggleButtonId).click();

				toggleButton.state('disabled');
				enableBrowseButton(false);
			}

			jQuery('#' + clearId).css('color', '#666');
			jQuery('#' + clearId).css('cursor', 'default');

			jQuery('#' + rwdButtonId).attr('enabled', 'false');

			jQuery('#' + rwdButtonId).addClass('ref-button-a-disabled');
			jQuery('#' + rwdButtonId).addClass('ref-button-a-disabled:before');

			jQuery('#' + fwdButtonId).attr('enabled', 'false');

			jQuery('#' + fwdButtonId).addClass('ref-button-a-disabled');
			jQuery('#' + fwdButtonId).addClass('ref-button-a-disabled:before');

			GIAPI.Common_UINode._deselectAll();

			widget.update();

			if (options.onBrowsingReady) {
				options.onBrowsingReady.apply(this, [refiner]);
			}
		});

		// ----------------------------------------------
		// rewind and forward buttons
		//
		rwdButtonId = GIAPI.random();
		fwdButtonId = GIAPI.random();

		var backButton = '<div title="Back to search results" enabled="false" id="' + rwdButtonId + '" class="ref-rwd-a ref-button-a "></div>';
		var collDiv = '<div style="display:inline-block; margin-left: 5px; "id="collDiv"></div>';
		var fwdButton = '<div title="Next browsing results" style="margin-left:5px" enabled="false" id="' + fwdButtonId + '" class="ref-fwd-a ref-button-a "></div>';

		// browse button action
		var handler = function() {

			GIAPI.UI_Utils.discoverDialog('open');

			var constraints = constWidget.constraints();

			refiner.refine(
				options.onDiscoverResponse,
				{
					'who': GIAPI.ui.collections.toString(),
					'where': constraints.where,
					'when': constraints.when,
					'what': constraints.what
				}, {
				merge: false,
				searchFields: 'title,keyword,description'
			}
			);
		};

		// browse button
		browseButton = GIAPI.FontAwesomeButton({
			'id': browseButtonId,
			'width': 70,
			'label': 'Browse',
			'icon': 'fa-folder-open',
			'handler': handler
		});
		browseButton.css('div', 'padding', '5px');
		browseButton.css('div', 'text-align', 'center');
		browseButton.css('div', 'margin-top', '5px');
		browseButton.css('div', 'margin-bottom', '0px');

		browseButton.css('div', 'margin-left', '8px');
		browseButton.css('icon', 'font-size', '13px');
		browseButton.css('icon', 'margin-left', '-5px');
		browseButton.css('label', 'vertical-align', 'baseline');
		browseButton.css('label', 'font-size', '12px');

		var labelCss = "font-size: 12px; margin-left: 7px; color: black;";

		table += '<tr><td colspan="2" align="left"><div style="display:inline-block"><label style="' + labelCss + '" id="' + infoLabelId + '">You have 0 collections to browse</label></div></td></tr>';

		table += '<tr><td style="width: 70px;">' + browseButton.div() + '</td><td style="vertical-align: middle;"><div style="margin-top:7px;margin-bottom:-5px;">' + clear + backButton + collDiv + fwdButton + '</div></td></tr>';

		table += '<tr><td style="display: none;" colspan="2" align="left"><div style="display:inline-block">' + toggleButton.div() + '</div></td></tr>';


		var css = 'display: none;';
		css += 'width:100%;';
		table += '<tr><td colspan="2"  align="left" ><div style="' + css + '" id=' + browserDivId + ' ></div></td></tr></table>';

		jQuery('#' + headerDivId).append(table);

		// --------------------------------------------
		// set the rewind and forward buttons listener
		//
		jQuery('#' + rwdButtonId).click(function() {

			if (jQuery('#' + rwdButtonId).attr('enabled') === 'true') {
				GIAPI.UI_Utils.discoverDialog('open');
				refiner.rewind(options.onDiscoverResponse);
			}
		});
		jQuery('#' + fwdButtonId).click(function() {

			if (jQuery('#' + fwdButtonId).attr('enabled') === 'true') {
				GIAPI.UI_Utils.discoverDialog('open');
				refiner.forward(options.onDiscoverResponse);
			}
		});

		// ------------------------------------------
		// set the label of the rewind button 
		//
		switch (refiner.cursor()) {
			case 1:
				jQuery('#' + rwdButtonId).attr('title', 'Back to search results');
				break;
			default:
				jQuery('#' + rwdButtonId).attr('title', 'Back to previous browsing results');
				break;
		}

		// --------------------------------------------
		// updates the collections array
		//
		var cur = refiner.cursor();
		var chron = refiner.chronology()[cur + 1];

		// empty the collections array since now the collections are consumed
		GIAPI.ui.collections = [];

		// fill the the collections array with the chronology collections identifiers
		if (chron && chron.who) {
			var who = chron.who.split(',');
			who.forEach(function(id) {
				GIAPI.ui.collections.push(id);
			});
		}

		// ------------------------------------------
		// set the chronology icons
		//
		var title = function(j) {
			var chrono = refiner.chronology()[j];
			var len = chrono.who.split(',').length;
			var sub = j > 1 ? 'sub' : '';
			return len + ' opened ' + sub + ' collection' + (len > 1 ? 's' : '');
		}

		if (refiner.chronology().length > 1) {
			var chronology = '';
			for (var j = 0; j < refiner.chronology().length; j++) {

				// at index 0 draws the search icon
				if (j === 0) {
					// cursor 0, we are at search results level so the the search icon is black
					if (refiner.cursor() === 0) {
						chronology += '<i title="Search results"  class="fa fa-search series-browser-widget-history-icon" aria-hidden="true"></i> >';
					} else {
						// cursor > 0, we are at collections level so the the search icon is gray
						chronology += '<i title="Search results" style="color: #666;" class="fa fa-search series-browser-widget-history-icon" aria-hidden="true"></i> >';
					}
				} else if (refiner.cursor() === j) {
					// black icon
					chronology += '<i title="' + title(j) + '" style="margin-left:3px" class="fa fa-folder-open series-browser-widget-history-icon" aria-hidden="true"></i> >';
				} else {
					// gray icon
					chronology += '<i  title="' + title(j) + '" style="color: #666;margin-left:3px" class="fa fa-folder-o series-browser-widget-history-icon" aria-hidden="true"></i> >';
				}
			}

			chronology = chronology.substring(0, chronology.length - 2);

			jQuery('#collDiv').append(chronology);
		}

		// ------------------------------------------
		// enables or disables the refiner buttons
		//
		if (refiner.rewind()) {

			jQuery('#' + rwdButtonId).attr('enabled', 'true');

			jQuery('#' + rwdButtonId).removeClass('ref-button-a-disabled');
			jQuery('#' + rwdButtonId).removeClass('ref-button-a-disabled:before');

		} else {

			jQuery('#' + rwdButtonId).attr('enabled', 'false');

			jQuery('#' + rwdButtonId).addClass('ref-button-a-disabled');
			jQuery('#' + rwdButtonId).addClass('ref-button-a-disabled:before');
		}

		if (refiner.forward()) {

			jQuery('#' + fwdButtonId).attr('enabled', 'true');

			jQuery('#' + fwdButtonId).removeClass('ref-button-a-disabled');
			jQuery('#' + fwdButtonId).removeClass('ref-button-a-disabled:before');

		} else {

			jQuery('#' + fwdButtonId).attr('enabled', 'false');

			jQuery('#' + fwdButtonId).addClass('ref-button-a-disabled');
			jQuery('#' + fwdButtonId).addClass('ref-button-a-disabled:before');
		}

		// initially disabled
		toggleButton.state('disabled');
		enableBrowseButton(false);

		// --------------------------------------------
		// if set invokes the onBrowsingReady function
		//
		if (options.onBrowsingReady) {
			options.onBrowsingReady.apply(this, [refiner]);
		}
	};

	var createDiv = function(browserDivId, refiner) {

		constWidget = GIAPI.ConstraintsWidget(
			options.dabNode, {
			'keyDownAction': (function() { jQuery('#' + browseButtonId).click() }),
			'fieldsWidth': 255
		});

		var who;
		var what;
		var where;
		var from;
		var to;
		var len = refiner.chronology().length;
		var cur = refiner.cursor();
		if (len > 1 && cur === len - 2) {
			var chrono = refiner.chronology()[cur + 1];
			what = chrono.what || '';
			where = chrono.where || null;
			from = chrono.when && chrono.when.from || '';
			to = chrono.when && chrono.when.to || '';
			who = chrono.who;
		}

		constWidget.whatConstraint('add', { 'showHelpIcon': false, 'showOptDialog': false, 'value': what });

		var apply = where ? true : false;

		var where = constWidget.whereConstraint('add', {
			widgetPosition: 'bottom',
			'showHelpIcon': false,
			'value': where,
			'applyValue': apply
		});

		var from = constWidget.whenConstraint('get', 'from', { 'showHelpIcon': false, 'value': from });
		var to = constWidget.whenConstraint('get', 'to', { 'showHelpIcon': false, 'value': to });

		var css = "margin-left: 14px; margin-top: -5px;";
		var timeDiv = '<table style="' + css + '"><tr><td>' + from + '</td><td>' + to + '</td></tr></table>';

		// appends the basic constraints
		constWidget.append(browserDivId);

		jQuery('#' + browserDivId).append(timeDiv);


		jQuery('#' + constWidget.getId('what')).css('font-size', '85%');
		jQuery('#' + constWidget.getId('where')).css('font-size', '85%');
		//        jQuery('#'+constWidget.getId('where')).parent('div').parent('td').css('width','120px');

		jQuery('#' + constWidget.getId('from')).css('width', '123px');
		jQuery('#' + constWidget.getId('from')).css('font-size', '85%');
		jQuery('#' + constWidget.getId('from')).css('margin-left', '-15px');
		jQuery('#' + constWidget.getId('from')).parent('div').parent('td').css('width', '123px');

		jQuery('#' + constWidget.getId('to')).css('width', '123px');
		jQuery('#' + constWidget.getId('to')).css('font-size', '85%');
		jQuery('#' + constWidget.getId('to')).css('margin-left', '-15px');
		jQuery('#' + constWidget.getId('to')).parent('div').parent('td').css('width', '123px');



		//	    var help = 'Click to browse the content of the selected collections. You can filter the collections content by setting temporal and/or';
		//	    help +=' spatial extent and/or search terms';
		//	    var browseDiv = '<div>'+browseButton.div() + GIAPI.UI_Utils.helpImage('Browse selected collections',help,'margin-left: 5px')+'</div>';
		//	    
		//	    jQuery('#'+browserDivId).append(browseDiv);

	};

	return widget;

};
