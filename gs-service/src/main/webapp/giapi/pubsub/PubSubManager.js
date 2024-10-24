/**
 * 
 * This module provides {{#crossLink "Subscription"}}{{/crossLink}} and {{#crossLink "PubSubManager"}}{{/crossLink}} 
 * objects that can be used to take advantage of the pattern "publish/subscribe" 
 * applied to the {{#crossLink "DAB/discover:method"}}discovery{{/crossLink}} of {{#crossLink "GINode"}}nodes{{/crossLink}} 
 * with the {{#crossLink "DAB"}}DAB{{/crossLink}}.<br><br>
 * <b>Note:</b> the underlying technology requires a supported browser; please see 
 * <a href="http://www.w3schools.com/html/html5_serversentevents.asp" target=_blank>HTML 5 Server-Sent Events</a> 
 * for an updated list.
 *
 * @module PubSub
 * @main PubSub
 */

/**
 * This object allows to take advantage of the pattern "publish/subscribe" 
 * applied to the {{#crossLink "DAB/discover:method"}}discovery{{/crossLink}} of {{#crossLink "GINode"}}nodes{{/crossLink}} 
 * with the {{#crossLink "DAB"}}DAB{{/crossLink}}.
 * <b>Note:</b> the underlying technology requires a supported browser; please see 
 * <a href="http://www.w3schools.com/html/html5_serversentevents.asp" target=_blank>HTML 5 Server-Sent Events</a> 
 * for an updated list.<br><br>
 * A {{#crossLink "Subscription"}}subscription{{/crossLink}} can be made with the same set of 
 * <a href="../classes/DAB.html#constraints" class="crosslink">constraints</a>
 *  of a {{#crossLink "DAB/discover:method"}}DAB discover{{/crossLink}}. Basing on the "publish/subscribe" pattern,
 *  once a {{#crossLink "Subscription"}}subscription{{/crossLink}} is {{#crossLink "PubSubManager/subscribe:method"}}subscribed{{/crossLink}} 
 *  to the <code>PubSubManager</code>, 
 *  the {{#crossLink "DAB"}}DAB{{/crossLink}} (<i>"publisher"</i>) performs cyclically a {{#crossLink "DAB/discover:method"}}discover{{/crossLink}} 
 *  with the specified {{#crossLink "Subscription/constraints:method"}}{{/crossLink}} and {{#crossLink "Subscription/options:method"}}{{/crossLink}} and 
 *  the API client (<i>"subscriber"</i>) is updated when there are some changes on the result set. The updates are sent until the 
 *  {{#crossLink "Subscription/expiration:method"}}subscription expires{{/crossLink}} or it is {{#crossLink "PubSubManager/unsubscribe:method"}}canceled{{/crossLink}}. 
 *  
 *  <pre><code> // this function is called when there are some changes
 *  var onUpdates = function(resultSet, timeRange){<br>
	// retrieves the paginator
	var resultSet = resultSet.paginator;<br>
	// prints the result set
	document.writeln("- Result set -"); 
	document.writeln("start:"+resultSet.start+"&lt;br&gt;");
	// ... other prints  
	document.writeln('&lt;br&gt;&lt;br&gt;');<br>
	// the current paginator page (the first of the result set)    
	var page = paginator.page();<br>	
	// prints page nodes
	while(page.hasNext()){<br>	
		// retrieves the next page node
		var node = page.next();<br>	
		// retrieves the node report
		var report = node.report();<br>	     
	    // document.writeln(JSON.stringify(report,null,4));
		document.writeln(report.title+'&lt;br&gt;');
	}<br>
	document.writeln('&lt;br&gt;');
};<br>
// this function is called when the subscription expires
// here the subscription is renewed and submitted again
var onExpiration = function(event){<br>			
	subscription = subscription.renew();<br>	
	pubSub.subscribe(subscription, onUpdates, onExpiration, onConnectionError, false);
};<br>
// this function is called in case of connection errors
var onConnectionError = function(error){<br>
	var msg = '';
	switch(error){
	case 'TOO_MANY_CONNECTIONS':
		msg = 'There too many subscriptions at the moment, please wait a while for a free slot...';
		break;
	case 'CONNECTION_LOST':
		msg = 'The connection with the server is lost, please try again later'; 
		break;
	case 'SUBSCRIPTION_REJECTED':
		msg = 'A subscription with the specified label and client identifier already exists';
		break;		 
	}<br>
	document.writeln('&lt;b&gt;---&lt;/b&gt;');
	document.writeln('&lt;b&gt;'+msg+'&lt;/b&gt;');
	document.writeln('&lt;b&gt;---&lt;/b&gt;');
};<br>
// creates the DAB object
var dab = GIAPI.DAB(GIAPI.demo.local);<br>			
// get the PubSubManager from the DAB
var pubSub = dab.pubSubManager();<br>
// creates some constraints
var constraints = {			
	"where": {
		"south": -10,
		"west": -20,
		"north": 10,
		"east": 20
	},<br>		
	"when": {
		"from" : "2000-01-01",
		"to": "2013-01-01"
	},<br>
	"what": ["water"]	
};<br>
// creates an ID which identifies this API client
var clientID = 'example-client-ID';<br>
// creates a random label
var subscriptionLabel = 'label-'+GIAPI.random();<br>
// creates a subscription with the above constraints and client id
var subscription = GIAPI.Subscription(subscriptionLabel, constraints, clientID);<br>
// subscribes the subscription to the PubSubManager
pubSub.subscribe(subscription, onUpdates, onExpiration, onConnectionError);<br>	
</pre></code>
 * 
 * @class PubSubManager
 * @constructor
 */
GIAPI.PubSubManager = function(dabNode) {

	var manager = {};

	/**
	 * Submits the specified <code>subscription</code>. If the <code>timeZeroResultSet</code> parameter is <code>true</code>, the first result of the 
	 * {{#crossLink "DAB/discover:method"}}{{/crossLink}} is notified to <code>onUpdates.response</code>, 
	 * otherwise only the updates will be notified.<br>
	 * After 1 hour, the specified <code>subscription</code> {{#crossLink "Subscription/expiration:method"}}expires{{/crossLink}} 
	 * and <code>onExpiration</code> is called; once expired it can optionally be 
	 * {{#crossLink "Subscription/renew:method"}}renewed{{/crossLink}} and submitted again.<br>
	 * <b>Note:</b> Each {{#crossLink "DAB"}}DAB{{/crossLink}} instance allows a predefined maximum of 
	 * {{#crossLink "Subscription"}}subscriptions{{/crossLink}}, and if the maximum is reached <code>onConnectionError</code> is called 
	 * and the API client must wait until a free slot is available.   
	 * 
	 * @method subscribe
	 * @async
	 * 
	 * @param {Subscription} subscription the {{#crossLink "Subscription"}}subscription{{/crossLink}} for which to receive updates 
	 * @param {Function} onUpdates callback function for receiving asynchronous {{#crossLink "Subscription"}}subscription{{/crossLink}} updates 
	 * @param {ResultSet} onUpdates.resultSet <a name="result">The</a> {{#crossLink "ResultSet"}}{{/crossLink}} resulting
     * from the {{#crossLink "DAB/discover:method"}}{{/crossLink}}. In case of error (or <a href="../classes/DAB.html#timeout" class="crosslink" target=_blank>timeout</a>) during the {{#crossLink "DAB/discover:method"}}{{/crossLink}}, 
     * the {{#crossLink "ResultSet"}}{{/crossLink}} is empty and the {{#crossLink "ResultSet/error:property"}}error{{/crossLink}} 
     * property describes the problem occurred
	 * @param {TimePeriod} onUpdates.timeRange this {{#crossLink "TimePeriod"}}object{{/crossLink}} provides the time range in which the updates are included
	 * @param {Subscription} onUpdates.subscription the specified {{#crossLink "Subscription"}}{{/crossLink}} 
	 * @param {Function} onExpiration <a name="onExp">this function</a> is called when <code>subscription</code> expires
	 * @param {Subscription} onExpiration.subscription the expired {{#crossLink "Subscription"}}subscription{{/crossLink}} 
 	 * @param {Function} onConnectionError this function is called in case of connection problems
	 * @param {String} onConnectionError.error this field has one of the following values:<ul>
	 * <li><code>CONNECTION_LOST</code>: the connection with the server is lost and the updates cannot be received</li>
	 * <li><code>TOO_MANY_CONNECTIONS</code>: the maximum number of subscriptions is reached and no free slot is available at the moment</li>
	 * <li><a name="rej"><code>SUBSCRIPTION_REJECTED</code></a>: a subscription with the given label and client identifier is already subscribed</li>
	 * </ul>    
	 */
	manager.subscribe = function(subscription, onUpdates, onExpiration, onConnectionError, timeZeroResultSet) {
		
		var sources = dabNode.findSources(subscription.constraints());
		
	    var query = GIAPI.query(
	    		dabNode.endpoint(),//
	    		subscription.constraints(),//
	    		subscription.options(),//
	    		null,//
	    		null,//
	    		sources,//
	    		subscription.id(),
	    		null,
	    		dabNode.servicePath(),
	    		dabNode.openSearchPath());
	    
		if(timeZeroResultSet === null || timeZeroResultSet === undefined){
			timeZeroResultSet = true;
		}
		
		var _onUpdates = function(event){
			
			var data = JSON.parse(event.data);		
			var result = JSON.parse(data.result);
			
			var updates = result.resultSet.size;
			
		    var nodes = [];
	
	        var discoverArgs = [];
	        if(data.from){
	        	if(!discoverArgs[1]){
	        		discoverArgs[1] = {};
	        	}
	        	discoverArgs[1].from = data.from;
			}
	        
	        if(data.until){
	        	if(!discoverArgs[1]){
	        		discoverArgs[1] = {};
	        	}
	        	discoverArgs[1].until = data.until;
			}
	        discoverArgs[4] = dabNode;
	        
	        for (var i = 0; i < result.reports.length; i++) {
	
	             var report = result.reports[i];
	             nodes.push(GIAPI.GINode(report, dabNode));
	         }
	
	        var paginator = GIAPI.Paginator([discoverArgs]);
	        
	        paginator._resultSet = result.resultSet; // this field is maintained for internal use
	        result.resultSet.paginator = paginator;
	       
	        paginator._page = GIAPI.Page(nodes, paginator.pageSize);
	        
	        if(data.from){
	        	data.from = new Date(parseFloat(data.from));
	        }
	        
	        if(data.until){
	        	data.until = new Date(parseFloat(data.until));
	        }
			
	        var timeRange = {};
	        timeRange.from = data.from;
	        timeRange.to = data.until;
	        
	        var response = [];
	        response[0] = result.resultSet;
	        
	        _onUpdates.clientFuntion.apply(manager, [response,
	                                                 timeRange,
	                                                 subscription
	                                                 // ,data.query
	                                                 ]);		
		};
		
		var _onConnectionError = function(event){
			
			var msg = 'CONNECTION_LOST';
			if(event.data){
				
				msg = JSON.parse(event.data).error;
			};
			
			_onConnectionError.clientFuntion.apply(manager, [msg]);		
		};
		
		var _onExpiration = function(event){
			
			 event.target.close(); // closes the EventSource
			
			_onExpiration.clientFuntion.apply(manager, [subscription]);		
		};
		
		var encodedLabel = encodeURIComponent(subscription.label());
				
	    var subscribe = query.replace('opensearch?',
	    		'pubsub?client=js&request=subscribe'+
	    		'&clientID='+subscription.clientID()+
	    		'&subscriptionID='+subscription.id()+
	    		'&label='+encodedLabel+
	    		'&timeZeroRecordSet='+timeZeroResultSet+
	    		'&creation='+subscription.creation().getTime()+
	    		'&expiration='+subscription.expiration().getTime()+'&');

		_onUpdates.clientFuntion = onUpdates;
		_onConnectionError.clientFuntion = onConnectionError;
		_onExpiration.clientFuntion = onExpiration;

		var source = new EventSource(subscribe);
		source.addEventListener(subscription.id(), _onUpdates, false);
		source.addEventListener('expiration',_onExpiration, false);
		source.addEventListener('error', _onConnectionError, false);
		
		// called after an unsubscribe request
		source.addEventListener('close', function(event) {

			source.close();
			
        }, false);		 	 
	};
	
	/**
	 *  Renews this subscription by setting the expiration time stamp in hour from the calling of this method
	 *  
	 *  @param subscription
	 *  @param {Function} [onResponse] callback function for receiving asynchronous request response
	 */
	manager.renew = function(subscription, onUpdates, onExpiration, onConnectionError){
				
		manager.subscribe(subscription._renew(), onUpdates, onExpiration, onConnectionError, false)	
	};
	
	/**
	 * Retrieves all the {{#crossLink "Subscription"}}subscriptions{{/crossLink}} made with the specified client identifier
	 * 
	 * @method subscriptions
	 * @async
	 * 
	 * @param {String} clientID the client identifier            
	 * @param {Function} onResponse callback function for receiving asynchronous request response
	 * @param {Array/Subscription} onResponse.result an array of {{#crossLink "Subscription"}}subscriptions{{/crossLink}}
 	 * @param {String} onResponse.clientID the client identifier       
	 */
	manager.subscriptions = function(clientID, onResponse){
		
		var endpoint = dabNode.endpoint();
		if(!endpoint.endsWith('/')){
			endpoint += '/';
		}
		
   	 	var servicePath = dabNode.servicePath();
		
		jQuery.ajax({

            type : 'GET',
            url : endpoint+servicePath+'/pubsub?client=js&clientID='+clientID+'&request=subscriptions',
            crossDomain : true,
            dataType : 'jsonp',

            success : function(data, status, jqXHR) {
            	
            	var out = [];
            	for (var i = 0; i < data.length; i++) {
					
            		var json = data[i];
            		if( typeof(json) !== "object" ){
            			// it should be an object, but in some cases it is a string
            			// and it must be parsed
            			json = JSON.parse(data[i]);
            		}
            		
            	    var sub = GIAPI.Subscription(json.label,json.constraints,json.clientID);
            	    sub.creation(json.creation);
            	    sub.expiration(json.expiration);
            	    sub.id(json.id);
            	    out.push(sub);
            	}
            	
            	onResponse.apply(manager, [out,clientID]);
            },

            complete : function(jqXHR, status) {
            },

            error : function(jqXHR, msg, exception) {
            	
            	  
            }
        });
	};
	
	/**
	 * Cancels the {{#crossLink "Subscription"}}subscription{{/crossLink}} with the specified
	 *  <code>subscriptionID</code>.
	 * 
	 * See also <a href="../classes/PubSubManager.html#onExp" class="crosslink" target=_blank>onExpiration</a> parameter.<br>
	 * See also {{#crossLink "Subscription/id:method"}}Subscription id{{/crossLink}} method 
	 * 
	 * @param subscriptionID {String} the {{#crossLink "Subscription"}}subscription{{/crossLink}} identifier
	 * @param onResponse {Function} callback function for receiving asynchronous request response
	 * @param onResponse.message {String} a message with one of the following values:<ul>
	 * <li><code>OK</code>: the subscription is canceled</li>
	 * <li><code>NOT_FOUND</code>: a subscription with the specified <code>subscriptionID</code> does not exist</li>
	 * </ul>
	 * @param onResponse.subscriptionID {String} the subscription <code>subscriptionID</code>
	 * @method unsubscribe
	 * @async
	 * 
	 */
	manager.unsubscribe = function(subscriptionID, onResponse) {
		
		 var endpoint = dabEndpoint();
    	 if(!endpoint.endsWith('/')){
    		 endpoint += '/';
    	 }
    	 
    	 var servicePath = dabNode.servicePath();
		
		jQuery.ajax({

            type : 'GET',
            url : endpoint+servicePath+'/pubsub?client=js&subscriptionID='+subscriptionID+'&request=unsubscribe',
            crossDomain : true,
            dataType : 'jsonp',

            success : function(data, status, jqXHR) {
            	
            	onResponse.apply(manager, [data.message,data.subscriptionID]);
            },

            complete : function(jqXHR, status) {
            },

            error : function(jqXHR, msg, exception) {
            	
            }
        });
	};

	return manager;
};