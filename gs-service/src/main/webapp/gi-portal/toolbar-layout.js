/**
 * Compact toolbar layout: Query / Filter / Results header buttons with toggle panels.
 */
import { GIAPI } from '../giapi/core/GIAPI.js';

export function isToolbarLayout(config) {
	return config && config.toolbarLayout === true;
}

export function isPredefinedSearchAreaVisible(config) {
	if (!config) {
		return false;
	}
	if (config.predefinedSearchAreaVisibility !== undefined) {
		return !!config.predefinedSearchAreaVisibility;
	}
	return !!config.shapeView;
}

function toolbarLabel(t, key, fallback) {
	var label = t(key);
	return label === key ? fallback : label;
}

function updateMapSize() {
	if (GIAPI.search.resultsMapWidget && GIAPI.search.resultsMapWidget.map) {
		setTimeout(function() {
			GIAPI.search.resultsMapWidget.map.updateSize();
		}, 50);
	}
}

function setButtonActive(id, active) {
	jQuery('#' + id).toggleClass('active', active);
}

function sidebarHasVisiblePanel() {
	return jQuery('#filters-panel:visible, #results-panel:visible').length > 0;
}

export function prepareToolbarHeader(t) {
	jQuery('body').addClass('toolbar-layout');
	jQuery('#headerDiv').addClass('portal-toolbar-header');

	jQuery('#tabs-ul').remove();
	jQuery('#search-button, #hide-results-button, #what-div, #from-div, #to-div, #where-div, #adv-search-div, #logoDiv').hide();

	var toolbarLeft = jQuery('<div id="header-toolbar-left" class="header-toolbar-left"></div>');
	[
		{ id: 'toolbar-query-btn', key: 'query_toolbar', fallback: 'Query', icon: 'fa-book' },
		{ id: 'toolbar-filter-btn', key: 'filter_toolbar', fallback: 'Filter', icon: 'fa-filter' },
		{ id: 'toolbar-results-btn', key: 'results_toolbar', fallback: 'Results', icon: 'fa-list-ul' }
	].forEach(function(spec) {
		var label = toolbarLabel(t, spec.key, spec.fallback);
		toolbarLeft.append(
			jQuery('<button type="button" class="portal-toolbar-btn"></button>')
				.attr('id', spec.id)
				.html('<i class="fa ' + spec.icon + ' portal-toolbar-btn-icon" aria-hidden="true"></i>' + label)
		);
	});

	jQuery('#headerDiv').prepend(toolbarLeft);
}

export function prepareHeaderLayersControl(t) {
	if (jQuery('#header-layers-panel-body').length) {
		return document.getElementById('header-layers-panel-body');
	}

	var headerRight = jQuery('#header-toolbar-right');
	if (!headerRight.length) {
		headerRight = jQuery('<div id="header-toolbar-right" class="header-toolbar-right"></div>');
		jQuery('#headerDiv').append(headerRight);
		var loginContainer = jQuery('.login-container');
		if (loginContainer.length) {
			headerRight.append(loginContainer);
		}
	}

	var label = toolbarLabel(t, 'layers_toolbar', 'Layers');
	var layersBtn = jQuery('<button type="button" id="header-layers-btn" class="login-button header-layers-btn"></button>')
		.html('<i class="fa fa-map-o header-layers-btn-icon" aria-hidden="true"></i>' + label);
	headerRight.prepend(layersBtn);

	var panel = jQuery(
		'<div id="header-layers-panel" class="header-layers-panel" style="display:none;">' +
			'<div id="header-layers-panel-body" class="header-layers-panel-body"></div>' +
		'</div>'
	);
	headerRight.append(panel);

	layersBtn.on('click', function(e) {
		e.stopPropagation();
		var open = !panel.is(':visible');
		panel.toggle(open);
		layersBtn.toggleClass('active', open);
	});

	jQuery(document).off('click.headerLayersPanel').on('click.headerLayersPanel', function(e) {
		if (!jQuery(e.target).closest('#header-layers-panel, #header-layers-btn').length) {
			panel.hide();
			layersBtn.removeClass('active');
		}
	});

	return document.getElementById('header-layers-panel-body');
}

export function prepareQueryPanelShell(t, config) {
	var predefinedSection = isPredefinedSearchAreaVisible(config) ?
		'<section id="query-section-predefined" class="query-panel-section">' +
			'<h4 class="query-panel-section-title">' + toolbarLabel(t, 'predefined_selection', 'Predefined search area') + '</h4>' +
			'<div id="query-predefined-fields" class="query-panel-fields query-spatial-inline"></div>' +
		'</section>' :
		'';

	var queryPanel = jQuery(
		'<div id="query-panel" class="portal-panel query-panel" style="display:none;">' +
			'<div id="query-panel-body" class="portal-panel-body">' +
				'<section id="query-section-temporal" class="query-panel-section">' +
					'<h4 class="query-panel-section-title">' + toolbarLabel(t, 'temporal_constraints', 'Temporal constraints') + '</h4>' +
					'<div id="query-temporal-fields" class="query-panel-fields"></div>' +
				'</section>' +
				'<section id="query-section-bbox" class="query-panel-section">' +
					'<h4 class="query-panel-section-title">' + toolbarLabel(t, 'spatial_extent', 'Spatial extent') + '</h4>' +
					'<div id="query-bbox-fields" class="query-panel-fields query-spatial-inline"></div>' +
				'</section>' +
				predefinedSection +
				'<section id="query-section-sources" class="query-panel-section">' +
					'<h4 class="query-panel-section-title">' + toolbarLabel(t, 'sources_tab', 'Sources') + '</h4>' +
					'<div id="query-sources-fields" class="query-panel-fields"></div>' +
				'</section>' +
				'<section id="query-section-advanced" class="query-panel-section">' +
					'<h4 class="query-panel-section-title">' + toolbarLabel(t, 'advanced', 'Advanced constraints') + '</h4>' +
					'<div id="query-advanced-fields" class="query-panel-fields"></div>' +
				'</section>' +
			'</div>' +
			'<div id="query-panel-footer" class="query-panel-footer">' +
				'<div id="query-panel-search"></div>' +
			'</div>' +
		'</div>'
	);
	jQuery(document.body).append(queryPanel);
	ensureSourcesMountInQueryPanel();
}

export function mountQueryPanel(contentWrapper) {
	var queryPanel = jQuery('#query-panel');
	if (queryPanel.length && contentWrapper && contentWrapper.length) {
		contentWrapper.prepend(queryPanel);
	}
}

export function restructureToolbarSidebar(leftSidebar, tabsDiv) {
	if (leftSidebar.data('toolbar-restructured')) {
		return;
	}

	jQuery('#tabs-ul').remove();
	ensureSourcesMountInQueryPanel();

	tabsDiv = tabsDiv && tabsDiv.length ? tabsDiv : jQuery('#tabs-div');

	// Query tab panels from tabsDiv before it is detached from the document.
	// Global jQuery('#filters-tab') fails once tabsDiv is appended to a detached leftSidebar.
	var filtersTab = tabsDiv.find('#filters-tab');
	var resultsTab = tabsDiv.find('#results-tab');
	if (!filtersTab.length) {
		filtersTab = jQuery('#filters-tab');
	}
	if (!resultsTab.length) {
		resultsTab = jQuery('#results-tab');
	}
	if (!filtersTab.length) {
		filtersTab = jQuery('<div id="filters-tab"></div>');
	}
	if (!resultsTab.length) {
		resultsTab = jQuery('<div id="results-tab"></div>');
	}

	var filtersPanel = jQuery('<div id="filters-panel" class="portal-side-panel" style="display:none;"></div>');
	var resultsPanel = jQuery('<div id="results-panel" class="portal-side-panel" style="display:none;"></div>');

	filtersTab.removeClass('tabs-element ui-tabs-panel ui-tabs-hide');
	resultsTab.removeClass('tabs-element ui-tabs-panel ui-tabs-hide');
	filtersPanel.append(filtersTab);

	var paginator = jQuery('#paginator-widget');
	if (paginator.length) {
		resultsPanel.append(paginator);
	}
	resultsPanel.append(resultsTab);

	leftSidebar.empty().append(filtersPanel).append(resultsPanel);
	leftSidebar.css('display', 'none');
	leftSidebar.data('toolbar-restructured', true);

	tabsDiv.remove();
	jQuery('#results-tab, #filters-tab').hide();
}

function attachHelpIconToSectionTitle(sectionSelector, helpIcon) {
	var sectionTitle = jQuery(sectionSelector);
	if (!sectionTitle.length || !helpIcon.length || sectionTitle.children('i.fa').length) {
		return;
	}

	helpIcon.detach().appendTo(sectionTitle);
	helpIcon.css({
		marginLeft: '6px',
		verticalAlign: 'middle',
		fontSize: '14px',
		color: '#666',
		cursor: 'pointer',
		float: 'none',
		marginRight: '0'
	});
}

function attachPredefinedHelpToSectionTitle() {
	var sectionTitle = jQuery('#query-section-predefined .query-panel-section-title');
	if (!sectionTitle.length || sectionTitle.children('i.fa').length) {
		return;
	}

	var t = window.__t || function(s) { return s; };
	var title = toolbarLabel(t, 'predefined_selection', 'Predefined search area');
	var help = toolbarLabel(
		t,
		'predefined_selection_help',
		'These polygons are used for search. Click a row to select it; click again to unselect it.'
	);

	sectionTitle.append(GIAPI.UI_Utils.helpImage(
		title,
		help,
		'margin-left: 6px; vertical-align: middle; font-size: 14px; color: #666;'
	));
}

function attachSpatialExtentHelpToSectionTitle(controlTable) {
	var clearRow = controlTable.find('tr').has('.font-awesome-button').not(':has(#containsButton, #overlapsButton)').last();
	var helpIcon = clearRow.find('i.fa').not('.font-awesome-button-icon').first();
	attachHelpIconToSectionTitle('#query-section-bbox .query-panel-section-title', helpIcon);
}

function renameSpatialClearButton(bboxHost) {
	var clearBtn = bboxHost.find('table.cnst-widget-where-input-control tr')
		.has('.font-awesome-button')
		.not(':has(#containsButton, #overlapsButton)')
		.last()
		.find('.font-awesome-button');

	if (!clearBtn.length) {
		return;
	}

	clearBtn.css({ width: 'auto', minWidth: '130px', whiteSpace: 'nowrap' });
	clearBtn.find('.font-awesome-button-label').text('CLEAR BBOX').css('white-space', 'nowrap');
}

export function relocateSpatialControlToQueryPanel(config) {
	var bboxHost = jQuery('#query-bbox-fields');
	if (!bboxHost.length) {
		return;
	}

	var showPredefined = isPredefinedSearchAreaVisible(config);
	var predefinedHost = showPredefined ? jQuery('#query-predefined-fields') : jQuery();

	if (bboxHost.find('table.cnst-widget-where-input-control').length) {
		var existingTable = bboxHost.find('table.cnst-widget-where-input-control').first();
		attachSpatialExtentHelpToSectionTitle(existingTable);
		if (showPredefined) {
			attachPredefinedHelpToSectionTitle();
		}
		renameSpatialClearButton(bboxHost);
		jQuery('#mapControlDiv').hide();
		jQuery('#whereTableCaption, #onoffswitch-div-hideMapInputControl').hide();
		return;
	}

	var mapControl = jQuery('#mapControlDiv');
	if (!mapControl.length) {
		return;
	}

	bboxHost.empty();
	if (predefinedHost.length) {
		predefinedHost.empty();
	}

	var controlTable = mapControl.find('table.cnst-widget-where-input-control').first();
	var layerSelectorDiv = showPredefined ? mapControl.find('#layerSelectorDiv') : jQuery();

	if (controlTable.length) {
		controlTable.detach().appendTo(bboxHost);
		controlTable.css({ width: '100%', display: 'table' });
		controlTable.find('input[type="number"]').css({ width: '100%', boxSizing: 'border-box' });
		attachSpatialExtentHelpToSectionTitle(controlTable);
		if (showPredefined) {
			attachPredefinedHelpToSectionTitle();
		}
		controlTable.find('tr').has('.cnst-widget-location-field').hide();
		controlTable.find('#layersSelectorButton').closest('tr').hide();
		controlTable.find('#containsButton, #overlapsButton').closest('tr').hide();
		controlTable.find('img').hide();
		controlTable.find('tr').filter(function() {
			return jQuery(this).text().trim() === '';
		}).hide();
		renameSpatialClearButton(bboxHost);
	}

	if (showPredefined && layerSelectorDiv.length && predefinedHost.length) {
		layerSelectorDiv.detach().appendTo(predefinedHost);
		jQuery('#layersSelectorButton').prop('checked', false).closest('div').hide();
		layerSelectorDiv.css({
			display: 'block',
			position: 'relative',
			width: '100%',
			maxWidth: 'none',
			height: 'auto',
			marginTop: '0',
			marginLeft: '0'
		});
		jQuery('#layerNameSearchInput').css({ width: '100%', boxSizing: 'border-box' });
		jQuery('#layerSelectorDiv div[style*="overflow-y"]').css({ maxHeight: '180px' });
		jQuery('#layerSelectorDiv #wrapLayerSelectorTable th').hide();
	}

	mapControl.find('.map-control-div').css('display', 'none');
	mapControl.hide();
	jQuery('#whereTableCaption, #onoffswitch-div-hideMapInputControl').hide();
}

function formatIsoDate(date) {
	var month = date.getMonth() + 1;
	var day = date.getDate();
	return date.getFullYear() + '-' +
		(month < 10 ? '0' : '') + month + '-' +
		(day < 10 ? '0' : '') + day;
}

function setTemporalInputDate(fieldId, date) {
	var field = jQuery('#' + fieldId);
	field.val(formatIsoDate(date));
	if (field.hasClass('hasDatepicker')) {
		field.datepicker('setDate', date);
	}
}

function applyTemporalPreset(monthsBack, yearsBack) {
	if (!GIAPI.search.constWidget) {
		return;
	}

	var end = new Date();
	var start = new Date();
	if (monthsBack) {
		start.setMonth(start.getMonth() - monthsBack);
	}
	if (yearsBack) {
		start.setFullYear(start.getFullYear() - yearsBack);
	}

	setTemporalInputDate(GIAPI.search.constWidget.getId('from'), start);
	setTemporalInputDate(GIAPI.search.constWidget.getId('to'), end);
}

function setupTemporalPresetButtons() {
	var container = jQuery('#query-temporal-presets');
	if (!container.length) {
		container = jQuery('<div id="query-temporal-presets" class="query-temporal-presets"></div>');
		jQuery('#query-temporal-fields').append(container);
	}
	if (container.data('initialized')) {
		return;
	}
	container.data('initialized', true);

	var t = window.__t || function(s) { return s; };
	[
		{ id: 'temporal-preset-2months', key: 'last_2_months', fallback: 'Last 2 months', months: 2 },
		{ id: 'temporal-preset-year', key: 'last_year', fallback: 'Last year', years: 1 }
	].forEach(function(spec) {
		container.append(
			jQuery('<button type="button" class="query-temporal-preset-btn"></button>')
				.attr('id', spec.id)
				.text(toolbarLabel(t, spec.key, spec.fallback))
				.on('click', function() {
					applyTemporalPreset(spec.months || 0, spec.years || 0);
				})
		);
	});
}

function setupTemporalFieldsInQueryPanel() {
	jQuery('#from-div, #to-div').show().css({
		display: 'block',
		marginLeft: '0',
		width: '100%'
	});

	if (!GIAPI.search.constWidget) {
		return;
	}

	var fromId = GIAPI.search.constWidget.getId('from');
	var toId = GIAPI.search.constWidget.getId('to');
	jQuery('#' + fromId + ', #' + toId).css({
		width: '100%',
		boxSizing: 'border-box'
	});
	jQuery('#' + fromId + ', #' + toId).parent('div').parent('td').css('width', '100%');
	jQuery('#query-temporal-fields .cnst-widget-div, #query-temporal-fields .cnst-widget-table').css('width', '100%');
	setupTemporalPresetButtons();
}

function setupWhatFieldsInQueryPanel() {
	jQuery('#what-div').show().css({
		display: 'block',
		marginLeft: '0',
		width: '100%'
	});

	var container = jQuery('#query-what-fields');
	if (!container.length) {
		return;
	}

	container.find('.cnst-widget-div, .cnst-widget-table').css({ width: '100%' });

	var innerTables = container.find('.cnst-widget-table > tbody > tr > td > table');
	innerTables.css({ display: 'block', width: '100%' });
	innerTables.children('tbody').css({ display: 'block' });

	var innerRows = innerTables.children('tbody').children('tr');
	innerRows.css({
		display: 'flex',
		alignItems: 'center',
		width: '100%'
	});
	innerRows.children('td:first-child').css({
		flex: '1 1 auto',
		minWidth: '0',
		width: 'auto',
		display: 'block',
		resize: 'none',
		overflow: 'visible',
		paddingRight: '0'
	});
	innerRows.children('td:nth-child(2)').css({
		flex: '0 0 auto',
		width: 'auto'
	});

	container.find('.cnst-widget-input').css({
		width: '100%',
		boxSizing: 'border-box',
		minWidth: '0'
	});

	if (!GIAPI.search.constWidget) {
		return;
	}

	jQuery('#' + GIAPI.search.constWidget.getId('what')).css({
		width: '100%',
		boxSizing: 'border-box',
		minWidth: '0'
	});
}

export function moveQueryPanelContent(config) {
	jQuery('#query-temporal-fields').append(jQuery('#from-div'), jQuery('#to-div'));
	setupTemporalFieldsInQueryPanel();
	relocateSpatialControlToQueryPanel(config);

	if (config.generalTermSearch === undefined || config.generalTermSearch) {
		jQuery('#query-panel-body').prepend(
			jQuery('<section id="query-section-what" class="query-panel-section">' +
				'<h4 class="query-panel-section-title">' + toolbarLabel(window.__t || function(s) { return s; }, 'what_label', 'Search terms') + '</h4>' +
				'<div id="query-what-fields" class="query-panel-fields"></div>' +
			'</section>')
		);
		jQuery('#query-what-fields').append(jQuery('#what-div'));
		setupWhatFieldsInQueryPanel();
	}

	var clearAllBtn = window._clearAllButton || document.getElementById('clearAllButton');
	if (clearAllBtn && !clearAllBtn.parentNode) {
		jQuery('#query-panel-footer').prepend(clearAllBtn);
	} else if (clearAllBtn) {
		jQuery(clearAllBtn).detach();
		jQuery('#query-panel-footer').prepend(clearAllBtn);
	}
	if (clearAllBtn) {
		jQuery(clearAllBtn).addClass('query-panel-clear-btn');
	}
}

function setupAdvancedFieldsInQueryPanel() {
	var container = jQuery('#query-advanced-fields #advConstDiv');
	if (!container.length) {
		return;
	}

	container.css({
		display: 'block',
		position: 'static',
		top: 'auto',
		left: 'auto',
		width: '100%',
		maxHeight: 'none',
		overflow: 'visible',
		boxShadow: 'none',
		border: 'none',
		background: 'transparent',
		padding: '0',
		margin: '0',
		zIndex: 'auto'
	});

	container.find('table').css({ width: '100%', marginBottom: '8px' });
	container.find('.cnst-widget-input, select.cnst-widget-input').css({
		width: '100%',
		boxSizing: 'border-box'
	});
	container.find('td').css({ paddingLeft: '0' });

	var innerTables = container.find('table > tbody > tr > td > table');
	innerTables.css({ display: 'block', width: '100%' });
	innerTables.children('tbody').css({ display: 'block' });
	var innerRows = innerTables.children('tbody').children('tr');
	innerRows.css({
		display: 'flex',
		alignItems: 'center',
		width: '100%'
	});
	innerRows.children('td').css({ display: 'block' });
	innerRows.children('td:first-child').css({
		flex: '1 1 auto',
		minWidth: '0',
		width: 'auto'
	});
	innerRows.children('td:first-child').children('div').css({
		height: 'auto',
		display: 'flex',
		alignItems: 'center',
		width: '100%'
	});
	innerRows.children('td:nth-child(2)').css({
		flex: '0 0 auto',
		width: 'auto'
	});
	innerRows.children('td:last-child').css({
		flex: '0 0 24px',
		width: '24px',
		height: 'auto',
		display: 'flex',
		alignItems: 'center',
		justifyContent: 'center',
		padding: '0',
		textAlign: 'center'
	});
	container.find('.cnst-widget-clear-button').css({
		marginTop: '0',
		marginLeft: '0'
	});
	container.find('i.odip-help').css({
		marginLeft: '0',
		lineHeight: '1'
	});
}

export function appendInlineAdvancedConstraints(advancedConstraints) {
	if (!advancedConstraints.length) {
		jQuery('#query-section-advanced').hide();
		return;
	}

	jQuery('#query-section-advanced').show();
	var host = jQuery('#query-advanced-fields');
	var container = host.find('#advConstDiv');
	if (!container.length) {
		container = jQuery('<div id="advConstDiv" class="query-advanced-inline"></div>');
		host.prepend(container);
	} else {
		container.empty();
	}

	advancedConstraints.forEach(function(constraint) {
		container.append(constraint);
	});
	setupAdvancedFieldsInQueryPanel();
}

export function installToolbarDiscoverButton(t, runDiscover) {
	var searchButton = GIAPI.FontAwesomeButton({
		'width': 140,
		'label': t('search'),
		'icon': 'fa-search',
		'handler': runDiscover
	});
	jQuery('#query-panel-search').empty().append(searchButton.div());
	jQuery('#query-panel-search .font-awesome-button').css({
		'background-color': '#2c3e50',
		'color': 'white',
		'border': 'none',
		'border-radius': '4px',
		'padding': '8px 16px',
		'cursor': 'pointer',
		'font-size': '14px',
		'height': '36px',
		'display': 'inline-flex',
		'align-items': 'center',
		'justify-content': 'center',
		'gap': '6px'
	});
}

function showToolbarSidePanel(panel) {
	panel.css({
		display: 'flex',
		flexDirection: 'column'
	});
}

function ensureFiltersTabInPanel() {
	var filtersPanel = jQuery('#filters-panel');
	var filtersTab = jQuery('#filters-tab');
	if (!filtersPanel.length) {
		return filtersTab;
	}
	if (!filtersTab.length) {
		filtersTab = jQuery('<div id="filters-tab"></div>');
	}
	if (filtersTab.parent().attr('id') !== 'filters-panel') {
		filtersPanel.append(filtersTab);
	}
	return filtersTab;
}

export function refreshFiltersPanelLayout() {
	var filtersPanel = jQuery('#filters-panel');
	var filtersTab = ensureFiltersTabInPanel();
	if (!filtersPanel.length || !filtersTab.length) {
		return;
	}

	filtersTab.css({
		display: 'block',
		flex: '1 1 auto',
		minHeight: '0',
		width: '100%',
		overflowX: 'hidden',
		overflowY: 'auto',
		visibility: 'visible'
	});

	filtersTab.find('[widget="tf"]').css({
		width: '100%',
		maxWidth: '100%',
		marginLeft: '0',
		marginBottom: '8px',
		boxSizing: 'border-box'
	});

	if (filtersTab.children('h3').length) {
		filtersTab.find('.filters-panel-empty-msg').remove();
		filtersTab.find('h3').css({
			display: 'block',
			visibility: 'visible'
		});
	} else if (!filtersTab.find('.filters-panel-empty-msg').length) {
		var emptyText = toolbarLabel(window.__t || function(s) { return s; }, 'filters_panel_empty', 'Run a search to load filters.');
		filtersTab.append('<p class="filters-panel-empty-msg">' + emptyText + '</p>');
	}
}

function filtersAccordionContent(header) {
	return header.next('.ui-accordion-content, div[widget="tf"]');
}

export function setFiltersAccordionSectionOpen(header, open) {
	var $header = jQuery(header);
	var $content = filtersAccordionContent($header);
	if (!$content.length) {
		return;
	}
	if (open) {
		$content.addClass('filters-section-open').attr('aria-hidden', 'false');
		$header.addClass('ui-accordion-header-active ui-state-active').removeClass('ui-state-default');
		$header.find('.ui-accordion-header-icon')
			.removeClass('ui-icon-triangle-1-e')
			.addClass('ui-icon-triangle-1-s');
	} else {
		$content.removeClass('filters-section-open').attr('aria-hidden', 'true');
		$header.removeClass('ui-accordion-header-active ui-state-active').addClass('ui-state-default');
		$header.find('.ui-accordion-header-icon')
			.removeClass('ui-icon-triangle-1-s')
			.addClass('ui-icon-triangle-1-e');
	}
}

function filtersOpenPanelIndices() {
	if (!GIAPI.search.filtersOpenPanelIndices) {
		GIAPI.search.filtersOpenPanelIndices = [];
	}
	return GIAPI.search.filtersOpenPanelIndices;
}

export function captureFiltersAccordionOpenState(filtersTab) {
	filtersTab = jQuery(filtersTab);
	var indices = [];
	if (!filtersTab.length) {
		return indices;
	}
	filtersTab.find('h3').each(function(index) {
		var header = jQuery(this);
		var content = filtersAccordionContent(header);
		if (header.hasClass('ui-accordion-header-active') || content.hasClass('filters-section-open')) {
			indices.push(index);
		}
	});
	GIAPI.search.filtersOpenPanelIndices = indices;
	return indices;
}

export function restoreFiltersAccordionOpenState(filtersTab) {
	filtersTab = jQuery(filtersTab);
	if (!filtersTab.length || !filtersTab.find('h3').length) {
		return;
	}
	var indices = filtersOpenPanelIndices();
	filtersTab.find('h3').each(function(index) {
		var header = jQuery(this);
		header.css({ display: 'block', visibility: 'visible' });
		setFiltersAccordionSectionOpen(header, indices.indexOf(index) !== -1);
	});
}

export function collapseFiltersAccordionSections(filtersTab) {
	filtersTab = jQuery(filtersTab);
	if (!filtersTab.length || !filtersTab.find('h3').length) {
		return;
	}
	filtersTab.find('h3').each(function() {
		var header = jQuery(this);
		header.css({ display: 'block', visibility: 'visible' });
		setFiltersAccordionSectionOpen(header, false);
	});
	filtersOpenPanelIndices().length = 0;
}

function hideToolbarQueryPanel() {
	jQuery('#query-panel').removeClass('toolbar-panel-visible');
	setButtonActive('toolbar-query-btn', false);
}

function hideToolbarFiltersPanel() {
	var filtersTab = jQuery('#filters-tab');
	if (filtersTab.length) {
		captureFiltersAccordionOpenState(filtersTab);
	}
	jQuery('#filters-panel').hide();
	jQuery('#filters-tab').hide();
	setButtonActive('toolbar-filter-btn', false);
}

function hideToolbarResultsPanel() {
	jQuery('#results-panel').hide();
	jQuery('#results-tab').hide();
	setButtonActive('toolbar-results-btn', false);
}

function hideToolbarSidebarPanels() {
	hideToolbarFiltersPanel();
	hideToolbarResultsPanel();
	jQuery('#left-sidebar').removeClass('toolbar-sidebar-visible').hide();
}

function hideAllToolbarPanels() {
	hideToolbarQueryPanel();
	hideToolbarSidebarPanels();
}

function isToolbarQueryPanelActive() {
	return jQuery('#query-panel').hasClass('toolbar-panel-visible');
}

function isToolbarFiltersPanelActive() {
	return jQuery('#filters-panel').is(':visible');
}

function isToolbarResultsPanelActive() {
	return jQuery('#results-panel').is(':visible');
}

export function showToolbarQueryPanel() {
	hideToolbarSidebarPanels();
	jQuery('#query-panel').addClass('toolbar-panel-visible');
	setButtonActive('toolbar-query-btn', true);
	setButtonActive('toolbar-filter-btn', false);
	setButtonActive('toolbar-results-btn', false);
	updateMapSize();
	GIAPI.search.syncMarkersLayerVisibility();
}

export function showToolbarFiltersPanel() {
	hideToolbarQueryPanel();
	hideToolbarSidebarPanels();
	jQuery('#mapControlDiv').hide();
	jQuery('#left-sidebar').addClass('toolbar-sidebar-visible').css('display', 'flex');
	showToolbarSidePanel(jQuery('#filters-panel'));
	var filtersTab = ensureFiltersTabInPanel();
	filtersTab.show();
	refreshFiltersPanelLayout();
	restoreFiltersAccordionOpenState(filtersTab);
	setButtonActive('toolbar-filter-btn', true);
	setButtonActive('toolbar-query-btn', false);
	setButtonActive('toolbar-results-btn', false);
	updateMapSize();
	GIAPI.search.syncMarkersLayerVisibility();
}

export function showToolbarResultsPanel() {
	hideToolbarQueryPanel();
	hideToolbarSidebarPanels();
	jQuery('#mapControlDiv').hide();
	jQuery('#left-sidebar').addClass('toolbar-sidebar-visible').css('display', 'flex');
	showToolbarSidePanel(jQuery('#results-panel'));
	jQuery('#results-tab').css({ display: 'block', flex: '1 1 auto', minHeight: '0' }).show();
	jQuery('#paginator-widget').show();
	setButtonActive('toolbar-results-btn', true);
	setButtonActive('toolbar-query-btn', false);
	setButtonActive('toolbar-filter-btn', false);
	updateMapSize();
	GIAPI.search.syncMarkersLayerVisibility();
}

export function installToolbarPanelHandlers() {
	jQuery('#toolbar-query-btn').on('click', function() {
		if (isToolbarQueryPanelActive()) {
			hideAllToolbarPanels();
			updateMapSize();
			GIAPI.search.syncMarkersLayerVisibility();
			return;
		}
		showToolbarQueryPanel();
	});
	jQuery('#toolbar-filter-btn').on('click', function() {
		if (isToolbarFiltersPanelActive()) {
			hideAllToolbarPanels();
			updateMapSize();
			GIAPI.search.syncMarkersLayerVisibility();
			return;
		}
		showToolbarFiltersPanel();
	});
	jQuery('#toolbar-results-btn').on('click', function() {
		if (isToolbarResultsPanelActive()) {
			hideAllToolbarPanels();
			updateMapSize();
			GIAPI.search.syncMarkersLayerVisibility();
			return;
		}
		showToolbarResultsPanel();
	});
}

export function installToolbarResultsPanelApi() {
	GIAPI.search.isResultsTabActive = function() {
		return jQuery('#results-panel').length > 0 && jQuery('#results-panel').is(':visible');
	};

	GIAPI.search.showResultsPanel = function() {
		showToolbarResultsPanel();
	};
}

function styleSourcesListContainer(sourcesTab) {
	sourcesTab.find('div[style*="overflow-y"]').css({
		'overflow-y': 'auto',
		'overflow-x': 'hidden',
		'min-height': '0',
		'max-height': '220px',
		'height': 'auto'
	});

	sourcesTab.find('#wrapSourcesWidgetTable, #sourcesWidgetTable').css('height', 'auto');
	sourcesTab.find('#sourcesWidgetTable .sources-widget-switch > div').css('margin', '0');

	jQuery('#sourceNameSearchInput').css({
		'width': '100%',
		'box-sizing': 'border-box'
	});

	jQuery('.sources-widget').css({
		'padding-right': '0',
		'padding-left': '0',
		'padding-top': '0'
	});
}

export function ensureSourcesMountInQueryPanel() {
	var sourcesHost = jQuery('#query-sources-fields');
	if (!sourcesHost.length) {
		return;
	}

	jQuery('#tabs-ul').remove();

	var sourcesTab = jQuery('#sources-tab');
	if (!sourcesTab.length) {
		sourcesTab = jQuery('<div id="sources-tab" class="query-sources-mount"></div>');
	}

	if (sourcesTab.parent().attr('id') !== 'query-sources-fields') {
		sourcesHost.append(sourcesTab);
	}

	sourcesTab
		.removeClass('tabs-element ui-tabs-panel ui-tabs-hide')
		.addClass('query-sources-compact')
		.show()
		.css({
			'display': 'block',
			'width': '100%',
			'min-height': '180px',
			'max-height': '280px',
			'margin': '0',
			'background': 'transparent',
			'box-shadow': 'none',
			'overflow': 'visible'
		});

	styleSourcesListContainer(sourcesTab);
}

export function moveSourcesToQueryPanel() {
	ensureSourcesMountInQueryPanel();
}

export function finalizeToolbarLayout() {
	ensureSourcesMountInQueryPanel();
	jQuery('#tabs-ul, #tabs-div').remove();
	jQuery('#left-sidebar').removeClass('toolbar-sidebar-visible').hide();
	jQuery('#filters-panel, #results-panel, #query-panel').hide().removeClass('toolbar-panel-visible');
	jQuery('#mapControlDiv').hide();
	jQuery('#query-bbox-fields #mapControlDiv, #query-predefined-fields #mapControlDiv').remove();
	jQuery('#results-tab, #filters-tab').hide();
	jQuery('#paginator-widget').show();
	jQuery('#filters-tab, #results-tab').css({
		'flex': '1 1 auto',
		'min-height': '0',
		'height': 'auto',
		'max-height': 'none'
	});
	refreshFiltersPanelLayout();
	updateMapSize();
}
