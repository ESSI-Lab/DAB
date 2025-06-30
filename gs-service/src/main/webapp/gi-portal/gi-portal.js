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
	let logoutBtn = document.getElementById('logoutBtn');
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

		// Create user menu
		const userMenu = document.createElement('div');
		userMenu.id = 'userMenu';
		userMenu.className = 'user-menu';
		userMenu.style.display = 'none';
		userMenu.innerHTML = `
			<button id="statusBtn" class="menu-button">Status of bulk downloads</button>
			<button id="logoutMenuBtn" class="menu-button">Logout</button>
		`;
		document.body.appendChild(userMenu);

		// Remove the old logout event listener and add menu toggle
		const oldLogout = logoutBtn.cloneNode(true);
		logoutBtn.parentNode.replaceChild(oldLogout, logoutBtn);
		logoutBtn = oldLogout;

		// Show/hide menu on logged-in user button click
		logoutBtn.addEventListener('click', function(e) {
			e.stopPropagation();
			const rect = logoutBtn.getBoundingClientRect();
			userMenu.style.top = (rect.bottom + 5) + 'px';
			userMenu.style.right = (window.innerWidth - rect.right) + 'px';
			userMenu.style.display = userMenu.style.display === 'none' ? 'block' : 'none';
		});

		// Hide menu when clicking outside
		document.addEventListener('click', function(e) {
			if (!userMenu.contains(e.target) && e.target !== logoutBtn) {
				userMenu.style.display = 'none';
			}
		});

		// Status button click handler
		document.getElementById('statusBtn').addEventListener('click', function() {
			userMenu.style.display = 'none';  // Hide menu when status is clicked
			
			// Create dialog content
			const dialogContent = $('<div>');
			
			// Add informative text
			const infoText = $('<div>')
				.css({
					'margin-bottom': '20px',
					'padding': '10px',
					'background-color': '#f8f9fa',
					'border-left': '4px solid #2c3e50',
					'border-radius': '4px'
				})
				.append(
					$('<p>')
						.css({
							'margin': '0',
							'color': '#2c3e50'
						})
						.html('<strong>Note:</strong> This panel shows your initiated bulk downloads. Downloads will be automatically removed two days after completion.')
				);

			dialogContent.prepend(infoText);

			// Create refresh button
			const refreshButton = $('<button>')
				.addClass('refresh-button')
				.html('<i class="fa fa-refresh"></i> Refresh')
				.css({
					'margin-bottom': '10px',
					'margin-top': '15px',
					'float': 'right'
				});

			dialogContent.append(refreshButton);
			dialogContent.append($('<div>').attr('id', 'status-content'));

			// Show dialog first
			const dialog = dialogContent.dialog({
				title: 'Bulk Downloads Status',
				modal: true,
				width: 1310,
				position: { my: "center", at: "center top+150", of: window },
				classes: {
					"ui-dialog": "bulk-download-dialog"
				},
				close: function() {
					// Cleanup when dialog is closed
					$(this).dialog('destroy').remove();
				}
			});

			// Add CSS to ensure table fits in dialog
			dialogContent.css({
				'overflow-x': 'hidden',
				'padding': '0 15px'
			});

			// Function to fetch and update status
			const fetchAndUpdateStatus = () => {
				const statusContent = $('#status-content');
				statusContent.html('<p>Loading status of bulk downloads...</p>');
				
				// Disable refresh button while loading
				refreshButton.prop('disabled', true);
				refreshButton.find('i').addClass('fa-spin');

				// Fetch status from API
				const authToken = localStorage.getItem('authToken');
				fetch(`../services/essi/token/${authToken}/view/${config.view}/om-api/downloads`, {
					headers: {
						'Accept': 'application/json'
					}
				})
				.then(response => response.json())
				.then(data => {
					statusContent.empty();
					if (data.results && data.results.length > 0) {
						// Sort results by timestamp in descending order
						const sortedResults = data.results.sort((a, b) => {
							const dateA = new Date(a.timestamp || 0);
							const dateB = new Date(b.timestamp || 0);
							return dateB - dateA;
						});

						const table = $('<table>').addClass('status-table').css({
							'width': '100%',
							'table-layout': 'fixed',
							'border-collapse': 'collapse'
						});
						
						const headerRow = $('<tr>')
							.append($('<th>').text('Timestamp').css('width', '160px'))
							.append($('<th>').text('Task ID').css('width', '250px'))
							.append($('<th>').text('Name').css('width', '200px'))
							.append($('<th>').text('Status').css('width', '200px'))
							.append($('<th>').text('Size (MB)').css('width', '100px'))
							.append($('<th>').text('Download').css('width', '100px'))
							.append($('<th>').text('Actions').css('width', '100px'));
						table.append(headerRow);

						sortedResults.forEach((item, index) => {
							const row = $('<tr>');
							
							// Timestamp column
							let formattedDate = '';
							if (item.timestamp) {
								try {
									const date = new Date(item.timestamp);
									const year = date.getFullYear();
									const month = String(date.getMonth() + 1).padStart(2, '0');
									const day = String(date.getDate()).padStart(2, '0');
									const hours = String(date.getHours()).padStart(2, '0');
									const minutes = String(date.getMinutes()).padStart(2, '0');
									const seconds = String(date.getSeconds()).padStart(2, '0');
									formattedDate = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
								} catch (e) {
									console.error('Error formatting date:', e);
									formattedDate = item.timestamp;
								}
							}
							row.append($('<td>')
								.text(formattedDate)
								.css({
									'width': '160px',
									'white-space': 'nowrap',
									'overflow': 'hidden',
									'text-overflow': 'ellipsis'
								})
							);
							
							// Task ID column
							row.append($('<td>')
								.css({
									'width': '250px',
									'display': 'flex',
									'align-items': 'center',
									'gap': '8px'
								})
								.append(
									$('<span>')
										.text(item.id)
										.attr('title', item.id)
										.css({
											'overflow': 'hidden',
											'text-overflow': 'ellipsis'
										})
								)
								.append(
									$('<button>')
										.addClass('copy-button')
										.html('<i class="fa fa-copy"></i>')
										.attr('title', 'Copy Task ID')
										.css({
											'padding': '2px 6px',
											'min-width': 'unset',
											'flex-shrink': '0'
										})
										.on('click', function() {
											const tempInput = $('<input>');
											$('body').append(tempInput);
											tempInput.val(item.id).select();
											document.execCommand('copy');
											tempInput.remove();
											
											const button = $(this);
											const originalTitle = button.attr('title');
											button.attr('title', 'Copied!');
											setTimeout(() => {
												button.attr('title', originalTitle);
											}, 2000);
										})
								)
							);
							
							// Name column
							row.append($('<td>')
								.text(item.downloadName || '-')
								.attr('title', item.downloadName)
								.css({
									'width': '200px',
									'white-space': 'nowrap',
									'overflow': 'hidden',
									'text-overflow': 'ellipsis'
								})
							);
							
							// Status column
							row.append($('<td>')
								.text(item.status)
								.css({
									'width': '200px'
								})
							);

							// Size column
							row.append($('<td>')
								.text(item.sizeInMB ? item.sizeInMB.toLocaleString(undefined, {
									minimumFractionDigits: 2,
									maximumFractionDigits: 2
								}) : '-')
								.css({
									'width': '100px',
									'white-space': 'nowrap',
									'overflow': 'hidden',
									'text-overflow': 'ellipsis'
								})
							);
							
							// Download column
							const downloadCell = $('<td>').css({
								'width': '100px',
								'white-space': 'nowrap',
								'overflow': 'hidden',
								'text-overflow': 'ellipsis'
							});
							if (item.status === 'Completed' && item.locator) {
								downloadCell.append(
									$('<a>')
										.attr('href', item.locator)
										.attr('target', '_blank')
										.text('Download')
								);
							} else {
								downloadCell.text('-');
							}
							row.append(downloadCell);

							// Actions column
							const actionsCell = $('<td>').css({
								'width': '100px',
								'white-space': 'nowrap'
							});

							// Show cancel button for active downloads
							if (!['Completed', 'Failed', 'Canceled', 'Removed'].includes(item.status)) {
								const cancelButton = $('<button>')
									.addClass('cancel-button')
									.html('<i class="fa fa-times"></i>')
									.attr('title', 'Cancel download')
									.css({
										'padding': '2px 6px',
										'min-width': 'unset',
										'background-color': '#dc3545',
										'color': 'white',
										'border': 'none',
										'border-radius': '4px',
										'cursor': 'pointer'
									})
									.on('click', function() {
										if (confirm('Are you sure you want to cancel this download?')) {
											const authToken = localStorage.getItem('authToken');
											fetch(`../services/essi/token/${authToken}/view/${config.view}/om-api/downloads?id=${item.id}`, {
												method: 'DELETE',
												headers: {
													'Accept': 'application/json'
												}
											})
											.then(response => {
												if (!response.ok) {
													throw new Error('Failed to cancel download');
												}
												// Refresh the status panel after successful cancellation
												fetchAndUpdateStatus();
											})
											.catch(error => {
												console.error('Error canceling download:', error);
												alert('Failed to cancel download. Please try again.');
											});
										}
									});
								actionsCell.append(cancelButton);
							}
							// Show remove button for completed downloads
							else if (item.status === 'Completed') {
								const removeButton = $('<button>')
									.addClass('remove-button')
									.html('<i class="fa fa-trash"></i>')
									.attr('title', 'Remove')
									.css({
										'padding': '2px 6px',
										'min-width': 'unset',
										'background-color': '#6c757d',
										'color': 'white',
										'border': 'none',
										'border-radius': '4px',
										'cursor': 'pointer'
									})
									.on('click', function() {
										if (confirm('Are you sure you want to remove this completed download?')) {
											const authToken = localStorage.getItem('authToken');
											fetch(`../services/essi/token/${authToken}/view/${config.view}/om-api/downloads?id=${item.id}`, {
												method: 'DELETE',
												headers: {
													'Accept': 'application/json'
												}
											})
											.then(response => {
												if (!response.ok) {
													throw new Error('Failed to remove download');
												}
												// Refresh the status panel after successful removal
												fetchAndUpdateStatus();
											})
											.catch(error => {
												console.error('Error removing download:', error);
												alert('Failed to remove download. Please try again.');
											});
										}
									});
								actionsCell.append(removeButton);
							}
							
							row.append(actionsCell);
							
							table.append(row);
						});
						
						statusContent.append(table);
					} else {
						statusContent.append($('<p>').text('No bulk downloads found.'));
					}
				})
				.catch(error => {
					console.error('Error fetching status:', error);
					statusContent.empty().append(
						$('<p>').text('Error loading bulk download status. Please try again later.')
					);
				})
				.finally(() => {
					// Re-enable refresh button and stop spinning
					refreshButton.prop('disabled', false);
					refreshButton.find('i').removeClass('fa-spin');
				});
			};

			// Add click handler to refresh button
			refreshButton.on('click', fetchAndUpdateStatus);

			// Initial fetch after dialog is shown
			setTimeout(fetchAndUpdateStatus, 100);
		});

		// Logout menu button click handler
		document.getElementById('logoutMenuBtn').addEventListener('click', function() {
			localStorage.removeItem('authToken');
			localStorage.removeItem('userEmail');
			loginBtn.style.display = 'inline-block';
			loginBtn.textContent = 'Login';
			loginBtn.disabled = false;
			logoutBtn.style.display = 'none';
			userMenu.style.display = 'none';
			window.location.reload();
		});
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


			'clusterWMS': (config.wmsEndpoint!==undefined),
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
			'fieldsWidth': 205,
			'enableBrowseGEMET': config.enableBrowseGEMET
		});
		if (config.generalTermSearch == undefined || config.generalTermSearch) {
			GIAPI.search.constWidget.whatConstraint('add', {
				showOptDialog: true,
				showResultSetExtensionOpt: false,
				optDialogPosition: 'bottom',
				showHelpIcon: false,
				resizable: true,
				enableBrowseGEMET: config.enableBrowseGEMET
			});

			GIAPI.search.constWidget.append('what-div');
			jQuery('#' + GIAPI.search.constWidget.getId('what')).css('padding', '6px');
		}

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
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'attributeTitle',
				{ id: 'attributeNameConstraint', helpIconImage: 'fa-flask' }));
		}
		if (config.platformSearch !== undefined && config.platformSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'platformTitle',
				{ id: 'platformNameConstraint', helpIconImage: 'fa-circle' }));
		}
		if (config.validatedSearch !== undefined && config.validatedSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.booleanConstraint('get', 'isValidated'));
		}

		if (config.riverSearch !== undefined && config.riverSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'riverName', { helpIconImage: 'fa-tint' }));
		}

		if (config.timeInterpolation !== undefined && config.timeInterpolation) {
			const timeInterpolationId = GIAPI.search.constWidget.getId('timeInterpolation');
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'timeInterpolation', {
				helpIconImage: 'fa-line-chart',
				values: [
					{ label: 'Select interpolation type', value: '' },
					{ label: 'Continuous', value: 'CONTINUOUS' },
					{ label: 'Average', value: 'AVERAGE' },
					{ label: 'Minimum', value: 'MIN' },
					{ label: 'Maximum', value: 'MAX' },
					{ label: 'Total', value: 'TOTAL' },
					{ label: 'Discontinuous', value: 'DISCONTINUOUS' },
					{ label: 'Incremental', value: 'INCREMENTAL' },
					{ label: 'Categorical', value: 'CATEGORICAL' }
				],
				readOnlyValues: true
			}));

			// After constraints are initialized, try to fetch and update values
			const authToken = localStorage.getItem('authToken') || 'my-token';
			fetch(`../services/essi/token/${authToken}/view/${config.view}/om-api/properties?property=timeInterpolation&limit=50`)
				.then(response => response.json())
				.then(data => {
					if (data.timeInterpolation && data.timeInterpolation.length > 0) {
						// Find the select element using the correct ID
						const selectElement = document.getElementById(timeInterpolationId);
						if (selectElement) {
							const options = [
								{ label: 'Select interpolation type', value: '' },
								...data.timeInterpolation.map(type => ({
									label: `${type.value} (${type.observationCount} observations)`,
									value: type.value
								}))
							];
							
							// Update the select options
							selectElement.innerHTML = options.map(option => 
								`<option value="${option.value}">${option.label}</option>`
							).join('');
						}
					}
				})
				.catch(error => {
					console.error('Error fetching interpolation types:', error);
					// Keep default values if API fails
				});
		}

		if (config.intendedObservationSpacing !== undefined && config.intendedObservationSpacing) {
			const spacingId = GIAPI.search.constWidget.getId('intendedObservationSpacing');
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'intendedObservationSpacing', {
				helpIconImage: 'fa-arrows-h',
				values: [
					{ label: 'Select observation spacing', value: '' }
				],
				readOnlyValues: true
			}));

			// After constraints are initialized, try to fetch and update values
			const authToken = localStorage.getItem('authToken') || 'my-token';
			fetch(`../services/essi/token/${authToken}/view/${config.view}/om-api/properties?property=intendedObservationSpacing&limit=50`)
				.then(response => response.json())
				.then(data => {
					if (data.intendedObservationSpacing && data.intendedObservationSpacing.length > 0) {
						// Find the select element using the correct ID
						const selectElement = document.getElementById(spacingId);
						if (selectElement) {
							const options = [
								{ label: 'Select observation spacing', value: '' },
								...data.intendedObservationSpacing.map(type => ({
									label: `${type.value} (${type.observationCount} observations)`,
									value: type.value
								}))
							];
							
							// Update the select options
							selectElement.innerHTML = options.map(option => 
								`<option value="${option.value}">${option.label}</option>`
							).join('');
						}
					}
				})
				.catch(error => {
					console.error('Error fetching observation spacing types:', error);
					// Keep default values if API fails
				});
		}

		if (config.aggregationDuration !== undefined && config.aggregationDuration) {
			const durationId = GIAPI.search.constWidget.getId('aggregationDuration');
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'aggregationDuration', {
				helpIconImage: 'fa-hourglass',
				values: [
					{ label: 'Select aggregation duration', value: '' }
				],
				readOnlyValues: true
			}));

			// After constraints are initialized, try to fetch and update values
			const authToken = localStorage.getItem('authToken') || 'my-token';
			fetch(`../services/essi/token/${authToken}/view/${config.view}/om-api/properties?property=aggregationDuration&limit=50`)
				.then(response => response.json())
				.then(data => {
					if (data.aggregationDuration && data.aggregationDuration.length > 0) {
						// Find the select element using the correct ID
						const selectElement = document.getElementById(durationId);
						if (selectElement) {
							const options = [
								{ label: 'Select aggregation duration', value: '' },
								...data.aggregationDuration.map(type => ({
									label: `${type.value} (${type.observationCount} observations)`,
									value: type.value
								}))
							];
							
							// Update the select options
							selectElement.innerHTML = options.map(option => 
								`<option value="${option.value}">${option.label}</option>`
							).join('');
						}
					}
				})
				.catch(error => {
					console.error('Error fetching aggregation duration types:', error);
					// Keep default values if API fails
				});
		}

		var semanticValue = 0;
		if (config.semanticSearchValue !== undefined) {
			semanticValue = config.semanticSearchValue;
		}

		if (config.semanticSearch !== undefined && config.semanticSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.booleanConstraint('get', 'semantics', { ontology: config.ontology, value: semanticValue, helpIconImage: 'fa-comments' }));
		}

		// Only show advanced search if we have constraints to show
		if (advancedConstraints.length > 0) {
			GIAPI.search.constWidget.advancedSearch(
				'advConstDiv',
				'adv-search-div',
				advancedConstraints,
				{
					searchButtonBckColor: '#2c3e50',
					searchButtonLabelColor: 'white',
					advConstDivBckColor: '#f8f9fa'
				}
			);

			// Ensure the advanced search div exists
			if (!$('#adv-search-div').length) {
				$('<div>').attr('id', 'adv-search-div').insertAfter('#search-button');
			}
			if (!$('#advConstDiv').length) {
				$('<div>').attr('id', 'advConstDiv').appendTo('#adv-search-div');
			}
		}

		//------------------------------------
		// Custom PaginatorWidget
		//
		var originalPaginatorWidget = GIAPI.PaginatorWidget;
		GIAPI.PaginatorWidget = function(id, onResponse, options) {
			// Create a wrapper for the onResponse callback
			var wrappedOnResponse = function(response) {
				// Ensure response is an object before setting _origin
				if (response && typeof response === 'object') {
					response._origin = 'paginator';
				}
				// Call the original onResponse if it's a function
				if (typeof onResponse === 'function') {
					return onResponse(response);
				}
				return response;
			};

			// Create the original widget with our wrapped callback
			var widget = originalPaginatorWidget(id, wrappedOnResponse, options);

			// Store the original update function
			var originalUpdate = widget.update;

			// Override the update function
			widget.update = function(resultSet) {
				// Call the original update first
				originalUpdate.call(this, resultSet);

				// Check if user is logged in
				var authToken = localStorage.getItem('authToken');
				if (authToken) {
					// Remove any existing download button
					$('#paginator-widget-top-label .login-button').remove();
					
					// Add bulk download button next to results count
					var downloadButton = $('<button>')
						.addClass('login-button')
						.text('Bulk data download')
						.css({
							'margin-left': '10px',
							'padding': '5px 10px',
							'font-size': '0.9em',
							'background-color': '#2c3e50',
							'color': 'white',
							'border': 'none',
							'border-radius': '4px',
							'cursor': 'pointer'
						})
						.on('click', function() {
							// Create dialog content
							const dialogContent = $('<div>');
							
							// Add description paragraphs
							dialogContent.append($('<p>').text(`This will initiate the bulk download of ${resultSet.size} resources. The process may take some time, depending on the number of resources and current server load.`));
							dialogContent.append($('<p>').text('You can monitor the download status from your personal menu.'));
							
							// Add download name input
							const nameDiv = $('<div>').css({
								'margin-top': '15px',
								'margin-bottom': '15px',
								'padding': '10px',
								'background-color': '#f8f9fa',
								'border-radius': '4px'
							});
							
							nameDiv.append(
								$('<label>')
									.text('Download name:')
									.css({
										'display': 'block',
										'margin-bottom': '10px',
										'font-weight': 'bold',
										'color': '#2c3e50'
									})
							);
							
							const defaultName = new Date().toISOString().slice(0,16).replace('T', '_').replace(':', '-');
							
							// Remove any existing download name input
							$('#downloadName').remove();
							
							nameDiv.append(
								$('<input>')
									.attr({
										'type': 'text',
										'id': 'downloadName',
										'placeholder': 'Enter a name for this download',
										'value': defaultName
									})
									.css({
										'width': '80%',
										'padding': '8px',
										'border': '1px solid #bdc3c7',
										'border-radius': '4px',
										'font-size': '14px',
										'color': '#2c3e50'
									})
							);
							
							dialogContent.append(nameDiv);
							
							// Add format selection
							const formatDiv = $('<div>').css({
								'margin-top': '15px',
								'margin-bottom': '15px',
								'padding': '10px',
								'background-color': '#f8f9fa',
								'border-radius': '4px'
							});
							
							formatDiv.append(
								$('<label>')
									.text('Select data format:')
									.css({
										'display': 'block',
										'margin-bottom': '10px',
										'font-weight': 'bold',
										'color': '#2c3e50'
									})
							);
							
							const formatOptions = $('<div>').css({
								'display': 'flex',
								'gap': '20px'
							});
							
							// CSV option
							const csvOption = $('<div>').css({
								'display': 'flex',
								'align-items': 'center'
							});
							csvOption.append(
								$('<input>').attr({
									'type': 'radio',
									'name': 'downloadFormat',
									'id': 'formatCSV',
									'value': 'CSV',
									'checked': true
								}).css('margin-right', '8px')
							);
							csvOption.append(
								$('<label>')
									.attr('for', 'formatCSV')
									.text('CSV')
									.css('color', '#2c3e50')
							);
							
							// JSON option
							const jsonOption = $('<div>').css({
								'display': 'flex',
								'align-items': 'center'
							});
							jsonOption.append(
								$('<input>').attr({
									'type': 'radio',
									'name': 'downloadFormat',
									'id': 'formatJSON',
									'value': 'JSON'
								}).css('margin-right', '8px')
							);
							jsonOption.append(
								$('<label>')
									.attr('for', 'formatJSON')
									.text('JSON')
									.css('color', '#2c3e50')
							);
							
							formatOptions.append(csvOption);
							formatOptions.append(jsonOption);
							formatDiv.append(formatOptions);
							dialogContent.append(formatDiv);
							
							// Add email notifications checkbox
							const notificationsDiv = $('<div>').css({
								'margin-top': '15px',
								'display': 'flex',
								'align-items': 'center'
							});
							
							notificationsDiv.append(
								$('<input>').attr({
									'type': 'checkbox',
									'id': 'emailNotifications',
									'checked': true
								}).css('margin-right', '8px')
							);
							
							notificationsDiv.append(
								$('<label>')
									.attr('for', 'emailNotifications')
									.text('Send me email notifications about the download status')
									.css({
										'font-size': '14px',
										'color': '#2c3e50'
									})
							);
							
							dialogContent.append(notificationsDiv);

							// Create and show dialog
							dialogContent.dialog({
								title: 'Confirm Bulk Download',
								modal: true,
								width: 400,
								classes: {
									"ui-dialog": "bulk-download-dialog"
								},
								buttons: [
									{
										text: "Proceed to download",
										class: "login-button",
										click: function() {
											// Get the current constraints
											var constraints = GIAPI.search.constWidget.constraints();
											var where = GIAPI.search.resultsMapWidget.where();

											// Build the download URL
											var baseUrl = '../services/essi';
											var token = localStorage.getItem('authToken');
											var params = new URLSearchParams();

											// Add temporal constraints if they exist
											if (constraints.when && constraints.when.from) {
												params.append('beginPosition', constraints.when.from);
											}
											if (constraints.when && constraints.when.to) {
												params.append('endPosition', constraints.when.to);
											}

											// Add spatial constraints if they exist
											if (where) {
												if (where.predefinedLayer) {
													params.append('predefinedLayer', where.predefinedLayer);
												} 
												
												if (where.south && where.west && where.north && where.east)
												{
													params.append('west', where.west);
													params.append('south', where.south);
													params.append('east', where.east);
													params.append('north', where.north);
												}
											}

											// Add parameter constraint if it exists
											if (constraints.kvp && Array.isArray(constraints.kvp)) {
												const attributeTitleValue = constraints.kvp.find(kvp => kvp.key === 'attributeTitle');
												if (attributeTitleValue) {
													params.append('observedProperty', attributeTitleValue.value);
												}
												const intendedObservationSpacingValue = constraints.kvp.find(kvp => kvp.key === 'intendedObservationSpacing');
												if (intendedObservationSpacingValue) {
													params.append('intendedObservationSpacing', intendedObservationSpacingValue.value);
												}
												const aggregationDurationValue = constraints.kvp.find(kvp => kvp.key === 'aggregationDuration');
												if (aggregationDurationValue) {
													params.append('aggregationDuration', aggregationDurationValue.value);
												}
												const timeInterpolationValue = constraints.kvp.find(kvp => kvp.key === 'timeInterpolation');
												if (timeInterpolationValue) {
													params.append('timeInterpolation', timeInterpolationValue.value);
												}
											}

											// Add fixed parameters
											params.append('ontology', config.ontology);

											// Add download name parameter
											const downloadName = $('#downloadName').val().trim() || defaultName;
											
											params.append('asynchDownloadName', downloadName);

											// Add format parameter based on radio selection
											const selectedFormat = $('input[name="downloadFormat"]:checked').val();
											params.append('format', selectedFormat);

											// Add email notifications parameter if checkbox is checked
											if ($('#emailNotifications').is(':checked')) {
												params.append('eMailNotifications', 'true');
											}

											params.append('includeData', 'true');
											params.append('asynchDownload', 'true');

											// Construct the final URL using the view from config
											var downloadUrl = `${baseUrl}/token/${token}/view/${config.view}/om-api/observations?${params.toString()}`;

											// Make the GET request
											fetch(downloadUrl)
												.then(response => {
													if (!response.ok) {
														throw new Error('Network response was not ok');
													}
													return response.json();
												})
												.then(data => {
													// Show success message
													GIAPI.UI_Utils.dialog('open', {
														title: 'Download Started',
														message: 'Your bulk download request has been initiated. You can monitor the download status from your personal menu later.'
													});

													// Find any open status dialog and refresh it
													const existingDialog = $('.bulk-download-dialog');
													if (existingDialog.length > 0) {
														// Trigger a refresh of the status content
														fetchAndUpdateStatus();
													}
												})
												.catch(error => {
													// Show error message
													GIAPI.UI_Utils.dialog('open', {
														title: 'Error',
														message: 'Failed to initiate bulk download. Please try again later.'
													});
													console.error('Download error:', error);
												});

											$(this).dialog("close");
										}
									}
								]
							});
						});

					// Append the button after the results label
					$('#paginator-widget-top-label').append(downloadButton);
				}
			};

			return widget;
		};

		//------------------------------------
		// PaginatorWidget instance
		//
		GIAPI.search.paginatorWidget = GIAPI.PaginatorWidget('paginator-widget',
			function(response) {
				// Ensure response is an object before setting _origin
				if (response && typeof response === 'object') {
					response._origin = 'paginator';
				}
				// Call the original onDiscoverResponse if it exists
				if (typeof GIAPI.search.onDiscoverResponse === 'function') {
					return GIAPI.search.onDiscoverResponse(response);
				}
				return response;
			},
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

		if (config.wmsEndpoint!==undefined){
		GIAPI.search.resultsMapWidget.updateWMSClusterLayers(constraints);
}
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



