/**
 * @module Core
 */

/**
 * This kind of {{#crossLink "GINode"}}node{{/crossLink}} represents a <i>source</i> brokered by a {{#crossLink "DAB"}}{{/crossLink}} instance.
 * {{#crossLink "DAB"}}{{/crossLink}} sources can be retrieved with the {{#crossLink "DAB/sources:method"}}sources{{/crossLink}} method. Since the {{#crossLink "DAB"}}{{/crossLink}}
 * sources are <i>first level {{#crossLink "GINode"}}nodes{{/crossLink}}</i> (the DAB direct "children"), they can also be retrieved as result of the
 * first call of the {{#crossLink "GINode/expand:method"}}expand{{/crossLink}} method.
 *
 * <h4><a style="color:#30418C" name="inexsource">Including or excluding</a> a {{#crossLink "DABSource"}}source{{/crossLink}} from {{#crossLink "DAB/discover:method"}}the discover{{/crossLink}}</h4>
 *  By default all the sources brokered by the {{#crossLink "DAB"}}DAB{{/crossLink}} are included in the {{#crossLink
 * "DAB/discover:method"}}{{/crossLink}}. If a source is included, its content (depending on the given <a href="../classes/DAB.html#method_discover">constraints</a>) 
 * will be added to the {{#crossLink "ResultSet"}}result set{{/crossLink}}, otherwise it is ignored and its content is not added to the {{#crossLink "ResultSet"}}result set{{/crossLink}}.<br>
 * At least one source must be included in the {{#crossLink "DAB/discover:method"}}{{/crossLink}} so the exclusion of all the sources is equivalent to include them all
 *
 * <h4><a style="color:#30418C" name="harv">Distributed and harvested</a> content</h4>
 * 
 * The {{#crossLink "DABSource/contentType:method"}}content{{/crossLink}} of the sources brokered by the {{#crossLink "DAB"}}{{/crossLink}} can be "distributed" or "harvested":
 * <ul>
 * <li><b>distributed</b>: the content of the source is retrieved "on demand" during the {{#crossLink "DAB/discover:method"}}{{/crossLink}} process. 
 * The response time is affected by the source latency and several features (such as {{#crossLink "TermFrequency"}}term frequency{{/crossLink}}) are not available for
 * distributed content. The retrieved content is always up to date</li>
 *
 * <li><b>harvested</b>: the content of the source is permanently stored in database accessed by the {{#crossLink "DAB"}}{{/crossLink}}. 
 * The response time of a {{#crossLink "DAB/discover:method"}}{{/crossLink}} is much faster than for harvested sources and several features 
 * (such as {{#crossLink "TermFrequency"}}term frequency{{/crossLink}}) are available only for harvested content. Not all the sources content can be harvested, 
 * and (depending on the harvesting frequency) then content can be out of date</li>
 * </ul>

 * @class DABSource
 * @extends GINode
 **/
GIAPI.DABSource = function(report, dabNode) {

    var node = GIAPI.GINode(report, dabNode, dabNode.servicePath());
    var included = true;

    /**
     * Includes or excludes this {{#crossLink "DABSource"}}source{{/crossLink}} from/in the {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}
     *
     * @method include
     * @param {Boolean} include
     **/
    node.include = function(include) {

        if (include !== undefined && include !== null) {

            if (include) {
                Array.prototype.remove(dabNode._includedSources, report.id);
                dabNode._includedSources.push(report.id);

            } else if (!include) {
                Array.prototype.remove(dabNode._includedSources, report.id);
            }

            included = include;
        }

        return included || !dabNode._includedSources.length;
    };
    
    /**
     * Returns the content type of this source; see <a href="../classes/DABSource.html#harv">here</a> for more info
     * 
     * @return returns the content type of this source; possible values are "distributed" and "harvested"
     * @method contentType
     */
    node.contentType = function(){
    	
    	return node.report().harvested ? 'harvested':'distributed';   	
    };

    return node;
};
