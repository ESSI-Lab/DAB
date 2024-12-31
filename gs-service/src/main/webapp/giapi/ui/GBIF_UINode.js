/**
 * @module UI
 **/


/**
 *  This class extends <code>{{#crossLink "UINode"}}{{/crossLink}}</code> in order to provide a specific graphical representation of 
 *  <code>{{#crossLink "GINode"}}nodes{{/crossLink}}</code> originating from the <i>GBIF</i> <code>{{#crossLink "DABSource"}}source{{/crossLink}}</code> 
 *  as depicted in the below snapshots.<br>
 *  The following CSS is required:<pre><code>
&lt;!-- API CSS --&gt; 
&lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
</code></pre>

 *  <img style="width: 1000px;border: none;" src="../assets/img/species-node.png" /><br>
	<i>The image above shows a <code>GBIF_UINode</code> which renders a GBIF resource of type "species/higher taxa"</i> 

 *  <img style="width: 1000px;border: none;" src="../assets/img/occurrence-node.png" /><br>
	<i>The image above shows a <code>GBIF_UINode</code> which renders a GBIF resource of type "occurrence"</i> 
  
	<img style="width: 1000px;border: none;" src="../assets/img/dataset-node.png" /><br>
	<i>The image above shows a <code>GBIF_UINode</code> which renders a GBIF resource of type "dataset"</i> 

This {{#crossLink "UINode"}}{{/crossLink}} implementation requires to set some mandatory global properties
as depicted in the following code snippet:
	
<pre><code> 
var resultSetLayout = GIAPI.ResultSetLayout(id,{
   // registers this class to the layout
	   'uiNodes': [GIAPI.GBIF_UINode],
 
		 // ...other default ResultSetLayout properties 
   });
</code></pre>

 *  @constructor 
 *  @class GBIF_UINode
 *  @extends Common_UINode
 */
GIAPI.GBIF_UINode = function(options) {

	var uiNode = GIAPI.Common_UINode(options);

	// -------------------------------------------
	// creates the aside dom only for the datasets 
	//
	options.asideDom = function(node, options, asideId) {

		var out = null;

		if (node.report().GBIF_Dataset && node.report().where) {

			var nodeMapId = 'node-map-' + node.uiId;
			out = '<div id=' + nodeMapId + ' class="common-ui-node-node-map-div"></div>';
		}

		return out;
	};

	// -----------------------------------------
	// appends the NodeMapWidget in the datasets 
	//
	options.onAsideReady = function(aside, node) {

		var nodeMapId = 'node-map-' + node.uiId;
		var nodeMapWidget = GIAPI.NodeMapWidget(
			nodeMapId,
			node,
			{
				'mode': GIAPI.Common_UINode.mapMode || 'al',
				'zoom': 9,
				'height': 200
			});
		nodeMapWidget.init();
	};

	// --------------------------------------------
	// appends the NodeMapWidget in the occurrences 
	//
	options.onSectionReady = function(aside, node) {

		if (node.report().type === 'simple') {

			var nodeMapWidget = GIAPI.NodeMapWidget(
				'node-map-' + node.uiId,
				node,
				{
					'mode': 'al',
					'zoom': 9,
					'height': 140
				});
			nodeMapWidget.init();
		}
	};

	options.sectionDom = function(node, options, sectionId) {

		var report = node.report();
		var reportId = report.id;
		var type = report.type;
		var title = report.title;
		var desc = report.description === 'none' || !report.description ? 'No descripion available' : report.description;
		var ovr = report.overview;
		var type = report.type;
		var keyword = report.keyword;

		var rowDivId = GIAPI.random();
		var datasetDivId = GIAPI.random();

		var sectionDivId = 'section-div-' + node.uiId;
		var div = '<div id="' + sectionDivId + '" class="common-ui-node-report-div">';

		// this anchor is used to localize this ui node when clicking on a marker of the result map
		div += '<a name="node-' + node.uiId + '"/>';

		switch (type) {
			// -----------
			// OCCURRENCES
			// -----------
			case 'simple':

				jQuery(document).on('mouseover', '#' + sectionDivId, function() {
					if (options.mapWidget) {
						options.mapWidget.markerIcon(node, { url: 'https://api.geodab.eu/docs/assets/img/circle-yellow-marker.png' });
					}
				});

				jQuery(document).on('mouseout', '#' + sectionDivId, function() {
					if (options.mapWidget) {
						options.mapWidget.markerIcon(node, { color: 'red' });
					}
				});

				var topTable = '<table><tr>';

				// inserts the classification table
				topTable += '<td>' + createOccurrenceTable(report);
				topTable += '<br>';

				// inserts the classification table
				topTable += createClassificationTable(report.GBIF_Occurrence);
				topTable += '</td>';

				if (node.report().where) {
					topTable += '<td><div style="width:300px" id="node-map-' + node.uiId + '" class="common-ui-node-node-map-div"></div></td>';
				}
				topTable += '</tr></table>';

				div += topTable;

				// inserts the overview
				if (ovr) {
					div += '<br>';
					for (var i = 0; i < ovr.length; i++) {

						var divId = 'overview_div_' + GIAPI.random();
						div += '<div style="display:inline-block" class="common-ui-node-overview-div" id="' + divId + '" ></div>';

						GIAPI.UI_Utils.loadOverview(node, divId, { 'index': i });
					}
					div += '<br>';
				}

				div += '<div class="gbif-ui-node-occ-button-div">';

				// inserts the download occurrence button          
				if (node.directAccessLinks().length > 1) {

					var link = node.directAccessLinks()[1];
					var darwinButton = GIAPI.FontAwesomeButton({
						//	  			     'width': 200,
						'label': 'DARWIN CORE ARCHIVE',
						'icon': 'fa-download',
						'attr': [{ 'name': 'link', 'value': link }, { 'name': 'title', 'value': 'Download the Darwin Core Archive' }],
						'handler': function() {
							var link = jQuery(this).attr('link');
							window.open(link);
						}
					});
					darwinButton.css('div', 'margin-top', '10px');
					darwinButton.css('div', 'margin-right', '5px');
					darwinButton.css('div', 'padding-right', '10px');

					div += '<div style="display:inline-block">' + darwinButton.div() + '</div>';
				}

				// inserts the JSON occurrence button               
				var link = node.directAccessLinks()[0];
				var jsonButton = GIAPI.FontAwesomeButton({
					//  			     'width': 120,
					'label': 'FULL JSON',
					'icon': 'fa-file-text-o',
					'attr': [{ 'name': 'link', 'value': link }, { 'name': 'title', 'value': 'Download in JSON format' }],
					'handler': function() {
						var link = jQuery(this).attr('link');
						window.open(link);
					}
				});
				jsonButton.css('div', 'margin-top', '10px');
				jsonButton.css('div', 'background', '#467fc9');
				jsonButton.css('div', 'padding-right', '10px');

				div += '<div style="display:inline-block">' + jsonButton.div() + '</div>';

				var datasetKey = report.GBIF_Occurrence.datasetKey;

				// inserts the DATASET button
				if (datasetKey) {

					var dsButtonId = 'dataset-button-' + node.uiId;

					// the keyword to get a dataset search
					var what = 'datasetKey=' + datasetKey;

					div += '<div style="vertical-align: middle;display:inline-block; margin-left:10px">' + GIAPI.ButtonsFactory.onOffSwitchButton('DATASET', 'DATASET', {
						'id': dsButtonId,
						'size': 'medium',
						'attr': [{ 'name': 'what', 'value': what }],
						'handler': function() {

							var w = jQuery(this).attr('what');
							var empty = !jQuery('#' + datasetDivId + ' div')[0];

							if (empty && !jQuery(this).prop('checked')) {

								options.dabNode.discover(function(response) {

									var resultSet = response[0];
									var paginator = resultSet.paginator;
									var node = paginator.page().next();

									jQuery('#' + datasetDivId).empty();

									// creates the first subsection                   	 
									var div = '<div style="margin-top:5px; ">' + datasetSubSection1(node);

									// creates the second subsection
									div += datasetSubSection2(node) + '</div>';

									jQuery('#' + datasetDivId).append(div);

								}, { 'what': w });
							}

							jQuery("#" + datasetDivId).slideToggle({
								duration: 800,
							});
						}
					}) + '</div>';

					// closes the gbif-ui-node-occ-button-div div
					div += '</div>';

					// div which containts the dataset info
					div += '<div class="common-ui-node-report-content-div" id="' + datasetDivId + '"></div>';
				} else {
					// closes the gbif-ui-node-occ-button-div div
					div += '</div>';
				}

				break;

			case 'composed':
				if (report.GBIF_Dataset) {
					// --------
					// DATASETS
					// --------

					// creates the first subsection                   	 
					div += datasetSubSection1(node);

					// creates the second subsection
					div += datasetSubSection2(node);

					// download
					if (report.online) {
						for (var o = 0; o < report.online.length; o++) {

							var link = report.online[o].url;
							var label = "DOWNLOAD";

							if (report.format && report.format[o]) {
								label = report.format[o].toUpperCase();
							}

							var format = report.format[o] ? (' in ' + report.format[o] + ' format') : '';

							var downloadButton = GIAPI.FontAwesomeButton({
								'width': GIAPI.UI_Utils.getTextWidth(label, '"Helvetica Neue", Helvetica, Arial, sans-serif;') + 100,
								'label': label,
								'icon': 'fa-download',
								'attr': [{ 'name': 'link', 'value': link }, { 'name': 'title', 'value': 'Download ' + format }],
								'handler': function() {
									var link = jQuery(this).attr('link');
									window.open(link);
								}
							});
							downloadButton.css('div', 'background', '#447700');
							downloadButton.css('div', 'margin-right', '3px');

							div += '<div style=" display:inline-block">' + downloadButton.div() + '</div>';
						}
						div += '<br>';
					}

					// retrieves the taxonKey of this dataset
					var taxonKey = report.GBIF_Dataset.taxonKey;

					// creates the constraints widget to filter the species occurrences
					var constWidget = GIAPI.ConstraintsWidget(options.dabNode, {

						'fieldsWidth': 350
					});

					var occButton = createOccurrencesButton(reportId, constWidget, 'GET DATASET OCCURRENCES', taxonKey);

					// creates the occurrences count div		         		         
					var count = report.GBIF_Dataset.occurrencesCount;

					var countDiv = '<div class="gbif-ui-node-occurrence-count-div">';
					countDiv += '<label>Occurrences count: <b>' + count + '</b></div>';

					// creates the species occurrences filters div
					div += createFiltersDiv(constWidget, occButton, 'GET DATASET OCCURRENCES', countDiv);

				} else {
					// -------
					// SPECIES
					// -------

					var headerTable = '<table><tr>';
					headerTable += '<td>';

					// inserts the overview
					var ovrDiv = '<div style="height: 100px;">';
					if (ovr) {
						for (var i = 0; i < ovr.length; i++) {

							var divId = 'overview_div_' + GIAPI.random();
							ovrDiv += '<div style="display:inline-block" class="common-ui-node-overview-div" id="' + divId + '" ></div>';

							GIAPI.UI_Utils.loadOverview(node, divId,
								{
									'backgroundURL': 'http://2.maps.nlp.nokia.com/maptile/2.1/maptile/newest/normal.day.grey/0/0/0/256/png8?app_id=_peU-uCkp-j8ovkzFGNU&app_code=gBoUkAMoxoqIWfxWA5DuMQ',
									'index': i
								});
						}
					} else {
						ovrDiv += '<img src="https://api.geodab.eu/docs/assets/img/no_overview_576.png" width="96" height="96">';
					}

					headerTable += ovrDiv;
					headerTable += '</td>';

					headerTable += '<td style="width:100%"><label class="common-ui-node-report-title">' + title + '</label><br>';
					var href = '';
					//	            if(desc.length > 700){
					//	       		 
					//		           	 var shortDesc = (desc.substr(0,700)+'...');
					//		       		 
					//		       		 var labelId = GIAPI.random();
					//		           	 href = '<label id='+labelId+' style="cursor: pointer; text-decoration: underline; color:blue; display:inline-block">view more</label>';
					//		           	 
					//		           	 jQuery(document).on('click','#'+labelId, function(){
					//		           		 
					//	           		 GIAPI.UI_Utils.dialog('open',{
					//	           			  height : 300,
					//	           			  width : 600,    
					//	           			  title: report.title,
					//	           			  message: report.description,
					//	           			  maximize: true 
					//	           		    });	           		  
					//		           	 });
					//		           	 
					//		           	 desc = shortDesc;
					//		       	 }
					headerTable += '<textarea style="margin-top: 5px;" rows="4" class="common-ui-node-report-desc" readonly>' + desc + '</textarea>';
					headerTable += href;
					headerTable += '<br><br>';
					headerTable += '</td>';

					headerTable += '</tr></table>';

					div += headerTable;

					// inserts the classification table
					div += '<div style="margin-top: 10px;">' + createClassificationTable(report.GBIF_Species) + '</div><br>'

					// retrieves the number of occurrences 
					if (report.GBIF_Species.nubKey) {
						// get the nubKey
						var nubKey = report.GBIF_Species.nubKey;
						// set the taxonKey property
						uiNode.taxonKey = nubKey;
					}

					// id to append the occurrences count div
					uiNode.occCountDivId = GIAPI.random();

					// creates the constraints widget to filter the species occurrences
					var constWidget = GIAPI.ConstraintsWidget(options.dabNode, {

						'fieldsWidth': 350
					});

					// creates the occurrences button		     	 
					//	       	     var buttonLabel = 'GET '+title.toUpperCase()+' OCCURRENCES';	    
					var buttonLabel = 'GET OCCURRENCES';
					var occButton = createOccurrencesButton(reportId, constWidget, buttonLabel);

					// creates the species occurrences filters div
					div += createFiltersDiv(constWidget, occButton, buttonLabel);

					div += '</div>';
				}
		}

		return div;
	};

	var createOccurrencesButton = function(id, constWidget, label, datasetTaxonKey) {

		var occButton = GIAPI.FontAwesomeButton({
			//			    'width': GIAPI.UI_Utils.getTextWidth(label,'"Helvetica Neue", Helvetica, Arial, sans-serif;') + 100,
			'label': label,
			'icon': 'fa-paw',
			'attr': [{ 'name': 'parent', 'value': id }],
			'handler': function() {

				GIAPI.UI_Utils.discoverDialog('open');

				var constrains = datasetTaxonKey ?
					{
						'who': jQuery(this).attr('parent'),
						'kvp': { 'key': 'kwd', 'value': 'taxonKey%3D' + datasetTaxonKey },
						'where': constWidget.constraints().where,
						'when': constWidget.constraints().when,
					} :
					{
						'who': jQuery(this).attr('parent'),
						'where': constWidget.constraints().where,
						'when': constWidget.constraints().when,
						'what': constWidget.constraints().what,
					};

				var opt = datasetTaxonKey ?
					{
						'pageSize': GIAPI.GBIF_UINode.pageSize,
					} :
					{
						'pageSize': GIAPI.GBIF_UINode.pageSize,
						'merge': false
					};

				var resultSet = options.response[0];
				if (resultSet.refiner) {

					resultSet.refiner.refine(
						options.onDiscoverResponse,
						constrains,
						opt
					);
				} else {
					options.dabNode.discover(
						options.onDiscoverResponse,
						constrains,
						{
							'pageSize': GIAPI.GBIF_UINode.pageSize
						}
					);
				}
			}
		});
		occButton.css('div', 'padding-top', '10px');
		occButton.css('div', 'padding-bottom', '10px');
		occButton.css('div', 'padding-right', '15px');
		occButton.css('div', 'font-size', '110%');

		return occButton;
	};

	var createFiltersDiv = function(constWidget, occButton, buttonLabel, datasetOccDiv) {

		// occurrences filters button
		var filtersDivId = GIAPI.random();
		var filtersButton = GIAPI.ToggleButton({
			'width': 190,
			'targetId': filtersDivId,
			'offLabel': 'OCCURRENCES FILTERS',
			'onLabel': 'OCCURRENCES FILTERS'
		});
		filtersButton.css('div', 'margin-top', '5px');

		var divCSS = 'position: relative; display:inline-block';
		var div = '<div class="gbif-ui-node-filters-button-div" style="' + divCSS + '">' + occButton.div();

		if (!datasetOccDiv) {

			div += '<div class="gbif-ui-node-occurrence-count-div">';
			div += '<label id="' + uiNode.occCountDivId + '">Calculating occurrences...</label></div>';
		} else {
			div += '<div style="margin-left: 5px;display:inline-block">' + datasetOccDiv + '</div>';
		}

		div += filtersButton.div() + '</div><br>';

		// creates the species filters div
		var filtersDiv = '<div class="gbif-ui-node-filters-div" id="' + filtersDivId + '" style="width:100%; padding-bottom: 5px; display:none">';

		if (!datasetOccDiv) {
			filtersDiv += '<div style="margin-bottom:-5px">' + constWidget.whatConstraint('get', {
				'showOptDialog': false,
				'label': 'Occurrence keyword'
			}) + '</div>';
		}

		var whereConst = constWidget.whereConstraint('get', {
			'resultsMapWidget': options.mapWidget,
			'widgetModal': true
		});

		filtersDiv += '<div style="margin-bottom:-5px">' + whereConst + '</div>';
		filtersDiv += '<div style="margin-bottom:-5px">' + constWidget.whenConstraint('get', 'from') + '</div>';
		filtersDiv += '<div style="margin-bottom:-5px">' + constWidget.whenConstraint('get', 'to') + '</div>';
		filtersDiv += '</div>';

		div += filtersDiv;

		return div;
	};

	/**
   * Return <code>true</code> if this <code>{{#crossLink "GINode"}}{{/crossLink}}</code> 
   * {{#crossLink "Report/id:property"}}report.id{{/crossLink}} contains the word "GBIF" 
   * 
   * @method isRenderable
   * @return <code>true</code> if this <code><a href="../classes/GINode.html">GINode</a></code> 
   * <a href="../classes/Report.html#property_id">report.id</a> contains the word "GBIF" 
   */
	uiNode.isRenderable = function(node) {

		return node.report().id.indexOf('GBIF') > -1;
	};

	// -----------------------------------------------------------
	// private methods -------------------------------------------
	// -----------------------------------------------------------

	var createOccurrenceTable = function(report) {

		var table = '<table class="classification-table" id="occTable">';
		table += '<tr><th colspan="5">' + report.title + '</th></tr>';
		table += '<tr><th colspan="5">' + GIAPI.UI_Utils.separator('width:100%') + '</th></tr>';
		table += '<tr><th>ID</th><th>Event date</th><th>Country</th><th>Lat</th><th>Lon</th></tr>';

		var occurrenceKey = report.GBIF_Occurrence.key;

		// 0: ID
		table += '<tr>';
		table += '<td style="font-size:90%;">' + occurrenceKey + '</td>';

		// 1: Event date
		if (report.when) {
			table += '<td style="font-size:90%;">' + report.when[0].from + '</td>';
		} else {
			table += '<td style="font-size:90%;">-</td>';
		}

		// 2: country
		var country = report.GBIF_Occurrence.country;
		if (country) {
			table += '<td style="font-size:90%;">' + country + '</td>';
		} else {
			table += '<td style="font-size:90%;">-</td>';
		}

		// 3: lat
		// 4: lon
		if (report.where) {
			var s = report.where[0].south ? report.where[0].south : '-';
			table += '<td style="font-size:90%;">' + s + '</td>';
			var w = report.where[0].west ? report.where[0].west : '-';
			table += '<td style="font-size:90%;">' + w + '</td>';
		} else {
			table += '<td style="font-size:90%;">-</td>';
			table += '<td style="font-size:90%;">-</td>';
		}

		table += '</tr></table>';
		return table;
	};

	var createClassificationTable = function(extension) {

		var table = '<table class="classification-table" id="classifTable">';
		table += '<tr><th>Genus</th><th>Family</th><th>Order</th><th>Class</th><th>Phylum</th><th>Kingdom</th></tr>';

		if (extension) {
			var values = [];
			// 0: genus
			// 1: family
			// 2: order
			// 3: class
			// 4: phylum
			// 5: kingdom
			values[0] = extension.genus;
			values[1] = extension.family;
			values[2] = extension.order;
			values[3] = extension.clazz;
			values[4] = extension.phylum;
			values[5] = extension.kingdom;

			for (var i = 0; i < values.length; i++) {
				var value = values[i];
				if (!value) {
					value = '-';
				}
				if (i === values.length) {
					table += '</tr>';
				} else {
					table += '<td style=" font-size:90%;">' + value + '</td>';
				}
			}
		}

		table += '</table>';
		return table;
	};

	var datasetSubSection1 = function(node) {

		var report = node.report();

		var desc = report.description;
		if (!desc || desc === 'none') {
			desc = 'No description available';
		}
		var title = report.title;

		// this anchor is used to localize this ui node when clicking on a marker of the result map
		var out = '<a name="node-' + node.uiId + '"/>';

		out += '<label class="common-ui-node-report-title">' + title + '</label><br><br>';
		out += '<textarea rows="4" class="common-ui-node-report-desc" readonly>' + desc + '</textarea>';

		return out;
	};

	var datasetSubSection2 = function(node) {

		var out = '';
		var report = node.report();

		var table = '<table style=" " class="common-ui-node-report-content-table"><tbody>';

		var altTitle = report.alternateTitle;
		if (altTitle) {
			table += '<tr><td class="common-ui-node-report-content-table-left-td"><label class="common-ui-node-report-content-table-left">Citation</td>';
			table += '<td class="common-ui-node-report-content-table-right-td">';
			table += '<label class="common-ui-node-report-content-table-right">' + altTitle + '</label></td></tr>';
		}

		// author
		table += GIAPI.Common_UINode.authorRow(report) + '<br><br>';

		// created
		table += GIAPI.Common_UINode.createdRow(report);

		// when
		table += GIAPI.Common_UINode.whenRow(report);

		table += '</tbody></table><br>';

		out += '<div style="display:initial; border:none" class="common-ui-node-report-content-div" >' + table + '</div>';

		return out;
	};

	return uiNode;
};

/**
 * This static function provides an implementation for the <a href="../classes/ResultSetLayout.html#onUpdateReady">onUpdateReady</a> function.<br>
 * Set the number of occurrences in all the <a href="../classes/UINode.html#method_render">rendered</a> 
 * <code>GBIF_UINode</code> which represents resources of type <i>species</i> or <i>dataset</i>
 * 
 * @static
 * @method GIAPI.GBIF_UINode.onUpdateReady
 * @param {DAB} instance of <a href="../classes/DAB">DAB</a> node
 * @param {UINode[]} array with the <a href="../classes/UINode.html#method_render">rendered nodes</a> 
 */
GIAPI.GBIF_UINode.onUpdateReady = function(dabNode, renderedNodes) {

	//if the function contains only one parameter
	if (arguments.length == 1) {
		renderedNodes = dabNode;
	}

	// aggregates all the keys
	var keys = [];
	renderedNodes.forEach(function(uiNode) {
		if (uiNode.taxonKey) {
			keys.push(uiNode.taxonKey);
		}
	});

	if (!keys.length) {
		return;
	}

	// executes a single query with the aggregated keys
	var endpoint = GIAPI.GBIF_UINode.dabNode.endpoint();
	endpoint = endpoint.endsWith('/') ? endpoint : endpoint + '/';

	var servicePath = dabNode.servicePath();

	var query = endpoint + servicePath + '/opensearch?reqID=' + GIAPI.random() + '&occurrencesCount=' + keys;

	jQuery.ajax({

		type: 'GET',
		url: query,
		crossDomain: true,
		dataType: 'jsonp',

		success: function(data, status, jqXHR) {
			if (data.response) {
				data.response.forEach(function(o) {
					var key = o.taxonKey;
					renderedNodes.forEach(function(uiNode) {
						if (!uiNode.taxonKey) {
							// unknown
							GIAPI.GBIF_UINode.occurrenceCount(uiNode);
						} else if (uiNode.taxonKey === key) {
							// set the occurrence count
							GIAPI.GBIF_UINode.occurrenceCount(uiNode, o.count);
							return;
						}
					});
				});
			};
		}
	});
};

////
// if this node renders a <i>GBIF</i> "species/higher taxa", 
// updates the small info panel wich reports the number of related occurrences.
// note: the current implementation of the onUpateReady function makes the same
// call of this method several times
////
GIAPI.GBIF_UINode.occurrenceCount = function(uiNode, count) {

	if (!count) {
		count = 'unknown';
	} else {
		count = GIAPI.thousandsSeparator(count);
	}

	jQuery('#' + uiNode.occCountDivId).html('Available occurrences: <b>' + count + '</b>');
};