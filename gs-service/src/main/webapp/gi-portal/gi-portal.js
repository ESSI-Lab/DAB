import { GIAPI } from '../giapi/core/GIAPI.js';

var view = '';
var token = '';

function initializeLogin(config) {
	if (!config.login) {
		return;
	}

	// Create and append login elements
	const loginContainer = document.createElement('div');
	loginContainer.className = 'login-container';
	loginContainer.innerHTML = `
		<button id="loginBtn" class="login-button">Login</button>
		<button id="logoutBtn" class="login-button" style="display: none;">Logout</button>
		<div id="loginModal" class="login-modal">
			<h3>Login to ${config.title || 'Portal'}</h3>
			<p class="login-info">After logging in, you will gain access to additional features, such as asynchronous bulk data download.</p>
			<input type="email" id="email" placeholder="Email" autocomplete="off">
			<input type="password" id="apiKey" placeholder="API Key" autocomplete="off">
			<button id="submitLogin">Login</button>
		</div>
		<div id="modalOverlay" class="modal-overlay"></div>
	`;

	document.body.insertBefore(loginContainer, document.body.firstChild);

	// Setup event listeners
	const loginBtn = document.getElementById('loginBtn');
	const logoutBtn = document.getElementById('logoutBtn');
	const loginModal = document.getElementById('loginModal');
	const modalOverlay = document.getElementById('modalOverlay');
	const submitLogin = document.getElementById('submitLogin');
	const emailInput = document.getElementById('email');
	const apiKeyInput = document.getElementById('apiKey');

	// Show modal
	loginBtn.addEventListener('click', function() {
		loginModal.style.display = 'block';
		modalOverlay.style.display = 'block';
		// Clear inputs when opening modal
		emailInput.value = '';
		apiKeyInput.value = '';
	});

	// Handle logout
	logoutBtn.addEventListener('click', function() {
		localStorage.removeItem('authToken');
		localStorage.removeItem('userEmail');
		loginBtn.style.display = 'inline-block';
		loginBtn.textContent = 'Login';
		loginBtn.disabled = false;
		logoutBtn.style.display = 'none';
		window.location.reload();
	});

	// Hide modal when clicking outside
	modalOverlay.addEventListener('click', function() {
		loginModal.style.display = 'none';
		modalOverlay.style.display = 'none';
		// Clear inputs when closing modal
		emailInput.value = '';
		apiKeyInput.value = '';
	});

	// Handle login submission
	submitLogin.addEventListener('click', function() {
		const email = emailInput.value;
		const apiKey = apiKeyInput.value;

		if (!email || !apiKey) {
			alert('Please enter both email and API key');
			return;
		}

		// Call the authentication endpoint
		fetch('../services/support/auth/login', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({
				email: email,
				apiKey: apiKey
			})
		})
		.then(response => response.json())
		.then(data => {
			if (data.success) {
				// Store the token and email
				localStorage.setItem('authToken', data.apiKey);
				localStorage.setItem('userEmail', data.email);
				
				// Update UI
				loginBtn.style.display = 'none';
				logoutBtn.style.display = 'inline-block';
				logoutBtn.textContent = `Logged in (${data.email})`;
				
				// Close modal
				loginModal.style.display = 'none';
				modalOverlay.style.display = 'none';

				// Refresh the portal with authentication
				window.location.reload();
			} else {
				alert('Login failed: ' + (data.message || 'Invalid credentials'));
			}
		})
		.catch(error => {
			console.error('Login error:', error);
			alert('Login failed. Please try again.');
		});
	});

	// Check for existing token and email
	const existingToken = localStorage.getItem('authToken');
	const existingEmail = localStorage.getItem('userEmail');
	if (existingToken && existingEmail) {
		loginBtn.style.display = 'none';
		logoutBtn.style.display = 'inline-block';
		logoutBtn.textContent = `Logged in (${existingEmail})`;
	}
}

export function initializePortal(config) {
	view = config.view;
	token = config.token;
	document.title = config.title;

	// Initialize login if enabled
	initializeLogin(config);

	var centerLat = config.centerLat;
	var centerLon = config.centerLon;
	var zoom = config.zoom;
	var minZoom = config.minZoom;

	$.extend(true, $.hik.jtable.prototype.options, {
		jqueryuiTheme: true
	});
	var getUrlParameter = function getUrlParameter(sParam) {
		var sPageURL = window.location.search.substring(1),
			sURLVariables = sPageURL.split('&'),
			sParameterName,
			i;

		for (i = 0; i < sURLVariables.length; i++) {
			sParameterName = sURLVariables[i].split('=');

			if (sParameterName[0] === sParam) {
				return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
			}
		}
	};

	GIAPI.logger.enabled = true;

	GIAPI.search = {};

	// GIAPI.search.dab = GIAPI.DAB('http://localhost:9090/gs-service', view, 'services/essi','csw');
	// GIAPI.search.dab = GIAPI.DAB('https://gs-service-preproduction.geodab.eu/gs-service', view, 'services/essi','csw');
	GIAPI.search.dab = GIAPI.DAB('../', view, 'services/essi', 'csw');

	jQuery(function() {

		var seadatanetLogos = '<a style="display:inline-block" target=_blank href="https://www.seadatanet.org/"><img style="width: 56px;" src="http://api.geodab.eu/docs/assets/img/sdn_logo.png"></img></a><a style="display:inline-block" target=_blank href="http://www.iia.cnr.it/"><img style="vertical-align: super" src="http://api.geodab.eu/docs/assets/img/iia.png"></img></a><a style="display:inline-block" targ et=_blank href="http://www.uos-firenze.iia.cnr.it/"><img style="vertical-align: super" src="http://api.geodab.eu/docs/assets/img/essilab.png"></img></a>';

		var standardLogos = '<a style="display:inline-block" target=_blank href="http://api.geodab.eu/"><img style="margin-top:-3px;" src="http://api.geodab.eu/docs/assets/img/api-logo-small-2.png"></img></a><a style="display:inline-block" target=_blank href="http://www.eurogeoss.eu/"><img src="http://api.geodab.eu/docs/assets/img/eurogeoss-small.png"></img></a><a style="display:inline-block" target=_blank href="http://www.iia.cnr.it/"><img style="vertical-align: super" src="http://api.geodab.eu/docs/assets/img/iia.png"></img></a><a style="display:inline-block" targ et=_blank href="http://www.uos-firenze.iia.cnr.it/"><img style="vertical-align: super" src="http://api.geodab.eu/docs/assets/img/essilab.png"></img></a>';

		// init the tabs	        	
		jQuery('#tabs-div').tabs({
			activate: function(event, ui) {

				// hides/shows the paginator widget
				if (ui.newPanel.selector === '#results-tab') {

					jQuery('#paginator-widget').css('display', 'block');

				} else {
					jQuery('#paginator-widget').css('display', 'none');
				}

				// refreshes the filters accordion 
				if (ui.newPanel.selector === '#filters-tab') {
					jQuery('#filters-tab').accordion('refresh');
				}
			}
		});

		//------------------------------------------------------------------
		// header settings
		//
		jQuery('#headerDiv').css('padding', '10px');
		jQuery('#headerDiv').css('padding-top', '5px');
		jQuery('#headerDiv').css('height', '30px');

		//------------------------------------------------------------------
		// logo div settings
		//
		jQuery('#logoDiv').css('margin-top', '-2px');
		jQuery('#logoDiv').css('margin-left', '50px');

		var baseWidth = 600;

		//------------------------------------------------------------------
		// search button
		//
		jQuery('#search-button').css('margin-left', '-3px');
		jQuery('#search-button').css('margin-top', '5px');

		//------------------------------------------------------------------
		// adv search button
		//
		jQuery('#adv-search-button').css('margin-top', '6px');

		//------------------------------------------------------------------
		// tabs
		//
		jQuery('#tabs-ul').css('width', (baseWidth + 22) + 'px');
		jQuery('#tabs-ul').css('height', '40px');
		jQuery('#tabs-ul').css('margin-left', '3px');

		jQuery('#tabs-div').css('top', '60px');
		jQuery('#tabs-div').css('left', '15px');
		jQuery('#tabs-div').css('padding', '0px');

		//------------------------------------------------------------------
		// results tab
		//
		jQuery('#results-tab').css('width', (baseWidth + 31) + 'px');
		jQuery('#results-tab').css('margin-top', '52px');

		jQuery('li[aria-controls="results-tab"').css('margin-left', '190px');

		var css = 'width: ' + (baseWidth + 22) + 'px';
		GIAPI.UI_Utils.appendStyle('.resultset-layout-ul {' + css + '}');

		jQuery('#paginator-widget').css('width', (baseWidth + 30) + 'px');
		jQuery('#paginator-widget').css('height', '55px');
		jQuery('#paginator-widget').css('top', '108px');
		jQuery('#paginator-widget').css('left', '18px');

		css = 'width: 290px;';
		css += 'margin-left:640px;';
		css += 'margin-top:-99px;';
		GIAPI.UI_Utils.appendStyle('.series-browser-widget-table {' + css + '}');

		//------------------------------------------------------------------
		// sources tab
		//
		jQuery('#sources-tab').css('width', (baseWidth + 27) + 'px');
		jQuery('#sources-tab').css('margin-top', '3px');
		jQuery('#sources-tab').css('margin-left', '2px');

		jQuery('.sources-widget').css('padding-right', '10px');
		jQuery('.sources-widget').css('padding-left', '10px');
		jQuery('.sources-widget').css('padding-top', '0px');

		//------------------------------------------------------------------
		// filters tab     
		//
		jQuery('#filters-tab').css('width', (baseWidth + 29) + 'px');
		jQuery('#filters-tab').css('height', '100%');
		jQuery('#filters-tab').css('margin-top', '3px');

		//------------------------------------------------------------------
		// browse tab     
		//
		jQuery('#browse-tab').css('width', (baseWidth + 23) + 'px');
		jQuery('#browse-tab').css('margin-left', '3px');
		jQuery('#browse-tab').css('margin-top', '3px');
		jQuery('#browse-tab').css('padding-left', '5px');
		jQuery('#browse-tab').css('padding-top', '5px');

		//------------------------------------------------------------------
		// nodes
		//
		var css = 'width: ' + (baseWidth) + 'px';
		GIAPI.UI_Utils.appendStyle('.classification-table {' + css + '}');

		//------------------------------------
		// ResultsMapWidget
		//

		var showLayersControl = true;

		if (config.layersSelectorButtonVisibility !== undefined && !config.layersSelectorButtonVisibility) {
			showLayersControl = false;
		}

		var startActive = true;

		if (config.layersSelectorVisibility !== undefined && !config.layersSelectorVisibility) {
			startActive = false;
		}

		GIAPI.search.resultsMapWidget = GIAPI.ResultsMapWidget('resMapWidget', centerLat, centerLon, {


			'width': '100%',
			'height': jQuery(window).height() - 70,
			'markerTitle': function(node) {

				return node.report().title;
			},

			'mapType': 'ol',

			'showSelectionControl': true,

			//                	'onMarkerClick': function(node) {
			//             		},

			'dabNode': GIAPI.search.dab,


			'wmsEndpoint': config.wmsEndpoint,

			'clusterWMS': true,
			'clusterWMSToken': token,
			'clusterWMSView': view,
			'clusterWMSLayerName': view,
			'clusterWMSLayerTitle': view,

			'stationInfoId': 'stationInfo',
			'stationNameAddId': 'platformNameConstraint',
			'advancedConstraintDivId': 'advConstDiv',

			'onMarkerMouseOver': function(node) {

				window.open('#section-div-' + node.uiId, '_self');
				jQuery('#section-' + node.uiId + ' div').first().addClass('common-ui-node-report-div-hover');
			},

			'onMarkerMouseOut': function(node) {

				jQuery('#section-' + node.uiId + ' div').first().removeClass('common-ui-node-report-div-hover');
			},

			//'mapTypeControlOptions': {
			//        style : google.maps.MapTypeControlStyle.HORIZONTAL_BAR,	        	     
			//      position : google.maps.ControlPosition.TOP_RIGHT
			//},

			'zoom': zoom,
			'minZoom': minZoom,
			'addLayers': false,
			'startActive': startActive,
			'showLayersControl': showLayersControl,
			'layersControlWidth': 180,
			'layersControlHeight': 200,
			'layersControlOpacity': 0.9,
			'zoomSlider': config.zoomSlider,
			'defaultLayer': config.defaultLayer
		});



		//------------------------------------
		// search button
		//
		var searchButton = GIAPI.FontAwesomeButton({
			'width': baseWidth - 100,
			'label': 'SEARCH',
			'icon': 'fa-search',
			'handler': function() {
				if (GIAPI.search.sourcesWidget.sourcesCount() === 0) {

					GIAPI.UI_Utils.dialog('open', {
						title: 'No sources selected',
						message: 'Please select at least one data source before starting the search'
					});

				} else {
					GIAPI.search.discover();
				}
			}
		});

		searchButton.css('div', 'padding', '6.5px');
		searchButton.css('div', 'text-align', 'center');
		searchButton.css('label', 'font-size', '1.2em');

		jQuery('#search-button').append(searchButton.div());

		//------------------------------------------------------------------
		// hide results button
		//           	
		var hideResultsButton = GIAPI.ButtonsFactory.onOffSwitchButton('Show results', 'Hide results', {
			'id': 'hideResultsButton',
			'checked': false,
			'size': 'large',
			'offBckColor': 'white',
			'onBckColor': 'white',
			'offColor': 'black',
			'onColor': 'black',
			'switchColor': '#2c3e50',
			'switchBorderColor': '#2c3e50',
			'borderColor': 'rgba(44, 62, 80, 0.07)'
		});

		jQuery('#hide-results-button').append(hideResultsButton);

		jQuery(document).on('click', '#hideResultsButton', function() {

			if (jQuery('#hideResultsButton').is(":checked")) {

				jQuery('#paginator-widget').css('display', 'none');
				jQuery('#tabs-div').css('display', 'none');

			} else {

				jQuery('#paginator-widget').css('display', 'inline-block');
				jQuery('#tabs-div').css('display', 'block');
			}
		});






		//------------------------------------
		// ConstraintsWidget
		//
		GIAPI.search.constWidget = GIAPI.ConstraintsWidget(GIAPI.search.dab, {
			'ontology': config.ontology,
			'keyDownAction': (function() { GIAPI.search.discover(); }),
			'fieldsWidth': 205
		});

		GIAPI.search.constWidget.whatConstraint('add', {
			showOptDialog: true,
			showResultSetExtensionOpt: false,
			optDialogPosition: 'bottom',
			showHelpIcon: false,
			resizable: true
		});

		GIAPI.search.constWidget.append('what-div');
		jQuery('#' + GIAPI.search.constWidget.getId('what')).css('padding', '6px');

		GIAPI.search.constWidget.whenConstraint('add', 'from', { showHelpIcon: false });
		GIAPI.search.constWidget.append('from-div');

		jQuery('#' + GIAPI.search.constWidget.getId('from')).css('padding', '6px');
		jQuery('#' + GIAPI.search.constWidget.getId('from')).css('width', '80px');
		jQuery('#' + GIAPI.search.constWidget.getId('from')).parent('div').parent('td').css('width', '80px');

		GIAPI.search.constWidget.whenConstraint('add', 'to', { showHelpIcon: false });
		GIAPI.search.constWidget.append('to-div');

		jQuery('#' + GIAPI.search.constWidget.getId('to')).css('padding', '6px');
		jQuery('#' + GIAPI.search.constWidget.getId('to')).css('width', '80px');
		jQuery('#' + GIAPI.search.constWidget.getId('to')).parent('div').parent('td').css('width', '80px');

		GIAPI.search.constWidget.append('constraints-div');

		//
		// mapControlDiv
		//

		jQuery('#mapControlDiv').css('position', 'relative');
		jQuery('#mapControlDiv').css('top', '5px');
		jQuery('#mapControlDiv').css('z-index', '1');

		jQuery('#where-div').append(document.getElementById("mapControlDiv"));


		jQuery('#disclaimer-div').append(config.disclaimer);

		if (config.disclaimer && config.disclaimer.trim()) {
			var agreed = false;
			$(document).ready(function() {
				$("#disclaimer-div").dialog({
					resizable: false,
					height: "auto",
					width: 800,
					title: config.disclaimerTitle,
					modal: true,
					buttons: [
						{
							text: "Accept",
							click: function() {
								agreed = true;
								$(this).dialog("close");
							}
						}
					],
					beforeClose: function() {
						return agreed;
					}
				});
			});
		}

		//
		// toggle button for the map input control (mapControlDiv)
		//

		var layerSelectorDisplay = 'none';

		var hideMapInputControlButton = GIAPI.ButtonsFactory.onOffSwitchButton('Show', 'Hide', {
			'id': 'hideMapInputControl',
			'checked': false,
			'size': 'medium',
			'offBckColor': 'white',
			'onBckColor': 'white',
			'offColor': 'black',
			'onColor': 'black',
			'switchColor': '#2c3e50',
			'switchBorderColor': '#2c3e50',
			'borderColor': 'rgba(44, 62, 80, 0.07)'
		});



		jQuery(document).on('click', '#hideMapInputControl', function() {

			if (jQuery('#hideMapInputControl').is(":checked")) {

				jQuery('#mapControlDiv > div> table').css('display', 'none');

				layerSelectorDisplay = jQuery('#mapControlDiv > div> div[id^="layerSelectorDiv"]').css('display');

				jQuery('#mapControlDiv > div> div[id^="layerSelectorDiv"]').css('display', 'none');

			} else {

				jQuery('#mapControlDiv >  div> table').css('display', 'block');

				jQuery('#mapControlDiv > div> div[id^="layerSelectorDiv"]').css('display', layerSelectorDisplay);

			}
		});





		jQuery('#where-div').append(hideMapInputControlButton);

		jQuery('#onoffswitch-div-hideMapInputControl').css('z-index', '1');
		jQuery('#onoffswitch-div-hideMapInputControl').css('margin-top', '-22px');
		jQuery('#onoffswitch-div-hideMapInputControl').css('margin-left', '100px');

		//
		// advanced search div        
		//


		var advancedConstraints = [];
		if (config.instrumentSearch !== undefined && config.instrumentSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'instrumentTitle'));
		}
		if (config.attributeSearch !== undefined && config.attributeSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'attributeTitle',{ id: 'attributeNameConstraint'}));
		}
		if (config.platformSearch !== undefined && config.platformSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'platformTitle',
				{ id: 'platformNameConstraint', helpIconImage: 'fa-wifi' }));
		}
		if (config.validatedSearch !== undefined && config.validatedSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.booleanConstraint('get', 'isValidated'));
		}

		if (config.riverSearch !== undefined && config.riverSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'riverName', { helpIconImage: 'fa-tint' }));
		}

		var semanticValue = 0;
		if (config.semanticSearchValue !== undefined) {
			semanticValue = config.semanticSearchValue;
		}

		if (config.semanticSearch !== undefined && config.semanticSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.booleanConstraint('get', 'semantics', { ontology: config.ontology, value: semanticValue, helpIconImage: 'fa-comments' }));
		}


		GIAPI.search.constWidget.advancedSearch(
			'advConstDiv',
			'adv-search-div',
			advancedConstraints
		);

		//------------------------------------
		// PaginatorWidget
		//
		GIAPI.search.paginatorWidget = GIAPI.PaginatorWidget('paginator-widget',

			GIAPI.search.onDiscoverResponse,
			{
				'onPagination': function(action) {

					GIAPI.UI_Utils.discoverDialog('open');
				},
				'border': 'none'
			}
		);

		//------------------------------------
		// SourcesWidget
		//
		GIAPI.search.sourcesWidget = GIAPI.SourcesWidget('sources-tab', GIAPI.search.dab, {
			'width': 'auto',
			'height': 'auto',
			'viewId': view,
			'include': function(source) {
				// includes only harvested sources to speedup the initialization
				return source.contentType() === 'harvested';
			},

			'onSourcesReady': function(sources) {
				// starts the init discover
				GIAPI.search.discover();
			}
		});

		GIAPI.UI_Utils.appendStyle('#sources-tab{ max-height: ' + (jQuery(window).height() - 150) + 'px}');

		//------------------------------------
		// TermFrequencyWidget
		//
		GIAPI.search.tfWidget = GIAPI.TermFrequencyWidget('filters-tab',
			(function() { GIAPI.UI_Utils.discoverDialog('open') }), null,
			{
				'itemLabelFontSize': '80%',
				'divCSS': 'max-height:550px; overflow:auto',
				'accordionMode': true
			}
		);

		jQuery('#filters-tab').css('height', jQuery(window).height() - 150);


		//------------------------------------
		// ResultSetLayout
		//
		var Common_UINode_No_Aside = function(options) {

			var uiNode = GIAPI.Common_UINode(options);

			options.asideDom = function(node, options, asideId) {
			};

			options.onAsideReady = function(aside, node) {
			};

			return uiNode;
		};


		// creates the layout
		GIAPI.search.resultSetLayout = GIAPI.ResultSetLayout('results-tab', {

			// registers the ui nodes
			'uiNodes': [Common_UINode_No_Aside],

			// set the widgets to update
			'mapWidget': GIAPI.search.resultsMapWidget,
			'pagWidget': GIAPI.search.paginatorWidget,
			'tfWidget': GIAPI.search.tfWidget,


			'browseCollection': false,
			'browseCollectionMapType': 'ol',
			'onDiscoverResponse': GIAPI.search.onDiscoverResponse,
			// ---------------------------------------------------

			'dabNode': GIAPI.search.dab,
			//'height': jQuery(window).height()-100,
			'maxHeight': jQuery(window).height() - 210,
		});

		//------------------------------------
		// Starts discover
		//
		//	            GIAPI.search.discover();  
	});

	GIAPI.search.discover = function(init) {

		var constraints = GIAPI.search.constWidget.constraints();
		constraints.where = GIAPI.search.resultsMapWidget.where();

		var sources = GIAPI.search.dab.findSources(null);
		var tokenParam = getUrlParameter('token');

		constraints.sources = sources;

		if (typeof tokenParam !== 'undefined') {

			constraints.kvp.push(

				{ 'key': 'token', 'value': tokenParam }
			);
		}

		var options = GIAPI.search.constWidget.options();

		options.spatialRelation = GIAPI.search.resultsMapWidget.spatialRelation();

		constraints.spatialOp = options.spatialRelation;

		constraints.ontology = config.ontology;

		GIAPI.search.resultsMapWidget.updateWMSClusterLayers(constraints);

		// set the termFrequency option
		options.termFrequency = 'source,keyword,format,protocol';



		if (config.filters !== undefined) {
			options.termFrequency = config.filters;
		}

		try {
			GIAPI.search.dab.discover(GIAPI.search.onDiscoverResponse, constraints, options);
			GIAPI.UI_Utils.discoverDialog('open');

		} catch (err) {
			GIAPI.UI_Utils.dialog('open', { title: 'Error', message: err });
		}
	};

	GIAPI.search.onDiscoverResponse = function(response) {

		if (response.error) {

			GIAPI.UI_Utils.discoverDialog('close');
			GIAPI.UI_Utils.dialog('open', { title: 'Error', message: response.error });
			return;
		}

		var resultSet = response[0];

		// updates the result set layout
		GIAPI.search.resultSetLayout.update(response);

		if (resultSet.extension) {
			jQuery('.resultset-layout-table-div').css('max-height', jQuery(window).height() - 280 + 'px');
		} else {
			jQuery('.resultset-layout-table-div').css('max-height', jQuery(window).height() - 210 + 'px');
		}

		if (!response[0].termFrequency) {
			jQuery('#filters-tab').empty();
		}

		if (GIAPI.UI_Utils.discoverDialog('isOpen')) {
			GIAPI.UI_Utils.discoverDialog('close');
		}
	};

	if (config.resultsVisibility !== undefined && !config.resultsVisibility) {
		$('#hideResultsButton').click();
	}

	if (config.bboxSelectorVisibility !== undefined && !config.bboxSelectorVisibility) {
		$('#hideMapInputControl').click();
	}




}



