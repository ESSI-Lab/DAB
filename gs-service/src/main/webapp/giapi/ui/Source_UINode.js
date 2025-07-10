/**
 * @module UI
 **/
import { GIAPI } from '../core/GIAPI.js';

/**
 *  
 * @class Source_UINode
 * @constructor
 *
 * @param {DABSource} dabNode
 * @param {String} id
 *
 */
GIAPI.Source_UINode = function(source, id) {

	var widget = {};

	var report = source.report();
	var where = report.where && report.where[0];

	var nodeMapId = GIAPI.random();
	var css = 'border: 1px solid #BDBDBD;';
	css += 'padding: 3px;';

	var mainDiv = '<table style="' + css + '">';
	mainDiv += '<tr>'

	//--------------------
	// link
	//	
	mainDiv += '<tr>';
	mainDiv += '<td colspan=2>';
	mainDiv += '<div style="margin-top:2px; font-size: 11px">';
	// Add Provider Page link first

	var providerId = report.id ? report.id : source.uiId;
	var view = (typeof config !== 'undefined' && config.view) ? config.view : '';
	var providerPageUrl = '/gs-service/stats/stats.jsp?view='+view+'&source=' + encodeURIComponent(providerId)+'&token='+config.token;
	mainDiv += '<a target="_blank" style="color:blue;text-decoration: none;" href="' + providerPageUrl + '">Provider Page</a>';
	mainDiv += ' &nbsp;|&nbsp; ';
	mainDiv += '<a href="#" id="zoom-to-area-' + id + '">Zoom to Area</a>';
	if (typeof config !== 'undefined' && config.showSourceEndpoint === true) {
		mainDiv += ' &nbsp;|&nbsp; ';
		mainDiv += '<a target="_blank" class="dont-break-out common-ui-node-report-content-table-right" style="color:blue;text-decoration: none;" href="' + report.online[0].url + '">Service URL</a>';
		mainDiv += ' <button id="copy-service-url-' + id + '" title="Copy Service URL" style="border:none;background:transparent;cursor:pointer;padding:0 4px;vertical-align:middle;"><i class="fa fa-copy"></i></button>';
	}
	mainDiv += '</div>';
	mainDiv += '</td>';
	mainDiv += '</tr>';

	if (report.harvested) {

		//--------------------
		// where
		//
		if (report.where) {
			var width = report.when ? '375' : '450';
			mainDiv += '<td style="height: 140px; width: ' + width + 'px;">'
			var mapDiv = '<div style="margin-top:3px; height: 140px; width: 100%"  id="' + nodeMapId + '"></div>';
			mainDiv += mapDiv;
			mainDiv += '</td>'
		}

		//--------------------
		// when
		//	
		if (report.when) {
			mainDiv += '<td>'
			mainDiv += '<div>' + GIAPI.Common_UINode.whenDiv('from', report) + '</div>';
			mainDiv += '<div style="margin-top: 10px">' + GIAPI.Common_UINode.whenDiv('to', report) + '</div>';
			mainDiv += '</td>'
		}
		mainDiv += '</tr>';

		//--------------------
		// keywords
		//
		if (report.keyword || report.thesaurusKeyword) {
			mainDiv += '<tr>';
			mainDiv += '<td colspan=2>';
			mainDiv += '<div style="margin-top:3px; margin-bottom:3px; font-size: 11px"><label>Keywords: </label></div>';
			var css = 'margin-left: -27px;';
			css += 'max-height: 200px;';
			css += 'font-size: 11px;';
			css += 'overflow: auto;';
			mainDiv += '<div style="' + css + '"><ul>';

			if (report.thesaurusKeyword) {
				for (var i = 0; i < report.thesaurusKeyword.length; i++) {
					var keyword = report.thesaurusKeyword[i].keyword;
					for (var j = 0; j < keyword.length; j++) {
						mainDiv += '<li><label>' + keyword[j] + '</label></li>';
					}
				}
			} else {
				for (var i = 0; i < report.keyword.length; i++) {
					mainDiv += '<li><label>' + report.keyword[i] + '</label></li>';
				}
			}

			mainDiv += '</ul></div>';
		}

		//	mainDiv += '<textarea style="font-size:11px; width: 100%">'+report.online[0].url+'</textarea>';
		mainDiv += '</td>';
		mainDiv += '</tr>';
	}

	mainDiv += '</table>';
	jQuery('#' + id).append(mainDiv);

	widget.updateMap = function() {

		if (where) {
			var nodeMapWidget = GIAPI.NodeMapWidget(
				nodeMapId,
				source,
				{
					'mode': GIAPI.Common_UINode.mapMode || 'al',
					'zoom': 9,
					'height': 140,
					'coordinatesDialogPosition': 'right'
				});
			nodeMapWidget.init();
		};
	};

	widget.div = function() {

		return mainDiv;
	};

	// After jQuery('#' + id).append(mainDiv);
	jQuery(document).on('click', '#copy-service-url-' + id, function() {
		const url = report.online[0].url;
		if (navigator.clipboard) {
			navigator.clipboard.writeText(url).then(function() {
				const btn = jQuery('#copy-service-url-' + id + '');
				const originalTitle = btn.attr('title');
				btn.attr('title', 'Copied!');
				btn.find('i').removeClass('fa-copy').addClass('fa-check');
				setTimeout(function() {
					btn.attr('title', originalTitle);
					btn.find('i').removeClass('fa-check').addClass('fa-copy');
				}, 1500);
			});
		} else {
			// fallback for older browsers
			const tempInput = $('<input>');
			$('body').append(tempInput);
			tempInput.val(url).select();
			document.execCommand('copy');
			tempInput.remove();
			const btn = jQuery('#copy-service-url-' + id + '');
			const originalTitle = btn.attr('title');
			btn.attr('title', 'Copied!');
			btn.find('i').removeClass('fa-copy').addClass('fa-check');
			setTimeout(function() {
				btn.attr('title', originalTitle);
				btn.find('i').removeClass('fa-check').addClass('fa-copy');
			}, 1500);
		}
	});

	jQuery(document).on('click', '#zoom-to-area-' + id, function(e) {
		e.preventDefault();
		var bboxUrl = '/gs-service/stats/bbox.jsp?view=' + encodeURIComponent(view) + '&source=' + encodeURIComponent(providerId);
		fetch(bboxUrl)
			.then(function(response) { return response.json(); })
			.then(function(bbox) {
				
				// bbox should be {west, south, east, north}
					window.GIAPI.zoomToBoundingBox(bbox);
			})
			.catch(function() {
				alert('Could not retrieve bounding box for this provider.');
			});
	});

	return widget;


};
