/**
 * @module UI
 **/
import { GIAPI } from '../core/GIAPI.js';

/**
 * A widget to control and visualize a <a href="../classes/TermFrequency.html" class="crosslink">TermFrequency</a> object.<br> 
 * The widget control is based on the <a href="http://www.jtable.org/">JTable.org</a> plugin and requires the following CSS (scripts are included in the minified version of the API):
 * <pre><code>&lt;!-- JTable CSS --&gt;
 * &lt;link href="http://www.jtable.org/Content/highlight.css" rel="stylesheet" type="text/css" /&gt;
 * &lt;link href="http://www.jtable.org/Scripts/jtable/themes/jqueryui/jtable_jqueryui.css" rel="stylesheet" type="text/css" /&gt;<br>
 * &lt;!-- API CSS --&gt;
 * &lt;link href="https://api.geodab.eu/docs/assets/css/giapi.css" rel="stylesheet" type="text/css" /&gt;<br>
 </pre></code>
 The graphics are created using <a href="http://www.chartjs.org/">Charts.js</a> library and the scripts are included in the minified version of the API.<br>
 
 <h3> Control widget </h3>
 
 The control widget is composed by one or more <a href="http://www.jtable.org/" target=_blank>table</a> depending on the 
 <a href="../classes/TermFrequency.html#method_targets" class="crosslink">targets</a> of the provided 
 <a href="../classes/TermFrequency.html" class="crosslink">TermFrequency</a> object;
  there is a <a href="http://www.jtable.org/" target=_blank>table</a> for each <a href="../classes/TermFrequency.html#termfreq" target=_blank>term frequency target</a>.<br>
 The image below shows a widget of a <a href="../classes/TermFrequency.html" class="crosslink">TermFrequency</a> object having
 3 <a href="../classes/TermFrequency.html#termfreq">term frequency targets</a>: "keyword","format" and "protocol".<br><br>
 <img style="border: none;width: 1331px;" src="../assets/img/tf.png" /><br>
 
 The <a href="http://www.jtable.org/" target=_blank>table</a> caption corresponds to the related <a href="../classes/TermFrequency.html#termfreq">term frequency target</a>. 
 Each <a href="http://www.jtable.org/" target=_blank>table</a> has a checkbox for 
 each <a href="../classes/TermFrequencyItem.html" class="crosslink">term frequency item</a>; 
 the <a href="../classes/TermFrequency.html#property_term" class="crosslink">term</a> is in the middle and the 
 <a href="../classes/TermFrequency.html#property_freq" class="crosslink">frequency</a> on the right.<br>
 When the checkbox of a <a href="../classes/TermFrequencyItem.html" class="crosslink">term frequency item</a>
 is <a href="../classes/TermFrequency.html#method_checkItems" class="crosslink">checked</a>, the 
 <a href="../classes/TermFrequency.html#method_refine" class="crosslink">refine method</a> is called including all the 
 <a href="../classes/TermFrequency.html#method_checkedItems" class="crosslink">checked items</a>.<br>
 
 <h3> Visualization widget </h3>
 
 The optional visualization widget is composed by one or more <a href="http://www.chartjs.org/docs/#bar-chart">Bar Chart</a> depending on the 
 <a href="../classes/TermFrequency.html#method_targets" class="crosslink">targets</a> of the provided 
 <a href="../classes/TermFrequency.html" class="crosslink">TermFrequency</a> object; as for the control widget,
  there is a <a href="http://www.chartjs.org/docs/#bar-chart">bar chart</a> for each <a href="../classes/TermFrequency.html#termfreq" target=_blank>term frequency target</a>.<br>
  The charts are inserted in a <a href="https://jqueryui.com/tabs/">JQuery UI Tabs</a>, with a tab for each chart as depicted in the image below.<br><br>
  <img style="border: none;width: 1024px;" src="../assets/img/chart.png" /><br>
  
  Each <a href="http://www.chartjs.org/docs/#bar-chart">chart</a> has a bar for 
 each <a href="../classes/TermFrequencyItem.html" class="crosslink">term frequency item</a>; 
 the <a href="../classes/TermFrequency.html#property_term" class="crosslink">terms</a> are the label on the x axis, while the 
 <a href="../classes/TermFrequency.html#property_freq" class="crosslink">frequency</a> are the values on the y axis.<br>
 Click on a bar is equivalent to the selection with the control widget; when the bar of a <a href="../classes/TermFrequencyItem.html" class="crosslink">term frequency item</a>
 is <a href="../classes/TermFrequency.html#method_checkItems" class="crosslink">clicked</a>, the 
 <a href="../classes/TermFrequency.html#method_refine" class="crosslink">refine method</a> is called including all the 
 <a href="../classes/TermFrequency.html#method_checkedItems" class="crosslink">checked items</a>.<br>
 The visualization widget can be enabled and initialized with the <a href="../classes/TermFrequencyWidget.html#method_chart" class="crosslink">chart</a> method 
 <i>before the calling of the <a href="../classes/TermFrequencyWidget.html#method_init" class="crosslink">init</a> method</i>
  
  <pre><code>
  // something to do before the discover is refined
  var beforeRefine = function(){
  ...
  };<br>
  // something to do after the discover has been refined               
  var afterRefine = function(){
  ...
  };<br>
  // creates the widget with the default options             
  var freqWidget = GIAPI.TermFrequencyWidget(id, beforeRefine, afterRefine,{
	 'headerBckColor': '#2C3E50', 
	'headerBorder': '1px solid #2C3E50', 
	'headerLabelColor': '#fff',
	'headerLabelWeight':'bold',
	'headerLabelFontFamily':'Helvetica Neue, Helvetica, Arial, sans-serif',<br>
	'itemLineColor': '#ddd',
	'itemLabelColor': 'black',
	'itemLabelWeight': 'normal',
	'itemLabelFontFamily': 'Helvetica Neue, Helvetica, Arial, sans-serif'
  });<br>
  // enables the visualization control with the default options and styles
  freqWidget.chart('chart');<br>
  // ...<br>
  var onDiscoverResponse = function(response) {
		var resultSet = response[0];
		...
		// updates the widget with the current result set
		freqWidget.update(resultSet);
		...
  }</pre></code>


 * @class TermFrequencyWidget
 * @constructor
 * 
 * @param {String} id id of an existent HTML container (typically <code>&lt;div&gt;</code> element) in which the control widget is inserted
 * @param {Function} beforeRefine a function called when a checkbox or a bar is clicked, before the <a href="../classes/TermFrequency.html#method_refine" class="crosslink">refine method</a> is called
 * @param {Function} afterRefine a function called when a checkbox or a bar is clicked, after the <a href="../classes/TermFrequency.html#method_refine" class="crosslink">refine method</a> is called
 * 
 * @param {Object} [options] 
 * 
 * @param {String} [options.divCSS='width: 350px;display: inline-block;margin-left: 5px;margin-bottom: 5px;'] CSS of the &lt;div&gt; which contains the widget tables 

 * @param {String} [options.headerBckColor='#2C3E50']
 * @param {String} [options.headerBorder='1px solid #2C3E50']
 * @param {String} [options.headerLabelColor='white']
 * @param {String} [options.headerLabelWeight='bold'] 
 * @param {String} [options.headerLabelFontFamily='Helvetica Neue, Helvetica, Arial, sans-serif'] 
 * 
 * @param {String} [options.itemBckColor]
 * @param {String} [options.itemLineColor='#ddd']
 * @param {String} [options.itemLabelFontSize='100%']
 * @param {String} [options.itemLabelColor='black']
 * @param {String} [options.itemLabelWeight='normal']
 * @param {String} [options.itemLabelFontFamily='Helvetica Neue, Helvetica, Arial, sans-serif;']
 *
 **/
GIAPI.TermFrequencyWidget = function(id, beforeRefine, afterRefine, options) {

	var widget = {};
	// map target -> jtable div id
	var jtables = {};
	// map jtable div id -> target
	var idToTargetMap = {};
	// map chart div id -> chart
	var charts = {};
	// map target -> chart
	var chartData = {
		source: {},
		format: {},
		keyword: {},
		protocol: {},
		instrumentId: {},
		platformId: {},
		origOrgId: {},
		attributeId: {},
		attributeURI: {},
		sscScore: {}
	};
	var chartTabId;
	var chartStyles;
	var chartOptions;

	if (!options) {
		options = {};
	}

	if (!options.divCSS) {
		options.divCSS = 'width: 350px;display: inline-block;margin-left: 5px;margin-bottom: 5px;';
	}

	if (!options.headerBckColor) {
		options.headerBckColor = '#2C3E50';
	}

	if (!options.headerBorder) {
		options.headerBorder = '1px solid #2C3E50';
	}

	if (!options.headerLabelColor) {
		options.headerLabelColor = 'white';
	}

	if (!options.headerLabelWeight) {
		options.headerLabelWeight = 'bold';
	}

	if (!options.headerLabelFontFamily) {
		options.headerLabelFontFamily = 'Helvetica Neue, Helvetica, Arial, sans-serif';
	}

	if (options.itemBckColor) {
		options.itemBckColor = 'background: ' + options.itemBckColor;
	} else {
		options.itemBckColor = '';
	}

	if (!options.itemLabelColor) {
		options.itemLabelColor = 'black';
	}

	if (!options.itemLabelFontSize) {
		options.itemLabelFontSize = '100%';
	}

	if (!options.itemLabelWeight) {
		options.itemLabelWeight = 'normal';
	}

	if (!options.itemLabelFontFamily) {
		options.itemLabelFontFamily = 'Helvetica Neue, Helvetica, Arial, sans-serif';
	}

	if (options.itemLineColor) {
		options.itemLineColor = 'border-bottom: 1px solid ' + options.itemLineColor;
	} else {
		options.itemLineColor = 'border-bottom: none';
	}

	if (options.accordionMode === null || options.accordionMode === undefined) {
		options.accordionMode = false;
	}


	var style = '';
	style += '[widget="tf"] div > div.ui-widget-header{ border: ' + options.headerBorder + '; background: ' + options.headerBckColor + '; ';
	style += 'color: ' + options.headerLabelColor + '; font-weight: ' + options.headerLabelWeight + '; font-family: ' + options.headerLabelFontFamily + ';} ';

	style += '[widget="tf"] div.jtable-main-container table.jtable tbody > tr { ' + options.itemBckColor + '; ' + options.itemLineColor + '; ';
	style += 'font-size: ' + options.itemLabelFontSize + '; color: ' + options.itemLabelColor + '; font-weight: ' + options.itemLabelWeight + '; font-family: ' + options.itemLabelFontFamily + ';} ';

	// text on the left for all cells
	style += '[widget="tf"] div.jtable-main-container table.jtable tbody > tr > td { text-align: left }';

	// 3 px from right only for the last column
	style += '[widget="tf"] div.jtable-main-container table.jtable tbody > tr > td:nth-child(4) { padding-right: 3px}';

	// 10 px width onlyt for the first column
	style += '.jtable-selecting-column {width: 10px;}';

	if (options.accordionMode) {

		// hides the tf widget headers (the label is shown by the accordion)
		style += '[widget="tf"] div.jtable-main-container div.jtable-title { display: none;}';

		jQuery('#' + id).accordion();
	}

	GIAPI.UI_Utils.appendStyle(style);

	/**
	 * Enables and initializes the visualization control
	 * 
	 * @param {String} chartElementId id id of the HTML container (tipically <code>&lt;div&gt;</code> element) where the charts tab is inserted
	 * @param {Object} [options] the charts options. See <a href="http://www.chartjs.org/docs/#getting-started-global-chart-configuration" target=_blank>chart options</a> and specific 
	 * <a href="http://www.chartjs.org/docs/#bar-chart-chart-options" target=_blank>bar chart options</a> for more info
	 * @param {Object} [styles] an object with a style property for each chart. The style defines the aspect of the chart, as described 
	 * <a href="http://www.chartjs.org/docs/#bar-chart-data-structure" target=_blank>here</a>. This API defines some extra properties; see the following code snippet which sows the default values for each property.
	 * <pre><code>var styles = {};<br> 
	 * // style for the "format" chart
	 * styles.format = {<br>
	 * 		// --- extra properties for the canvas where the chart is drawn ---<br>
	 * 		canvasStyle: "background: white; margin: 5px; margin-top: 10px; box-shadow: 0 0 20px #999;",
	 * 		canvasWidth: 1000,
	 * 		canvasHeight: 580,<br>
	 * 		// --- extra properties for the checked bars ---<br>
	 * 		checkedFillColor: "#BBDAF7",
	 * 		checkedStrokeColor: "#378DE5",
	 *		checkedHighlightFill: "rgba(55, 141, 229, 0.59)",
	 * 		checkedHighlightStroke: "#377DE5",<br>
	 * 		// --- default bar chart properties ---<br>
	 * 		fillColor: "rgba(151,187,205,0.5)",
	 * 		strokeColor: "rgba(151,187,205,0.8)",
	 *		highlightFill: "rgba(151,187,205,0.75)",
	 * 		highlightStroke: "rgba(151,187,205,1)"
	 * };<br>
	 * // style for the "source" chart 
	 * styles.source =...<br>
	 * // style for the "keyword" chart 
	 * styles.keyword = ...<br>
	 * // style for the "protocol" chart
	 * styles.protocol = ...
	 * </pre></code>
	 *
	 * @method chart
	 */
	widget.chart = function(chartElementId, options, styles) {

		// current selected tab
		if (!GIAPI._chartTab) {
			GIAPI._chartTab = 0;
		}

		chartTabId = chartElementId;

		if (!options) {
			options = {};
		}
		// the charts size is fixed
		options.responsive = false;
		// *** extension **** // 
		options.barMaxWidth = 85;
		chartOptions = options;

		try {
			jQuery("#" + chartTabId).tabs("destroy");
		} catch (e) { }

		jQuery("#" + chartTabId).empty();
		jQuery("#" + chartTabId).append('<ul id="' + chartTabId + '-ul"></ul>');

		if (!styles) {

			styles = {};
			styles.format = {

				// canvas properties
				canvasStyle: "background: white; margin: 5px; margin-top: 10px; box-shadow: 0 0 20px #999;",
				canvasWidth: 1000,
				canvasHeight: 580,

				// checked chart properties
				checkedFillColor: "#BBDAF7",
				checkedStrokeColor: "#378DE5",
				checkedHighlightFill: "rgba(55, 141, 229, 0.59)",
				checkedHighlightStroke: "#377DE5",

				// chart properties			    			   
				fillColor: "rgba(151,187,205,0.5)",
				strokeColor: "rgba(151,187,205,0.8)",
				highlightFill: "rgba(151,187,205,0.75)",
				highlightStroke: "rgba(151,187,205,1)"
			};

			styles.source = GIAPI.clone(styles.format);
			styles.keyword = GIAPI.clone(styles.format);
			styles.protocol = GIAPI.clone(styles.format);
			styles.instrument = GIAPI.clone(styles.format);
			styles.platform = GIAPI.clone(styles.format);
			styles.originatorOrganisation = GIAPI.clone(styles.format);
			styles.attribute = GIAPI.clone(styles.format);
			styles.attributeURI = GIAPI.clone(styles.format);
			styles.sscScore = GIAPI.clone(styles.format);
		}
		chartStyles = styles;
	};

	function compareTFObjects(a, b) {
	if (nameA < nameB) {
    return -1;
  }
  if (nameA > nameB) {
    return 1;
  }

  // names must be equal
  return 0;
	}

	/**
	 * Updates the widget with the <a href="../classes/TermFrequencyObject.html" class="crosslink">term frequency object</a> of the current <a href="../classes/ResultSet.html" class="crosslink">result set</a>
	 * 
	 * @param {ResultSet} resultSet
	 * @method update
	 */
	widget.update = function(resultSet) {

		if (!resultSet.termFrequency) {

			jQuery("#" + chartTabId).empty();

			return;
		}

		jQuery('#' + id).empty();

		var tfObject = resultSet.termFrequency;

		var idList = [];
		for (var i = 0; i < tfObject.targets().length; i++) {
			idList.push(GIAPI.random());
		}

		for (var i = 0; i < tfObject.targets().length; i++) {

			var currentId = idList[i];
			var target = tfObject.targets()[i];

			var label = null;
			switch (target) {

				case 'format': label = 'Format'; break;
				case 'source': label = 'Source'; break;
				case 'keyword': label = 'Keywords'; break;
				case 'protocol': label = 'Protocol'; break;

				case 'instrumentTitle': label = 'Instrument'; break;
				case 'organisationName': label = 'Organization'; break;
				case 'platformTitle': label = 'Platform'; break;
				case 'attributeTitle': label = 'Attribute'; break;
				case 'observedPropertyURI': label = 'Attribute URI'; break;

				case 'attributeId': label = 'Attribute identifier'; break;
				case 'instrumentId': label = 'Instrument identifier'; break;
				case 'platformId': label = 'Platform identifier'; break;
				case 'origOrgId': label = 'Organization identifier'; break;
				case 'instrumentDescription': label = 'Instrument description'; break;
				case 'platformDescription': label = 'Platform description'; break;
				case 'origOrgDescription': label = 'Origin Organization description'; break;

				case 'sensorOpMode': label = 'Sensor Operation Mode'; break;
				case 'sarPolCh': label = 'Polarisation Channels'; break;
				case 'prodType': label = 'Product Type'; break;
				case 'sensorSwath': label = 'Sensor Swath'; break;

				case 's3Timeliness': label = 'S3 Timeliness'; break;
				case 's3InstrumentIdx': label = 'S3 Instrument Idx'; break;
				case 's3ProductLevel': label = 'S3 Product Level'; break;

			}

			if (options.accordionMode) {
				jQuery('#' + id).append('<h3>' + label + '</h3><div style="' + options.divCSS + '" id="' + currentId + '"></div>');
			} else {
				jQuery('#' + id).append('<div style="' + options.divCSS + '" id="' + currentId + '"></div>');
			}

			jtables[target] = currentId;

			jQuery('#' + currentId).attr('widget', 'tf');

			idToTargetMap[idList[i]] = target;

			jQuery('#' + currentId).jtable({

				title: target.toUpperCase(),

				selecting: true,
				multiselect: true,
				selectingCheckboxes: true,
				selectOnRowClick: true,

				fields: {

					term: {
						visibility: "hidden"
					},
					decodedTerm: {
						key: true,
						title: '',
						width: '80%',
					},
					freq: {
						title: '',
						width: '20%'
					}
				},

				selectionChanged: function(event, data) {

					var selRows = jQuery('#' + this.id).jtable('selectedRows');
					var id = this.id;

					if (selRows.length > 0) {

						if (selRows[selRows.length - 1].ignoreEvent) {
							delete (selRows[selRows.length - 1].ignoreEvent);
							return;
						}

						var selectedItems = [];

						selRows.each(function() {

							var record = jQuery(this).data('record');

							selectedItems.push(record);
						});

						tfObject.checkItems(idToTargetMap[this.id], selectedItems);

						if (beforeRefine) {
							beforeRefine.apply(this, []);
						}

						tfObject.refine();

						if (afterRefine) {
							afterRefine.apply(this, []);
						}
					} else {

						var target = idToTargetMap[this.id];
						tfObject.clearCheckedItems(target);

						if (beforeRefine) {
							beforeRefine.apply(this, []);
						}

						tfObject.refine();

						if (afterRefine) {
							afterRefine.apply(this, []);
						}
					}
				},

				rowInserted: function(event, data) {

					if (tfObject.isCheckedItem(idToTargetMap[event.target.id], data.record)) {

						data.row[0].ignoreEvent = true;

						jQuery('#' + currentId).jtable('selectRows', data.row);
					}
				}
			});

			jQuery('.jtable thead').remove();

			var checkedItems = tfObject.checkedItems(target);
			if (checkedItems) {
				for (var j = 0; j < checkedItems.length; j++) {

					jQuery('#' + currentId).jtable('addRecord', {

						record: checkedItems[j],
						clientOnly: true
					});
				};
			}

			var items = tfObject.items(target);
			for (var j = 0; j < items.length; j++) {

				if (!tfObject.isCheckedItem(target, items[j])) {

					if (items[j].term != null) {
						if (items[j].term.length > 32 && items[j].term.indexOf('%3A') != -1) {
							items[j].decodedTerm = items[j].decodedTerm.replace(/:/g, ' : ');
						}

						jQuery('#' + currentId).jtable('addRecord', {

							record: items[j],
							clientOnly: true
						});
					}
				}
			};
		};

		if (options.accordionMode) {

			var active = jQuery('#' + id).accordion('option', 'active');
			jQuery('#' + id).accordion('refresh');
			jQuery('#' + id).accordion('option', 'active', active);
		}

		if (chartTabId) {

			// customizes the tooltips in order to show the full label
			Chart.defaults.global.tooltipTemplate = "<%if (fullLabel){%><%=fullLabel%>: <%}%><%= value %>";
			try {
				jQuery("#" + chartTabId).tabs("destroy");
			} catch (e) { }

			jQuery("#" + chartTabId).empty();
			jQuery("#" + chartTabId).append('<ul id="' + chartTabId + '-ul"></ul>');
			// creates a graph for each target
			for (var i = 0; i < tfObject.targets().length; i++) {

				var target = tfObject.targets()[i];
				var chartId = target + '-chart';

				// initializes the chart
				initChart(tfObject, target);

				var ctx = document.getElementById(chartId).getContext("2d");
				var chart = new Chart(ctx).Bar(chartData[target], chartOptions);

				charts[chartId] = chart;

				// manages the click event on the bars in order to 
				// simulate a row selection on the jtable
				$("#" + chartId).click(function(e) {

					var chartId = e.toElement.id;
					var bar = charts[chartId].getBarsAtEvent(e)[0];

					var trgt = chartId.replace('-chart', '');
					var term = bar.fullLabel;

					// using jquery to simulate the click
					jQuery('[data-record-key="' + term + '"] td input').click();
				});
			}

			// initializes the graph tab
			jQuery("#" + chartTabId).tabs({
				activate: function(event, ui) {
					// store the current active tab 
					GIAPI._chartTab = jQuery("#" + chartTabId).tabs("option", "active");
				}
			});

			// activate the last active tab
			jQuery("#" + chartTabId).tabs("option", "active", GIAPI._chartTab);
		}
	};

	var initChart = function(tfObject, target) {

		var items = tfObject.items(target);
		var chartId = target + '-chart';

		jQuery('#' + chartTabId + '-ul').append('<li><a href="#' + chartId + '">' + target.toUpperCase() + '</a></li>');

		var canvasStyle = chartStyles[target].canvasStyle;
		var canvasWidth = chartStyles[target].canvasWidth || 1000; // 1000 the default canvas width
		var canvasHeight = chartStyles[target].canvasHeight || 580; // 580 the default canvas height

		var canvas = '<canvas width="' + canvasWidth + '" height="' + canvasHeight + '" id="' + chartId + '" style="' + canvasStyle + '"/>';
		jQuery('#' + chartTabId).append(canvas);

		chartData[target].datasets = [];
		chartData[target].datasets.push({});

		chartData[target].datasets[0].checkedFillColor = chartStyles[target].checkedFillColor;
		chartData[target].datasets[0].checkedStrokeColor = chartStyles[target].checkedStrokeColor;
		chartData[target].datasets[0].checkedHighlightFill = chartStyles[target].checkedHighlightFill;
		chartData[target].datasets[0].checkedHighlightStroke = chartStyles[target].checkedHighlightStroke;

		chartData[target].datasets[0].fillColor = chartStyles[target].fillColor;
		chartData[target].datasets[0].strokeColor = chartStyles[target].strokeColor;
		chartData[target].datasets[0].highlightFill = chartStyles[target].highlightFill;
		chartData[target].datasets[0].highlightStroke = chartStyles[target].highlightStroke;

		chartData[target].labels = [];
		chartData[target].fullLabels = [];
		chartData[target].checked = [];
		chartData[target].datasets[0].data = [];

		items.forEach(function(item) {

			var term = item.decodedTerm;
			if (term) {
				var shortTerm = term.length < 20 ? term : term.substring(0, 20) + '...';
				var freq = item.freq;

				chartData[target].labels.push(shortTerm);
				// add a full label
				chartData[target].fullLabels.push(term);
				// add the checked property
				chartData[target].checked.push(tfObject.isCheckedItem(target, item));

				chartData[target].datasets[0].data.push(freq);
			}
		});
	};

	return widget;
};
