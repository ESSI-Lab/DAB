/**
 * This module allows to to retrieve the content of <a href="../classes/ResultSet.html">result set</a> 
 * by means of the <a href="../classes/Paginator.html" target=_blank>Paginator</a> and <a href="../classes/Page.html" target=_blank>Page</a> objects
 * 
 * @main
 * @module ResultSet
 * @submodule Retrieval
 **/

/**
 * Objects of this class can be used to retrieve the {{#crossLink "GINode"}}nodes{{/crossLink}} of the related {{#crossLink "ResultSet"}}result set{{/crossLink}}
 *
 * @class Paginator
 **/
GIAPI.Paginator = function() {
    
    if(arguments && arguments.length > 0){

        var cnstr = arguments[0][1];
        var options = arguments[0][2];
        var onStatus = arguments[0][3];
        var gidab = arguments[0][4];
    }
    
    var index;
    var paginator = {};
    paginator._id = 'paginator_' + GIAPI.random();

    /**
     *
     * Tests if subsequent {{#crossLink "Page"}}pages{{/crossLink}} in the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}} are available. If <code>execute</code> is <code>true</code>
     *  and the test is positive, retrieves the next {{#crossLink "Page"}}page{{/crossLink}} from the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}.
     *  An <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown if the last call to one of the methods
     * {{#crossLink "Paginator/prev:method"}}{{/crossLink}}, {{#crossLink "Paginator/first:method"}}{{/crossLink}},
     * {{#crossLink "Paginator/skip:method"}}{{/crossLink}} or {{#crossLink "Paginator/last:method"}}{{/crossLink}}
     *  is still running
     *
     * @method next
     * @async
     *
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {Boolean} [execute] If omitted or <code>false</code>, the method only executes the test. If <code>true</code> and the test
     * is positive, retrieves the next {{#crossLink "Page"}}page{{/crossLink}} from the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}
     *
     * @param {Array} onResponse.result This array contains a reference to this {{#crossLink "Paginator"}}paginator{{/crossLink}}
     * (see <a href="../classes/DAB.html#onResponse">here</a> for more info)
     *
     * @return {Boolean} <code>true</code> if subsequent pages are available, <code>false</code> otherwise
     **/
    paginator.next = function(onResponse, execute) {

        if (!paginator._resultSet) {
            throw "Paginator not ready";
        }

        var size = paginator._resultSet.size;
        var start = paginator._resultSet.start;
        var pageSize = paginator._resultSet.pageSize;
        var pageCount = paginator._resultSet.pageCount;
        var pageIndex = paginator._resultSet.pageIndex;

        if (!index) {
            index = start;
        }

        if (!size || pageIndex === pageCount) {
            return false;
        }

        if (!execute) {
            return true;
        }

        index += pageSize;
        if (!options) {
            options = {
            };
        }
        options.start = index;
        onResponse._origin = 'paginator';
        
        gidab._paginator(onResponse, cnstr, options, onStatus, this);

        return true;
    };

    /**
     *
     * Tests if previous pages in the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}} are available. If <code>execute</code> is <code>true</code> and the test is positive,
     * retrieves the previous {{#crossLink "Page"}}page{{/crossLink}} from the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}.
     * An <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown if the last call to one of the methods {{#crossLink "Paginator/next:method"}}{{/crossLink}},{{#crossLink "Paginator/skip:method"}}{{/crossLink}}
     *  {{#crossLink "Paginator/first:method"}}{{/crossLink}},{{#crossLink "Paginator/skip:method"}}{{/crossLink}} or {{#crossLink "Paginator/last:method"}}{{/crossLink}}
     * is still running
     *
     * @method prev
     * @async
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {Boolean} [execute] If omitted or <code>false</code>, the method only executes the test. If <code>true</code> and the test
     * is positive, retrieves the previous {{#crossLink "Page"}}page{{/crossLink}} from the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}
     *
     * @param {Array} onResponse.result This array contains a reference to this {{#crossLink "Paginator"}}paginator{{/crossLink}}
     * (see <a href="../classes/DAB.html#onResponse">here</a> for more info)
     *
     * @return {Boolean} <code>true</code> if previous pages are available, <code>false</code> otherwise
     **/
    paginator.prev = function(onResponse, execute) {

        if (!paginator._resultSet) {
            throw "Paginator not ready";
        }

        var size = paginator._resultSet.size;
        var start = paginator._resultSet.start;
        var pageSize = paginator._resultSet.pageSize;
        var pageIndex = paginator._resultSet.pageIndex;

        if (!size || pageIndex === 1) {
            return false;
        }

        if (!execute) {
            return true;
        }

        index -= pageSize;
        if (!options) {
            options = {
            };
        }
        options.start = index;
        onResponse._origin = 'paginator';
        
        gidab._paginator(onResponse, cnstr, options, onStatus, this);

        return true;
    };

    /**
     *
     * Tests if the {{#crossLink "Paginator/page:method"}}current page{{/crossLink}} is not the first of the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}. If <code>execute</code> is <code>true</code> and the
     * test is positive, retrieves the first {{#crossLink "Page"}}page{{/crossLink}} from the
     * {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}. An <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown if the last call to one of the methods {{#crossLink "Paginator/next:method"}}{{/crossLink}},
     * {{#crossLink "Paginator/prev:method"}}{{/crossLink}},{{#crossLink "Paginator/skip:method"}}{{/crossLink}} or {{#crossLink "Paginator/last:method"}}{{/crossLink}}
     * is still running
     *
     * @method first
     * @async
     *
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {Boolean} [execute] If omitted or <code>false</code>, the method only executes the test. If <code>true</code> and the test
     * is positive, retrieves the first {{#crossLink "Page"}}page{{/crossLink}} from the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}
     *
     * @param {Array} onResponse.result This array contains a reference to this {{#crossLink "Paginator"}}paginator{{/crossLink}}
     * (see <a href="../classes/DAB.html#onResponse">here</a> for more info)
     *
     * @return {Boolean} <code>true</code> if the current page is not the first of the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}, <code>false</code> otherwise
     **/
    paginator.first = function(onResponse, execute) {

        if (!paginator._resultSet) {
            throw "Paginator not ready";
        }

        var size = paginator._resultSet.size;
        var pageIndex = paginator._resultSet.pageIndex;
        if (!size || pageIndex === 1) {
            return false;
        }

        if (!execute) {
            return true;
        }

        index = 1;
        if (!options) {
            options = {
            };
        }
        options.start = index;
        onResponse._origin = 'paginator';
        
        gidab._paginator(onResponse, cnstr, options, onStatus, this);

        return true;
    };

    /**
     * Tests if the {{#crossLink "Paginator/page:method"}}current page{{/crossLink}} is not the last of the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}. If <code>execute</code> is <code>true</code> and the
     * test is positive, retrieves the last {{#crossLink "Page"}}page{{/crossLink}} from the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}.
     * An <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown if the last call to one of the methods {{#crossLink "Paginator/next:method"}}{{/crossLink}},
     * {{#crossLink "Paginator/prev:method"}}{{/crossLink}},{{#crossLink "Paginator/skip:method"}}{{/crossLink}} or {{#crossLink "Paginator/first:method"}}{{/crossLink}} is still running
     *
     * @method last
     * @async
     *
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {Boolean} [execute] If omitted or <code>false</code>, the method only executes the test. If <code>true</code> and the test
     * is positive, retrieves the last {{#crossLink "Page"}}page{{/crossLink}} from the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}
     *
     * @param {Array} onResponse.result This array contains a reference to this {{#crossLink "Paginator"}}paginator{{/crossLink}}
     * (see <a href="../classes/DAB.html#onResponse">here</a> for more info)
     *
     * @return {Boolean} <code>true</code> if the current page is not the last of the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}, <code>false</code> otherwise
     **/
    paginator.last = function(onResponse, execute) {

        if (!paginator._resultSet) {
            throw "Paginator not ready";
        }

        var size = paginator._resultSet.size;
        var pageCount = paginator._resultSet.pageCount;
        var pageIndex = paginator._resultSet.pageIndex;
        var pageSize = paginator._resultSet.pageSize;

        if (!size || pageIndex === pageCount) {
            return false;
        }

        if (!execute) {
            return true;
        }

        index = ((pageCount - 1) * pageSize) + 1;
        if (!options) {
            options = {
            };
        }
        options.start = index;
        onResponse._origin = 'paginator';
        
        gidab._paginator(onResponse, cnstr, options, onStatus, this);

        return true;
    };

    /**
     * Tests if <code>newPageIndex</code> satisfies the following statement:<br>
     * <code>newPageIndex != currentPageIndex && newPageIndex >= 1 && newPageIndex <= pageCount</code>.
     * If <code>execute</code> is <code>true</code> and the
     * test is positive, retrieves the <code>newPageIndex-th</code> {{#crossLink "Page"}}page{{/crossLink}} from 
     * the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}.
     * An <span class="flag deprecated" style="border: 1px solid black; background: yellow;">Exception</span> is thrown if the last call to one of the methods {{#crossLink "Paginator/next:method"}}{{/crossLink}},
     * {{#crossLink "Paginator/prev:method"}}{{/crossLink}}, {{#crossLink "Paginator/last:method"}}{{/crossLink}} or 
     * {{#crossLink "Paginator/first:method"}}{{/crossLink}} is still running
     *
     * @method skip
     * @async
     *
     * @param {Function} onResponse Callback function for receiving asynchronous query response
     * @param {Array} onResponse.result This array contains a reference to this {{#crossLink "Paginator"}}paginator{{/crossLink}}
     * (see <a href="../classes/DAB.html#onResponse">here</a> for more info)
     * @param {Integer} newPageIndex The index in the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}} of the {{#crossLink "Page"}}page{{/crossLink}} to retrieve
     * @param {Boolean} [execute] If omitted or <code>false</code>, the method only executes the test. If <code>true</code> and the test
     * is positive, retrieves the <code>newPageIndex-th</code> {{#crossLink "Page"}}page{{/crossLink}} from the {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}}
     *
     * @return {Boolean} <code>true</code> if <code>newPageIndex</code> satisfies the following statement:<br>
     * <code>newPageIndex != currentPageIndex && newPageIndex >= 1 && newPageIndex <= pageCount</code>,
     *  <code>false</code> otherwise
     **/
    paginator.skip = function(onResponse, newPageIndex, execute) {

        var idx = typeof onResponse != 'function' ? onResponse : newPageIndex;
        newPageIndex = parseInt(idx);

        if (!paginator._resultSet) {
            throw "Paginator not ready";
        }

        var pageCount = paginator._resultSet.pageCount;
        var pageSize = paginator._resultSet.pageSize;
        var pageIndex = paginator._resultSet.pageIndex;

        if (newPageIndex === pageIndex || newPageIndex < 1 || newPageIndex > pageCount) {
            return false;
        }
        
        if (!execute) {
            return true;
        }

        index = ((newPageIndex - 1) * pageSize) + 1;
        if (!options) {
            options = {
            };
        }
        options.start = index;
        onResponse._origin = 'paginator';
        
        gidab._paginator(onResponse, cnstr, options, onStatus, this);

        return true;
    };
    
    /**
     * Retrieves the current {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}} {{#crossLink "Page"}}page{{/crossLink}}
     *
     * @method page
     * @return {Page} The current {{#crossLink "Paginator/resultSet:method"}}result set{{/crossLink}} {{#crossLink "Page"}}page{{/crossLink}}
     */
    paginator.page = function() {

        return paginator._page;
    };
    
    // hidden method used by the PaginatorWidget
    paginator._offset = function(action){
    	
        var start = paginator._resultSet.start;
        var pageSize = paginator._resultSet.pageSize;
        var pageCount = paginator._resultSet.pageCount;
    	
    	switch(action){
    	case 'prev':
            return parseInt(index - pageSize);
    	case 'next':
    		if (!index) {
                return parseInt(start);
            }
            return parseInt(index + pageSize);
    	case 'first':
    		return 1
    	case 'last':
    		return parseInt(((pageCount - 1) * pageSize) + 1);    	
    	}
    };
       
    // hidden method used by refiner to update the constraints with the merged ones
    // after a call to refine, restore or reset methods
    paginator._cnstr = function(_cnstr){
    	
    	cnstr = _cnstr;
    };
    
    // hidden method to set the index (start) value
    // used by the refiner
    paginator._index = function(value){
    	
		index = value;
    };
    
    return paginator;
};
