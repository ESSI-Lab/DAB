/**
 * This module allows to to refine the content of a <a href="../classes/ResultSet.html" target=_blank>result set</a> by means of the <a href="../classes/Refiner.html#onlprot">Refiner</a> and 
 * <a href="../classes/TermFrequency.html" target=_blank>TermFrequency</a> objects
 * 
 * @main
 * @module ResultSet
 * @submodule Refinement
  **/

/**
 * This object is <a href="../classes/ResultSet.html#resSetRef">optionally provided</a> as <a href="../classes/ResultSet.html#termFrequency">property of a result set</a>.<br>
 *  It allows to {{#crossLink "TermFrequency/refine:method"}}refine{{/crossLink}} the related {{#crossLink "ResultSet"}}result set{{/crossLink}} using as constraints one or more {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}}
 *  related to a <i>term frequency target</i>.
 * The <a href="../classes/DAB.html#termfreq">available</a> term frequency targets are:
 * <ul>
 *   <li><b>keyword</b>: frequency at which the <a href="../classes/Report.html#repkwd">keyword</a> field items appear in a {{#crossLink "Report"}}node report{{/crossLink}}</li>
 *   <li><b>format</b>: frequency at which the <a href="../classes/Report.html#repfrmt">format</a> field items appear in a {{#crossLink "Report"}}node report{{/crossLink}}</li>
 *   <li><b>protocol</b>: frequency at which the <a href="../classes/OnlineInfo.html#onlprot">protocol</a> field items appear in a {{#crossLink "Report"}}node report{{/crossLink}}</li>
 *   <li><b>source</b>: frequency at which the {{#crossLink "GINode"}}nodes{{/crossLink}} of the {{#crossLink "ResultSet"}}result set{{/crossLink}} own to a 
 *   {{#crossLink "DABSource"}}source{{/crossLink}}</li>
 * </ul>
 * 
 * <pre><code>// creates a new DAB instance with the given endpoint
var dab = GIAPI.DAB('https://api.geodab.eu/dab');<br>
// set to true after the first refinement
var refined = false;<br>
// defines discover response callback function
 var onResponse = function(response){<br>     
	var resultSet = response[0];<br>    
	// retrieves the TermFrequency object
	var tf = resultSet.termFrequency;<br>
	// retrieves the term frequency items of  
	// all the available term frequency targets
	var kwdTerms = tf.items('keyword');
	var frmTerms = tf.items('format');
	var proTerms = tf.items('protocol');
	var srcTerms = tf.items('source');<br>
	if(refined){// exit
		return;
	}<br>
	// checks the first term of the keyword target
	tf.checkItems('keyword',[ kwdTerms[0] ]);<br>    
	// refines the discover; the new result set contains only 
	// nodes matching the first term of the keyword target
	tf.refine();<br>   
	refined = true;
};<br>
// setting the termFrequency option
var options = {  
	"termFrequency": "keyword,format,protocol,source"
};<br>
// starts discover
dab.discover(onResponse, options);
 * </pre></code>
 *  
 * This API provides a {{#crossLink "TermFrequencyWidget"}}widget{{/crossLink}} which allows to refine the {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}
 * using check boxes to select the term frequency items.<br>
 * 
 * See also {{#crossLink "TermFrequencyWidget"}}{{/crossLink}}
 *
 * @class TermFrequency
 **/
import { GIAPI } from '../../core/GIAPI.js';

GIAPI.TermFrequency = function(dabNode, cnstr, options, onStatus, onResponse, termFrequency, reset) {

	var tf = {};
	tf._id = 'termFrequency_' + GIAPI.random();
	tf._cnstr = GIAPI.clone(cnstr);

	var targets = [];
	if (!tf._cnstr) {
		tf._cnstr = {};
	}

	if (!GIAPI.tfHelper.checkedItems || reset) {
		GIAPI.tfHelper.checkedItems = {};
	}

	for (let p in termFrequency) {
		targets.push(p);
	}

	// Preserve the order specified in options.termFrequency (e.g., from config.filters),
	// falling back to alphabetical order for any targets not listed there.
	if (options && typeof options.termFrequency === 'string') {
		var configuredOrder = options.termFrequency.split(',').map(function(v) {
			return v.trim();
		}).filter(function(v) {
			return v.length > 0;
		});

		// Helper to resolve the index of a target name in the configured order,
		// handling aliases like attributeURI/observedPropertyURI.
		var getIndexForTarget = function(name) {
			var idx = configuredOrder.indexOf(name);
			if (idx === -1) {
				// Treat attributeURI and observedPropertyURI as synonyms for ordering,
				// since the backend uses observedPropertyURI while configs may use attributeURI.
				if (name === 'observedPropertyURI') {
					idx = configuredOrder.indexOf('attributeURI');
				} else if (name === 'attributeURI') {
					idx = configuredOrder.indexOf('observedPropertyURI');
				}
			}
			return idx;
		};

		targets.sort(function(a, b) {
			var ia = getIndexForTarget(a);
			var ib = getIndexForTarget(b);

			if (ia === -1 && ib === -1) {
				// Neither target is in the configured list: keep alphabetical order between them
				return a.localeCompare(b);
			}
			if (ia === -1) {
				// 'a' is not configured: it goes after any configured targets
				return 1;
			}
			if (ib === -1) {
				// 'b' is not configured: it goes after any configured targets
				return -1;
			}
			// Both are configured: keep the order from config.filters/options.termFrequency
			return ia - ib;
		});
	} else {
		// Default behavior: alphabetical order
		targets.sort();
	}
	
	/**
	 * Checks the {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}} of the given term frequency <code>target</code>. 
	 * Once the {{#crossLink "TermFrequencyItem"}}items{{/crossLink}} are checked,
	 * the {{#crossLink "TermFrequency/refine:method"}}{{/crossLink}} method is ready to be called.<br>
	 * 
	 * See also {{#crossLink "TermFrequency/refine:method"}}{{/crossLink}} method
	 *          
	 * @param {String} target
	 * @param {TermFrequencyItem[]} targetItems
	 *
	 * @method checkItems
	 */
	tf.checkItems = function(target, targetItems) {

		var items = tf.items(target);
		var checkedItems = GIAPI.tfHelper.checkedItems[target];
		var found = false;

		// checked item deselected
		if (checkedItems && targetItems.length < checkedItems.length) {

			for (var i = 0; i < checkedItems.length; i++) {

				for (var j = 0; j < targetItems.length; j++) {

					var chkItem = checkedItems[i];
					var trgItem = targetItems[j];

					if (chkItem.term === trgItem.term) {
						chkItem.veryfied = true;
					}
				};
			};

			var temp = [];

			for (var i = 0; i < checkedItems.length; i++) {

				if (checkedItems[i].veryfied) {

					var item = checkedItems[i];
					delete (item.veryfied);

					temp.push(item);
				}
			}

			GIAPI.tfHelper.checkedItems[target] = temp;

		} else {

			// new item selected
			for (var i = 0; i < items.length && !found; i++) {

				for (var j = 0; j < targetItems.length; j++) {

					var item = items[i];
					var trgItem = targetItems[j];

					if (item.term === trgItem.term && !tf.isCheckedItem(target, item)) {

						if (!GIAPI.tfHelper.checkedItems[target]) {
							GIAPI.tfHelper.checkedItems[target] = [];
						}

						GIAPI.tfHelper.checkedItems[target].push(item);
						found = true;
					}
				};
			};
		}

		// console.log(JSON.stringify(GIAPI.tfHelper.checkedItems, null, 4));
	};

	/**
	 * Refines the related {{#crossLink "ResultSet"}}result set{{/crossLink}} by adding constraints basing on the
	 * currently checked {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}}.<br>
	 * 
	 * See also {{#crossLink "TermFrequency/checkItems:method"}}{{/crossLink}} method.<br>
	 * See also {{#crossLink "TermFrequency/items:method"}}{{/crossLink}} method
	 *
	 * @method refine
	 */
	tf.refine = function() {

		for (var i = 0; i < tf.targets().length; i++) {

			var target = tf.targets()[i];

			delete (tf._cnstr[target]);

			var checkedItems = GIAPI.tfHelper.checkedItems[target];
			if (checkedItems) {
				var values = [];
				for (var j = 0; j < checkedItems.length; j++) {

					var term = checkedItems[j].sourceId ? checkedItems[j].sourceId : checkedItems[j].term;

					if (target == 'score') {
						term = term.replace(' - ', ',');
						term = '[' + term + ']';
					}

					values.push(term);
				};

				tf._cnstr[target] = values;
			}
		};

		// console.log(JSON.stringify(constraints, null, 4));

		// signals that the discover is started from this object (not from the DAB)
		tf._cnstr.tfDiscover = true;

		var _onResponse = function(response) {

			var termFreq = response[0].termFrequency;

			// ---------------------------------------------------------------
			// ensures that all the checked items have the updated frequencies
			// 
			//
			for (var trg = 0; trg < termFreq.targets().length; trg++) {

				var curTarget = termFreq.targets()[trg];

				var checkedItems = termFreq.checkedItems(curTarget);

				if (checkedItems) {

					for (var chk = 0; chk < checkedItems.length; chk++) {

						var curChk = termFreq.checkedItems(curTarget)[chk];

						for (var it = 0; it < termFreq.items(curTarget).length; it++) {

							var curItem = termFreq.items(curTarget)[it];

							if (curChk.term === curItem.term) {

								curChk.freq = curItem.freq;
							}
						}
					}
				}
			}

			onResponse.apply(this, [response]);
		};

		_onResponse._origin = 'termFrequency';

		dabNode.discover(_onResponse, tf._cnstr, options, onStatus);

		// Preserve existing sources selection from the main constraints when no term-frequency
		// source filter is applied. Only override if a TF 'source' constraint has been built.
		if (tf._cnstr.source) {
			tf._cnstr.sources = tf._cnstr.source;
		}

		GIAPI.search.resultsMapWidget.updateWMSClusterLayers(tf._cnstr);

	};

	/**
	 * Clear all the checked {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}} related to the
	 * given term frequency <code>target</code>.<br>
	 * 
	 * See also {{#crossLink "TermFrequency/checkItems:method"}}{{/crossLink}} method
	 *
	 * @method clearCheckedItems
	 *
	 * @param {String} target a term frequency target. Available targets are:
	 * <ul>
	 *   <li><b>keyword</b> </li>
	 *   <li><b>format</b> </li>
	 *   <li><b>source</b> </li>
	 *   <li><b>protocol</b> </li>
	 * </ul> 
	 */
	tf.clearCheckedItems = function(target) {

		GIAPI.tfHelper.checkedItems[target] = null;
	};

	/**
	 * Return an array of all the checked {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}} related to the
	 * given term frequency <code>target</code>.<br>
	 * 
	 * See also {{#crossLink "TermFrequency/checkItems:method"}}{{/crossLink}} method
	 *
	 * @method checkedItems
	 *
	 * @param {String} target a term frequency target. Available targets are:
	 * <ul>
	 *   <li><b>keyword</b> </li>
	 *   <li><b>format</b> </li>
	 *   <li><b>source</b> </li>
	 *   <li><b>protocol</b> </li>
	 * </ul> 
	 * @return {TermFrequencyItem[]} array of checked {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}}
	 */
	tf.checkedItems = function(target) {

		return GIAPI.tfHelper.checkedItems[target];
	};

	/**
	 * Return <code>true</code> if the given {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}} related to the
	 * given term frequency <code>target</code> is checked.<br>
	 * 
	 * See also {{#crossLink "TermFrequency/checkItems:method"}}{{/crossLink}} method
	 *
	 * @method isCheckedItem
	 *
	 * @param {String} target a term frequency target. Available targets are:
	 * <ul>
	 *   <li><b>keyword</b> </li>
	 *   <li><b>format</b> </li>
	 *   <li><b>source</b> </li>
	 *   <li><b>protocol</b> </li>
	 * </ul> 
	 * @param {TermFrequencyItem} item the {{#crossLink "TermFrequencyItem"}}term frequency item{{/crossLink}} to check
	 */
	tf.isCheckedItem = function(target, item) {

		var items = GIAPI.tfHelper.checkedItems[target];
		if (items) {

			for (var i = 0; i < items.length; i++) {

				if (items[i].term === item.term) {
					return true;
				}
			};
		}

		return false;
	};

	/**
	 * Return an array of all the available term frequency targets. Available targets are:
	 * <ul>
	 *   <li><b>keyword</b> </li>
	 *   <li><b>format</b> </li>
	 *   <li><b>source</b> </li>
	 *   <li><b>protocol</b> </li>
	 * </ul>
	 *
	 * See also <a href="../classes/DAB.html#termfreq">termFrequency option</a>
	 *
	 * @method targets
	 *
	 * @return {String[]} array of all the available term frequency targets
	 */
	tf.targets = function() {

		return targets;
	};

	/**
	 * Returns the number of all the available term frequency targets.<br>
	 * 
	 * See also {{#crossLink "TermFrequency/targets:method"}}{{/crossLink}} method
	 *
	 * @method targetsCount
	 *
	 * @return {Integer} the number of all the available term frequency targets
	 */
	tf.targetsCount = function() {

		return targets.length;
	};

	/**
	 * Returns an array of all the available {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}} related to the
	 * given <code>target</code>
	 *
	 * @method items
	 *
	 * @param {String} target a term frequency target. Available targets are:
	 * <ul>
	 *   <li><b>keyword</b> </li>
	 *   <li><b>format</b> </li>
	 *   <li><b>source</b> </li>
	 *   <li><b>protocol</b> </li>
	 * </ul> 
	 * @return {TermFrequencyItem[]} array of all the available {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}} related to the
	 * given <code>target</code>
	 */
	tf.items = function(target) {

		return termFrequency[target];
	};

	/**
	 * Returns the number of all the available {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}}
	 * related to the given <code>target</code>
	 *
	 * @method itemsCount
	 * 
	 * @param {String} target a term frequency target. Available targets are:
	 * <ul>
	 *   <li><b>keyword</b> </li>
	 *   <li><b>format</b> </li>
	 *   <li><b>source</b> </li>
	 *   <li><b>protocol</b> </li>
	 * </ul> 
	 * 
	 * @return {Integer} the number of all the available {{#crossLink "TermFrequencyItem"}}term frequency items{{/crossLink}}
	 * related to the given <code>target</code>
	 */
	tf.itemsCount = function(target) {

		return termFrequency[target].length;
	};

	return tf;
};
