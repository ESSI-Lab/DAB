/**
 * This module contains the <b>API entry point</b> {{#crossLink "DAB"}}{{/crossLink}} and other core objects.
 *
 * <ul style="margin-left:-25px;"><li> Core objects use <a href="http://jquery.com/">JQuery</a> and requires the following dependency:</li></ul>
 * <pre><code>
 * &lt;script type="text/javascript" src="http://code.jquery.com/jquery-1.10.2.min.js"&gt;&lt;/script&gt;
 * <br></code></pre>
 *
 * @module Core
 * @main Core
 **/

/**
 * This object provides some utility methods and a single global variable for all the objects of this API 
 *
 * @class GIAPI
 * @static
 **/
export const GIAPI = {

	demo: {

		//medsuv: 'http://hermes.essi-lab.eu/medsuv-demo/',
		medsuv: 'http://med-suv.essi-lab.eu/dab/',

		nextData: 'http://130.186.13.22/next-data-dab/',

		api: 'http://gs-service-production.geodab.eu/gs-service/',
		//api : 'http://api.eurogeoss-broker.eu/dab/',

		prod: 'http://production.geodab.eu/gi-cat-StP/',

		preProd: 'http://preproduction.geodab.eu/gi-cat-StP/',

		local: 'http://localhost:8085/gi-cat/',

		hermes: 'http://hermes.essi-lab.eu/dab/',

		v_hub: 'http://vh-it.energic-od.eu/gi-cat/',

		tb12: 'http://tb12.essi-lab.eu/pubsub-csw/',

		arpa: 'http://arpa-er.geodab.eu/gi-cat-arpa/'

	},

	ui: {


	},

	nameSpace: {

		/** 
		 * Set the nameSpace option for retrieving full metadata (getRecordById) records. Default value: ISO19115.
		 * Other possible value is: BLUECLOUD 
		 * If set before the creation of a ResultsMapWidget, it is used as value for the getRecordById (Full Metadata) option 
		 * 
		 * @static
		 * @property {String} [GIAPI.nameSpace.nameSpaceType='ISO19115']
		 */
		nameSpaceType: 'ISO19115'
	},



	logger: {
		enabled: false,
		info: {
			stringify: function() {
				return JSON.stringify(this, null, 4);
			}
		},
		warn: {
			stringify: function() {
				return JSON.stringify(this, null, 4);
			}
		},
		error: {
			stringify: function() {
				return JSON.stringify(this, null, 4);
			}
		},
		log: function(msg, level) {
			if (!GIAPI.logger.enabled) {
				return;
			}
			if (!level) {
				level = 'info';
			}
			// console.log(GIAPI.isoDateTime()+' ['+level.toUpperCase()+'] -> '+msg);
			switch (level) {
				case 'info':
					GIAPI.logger.info[GIAPI.isoDateTime()] = msg;
					break;
				case 'warn':
					GIAPI.logger.warn[GIAPI.isoDateTime()] = msg;
					break;
				case 'error':
					GIAPI.logger.error[GIAPI.isoDateTime()] = msg;
					break;
				default:
					return;
			}
		}
	},

	/**
	 * Returns a string of 16 random alphanumeric characters
	 * 
	 * @static
	 * @method random
	 * @return a string of 16 random alphanumeric characters
	 */
	random: function() {

		return Math.random().toString(36).slice(2);
	},

	query: function(dabEndpoint, constraints, options, parentId, targetId, sources, queryID, viewId, servicePath, openSearchPath) {

		if (!queryID) {
			queryID = GIAPI.random();
		}
		//*******************
		// Basic constraints
		//*******************

		var start = !options ? 1 : options.start || 1;
		var pageSize = !options ? 10 : options.pageSize || 10;

		var where = !constraints ? '' : constraints.where || '';

		// bounding box
		// (this API likes lat,lon while the OpenSearch service returns lon,lat)
		if (Array.prototype.isArray(where)) {
			var tmp = '';
			for (var i = 0; i < where.length; i++) {
				tmp += where[i].west + ',' + where[i].south + ',' + where[i].east + ',' + where[i].north + '_';
			}
			where = tmp.substring(0, tmp.length - 1);
		} else if (where) {
			where = where.west + ',' + where.south + ',' + where.east + ',' + where.north;
		}

		// time start and time end
		var when = !constraints ? '' : constraints.when || '';

		var when_from = '';
		if (Array.prototype.isArray(when)) {

			var tmp = '';
			for (var i = 0; i < when.length; i++) {
				var fr = when[i] && when[i].from ? when[i].from : '';
				if (fr) {
					var t = fr != 'NONE' ? 'T00:00:00Z' : '';
					tmp += (fr.indexOf('T') === -1 ? fr + t : fr) + '_';
				}
			}

			when_from = tmp.substring(0, tmp.length - 1);

		} else if (when && when.from) {

			var t = when.from != 'NONE' ? 'T00:00:00Z' : '';
			when_from = when.from.indexOf('T') === -1 ? when.from + t : when.from;
		}

		var when_to = '';
		if (Array.prototype.isArray(when)) {

			var tmp = '';
			for (var i = 0; i < when.length; i++) {
				var to = when[i] && when[i].to ? when[i].to : '';
				if (to) {
					var t = to != 'NONE' ? 'T00:00:00Z' : '';
					tmp += (to.indexOf('T') === -1 ? to + t : to) + '_';
				}
			}

			when_to = tmp.substring(0, tmp.length - 1);

		} else if (when && when.to) {

			var t = when.to != 'NONE' ? 'T00:00:00Z' : '';
			when_to = when.to.indexOf('T') === -1 ? when.to + t : when.to;
		}

		// search terms
		var what = GIAPI.readWhat(constraints, options);

		// parent identifiers
		var who = !constraints ? '' : constraints.who || '';
		var who2 = !constraints ? '' : constraints.who2 || '';

		if (!who && who2) {
			who = who2;
		} else if (who && who2) {
			who = who + ',' + who2;
		}

		// additional kvp
		var kvp = constraints && constraints.kvp;
		var queryKVP = '&';

		var kwd = '';
		var format = '';
		var protocol = '';
		var kwdOrBbox = '';
		var pla = '';
		var sen = '';
		var att = '';
		var ori = '';
		var score = '';

		var instr = '';
		var platTitle = '';
		var attrTitle = '';
		var attrURI = '';
		var orgName = '';
		var semantics = '';
		var ontology = '';

		if (kvp) {
			if (!Array.prototype.isArray(kvp)) {
				kvp = [kvp];
			}

			for (var kvpel = 0; kvpel < kvp.length; kvpel++) {
				var key = kvp[kvpel].key;
				var val = kvp[kvpel].value;

				if (key === 'kwdOrBbox') {
					kwdOrBbox = val;
					continue;
				}

				if (key === 'kwd') {
					kwd = val;
					continue;
				}

				if (key === 'frmt') {
					format = val;
					continue;
				}

				if (key === 'prot') {
					protocol = val;
					continue;
				}

				if (key === 'pla') {
					pla = val;
					continue;
				}
				if (key === 'sen') {
					sen = val;
					continue;
				}
				if (key === 'att') {
					att = val;
					continue;
				}
				if (key === 'ori') {
					ori = val;
					continue;
				}
				if (key === 'score') {
					score = val;
					continue;
				}

				if (key === 'instrumentTitle') {
					instr = val;
					continue;
				}

				if (key === 'platformTitle') {
					platTitle = val;
					continue;
				}

				if (key === 'ontology') {
					ontology = val;
					continue;
				}

				if (key === 'attributeTitle') {
					attrTitle = val;
					continue;
				}

				if (key === 'semantics') {
					semantics = val;
					continue;
				}

				if (key === 'observedPropertyURI') {
					attrURI = val;
					continue;
				}

				if (key === 'orgName') {
					orgName = val;
					continue;
				}

				queryKVP += key + '=' + val + '&';
			}
		}

		// ****************************
		// Term frequency constraints
		//****************************

		kwd = options && options.termFrequency && GIAPI.readConstraint(constraints, 'keyword') || kwd;
		format = options && options.termFrequency && GIAPI.readConstraint(constraints, 'format') || format;
		protocol = options && options.termFrequency && GIAPI.readConstraint(constraints, 'protocol') || protocol;
		sen = options && options.termFrequency && GIAPI.readConstraint(constraints, 'instrumentId') || sen;
		pla = options && options.termFrequency && GIAPI.readConstraint(constraints, 'platformId') || pla;
		ori = options && options.termFrequency && GIAPI.readConstraint(constraints, 'origOrgId') || ori;
		att = options && options.termFrequency && GIAPI.readConstraint(constraints, 'attributeId') || att;
		score = options && options.termFrequency && GIAPI.readConstraint(constraints, 'score') || score;

		instr = options && options.termFrequency && GIAPI.readConstraint(constraints, 'instrumentTitle') || instr;
		platTitle = options && options.termFrequency && GIAPI.readConstraint(constraints, 'platformTitle') || platTitle;
		attrTitle = options && options.termFrequency && GIAPI.readConstraint(constraints, 'attributeTitle') || attrTitle;
		attrURI = options && options.termFrequency && GIAPI.readConstraint(constraints, 'observedPropertyURI') || attrURI;
		orgName = options && options.termFrequency && GIAPI.readConstraint(constraints, 'organisationName') || orgName;

		semantics = options && options.termFrequency && GIAPI.readConstraint(constraints, 'semantics') || semantics;
		ontology = options && options.termFrequency && GIAPI.readConstraint(constraints, 'ontology') || ontology;


		// *******************************************************
		// Special constraints used only by the PubSubManager
		//
		var from = !constraints ? '' : constraints.from || '';
		var until = !constraints ? '' : constraints.until || '';
		//
		// *******************************************************

		//********************
		// Discover extension
		//********************

		var subject = '';
		var relation = '';
		if (options && options.extension) {
			if (typeof options.extension.relation === 'string') {
				relation = options.extension.relation;
			} else {
				relation = options.extension.relation.value;
			}

			if (options.extension.concepts) {

				what = '';
				var concepts = options.extension.concepts;

				if (typeof concepts === 'string') {
					subject = concepts;
				} else {

					for (var i = 0; i < concepts.length; i++) {
						var con = concepts[i];
						var uri = con.uri();
						subject += uri + ',';
					}
					subject = subject.substring(0, subject.length - 1);
				}
			} else {

				what = options.extension.keyword;
			}
		}

		//**********************
		// Search fields option
		//**********************

		var tmp_sf = !options ? '' : options.searchFields || '';
		var sf = '';
		if (what && tmp_sf) {
			if (Array.prototype.isArray(tmp_sf)) {
				for (var i = 0; i < tmp_sf.length; i++) {
					sf += tmp_sf[i] + ",";
				}
				sf = sf.substring(0, sf.length - 4);
			} else {
				sf = tmp_sf;
			}
		}

		var spatialRel = '';
		if (options && options.spatialRelation) {

			spatialRel = options.spatialRelation;
		}

		// term frequency
		var termFrequency = '';
		if (queryID.indexOf('expand') === -1) {
			if (options && options.termFrequency) {
				termFrequency = options.termFrequency.replace('source', 'providerID');
			} else {
				// by default all the targets are selected
				// termFrequency = 'keyword,format,providerID,protocol,instrumentId,platformId,origOrgId,attributeId,sscScore';
				termFrequency = 'keyword,format,providerID,protocol,instrumentId,platformId,origOrgId,attributeId,instrumentTitle,platformTitle,orgName,attributeTitle,observedPropertyURI';
			}
		}

		var path = 'opensearch/query?';
		if (viewId) {
			//        	viewId = '/'+viewId;

			path = 'view/' + viewId + '/opensearch/query?';
		}

		//        else{
		//        	viewId = '';
		//        }

		//        if (options && options.aggregate) {
		//            path = openSearchPath + viewId+'?reqID=' + queryID + '&aggregate=true&';
		//        } else {
		//            path = (subject || relation ) ? 'opensearchsemanticenhanced'+viewId+'?reqID=' + queryID + '&semExec=expand&' : openSearchPath+viewId+'?reqID=' + queryID + '&';
		//        }

		var slash = dabEndpoint.endsWith('/') ? '' : '/';
		var trgId = targetId ? targetId : '';

		var httpGet = dabEndpoint + slash + servicePath + '/' + path;

		httpGet += 'si=' + start + '&';
		httpGet += 'ct=' + pageSize + '&';
		httpGet += 'st=' + what + '&';

		httpGet += 'kwd=' + kwd + '&';
		httpGet += 'frmt=' + format + '&';
		httpGet += 'prot=' + protocol + '&';
		httpGet += 'kwdOrBbox=' + kwdOrBbox + '&';
		httpGet += 'sscScore=' + score + '&';

		//        httpGet += 'instrumentId=' + sen + '&';
		//        httpGet += 'platformId=' + pla + '&';
		//        httpGet += 'attributeId=' + att + '&';        
		//        httpGet += 'origOrgId=' + ori + '&';


		httpGet += 'semantics=' + semantics + '&';
		httpGet += 'ontology=' + ontology + '&';
		httpGet += 'instrumentTitle=' + instr + '&';
		httpGet += 'platformTitle=' + platTitle + '&';
		httpGet += 'attributeTitle=' + attrTitle + '&';
		httpGet += 'observedPropertyURI=' + attrURI + '&';
		httpGet += 'organisationName=' + orgName + '&';



		httpGet += 'searchFields=' + sf + '&';
		httpGet += 'bbox=' + where + '&';
		httpGet += 'rel=' + spatialRel + '&';
		httpGet += 'tf=' + termFrequency + '&';
		httpGet += 'ts=' + when_from + '&';
		httpGet += 'te=' + when_to + '&';
		httpGet += 'targetId=' + trgId + '&';

		httpGet += 'from=' + from + '&';
		httpGet += 'until=' + until + '&';

		// static variable to disable the crawler and speed up the response time
		if (GIAPI.disableCrawler) {
			httpGet += 'crawler=disabled&';
		}

		if (sources) {
			httpGet += 'sources=' + sources + '&';
		}

		if (parentId || who) {
			httpGet += 'parents=' + (parentId ? encodeURIComponent(parentId) : encodeURIComponent(who)) + '&';
		}

		httpGet += 'subj=' + encodeURIComponent(subject) + '&rela=' + relation + queryKVP + 'outputFormat=application/json';

		GIAPI.logger.log('query: ' + httpGet);

		return httpGet;
	},


	status: function(dabEndpoint, extended, queryID, servicePath) {

		var slash = dabEndpoint.endsWith('/') ? '' : '/';
		var sem = extended ? '&semantic=true' : '';
		var endpoint = GIAPI.lbStatusProxyEndpoint ? GIAPI.lbStatusProxyEndpoint : dabEndpoint + slash + servicePath + '/status?';
		return endpoint + 'id=' + queryID + '&format=application/json' + sem;
	},

	concept: function(dabEndpoint, keyword, uri, relation, options, servicePath) {

		// 1) keyword
		// 2) uri + relation. se (uri & !relation) --> relation=lista_completa

		//////////////////////////////////////////////////////////////////////////////////////
		// dabEndpoint = 'http://ec2-54-226-142-244.compute-1.amazonaws.com/normalization-test';
		//////////////////////////////////////////////////////////////////////////////////////

		var slash = dabEndpoint.endsWith('/') ? '' : '/';
		var queryID = GIAPI.random();
		var rela = '';
		if (!keyword) {
			keyword = '';
			// ensures keyword is not null or undefined
			if (relation) {
				var tmpArray = [];
				for (var i = 0; i < relation.length; i++) {
					var r = relation[i];
					if (typeof r === 'string') {
						tmpArray.push(r);
					} else {
						tmpArray.push(r.value);
					}
				}
				rela = tmpArray;
			} else {
				// this is only for the Concept.extend (no relation = all relations)
				var allRela = '';
				for (name in GIAPI.Relation) {
					if (name !== 'NONE' && name !== 'decode') {
						allRela += GIAPI.Relation[name].value + ',';
					}
				}
				rela = allRela.substring(0, allRela.length - 1);
			}
		}
		var si = options && options.start ? options.start : 1;
		var ct = options && options.count ? options.count : 10;
		var topLevel = options && options.topLevel ? options.topLevel : 'false';
		uri = encodeURIComponent(uri);


		var httpGet = dabEndpoint + slash + servicePath + '/opensearchsemanticenhanced?semExec=suggestion&si=' + si + '&ct=' + ct + '&reqID=' + queryID + '&st=' + keyword + '&topLevel=' + topLevel + '&subj=' + uri + '&rela=' + rela + '&outputFormat=application/json';

		GIAPI.logger.log('concept: ' + httpGet);

		return httpGet;
	},

	/**
	 * Merges the given <a href="../classes/DAB.html#kvp_list">basic constraints</a> and returns the merged ones
	 *
	 * @static
	 * @method mergeConstraints
	 * @param constraints1 the first <a href="../classes/DAB.html#constraints">constraints</a> object to merge
	 * @param constraints2 the second <a href="../classes/DAB.html#constraints">constraints</a> object to merge
	 * @return the merged <a href="../classes/DAB.html#constraints">constraints</a>
	 */
	mergeConstraints: function(constraints1, constraints2) {

		var cnst1 = GIAPI.clone(constraints1);

		var where1 = cnst1 ? cnst1.where : '';
		var when1 = cnst1 ? cnst1.when : '';
		var what1 = cnst1 ? cnst1.what : '';
		var who1 = cnst1 ? cnst1.who : '';


		var where2 = constraints2 ? constraints2.where : '';
		var when2 = constraints2 ? constraints2.when : '';
		if (when2 && when2.from && !when2.to) {
			when2.to = 'NONE';
		} else if (when2 && when2.to && !when2.from) {
			when2.from = 'NONE';
		}

		var what2 = constraints2 ? constraints2.what : '';
		var who2 = constraints2 ? constraints2.who : '';

		if (where2) {
			if (Array.isArray(where1)) {
				if (Array.isArray(where2)) {
					for (var i = 0; i < where2.length; i++) {
						where1.push(where2[i]);
					}
				} else {
					where1.push(where2);
				}
			} else {
				if (!where1) {
					cnst1.where = where2;
				} else if (!GIAPI.compareWhere(cnst1.where, where2)) {
					delete cnst1.where;
					cnst1.where = [];
					cnst1.where.push(where1);

					if (Array.isArray(where2)) {
						for (var i = 0; i < where2.length; i++) {
							cnst1.where.push(where2[i]);
						}
					} else {
						cnst1.where.push(where2);
					}
				}
			}
		}

		if (when2) {
			if (Array.isArray(when1)) {
				if (Array.isArray(when2)) {
					for (var i = 0; i < when2.length; i++) {
						when1.push(when2[i]);
					}
				} else {
					when1.push(when2);
				}
			} else {
				if (!when1) {
					cnst1.when = when2;
				} else if (!GIAPI.compareWhen(cnst1.where, where2)) {
					delete cnst1.when;
					cnst1.when = [];
					cnst1.when.push(when1);

					if (Array.isArray(when2)) {
						for (var i = 0; i < when2.length; i++) {
							cnst1.when.push(when2[i]);
						}
					} else {
						cnst1.when.push(when2);
					}
				}
			}
		}

		if (what2) {
			if (Array.isArray(what1)) {
				if (Array.isArray(what2)) {
					what1[what1.length - 1] = what1[what1.length - 1] + ' AND ';
					for (var i = 0; i < what2.length; i++) {
						what1.push(what2[i]);
					}
				} else {
					what1.push(' AND ' + what2);
				}
			} else {
				if (!what1) {
					cnst1.what = what2;
				} else if (!GIAPI.compare(cnst1.what, what2)) {
					delete cnst1.what;
					cnst1.what = [];
					cnst1.what.push(what1 + ' AND ');

					if (Array.isArray(what2)) {
						for (var i = 0; i < what2.length; i++) {
							cnst1.what.push(what2[i]);
						}
					} else {
						cnst1.what.push(what2);
					}
				}
			}
		}

		if (who2) {
			if (Array.isArray(who1)) {
				if (Array.isArray(who2)) {
					for (var i = 0; i < who2.length; i++) {
						who1.push(who2[i]);
					}
				} else {
					who1.push(who2);
				}
			} else {
				if (!who1) {
					cnst1.who = who2;
				} else if (!GIAPI.compare(cnst1.who, who2)) {
					delete cnst1.who;
					cnst1.who = [];
					cnst1.who.push(who1);

					if (Array.isArray(who2)) {
						for (var i = 0; i < who2.length; i++) {
							cnst1.who.push(who2[i]);
						}
					} else {
						cnst1.who.push(who2);
					}
				}
			}
		}

		//
		// merging of advanced params not supported by the refiner
		//

		if (!cnst1.kvp && !constraints2.kvp) {
			return cnst1;
		}

		cnst1.kvp = cnst1.kvp || [{}]; // a] this empty element will be removed
		if (Array.isArray(cnst1.kvp) && cnst1.kvp.length === 0) {
			cnst1.kvp = [{}];
		}
		if (!Array.isArray(cnst1.kvp)) {
			cnst1.kvp = [cnst1.kvp];
		}

		constraints2.kvp = constraints2.kvp || [{}];
		if (Array.isArray(constraints2.kvp) && constraints2.kvp.length === 0) {
			constraints2.kvp = [{}];
		}
		if (!Array.isArray(constraints2.kvp)) {
			constraints2.kvp = [constraints2.kvp];
		}

		cnst1.kvp.forEach(function(obj1, index1) {
			var key1 = obj1 && obj1.key;
			constraints2.kvp.forEach(function(obj2, index2) {

				var key2 = obj2 && obj2.key;
				if (key2 !== "#") {
					if (key1 && !key2) {
						// nothing to do
					} else if (!key1 && key2) {
						// cnst.kvp is the empty object
						cnst1.kvp.push(obj2);

					} else if (key1 === key2) {

						var val1 = obj1.value;
						var val2 = obj2.value;

						val1 += ',' + val2;
						cnst1.kvp[index1].value = val1;
						constraints2.kvp[index1].key = "#";

					} else {
						var found = false;
						for (var i = 0; i < cnst1.kvp.length; i++) {
							if (cnst1.kvp[i].key === key2) {
								found = true;
								break;
							}
						}
						if (!found) {
							cnst1.kvp.push({ "key": obj2.key, "value": obj2.value });
						}
					}
				}
			});
		});
		// if the first element is the empty element (see a]), removes it
		if (cnst1.kvp && cnst1.kvp[0] && !cnst1.kvp[0].key) {
			cnst1.kvp.splice(0, 1);
		}

		return cnst1;
	},

	compareWhere: function(w1, w2) {

		return !Array.isArray(w2) && w1.west === w2.west && w1.east === w2.east && w1.north === w2.north && w1.south === w2.south;
	},

	compareWhen: function(w1, w2) {

		return !Array.isArray(w2) && w1.from === w2.from && w1.to === w2.to;
	},

	compare: function(v1, v2) {

		return !Array.isArray(v2) && v1 === v2;
	},

	readWhat: function(constraints, options) {

		var tmp_what = !constraints ? '' : constraints.what || '';
		var searchOp = options && options.searchOperator ? ' ' + options.searchOperator + ' ' : ' OR ';
		var what = '';
		if (Array.isArray(tmp_what)) {
			for (var i = 0; i < tmp_what.length; i++) {
				what += tmp_what[i].trim() + searchOp;
			}
			what = what.substring(0, what.length - 4).trim();
		} else {
			what = tmp_what.trim();
		}

		return what.replace(/OR AND/g, 'AND').replace(/AND OR/g, 'AND');
	},

	readWho: function(constraints) {

		var tmp_who = !constraints ? '' : constraints.who || '';
		var who = '';
		if (Array.isArray(tmp_who)) {
			for (var i = 0; i < tmp_who.length; i++) {
				who += tmp_who[i].trim() + ',';
			}
			who = who.substring(0, who.length - 1).trim();
		} else {
			who = tmp_who.trim();
		}

		return who;
	},

	readConstraint: function(constraints, param) {

		var tmp = !constraints ? '' : constraints[param] || '';
		var out = '';
		if (Array.isArray(tmp)) {
			for (var i = 0; i < tmp.length; i++) {
				out += tmp[i] + ' AND ';
			}
			out = out.substring(0, out.length - 5);
		} else {
			out = tmp;
		}

		return out;
	},

	/**
	 * Creates an empty <a href="../classes/ResultSet.html" target=_blank>ResultSet</a>
	 * 
	 * @static
	 * @method emptyResultSet
	 * @param [error] an optional error message 
	 * @return an empty <a href="../classes/ResultSet.html" target=_blank>ResultSet</a>
	 */
	emptyResultSet: function(error) {

		var resultSet = {};
		resultSet.size = 0;
		resultSet.pageSize = 0;
		resultSet.start = 0;
		resultSet.pageIndex = 0;
		resultSet.pageCount = 0;
		if (error) {
			resultSet.error = error;
		}

		return resultSet;
	},

	/**
	 * 
	 */
	getUrlParameter: function getUrlParameter(sParam) {

		var sPageURL = decodeURIComponent(window.location.search.substring(1));
		var sURLVariables = sPageURL.split('&');

		for (var i = 0; i < sURLVariables.length; i++) {
			var sParameterName = sURLVariables[i].split('=');

			if (sParameterName[0] === sParam) {
				return sParameterName[1] === undefined ? true : sParameterName[1];
			}
		}
	},

	/**
	 *  
	 */
	emptyResponse: function() {

		var response = [];
		var rs = GIAPI.emptyResultSet();
		rs.paginator = GIAPI.Paginator();
		rs.paginator._page = GIAPI.Page([], 0);
		response.push(rs);

		return response;
	},

	/**
	 * Creates a clone of the given <code>object</code>
	 * 
	 * @static
	 * @method clone
	 * @param object the object to clone
	 * @return the cloned object
	 */
	clone: function(object) {

		return jQuery.extend(true, {}, object);
	},

	/**
	 * Returns the current time in the ISO8601 format YYYY-MM-DDThh:mm:ss.mls
	 * 
	 * @static
	 * @method isoDateTime
	 * @return the current time in the ISO8601 format YYYY-MM-DDThh:mm:ss.mls
	 */
	isoDateTime: function() {

		return new Date().format('isoDateTimeMls');
	},

	/**
	 * Formats the given integer <code>number</code> inserting a '.' to separate the thousands
	 * 
	 * @static
	 * @param {String/Integer} integer in integer number
	 * @return the formatted number as a string
	 * 
	 * @method thousandsSeparator
	 * 
	 */
	// http://stackoverflow.com/questions/2901102/how-to-print-a-number-with-commas-as-thousands-separators-in-javascript
	thousandsSeparator: function(integer) {

		return (integer + '').replace(/\B(?=(\d{3})+(?!\d))/g, ',');
	},

	/**
	 * Formats all the words in the given <code>string</code> by capitalizing the first character of each word and 
	 * setting the lower case to the remaining characters
	 * 
	 * @static
	 * @param {String} string the words to format
	 * @method formatWords
	 * @return the formatted string
	 */
	formatWords: function(string) {

		var array = string.split(' ');
		var out = '';
		array.forEach(function(word) {

			word = word.substring(0, 1).toUpperCase() + word.substring(1, word.length).toLowerCase();
			out += word + ' ';
		});
		return out.substring(0, out.length - 1);
	},

	/**
	 * Returns a string of <code>count</code> characters <code>&nbsp;</code>
	 * 
	 * @static
	 * @method spaces
	 * @return the formatted string
	 */
	spaces: function(count) {
		var out = '';
		for (var i = 0; i < count; i++) {
			out += '&nbsp;';
		}
		return out;
	},

	readParam: function(url, key) {

		var value = url.split(key + '=')[1];
		if (value.indexOf('&') > -1) {
			value = value.substring(0, value.indexOf('&'));
		}
		return value;
	},

	position: function(pos) {
		var my;
		var at;
		switch (pos) {
			case 'left':
				my = 'right center';
				at = 'left center';
				break;

			case 'right':
				my = 'left center';
				at = 'right center';
				break;
			case 'top':
				my = 'center bottom';
				at = 'center top';
				break;
			case 'bottom':
				my = 'center top';
				at = 'center bottom';
				break;
		}

		return { 'my': my, 'at': at };
	},

	protocols: {

		"ESRI MapServer Protocol": "urn:x-esri:specification:ServiceType:ArcGIS",
		"ESRI MapServer Protocol 10.0.0": "urn:x-esri:specification:ServiceType:ArcGIS:10.0.0",
		"OGC Web Coverae Service 1.0 Protocol": "urn:ogc:serviceType:WebCoverageService:1.0:HTTP",
		"Trajectory Protocol": "urn:essi:serviceType:Trajectory:HTTP",
		"Tiled Service": "urn:essi:serviceType:TiledMapService:HTTP",
		"OGC Web Coverage Service 1.0.0 Protocol": "urn:ogc:serviceType:WebCoverageService:1.0.0:HTTP",
		"OGC Web Coverage Service 1.0.0 Protocol - EDO profile": "urn:ogc:serviceType:WebCoverageService:EDO:1.0:HTTP",
		"OGC Web Coverage Service 1.1 Protocol": "urn:ogc:serviceType:WebCoverageService:1.1:HTTP",
		"OGC Web Coverage Service 1.1.1 Protocol": "urn:ogc:serviceType:WebCoverageService:1.1.1:HTTP",
		"OGC Web Coverage Service 2.0 Protocol": "urn:ogc:serviceType:WebCoverageService:2.0:HTTP",
		"OGC Web Coverage Service Protocol": "urn:ogc:serviceType:WebCoverageService:HTTP",
		"OGC Web Coverage Service 1.1.2 Protocol": "urn:ogc:serviceType:WebCoverageService:1.1.2:HTTP",
		"OGC Web Map Service 1.1.1 Protocol": "urn:ogc:serviceType:WebMapService:1.1.1:HTTP",
		"OGC Web Map Service 1.3.0 Protocol": "urn:ogc:serviceType:WebMapService:1.3.0:HTTP",
		"OGC Web Map Service 1.3.0 Protocol Quality Profile": "urn:ogc:serviceType:WebMapService:1.3.0:HTTP:QualityProfile",
		"OGC Web Map Service Protocol": "urn:ogc:serviceType:WebMapService:HTTP",
		"OGC Web Map Tile Service 1.0.0 Protocol": "urn:ogc:serviceType:WebMapTileService:1.0.0:HTTP",
		"OGC Web Feature Service 2.0.0 Protocol": "urn:ogc:serviceType:WebFeatureService:2.0.0:HTTP",
		"OGC Web Feature Service 1.0.0 Protocol": "urn:ogc:serviceType:WebFeatureService:1.0.0:HTTP",
		"OGC Web Feature Service 1.1.0 Protocol": "urn:ogc:serviceType:WebFeatureService:1.1.0:HTTP",
		"OGC Web Feature Service Protocol": "urn:ogc:serviceType:WebFeatureService:HTTP",
		"OPeNDAP Protocol": "OPeNDAP",
		"NetCDF Subset": "NETCDF-SUBSET",
		"NetCDF Attribute Convention for Dataset Discovery Report": "UDDC",
		"ISO 19115 document": "ISO19115",
		"NetCDF Markup Language": "NCML",
		"Earth Engine Protocol": "EARTH-ENGINE",
		"OGC Sensor Observation Service 1.0.0 Protocol": "urn:ogc:serviceType:SensorObservationService:1.0.0:HTTP",
		"OGC Sensor Observation Service 2.0.0 Protocol": "urn:ogc:serviceType:SensorObservationService:2.0.0:HTTP",
		"OGC Sensor Observation Service 2.0.0 Protocol, Hydro Application Profile": "urn:ogc:serviceType:SensorObservationServiceHydroProf:2.0.0:HTTP",
		"OGC Sensor Observation Service Protocol": "urn:ogc:serviceType:SensorObservationService:HTTP",
		"Access Broker Protocol": "gi-axe",
		"GEOSS Helper Application": "geoss:helperapp",
		"GEOSS Online Helper Application": "geoss:helperapp:online",
		"EGASKRO Protocol": "EGASKRO",
		"HYRAX Protocol": "HYRAX",
		"RASAQM Protocol": "RASAQM",
		"FILE Protocol": "FILE",
		"FTP Protocol": "FTP",
		"KISTERS Protocol": "KISTERS",
		"HIS HYDRO Protocol": "HYDRO",
		"ARPA HYDRO-DB Protocol": "HYDROSERVER-DB",
		"Environment Canada Protocol": "ECANADA",
		"HTTP Protocol": "HTTP"
	}
};

if (typeof Array.prototype.isArray !== 'function') {
	Array.prototype.isArray = function(value) {
		return Object.prototype.toString.apply(value) === '[object Array]';
	};
};

if (typeof Array.prototype.remove !== 'function') {
	Array.prototype.remove = function(array, item) {
		for (var i in array) {
			if (array[i] === item) {
				array.splice(i, 1);
				break;
			}
		}
	};
};

if (typeof Array.prototype.distinctValues !== 'function') {
	Array.prototype.distinctValues = function(a) {
		return function() {
			return this.filter(a);
		};
	}(function(a, b, c) {
		return c.indexOf(a, b + 1) < 0;
	});
};

if (typeof String.prototype.endsWith !== 'function') {
	String.prototype.endsWith = function(suffix) {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};
};

if (typeof String.prototype.includes !== 'function') {
	String.prototype.includes = function(value) {
		return this.indexOf(value) !== -1;
	};
};

//**
//* jquery.timer.js
//*
//* Copyright (c) 2011 Jason Chavannes <jason.chavannes@gmail.com>
//*
//* http://jchavannes.com/jquery-timer
//*
//* Permission is hereby granted, free of charge, to any person
//* obtaining a copy of this software and associated documentation
//* files (the "Software"), to deal in the Software without
//* restriction, including without limitation the rights to use, copy,
//* modify, merge, publish, distribute, sublicense, and/or sell copies
//* of the Software, and to permit persons to whom the Software is
//* furnished to do so, subject to the following conditions:
//*
//* The above copyright notice and this permission notice shall be
//* included in all copies or substantial portions of the Software.
//*
//* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
//* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
//* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
//* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
//* BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
//* ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
//* CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//* SOFTWARE.
//*/
(function($) {
	$.timer = function(func, time, autostart) {

		this.set = function(func, time, autostart) {
			this.init = true;

			if (typeof func == 'object') {
				var paramList = ['autostart', 'time'];
				for (var arg in paramList) {
					if (func[paramList[arg]] != undefined) {
						eval(paramList[arg] + " = func[paramList[arg]]");
					}
				};
				func = func.action;
			}

			if (typeof func == 'function') {
				this.action = func;
			}

			if (!isNaN(time)) {
				this.intervalTime = time;
			}

			if (autostart && !this.isActive) {

				this.isActive = true;
				this.setTimer();
			}
			return this;
		};

		this.once = function(time) {
			var timer = this;
			if (isNaN(time)) {
				time = 0;
			}
			window.setTimeout(function() {
				timer.action();
			}, time);
			return this;
		};

		this.play = function(reset) {
			if (!this.isActive) {
				if (reset) {
					this.setTimer();
				} else {
					this.setTimer(this.remaining);
				}
				this.isActive = true;
			}
			return this;
		};

		this.pause = function() {
			if (this.isActive) {
				this.isActive = false;
				this.remaining -= new Date() - this.last;
				this.clearTimer();
			}
			return this;
		};

		this.stop = function() {
			this.isActive = false;
			this.remaining = this.intervalTime;
			this.clearTimer();
			return this;
		};

		this.toggle = function(reset) {
			if (this.isActive) {
				this.pause();
			} else if (reset) {
				this.play(true);
			} else {
				this.play();
			}
			return this;
		};

		this.reset = function() {
			this.isActive = false;
			this.play(true);
			return this;
		};

		this.clearTimer = function() {
			window.clearTimeout(this.timeoutObject);
		};

		this.setTimer = function(time) {
			var timer = this;
			if (typeof this.action != 'function') {
				return;
			}
			if (isNaN(time)) {
				time = this.intervalTime;
			}
			this.remaining = time;
			this.last = new Date();
			this.clearTimer();
			this.timeoutObject = window.setTimeout(function() {
				timer.go();
			}, time);
		};

		this.go = function() {
			if (this.isActive) {
				this.action();
				this.setTimer();
			}
		};

		if (this.init) {
			return new $.timer(func, time, autostart);
		} else {
			this.set(func, time, autostart);
			return this;
		}
	};
})(jQuery);

/*
 * Date Format 1.2.3
 * (c) 2007-2009 Steven Levithan <stevenlevithan.com>
 * MIT license
 *
 * Includes enhancements by Scott Trenda <scott.trenda.net>
 * and Kris Kowal <cixar.com/~kris.kowal/>
 *
 * Accepts a date, a mask, or a date and a mask.
 * Returns a formatted version of the given date.
 * The date defaults to the current date/time.
 * The mask defaults to dateFormat.masks.default.
 */
dateFormat = function() {
	var token = /d{1,4}|m{1,4}|yy(?:yy)?|([HhMsTt])\1?|[LloSZ]|"[^"]*"|'[^']*'/g, timezone = /\b(?:[PMCEA][SDP]T|(?:Pacific|Mountain|Central|Eastern|Atlantic) (?:Standard|Daylight|Prevailing) Time|(?:GMT|UTC)(?:[-+]\d{4})?)\b/g, timezoneClip = /[^-+\dA-Z]/g, pad = function(val, len) {
		val = String(val);
		len = len || 2;
		while (val.length < len)
			val = "0" + val;
		return val;
	};

	// Regexes and supporting functions are cached through closure
	return function(date, mask, utc) {
		var dF = dateFormat;

		// You can't provide utc if you skip other args (use the "UTC:" mask
		// prefix)
		if (arguments.length == 1 && Object.prototype.toString.call(date) == "[object String]" && !/\d/.test(date)) {
			mask = date;
			date = undefined;
		}

		// Passing date through Date applies Date.parse, if necessary
		date = date ? new Date(date) : new Date;
		if (isNaN(date))
			throw SyntaxError("invalid date");

		mask = String(dF.masks[mask] || mask || dF.masks["default"]);

		// Allow setting the utc argument via the mask
		if (mask.slice(0, 4) == "UTC:") {
			mask = mask.slice(4);
			utc = true;
		}

		var _ = utc ? "getUTC" : "get", d = date[_ + "Date"](), D = date[_ + "Day"](), m = date[_ + "Month"](), y = date[_ + "FullYear"](), H = date[_ + "Hours"](), M = date[_ + "Minutes"](), s = date[_ + "Seconds"](), L = date[_ + "Milliseconds"](), o = utc ? 0 : date.getTimezoneOffset(), flags = {
			d: d,
			dd: pad(d),
			ddd: dF.i18n.dayNames[D],
			dddd: dF.i18n.dayNames[D + 7],
			m: m + 1,
			mm: pad(m + 1),
			mmm: dF.i18n.monthNames[m],
			mmmm: dF.i18n.monthNames[m + 12],
			yy: String(y).slice(2),
			yyyy: y,
			h: H % 12 || 12,
			hh: pad(H % 12 || 12),
			H: H,
			HH: pad(H),
			M: M,
			MM: pad(M),
			s: s,
			ss: pad(s),
			l: pad(L, 3),
			L: pad(L > 99 ? Math.round(L / 10) : L),
			t: H < 12 ? "a" : "p",
			tt: H < 12 ? "am" : "pm",
			T: H < 12 ? "A" : "P",
			TT: H < 12 ? "AM" : "PM",
			Z: utc ? "UTC" : (String(date).match(timezone) || [""]).pop().replace(timezoneClip, ""),
			o: (o > 0 ? "-" : "+") + pad(Math.floor(Math.abs(o) / 60) * 100 + Math.abs(o) % 60, 4),
			S: ["th", "st", "nd", "rd"][d % 10 > 3 ? 0 : (d % 100 - d % 10 != 10) * d % 10]
		};

		return mask.replace(token, function($0) {
			return $0 in flags ? flags[$0] : $0.slice(1, $0.length - 1);
		});
	};
}();

// Some common format strings
dateFormat.masks = {
	"default": "ddd mmm dd yyyy HH:MM:ss",
	shortDate: "m/d/yy",
	mediumDate: "mmm d, yyyy",
	longDate: "mmmm d, yyyy",
	fullDate: "dddd, mmmm d, yyyy",
	shortTime: "h:MM TT",
	mediumTime: "h:MM:ss TT",
	longTime: "h:MM:ss TT Z",
	isoDate: "yyyy-mm-dd",
	isoTime: "HH:MM:ss",
	isoTimeMls: "HH:MM:ss.l",
	isoDateTime: "yyyy-mm-dd'T'HH:MM:ss",
	isoDateTimeMls: "yyyy-mm-dd'T'HH:MM:ss.l",
	isoUtcDateTime: "UTC:yyyy-mm-dd'T'HH:MM:ss'Z'"
};

// Internationalization strings
dateFormat.i18n = {
	dayNames: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
	monthNames: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"]
};

if (typeof Date.prototype.format !== 'function') {

	Date.prototype.format = function(mask, utc) {
		return dateFormat(this, mask, utc);
	};
}; 