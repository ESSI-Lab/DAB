import { GIAPI } from '../giapi/core/GIAPI.js';

var view = '';
var token = '';
var availableViews = []; // Store views fetched for the current source deployment


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

// i18n support
var i18n = { current: 'en', en: {}, it: {} };
function loadI18nSync(lang) {
	var desired = (localStorage.getItem('lang') || (window.config && window.config.language) || lang || 'en').toLowerCase();
	i18n.current = (desired === 'it') ? 'it' : 'en';

	function loadJsonSync(paths) {
		for (var j = 0; j < paths.length; j++) {
			try {
				var xhr = new XMLHttpRequest();
				var url = paths[j];
				// Add cache-busting to ensure latest translations are loaded
				// Use a version that changes when translations are updated
				var separator = url.indexOf('?') === -1 ? '?' : '&';
				url += separator + '_v=2.0'; // Increment this when adding new translation keys
				xhr.open('GET', url, false);
				xhr.setRequestHeader('Cache-Control', 'no-cache');
				xhr.send(null);
				if (xhr.status >= 200 && xhr.status < 300 && (xhr.responseText || '').trim().length > 0) {
					return JSON.parse(xhr.responseText);
				}
			} catch (e) { }
		}
		return {};
	}

	// Base translations shipped with the portal (resolved relative to this module)
	var baseEnUrl = '';
	var baseItUrl = '';
	try {
		baseEnUrl = new URL('./lang/en.json', import.meta.url).toString();
		baseItUrl = new URL('./lang/it.json', import.meta.url).toString();
	} catch (e) {
		// Fallback (works when portal is hosted as /<portal>/search.html and gi-portal is a sibling dir)
		baseEnUrl = '../gi-portal/lang/en.json';
		baseItUrl = '../gi-portal/lang/it.json';
	}

	// Optional portal-local overrides (e.g. /hisc/en.json, /hisc/it.json)
	// These should override the base translations when present.
	var localEnUrl = './en.json';
	var localItUrl = './it.json';
	try {
		localEnUrl = new URL('./en.json', window.location.href).toString();
		localItUrl = new URL('./it.json', window.location.href).toString();
	} catch (e) { }

	var baseEn = loadJsonSync([baseEnUrl, '../gi-portal/lang/en.json']);
	var baseIt = loadJsonSync([baseItUrl, '../gi-portal/lang/it.json']);
	var overrideEn = loadJsonSync([localEnUrl, './en.json']);
	var overrideIt = loadJsonSync([localItUrl, './it.json']);

	// Merge base + overrides (overrides win)
	i18n.en = Object.assign({}, baseEn, overrideEn);
	i18n.it = Object.assign({}, baseIt, overrideIt);
}
function interpolate(template, vars) {
	if (!template || !vars) return template;
	return template.replace(/\$\{([^}]+)\}/g, function(_, k) { return (vars[k] != null) ? vars[k] : ''; });
}
function t(key, vars) {
	var cur = (i18n[i18n.current] || {});
	var str = cur[key] || i18n.en[key] || key;
	return interpolate(str, vars);
}

function translateStatus(status) {
	// Map status values to translation keys
	var statusMap = {
		'Completed': 'status_completed',
		'CompletedWithLimit': 'status_completed_with_limit',
		'PartCompleted': 'status_part_completed',
		'PartInProgress': 'status_part_in_progress',
		'InProgress': 'status_in_progress',
		'Failed': 'status_failed',
		'Canceled': 'status_canceled',
		'Removed': 'status_removed',
		'In Progress': 'status_in_progress',
		'Pending': 'status_pending',
		'Processing': 'status_processing',
		'Started': 'status_started'
	};
	var key = statusMap[status] || null;
	if (key) {
		return t(key);
	}
	// If no translation found, return original status
	return status;
}

/**
 * Parse English status message to get translation key and params (for statuses written before statusMessageKey was added).
 * @param {string} msg - statusMessage string
 * @returns {{ key: string, params: object } | null}
 */
function parseStatusMessageForTranslation(msg) {
	if (!msg || typeof msg !== 'string') return null;
	var m;
	m = msg.match(/^Part (\d+) ready: (\d+) files, ([\d.?]+) MB \(more parts in progress\)$/);
	if (m) return { key: 'status_message_part_ready_more_in_progress', params: { part: m[1], fileCount: m[2], sizeMb: m[3] } };
	m = msg.match(/^Part (\d+): (\d+) files, ([\d.?]+) MB(.*)$/);
	if (m) return { key: 'status_message_part_in_progress', params: { part: m[1], fileCount: m[2], sizeMb: m[3], errorSuffix: m[4] || '' } };
	m = msg.match(/^Downloading: (\d+) files, ([\d.?]+) MB(.*)$/);
	if (m) return { key: 'status_message_downloading', params: { fileCount: m[1], sizeMb: m[2], errorSuffix: m[3] || '' } };
	return null;
}

function lang() {
	return i18n.current;
}

/**
 * Converts ISO8601 duration to a sortable numeric value (total seconds)
 * @param {string} iso8601Value - The ISO8601 duration string
 * @returns {number} - Total seconds for sorting, or 0 if not ISO8601
 */
function getISO8601DurationSortValue(iso8601Value) {
	if (!iso8601Value || typeof iso8601Value !== 'string' || !iso8601Value.startsWith('P')) {
		return 0;
	}

	// Parse ISO8601 pattern: P[nY][nM][nD][T[nH][nM][nS]]
	const pattern = /^P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?)?$/;
	const match = iso8601Value.match(pattern);
	
	if (!match) {
		return 0;
	}

	const [, years, months, days, hours, minutes, seconds] = match.map(v => v ? parseInt(v, 10) : 0);
	
	// Convert to total seconds for comparison
	// Note: Using approximate values for months and years (30 days/month, 365 days/year)
	const totalSeconds = 
		(years * 365 * 24 * 60 * 60) +
		(months * 30 * 24 * 60 * 60) +
		(days * 24 * 60 * 60) +
		(hours * 60 * 60) +
		(minutes * 60) +
		seconds;
	
	return totalSeconds;
}

/**
 * Converts ISO8601 duration strings to natural language
 * @param {string} iso8601Value - The ISO8601 duration string (e.g., "P1D", "PT30M", "PT5H")
 * @returns {string} - Natural language representation or original value if not ISO8601
 */
function formatISO8601Duration(iso8601Value) {
	// Only process values that start with 'P' (ISO8601 duration indicator)
	if (!iso8601Value || typeof iso8601Value !== 'string' || !iso8601Value.startsWith('P')) {
		return iso8601Value;
	}

	const currentLang = lang() || 'en';
	
	// Common ISO8601 duration mappings for frequently used values
	const mappings = {
		en: {
			'P1Y': 'yearly',
			'P1M': 'monthly',
			'P1W': 'weekly',
			'P1D': 'daily',
			'PT1H': 'hourly',
			'PT1M': 'every minute'
		},
		it: {
			'P1Y': 'annuale',
			'P1M': 'mensile',
			'P1W': 'settimanale',
			'P1D': 'giornaliero',
			'PT1H': 'orario',
			'PT1M': 'ogni minuto'
		}
	};

	// Check for exact matches first
	if (mappings[currentLang] && mappings[currentLang][iso8601Value]) {
		return mappings[currentLang][iso8601Value];
	}

	// Parse ISO8601 pattern: P[nY][nM][nD][T[nH][nM][nS]]
	// Examples: P1D, PT1H, PT30M, PT5H, PT20M, P1Y2M3DT4H5M6S
	const pattern = /^P(?:(\d+)Y)?(?:(\d+)M)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?)?$/;
	const match = iso8601Value.match(pattern);
	
	if (!match) {
		return iso8601Value; // Not a valid ISO8601 duration, return as-is
	}

	const [, years, months, days, hours, minutes, seconds] = match.map(v => v ? parseInt(v, 10) : 0);
	
	// Check if this is a time-only duration (PT...)
	const isTimeOnly = !years && !months && !days && (hours || minutes || seconds);
	
	// Check if this is a date-only duration (P... without T)
	const isDateOnly = (years || months || days) && !hours && !minutes && !seconds;
	
	// Check if this is a single unit duration (only one component)
	const componentCount = [years, months, days, hours, minutes, seconds].filter(v => v > 0).length;
	const isSingleUnit = componentCount === 1;

	if (currentLang === 'en') {
		// English formatting
		if (isTimeOnly && isSingleUnit) {
			// Single time unit: "every X hours/minutes"
			if (hours) return hours === 1 ? 'hourly' : `every ${hours} hours`;
			if (minutes) return minutes === 1 ? 'every minute' : `every ${minutes} minutes`;
			if (seconds) return seconds === 1 ? 'every second' : `every ${seconds} seconds`;
		} else if (isDateOnly && isSingleUnit) {
			// Single date unit: use natural forms
			if (years) return years === 1 ? 'yearly' : `every ${years} years`;
			if (months) return months === 1 ? 'monthly' : `every ${months} months`;
			if (days) return days === 1 ? 'daily' : `every ${days} days`;
		}
		
		// Multiple components or complex durations
		const parts = [];
		if (years) parts.push(years === 1 ? '1 year' : `${years} years`);
		if (months) parts.push(months === 1 ? '1 month' : `${months} months`);
		if (days) parts.push(days === 1 ? '1 day' : `${days} days`);
		if (hours) parts.push(hours === 1 ? '1 hour' : `${hours} hours`);
		if (minutes) parts.push(minutes === 1 ? '1 minute' : `${minutes} minutes`);
		if (seconds) parts.push(seconds === 1 ? '1 second' : `${seconds} seconds`);
		
		return parts.length > 0 ? parts.join(', ') : iso8601Value;
	} else {
		// Italian formatting
		if (isTimeOnly && isSingleUnit) {
			// Single time unit: "ogni X ore/minuti"
			if (hours) return hours === 1 ? 'orario' : `ogni ${hours} ore`;
			if (minutes) return minutes === 1 ? 'ogni minuto' : `ogni ${minutes} minuti`;
			if (seconds) return seconds === 1 ? 'ogni secondo' : `ogni ${seconds} secondi`;
		} else if (isDateOnly && isSingleUnit) {
			// Single date unit: use natural forms
			if (years) return years === 1 ? 'annuale' : `ogni ${years} anni`;
			if (months) return months === 1 ? 'mensile' : `ogni ${months} mesi`;
			if (days) return days === 1 ? 'giornaliero' : `ogni ${days} giorni`;
		}
		
		// Multiple components or complex durations
		const parts = [];
		if (years) parts.push(years === 1 ? '1 anno' : `${years} anni`);
		if (months) parts.push(months === 1 ? '1 mese' : `${months} mesi`);
		if (days) parts.push(days === 1 ? '1 giorno' : `${days} giorni`);
		if (hours) parts.push(hours === 1 ? '1 ora' : `${hours} ore`);
		if (minutes) parts.push(minutes === 1 ? '1 minuto' : `${minutes} minuti`);
		if (seconds) parts.push(seconds === 1 ? '1 secondo' : `${seconds} secondi`);
		
		return parts.length > 0 ? parts.join(', ') : iso8601Value;
	}
}

// Expose translator globally for GIAPI widgets
try { window.__t = t; } catch (e) { }

try { window.__lang = lang; } catch (e) { }

function openLanguageChooser() {
	try {
		const dialogDiv = $('<div>');
		dialogDiv.append($('<div>').text(t('choose_language')).css({ 'margin-bottom': '10px' }));
		const current = i18n.current || 'en';
		const enOpt = $('<div>');
		enOpt.append($('<input>').attr({ type: 'radio', name: 'langSel', id: 'lang_en', value: 'en', checked: (current === 'en') }));
		enOpt.append($('<label>').attr('for', 'lang_en').text(t('language_en')).css({ 'margin-left': '6px' }));
		const itOpt = $('<div>');
		itOpt.append($('<input>').attr({ type: 'radio', name: 'langSel', id: 'lang_it', value: 'it', checked: (current === 'it') }));
		itOpt.append($('<label>').attr('for', 'lang_it').text(t('language_it')).css({ 'margin-left': '6px' }));
		dialogDiv.append(enOpt).append(itOpt);
		dialogDiv.dialog({
			title: t('change_language_title'),
			modal: true,
			width: 360,
			buttons: [
				{
					text: 'OK', click: function() {
						const sel = dialogDiv.find('input[name="langSel"]:checked').val() || 'en';
						localStorage.setItem('lang', sel);
						$(this).dialog('close');
						window.location.reload();
					}
				},
				{ text: 'Cancel', click: function() { $(this).dialog('close'); } }
			]
		});
	} catch (e) {
		// Fallback: just toggle en/it and reload
		const next = (i18n.current === 'it') ? 'en' : 'it';
		try { localStorage.setItem('lang', next); } catch (ex) { }
		window.location.reload();
	}
}


function initializeLogin(config) {
	if (!config.login) {
		return;
	}

	// Create and append login elements
	const loginContainer = document.createElement('div');
	loginContainer.className = 'login-container';
	const curLang = (i18n.current || 'en').toUpperCase();
	loginContainer.innerHTML = `
		<button id=\"loginBtn\" class=\"login-button\">${t('login')}</button>
		<button id=\"logoutBtn\" class=\"login-button\" style=\"display: none;\">${t('logout')}</button>
		<button id=\"langBtn\" class=\"login-button lang-button\" title=\"${t('menu_change_language')}\">${curLang}</button>
	`;
	
	// Adjust login container top position to better align with other buttons
	// This will be set after headerDiv is ready
	setTimeout(function() {
		var loginContainerEl = document.querySelector('.login-container');
		if (loginContainerEl) {
			loginContainerEl.style.top = '2px'; // Adjust to match button positions
		}
	}, 100);

	// Append login container to headerDiv instead of body
	const headerDiv = document.getElementById('headerDiv');
	if (headerDiv) {
		headerDiv.appendChild(loginContainer);
	} else {
		document.body.insertBefore(loginContainer, document.body.firstChild);
	}

	// IMPORTANT: Append modal + overlay to <body> so they are fixed to the viewport.
	// If they were inside a transformed ancestor (e.g. the header login container),
	// position:fixed would behave like position:absolute and could overflow the window.
	const existingModal = document.getElementById('loginModal');
	const existingOverlay = document.getElementById('modalOverlay');
	if (!existingModal) {
		const loginModal = document.createElement('div');
		loginModal.id = 'loginModal';
		loginModal.className = 'login-modal';
		loginModal.innerHTML = `
			<button type="button" class="login-modal-close" aria-label="Close">×</button>
			<h3>${t('login_to_portal', { title: (config.title || 'Portal') })}</h3>
			<p class=\"login-info\">${t('login_info')}</p>
			<input type=\"email\" id=\"email\" placeholder=\"${t('email_placeholder')}\" autocomplete=\"off\">
			<input type=\"password\" id=\"apiKey\" placeholder=\"${t('api_key_placeholder')}\" autocomplete=\"off\">
			<button id=\"submitLogin\">${t('login_submit')}</button>
		`;
		document.body.appendChild(loginModal);
	}
	if (!existingOverlay) {
		const modalOverlay = document.createElement('div');
		modalOverlay.id = 'modalOverlay';
		modalOverlay.className = 'modal-overlay';
		document.body.appendChild(modalOverlay);
	}

	// Setup event listeners
	const loginBtn = document.getElementById('loginBtn');
	let logoutBtn = document.getElementById('logoutBtn');
	const langBtn = document.getElementById('langBtn');
	const loginModal = document.getElementById('loginModal');
	const modalOverlay = document.getElementById('modalOverlay');
	const submitLogin = document.getElementById('submitLogin');
	const emailInput = document.getElementById('email');
	const apiKeyInput = document.getElementById('apiKey');
	const closeBtn = loginModal ? loginModal.querySelector('.login-modal-close') : null;

	// Always-visible language chooser button (also for non-logged users)
	if (langBtn) {
		langBtn.addEventListener('click', function(e) {
			e.preventDefault();
			openLanguageChooser();
		});
	}

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
		localStorage.removeItem('lang');
		loginBtn.style.display = 'inline-block';
		loginBtn.textContent = t('login');
		loginBtn.disabled = false;
		logoutBtn.style.display = 'none';
		window.location.reload();
	});

	function closeLoginModal() {
		loginModal.style.display = 'none';
		modalOverlay.style.display = 'none';
		// Clear inputs when closing modal
		emailInput.value = '';
		apiKeyInput.value = '';
	}

	// Close modal using the X button
	if (closeBtn) {
		closeBtn.addEventListener('click', function(e) {
			e.preventDefault();
			closeLoginModal();
		});
	}

	// Hide modal when clicking outside
	modalOverlay.addEventListener('click', function() {
		closeLoginModal();
	});

	// Close modal with ESC key
	document.addEventListener('keydown', function(e) {
		if (e.key === 'Escape' && loginModal.style.display === 'block') {
			closeLoginModal();
		}
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
					// Store admin flag if present
					if (typeof data.admin !== 'undefined') {
						localStorage.setItem('isAdmin', data.admin ? 'true' : 'false');
					} else {
						localStorage.removeItem('isAdmin');
					}
					// In the login response handler, after storing email and admin flag:
					if (typeof data.permissions !== 'undefined') {
						localStorage.setItem('userPermissions', data.permissions);
					} else {
						localStorage.removeItem('userPermissions');
					}

					// Update UI
					loginBtn.style.display = 'none';
					logoutBtn.style.display = 'inline-block';
					logoutBtn.textContent = t('logged_in', { email: data.email });

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
	const isAdmin = localStorage.getItem('isAdmin') === 'true';
	if (existingToken && existingEmail) {
		loginBtn.style.display = 'none';
		logoutBtn.style.display = 'inline-block';
		logoutBtn.textContent = t('logged_in', { email: existingEmail });

		// Create user menu
		const userMenu = document.createElement('div');
		userMenu.id = 'userMenu';
		userMenu.className = 'user-menu';
		userMenu.style.display = 'none';
		let menuHtml = `
			<button id=\"statusBtn\" class=\"menu-button\">${t('menu_status')}</button>\n`;
		if (isAdmin) {
			menuHtml += `<button id=\"listUsersBtn\" class=\"menu-button\">${t('menu_manage_users')}</button>\n`;
			menuHtml += `<button id=\"dataReportBtn\" class=\"menu-button\">${t('menu_data_report')}</button>\n`;
		}
		menuHtml += `<button id=\"logoutMenuBtn\" class=\"menu-button\">${t('menu_logout')}</button>`;
		userMenu.innerHTML = menuHtml;
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
					'border-radius': '4px',
					'flex-shrink': '0'
				})
				.append(
					$('<p>')
						.css({
							'margin': '0',
							'color': '#2c3e50'
						})
						.html(t('bulk_note_html'))
				);

			dialogContent.prepend(infoText);

			// Create a wrapper div for the refresh button
			const refreshButtonWrapper = $('<div>')
				.css({
					'margin-bottom': '10px',
					'margin-top': '15px',
					'text-align': 'right',
					'position': 'relative',
					'z-index': '1000',
					'flex-shrink': '0'
				});

			// Create refresh button
			const refreshButton = $('<button>')
				.attr('type', 'button')
				.attr('id', 'bulk-download-refresh-btn')
				.addClass('refresh-button')
				.html(`<i class="fa fa-refresh"></i> ${t('refresh')}`)
				.css({
					'position': 'relative',
					'z-index': '1001',
					'pointer-events': 'auto',
					'cursor': 'pointer',
					'padding': '6px 12px',
					'background-color': '#2c3e50',
					'color': 'white',
					'border': 'none',
					'border-radius': '4px',
					'font-size': '14px',
					'margin': '0'
				});

			refreshButtonWrapper.append(refreshButton);
			dialogContent.append(refreshButtonWrapper);
			
			// Create a scrollable container for the status content
			const statusContentWrapper = $('<div>')
				.attr('id', 'status-content-wrapper')
				.css({
					'max-height': (window.innerHeight - 300) + 'px',
					'overflow-y': 'auto',
					'overflow-x': 'hidden',
					'position': 'relative',
					'flex': '1',
					'min-height': '0'
				});
			
			statusContentWrapper.append($('<div>').attr('id', 'status-content'));
			dialogContent.append(statusContentWrapper);

			// Show dialog first
			const dialog = dialogContent.dialog({
				title: t('bulk_downloads_status_title'),
				modal: true,
				width: 1310,
				maxHeight: window.innerHeight - 100,
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
				'padding': '0 15px',
				'position': 'relative',
				'max-height': (window.innerHeight - 200) + 'px',
				'display': 'flex',
				'flex-direction': 'column'
			});
			
			// Add custom scrollbar styling for the status content wrapper
			setTimeout(function() {
				const wrapper = $('#status-content-wrapper');
				if (wrapper.length) {
					// Add smooth scrolling
					wrapper.css({
						'scroll-behavior': 'smooth'
					});
					
					// Add some padding to the status content for better appearance
					$('#status-content').css({
						'padding-bottom': '10px'
					});
				}
			}, 150);
			
			// Function to recalculate scrollable area height
			const recalculateScrollHeight = function() {
				const wrapper = $('#status-content-wrapper');
				if (wrapper.length && dialogContent.parent().length) {
					// Recalculate max-height based on actual dialog size
					const dialogHeight = dialogContent.parent().height();
					const infoTextHeight = infoText.outerHeight(true) || 0;
					const headerHeight = refreshButtonWrapper.outerHeight(true) || 50;
					const padding = 40;
					const calculatedHeight = dialogHeight - infoTextHeight - headerHeight - padding;
					wrapper.css('max-height', Math.max(300, calculatedHeight) + 'px');
				}
			};
			
			// Initial calculation
			setTimeout(recalculateScrollHeight, 100);
			
			// Recalculate on window resize
			const resizeHandler = function() {
				recalculateScrollHeight();
			};
			$(window).on('resize.bulk-download-dialog', resizeHandler);
			
			// Clean up resize handler when dialog is closed
			const originalClose = dialogContent.dialog('option', 'close');
			dialogContent.dialog('option', 'close', function() {
				$(window).off('resize.bulk-download-dialog');
				if (originalClose) {
					originalClose.call(this);
				}
			});

			// Ensure the refresh button wrapper is clickable after dialog is created
			setTimeout(function() {
				refreshButtonWrapper.css({
					'position': 'relative',
					'z-index': '10000',
					'pointer-events': 'auto'
				});
				refreshButton.css({
					'position': 'relative',
					'z-index': '10001',
					'pointer-events': 'auto',
					'display': 'inline-block'
				});
			}, 100);

			// Add hover effect to refresh button using event delegation
			$(document).off('mouseenter mouseleave', '#bulk-download-refresh-btn')
				.on('mouseenter', '#bulk-download-refresh-btn', function() {
					$(this).css('background-color', '#34495e');
				})
				.on('mouseleave', '#bulk-download-refresh-btn', function() {
					$(this).css('background-color', '#2c3e50');
				});

			// Function to fetch and update status
			const fetchAndUpdateStatus = () => {
				const statusContent = $('#status-content');
				const refreshBtn = $('#bulk-download-refresh-btn');
				statusContent.html(`<p>${t('bulk_status_loading')}</p>`);

				// Disable refresh button while loading
				refreshBtn.prop('disabled', true);
				refreshBtn.find('i').addClass('fa-spin');

				// Fetch status from API
				const authToken = localStorage.getItem('authToken');
				fetch(`../services/essi/token/${authToken}/view/${view}/om-api/downloads`, {
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
								.append($('<th>').text(t('col_timestamp')).css('width', '160px'))
								.append($('<th>').text(t('col_task_id')).css('width', '250px'))
								.append($('<th>').text(t('col_name')).css('width', '200px'))
								.append($('<th>').text(t('col_status')).css('width', '200px'))
								.append($('<th>').text(t('col_size_mb')).css('width', '100px'))
								.append($('<th>').text(t('col_download')).css('width', '100px'))
								.append($('<th>').text(t('col_actions')).css('width', '100px'));
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

								// Status column: translate statusMessageKey+Params when present; fallback parse English statusMessage for translation
								let statusText;
								if (item.statusMessageKey && item.statusMessageParams && typeof item.statusMessageParams === 'object') {
									statusText = t(item.statusMessageKey, item.statusMessageParams);
								} else if (item.statusMessage != null && item.statusMessage !== '') {
									const parsed = parseStatusMessageForTranslation(item.statusMessage);
									statusText = parsed ? t(parsed.key, parsed.params) : item.statusMessage;
								} else if (item.message != null && item.message !== '') {
									statusText = item.message;
								} else {
									statusText = translateStatus(item.status);
								}
								row.append($('<td>')
									.text(statusText)
									.attr('title', statusText)
									.css({
										'width': '200px',
										'white-space': 'normal',
										'overflow': 'visible'
									})
								);

								// Size column: total uncompressed size in MB (fallback to sizeInMB for backward compatibility)
								const sizeToShow = item.totalUncompressedSizeInMB != null ? item.totalUncompressedSizeInMB : item.sizeInMB;
								row.append($('<td>')
									.text(sizeToShow != null ? sizeToShow.toLocaleString(undefined, {
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

								// Download column: list all parts when partNumber > 1 (infer from last locator)
								const downloadCell = $('<td>').css({
									'width': '100px',
									'min-width': '100px',
									'white-space': 'normal',
									'overflow': 'visible',
									'vertical-align': 'top'
								});
								const showPartLinksStatuses = ['Completed', 'CompletedWithLimit', 'PartCompleted', 'PartInProgress'];
								const locators = (item.locators && Array.isArray(item.locators) && item.locators.length > 0)
									? item.locators
									: (item.locator ? [item.locator] : []);
								if (showPartLinksStatuses.includes(item.status) && locators.length > 0) {
									locators.forEach((partUrl, idx) => {
										const partNum = idx + 1;
										const linkText = locators.length > 1 ? t('download_part_label', { n: partNum }) : t('download_single_label');
										const linkTitle = locators.length > 1 ? t('download_part_link_title', { n: partNum }) : t('download_link_title');
										const link = $('<a>')
											.attr('href', partUrl)
											.attr('target', '_blank')
											.attr('title', linkTitle + ': ' + partUrl)
											.text(linkText)
											.css('display', 'block');
										downloadCell.append(link);
									});
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
								if (!['Completed', 'CompletedWithLimit', 'Failed', 'Canceled', 'Removed'].includes(item.status)) {
									const cancelButton = $('<button>')
										.addClass('cancel-button')
										.html('<i class="fa fa-times"></i>')
										.attr('title', t('cancel_download_title'))
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
											if (confirm(t('confirm_cancel_download'))) {
												const authToken = localStorage.getItem('authToken');
												fetch(`../services/essi/token/${authToken}/view/${view}/om-api/downloads?id=${item.id}`, {
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
														alert(t('cancel_download_failed'));
													});
											}
										});
									actionsCell.append(cancelButton);
								}
								// Show remove button for completed downloads
								else if (['Completed', 'CompletedWithLimit'].includes(item.status)) {
									const removeButton = $('<button>')
										.addClass('remove-button')
										.html('<i class="fa fa-trash"></i>')
										.attr('title', t('remove_download_title'))
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
											if (confirm(t('confirm_remove_download'))) {
												const authToken = localStorage.getItem('authToken');
												fetch(`../services/essi/token/${authToken}/view/${view}/om-api/downloads?id=${item.id}`, {
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
														alert(t('remove_download_failed'));
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
							statusContent.append($('<p>').text(t('bulk_empty')));
						}
					})
					.catch(error => {
						console.error('Error fetching status:', error);
						statusContent.empty().append(
							$('<p>').text(t('bulk_error'))
						);
					})
					.finally(() => {
						// Re-enable refresh button and stop spinning
						const refreshBtn = $('#bulk-download-refresh-btn');
						refreshBtn.prop('disabled', false);
						refreshBtn.find('i').removeClass('fa-spin');
					});
			};

			// Add click handler to refresh button using event delegation
			// This ensures it works even after dialog DOM manipulation
			$(document).off('click', '#bulk-download-refresh-btn')
				.on('click', '#bulk-download-refresh-btn', function(e) {
					e.preventDefault();
					e.stopPropagation();
					if (!$(this).prop('disabled')) {
						fetchAndUpdateStatus();
					}
					return false;
				});

			// Also attach handler directly after dialog is fully rendered
			setTimeout(function() {
				const btn = $('#bulk-download-refresh-btn');
				if (btn.length) {
					btn.off('click.refresh').on('click.refresh', function(e) {
						e.preventDefault();
						e.stopPropagation();
						if (!$(this).prop('disabled')) {
							fetchAndUpdateStatus();
						}
						return false;
					});
					// Ensure button is visible and clickable
					btn.css({
						'pointer-events': 'auto',
						'cursor': 'pointer',
						'opacity': '1'
					});
				}
			}, 300);

			// Initial fetch after dialog is shown
			setTimeout(fetchAndUpdateStatus, 100);
		});

		// List Users button click handler (admin only)
		if (isAdmin) {
			document.getElementById('listUsersBtn').addEventListener('click', function() {
				userMenu.style.display = 'none';
				// Create dialog content
				const dialogContent = $('<div>');
				dialogContent.append($('<h3>').text(t('menu_manage_users')));
				dialogContent.append($('<p>').text(t('users_intro')));
				// Results area
				const resultsDiv = $('<div>').attr('id', 'listUsersResults').css({ 'margin-top': '15px' });
				dialogContent.append(resultsDiv);
				// --- Move function definitions here so they are in scope for both dialog and fetch ---
				function renderTable(users) {
					// Sort users: first by having permissions, then by email
					const sortedUsers = users.slice().sort((a, b) => {
						// Extract email and permissions for both users
						const getProp = (u, name) => {
							if (!Array.isArray(u.properties)) return '';
							const p = u.properties.find(p => p.name === name);
							return p ? p.value : '';
						};
						const aPerm = getProp(a, 'permissions');
						const bPerm = getProp(b, 'permissions');
						const aEmail = getProp(a, 'email').toLowerCase();
						const bEmail = getProp(b, 'email').toLowerCase();
						// Users with permissions come first
						if (!!aPerm && !bPerm) return -1;
						if (!aPerm && !!bPerm) return 1;
						// If both have (or both don't have) permissions, sort by email
						if (aEmail < bEmail) return -1;
						if (aEmail > bEmail) return 1;
						return 0;
					});
					let table = '<table id="usersTable" style="width:100%;border-collapse:collapse;margin-top:10px;cursor:pointer">';
					table += '<tr>' +
						`<th style="border-bottom:1px solid #ccc;text-align:left;padding:4px">${t('th_email')}</th>` +
						`<th style="border-bottom:1px solid #ccc;text-align:left;padding:4px">${t('th_first_name')}</th>` +
						`<th style="border-bottom:1px solid #ccc;text-align:left;padding:4px">${t('th_last_name')}</th>` +
						`<th style="border-bottom:1px solid #ccc;text-align:left;padding:4px">${t('th_institution')}</th>` +
						`<th style="border-bottom:1px solid #ccc;text-align:left;padding:4px">${t('th_permissions')}</th>` +
						'</tr>';
					sortedUsers.forEach((u, idx) => {
						const propMap = {};
						if (Array.isArray(u.properties)) {
							u.properties.forEach(p => { propMap[p.name] = p.value; });
						}
						table += `<tr class='user-row' data-user-idx='${idx}' style='cursor:pointer'>` +
							`<td style='padding:4px'>${propMap['email'] || ''}</td>` +
							`<td style='padding:4px'>${propMap['firstName'] || ''}</td>` +
							`<td style='padding:4px'>${propMap['lastName'] || ''}</td>` +
							`<td style='padding:4px'>${propMap['institution'] || ''}</td>` +
							`<td style='padding:4px'>${propMap['permissions'] || ''}</td>` +
							'</tr>';
					});
					table += '</table>';
					// Wrap table in a scrollable div
					// Pass sortedUsers to bindRowClicks for correct details
					setTimeout(() => { bindRowClicks(sortedUsers); }, 0);
					return `<div style='max-height:400px;overflow-y:auto'>${table}</div>`;
				}
				function bindRowClicks(users) {
					$('#usersTable .user-row').off('click').on('click', function() {
						const userIdx = $(this).data('user-idx');
						const user = users[userIdx];
						const propMap = {};
						if (Array.isArray(user.properties)) {
							user.properties.forEach(p => { propMap[p.name] = p.value; });
						}
						let details = '<table style="width:100%;border-collapse:collapse">';
						details += `<tr><th style='text-align:left;padding:4px'>${t('details_field')}</th><th style='text-align:left;padding:4px'>${t('details_value')}</th></tr>`;
						details += `<tr><td style='padding:4px'>${t('details_identifier')}</td><td style='padding:4px'>${user.identifier || ''}</td></tr>`;
						details += `<tr><td style='padding:4px'>${t('details_role')}</td><td style='padding:4px'>${user.role || ''}</td></tr>`;
						details += `<tr><td style='padding:4px'>${t('details_enabled')}</td><td style='padding:4px'>${user.enabled === false ? t('no') : t('yes')}</td></tr>`;
						Object.keys(propMap).forEach(name => {
							const value = propMap[name];
							if (name === 'permissions') {
								details += `<tr><td style='padding:4px'>${name}</td><td style='padding:4px'><span class='user-prop-value' data-prop-name='${name}'>${value}</span> <button class='edit-permissions-btn' data-prop-name='${name}' style='border:none;background:none;cursor:pointer;padding:0 4px'><i class='fa fa-pencil'></i> ${t('edit')}</button></td></tr>`;
							} else if (name === 'allowedViews') {
								// Format allowedViews to show view labels with IDs in parentheses
								let displayValue = value;
								if (value && availableViews.length > 0) {
									const viewIds = value.split(',').map(v => v.trim()).filter(Boolean);
									const viewLabels = viewIds.map(id => {
										const view = availableViews.find(v => v.id === id);
										if (view) {
											const label = view.label || id;
											return label + ' (' + id + ')';
										}
										return id;
									});
									displayValue = viewLabels.join(', ');
								}
								// Store the actual value (IDs) in data attribute for editing
								details += `<tr><td style='padding:4px'>${name}</td><td style='padding:4px'><span class='user-prop-value' data-prop-name='${name}' data-prop-value='${value || ''}'>${displayValue}</span> <button class='edit-views-btn' data-prop-name='${name}' style='border:none;background:none;cursor:pointer;padding:0 4px'><i class='fa fa-pencil'></i> ${t('edit')}</button></td></tr>`;
							} else if (name === 'maxDownloadSizeMB') {
								details += `<tr><td style='padding:4px'>${name}</td><td style='padding:4px'><span class='user-prop-value' data-prop-name='${name}'>${value}</span> <button class='edit-download-size-btn' data-prop-name='${name}' style='border:none;background:none;cursor:pointer;padding:0 4px'><i class='fa fa-pencil'></i> ${t('edit')}</button></td></tr>`;
							} else if (name === 'maxDownloadPartSizeMB') {
								details += `<tr><td style='padding:4px'>${name}</td><td style='padding:4px'><span class='user-prop-value' data-prop-name='${name}'>${value}</span> <button class='edit-download-part-size-btn' data-prop-name='${name}' style='border:none;background:none;cursor:pointer;padding:0 4px'><i class='fa fa-pencil'></i> ${t('edit')}</button></td></tr>`;
							} else {
								details += `<tr><td style='padding:4px'>${name}</td><td style='padding:4px'><span class='user-prop-value' data-prop-name='${name}'>${value}</span> <button class='edit-prop-btn' data-prop-name='${name}' style='border:none;background:none;cursor:pointer;padding:0 4px'><i class='fa fa-pencil'></i> ${t('edit')}</button></td></tr>`;
							}
						});
						details += '</table>';
						// Add 'Add Permissions' button if permissions property is missing
						if (!('permissions' in propMap)) {
							details += `<div style='margin-top:12px'><button id='add-permissions-btn' style='background:#2c3e50;color:white;border:none;border-radius:4px;padding:6px 16px;cursor:pointer;font-size:1em'><i class='fa fa-plus'></i> ${t('add_permissions')}</button></div>`;
						}
						// Add 'Add Allowed Views' button if allowedViews property is missing
						if (!('allowedViews' in propMap)) {
							details += `<div style='margin-top:12px'><button id='add-allowed-views-btn' style='background:#2c3e50;color:white;border:none;border-radius:4px;padding:6px 16px;cursor:pointer;font-size:1em'><i class='fa fa-plus'></i> Add Allowed Views</button></div>`;
						}
						// Add 'Add Download Size (MB)' button if maxDownloadSizeMB property is missing
						if (!('maxDownloadSizeMB' in propMap)) {
							details += `<div style='margin-top:12px'><button id='add-download-size-btn' style='background:#2c3e50;color:white;border:none;border-radius:4px;padding:6px 16px;cursor:pointer;font-size:1em'><i class='fa fa-plus'></i> ${t('add_download_size_mb')}</button></div>`;
						}
						// Add 'Add Max Download Part Size (MB)' button if maxDownloadPartSizeMB property is missing
						if (!('maxDownloadPartSizeMB' in propMap)) {
							details += `<div style='margin-top:12px'><button id='add-download-part-size-btn' style='background:#2c3e50;color:white;border:none;border-radius:4px;padding:6px 16px;cursor:pointer;font-size:1em'><i class='fa fa-plus'></i> ${t('add_download_part_size_mb')}</button></div>`;
						}
						const detailsDialog = $('<div>').html(details).dialog({
							title: t('details_title'),
							modal: true,
							width: 600,
							buttons: [
								{
									text: t('remove_user'),
									class: 'remove-user-button',
									click: function() {
										if (!confirm(t('confirm_remove_user'))) return;
										const email = localStorage.getItem('userEmail');
										const apiKey = localStorage.getItem('authToken');
										const userIdentifier = user.identifier;
										if (!email || !apiKey || !userIdentifier) {
											alert(t('missing_credentials_or_id'));
											return;
										}
										fetch('../services/support/deleteUser', {
											method: 'DELETE',
											headers: { 'Content-Type': 'application/json' },
											body: JSON.stringify({ email, apiKey, userIdentifier })
										})
											.then(response => response.json())
											.then(result => {
												if (result.success) {
													alert(t('users_removed_ok'));
													detailsDialog.dialog('close');
													dialogContent.dialog('close');
													document.getElementById('listUsersBtn').click();
												} else {
													alert(t('users_removed_fail') + ' ' + (result.message || 'Unknown error'));
												}
											})
											.catch(err => {
												alert('Error removing user: ' + err);
											});
									}
								},
								{ text: t('close'), click: function() { $(this).dialog('close'); } }
							]
						});
						// Add handler for Add Permissions button
						detailsDialog.on('click', '#add-permissions-btn', function() {
							showPermissionsDialog('', function(permissions) {
								if (permissions === null) return;
								const email = localStorage.getItem('userEmail');
								const apiKey = localStorage.getItem('authToken');
								const userIdentifier = user.identifier;
								if (!email || !apiKey || !userIdentifier) {
									alert('Missing credentials or user identifier.');
									return;
								}
								fetch('../services/support/updateUser', {
									method: 'POST',
									headers: { 'Content-Type': 'application/json' },
									body: JSON.stringify({ email, apiKey, userIdentifier, propertyName: 'permissions', propertyValue: permissions })
								})
									.then(response => response.json())
									.then(result => {
										if (result.success) {
											alert('Permissions added.');
											detailsDialog.dialog('close');
											// Refresh the main user list panel
											dialogContent.dialog('close');
											document.getElementById('listUsersBtn').click();
										} else {
											alert('Failed to add permissions: ' + (result.message || 'Unknown error'));
										}
									})
									.catch(err => {
										alert('Error adding permissions: ' + err);
									});
							});
						});
						// Add handler for Add Allowed Views button
						detailsDialog.on('click', '#add-allowed-views-btn', function() {
							showViewsDialog('', function(allowedViews) {
								if (allowedViews === null) return;
								const email = localStorage.getItem('userEmail');
								const apiKey = localStorage.getItem('authToken');
								const userIdentifier = user.identifier;
								if (!email || !apiKey || !userIdentifier) {
									alert('Missing credentials or user identifier.');
									return;
								}
								fetch('../services/support/updateUser', {
									method: 'POST',
									headers: { 'Content-Type': 'application/json' },
									body: JSON.stringify({ email, apiKey, userIdentifier, propertyName: 'allowedViews', propertyValue: allowedViews })
								})
									.then(response => response.json())
									.then(result => {
										if (result.success) {
											alert('Allowed views added.');
											detailsDialog.dialog('close');
											// Refresh the main user list panel
											dialogContent.dialog('close');
											document.getElementById('listUsersBtn').click();
										} else {
											alert('Failed to add allowed views: ' + (result.message || 'Unknown error'));
										}
									})
									.catch(err => {
										alert('Error adding allowed views: ' + err);
									});
							});
						});
						// Helper: parse integer for MB properties; returns number or null if invalid
						function parseDownloadSizeMB(input) {
							if (input === null || input === undefined) return null;
							const s = String(input).trim();
							if (s === '') return null;
							const n = parseInt(s, 10);
							if (isNaN(n) || n < 0 || String(n) !== s) return null;
							return n;
						}
						// Add handler for Add Download Size (MB) button
						detailsDialog.on('click', '#add-download-size-btn', function() {
							const raw = prompt(t('download_size_mb_prompt_add'), '');
							const value = parseDownloadSizeMB(raw);
							if (value === null) {
								if (raw !== null && raw !== '') alert(t('download_size_mb_invalid'));
								return;
							}
							const email = localStorage.getItem('userEmail');
							const apiKey = localStorage.getItem('authToken');
							const userIdentifier = user.identifier;
							if (!email || !apiKey || !userIdentifier) {
								alert(t('missing_credentials_or_id'));
								return;
							}
							fetch('../services/support/updateUser', {
								method: 'POST',
								headers: { 'Content-Type': 'application/json' },
								body: JSON.stringify({ email, apiKey, userIdentifier, propertyName: 'maxDownloadSizeMB', propertyValue: String(value) })
							})
								.then(response => response.json())
								.then(result => {
									if (result.success) {
										alert(t('download_size_mb_added'));
										detailsDialog.dialog('close');
										dialogContent.dialog('close');
										document.getElementById('listUsersBtn').click();
									} else {
										alert(t('property_update_fail') + ' ' + (result.message || 'Unknown error'));
									}
								})
								.catch(err => {
									alert('Error adding download size: ' + err);
								});
						});
						// Edit maxDownloadSizeMB handler
						detailsDialog.on('click', '.edit-download-size-btn', function(e) {
							e.preventDefault();
							const propName = $(this).data('prop-name');
							const valueSpan = detailsDialog.find(`.user-prop-value[data-prop-name='${propName}']`);
							const oldValue = valueSpan.text();
							const raw = prompt(t('download_size_mb_prompt_edit'), oldValue);
							const newValue = parseDownloadSizeMB(raw);
							if (newValue === null) {
								if (raw !== null && raw !== '') alert(t('download_size_mb_invalid'));
								return;
							}
							if (parseDownloadSizeMB(oldValue) === newValue) return;
							const email = localStorage.getItem('userEmail');
							const apiKey = localStorage.getItem('authToken');
							const userIdentifier = user.identifier;
							if (!email || !apiKey || !userIdentifier) {
								alert(t('missing_credentials_or_id'));
								return;
							}
							fetch('../services/support/updateUser', {
								method: 'POST',
								headers: { 'Content-Type': 'application/json' },
								body: JSON.stringify({ email, apiKey, userIdentifier, propertyName: 'maxDownloadSizeMB', propertyValue: String(newValue) })
							})
								.then(response => response.json())
								.then(result => {
									if (result.success) {
										alert(t('property_updated'));
										detailsDialog.dialog('close');
										dialogContent.dialog('close');
										document.getElementById('listUsersBtn').click();
									} else {
										alert(t('property_update_fail') + ' ' + (result.message || 'Unknown error'));
									}
								})
								.catch(err => {
									alert('Error updating download size: ' + err);
								});
						});
						// Add handler for Add Download Part Size (MB) button
						detailsDialog.on('click', '#add-download-part-size-btn', function() {
							const raw = prompt(t('download_part_size_mb_prompt_add'), '');
							const value = parseDownloadSizeMB(raw);
							if (value === null) {
								if (raw !== null && raw !== '') alert(t('download_size_mb_invalid'));
								return;
							}
							const email = localStorage.getItem('userEmail');
							const apiKey = localStorage.getItem('authToken');
							const userIdentifier = user.identifier;
							if (!email || !apiKey || !userIdentifier) {
								alert(t('missing_credentials_or_id'));
								return;
							}
							fetch('../services/support/updateUser', {
								method: 'POST',
								headers: { 'Content-Type': 'application/json' },
								body: JSON.stringify({ email, apiKey, userIdentifier, propertyName: 'maxDownloadPartSizeMB', propertyValue: String(value) })
							})
								.then(response => response.json())
								.then(result => {
									if (result.success) {
										alert(t('download_part_size_mb_added'));
										detailsDialog.dialog('close');
										dialogContent.dialog('close');
										document.getElementById('listUsersBtn').click();
									} else {
										alert(t('property_update_fail') + ' ' + (result.message || 'Unknown error'));
									}
								})
								.catch(err => {
									alert('Error adding download part size: ' + err);
								});
						});
						// Edit maxDownloadPartSizeMB handler
						detailsDialog.on('click', '.edit-download-part-size-btn', function(e) {
							e.preventDefault();
							const propName = $(this).data('prop-name');
							const valueSpan = detailsDialog.find(`.user-prop-value[data-prop-name='${propName}']`);
							const oldValue = valueSpan.text();
							const raw = prompt(t('download_part_size_mb_prompt_edit'), oldValue);
							const newValue = parseDownloadSizeMB(raw);
							if (newValue === null) {
								if (raw !== null && raw !== '') alert(t('download_size_mb_invalid'));
								return;
							}
							if (parseDownloadSizeMB(oldValue) === newValue) return;
							const email = localStorage.getItem('userEmail');
							const apiKey = localStorage.getItem('authToken');
							const userIdentifier = user.identifier;
							if (!email || !apiKey || !userIdentifier) {
								alert(t('missing_credentials_or_id'));
								return;
							}
							fetch('../services/support/updateUser', {
								method: 'POST',
								headers: { 'Content-Type': 'application/json' },
								body: JSON.stringify({ email, apiKey, userIdentifier, propertyName: 'maxDownloadPartSizeMB', propertyValue: String(newValue) })
							})
								.then(response => response.json())
								.then(result => {
									if (result.success) {
										alert(t('property_updated'));
										detailsDialog.dialog('close');
										dialogContent.dialog('close');
										document.getElementById('listUsersBtn').click();
									} else {
										alert(t('property_update_fail') + ' ' + (result.message || 'Unknown error'));
									}
								})
								.catch(err => {
									alert('Error updating download part size: ' + err);
								});
						});
						// Edit permissions handler
						detailsDialog.on('click', '.edit-permissions-btn', function(e) {
							e.preventDefault();
							const propName = $(this).data('prop-name');
							const valueSpan = detailsDialog.find(`.user-prop-value[data-prop-name='${propName}']`);
							const oldValue = valueSpan.text();
							showPermissionsDialog(oldValue, function(newPermissions) {
								if (newPermissions === oldValue) return;
								const email = localStorage.getItem('userEmail');
								const apiKey = localStorage.getItem('authToken');
								const userIdentifier = user.identifier;
								if (!email || !apiKey || !userIdentifier) {
									alert('Missing credentials or user identifier.');
									return;
								}
								fetch('../services/support/updateUser', {
									method: 'POST',
									headers: { 'Content-Type': 'application/json' },
									body: JSON.stringify({ email, apiKey, userIdentifier, propertyName: 'permissions', propertyValue: newPermissions })
								})
									.then(response => response.json())
									.then(result => {
										if (result.success) {
											alert('Permissions updated.');
											detailsDialog.dialog('close');
											// Refresh the main user list panel
											dialogContent.dialog('close');
											document.getElementById('listUsersBtn').click();
										} else {
											alert('Failed to update permissions: ' + (result.message || 'Unknown error'));
										}
									})
									.catch(err => {
										alert('Error updating permissions: ' + err);
									});
							});
						});
						// Edit allowedViews handler
						detailsDialog.on('click', '.edit-views-btn', function(e) {
							e.preventDefault();
							const propName = $(this).data('prop-name');
							// Get the actual value (IDs) from the data attribute
							const valueSpan = detailsDialog.find(`.user-prop-value[data-prop-name='${propName}']`);
							const oldValue = valueSpan.data('prop-value') || propMap['allowedViews'] || '';
							showViewsDialog(oldValue, function(newAllowedViews) {
								if (newAllowedViews === oldValue) return;
								const email = localStorage.getItem('userEmail');
								const apiKey = localStorage.getItem('authToken');
								const userIdentifier = user.identifier;
								if (!email || !apiKey || !userIdentifier) {
									alert('Missing credentials or user identifier.');
									return;
								}
								fetch('../services/support/updateUser', {
									method: 'POST',
									headers: { 'Content-Type': 'application/json' },
									body: JSON.stringify({ email, apiKey, userIdentifier, propertyName: 'allowedViews', propertyValue: newAllowedViews })
								})
									.then(response => response.json())
									.then(result => {
										if (result.success) {
											alert('Allowed views updated.');
											detailsDialog.dialog('close');
											// Refresh the main user list panel
											dialogContent.dialog('close');
											document.getElementById('listUsersBtn').click();
										} else {
											alert('Failed to update allowed views: ' + (result.message || 'Unknown error'));
										}
									})
									.catch(err => {
										alert('Error updating allowed views: ' + err);
									});
							});
						});
						// Add handler for editing other properties
						detailsDialog.on('click', '.edit-prop-btn', function(e) {
							e.preventDefault();
							const propName = $(this).data('prop-name');
							const valueSpan = detailsDialog.find(`.user-prop-value[data-prop-name='${propName}']`);
							const oldValue = valueSpan.text();
							const newValue = prompt(`Edit value for ${propName}:`, oldValue);
							if (newValue === null || newValue === oldValue) return;
							const email = localStorage.getItem('userEmail');
							const apiKey = localStorage.getItem('authToken');
							const userIdentifier = user.identifier;
							if (!email || !apiKey || !userIdentifier) {
								alert('Missing credentials or user identifier.');
								return;
							}
							fetch('../services/support/updateUser', {
								method: 'POST',
								headers: { 'Content-Type': 'application/json' },
								body: JSON.stringify({ email, apiKey, userIdentifier, propertyName: propName, propertyValue: newValue })
							})
								.then(response => response.json())
								.then(result => {
									if (result.success) {
										alert('Property updated.');
										detailsDialog.dialog('close');
										dialogContent.dialog('close');
										document.getElementById('listUsersBtn').click();
									} else {
										alert('Failed to update property: ' + (result.message || 'Unknown error'));
									}
								})
								.catch(err => {
									alert('Error updating property: ' + err);
								});
						});
					});
				}
				// --- End function definitions ---
				dialogContent.dialog({
					title: t('menu_manage_users'),
					modal: true,
					width: 800,
					position: { my: 'center', at: 'center top+80', of: window },
					buttons: [
						{
							text: t('refresh'),
							click: function() {
								const resultsDiv = dialogContent.find('#listUsersResults');
								resultsDiv.html(t('refreshing'));
								const email = localStorage.getItem('userEmail');
								const apiKey = localStorage.getItem('authToken');
								if (!email || !apiKey) {
									resultsDiv.html('<span style="color:red">' + t('missing_credentials') + '</span>');
									return;
								}
								fetch('../services/support/listUsers', {
									method: 'POST',
									headers: { 'Content-Type': 'application/json' },
									body: JSON.stringify({ email: email, apiKey: apiKey })
								})
									.then(response => response.json())
									.then(newData => {
										if (newData.success && Array.isArray(newData.users)) {
											resultsDiv.html(renderTable(newData.users));
											bindRowClicks(newData.users);
										} else {
											resultsDiv.html('<span style="color:red">' + (newData.message || t('failed_fetch_users')) + '</span>');
										}
									})
									.catch(err => {
										resultsDiv.html('<span style="color:red">Error: ' + err + '</span>');
									});
								// Fetch views for the current source deployment
								const sourceDeployment = window.config && window.config.view ? window.config.view : view;
								if (sourceDeployment && email && apiKey) {
									fetch(`../services/support/views?sourceDeployment=${encodeURIComponent(sourceDeployment)}`, {
										method: 'POST',
										headers: { 'Content-Type': 'application/json' },
										body: JSON.stringify({ email: email, apiKey: apiKey })
									})
										.then(response => {
											if (!response.ok) {
												return response.json().then(err => Promise.reject(err));
											}
											return response.json();
										})
										.then(views => {
											console.log('Views fetched:', views);
											// Views are available for use if needed
										})
										.catch(err => {
											console.error('Error fetching views:', err);
										});
								}
							}
						},
						{
							text: t('close'),
							click: function() { $(this).dialog('close'); }
						}
					]
				});
				// Fetch users immediately using stored credentials
				const email = localStorage.getItem('userEmail');
				const apiKey = localStorage.getItem('authToken');
				if (!email || !apiKey) {
					resultsDiv.html('<span style="color:red">Missing credentials. Please log in again.</span>');
					return;
				}
				resultsDiv.html('Loading...');
				fetch('../services/support/listUsers', {
					method: 'POST',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ email: email, apiKey: apiKey })
				})
					.then(response => response.json())
					.then(data => {
						if (data.success && Array.isArray(data.users)) {
							if (data.users.length === 0) {
								resultsDiv.html('<span>' + t('no_users_found') + '</span>');
							} else {
								resultsDiv.html(renderTable(data.users));
								bindRowClicks(data.users);
							}
						} else {
							resultsDiv.html('<span style="color:red">' + (data.message || t('failed_fetch_users')) + '</span>');
						}
					})
					.catch(err => {
						resultsDiv.html('<span style="color:red">Error: ' + err + '</span>');
					});
				// Fetch views for the current source deployment
				const sourceDeployment = window.config && window.config.view ? window.config.view : view;
				if (sourceDeployment && email && apiKey) {
					fetch(`../services/support/views?sourceDeployment=${encodeURIComponent(sourceDeployment)}`, {
						method: 'POST',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify({ email: email, apiKey: apiKey })
					})
						.then(response => {
							if (!response.ok) {
								return response.json().then(err => Promise.reject(err));
							}
							return response.json();
						})
						.then(views => {
							console.log('Views fetched:', views);
							// Store views globally for use in allowedViews dialog
							availableViews = Array.isArray(views) ? views : [];
						})
						.catch(err => {
							console.error('Error fetching views:', err);
						});
				}
			});
		}

		// Logout menu button click handler
		document.getElementById('logoutMenuBtn').addEventListener('click', function() {
			localStorage.removeItem('authToken');
			localStorage.removeItem('userEmail');
			localStorage.removeItem('isAdmin');
			localStorage.removeItem('lang');
			loginBtn.style.display = 'inline-block';
			loginBtn.textContent = t('login');
			loginBtn.disabled = false;
			logoutBtn.style.display = 'none';
			userMenu.style.display = 'none';
			window.location.reload();
		});
	}
}

export function initializePortal(config) {
	window.config = config;
	view = config.view;
	var viewParam = getUrlParameter('view');
	if (viewParam!==undefined && viewParam!==null&&viewParam!=="") {
		view = viewParam;
	}
	token = config.token;
	document.title = config.title;

	

	// Initialize i18n (defaults to 'en', overridden by localStorage or config.language)
	try { loadI18nSync(); } catch (e) { }

	// Configure jQuery UI datepicker locale based on current language
	if (window.jQuery && window.jQuery.datepicker && typeof window.jQuery.datepicker.setDefaults === 'function') {
		if (lang() === 'it') {
			window.jQuery.datepicker.setDefaults({
				dateFormat: 'yy-mm-dd',
				monthNames: ['Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
				             'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'],
				monthNamesShort: ['Gen','Feb','Mar','Apr','Mag','Giu',
				                  'Lug','Ago','Set','Ott','Nov','Dic'],
				dayNames: ['Domenica','Lunedì','Martedì','Mercoledì','Giovedì','Venerdì','Sabato'],
				dayNamesMin: ['Do','Lu','Ma','Me','Gi','Ve','Sa'],
				firstDay: 1
			});
		} else {
			// Ensure a consistent default for other languages (English)
			window.jQuery.datepicker.setDefaults({
				dateFormat: 'yy-mm-dd',
				firstDay: 1
			});
		}
	}

	// Initialize login if enabled
	initializeLogin(config);

	var centerLat = config.centerLat;
	var centerLon = config.centerLon;
	var zoom = config.zoom;
	var minZoom = config.minZoom;

	$.extend(true, $.hik.jtable.prototype.options, {
		jqueryuiTheme: true
	});
	

	GIAPI.logger.enabled = true;

	GIAPI.search = {};

	// GIAPI.search.dab = GIAPI.DAB('http://localhost:9090/gs-service', view, 'services/essi','csw');
	// GIAPI.search.dab = GIAPI.DAB('https://gs-service-preproduction.geodab.eu/gs-service', view, 'services/essi','csw');
	GIAPI.search.dab = GIAPI.DAB('../', view, 'services/essi', 'csw');

	jQuery(function() {

		var seadatanetLogos = '<a style="display:inline-block" target=_blank href="https://www.seadatanet.org/"><img style="width: 56px;" src="http://api.geodab.eu/docs/assets/img/sdn_logo.png"></img></a><a style="display:inline-block" target=_blank href="http://www.iia.cnr.it/"><img style="vertical-align: super" src="http://api.geodab.eu/docs/assets/img/iia.png"></img></a><a style="display:inline-block" targ et=_blank href="http://www.uos-firenze.iia.cnr.it/"><img style="vertical-align: super" src="http://api.geodab.eu/docs/assets/img/essilab.png"></img></a>';

		var standardLogos = '<a style="display:inline-block" target=_blank href="http://api.geodab.eu/"><img style="margin-top:-3px;" src="http://api.geodab.eu/docs/assets/img/api-logo-small-2.png"></img></a><a style="display:inline-block" target=_blank href="http://www.eurogeoss.eu/"><img src="http://api.geodab.eu/docs/assets/img/eurogeoss-small.png"></img></a><a style="display:inline-block" target=_blank href="http://www.iia.cnr.it/"><img style="vertical-align: super" src="http://api.geodab.eu/docs/assets/img/iia.png"></img></a><a style="display:inline-block" targ et=_blank href="http://www.uos-firenze.iia.cnr.it/"><img style="vertical-align: super" src="http://api.geodab.eu/docs/assets/img/essilab.png"></img></a>';

		// Results tab now flows naturally in flexbox layout, no positioning needed
		function positionResultsTab() {
			// Remove any margins that might create white space
			jQuery('#results-tab').css('margin-top', '0px');
			jQuery('#results-tab').css('margin-bottom', '0px');
			jQuery('#results-tab').css('padding', '0px');
		}

		// init the tabs	        	
		jQuery('#tabs-div').tabs({
			activate: function(event, ui) {

				// hides/shows the paginator widget
				if (ui.newPanel.selector === '#results-tab') {

					jQuery('#paginator-widget').css('display', 'block');
					// Reposition results tab after paginator is shown and measured
					// Use multiple timeouts to ensure DOM is fully updated
					setTimeout(function() {
						positionResultsTab();
						// Try again after a brief delay to catch any layout changes
						setTimeout(positionResultsTab, 50);
					}, 10);

				} else {
					jQuery('#paginator-widget').css('display', 'none');
				}

				// refreshes the filters accordion (only if it's initialized)
				if (ui.newPanel.selector === '#filters-tab') {
					var $filtersTab = jQuery('#filters-tab');
					// Check if accordion is initialized before trying to refresh
					if ($filtersTab.hasClass('ui-accordion')) {
						$filtersTab.accordion('refresh');
					}
				}
			}
		});

		//------------------------------------------------------------------
		// header settings
		//
		jQuery('#headerDiv').css('padding', '10px');
		jQuery('#headerDiv').css('padding-top', '2px'); // Reduced padding-top to align buttons better
		jQuery('#headerDiv').css('padding-left', '10px');
		jQuery('#headerDiv').css('margin-left', '0px');
		jQuery('#headerDiv').css('height', '30px');
		jQuery('#headerDiv').css('position', 'relative'); // Ensure relative positioning for absolute children

		//------------------------------------------------------------------
		// Clear All Constraints function
		//
		function clearAllConstraints() {
			// Clear text constraints (what, from, to, and all advanced parameters)
			if (GIAPI.search.constWidget) {
				// Clear what constraint
				if (config.generalTermSearch == undefined || config.generalTermSearch) {
					jQuery('#' + GIAPI.search.constWidget.getId('what')).val('');
				}
				
				// Clear temporal constraints
				jQuery('#' + GIAPI.search.constWidget.getId('from')).val('');
				jQuery('#' + GIAPI.search.constWidget.getId('to')).val('');
				
				// Clear all advanced constraint fields
				var advancedKeys = [
					'instrumentTitle', 'attributeTitle', 'platformTitle', 'isValidated',
					'riverName', 'timeInterpolation', 'intendedObservationSpacing',
					'aggregationDuration'
				];
				advancedKeys.forEach(function(key) {
					var id = GIAPI.search.constWidget.getId(key);
					if (jQuery('#' + id).length) {
						jQuery('#' + id).val('');
					}
				});
				
				// Set semantic search to default: "Semantic search w/ translations and narrow matches" (value: 'true')
				var semanticSearchId = GIAPI.search.constWidget.getId('semanticSearch');
				if (jQuery('#' + semanticSearchId).length) {
					jQuery('#' + semanticSearchId).val('true');
				}
			}
			
			// Clear spatial constraints (map selection)
			if (GIAPI.search.resultsMapWidget) {
				var olMap = GIAPI.search.resultsMapWidget.olMap;
				if (olMap && olMap.selectionVisible) {
					olMap.selectionVisible(false);
				}
				// Clear input control fields (south, west, north, east, and location)
				var mapElement = GIAPI.search.resultsMapWidget.map ? 
					GIAPI.search.resultsMapWidget.map.getTargetElement() : null;
				if (mapElement) {
					var inputControl = jQuery(mapElement).find('.cnst-widget-where-input-control');
					if (inputControl.length) {
						// Clear all input fields in the input control (text, number, and any other types)
						inputControl.find('input').val('');
					}
				}
			}
			
			// Clear sources selection (select all sources)
			if (GIAPI.search.sourcesWidget) {
				// Use the "check all" checkbox to select all sources
				// This is the proper way as it handles both UI and source.include() calls
				var checkAll = jQuery('#check_all');
				if (checkAll.length) {
					if (!checkAll.is(':checked')) {
						// Trigger click on check_all, which will automatically check all individual sources
						// and call source.include(true) for each source
						checkAll.click();
					}
				} else {
					// Fallback: if check_all doesn't exist, find all individual source checkboxes
					// Source checkboxes have IDs like check_0, check_1, check_2, etc.
					var sourceIndex = 0;
					while (true) {
						var checkId = 'check_' + sourceIndex;
						var checkbox = jQuery('#' + checkId);
						if (checkbox.length) {
							if (!checkbox.is(':checked')) {
								checkbox.click();
							}
							sourceIndex++;
						} else {
							// No more checkboxes found
							break;
						}
					}
				}
			}
			
			// Clear filters (TermFrequency selections)
			if (GIAPI.search.tfWidget && GIAPI.search.tfWidget.tfObject) {
				var tfObject = GIAPI.search.tfWidget.tfObject;
				var targets = tfObject.targets();
				if (targets && targets.length > 0) {
					// Clear all checked items for each target
					targets.forEach(function(target) {
						tfObject.clearCheckedItems(target);
					});
					// Clear all jtable selections
					jQuery('#filters-tab table.jtable').each(function() {
						var tableId = jQuery(this).attr('id');
						if (tableId) {
							jQuery('#' + tableId).jtable('unselectAllRows');
						}
					});
				}
			}
			
			// Trigger a new search with cleared constraints
			setTimeout(function() {
				GIAPI.search.discover();
			}, 100);
		}

		//------------------------------------------------------------------
		// Add Clear All button to header
		//
		// Helper function to get translation with fallback
		function getTranslation(key, fallback) {
			var translation = t(key);
			// If translation returns the key itself, it means translation wasn't found in either current language or English
			// In that case, use the fallback
			if (translation === key) {
				return fallback;
			}
			return translation;
		}
		
		const clearAllButton = document.createElement('button');
		clearAllButton.id = 'clearAllButton';
		clearAllButton.className = 'login-button';
		// Use t() directly - it will use current language, fallback to English, or return key
		// The t() function checks i18n[i18n.current][key] first, then i18n.en[key], then returns key
		var buttonText = t('clear_all');
		// If we got the key back, it means translation wasn't found in current language or English
		if (buttonText === 'clear_all') {
			buttonText = 'Clear All'; // Fallback to English
		}
		// Add icon and text
		clearAllButton.innerHTML = '<i class="fa fa-times" style="margin-right: 5px;"></i>' + buttonText;
		// Remove custom inline styles - use login-button class styling instead
		var buttonTitle = t('clear_all_tooltip');
		if (buttonTitle === 'clear_all_tooltip') {
			buttonTitle = 'Clear all search constraints'; // Fallback to English
		}
		clearAllButton.title = buttonTitle;
		clearAllButton.addEventListener('click', function(e) {
			e.preventDefault();
			
			// Create a better confirmation dialog
			const confirmDialog = $('<div>').css({
				'padding': '15px',
				'min-width': '300px'
			});
			
			// Add icon and message
			const messageDiv = $('<div>').css({
				'display': 'flex',
				'align-items': 'flex-start',
				'margin-bottom': '15px'
			});
			
			const iconDiv = $('<div>').html('<i class="fa fa-exclamation-triangle" style="font-size: 24px; color: #e74c3c; margin-right: 12px; margin-top: 2px;"></i>');
			messageDiv.append(iconDiv);
			
			const textDiv = $('<div>').css({
				'flex': '1',
				'line-height': '1.5'
			});
			var confirmText = t('confirm_clear_all');
			var detailsText = t('confirm_clear_all_details');
			var titleText = t('clear_all_title');
			var clearButtonText = t('clear_all');
			var cancelButtonText = t('cancel');
			
			textDiv.append($('<p>').text((confirmText === 'confirm_clear_all') ? 'Are you sure you want to clear all constraints?' : confirmText).css({
				'margin': '0 0 8px 0',
				'font-size': '14px',
				'color': '#2c3e50'
			}));
			textDiv.append($('<p>').text((detailsText === 'confirm_clear_all_details') ? 'This will reset all search parameters including text, dates, spatial extent, sources, and filters.' : detailsText).css({
				'margin': '0',
				'font-size': '12px',
				'color': '#7f8c8d',
				'font-style': 'italic'
			}));
			messageDiv.append(textDiv);
			
			confirmDialog.append(messageDiv);
			
			// Show dialog
			confirmDialog.dialog({
				title: (titleText === 'clear_all_title') ? 'Clear All Constraints' : titleText,
				modal: true,
				width: 450,
				resizable: false,
				classes: {
					"ui-dialog": "clear-all-dialog"
				},
				buttons: [
					{
						text: (clearButtonText === 'clear_all') ? 'Clear All' : clearButtonText,
						class: 'login-button',
						click: function() {
							$(this).dialog('close');
							clearAllConstraints();
						}
					},
					{
						text: (cancelButtonText === 'cancel') ? 'Cancel' : cancelButtonText,
						click: function() {
							$(this).dialog('close');
						}
					}
				],
				close: function() {
					$(this).dialog('destroy').remove();
				}
			});
		});
		
		// Store the button reference - will be appended after advanced search setup
		// Store it in a variable accessible to the advanced search setup code
		window._clearAllButton = clearAllButton;

		//------------------------------------------------------------------
		// portal header row with logos and titles (if configured)
		//
		if (config['logo-left'] || config['logo-right'] || config['title-left'] || config['title-right']) {
			const headerDiv = document.getElementById('headerDiv');
			if (headerDiv && !document.getElementById('portalHeaderRow')) {
				const portalHeaderRow = document.createElement('div');
				portalHeaderRow.id = 'portalHeaderRow';
				portalHeaderRow.className = 'portal-header-row';

				// Left section: logo-left + title-left
				const leftSection = document.createElement('div');
				leftSection.className = 'portal-header-left';
				leftSection.style.padding = '0';
				leftSection.style.margin = '0';
				
				if (config['logo-left']) {
					// Support multiple comma-separated logos in "logo-left"
					const leftLogos = String(config['logo-left'])
						.split(',')
						.map(function(item) { return item.trim(); })
						.filter(function(item) { return item.length > 0; });

					if (leftLogos.length > 0) {
						// Function to recalculate position
						const recalculatePosition = function() {
							setTimeout(function() {
								if (window.positionTabsCallback) {
									window.positionTabsCallback();
								}
							}, 10);
						};

						leftLogos.forEach(function(logoSrc) {
							const leftLogoImg = document.createElement('img');
							leftLogoImg.className = 'portal-header-logo-left';
							leftLogoImg.alt = 'Left logo';
							leftLogoImg.style.padding = '0';
							leftLogoImg.style.margin = '0';

							// Use path as-is if relative, or full URL if absolute
							const leftLogoSrc = logoSrc.startsWith('http') ? logoSrc : logoSrc;

							// Add load listener to recalculate position when image loads
							leftLogoImg.addEventListener('load', recalculatePosition);
							// Also handle errors (image fails to load)
							leftLogoImg.addEventListener('error', recalculatePosition);

							// Add click handler if href is provided
							if (config['logo-left-href']) {
								leftLogoImg.style.cursor = 'pointer';
								leftLogoImg.addEventListener('click', function() {
									window.open(config['logo-left-href'], '_blank', 'noopener,noreferrer');
								});
							}

							leftLogoImg.src = leftLogoSrc;
							leftSection.appendChild(leftLogoImg);

							// If image is already loaded (cached), trigger recalculation
							if (leftLogoImg.complete) {
								recalculatePosition();
							}
						});
					}
				}
				
				if (config['title-left']) {
					const leftTitle = document.createElement('div');
					leftTitle.className = 'portal-header-title-left';
					leftTitle.textContent = config['title-left'];
					leftSection.appendChild(leftTitle);
				}

				// Right section: logo-right + title-right
				const rightSection = document.createElement('div');
				rightSection.className = 'portal-header-right';
				rightSection.style.padding = '0';
				rightSection.style.margin = '0';
				
				if (config['logo-right']) {
					// Support multiple comma-separated logos in "logo-right"
					const rightLogos = String(config['logo-right'])
						.split(',')
						.map(function(item) { return item.trim(); })
						.filter(function(item) { return item.length > 0; });

					if (rightLogos.length > 0) {
						// Function to recalculate position
						const recalculatePosition = function() {
							setTimeout(function() {
								if (window.positionTabsCallback) {
									window.positionTabsCallback();
								}
							}, 10);
						};

						rightLogos.forEach(function(logoSrc) {
							const rightLogoImg = document.createElement('img');
							rightLogoImg.className = 'portal-header-logo-right';
							rightLogoImg.alt = 'Right logo';
							rightLogoImg.style.padding = '0';
							rightLogoImg.style.margin = '0';

							// Use path as-is if relative, or full URL if absolute
							const rightLogoSrc = logoSrc.startsWith('http') ? logoSrc : logoSrc;

							// Add load listener to recalculate position when image loads
							rightLogoImg.addEventListener('load', recalculatePosition);
							// Also handle errors (image fails to load)
							rightLogoImg.addEventListener('error', recalculatePosition);

							// Add click handler if href is provided
							if (config['logo-right-href']) {
								rightLogoImg.style.cursor = 'pointer';
								rightLogoImg.addEventListener('click', function() {
									window.open(config['logo-right-href'], '_blank', 'noopener,noreferrer');
								});
							}

							rightLogoImg.src = rightLogoSrc;
							rightSection.appendChild(rightLogoImg);

							// If image is already loaded (cached), trigger recalculation
							if (rightLogoImg.complete) {
								recalculatePosition();
							}
						});
					}
				}
				
				if (config['title-right']) {
					const rightTitle = document.createElement('div');
					rightTitle.className = 'portal-header-title-right';
					rightTitle.textContent = config['title-right'];
					rightSection.appendChild(rightTitle);
				}

				portalHeaderRow.appendChild(leftSection);
				portalHeaderRow.appendChild(rightSection);

				// Insert before headerDiv
				headerDiv.parentNode.insertBefore(portalHeaderRow, headerDiv);
			}
		}

		if (config['top-logo']) {
			const headerDiv = document.getElementById('headerDiv');
			if (headerDiv && !document.getElementById('portalTopLogo')) {
				headerDiv.style.display = 'flex';
				headerDiv.style.alignItems = 'center';

				const logoLink = document.createElement('a');
				logoLink.id = 'portalTopLogo';
				logoLink.className = 'portal-top-logo-link';
				logoLink.href = config['top-logo-href'] || '#';
				logoLink.target = '_blank';
				logoLink.rel = 'noopener noreferrer';
				logoLink.style.display = 'flex';
				logoLink.style.alignItems = 'center';
				logoLink.style.marginLeft = '15px';

				const logoImg = document.createElement('img');
				logoImg.alt = config.title ? `${config.title} logo` : 'Portal logo';
				logoImg.style.height = '26px';
				logoImg.style.display = 'block';
				const logoSrc = config['top-logo'].startsWith('http') ? config['top-logo'] : `../gi-portal/${config['top-logo']}`;
				logoImg.src = logoSrc;

				logoLink.appendChild(logoImg);
				headerDiv.appendChild(logoLink);
			}
		}

		//------------------------------------------------------------------
		// logo div settings
		//
		jQuery('#logoDiv').css('margin-top', '-2px');
		jQuery('#logoDiv').css('margin-left', '50px');

		var baseWidth = 600;

		//------------------------------------------------------------------
		// search button
		// Note: Styling is now applied after the button is created (see below)

		//------------------------------------------------------------------
		// adv search button
		// Note: Styling is now applied after the button is created (see advancedSearch setup)

		//------------------------------------------------------------------
		// tabs
		//
		// Let tabs-ul size dynamically based on content
		jQuery('#tabs-ul').css('width', 'auto');
		jQuery('#tabs-ul').css('height', '40px');
		jQuery('#tabs-ul').css('margin-left', '0px');
		jQuery('#tabs-ul').css('white-space', 'nowrap');

		// Tabs and paginator now flow naturally in the document, no positioning needed
		jQuery('#tabs-div').css('padding', '0px');
		// Ensure tab panels are properly contained (filters-tab will override overflow-y)
		jQuery('#tabs-div .tabs-element').css({
			'position': 'relative'
		});
		// Ensure results-tab and sources-tab don't overflow
		jQuery('#results-tab, #sources-tab').css('overflow', 'hidden');

		//------------------------------------------------------------------
		// results tab
		//
		// Let results-tab take full width of its container
		jQuery('#results-tab').css('width', '100%');
		jQuery('#results-tab').css('margin-left', '0px');
		jQuery('#results-tab-link').text(t("results_tab"));

		// Remove margin-left from results tab to center tabs
		jQuery('li[aria-controls="results-tab"').css('margin-left', '0px');
		// Also remove inline margin from the link
		jQuery('#results-tab-link').css('margin-left', '0px');

		// Let results layout size dynamically
		var css = 'width: 100%;';
		GIAPI.UI_Utils.appendStyle('.resultset-layout-ul {' + css + '}');

		// Let paginator take full width of its container
		jQuery('#paginator-widget').css('width', '100%');
		jQuery('#paginator-widget').css('height', '55px');
		jQuery('#paginator-widget').css('padding', '0px');
		
		// Paginator now flows naturally in the document, no positioning needed
		// Position results tab after a short delay to ensure layout is complete
		setTimeout(function() {
			positionResultsTab();
		}, 100);
		
		// Reposition results tab on window resize
		let resizeTimeout;
		jQuery(window).on('resize', function() {
			clearTimeout(resizeTimeout);
			resizeTimeout = setTimeout(function() {
				positionResultsTab();
			}, 100);
		});

		css = 'width: 290px;';
		css += 'margin-left:640px;';
		css += 'margin-top:-99px;';
		GIAPI.UI_Utils.appendStyle('.series-browser-widget-table {' + css + '}');

		//------------------------------------------------------------------
		// sources tab
		//
		jQuery('#sources-tab').css('width', '100%');
		jQuery('#sources-tab').css('margin-top', '3px');
		jQuery('#sources-tab').css('margin-left', '2px');
		jQuery('#sources-tab-link').text(t("sources_tab"));

		jQuery('.sources-widget').css('padding-right', '10px');
		jQuery('.sources-widget').css('padding-left', '10px');
		jQuery('.sources-widget').css('padding-top', '0px');

		//------------------------------------------------------------------
		// filters tab     
		//
		jQuery('#filters-tab').css('width', '100%');
		jQuery('#filters-tab').css('margin-top', '3px');
		jQuery('#filters-tab').css('overflow-y', 'auto');
		jQuery('#filters-tab').css('overflow-x', 'hidden');
		jQuery('#filters-tab-link').text(t("filters_tab"));

		//------------------------------------------------------------------
		// browse tab     
		//
		jQuery('#browse-tab').css('width', '100%');
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
			'height': '100%',
			'markerTitle': function(node) {

				return node.report().title;
			},

			'mapType': 'ol',

			'showSelectionControl': true,

			//                	'onMarkerClick': function(node) {
			//             		},

			'dabNode': GIAPI.search.dab,


			'wmsEndpoint': config.wmsEndpoint,


			'clusterWMS': (config.clusterWMS !== undefined),
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

		// Create a wrapper div for the main content area (left sidebar + map)
		var contentWrapper = jQuery('<div id="main-content-wrapper"></div>');
		contentWrapper.css({
			'display': 'flex',
			'flex-direction': 'row',
			'flex': '1',
			'min-height': '0',
			'width': '100%'
		});
		
		// Create left sidebar container
		var leftSidebar = jQuery('<div id="left-sidebar"></div>');
		leftSidebar.css({
			'display': 'flex',
			'flex-direction': 'column',
			'flex-shrink': '0',
			'width': '40%',
			'min-width': '0',
			'max-width': 'none',
			'overflow': 'hidden'
		});
		
		// Move tabs and paginator into left sidebar (in order: tabs, paginator)
		// Keep results-tab inside tabs-div so only the active tab panel is visible (results, filters, or sources)
		var tabs = jQuery('#tabs-div');
		var paginator = jQuery('#paginator-widget');
		var map = jQuery('#resMapWidget');
		var stationInfo = jQuery('#stationInfo');
		
		// Add tabs first (at the top) - contains results-tab, filters-tab, sources-tab panels
		if (tabs.length) {
			tabs.css({
				'flex-shrink': '0',
				'flex': '1',
				'min-height': '0',
				'width': '100%',
				'display': 'flex',
				'flex-direction': 'column',
				'overflow': 'hidden'
			});
			leftSidebar.append(tabs);
		}
		
		// Add paginator second (below tabs)
		if (paginator.length) {
			paginator.css({
				'flex-shrink': '0',
				'width': '100%'
			});
			leftSidebar.append(paginator);
		}
		
		// Add left sidebar to wrapper
		contentWrapper.append(leftSidebar);
		
		// Add map to wrapper (takes remaining space on the right)
		if (map.length) {
			map.css({
				'flex': '1',
				'min-width': '0',
				'pointer-events': 'auto',
				'position': 'relative',
				'z-index': '0'
			});
			contentWrapper.append(map);

			// If a beta-message is configured, add a small beta badge over the map
			// Check for beta-message and beta-title in translation keys first, then fall back to config
			var betaMessage = (t('beta-message') !== 'beta-message' ? t('beta-message') : null) || config['beta-message'];
			var betaTitle = (t('beta-title') !== 'beta-title' ? t('beta-title') : null) || config['beta-title'] || 'beta';
			
			if (betaMessage) {
				var betaBadge = jQuery(
					'<div class="beta-badge">' +
						'<span>' + betaTitle + '</span>' +
						'<i class="fa fa-info-circle" aria-hidden="true"></i>' +
					'</div>'
				);

				var tooltipVisible = false;
				var tooltip;

				var toggleTooltip = function() {
					if (tooltipVisible) {
						if (tooltip) {
							tooltip.remove();
							tooltip = null;
						}
						tooltipVisible = false;
					} else {
						tooltip = jQuery('<div class="beta-badge-tooltip"></div>').text(betaMessage);
						betaBadge.append(tooltip);
						tooltipVisible = true;
					}
				};

				betaBadge.on('click', function(event) {
					event.stopPropagation();
					toggleTooltip();
				});

				// Close tooltip when clicking anywhere else on the document
				jQuery(document).on('click', function() {
					if (tooltipVisible) {
						toggleTooltip();
					}
				});

				map.append(betaBadge);
			}
		}
		
		// Add station info if needed
		if (stationInfo.length) {
			contentWrapper.append(stationInfo);
		}
		
		// Insert wrapper after header
		jQuery('#headerDiv').after(contentWrapper);
		
		// Add window resize listener to update map size
		var mapResizeTimeout;
		var updateMapSize = function() {
			if (GIAPI.search.resultsMapWidget && GIAPI.search.resultsMapWidget.map) {
				// Small delay to ensure DOM has updated
				setTimeout(function() {
					GIAPI.search.resultsMapWidget.map.updateSize();
				}, 50);
			}
		};
		
		jQuery(window).on('resize', function() {
			clearTimeout(mapResizeTimeout);
			mapResizeTimeout = setTimeout(updateMapSize, 100);
		});
		
		// Update map size after initial render
		setTimeout(updateMapSize, 200);
		
		// Ensure map is interactive after DOM manipulation
		// OpenLayers may need the map container to be re-initialized after DOM moves
		setTimeout(function() {
			if (map.length && GIAPI.search.resultsMapWidget && GIAPI.search.resultsMapWidget.map) {
				// Force pointer events on map container
				map.css({
					'pointer-events': 'auto',
					'z-index': '0'
				});
				
				// Critical: The overlay container should NOT block events
				var overlayContainer = map.find('.ol-overlaycontainer-stopevent');
				if (overlayContainer.length) {
					overlayContainer.css('pointer-events', 'none');
				}
				
				// The viewport and canvas MUST be interactive
				var viewport = map.find('.ol-viewport');
				if (viewport.length) {
					viewport.css('pointer-events', 'auto');
					// Find canvas inside viewport
					var canvas = viewport.find('canvas');
					if (canvas.length) {
						canvas.css('pointer-events', 'auto');
					}
				}
				
				// Also check for canvas directly in map
				var allCanvases = map.find('canvas');
				allCanvases.css('pointer-events', 'auto');
				
				var mapWidgetDiv = map.find('.map-widget-div');
				if (mapWidgetDiv.length) {
					mapWidgetDiv.css('pointer-events', 'auto');
				}
				
				// Update map size again to ensure it's properly sized
				GIAPI.search.resultsMapWidget.map.updateSize();
				
				// Force a repaint
				GIAPI.search.resultsMapWidget.map.render();
			}
		}, 400);

		//------------------------------------
		// search button
		//
		var searchButton = GIAPI.FontAwesomeButton({
			'width': 120, // Increased width to prevent wrapping
			'label': t('search'),
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
		
		// Style the search button to match login-button style
		// Apply login-button styling to the search button container
		jQuery('#search-button .font-awesome-button').css({
			'background-color': '#2c3e50',
			'color': 'white',
			'border': 'none',
			'border-radius': '4px',
			'padding': '6px 10px', // Reduced padding to match advanced button
			'cursor': 'pointer',
			'font-size': '14px',
			'transition': 'background-color 0.3s',
			'box-shadow': 'none', // Remove default hover shadow
			'line-height': '1.4', // Consistent line height
			'height': '32px', // Match other buttons exactly
			'box-sizing': 'border-box',
			'white-space': 'nowrap', // Prevent text wrapping
			'display': 'flex', // Use flexbox for proper alignment
			'align-items': 'center', // Center items vertically
			'justify-content': 'center', // Center items horizontally
			'gap': '4px' // Reduced gap to match advanced button
		});
		
		// Ensure icon and label stay inline
		jQuery('#search-button .font-awesome-button-icon').css({
			'display': 'inline-block',
			'vertical-align': 'middle',
			'flex-shrink': '0' // Prevent icon from shrinking
		});
		
		jQuery('#search-button .font-awesome-button-label').css({
			'display': 'inline-block',
			'vertical-align': 'middle',
			'white-space': 'nowrap' // Prevent label text from wrapping
		});
		
		// Add hover effect to match login-button
		jQuery('#search-button .font-awesome-button').hover(
			function() {
				jQuery(this).css('background-color', '#34495e');
			},
			function() {
				jQuery(this).css('background-color', '#2c3e50');
			}
		);
		
		// Align search button container properly
		// Use a small negative margin to move up and align with login buttons
		jQuery('#search-button').css({
			'margin-left': '-3px',
			'margin-top': '0px', // Start with 0, will be adjusted
			'vertical-align': 'top',
			'display': 'inline-block',
			'line-height': 'normal'
		});
		
		// Fine-tune alignment after a short delay to ensure DOM is ready
		setTimeout(function() {
			var searchBtn = jQuery('#search-button');
			var loginBtn = jQuery('.login-container');
			if (searchBtn.length && loginBtn.length) {
				var searchTop = searchBtn.position().top;
				var loginTop = loginBtn.position().top;
				var offset = loginTop - searchTop;
				if (Math.abs(offset) > 1) {
					searchBtn.css('margin-top', offset + 'px');
				}
			}
		}, 200);

		//------------------------------------------------------------------
		// hide results button
		//           	
		// Check config for initial visibility state
		var initialResultsVisible = config.resultsVisibility !== undefined ? config.resultsVisibility : true;
		var hideResultsButton = GIAPI.ButtonsFactory.onOffSwitchButton(t('show_results'), t('hide_results'), {
			'id': 'hideResultsButton',
			'checked': !initialResultsVisible,
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

		function updateResultsVisibility() {
			if (jQuery('#hideResultsButton').is(":checked")) {
				// Hide the entire left sidebar (tabs + paginator + results)
				jQuery('#left-sidebar').css('display', 'none');
				// Let the map reclaim space
				if (GIAPI.search.resultsMapWidget && GIAPI.search.resultsMapWidget.map) {
					setTimeout(function() {
						GIAPI.search.resultsMapWidget.map.updateSize();
					}, 50);
				}
			} else {
				// Show the entire left sidebar
				jQuery('#left-sidebar').css('display', 'flex');
				// Ensure map resizes after layout change
				if (GIAPI.search.resultsMapWidget && GIAPI.search.resultsMapWidget.map) {
					setTimeout(function() {
						GIAPI.search.resultsMapWidget.map.updateSize();
					}, 50);
				}
			}
		}
		
		jQuery(document).on('change', '#hideResultsButton', updateResultsVisibility);
		// Also listen to click as backup in case change event doesn't fire
		jQuery(document).on('click', '#hideResultsButton', function() {
			setTimeout(updateResultsVisibility, 10);
		});






		//------------------------------------
		// ConstraintsWidget
		//
		GIAPI.search.constWidget = GIAPI.ConstraintsWidget(GIAPI.search.dab, {
			'ontology': config.ontology,
			'ontologyBrowserUrl': config.ontologyBrowserUrl,
			'ontologyTreeUrl': config.ontologyTreeUrl,
			'conceptBaseUrl': config.conceptBaseUrl,
			'ontologyLabel': config.ontologyLabel,
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
		jQuery('#mapControlDiv').css('background-color', '#c0c0c0'); // Match headerDiv background

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

		var hideMapInputControlButton = GIAPI.ButtonsFactory.onOffSwitchButton(t('spatial_panel_show'), t('spatial_panel_hide'), {
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
					{ label: t("select_interpolation_type"), value: '' },
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
			const authToken = localStorage.getItem('authToken') || undefined;
			
			if (authToken !== undefined) {
				fetch(`../services/essi/token/${authToken}/view/${view}/om-api/properties?property=timeInterpolation&limit=50`)
					.then(response => response.json())
					.then(data => {
						if (data.timeInterpolation && data.timeInterpolation.length > 0) {
							// Find the select element using the correct ID
							const selectElement = document.getElementById(timeInterpolationId);
							if (selectElement) {
								const options = [
									{ label: t("select_interpolation_type"), value: '' },
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
		}

		if (config.intendedObservationSpacing !== undefined && config.intendedObservationSpacing) {
			const spacingId = GIAPI.search.constWidget.getId('intendedObservationSpacing');
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'intendedObservationSpacing', {
				helpIconImage: 'fa-arrows-h',
				values: [
					{ label: t("select_observation_spacing"), value: '' }
				],
				readOnlyValues: true
			}));

		// After constraints are initialized, try to fetch and update values
		const authToken = localStorage.getItem('authToken') || config.token;
		
		if (authToken !== undefined) {
			fetch(`../services/essi/token/${authToken}/view/${view}/om-api/properties?property=intendedObservationSpacing&limit=50`)
				.then(response => response.json())
				.then(data => {
					if (data.intendedObservationSpacing && data.intendedObservationSpacing.length > 0) {
						// Find the select element using the correct ID
						const selectElement = document.getElementById(spacingId);
						if (selectElement) {
							// Sort durations by value (largest first), keeping non-ISO8601 values at the end
							const sortedTypes = [...data.intendedObservationSpacing].sort((a, b) => {
								const aValue = getISO8601DurationSortValue(a.value);
								const bValue = getISO8601DurationSortValue(b.value);
								// If both are ISO8601, sort by value (descending - largest first)
								if (aValue > 0 && bValue > 0) {
									return bValue - aValue;
								}
								// If only one is ISO8601, put ISO8601 first
								if (aValue > 0) return -1;
								if (bValue > 0) return 1;
								// If neither is ISO8601, maintain original order
								return 0;
							});

							const options = [
								{ label: t("select_observation_spacing"), value: '' },
								...sortedTypes.map(type => {
									const displayValue = formatISO8601Duration(type.value);
									return {
										label: `${displayValue} (${type.observationCount} observations)`,
										value: type.value
									};
								})
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
		}
		
		if (config.aggregationDuration !== undefined && config.aggregationDuration) {
			const durationId = GIAPI.search.constWidget.getId('aggregationDuration');
			advancedConstraints.push(GIAPI.search.constWidget.textConstraint('get', 'aggregationDuration', {
				helpIconImage: 'fa-hourglass',
				values: [
					{ label: t("select_aggregation_duration"), value: '' }
				],
				readOnlyValues: true
			}));

		// After constraints are initialized, try to fetch and update values
		const authToken = localStorage.getItem('authToken') || config.token;
		
		if (authToken !== undefined) {
			fetch(`../services/essi/token/${authToken}/view/${view}/om-api/properties?property=aggregationDuration&limit=50`)
				.then(response => response.json())
				.then(data => {
					if (data.aggregationDuration && data.aggregationDuration.length > 0) {
						// Find the select element using the correct ID
						const selectElement = document.getElementById(durationId);
						if (selectElement) {
							// Sort durations by value (largest first), keeping non-ISO8601 values at the end
							const sortedTypes = [...data.aggregationDuration].sort((a, b) => {
								const aValue = getISO8601DurationSortValue(a.value);
								const bValue = getISO8601DurationSortValue(b.value);
								// If both are ISO8601, sort by value (descending - largest first)
								if (aValue > 0 && bValue > 0) {
									return bValue - aValue;
								}
								// If only one is ISO8601, put ISO8601 first
								if (aValue > 0) return -1;
								if (bValue > 0) return 1;
								// If neither is ISO8601, maintain original order
								return 0;
							});

							const options = [
								{ label: t("select_aggregation_duration"), value: '' },
								...sortedTypes.map(type => {
									const displayValue = formatISO8601Duration(type.value);
									return {
										label: `${displayValue} (${type.observationCount} observations)`,
										value: type.value
									};
								})
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
		}
		
		
		var semanticValue = 0;
		if (config.semanticSearchValue !== undefined) {
			semanticValue = config.semanticSearchValue;
		}

		if (config.semanticSearch !== undefined && config.semanticSearch) {
			advancedConstraints.push(GIAPI.search.constWidget.booleanConstraint('get', 'semanticSearch', { ontology: config.ontology, value: semanticValue, helpIconImage: 'fa-comments' }));
		}
		
		if (config.semanticOptions !== undefined && config.semanticOptions) {
		 			 
			var searchModeButton = GIAPI.FontAwesomeButton({
				'width': 250,
				'label': 'Search mode',
				'icon': ' fa-search',
				'handler': function() {

				 	GIAPI.OntologiesSelector.showDialog();
							 
				}
			});
			
			searchModeButton.css('div','margin-top','5px');
			
			jQuery("#adv-search-div").append(searchModeButton.div());	
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

			// Fix positioning to ensure panel opens downward and stays on screen
			setTimeout(function() {
				var $advSearchDiv = $('#adv-search-div');
				var $advConstDiv = $('#advConstDiv');
				
				// Ensure parent has relative positioning
				$advSearchDiv.css('position', 'relative');
				
				// Override the relative positioning set by advancedSearch function
				$advConstDiv.css({
					'position': 'absolute',
					'top': '100%',
					'left': '0',
					'margin-top': '5px',
					'z-index': '1000'
				});
				
				// Style the advanced search button to match login-button style
				var $advButton = $advSearchDiv.find('.font-awesome-button');
				if ($advButton.length) {
					$advButton.css({
						'background-color': '#2c3e50',
						'color': 'white',
						'border': 'none',
						'border-radius': '4px',
						'padding': '6px 10px', // Reduced padding to make button more compact
						'cursor': 'pointer',
						'font-size': '14px',
						'transition': 'background-color 0.3s',
						'box-shadow': 'none', // Remove default hover shadow
						'line-height': '1.4', // Consistent line height
						'height': 'auto', // Let height be determined by content
						'min-height': '32px', // Match login button height
						'width': 'auto', // Override the fixed 250px width to fit content
						'min-width': 'auto', // Remove any min-width constraint
						'box-sizing': 'border-box',
						'vertical-align': 'middle',
						'margin': '0',
						'white-space': 'nowrap', // Prevent text wrapping
						'display': 'flex', // Use flexbox for proper alignment
						'align-items': 'center', // Center items vertically
						'justify-content': 'center', // Center items horizontally
						'gap': '4px' // Reduced gap between icon and label
					});
					
					// Ensure icon and label stay inline
					$advButton.find('.font-awesome-button-icon').css({
						'display': 'inline-block',
						'vertical-align': 'middle',
						'flex-shrink': '0' // Prevent icon from shrinking
					});
					
					$advButton.find('.font-awesome-button-label').css({
						'display': 'inline-block',
						'vertical-align': 'middle',
						'white-space': 'nowrap' // Prevent label text from wrapping
					});
					
					// Add hover effect to match login-button
					$advButton.off('mouseenter mouseleave').on('mouseenter', function() {
						jQuery(this).css('background-color', '#34495e');
					}).on('mouseleave', function() {
						jQuery(this).css('background-color', '#2c3e50');
					});
				}
				
				// Align advanced search div properly to match other buttons
				$advSearchDiv.css({
					'margin-top': '0px', // Will be adjusted to match search button
					'vertical-align': 'top',
					'display': 'inline-block',
					'line-height': 'normal'
				});
				
				// Fine-tune alignment to match search button
				setTimeout(function() {
					var searchBtn = jQuery('#search-button');
					if (searchBtn.length) {
						var searchTop = searchBtn.position().top;
						var advTop = $advSearchDiv.position().top;
						var offset = searchTop - advTop;
						if (Math.abs(offset) > 1) {
							$advSearchDiv.css('margin-top', (parseFloat($advSearchDiv.css('margin-top')) || 0) + offset + 'px');
						}
					}
				}, 250);
				
				// Also ensure the table inside (which contains the button) is aligned
				$advSearchDiv.find('table').css({
					'vertical-align': 'middle',
					'margin': '0',
					'border-spacing': '0',
					'border-collapse': 'collapse'
				});
				
				// Align the table cells and the button inside
				$advSearchDiv.find('table td').css({
					'vertical-align': 'middle',
					'padding': '0'
				});
				
				// Ensure the button inside the table cell is also aligned
				$advButton.css({
					'vertical-align': 'middle',
					'margin': '0'
				});
				
				// Append Clear All button after the advanced search div
				var clearAllBtn = window._clearAllButton || document.getElementById('clearAllButton');
				if (clearAllBtn) {
					// Check if button is already in DOM, if not append it
					if (!clearAllBtn.parentNode) {
						$advSearchDiv.after(clearAllBtn);
					}
					// Style the button to match search and advanced buttons
					jQuery(clearAllBtn).css({
						'margin-left': '10px',
						'margin-right': '8px',
						'margin-top': '0px', // Will be adjusted to match search button
						'vertical-align': 'top',
						'display': 'inline-block',
						'line-height': '1.4', // Consistent line height
						'height': '32px', // Match button height exactly
						'box-sizing': 'border-box',
						'padding': '8px 15px', // Match search button padding
						'white-space': 'nowrap' // Prevent text wrapping
					});
					
					// Fine-tune alignment to match search button
					setTimeout(function() {
						var searchBtn = jQuery('#search-button');
						if (searchBtn.length && clearAllBtn) {
							var searchTop = searchBtn.position().top;
							var clearTop = jQuery(clearAllBtn).position().top;
							var offset = searchTop - clearTop;
							if (Math.abs(offset) > 1) {
								jQuery(clearAllBtn).css('margin-top', (parseFloat(jQuery(clearAllBtn).css('margin-top')) || 0) + offset + 'px');
							}
						}
					}, 300);
				}
			}, 100); // Increased timeout to ensure everything is ready
		} else {
			// If no advanced constraints, append after search button
			setTimeout(function() {
				var clearAllBtn = window._clearAllButton || document.getElementById('clearAllButton');
				if (clearAllBtn && !clearAllBtn.parentNode) {
					jQuery('#search-button').after(clearAllBtn);
					jQuery(clearAllBtn).css({
						'margin-left': '10px',
						'margin-right': '8px',
						'margin-top': '2px', // Match search button's margin-top
						'vertical-align': 'middle',
						'display': 'inline-block',
						'line-height': '1.4',
						'height': '32px', // Match button height exactly
						'box-sizing': 'border-box',
						'padding': '8px 15px', // Match search button padding
						'white-space': 'nowrap' // Prevent text wrapping
					});
				}
			}, 100);
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

				// Check if user is logged in and has 'downloads' permission
				var authToken = localStorage.getItem('authToken');
				var userPermissions = (localStorage.getItem('userPermissions') || '').split(',').map(p => p.trim()).filter(Boolean);
				if (authToken && userPermissions.includes('downloads')) {
					// Remove any existing download button
					$('#paginator-widget-top-label .login-button').remove();

					// Add bulk download button next to results count
					var downloadButton = $('<button>')
						.addClass('login-button')
						.text(t('bulk_data_download'))
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
							dialogContent.append($('<p>').text(t('bulk_download_initiate_message', { size: resultSet.size })));
							dialogContent.append($('<p>').text(t('bulk_download_monitor_message')));

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
									.text(t('download_name_label'))
									.css({
										'display': 'block',
										'margin-bottom': '10px',
										'font-weight': 'bold',
										'color': '#2c3e50'
									})
							);

							const defaultName = new Date().toISOString().slice(0, 16).replace('T', '_').replace(':', '-');

							// Remove any existing download name input
							$('#downloadName').remove();

							nameDiv.append(
								$('<input>')
									.attr({
										'type': 'text',
										'id': 'downloadName',
										'placeholder': t('download_name_placeholder'),
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

							// Define format options before using them in rows
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
							const waterml10Option = $('<div>').css({
								'display': 'flex',
								'align-items': 'center'
							});
							waterml10Option.append(
								$('<input>').attr({
									'type': 'radio',
									'name': 'downloadFormat',
									'id': 'formatWaterML10',
									'value': 'WATERML_1_0'
								}).css('margin-right', '8px')
							);
							waterml10Option.append(
								$('<label>')
									.attr('for', 'formatWaterML10')
									.text('WaterML 1.0')
									.css('color', '#2c3e50')
							);
							const waterml20Option = $('<div>').css({
								'display': 'flex',
								'align-items': 'center'
							});
							waterml20Option.append(
								$('<input>').attr({
									'type': 'radio',
									'name': 'downloadFormat',
									'id': 'formatWaterML20',
									'value': 'WATERML_2_0'
								}).css('margin-right', '8px')
							);
							waterml20Option.append(
								$('<label>')
									.attr('for', 'formatWaterML20')
									.text('WaterML 2.0')
									.css('color', '#2c3e50')
							);
							const netcdfOption = $('<div>').css({
								'display': 'flex',
								'align-items': 'center'
							});
							netcdfOption.append(
								$('<input>').attr({
									'type': 'radio',
									'name': 'downloadFormat',
									'id': 'formatNetCDF',
									'value': 'NETCDF'
								}).css('margin-right', '8px')
							);
							netcdfOption.append(
								$('<label>')
									.attr('for', 'formatNetCDF')
									.text('NetCDF')
									.css('color', '#2c3e50')
							);

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
									.text(t('select_data_format'))
									.css({
										'display': 'block',
										'margin-bottom': '10px',
										'font-weight': 'bold',
										'color': '#2c3e50'
									})
							);

							// Format selection
							const formatOptions = $('<div>').css({
								'display': 'flex',
								'flex-direction': 'column',
								'gap': '10px'
							});
							// First row: CSV, JSON, WaterML 1.0
							const formatRow1 = $('<div>').css({
								'display': 'flex',
								'gap': '20px',
								'margin-bottom': '0px'
							});
							formatRow1.append(csvOption);
							formatRow1.append(jsonOption);
							formatRow1.append(waterml10Option);
							// Second row: WaterML 2.0, NetCDF
							const formatRow2 = $('<div>').css({
								'display': 'flex',
								'gap': '20px',
								'margin-top': '0px'
							});
							formatRow2.append(waterml20Option);
							formatRow2.append(netcdfOption);
							formatOptions.append(formatRow1);
							formatOptions.append(formatRow2);
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
									'checked': false
								}).css('margin-right', '8px')
							);

							notificationsDiv.append(
								$('<label>')
									.attr('for', 'emailNotifications')
									.text(t('email_notifications_label'))
									.css({
										'font-size': '14px',
										'color': '#2c3e50'
									})
							);

							dialogContent.append(notificationsDiv);

							// Create and show dialog
							dialogContent.dialog({
								title: t('confirm_bulk_download_title'),
								modal: true,
								width: 400,
								classes: {
									"ui-dialog": "bulk-download-dialog"
								},
								buttons: [
									{
										text: t('proceed_to_download'),
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

												if (where.south && where.west && where.north && where.east) {
													params.append('west', where.west);
													params.append('south', where.south);
													params.append('east', where.east);
													params.append('north', where.north);
												}
											}

											// Add parameter constraint if it exists
											// Note: Filter panel values (added later) will take precedence
											if (constraints.kvp && Array.isArray(constraints.kvp)) {
												const attributeTitleValue = constraints.kvp.find(kvp => kvp.key === 'attributeTitle');
												if (attributeTitleValue) {
													params.append('observedProperty', attributeTitleValue.value);
												}
												const platformTitleValue = constraints.kvp.find(kvp => kvp.key === 'platformTitle');
												if (platformTitleValue) {
													params.append('featureName', platformTitleValue.value);
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

											// Add filters from filter panel (TermFrequencyWidget)
											// These take precedence over constraints.kvp values
											// Access checked items from global storage GIAPI.tfHelper.checkedItems
											var getCheckedItems = function(target) {
												// First try tfObject method
												if (GIAPI && GIAPI.search && GIAPI.search.tfWidget && GIAPI.search.tfWidget.tfObject) {
													if (typeof GIAPI.search.tfWidget.tfObject.checkedItems === 'function') {
														try {
															var items = GIAPI.search.tfWidget.tfObject.checkedItems(target);
															if (items && Array.isArray(items) && items.length > 0) {
																return items;
															}
														} catch (e) {
															// Continue to fallback
														}
													}
												}
												// Fallback to global storage directly
												if (GIAPI && GIAPI.tfHelper && GIAPI.tfHelper.checkedItems) {
													var items = GIAPI.tfHelper.checkedItems[target];
													if (items && Array.isArray(items) && items.length > 0) {
														return items;
													}
												}
												return null;
											};
											
											// Get source filter (provider)
											var sourceCheckedItems = getCheckedItems('source');
											if (sourceCheckedItems && sourceCheckedItems.length > 0) {
												// Extract source IDs from checked items
												var sourceIds = sourceCheckedItems.map(function(item) {
													return item.sourceId || item.term;
												});
												// Join multiple sources with comma
												params.set('provider', sourceIds.join(','));
											}
											
											// Get platformTitle filter (featureName/station)
											var platformTitleCheckedItems = getCheckedItems('platformTitle');
											if (platformTitleCheckedItems && platformTitleCheckedItems.length > 0) {
												// Extract terms from checked items
												var platformNames = platformTitleCheckedItems.map(function(item) {
													return item.term;
												});
												// Join multiple stations with comma
												params.set('featureName', platformNames.join(','));
											}
											
											// Get attributeURI filter (observedProperty - this is a URI)
											var attributeURICheckedItems = getCheckedItems('attributeURI');
											var observedPropertyURICheckedItems = getCheckedItems('observedPropertyURI');
											
											// Combine both attributeURI and observedPropertyURI if they exist
											var allObservedPropertyURIs = [];
											if (attributeURICheckedItems && attributeURICheckedItems.length > 0) {
												allObservedPropertyURIs = allObservedPropertyURIs.concat(
													attributeURICheckedItems.map(function(item) {
														return item.term;
													})
												);
											}
											if (observedPropertyURICheckedItems && observedPropertyURICheckedItems.length > 0) {
												allObservedPropertyURIs = allObservedPropertyURIs.concat(
													observedPropertyURICheckedItems.map(function(item) {
														return item.term;
													})
												);
											}
											if (allObservedPropertyURIs.length > 0) {
												// Join multiple URIs with comma
												params.set('observedProperty', allObservedPropertyURIs.join(','));
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

											// Construct the final URL using the view from config
											var downloadUrl = `${baseUrl}/token/${token}/view/${view}/om-api/downloads?${params.toString()}`;

											// Make the GET request
											fetch(downloadUrl, { method: 'PUT', body: '' })
												.then(response => {
													if (!response.ok) {
														throw new Error('Network response was not ok');
													}
													return response.json();
												})
												.then(data => {
													// Check if the response contains an error message
													if (data.message && !data.id) {
														// This is an error response (e.g., permission denied)
														GIAPI.UI_Utils.dialog('open', {
															title: t('error_title'),
															message: data.message || t('error_download_message')
														});
														return;
													}
													
													// Success response - should have an id field
													// Show success message
													GIAPI.UI_Utils.dialog('open', {
														title: t('download_started_title'),
														message: t('download_started_message')
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
														title: t('error_title'),
														message: t('error_download_message')
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

		// Simple filter accordion management - allow multiple panels open, all start closed
		// The accordion is initialized by TermFrequencyWidget with beforeActivate returning false
		// So we handle all state management ourselves
		var originalUpdate = GIAPI.search.tfWidget.update;
		var openPanelIndices = []; // Simple array tracking which panels are open
		var isFirstInit = true;
		var mutationObserver = null; // Observer to watch for table content changes
		
		// Function to adjust jtable container heights to fit content
		var adjustJtableHeights = function($filtersTab) {
			$filtersTab.find('[widget="tf"]').each(function() {
				var $container = jQuery(this);
				var $table = $container.find('table.jtable');
				var $tbody = $table.find('tbody');
				var rowCount = $tbody.find('tr.jtable-data-row').length;
				
				// Always set height to auto to fit content
				$container.css({
					'height': 'auto',
					'min-height': 'auto',
					'max-height': '550px' // Keep max-height from divCSS
				});
				$table.css({
					'height': 'auto',
					'min-height': 'auto',
					'max-height': 'none'
				});
				$tbody.css({
					'height': 'auto',
					'min-height': 'auto',
					'max-height': 'none'
				});
				
				// Also ensure the accordion content div doesn't have fixed height
				var $contentDiv = $container.closest('.ui-accordion-content');
				if ($contentDiv.length) {
					$contentDiv.css({
						'height': 'auto',
						'min-height': 'auto'
					});
				}
			});
		};
		
		// Simple function to toggle a panel
		var togglePanel = function($header, open) {
			var $content = $header.next();
			if (open) {
				$content.slideDown(300);
				$header.addClass('ui-accordion-header-active ui-state-active').removeClass('ui-state-default');
				$header.find('.ui-accordion-header-icon').removeClass('ui-icon-triangle-1-e').addClass('ui-icon-triangle-1-s');
				// Adjust heights after opening
				setTimeout(function() {
					adjustJtableHeights(jQuery('#filters-tab'));
				}, 350);
			} else {
				$content.slideUp(300);
				$header.removeClass('ui-accordion-header-active ui-state-active').addClass('ui-state-default');
				$header.find('.ui-accordion-header-icon').removeClass('ui-icon-triangle-1-s').addClass('ui-icon-triangle-1-e');
			}
		};
		
		// Setup click handlers for panels
		var setupPanelHandlers = function($filtersTab) {
			// Remove all existing handlers and add our custom one
			$filtersTab.find('h3').off('click.accordion click.customAccordion').on('click.customAccordion', function(event) {
				event.stopPropagation();
				event.preventDefault();
				
				var $header = jQuery(this);
				var headerIndex = $header.index();
				var isActive = $header.hasClass('ui-accordion-header-active');
				
				if (isActive) {
					// Close this panel
					togglePanel($header, false);
					// Remove from open list
					var idx = openPanelIndices.indexOf(headerIndex);
					if (idx > -1) {
						openPanelIndices.splice(idx, 1);
					}
				} else {
					// Open this panel
					togglePanel($header, true);
					// Add to open list if not already there
					if (openPanelIndices.indexOf(headerIndex) === -1) {
						openPanelIndices.push(headerIndex);
					}
				}
			});
		};
		
		// Wrap update to preserve open panels
		GIAPI.search.tfWidget.update = function(resultSet) {
			var $filtersTab = jQuery('#filters-tab');
			
			// Before update: get currently open panels (check both our state and DOM)
			var panelsToRestore = [];
			if ($filtersTab.hasClass('ui-accordion')) {
				$filtersTab.find('h3').each(function(index) {
					var $header = jQuery(this);
					// Check if panel is open in DOM or in our state
					if ($header.hasClass('ui-accordion-header-active') || openPanelIndices.indexOf(index) !== -1) {
						panelsToRestore.push(index);
					}
				});
			}
			
			// Call original update (this will refresh the accordion structure)
			var result = originalUpdate.call(this, resultSet);
			
			// After update: restore panels and setup handlers
			setTimeout(function() {
				if ($filtersTab.hasClass('ui-accordion')) {
					// Ensure accordion settings are correct
					$filtersTab.accordion('option', 'collapsible', true);
					$filtersTab.accordion('option', 'beforeActivate', function() {
						return false; // Prevent accordion from managing state
					});
					
					// On first init, close all panels
					if (isFirstInit) {
						$filtersTab.find('h3').each(function() {
							var $header = jQuery(this);
							var $content = $header.next();
							$content.hide();
							togglePanel($header, false);
						});
						openPanelIndices = [];
						isFirstInit = false;
					} else {
						// Restore panels that were open
						panelsToRestore.forEach(function(panelIndex) {
							var $header = $filtersTab.find('h3').eq(panelIndex);
							if ($header.length) {
								togglePanel($header, true);
								if (openPanelIndices.indexOf(panelIndex) === -1) {
									openPanelIndices.push(panelIndex);
								}
							}
						});
					}
					
					// Setup click handlers
					setupPanelHandlers($filtersTab);
					
					// Adjust jtable heights to fit content (fix white space issue)
					adjustJtableHeights($filtersTab);
					// Also adjust after a short delay to catch any delayed rendering
					setTimeout(function() {
						adjustJtableHeights($filtersTab);
					}, 200);
					
					// Setup MutationObserver to watch for table content changes
					if (mutationObserver) {
						mutationObserver.disconnect();
					}
					mutationObserver = new MutationObserver(function(mutations) {
						adjustJtableHeights($filtersTab);
					});
					// Observe all jtable containers for changes
					$filtersTab.find('[widget="tf"]').each(function() {
						mutationObserver.observe(this, {
							childList: true,
							subtree: true,
							attributes: false
						});
					});
				}
			}, 100);
			
			return result;
		};
		
		// Initial setup (before first update)
		setTimeout(function() {
			var $filtersTab = jQuery('#filters-tab');
			if ($filtersTab.hasClass('ui-accordion')) {
				// Ensure accordion settings
				$filtersTab.accordion('option', 'collapsible', true);
				$filtersTab.accordion('option', 'beforeActivate', function() {
					return false; // Prevent accordion from managing state
				});
				
				// Close all panels initially
				$filtersTab.find('h3').each(function() {
					var $header = jQuery(this);
					var $content = $header.next();
					$content.hide();
					togglePanel($header, false);
				});
				openPanelIndices = [];
				
				// Setup click handlers
				setupPanelHandlers($filtersTab);
			}
		}, 100);

		// Set height and ensure scrolling works properly
		// Calculate available height based on the left-sidebar container
		var calculateFiltersTabHeight = function() {
			var leftSidebar = jQuery('#left-sidebar');
			if (leftSidebar.length && leftSidebar.is(':visible')) {
				// Calculate based on available space in left-sidebar
				var sidebarHeight = leftSidebar.height();
				var tabsHeight = jQuery('#tabs-ul').outerHeight(true) || 40;
				var paginatorHeight = jQuery('#paginator-widget').is(':visible') ? jQuery('#paginator-widget').outerHeight(true) : 0;
				var margins = 10; // Small margin for spacing
				var availableHeight = sidebarHeight - tabsHeight - paginatorHeight - margins;
				// Ensure minimum height
				return Math.max(availableHeight, 300);
			} else {
				// Fallback to window-based calculation if sidebar not available
				var windowHeight = jQuery(window).height();
				var headerHeight = jQuery('#headerDiv').outerHeight(true) || 0;
				var portalHeaderHeight = jQuery('#portalHeaderRow').outerHeight(true) || 0;
				var tabsHeight = jQuery('#tabs-ul').outerHeight(true) || 40;
				var margins = 200;
				var availableHeight = windowHeight - headerHeight - portalHeaderHeight - tabsHeight - margins;
				return Math.max(availableHeight, 300);
			}
		};
		
		// Ensure filters-tab is properly contained
		jQuery('#filters-tab').css({
			'position': 'relative',
			'overflow-y': 'auto',
			'overflow-x': 'hidden',
			'background-color': 'white',
			'z-index': '1'
		});
		
		// Set initial height after a short delay to ensure layout is ready
		setTimeout(function() {
			var filtersTabHeight = calculateFiltersTabHeight();
			jQuery('#filters-tab').css({
				'height': filtersTabHeight + 'px',
				'max-height': filtersTabHeight + 'px',
				'min-height': '300px'
			});
		}, 100);
		
		// Update height on window resize
		jQuery(window).on('resize', function() {
			var newHeight = calculateFiltersTabHeight();
			jQuery('#filters-tab').css({
				'height': newHeight + 'px',
				'max-height': newHeight + 'px'
			});
		});
		
		// Also update when tabs are activated (in case paginator visibility changes)
		jQuery('#tabs-div').on('tabsactivate', function() {
			setTimeout(function() {
				var newHeight = calculateFiltersTabHeight();
				jQuery('#filters-tab').css({
					'height': newHeight + 'px',
					'max-height': newHeight + 'px'
				});
			}, 50);
		});


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

		if(GIAPI.OntologiesSelector.getSelectedIds().length > 0){
			constraints.kvp.push(
				{ 'key': 'semanticSearch', 'value': 'true' }
			);
		}
		
		if(config.semanticRelations){
			constraints.kvp.push(
				{ 'key': 'semanticRelations', 'value': config.semanticRelations }
			);
		}
		
		if(config.withObservedPropertiesURIs){
					constraints.kvp.push(
				{ 'key': 'withObservedPropertiesURIs', 'value': config.withObservedPropertiesURIs }
			);
		}			
		
		var ontologyIds = config.ontologyIds || GIAPI.OntologiesSelector.getSelectedIds();
		if(ontologyIds){
			constraints.kvp.push(
				{ 'key': 'ontologyIds', 'value': ontologyIds }
			);
		}
		
		// Add bboxUnion parameter if zoomOnResults is enabled
		if (config.zoomOnResults === true) {
			constraints.kvp.push(
				{ 'key': 'bboxUnion', 'value': 'true' }
			);
		}
		 
		GIAPI.search.resultsMapWidget.updateWMSClusterLayers(constraints);
		// set the termFrequency option
		options.termFrequency = 'source,keyword,format,protocol';

		if (config.filters) {
			
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
		// Handle zoom based on result set size and zoomOnResults setting
		if (config.zoomOnResults === true) {
			// Check if a bounding box is specified in the query
			var where = GIAPI.search.resultsMapWidget.where();
			var hasQueryBbox = where && where.south && where.west && where.north && where.east;
			
			if (resultSet.size < 1000 && response.bboxUnion) {
				// Zoom to bboxUnion for smaller result sets
				window.GIAPI.zoomToBoundingBox(response.bboxUnion);
			} else if (hasQueryBbox && response.bboxUnion) {
				// Zoom to bboxUnion if a bounding box is specified in the query
				window.GIAPI.zoomToBoundingBox(response.bboxUnion);
			} else if (hasQueryBbox && !response.bboxUnion) {
				// If query has bbox but no bboxUnion in response, construct bbox from query
				var queryBbox = {
					west: where.west,
					south: where.south,
					east: where.east,
					north: where.north
				};
				window.GIAPI.zoomToBoundingBox(queryBbox);
			} else if (resultSet.size >= 1000) {
				// Reset to default initial extent for larger result sets without query bbox
				window.GIAPI.zoomToDefaultExtent();
			}
		}
	};

	if (config.resultsVisibility !== undefined && !config.resultsVisibility) {
		// Set button to checked state and hide the entire left sidebar initially
		// Use setTimeout to ensure button and left sidebar are fully initialized
		setTimeout(function() {
			jQuery('#hideResultsButton').prop('checked', true);
			jQuery('#left-sidebar').css('display', 'none');
			// Let the map reclaim space
			if (GIAPI.search.resultsMapWidget && GIAPI.search.resultsMapWidget.map) {
				setTimeout(function() {
					GIAPI.search.resultsMapWidget.map.updateSize();
				}, 50);
			}
		}, 200);
	}

	if (config.bboxSelectorVisibility !== undefined && !config.bboxSelectorVisibility) {
		$('#hideMapInputControl').click();
	}

	// Expose a global function to zoom the map to a bounding box
	if (!window.GIAPI) window.GIAPI = {};
	window.GIAPI.zoomToBoundingBox = function(bbox) {
		var mapWidget = GIAPI.search && GIAPI.search.resultsMapWidget;
		var olMap = null;
		if (mapWidget) {
			if (mapWidget.olMap) {
				olMap = mapWidget.olMap;
			}
		}
		if (olMap && typeof olMap.fitBounds === 'function') {
			
			// Add padding to bbox to better frame stations, especially for point bboxes
			var padding = 0.05; // Default padding for non-point bboxes (degrees)
			var isPoint = (bbox.west === bbox.east && bbox.south === bbox.north);
			
			if (isPoint) {
				// For point bboxes, use larger padding (0.1 degrees) to avoid too much zoom
				padding = 0.1;
			}
			
			// Apply padding to bbox coordinates
			var paddedBbox = {
				west: bbox.west - padding,
				south: bbox.south - padding,
				east: bbox.east + padding,
				north: bbox.north + padding
			};
			
			// Clamp coordinates to valid ranges
			paddedBbox.west = Math.max(-180, paddedBbox.west);
			paddedBbox.south = Math.max(-90, paddedBbox.south);
			paddedBbox.east = Math.min(180, paddedBbox.east);
			paddedBbox.north = Math.min(90, paddedBbox.north);

			var minlatLon = ol.proj.transform([paddedBbox.west, paddedBbox.south], 'EPSG:4326', 'EPSG:3857');
			var maxlatLon = ol.proj.transform([paddedBbox.east, paddedBbox.north], 'EPSG:4326', 'EPSG:3857');

			var tbbox = { 'south': minlatLon[1], 'west': minlatLon[0], 'north': maxlatLon[1], 'east': maxlatLon[0] };

			olMap.fitBounds(tbbox);
		} else {
			alert('Map zoom function not available.');
		}
	};
	
	// Function to reset map to default initial extent from config
	window.GIAPI.zoomToDefaultExtent = function() {
		var mapWidget = GIAPI.search && GIAPI.search.resultsMapWidget;
		var olMap = null;
		if (mapWidget) {
			if (mapWidget.map) {
				olMap = mapWidget.map;
			} else if (mapWidget.olMap) {
				olMap = mapWidget.olMap;
			}
		}
		if (olMap && olMap.getView) {
			var view = olMap.getView();
			var centerLat = config.centerLat !== undefined ? config.centerLat : 0;
			var centerLon = config.centerLon !== undefined ? config.centerLon : 0;
			var zoom = config.zoom !== undefined ? config.zoom : 4;
			
			// Transform center coordinates to map projection
			var center = ol.proj.transform([centerLon, centerLat], 'EPSG:4326', 'EPSG:3857');
			
			// Set center and zoom
			view.setCenter(center);
			view.setZoom(zoom);
		} else {
			alert('Map zoom function not available.');
		}
	};
}

function showPermissionsDialog(currentPermissions, onSave) {
	// Read permissions from config, fallback to default if not present
	let allPermissions = ['downloads', 'api', 'admin'];
	if (window.config && typeof window.config.permissions === 'string') {
		allPermissions = window.config.permissions.split(',').map(p => p.trim()).filter(Boolean);
	}
	const selected = (currentPermissions || '').split(',').map(p => p.trim()).filter(Boolean);
	const dialogDiv = $('<div>').css({ 'padding': '10px' });
	dialogDiv.append($('<div>').text('Select permissions:').css({ 'margin-bottom': '10px' }));
	allPermissions.forEach(perm => {
		const checkbox = $('<input type="checkbox">').attr('id', 'perm_' + perm).val(perm);
		if (selected.includes(perm)) checkbox.prop('checked', true);
		const label = $('<label>').attr('for', 'perm_' + perm).text(perm).css({ 'margin-right': '20px' });
		dialogDiv.append(checkbox).append(label);
	});
	dialogDiv.dialog({
		title: 'Set Permissions',
		modal: true,
		width: 400,
		buttons: [
			{
				text: 'Save',
				click: function() {
					const checked = dialogDiv.find('input[type=checkbox]:checked').map(function() { return this.value; }).get();
					onSave(checked.join(','));
					$(this).dialog('close');
				}
			},
			{
				text: 'Cancel',
				click: function() { $(this).dialog('close'); }
			}
		]
	});
}

function showViewsDialog(currentViews, onSave) {
	if (availableViews.length === 0) {
		alert('No views available. Please ensure views are loaded.');
		return;
	}
	const selected = (currentViews || '').split(',').map(v => v.trim()).filter(Boolean);
	const dialogDiv = $('<div>').css({ 'padding': '10px', 'max-height': '400px', 'overflow-y': 'auto' });
	dialogDiv.append($('<div>').text('Select allowed views:').css({ 'margin-bottom': '10px', 'font-weight': 'bold' }));
	
	// Create a container for checkboxes with better layout
	const checkboxContainer = $('<div>').css({ 'display': 'flex', 'flex-direction': 'column', 'gap': '8px' });
	
	availableViews.forEach(view => {
		const viewId = view.id || '';
		const viewLabel = view.label || viewId;
		// Display label with ID in parentheses
		const displayText = viewLabel + (viewId ? ' (' + viewId + ')' : '');
		const checkbox = $('<input type="checkbox">').attr('id', 'view_' + viewId.replace(/[^a-zA-Z0-9]/g, '_')).val(viewId);
		if (selected.includes(viewId)) checkbox.prop('checked', true);
		const label = $('<label>').attr('for', 'view_' + viewId.replace(/[^a-zA-Z0-9]/g, '_'))
			.css({ 'cursor': 'pointer', 'display': 'flex', 'align-items': 'center', 'gap': '8px' })
			.append(checkbox)
			.append($('<span>').text(displayText));
		checkboxContainer.append($('<div>').append(label));
	});
	
	dialogDiv.append(checkboxContainer);
	
	dialogDiv.dialog({
		title: 'Set Allowed Views',
		modal: true,
		width: 500,
		buttons: [
			{
				text: 'Save',
				click: function() {
					const checked = dialogDiv.find('input[type=checkbox]:checked').map(function() { return this.value; }).get();
					onSave(checked.join(','));
					$(this).dialog('close');
				}
			},
			{
				text: 'Cancel',
				click: function() { $(this).dialog('close'); }
			}
		]
	});
}



