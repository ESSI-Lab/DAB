/**
 * @module UI
 **/

/**
 *  This widget retrieves the available {{#crossLink "DAB/sources:method"}}sources{{/crossLink}} during its initialization, and allows to select which
 *  of them to {{#crossLink "DABSource/include:method"}}include/exclude{{/crossLink}} from the {{#crossLink "DAB/discover:method"}}{{/crossLink}} by means of switches.<br>
 *  It also allows to group {{#crossLink "DABSource"}}sources{{/crossLink}} in order to {{#crossLink "DABSource/include:method"}}include/exclude{{/crossLink}}
 *   all the {{#crossLink "DABSource"}}sources{{/crossLink}} of the selected group.<br>
 *
 *  A set of {{#crossLink "DABSource"}}sources{{/crossLink}} to {{#crossLink "DABSource/include:method"}}{{/crossLink}} in the {{#crossLink "DAB/discover:method"}}{{/crossLink}} can be selected
 *  also during the initialization with the <code>options.include</code> function. The selection is effectively applied when the call to the {{#crossLink "DAB/sources:method"}}sources{{/crossLink}} method is ready;
 *  this event is fired with the <code>options.onSourcesReady</code> function. In order to apply the selection, the first {{#crossLink "DAB/discover:method"}}{{/crossLink}} call must be
 *  performed after the event has been generated.<br>

 *  The following CSS is required:<pre><code>
 &lt;!-- API CSS --&gt;
 &lt;link rel="stylesheet" type="text/css" href="https://api.geodab.eu/docs/assets/css/giapi.css" /&gt;<br>
 </code></pre>
 *  <img style="border: none;" src="../assets/img/sources-widget.png" /><br>
 <i>The image above shows a <code>SourcesWidget</code> (the header is not part of the widget)</i>

 *  <pre><code>  // creates the widget with two groups
 var sourcesWidget = GIAPI.SourcesWidget(id, dabNode,{
 'groups':{
 'In situ data': ["Source title 1","Source title 2","Source title 3"],
 'Satellite data': ["Source title 4","Source title 5","Source title 6"]
 }
 });<br>
 // creates the widget with an initial set of sources
 var sourcesWidget2  = GIAPI.SourcesWidget(id, dabNode,{
 // defines an initial set of sources
 'include': function(source){
 return source.report().title === 'Source Title 1'
 || source.report().title === 'Source Title 2';
 },
 // automatically starts the discover when the sources are ready
 // in order to apply the initial set of sources
 'onSourcesReady':  function(sources){
 dabNode.discover(onDiscoverResponse, options);
 }
 });</pre></code>
 *

 * <img style="border: none;" src="../assets/img/sources-group-widget.png" /><br>
 <i>The image above shows a <code>SourcesWidget</code> with 2 groups set (the header is not part of the widget)</i> </i>
 *
 *  See also the <code>sources-widget</code> class in the <a href="https://api.geodab.eu/docs/assets/css/giapi.css">API CSS</a> file
 *
 *
 * @class SourcesWidget
 * @constructor
 *
 * @param {String} id
 * @param {DAB} dabNode
 * @param {Object} [options]
 *
 * @param {Integer} [options.width=375]
 * @param {Integer} [options.height=210]
 *
 * @param {Function} [options.include] a function which return <code>true</code> if the given {{#crossLink "DABSource"}}{{/crossLink}} must be included
 * @param {DABSource} [options.include.source] the {{#crossLink "DABSource"}}{{/crossLink}} to check

 * @param {Function} [options.onSourcesReady] callback function called when the {{#crossLink "DABSource"}}sources{{/crossLink}} method is ready
 * @param {DABSource[]} [options.onSourcesReady.sources] the DAB sources

 * @param {Object} [options.groups] comma separated list of {{#crossLink "DABSource"}}sources{{/crossLink}} groups (see the code snippet above for more details)
 * 
 * @param {String} [options.viewId] a "server-side" view identifier
 *
 */
GIAPI.ODIPSourcesWidget = function(id, dabNode, options) {

    var widget = {};
    var sourcesCount = 0;

    if (!options) {
        options = {};
    }

    if (!options.width) {         
        options.width = 'width: 375px;'; 
    }else if(options.width === 'none'){
    	options.width = '';
    }else{
    	options.width = 'width: '+options.width+'px;';
    }

    if (!options.height) {
    	options.height = 'height: 210px;';      
    }else if(options.height === 'none'){
    	options.height = '';
    }else{
    	options.height = 'height: '+options.height+'px;';
    }

    if (!options.include) {
        options.include = function() {
            return true;
        };
    }
    
    /**
     * Returns the number of selected {{#crossLink "DAB/sources:method"}}sources{{/crossLink}} 
     * 
     * @return the number of selected {{#crossLink "DAB/sources:method"}}sources{{/crossLink}} 
     * @method sourcesCount
     */
    widget.sourcesCount = function(){
        
        return sourcesCount;
    };

    var includeSources = function(sources, check) {

        var indexes = jQuery(check).prop('id').replace('check_', '').split('_');
        indexes.forEach(function(index) {
            var source = sources[index];
            source.include(jQuery(check).is(":checked"));
        });

        //if (!options.groups) {
            if (check.checked) {
                sourcesCount++;
            } else {
                sourcesCount--;
            }
            if (!options.groups) {
            setSelectedSources(sources);
            }
    };

    dabNode.sources(function(sources, error) {

        if (error && options.errorHandler) {
            options.errorHandler.apply(this, [error]);
            return;
        }
               
        var table = '<table class="sources-widget" style="' + options.width + options.height + '">';

        if (!options.groups) {

            // control for all the sources
            var checkAllTable = '<table>';

            checkAllTable += '<tr>';
            var checkId = 'check_all';
            checkAllTable += '<td style="vertical-align: bottom;"><label class="sources-widget-all-label" id="check_all_label"></label></td>';

            checkAllTable += '<td>';
            checkAllTable += '<div style="margin-left: 320px; margin-top:5px; margin-bottom:-5px">' + GIAPI.ButtonsFactory.onOffSwitchButton('IN', 'OUT', {
                'id' : checkId,
                'size' : 'small',
                'offBckColor': '#11aeb1',
                'onBckColor': 'white',
                'offColor': 'white',
                'onColor': '#25418f',
                'switchColor': '#25418f',
                'switchBorderColor': '#25418f',
                'borderColor': 'transparent'
            });
            checkAllTable += '</div></td>';
            checkAllTable += '</tr></table>';
            
            table += '<tr><td colspan=3>'+checkAllTable+'</td></tr>';

            // table += '<tr>';
            // var checkId = 'check_all';
            // table += '<td>';
            // table += '<div style="margin-bottom:-5px">'+GIAPI.ButtonsFactory.onOffSwitchButton('IN','OUT',{
            // 'id': checkId,
            // 'size': 'small'
            // });
            // table += '</div></td>';
            // table += '<td id="check_all_label" style="vertical-align: middle;font-weight: bold;color:red"></td>';
            // table += '</tr>';

            jQuery(document).on('click', '#check_all', function(event) {

                if (jQuery('#check_all').is(':checked')) {
                     sourcesCount = sources.length;
                } else {
                     sourcesCount = 0;
                }
                setSelectedSources(sources);
                for (var i = 0; i < sources.length; i++) {
                    var checkId = 'check_' + i;
                    jQuery('#' + checkId).prop('checked', this.checked);
                    sources[i].include(this.checked);
                };
            });

            // controls for single source
            for (var i = 0; i < sources.length; i++) {

                var source = sources[i];
                var title = source.report().title;

                table += '<tr>';
                
                var targetDivId = GIAPI.random();
                var infoButtonId = 'source-info-'+source.uiId;       
                var toggleButton = GIAPI.ToggleButton({
         			'id': infoButtonId,
         			'width': 18,
         			'targetId': targetDivId,
         			'offLabel':'',
         			'onLabel':'',
         			'attr':[{ name:'title', value:'Source info'  }, 
         			        { name:'init', value:'false' },
         			        { name:'target', value:targetDivId }
 			        ],
 			        'source': source, 
         			'beforeStart': function(){
         			    
                        var init = this.attr[1].value;
                        if(init === 'false'){
                        	
 	                        var targetId = this.attr[2].value;	                                                
	                        this.sourceInfo = GIAPI.Source_UINode(this.source,targetId);	     
	                        
 	                  	    jQuery('#'+targetId+' > table').css('margin-left','-20px');
	                  	    jQuery('#'+targetId+' > table').css('margin-top','10px');
	                  	    jQuery('#'+targetId+' > table').css('border','1px solid white');
                        }  				
         			},
         			'onComplete': function(){
         				
         				 var init = this.attr[1].value;
                         if(init === 'false'){
                         	 this.attr[1].value = 'true';
                        	 this.sourceInfo.updateMap();
                        }
         			}
                });
                               
                toggleButton.stateIcon('on','fa-info-circle');
                toggleButton.stateIcon('off','fa-info-circle');
                toggleButton.css('div','padding','0px');
                toggleButton.css('div','background','transparent');            
                toggleButton.css('icon','color','#1A237E');
                toggleButton.css('icon','margin-left','2px');
                toggleButton.css('icon','font-size','15px');
                toggleButton.css('icon','vertical-align','middle');
   
                table += '<td style="width: 18px;" title="Source info">'+toggleButton.div()+'</td>';
  
                var checkId = 'check_' + i;
               
                table += '<td title="' + (source.report().title) + '" style="vertical-align: bottom;"><label class="sources-widget-src-label">' + title + '</label></br>';
             
                var infoDiv = '<div id="'+targetDivId+'" style="display:none"></div>';               
                table += infoDiv + '</td>';
                
 
                table += '<td style="width: 40px;">';

                var checked = options.include(source);
                source.include(checked);
                if (checked) {
                    sourcesCount++;
                }

                table += '<div style="margin-bottom:-5px; margin-left:-3px">' + GIAPI.ButtonsFactory.onOffSwitchButton('IN', 'OUT', {
                    'id' : checkId,
                    'checked' : checked,
                    'size' : 'small',
                    'offBckColor': '#11aeb1',
                    'onBckColor': 'white',
                    'offColor': 'white',
                    'onColor': '#25418f',
                    'switchColor': '#25418f',
                    'switchBorderColor': '#25418f',
                    'borderColor': 'transparent'
                });

                jQuery(document).on('click', '#' + checkId, function(event) {
                    includeSources(sources, this);
                });

                if (title.length > 55) {
                    title = title.substring(0, 55) + '...';
                }
                table += '</div></td>';
                


                table += '</tr>';
            }
            
            if( sourcesCount === 0){
            	sourcesCount = sources.length;
            }

        } else {
            
            
            // groups
            var groupCount = 0 ;
            for (var name in options.groups) {

                table += '<tr>';

                var checkId = 'check_';

                var labels = options.groups[name];
                labels.forEach(function(label) {

                    for (var i = 0; i < sources.length; i++) {

                        var source = sources[i];
                        var title = source.report().title;

                        if (title === label) {
                            checkId += i + '_';
                        }
                    }
                });
                checkId = checkId.substring(0, checkId.length - 1);

                table += '<td>';
                table += '<div>' + GIAPI.ButtonsFactory.onOffSwitchButton('YES', 'NO', {
                    'id' : checkId,
                    'size' : 'small',
                    'offBckColor': '#11aeb1',
                    'onBckColor': 'white',
                    'offColor': 'white',
                    'onColor': '#25418f',
                    'switchColor': '#25418f',
                    'switchBorderColor': '#25418f',
                    'borderColor': 'transparent'
                });

                jQuery(document).on('click', '#' + checkId, function(event) {
                    includeSources(sources, this);
                });

                table += '</div></td>';
                table += '<td title=' + name + ' style="cursor:default; vertical-align: middle; ">' + name + '</td>';
                table += '</tr>';
                groupCount++;
            }
            sourcesCount = groupCount;
        }

        table += '</table>';
        
        
//        if (checkAllTable) {
//            jQuery('#' + id).append(checkAllTable);
//        }

        jQuery('#' + id).append(table);
        
        if (!options.groups) {
            setSelectedSources(sources);
        }
        if (options.onSourcesReady) {
            options.onSourcesReady.apply(widget, [sources]);
        }
    }, options.viewId);
    
    var setSelectedSources = function(sources){
        
        if (sourcesCount === sources.length) {
            
             jQuery('#check_all_label').html('<b>All</b> sources selected');
             
        } else if(sourcesCount > 0){
            
            var s = sourcesCount > 1 ? 's':'';
            jQuery('#check_all_label').html('<b>'+sourcesCount + '</b> source'+s+' selected');
             
        } else {
            
            jQuery('#check_all_label').html('<b >No</b> sources selected');
        }
    };

    return widget;

};