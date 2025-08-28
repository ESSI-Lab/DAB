/**
 * @module UI
 **/

import { GIAPI } from '../core/GIAPI.js';

/**
 * A widget to control a <a href="../classes/Paginator.html" class="crosslink">Paginator</a> object. The following CSS is required:<pre><code>&lt;!-- API CSS --&gt;
 &lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
 &lt;!-- Font Awesome CSS --&gt;        
 &lt;link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.6.1/css/font-awesome.min.css" /&gt;<br>
</code></pre>
 * The control buttons are created using <a href="../classes/ButtonsFactory.html#method_roundCornerButton" class="crosslink">rounded corner buttons</a>.<br>
 * 
 *<pre><code> 
 *  // creates the widget with the default options
 *  var pagWidget = GIAPI.PaginatorWidget(id, onDiscoverResponse, onPagination);<br>
  // ...<br> 
  var onDiscoverResponse = function(response) {
  	var resultSet = response[0];
  	...
  	// updates the widget with the current result set
  	pagWidget.update(resultSet);
  	...
  }</pre></code> 
  
  <img style="border: none;" src="../assets/img/paginator-widget.png" />
      	
 * For CSS personalization, see the <code>paginator-widget</code> class of the <a href="https://api.geodab.eu/docs/assets/css/giapi.css">API CSS</a> file
 * 
 * @class PaginatorWidget
 * @constructor
 * 
 * @param {String} id id of an existent HTML container (typically <code>&lt;div&gt;</code> element) in which the widget is inserted
 * @param {Function} onResponse
 * @param {Object} [options]
 * 
 * @param {Integer} [options.maxOffset]
 * @param {String} [options.offsetExceededMessage]
 * @param {String} [options.offsetExceededTitle]
 * 
 *  
 **/
GIAPI.PaginatorWidget = function(id, onResponse, options) {

    var widget = {};
    
    var paginator;
    
    if(!options){
    	options = {};
    }
        
    if(!options.border){
    	options.border = '1px solid #2C3E50';
    }
    
    if(!options.borderRadius){
    	options.borderRadius = 8;
    }
    
    if(jQuery('#maxOffsetDialog').length === 0){
    	
    	// register the actions only once
        jQuery(document).on('click','[id^="pgn_but_"]',(function(){   
        	
        	 if( jQuery(this).attr('class').indexOf('disabled') > -1){
        		 return false;
        	 }

	       	 var action = jQuery(this).attr('action');
	       	 pagination(action);              
        }));
        
        jQuery(document).on('click','[id^="page_but_"]',(function(){   
        	
        	 if( jQuery(this).attr('class').indexOf('selected') > -1){
        		 return false;
        	 }

	       	 var page = jQuery(this).attr('page');
	       	 paginator.skip(onResponse,parseInt(page),true);   
	       	 
	       	 if(options.onPagination){        	
	         	options.onPagination.apply(widget, [page]);
	         }
        }));
		
		jQuery('head').append('<div id="maxOffsetDialog"/>');
	}
     
    /**
	 * Updates the widget with the <a href="../classes/Paginator.html" class="crosslink">paginator</a> of the current <a href="../classes/ResultSet.html" class="crosslink">result set</a>
	 * 
	 * @param {ResultSet} resultSet
	 * @method update
	 */
    widget.update = function(resultSet){
    	
        paginator = resultSet.paginator;
                
        jQuery('#'+id).empty();
    	
        var style = resultSet.size ? 'font-weight:bold':'';
        var __t = window.__t || function(s){ return s; };
        var results = __t('matching_results') + ': <span style="'+style+'">'+GIAPI.thousandsSeparator(resultSet.size)+'</span>';
                 
        var first = createButton('first');
        var prev =  createButton('prev');
        
        var next =  createButton('next');
        var last =  createButton('last');
        
        var resultsLabel = '<div style=""><label class="paginator-widget-results-label" id="paginator-widget-top-label">'+results+'</label>';
        
        var pageIndex = resultSet.pageIndex;
        var pageCount = resultSet.pageCount;
        
        var array = [];
        var startValue = pageIndex + 5 <= pageCount ? pageIndex : pageCount >= 5 ? pageCount - 4 : 1;
        var stop = Math.min(pageCount,5);

        for(var i = 0; i < stop; i++){
        	array.push(startValue++);
        }
        
        var pagesDiv = '<div style="display:inline-block">';
        for(var i = 0; i < stop; i++){
        	pagesDiv += createPageButton(array[i], array[i] === pageIndex);
        }
                
        var pgnDiv = '<table style="width:100%" id="paginator-div">';
        pgnDiv += '<tr><td>'+resultsLabel+'</td></tr>';
        pgnDiv += '<tr><td>'+first + prev  + pagesDiv + next + last + '</td></tr></table>';
                       
        jQuery('#'+id).append(pgnDiv);
    };
    
    var createPageButton = function(page, selected){
    	
        var width = (page+'').length <= 2 ? 20 : (page+'').length * 10;
        width = 'width:'+width+'px';
        
    	var cl = selected ? 'paginator-widget-button paginator-widget-button-selected':'paginator-widget-button'; 
		var title = !selected ? 'Skip to page '+page : '';
    	
    	return '<div title="'+title+'" page="'+page+'" id="page_but_'+page+'" style="margin-left:5px;'+width+'" class="'+cl+'">'+page+'</div>';
    };
    
    var createButton = function(action){
    	
    	var cl;
    	var divClass;
    	var title;
    	switch(action){
    	case 'first':
    		cl = 'fa-step-backward';
    		divClass = paginator.first() ? 'paginator-widget-button-first paginator-widget-button' : 'paginator-widget-button-first paginator-widget-button paginator-widget-button-disabled' ;
    		title = 'Skip to the first page';
    		break;
    	case 'prev':
    		cl = 'fa-backward';
    		divClass = paginator.prev() ? 'paginator-widget-button paginator-widget-button-prev' : 'paginator-widget-button-prev paginator-widget-button paginator-widget-button-disabled' ;
    		title = 'Back to the previous page';
    		break;
    	case 'next':
    		cl = 'fa-forward';
    		divClass = paginator.next() ? 'paginator-widget-button paginator-widget-button-next' : 'paginator-widget-button-next paginator-widget-button paginator-widget-button-disabled' ;
    		title = 'Forward to the next page';
    		break;
    	case 'last':
    		cl = 'fa-step-forward';
    		divClass = paginator.last() ? 'paginator-widget-button-last paginator-widget-button' : 'paginator-widget-button-last paginator-widget-button paginator-widget-button-disabled' ;
    		title = 'Skip to the last page';
    		break;
    	}
    	    	
    	return '<div title="'+title+'" class="'+divClass+'" action="'+action+'" style="margin-left:5px;" id="pgn_but_'+action+'"><i class="fa '+cl+'" aria-hidden="true"></i></div>';
    }
      
    var pagination = function(action) {
    	        	
        switch(action) {
            case 'next':
            	if(options.maxOffset && paginator._offset('next') > options.maxOffset){
            		showMaxOffsetDialog(options.offsetExceededTitle, options.offsetExceededMessage);
            		return;
            	}            			
                paginator.next(onResponse, true);
                break;

            case 'prev':              
                paginator.prev(onResponse, true);
                break;
                       
            case 'last': 
            	if(options.maxOffset && paginator._offset('last') > options.maxOffset){
            		showMaxOffsetDialog(options.offsetExceededTitle, options.offsetExceededMessage);
            		return;
            	}
                paginator.last(onResponse, true);
                break;

            case 'first':              
                paginator.first(onResponse, true);
                break;
        }
        
        if(options.onPagination){        	
        	options.onPagination.apply(widget, [action]);
        }
    };
    
    var showMaxOffsetDialog = function(title, desc) {
		
        jQuery("#maxOffsetDialog").empty();
        jQuery("#maxOffsetDialog").append('<br>' + desc);
        jQuery("#maxOffsetDialog").dialog({
            dialogClass: 'alert',
        	title: title,
            height : 'auto',
            width : 'auto',
            modal : true
        });
    };

    return widget;
};
