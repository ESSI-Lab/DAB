<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Data Provider Information</title>
  <link rel="stylesheet" href="../giapi/ol/ol.css">
  <style>
    body {
      font-family: 'Segoe UI', Arial, sans-serif;
      background: #f8f9fa;
      color: #222;
      margin: 0;
      padding: 0 0 40px 0;
    }

    h1, h2, h3 {
      color: #005aef;
      margin-top: 30px;
    }

    .page-container {
      max-width: 1100px;
      margin: 0 auto;
      padding: 0 16px 32px;
    }

    .loading,
    .error {
      font-size: 1.1em;
      margin-top: 40px;
    }

    .error {
      color: #b22222;
    }

    a {
      color: #005aef;
      text-decoration: none;
    }

    a:hover {
      text-decoration: underline;
    }

    .source-list {
      list-style: none;
      padding: 0;
      margin: 20px 0;
    }

    .source-list li {
      margin-bottom: 8px;
    }

    .provider-layout {
      display: flex;
      flex-wrap: wrap;
      gap: 24px;
      margin-bottom: 32px;
    }

    .provider-left {
      flex: 1 1 360px;
      min-width: 320px;
    }

    .provider-right {
      flex: 1 1 360px;
      min-width: 320px;
    }

    .provider-map {
      width: 100%;
      height: 320px;
      background: #e5e5e5;
      border-radius: 6px;
      overflow: hidden;
    }

    .map-credit {
      font-size: 0.95em;
      color: #666;
      margin: 4px 0 16px 4px;
    }

    .stats-list {
      list-style: none;
      padding: 0;
      margin: 0 0 18px 0;
    }

    .stats-list li {
      margin-bottom: 8px;
      font-size: 1.05em;
    }

    .table-wrapper {
      overflow-x: auto;
    }

    .data-table {
      border-collapse: collapse;
      background: #fff;
      margin-top: 18px;
      margin-bottom: 30px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
      border-radius: 6px;
      overflow: hidden;
      width: 100%;
      min-width: 400px;
    }

    .data-table th,
    .data-table td {
      border: 1px solid #e0e0e0;
      padding: 10px 16px;
      text-align: left;
    }

    .data-table th {
      background: #005aef;
      color: #fff;
      font-weight: 600;
    }

    .data-table tr:nth-child(even) td {
      background: #f3f6fa;
    }

    .back-link {
      margin: 12px 0 0 0;
    }

    .back-link a {
      font-size: 0.95em;
    }

    .org-list {
      margin: 18px 0 0 0;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .org-card {
      border: 1px solid #e0e0e0;
      border-radius: 6px;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
      background: #fff;
    }

    .org-summary {
      width: 100%;
      display: flex;
      align-items: center;
      gap: 16px;
      justify-content: space-between;
      padding: 16px 20px;
      font-size: 1em;
      font-family: inherit;
      text-align: left;
      flex-wrap: wrap;
    }

    .org-summary-toggle {
      border: none;
      background: transparent;
      cursor: pointer;
    }

    .org-summary-toggle:hover {
      background: #f3f6fa;
    }

    .org-summary-static {
      border: none;
      background: transparent;
      cursor: default;
    }

    .org-summary .org-name {
      font-weight: 600;
      color: #1f2933;
    }

    .org-summary .org-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-left: auto;
      flex-wrap: wrap;
      justify-content: flex-end;
    }

    .org-summary .org-roles {
      color: #4b5563;
      font-size: 0.95em;
    }

    .org-summary .org-count {
      background: #005aef;
      color: #fff;
      border-radius: 999px;
      padding: 4px 12px;
      font-size: 0.9em;
    }

    .org-toggle {
      font-size: 0.9em;
      color: #005aef;
    }

    .org-details,
    .org-details-static {
      border-top: 1px solid #e0e0e0;
      padding: 16px 20px 18px 20px;
    }

    .org-details[hidden] {
      display: none;
    }

    .org-contact {
      margin-bottom: 12px;
    }

    .org-contact:last-child {
      margin-bottom: 0;
    }

    .org-contact-name {
      font-weight: 600;
      margin-bottom: 4px;
    }

    .org-contact-meta {
      font-size: 0.92em;
      color: #444;
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
    }

    .org-contact-meta a {
      color: #005aef;
      text-decoration: none;
    }

    .org-contact-meta a:hover {
      text-decoration: underline;
    }

    .org-role-label {
      font-weight: 500;
    }

    .org-meta-sep {
      color: #9ca3af;
      margin: 0 6px;
    }

    .map-placeholder {
      font-size: 0.95em;
      color: #666;
      padding: 12px;
    }

    .table-note {
      font-size: 0.95em;
      color: #555;
      margin-bottom: 8px;
    }

    @media (max-width: 900px) {
      .provider-layout {
        flex-direction: column;
      }

      .provider-left,
      .provider-right {
        min-width: 100%;
      }
    }
  </style>
</head>
<body>
  <div id="app">
    <div class="page-container">
      <div class="loading">Loading statistics...</div>
    </div>
  </div>

  <script src="../giapi/ol/ol.js"></script>
  <script>
    (function () {
      'use strict';

      var STATS_API_BASE = window.STATS_API_BASE || '../services/support/metadata-stats';
      var DEFAULT_LANGUAGE = 'en';

      var query = parseQueryParams();
      var app = document.getElementById('app');

      if (!query.viewId) {
        renderError('Unexpected: view parameter missing');
        return;
      }

      loadTranslations(query.language || DEFAULT_LANGUAGE)
        .then(function (translations) {
          var translator = createTranslator(query.language || DEFAULT_LANGUAGE, translations);

          if (query.format && query.format.toLowerCase() === 'csv') {
            initiateCsvDownload(query);
            renderMessage(translator('preparing_csv_download', 'Preparing CSV download...'));
            return;
          }

          renderMessage(translator('loading_statistics', 'Loading statistics...'));

          return fetchStatsData(query)
            .then(function (payload) {
              if (!payload) {
                throw new Error('Empty payload');
              }
              renderStatsPage(payload, translator, query);
            })
            .catch(function (error) {
              console.error('[stats] Failed to load data', error);
              renderError(translator('stats_load_failed', 'Unable to load statistics. Please try again later.'));
            });
        })
        .catch(function (error) {
          console.error('[stats] Failed to load translations', error);
          renderError('Unable to load translations.');
        });

      function parseQueryParams() {
        var params = new URLSearchParams(window.location.search);
        return {
          token: trimToNull(params.get('token')) || '',
          viewId: trimToNull(params.get('view')) || '',
          sourceId: trimToNull(params.get('source')) || '',
          format: trimToNull(params.get('format')) || '',
          language: trimToNull(params.get('language')) || ''
        };
      }

      function trimToNull(value) {
        if (value === undefined || value === null) {
          return null;
        }
        var trimmed = String(value).trim();
        return trimmed === '' ? null : trimmed;
      }

      function trimToLower(value) {
        var trimmed = trimToNull(value);
        return trimmed ? trimmed.toLowerCase() : null;
      }

      function escapeHtml(value) {
        if (value === undefined || value === null) {
          return '';
        }
        return String(value)
          .replace(/&/g, '&amp;')
          .replace(/</g, '&lt;')
          .replace(/>/g, '&gt;')
          .replace(/"/g, '&quot;')
          .replace(/'/g, '&#39;');
      }

      function joinWithSeparator(list, separator) {
        if (!list || !list.length) {
          return '';
        }
        var buffer = '';
        for (var i = 0; i < list.length; i++) {
          if (i > 0) {
            buffer += separator;
          }
          buffer += list[i];
        }
        return buffer;
      }

      function compareIgnoreCase(a, b) {
        var left = (a || '').toLowerCase();
        var right = (b || '').toLowerCase();
        if (left < right) {
          return -1;
        }
        if (left > right) {
          return 1;
        }
        var leftOriginal = a || '';
        var rightOriginal = b || '';
        if (leftOriginal < rightOriginal) {
          return -1;
        }
        if (leftOriginal > rightOriginal) {
          return 1;
        }
        return 0;
      }

      function fetchTranslationResource(language) {
        var url = '../gi-portal/lang/' + language + '.json';
        return fetch(url, { cache: 'no-cache' })
          .then(function (response) {
            if (!response.ok) {
              throw new Error('Translation ' + language + ' not available');
            }
            return response.json();
          });
      }

      function loadTranslations(language) {
        var requested = trimToNull(language) || DEFAULT_LANGUAGE;
        var payload = { primary: {}, fallback: {} };

        return fetchTranslationResource(requested)
          .then(function (data) {
            payload.primary = data || {};
          })
          .catch(function () {
            payload.primary = {};
          })
          .then(function () {
            if (requested === DEFAULT_LANGUAGE) {
              return payload;
            }
            return fetchTranslationResource(DEFAULT_LANGUAGE)
              .then(function (data) {
                payload.fallback = data || {};
              })
              .catch(function () {
                payload.fallback = {};
              })
              .then(function () {
                return payload;
              });
          });
      }

      function createTranslator(language, translations) {
        var primary = translations && translations.primary ? translations.primary : {};
        var fallback = translations && translations.fallback ? translations.fallback : {};
        return function translate(key, defaultText) {
          if (primary && primary.hasOwnProperty(key)) {
            return primary[key];
          }
          if (fallback && fallback.hasOwnProperty(key)) {
            return fallback[key];
          }
          return defaultText !== undefined ? defaultText : key;
        };
      }

      function renderMessage(message) {
        app.innerHTML = '<div class="page-container"><p>' + escapeHtml(message) + '</p></div>';
      }

      function renderError(message) {
        app.innerHTML = '<div class="page-container"><p class="error">' + escapeHtml(message) + '</p></div>';
      }

      function buildApiUrl(path, params) {
        if (!STATS_API_BASE) {
          throw new Error('STATS_API_BASE is not configured');
        }
        var base = STATS_API_BASE;
        if (base.charAt(base.length - 1) === '/') {
          base = base.slice(0, -1);
        }
        var normalizedPath = '';
        if (path) {
          normalizedPath = path.charAt(0) === '/' ? path : '/' + path;
        }
        var url = base + normalizedPath;
        if (params) {
          var queryString = '';
          if (typeof params === 'string') {
            queryString = params;
          } else if (params instanceof URLSearchParams) {
            queryString = params.toString();
          } else {
            var searchParams = new URLSearchParams();
            for (var key in params) {
              if (params.hasOwnProperty(key) && params[key] !== undefined && params[key] !== null && params[key] !== '') {
                searchParams.append(key, params[key]);
              }
            }
            queryString = searchParams.toString();
          }
          if (queryString) {
            url += '?' + queryString;
          }
        }
        return url;
      }

      function fetchStatsData(query) {
        var params = new URLSearchParams();
        params.append('view', query.viewId);
        if (query.sourceId) {
          params.append('source', query.sourceId);
        }
        if (query.token) {
          params.append('token', query.token);
        }
        if (query.language) {
          params.append('language', query.language);
        }

        return fetch(buildApiUrl('', params), {
          headers: { 'Accept': 'application/json' },
          credentials: 'include'
        }).then(function (response) {
          if (!response.ok) {
            throw new Error('HTTP ' + response.status);
          }
          return response.json();
        });
      }

      function initiateCsvDownload(query) {
        var params = new URLSearchParams();
        params.append('view', query.viewId);
        if (query.sourceId) {
          params.append('source', query.sourceId);
        }
        if (query.token) {
          params.append('token', query.token);
        }
        if (query.language) {
          params.append('language', query.language);
        }
        params.append('format', 'CSV');
        window.location.href = buildApiUrl('/download', params);
      }

      function renderStatsPage(payload, translator, query) {
        var statsEntries = Array.isArray(payload && payload.stats) ? payload.stats : [];
        if (!query.sourceId) {
          renderSourceList(statsEntries, translator, query);
          return;
        }

        var entry = null;
        for (var i = 0; i < statsEntries.length; i++) {
          var current = statsEntries[i];
          if (!current) {
            continue;
          }
          if (trimToLower(current['source']) === trimToLower(query.sourceId)) {
            entry = current;
            break;
          }
        }
        if (!entry && statsEntries.length) {
          entry = statsEntries[0];
        }

        if (!entry) {
          renderError(translator('provider_not_found', 'Provider not found.'));
          return;
        }

        renderProvider(entry, translator, query);
      }

      function renderSourceList(statsEntries, translator, query) {
        var wrapper = document.createElement('div');
        wrapper.className = 'page-container';

        var title = translator('data_provider_information', 'Data provider information');
        document.title = title;

        var heading = document.createElement('h1');
        heading.textContent = title;
        wrapper.appendChild(heading);

        var prompt = document.createElement('p');
        prompt.textContent = translator('select_provider_prompt', 'Select a provider to view detailed statistics.');
        wrapper.appendChild(prompt);

        var list = document.createElement('ul');
        list.className = 'source-list';

        var unique = {};
        var sources = [];
        for (var i = 0; i < statsEntries.length; i++) {
          var entry = statsEntries[i];
          if (!entry) {
            continue;
          }
          var id = trimToNull(entry['source']);
          var label = trimToNull(entry['source-label']) || id || translator('unknown_value', 'Unknown');
          var key = id ? id.toLowerCase() : label.toLowerCase();
          if (unique[key]) {
            continue;
          }
          unique[key] = true;
          sources.push({
            id: id,
            label: label
          });
        }

        sources.sort(function (a, b) {
          return compareIgnoreCase(a.label, b.label);
        });

        if (!sources.length) {
          var emptyItem = document.createElement('li');
          emptyItem.textContent = translator('no_sources_available', 'No providers available for this view.');
          list.appendChild(emptyItem);
        } else {
          for (var j = 0; j < sources.length; j++) {
            var item = sources[j];
            var link = document.createElement('a');
            link.href = buildSourceUrl(query, item.id || '');
            link.textContent = item.label;

            var listItem = document.createElement('li');
            listItem.appendChild(link);
            list.appendChild(listItem);
          }
        }

        wrapper.appendChild(list);
        app.innerHTML = '';
        app.appendChild(wrapper);
      }

      function buildSourceUrl(query, sourceId) {
        var url = new URL(window.location.href);
        if (sourceId) {
          url.searchParams.set('source', sourceId);
        } else {
          url.searchParams.delete('source');
        }
        url.searchParams.set('view', query.viewId);
        if (query.token) {
          url.searchParams.set('token', query.token);
        } else {
          url.searchParams.delete('token');
        }
        if (query.language) {
          url.searchParams.set('language', query.language);
        } else {
          url.searchParams.delete('language');
        }
        url.searchParams.delete('format');
        return url.pathname + url.search;
      }

      function renderProvider(entry, translator, query) {
        var platformsLabel = translator('platforms', 'Platforms');
        var observedLabel = translator('observed_properties', 'Observed properties');
        var datasetsLabel = translator('datasets', 'Datasets');
        var minTemporalLabel = translator('minimum_temporal_extent', 'Minimum temporal extent');
        var maxTemporalLabel = translator('maximum_temporal_extent', 'Maximum temporal extent');
        var bboxLabel = translator('bbox', 'BBox');
        var altitudeLabel = translator('altitude', 'Altitude');

        var siteCount = entry['site-count'];
        var uniqueAttributeCount = entry['unique-attribute-count'];
        var attributeCount = entry['attribute-count'];
        var timeSeriesCount = entry['timeseries-count'];
        var begin = entry['begin'];
        var end = entry['end'];
        var west = entry['west'];
        var south = entry['south'];
        var east = entry['east'];
        var north = entry['north'];
        var minElevation = entry['minimimum-elevation'] !== undefined ? entry['minimimum-elevation'] : entry['minimum-elevation'];
        var maxElevation = entry['maximum-elevation'];

        var statsItems = [];
        statsItems.push('<li><b># ' + escapeHtml(platformsLabel) + ':</b> ' + formatValue(siteCount) + '</li>');
        statsItems.push('<li><b># ' + escapeHtml(observedLabel) + ':</b> ' + formatValue(attributeCount) + '</li>');
        statsItems.push('<li><b># ' + escapeHtml(datasetsLabel) + ':</b> ' + formatValue(timeSeriesCount) + '</li>');
        statsItems.push('<li><b>' + escapeHtml(translator('unique_attribute_count', 'Unique observed properties')) + ':</b> ' + formatValue(uniqueAttributeCount) + '</li>');
        statsItems.push('<li><b>' + escapeHtml(minTemporalLabel) + ':</b> ' + formatValue(begin) + '</li>');
        statsItems.push('<li><b>' + escapeHtml(maxTemporalLabel) + ':</b> ' + formatValue(end) + '</li>');

        var bboxValues = [formatValue(west), formatValue(south), formatValue(east), formatValue(north)];
        statsItems.push('<li><b>' + escapeHtml(bboxLabel) + ':</b> ' + bboxValues.join(', ') + '</li>');

        var altitudeValues = formatValue(minElevation) + ' / ' + formatValue(maxElevation);
        statsItems.push('<li><b>' + escapeHtml(altitudeLabel) + ':</b> ' + altitudeValues + '</li>');

        var attributeStats = Array.isArray(entry['attribute-stats']) ? entry['attribute-stats'] : [];
        if (attributeStats.length) {
          var observedList = ['<li><b>' + escapeHtml(observedLabel) + ':</b><ul>'];
          for (var a = 0; a < attributeStats.length; a++) {
            var attr = attributeStats[a] || {};
            var attrLabel = trimToNull(attr.term) || trimToNull(attr.label) || translator('unknown_value', 'Unknown');
            var attrCount = attr.count !== undefined ? attr.count : attr.freq;
            observedList.push('<li>' + escapeHtml(attrLabel) + ' (' + formatValue(attrCount) + ')</li>');
          }
          observedList.push('</ul></li>');
          statsItems.push(observedList.join(''));
        }

        var page = [];
        page.push('<div class="page-container">');

        var pageTitle = translator('data_provider_information', 'Data provider information');
        var providerLabel = trimToNull(entry['source-label']) || trimToNull(entry['source']) || translator('provider', 'Provider');
        document.title = pageTitle + ' - ' + providerLabel;

        page.push('<h1>' + escapeHtml(providerLabel) + '</h1>');

        var backUrl = buildSourceUrl(query, '');
        page.push('<p class="back-link"><a href="' + escapeHtml(backUrl) + '">&#8592; ' + escapeHtml(translator('back_to_sources', 'Back to providers')) + '</a></p>');

        page.push('<div class="provider-layout">');
        page.push('<div class="provider-left">');
        page.push('<h2>' + escapeHtml(translator('provider_statistics', 'Provider statistics')) + '</h2>');
        page.push('<ul class="stats-list">' + statsItems.join('') + '</ul>');
        page.push('<div id="organizations-container"></div>');
        page.push('</div>');

        page.push('<div class="provider-right">');
        page.push('<div id="provider-map" class="provider-map"></div>');
        page.push('<div class="map-credit">© <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">OpenStreetMap contributors</a></div>');
        page.push('</div>');
        page.push('</div>');

        page.push('<h1>' + escapeHtml(translator('sample_platforms', 'Sample platforms')) + '</h1>');
        page.push('<div class="table-wrapper">');
        page.push('<table class="data-table">');
        page.push('<thead><tr>');
        page.push('<th>' + escapeHtml(translator('monitoring_point', 'Monitoring point')) + '</th>');
        page.push('<th>' + escapeHtml(translator('latitude', 'Latitude')) + '</th>');
        page.push('<th>' + escapeHtml(translator('longitude', 'Longitude')) + '</th>');
        page.push('<th>' + escapeHtml(translator('elevation', 'Elevation')) + '</th>');
        page.push('</tr></thead>');
        page.push('<tbody id="platform-table-body"></tbody>');
        page.push('</table>');
        page.push('</div>');

        page.push('</div>');

        app.innerHTML = page.join('');

        var orgContainer = document.getElementById('organizations-container');
        renderOrganizations(orgContainer, entry['organization-stats'] || [], translator);

        var tableBody = document.getElementById('platform-table-body');
        renderSamplePlatforms(tableBody, [], translator, query);

        createProviderMap({
          bbox: {
            west: west,
            south: south,
            east: east,
            north: north
          }
        }, translator, query);
      }

      function formatValue(value) {
        if (value === undefined || value === null || value === '') {
          return '-';
        }
        return value;
      }

      function renderOrganizations(container, frequencyItems, translator) {
        if (!container) {
          return;
        }
        var showLabel = translator('show_contributors', 'Show contributors');
        var hideLabel = translator('hide_contributors', 'Hide contributors');
        var data = aggregateOrganizations(frequencyItems, translator);

        if (!data.length) {
          container.innerHTML = '<h3>' + escapeHtml(translator('involved_organizations', 'Involved organizations')) + '</h3><p>' + escapeHtml(translator('no_organizations_available', 'No organization data available.')) + '</p>';
          return;
        }

        var html = ['<h3>' + escapeHtml(translator('involved_organizations', 'Involved organizations')) + '</h3>', '<div class="org-list">'];
        for (var i = 0; i < data.length; i++) {
          var org = data[i];
          var detailsId = 'org-details-' + i;
          var summaryTag = org.showToggle ? 'button' : 'div';
          var summaryAttrs = '';
          if (org.showToggle) {
            summaryAttrs = ' type="button" data-target="' + detailsId + '" aria-controls="' + detailsId + '" aria-expanded="false"';
          }
          var summaryClass = 'org-summary ' + (org.showToggle ? 'org-summary-toggle' : 'org-summary-static');

          html.push('<div class="org-card">');
          html.push('<' + summaryTag + summaryAttrs + ' class="' + summaryClass + '">');
          html.push('<span class="org-name">' + escapeHtml(org.name) + '</span>');
          html.push('<span class="org-meta"><span class="org-roles">' + escapeHtml(org.rolesSummary) + '</span><span class="org-count">' + org.totalCount + '</span></span>');
          if (org.showToggle) {
            html.push('<span class="org-toggle">' + escapeHtml(showLabel) + '</span>');
          }
          html.push('</' + summaryTag + '>');
          if (org.showToggle) {
            html.push('<div class="org-details" id="' + detailsId + '" hidden>');
            for (var j = 0; j < org.contributors.length; j++) {
              var contributor = org.contributors[j];
              var contributorName = contributor.name ? escapeHtml(contributor.name) : escapeHtml(translator('unnamed_contributor', 'Unnamed contributor'));
              html.push('<div class="org-contact">');
              html.push('<div class="org-contact-name">' + contributorName + ' (' + contributor.count + ')</div>');

              var metaParts = [];
              if (contributor.roleSummary) {
                metaParts.push('<span class="org-role-label">' + escapeHtml(contributor.roleSummary) + '</span>');
              }
              if (contributor.emails.length) {
                var emailFragments = [];
                for (var e = 0; e < contributor.emails.length; e++) {
                  var emailValue = contributor.emails[e];
                  var emailHref = 'mailto:' + encodeURIComponent(emailValue);
                  emailFragments.push('<a href="' + emailHref + '">' + escapeHtml(emailValue) + '</a>');
                }
                metaParts.push(emailFragments.join(', '));
              }
              if (contributor.homepages.length) {
                var homepageFragments = [];
                for (var h = 0; h < contributor.homepages.length; h++) {
                  var homepageValue = contributor.homepages[h];
                  homepageFragments.push('<a href="' + escapeHtml(homepageValue) + '" target="_blank" rel="noopener">Website</a>');
                }
                metaParts.push(homepageFragments.join(', '));
              }
              if (metaParts.length) {
                html.push('<div class="org-contact-meta">' + joinWithSeparator(metaParts, "<span class='org-meta-sep'>&#8226;</span>") + '</div>');
              }
              html.push('</div>');
            }
            html.push('</div>');
          }
          html.push('</div>');
        }
        html.push('</div>');

        container.innerHTML = html.join('');
        initializeOrgToggles(container, showLabel, hideLabel);
      }

      function initializeOrgToggles(container, showLabel, hideLabel) {
        var toggles = container.querySelectorAll('.org-summary-toggle');
        for (var i = 0; i < toggles.length; i++) {
          var button = toggles[i];
          if (button.dataset.bound === 'true') {
            continue;
          }
          button.dataset.bound = 'true';
          button.addEventListener('click', function (event) {
            var btn = event.currentTarget;
            var targetId = btn.getAttribute('data-target');
            var details = document.getElementById(targetId);
            var expanded = btn.getAttribute('aria-expanded') === 'true';
            btn.setAttribute('aria-expanded', (!expanded).toString());
            var label = btn.querySelector('.org-toggle');
            if (label) {
              label.textContent = expanded ? showLabel : hideLabel;
            }
            if (details) {
              if (expanded) {
                details.setAttribute('hidden', 'hidden');
              } else {
                details.removeAttribute('hidden');
              }
            }
          });
        }
      }

      function aggregateOrganizations(frequencyItems, translator) {
        var results = [];
        if (!frequencyItems || !frequencyItems.length) {
          return results;
        }

        var unknownOrganization = translator('unknown_organization', 'Unknown organization');
        var roleSingle = translator('role_label_single', 'Role');
        var rolePlural = translator('role_label_plural', 'Roles');
        var unspecifiedRole = translator('unspecified_role', '(unspecified role)');

        var map = {};

        for (var i = 0; i < frequencyItems.length; i++) {
          var item = frequencyItems[i];
          if (!item) {
            continue;
          }
          var props = item.properties || item.nestedProperties || {};
          var orgName = trimToNull(props.orgName) || trimToNull(item.label) || trimToNull(item.term) || unknownOrganization;
          var orgKey = orgName.toLowerCase();

          if (!map[orgKey]) {
            map[orgKey] = {
              name: orgName,
              totalCount: 0,
              roles: {},
              contributors: {}
            };
          }

          var entry = map[orgKey];
          var freqValueRaw = item.count !== undefined ? item.count : item.freq;
          var freqValue = Number(freqValueRaw);
          if (!isNaN(freqValue)) {
            entry.totalCount += freqValue;
          }

          var role = trimToNull(props.role);
          if (role) {
            entry.roles[role] = true;
          }

          var contributorKey = [
            trimToLower(props.individualName) || '',
            trimToLower(props.email) || '',
            trimToLower(props.homePageURL || props.homepage) || ''
          ].join('|');

          if (!entry.contributors[contributorKey]) {
            entry.contributors[contributorKey] = {
              name: trimToNull(props.individualName),
              count: 0,
              roles: {},
              emails: {},
              homepages: {}
            };
          }

          var contributor = entry.contributors[contributorKey];
          if (!isNaN(freqValue)) {
            contributor.count += freqValue;
          }
          if (role) {
            contributor.roles[role] = true;
          }
          var email = trimToNull(props.email);
          if (email) {
            contributor.emails[email] = true;
          }
          var homepage = trimToNull(props.homePageURL || props.homepage);
          if (homepage) {
            contributor.homepages[homepage] = true;
          }
        }

        for (var key in map) {
          if (!map.hasOwnProperty(key)) {
            continue;
          }
          var orgEntry = map[key];
          var roleKeys = Object.keys(orgEntry.roles);
          roleKeys.sort(compareIgnoreCase);

          var rolesSummary;
          if (!roleKeys.length) {
            rolesSummary = unspecifiedRole;
          } else if (roleKeys.length === 1) {
            rolesSummary = roleSingle + ': ' + roleKeys[0];
          } else {
            rolesSummary = rolePlural + ': ' + joinWithSeparator(roleKeys, ', ');
          }

          var contributors = [];
          for (var contributorKey in orgEntry.contributors) {
            if (!orgEntry.contributors.hasOwnProperty(contributorKey)) {
              continue;
            }
            var contributorEntry = orgEntry.contributors[contributorKey];
            var roleList = Object.keys(contributorEntry.roles);
            roleList.sort(compareIgnoreCase);
            var emailList = Object.keys(contributorEntry.emails);
            emailList.sort(compareIgnoreCase);
            var homepageList = Object.keys(contributorEntry.homepages);
            homepageList.sort(compareIgnoreCase);

            var hasDetails = !!trimToNull(contributorEntry.name) || roleList.length || emailList.length || homepageList.length;
            if (!hasDetails) {
              continue;
            }

            var contributorRoleSummary = null;
            if (roleList.length === 1) {
              contributorRoleSummary = roleSingle + ': ' + roleList[0];
            } else if (roleList.length > 1) {
              contributorRoleSummary = rolePlural + ': ' + joinWithSeparator(roleList, ', ');
            }

            contributors.push({
              name: contributorEntry.name,
              count: contributorEntry.count,
              roleSummary: contributorRoleSummary,
              emails: emailList,
              homepages: homepageList
            });
          }

          contributors.sort(function (a, b) {
            var compare = compareIgnoreCase(a.name, b.name);
            if (compare !== 0) {
              return compare;
            }
            compare = compareIgnoreCase(a.roleSummary, b.roleSummary);
            if (compare !== 0) {
              return compare;
            }
            var emailA = a.emails.length ? a.emails[0] : '';
            var emailB = b.emails.length ? b.emails[0] : '';
            return compareIgnoreCase(emailA, emailB);
          });

          results.push({
            name: orgEntry.name,
            totalCount: orgEntry.totalCount,
            rolesSummary: rolesSummary,
            contributors: contributors,
            showToggle: contributors.length > 0
          });
        }

        results.sort(function (a, b) {
          return compareIgnoreCase(a.name, b.name);
        });

        return results;
      }

      function renderSamplePlatforms(tbody, platforms, translator, query) {
        if (!tbody) {
          return;
        }
        while (tbody.firstChild) {
          tbody.removeChild(tbody.firstChild);
        }
        if (!platforms || !platforms.length) {
          var emptyRow = document.createElement('tr');
          var emptyCell = document.createElement('td');
          emptyCell.colSpan = 4;
          emptyCell.textContent = translator('no_sample_platforms', 'No sample platforms available.');
          emptyRow.appendChild(emptyCell);
          tbody.appendChild(emptyRow);
          return;
        }
        for (var i = 0; i < platforms.length; i++) {
          var platform = platforms[i] || {};
          var row = document.createElement('tr');

          var titleCell = document.createElement('td');
          var title = platform.title || translator('unknown_value', 'Unknown');
          var link = platform.link || buildStationLink(query, platform.platformId);
          if (link) {
            var anchor = document.createElement('a');
            anchor.href = link;
            anchor.target = '_blank';
            anchor.textContent = title;
            titleCell.appendChild(anchor);
          } else {
            titleCell.textContent = title;
          }
          row.appendChild(titleCell);

          var latCell = document.createElement('td');
          latCell.textContent = formatValue(platform.north);
          row.appendChild(latCell);

          var lonCell = document.createElement('td');
          lonCell.textContent = formatValue(platform.east);
          row.appendChild(lonCell);

          var elevCell = document.createElement('td');
          elevCell.textContent = formatValue(platform.elevation);
          row.appendChild(elevCell);

          tbody.appendChild(row);
        }
      }

      function buildStationLink(query, platformId) {
        if (!platformId || !query.viewId) {
          return '';
        }
        return '../services/view/' + encodeURIComponent(query.viewId) + '/bnhs/station/' + encodeURIComponent(platformId) + '/';
      }

      function createProviderMap(mapData, translator, query) {
        var mapContainer = document.getElementById('provider-map');
        if (!mapContainer) {
          return;
        }
        if (!window.ol) {
          mapContainer.innerHTML = '<div class="map-placeholder">' + escapeHtml(translator('map_library_not_available', 'Map library is not available.')) + '</div>';
          return;
        }

        var bbox = mapData && mapData.bbox ? mapData.bbox : {};
        var west = Number(bbox.west);
        var east = Number(bbox.east);
        var north = Number(bbox.north);
        var south = Number(bbox.south);
        if (isNaN(west) || isNaN(east) || isNaN(north) || isNaN(south)) {
          mapContainer.innerHTML = '<div class="map-placeholder">' + escapeHtml(translator('map_data_unavailable', 'Map data unavailable.')) + '</div>';
          return;
        }

        mapContainer.innerHTML = '';

        var layers = [
          new ol.layer.Tile({
            source: new ol.source.OSM()
          })
        ];

        if (query.viewId) {
          var wmsPath = '/gs-service/services/essi';
          if (query.token) {
            wmsPath += '/token/' + encodeURIComponent(query.token);
          }
          wmsPath += '/view/' + encodeURIComponent(query.viewId) + '/wms-cluster';
          var wmsUrl = wmsPath;
          if (query.sourceId) {
            wmsUrl += '?sources=' + encodeURIComponent(query.sourceId);
          }
          layers.push(new ol.layer.Tile({
            source: new ol.source.TileWMS({
              url: wmsUrl,
              params: {
                LAYERS: query.viewId,
                TILED: true
              },
              transition: 0
            })
          }));
        }

        var centerLon = (west + east) / 2;
        var centerLat = (south + north) / 2;

        var map = new ol.Map({
          target: mapContainer,
          layers: layers,
          view: new ol.View({
            center: ol.proj.fromLonLat([centerLon, centerLat]),
            zoom: 6
          }),
          controls: []
        });

        var extent = ol.proj.transformExtent([west, south, east, north], 'EPSG:4326', 'EPSG:3857');
        map.getView().fit(extent, { padding: [20, 20, 20, 20], maxZoom: 12 });
      }

    })();
  </script>
</body>
</html>

