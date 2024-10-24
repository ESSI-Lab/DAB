/**
 *
 * This module contains the {{#crossLink "Concept"}}{{/crossLink}} object and other related objects
 *
 * @module Concept
 * @main Concept
 **/

/**
 * This object represents a <i>concept</i> from a Controlled Vocabulary.
 *
 * See also {{#crossLink "DAB/concept:method"}}DAB concept method{{/crossLink}}.<br>
 * See also {{#crossLink "DAB/discover:method"}}DAB discover method{{/crossLink}}
 *
 * @class Concept
 **/

GIAPI.Concept = function() {

	var outConcept = {};
	var dabEndpoint = arguments[0];
	outConcept._report = arguments[1];

	/** 
	 * Retrieves {{#crossLink "Concept"}}concepts{{/crossLink}} linked to this {{#crossLink "Concept"}}concept{{/crossLink}} according to
	 * one or more {{#crossLink "Relation"}}relations{{/crossLink}} from the remote Controlled Vocabulary.<br>
	 * The size of the extension, that is the number of {{#crossLink "Concept"}}concepts{{/crossLink}} resulting from the extension, can be retrieved using the method {{#crossLink "Concept/extensionSize:method"}}{{/crossLink}}   
	 *
	 // extends a concept using two relations;
	 // the following styles are equivalents

	 // uses GIAPI.Relation instances
	 var rel = [];
	 rel.push(GIAPI.Relation.BROADER);
	 rel.push(GIAPI.Relation.NARROWER);

	 // uses GIAPI.Relation instances values
	 rel = [];
	 rel.push(GIAPI.Relation.BROADER.value);
	 rel.push(GIAPI.Relation.NARROWER.value);

	 // uses GIAPI.Relation instances value
	 rel = [];
	 rel.push('skos%3Abroader');
	 rel.push('skos%3Anarrower');

	 // uses a combined style
	 rel = [];
	 rel.push(GIAPI.Relation.BROADER);
	 rel.push('skos%3Anarrower');

	 // extends the concept
	 concept.extend( function(result,error) {

	 if(error){
    	 alert('Error occurred: '+error);
    	 return;
	 }

	 for (var i = 0; i < result.length; i++) {

    	 var con = concepts[i];
    	 var uri = con.uri();
    	 var label = con.label();
    	 var desc = con.description();
    	 var thes = con.sourceThesaurus();
    	 var rel = con.rootRelation();
    
    	 // invokes stringify method on all returned concepts
    	 alert(con.stringify());
	   }
	 }, rel);
     
  	 *
	 *
	 * @method extend
	 * @async
	 *
	 * @param {Function} onResponse Callback function for receiving asynchronous query response
	 * @param {Array} onResponse.result The array of linked {{#crossLink "Concept"}}concepts{{/crossLink}}
	 * @param {String} onResponse.error In case of error, this argument contains a message which describes the problem occurred
	 *
	 * @param {<a href="Relation.html" target=_blank>Relation</a>/String [[]]} [relation] An array of
	 * {{#crossLink "Relation"}}relations{{/crossLink}} or {{#crossLink "Relation"}}relations{{/crossLink}} values used
	 * to retrieve the linked concepts. If omitted, all available {{#crossLink "Relation"}}relations{{/crossLink}} are used
	 * @param {Object} [options] Object literal of available options
	 * @param {Integer} [options.start] The start index of the first returned {{#crossLink "Concept"}}concept{{/crossLink}}, starting from 1
	 * @param {Integer} [options.count=10] The maximum number of  {{#crossLink "Concept"}}concepts{{/crossLink}} to return
	 **/
	outConcept.extend = function(onResponse, relation, options) {
        
		var rel = isRelation(relation) || isRelation(options);
		var opt = isOptions(relation) || isOptions(options);

		var query = GIAPI.concept(dabEndpoint, '', outConcept._report.uri, rel, opt,'services');

		jQuery.ajax({

			type : 'GET',
			url : query,
			crossDomain : true,
			dataType : 'jsonp',

			success : function(data) {

				if (data.resultSet && data.resultSet.error) {
					GIAPI.logger.log('extend concept error: ' + data.resultSet.error, 'error');
					onResponse.apply(outConcept, [[], 'Error occurred:' + data.resultSet.error]);

					return;
				}

				GIAPI.logger.log('extend success');

				var con = data.concept;
				var concepts = [];

				for (var i = 0; i < con.length; i++) {

					var report = con[i];
					var concept = GIAPI.Concept(dabEndpoint, report);
					concepts.push(concept);
				}

				onResponse.apply(outConcept, [concepts]);
			},

			complete : function(jqXHR, status) {

				GIAPI.logger.log('extend concept complete status: ' + status);
			},

			error : function(jqXHR, error, exception) {

				error = error + (exception.message ? ', exception -> ' + exception.message : '');

				GIAPI.logger.log('extend concept error: ' + error, 'error');

				onResponse.apply(outConcept, [[], 'Error occurred:' + error]);
			}
		});
	};

	/**
	 * Retrieves the {{#crossLink "Property"}}{{/crossLink}} of this {{#crossLink "Concept"}}{{/crossLink}} with the
	 * specified <code>name</code>
	 *
	 * @method property
	 * @async
	 *
	 * @param {Function} onResponse Callback function for receiving asynchronous query response
	 * @param {Property} onResponse.property The {{#crossLink "Property"}}{{/crossLink}} with the
	 * specified <code>name</code>, or <code>null</code> in case of error or if no {{#crossLink "Property"}}{{/crossLink}} exists with the
	 * specified <code>name</code>
	 *
	 * @param {String} onResponse.error In case of error, this argument contains a message which describes the problem occurred
	 * @param {String} name The name of the {{#crossLink "Property"}}property{{/crossLink}}. See also {{#crossLink "PropertyName"}}{{/crossLink}}
	 *
	 **/
	outConcept.property = function(onResponse, name) {

		var query = GIAPI.concept(dabEndpoint, '', outConcept._report.uri, [name], null);

		jQuery.ajax({

			type : 'GET',
			url : query,
			crossDomain : true,
			dataType : 'jsonp',

			success : function(data) {

				if (data.resultSet && data.resultSet.error) {
					GIAPI.logger.log('concept property error: ' + data.resultSet.error, 'error');
					onResponse.apply(outConcept, [null, 'Error occurred:' + data.resultSet.error]);

					return;
				}

				GIAPI.logger.log('concept property success');

				var values = data.properties && data.properties.values ? data.properties.values : null;
				var property = null;
				if (values) {
					property = {
						name : name,
						values : values
					};
				}

				onResponse.apply(outConcept, [property]);
			},

			complete : function(jqXHR, status) {

				GIAPI.logger.log('concept property complete status: ' + status);
			},

			error : function(jqXHR, error, exception) {

				error = error + (exception.message ? ', exception -> ' + exception.message : '');

				GIAPI.logger.log('concept property error: ' + error, 'error');

				onResponse.apply(outConcept, [[], 'Error occurred:' + error]);
			}
		});
	};

	/**
	 * Retrieves the URI of this {{#crossLink "Concept"}}concept{{/crossLink}}
	 *
	 * @method uri
	 * @return {String} The URI string of this {{#crossLink "Concept"}}concept{{/crossLink}}
	 **/
	outConcept.uri = function() {

		return outConcept._report.uri;
	};

	/**
	 * Retrieves the labels of this {{#crossLink "Concept"}}concept{{/crossLink}}
	 *
	 * @method label
	 * @return {Array} An array of strings. Each string is a label of this {{#crossLink "Concept"}}concept{{/crossLink}} in different languages
	 **/
	outConcept.label = function() {

		return outConcept._report.label;
	};
    
    
    /**
     * Retrieves the number of {{#crossLink "Concept"}}concepts{{/crossLink}} resulting from the {{#crossLink "Concept/extend:method"}}extension{{/crossLink}} of
     * this {{#crossLink "Concept"}}concept{{/crossLink}} with the specified {{#crossLink "Relation"}}relation{{/crossLink}}
     *
     * @method extensionSize
     * @param {Relation} [relation] the {{#crossLink "Relation"}}relation{{/crossLink}} used to extend the {{#crossLink "Concept"}}concept{{/crossLink}}.
     * If no relation is provided, all relations are used to calculate the {{#crossLink "Concept/extend:method"}}extension{{/crossLink}} size
     * @return {Integer} The number of {{#crossLink "Concept"}}concepts{{/crossLink}} resulting from the
     * {{#crossLink "Concept/extend:method"}}extension{{/crossLink}} of
     * this {{#crossLink "Concept"}}concept{{/crossLink}} with the specified {{#crossLink "Relation"}}relation{{/crossLink}}
     **/
    outConcept.extensionSize = function(relation) {
        
    	if (!relation) {
			relation = 'all';
		}

		var tot = 0;

		if (relation.value === GIAPI.Relation.BROAD_MATCH.value || relation === 'all') {
			tot += parseInt(outConcept._report.relatedConcepts_broadMatch ? outConcept._report.relatedConcepts_broadMatch : '0');
		}

		if (relation.value === GIAPI.Relation.BROADER.value || relation === 'all') {
			tot += parseInt(outConcept._report.relatedConcepts_broader ? outConcept._report.relatedConcepts_broader : '0');
		}

		if (relation.value === GIAPI.Relation.CLOSE_MATCH.value || relation === 'all') {
			tot += parseInt(outConcept._report.relatedConcepts_closeMatch ? outConcept._report.relatedConcepts_closeMatch : '0');
		}

		if (relation.value === GIAPI.Relation.EXACT_MATCH.value || relation === 'all') {
			tot += parseInt(outConcept._report.relatedConcepts_exactMatch ? outConcept._report.relatedConcepts_exactMatch : '0');
		}

		if (relation.value === GIAPI.Relation.NARROW_MATCH.value || relation === 'all') {
			tot += parseInt(outConcept._report.relatedConcepts_narrowMatch ? outConcept._report.relatedConcepts_narrowMatch : '0');
		}

		if (relation.value === GIAPI.Relation.NARROWER.value || relation === 'all') {
			tot += parseInt(outConcept._report.relatedConcepts_narrower ? outConcept._report.relatedConcepts_narrower : '0');
		}

		//Added for consistency with Relation Enum
		if (relation.value === GIAPI.Relation.NONE.value || relation === 'all') {
			tot += 0;
		}

		if (relation.value === GIAPI.Relation.RELATED.value || relation === 'all') {
			tot += parseInt(outConcept._report.relatedConcepts_related ? outConcept._report.relatedConcepts_related : '0');
		}

		if (relation.value === GIAPI.Relation.RELATED_MATCH.value || relation === 'all') {
			tot += parseInt(outConcept._report.relatedConcepts_relatedMatch ? outConcept._report.relatedConcepts_relatedMatch : '0');
		}
		return tot;
    },   
	
	/**
	 * Retrieves the descriptions of this {{#crossLink "Concept"}}concept{{/crossLink}}
	 *
	 * @method description
	 * @return {Array} An array of strings, each string is a description of this {{#crossLink "Concept"}}concept{{/crossLink}} in different languages
	 **/
	outConcept.description = function() {

		return outConcept._report.description;
	};

	/**
	 * Retrieves the name of the Controlled Vocabulary that originated this {{#crossLink "Concept"}}concept{{/crossLink}}
	 *
	 * @method sourceThesaurus
	 * @return {String} The name of the Controlled Vocabulary
	 **/
	outConcept.sourceThesaurus = function() {

		return outConcept._report.sourceThesaurus;
	};

	/**
	 * Retrieves the {{#crossLink "Relation"}}relation{{/crossLink}} used to find this {{#crossLink
	 * "Concept"}}concept{{/crossLink}} from the origin {{#crossLink "Concept"}}concept{{/crossLink}}
	 *
	 * @method rootRelation
	 *
	 * @return {Relation} The {{#crossLink "Relation"}}relation{{/crossLink}} which links this
	 * {{#crossLink "Concept"}}concept{{/crossLink}} to the origin {{#crossLink "Concept"}}concept{{/crossLink}}
	 **/
	outConcept.rootRelation = function() {

		return outConcept._report.rootRelation;
	};

	/**
	 * Generates a string representation of this {{#crossLink "Concept"}}concept{{/crossLink}}
	 *
	 * @method stringify
	 * @return {String} The string representation of this {{#crossLink "Concept"}}concept{{/crossLink}}
	 **/
	outConcept.stringify = function() {

		return JSON.stringify(outConcept._report, null, 4);
	};

	var isRelation = function(object) {

		return (object && Array.isArray(object)) ? object : null;
	};

	var isOptions = function(object) {

		return (object && (object.start || object.count)) ? object : null;
	};

	return outConcept;
};
