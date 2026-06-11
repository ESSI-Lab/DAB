/**
 * SeaDataNet broker view: strip unsupported OpenSearch params and harden discover JSON handling.
 */
(function() {
	if (!window.GIAPI || typeof GIAPI.query !== 'function') {
		return;
	}

	var unsupportedParams = {
		kwdOrBbox: true,
		targetId: true,
		from: true,
		until: true,
		subj: true,
		rela: true,
		callback: true,
		_: true
	};

	function sanitizeOpenSearchUrl(url) {
		if (!url || url.indexOf('opensearch/query') === -1) {
			return url;
		}

		try {
			var parsed = new URL(url, window.location.href);
			var keys = [];
			parsed.searchParams.forEach(function(_value, key) {
				keys.push(key);
			});
			keys.forEach(function(key) {
				if (unsupportedParams[key]) {
					parsed.searchParams.delete(key);
				}
			});
			return parsed.pathname + '?' + parsed.searchParams.toString();
		} catch (e) {
			return url
				.replace(/([?&])(kwdOrBbox|targetId|from|until|subj|rela|callback|_)=[^&]*/g, '$1')
				.replace(/[?&]$/, '')
				.replace(/\?&/, '?');
		}
	}

	function isDiscoverQuery(url) {
		if (!url || url.indexOf('opensearch/query') === -1) {
			return false;
		}
		if (url.indexOf('parents=') !== -1) {
			return false;
		}
		if (url.indexOf('identifier=') !== -1 && url.indexOf('st=') === -1 && url.indexOf('si=') === -1) {
			return false;
		}
		return true;
	}

	function normalizeDiscoverData(data) {
		if (typeof data === 'string') {
			try {
				data = JSON.parse(data);
			} catch (e) {
				return data;
			}
		}
		if (data && typeof data === 'object' && !data.resultSet) {
			var keys = Object.keys(data);
			if (keys.length === 1 && data[keys[0]] && data[keys[0]].resultSet) {
				data = data[keys[0]];
			}
		}
		if (data && !data.reports) {
			data.reports = [];
		}
		return data;
	}

	var originalQuery = GIAPI.query;
	GIAPI.query = function() {
		return sanitizeOpenSearchUrl(originalQuery.apply(this, arguments));
	};

	var originalDAB = GIAPI.DAB;
	GIAPI.DAB = function() {
		var dab = originalDAB.apply(this, arguments);
		var originalDiscover = dab.discover;

		dab.discover = function(onResponse, constraints, options, onStatus) {
			var wrappedOnResponse = function(response) {
				try {
					if (onResponse) {
						onResponse.apply(this, arguments);
					}
				} finally {
					if (GIAPI.UI_Utils && GIAPI.UI_Utils.discoverDialog('isOpen')) {
						GIAPI.UI_Utils.discoverDialog('close');
					}
				}
			};
			return originalDiscover.call(dab, wrappedOnResponse, constraints, options, onStatus);
		};

		return dab;
	};

	if (!window.jQuery) {
		return;
	}

	jQuery.ajaxPrefilter(function(options) {
		if (!options.url || options.url.indexOf('opensearch/query') === -1) {
			return;
		}

		options.url = sanitizeOpenSearchUrl(options.url);

		var resolved;
		try {
			resolved = new URL(options.url, window.location.href);
		} catch (e) {
			return;
		}

		if (resolved.origin === window.location.origin) {
			options.dataType = 'json';
			options.crossDomain = false;
			delete options.jsonp;
			delete options.jsonpCallback;
		}
	});

	var originalAjax = jQuery.ajax;
	jQuery.ajax = function(url, options) {
		if (typeof url === 'object') {
			options = url;
			url = undefined;
		}
		options = options || {};

		if (options.url && isDiscoverQuery(options.url)) {
			options.url = sanitizeOpenSearchUrl(options.url);
			options.dataType = 'json';
			options.crossDomain = false;
			delete options.jsonp;
			delete options.jsonpCallback;

			var userSuccess = options.success;
			var userComplete = options.complete;
			var handled = false;

			options.success = function(data, textStatus, jqXHR) {
				handled = true;
				data = normalizeDiscoverData(data);
				if (userSuccess) {
					try {
						return userSuccess.call(this, data, textStatus, jqXHR);
					} catch (err) {
						GIAPI.logger.log('discover success handler error: ' + err, 'error');
						if (options.error) {
							options.error(jqXHR, 'parsererror', err);
						}
					}
				}
			};

			options.complete = function(jqXHR, textStatus) {
				if (!handled && jqXHR && jqXHR.status === 200 && jqXHR.responseText && userSuccess) {
					try {
						var data = normalizeDiscoverData(jqXHR.responseText);
						if (data && data.resultSet) {
							handled = true;
							userSuccess.call(this, data, 'success', jqXHR);
						}
					} catch (err) {
						GIAPI.logger.log('discover complete parse error: ' + err, 'error');
					}
				}
				if (userComplete) {
					userComplete.apply(this, arguments);
				}
			};
		}

		return originalAjax.call(jQuery, url, options);
	};
})();
