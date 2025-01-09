/**
 * @module PubSub 
 **/

import { GIAPI } from '../core/GIAPI.js';


/**
 * This object allows to {{#crossLink "PubSubManager/subscribe:method"}}make a subscription{{/crossLink}} to the 
 * {{#crossLink "PubSubManager"}}{{/crossLink}}.<br>
 * A subscription can be made with the same set of <a href="../classes/DAB.html#constraints" class="crosslink">constraints</a> and
 * <a href="../classes/DAB.html#options" class="crosslink">options</a>
 *  of a {{#crossLink "DAB/discover:method"}}discover{{/crossLink}}; after its {{#crossLink "Subscription/creation:method"}}{{/crossLink}}, 
 *  the subscription {{#crossLink "Subscription/expiration:method"}}expires{{/crossLink}} in 1 hour. 
 *  Once expired, the subscription can optionally be {{#crossLink "Subscription/renew:method"}}renewed{{/crossLink}} 
 *  and {{#crossLink "PubSubManager/subscribe:method"}}subscribed{{/crossLink}} again to the {{#crossLink "PubSubManager"}}{{/crossLink}}.<br><br>
 * Each subscription must be associated to the same "client identifier"; this way each API client {{#crossLink "PubSubManager/subscriptions:method"}}can retrieve{{/crossLink}} from the 
 * {{#crossLink "PubSubManager"}}{{/crossLink}} only its own subscriptions (and it will not be able to retrieve subscriptions made by other clients).<br>
 * <b>Note:</b> if a subscription with the specified <code>label</code> and <code>clientID</code> is already subscribed, this subscription will be rejected. See also
 *  <a href="../classes/PubSubManager.html#rej" class="crosslink" target=_blank>SUBSCRIPTION_REJECTED</a> error
 * <pre><code>   var constraints = {<br>
 *      "where": {
 *         "south": -10,
 *         "west": -20,
 *         "north": 10,
 *         "east": 20
 *    	},
 *      "when": {
 *      	"from" : "2000-01-01",
 *        	"to": "2013-01-01"
 *      }, 
 *      "what": "water"
 *    }<br>
 *    var clientID = 'example-client-ID';
 *    var subscription = GIAPI.Subscription('example-label',constraints,clientID);<br>
 *    var creDate = subscription.creation();
 *    var expDate = subscription.expiration();
 * </pre></code>
 * 
 * 
 * @class Subscription
 * @constructor
 * 
 * @param {String} label a label to describe this subscription. If a subscription with the specified <code>label</code> 
 * and <code>clientID</code> is already subscribed, this subscription will be rejected. See also
 *  <a href="../classes/PubSubManager.html#rej" class="crosslink" target=_blank>SUBSCRIPTION_REJECTED</a> error
 * @param {Object} constraints an object with the constraints of this subscription 
 * (see also <a href="../classes/DAB.html#constraints" class="crosslink">DAB discover constraints</a>)
 * @param {String} clientID a string which identifies this API client. If a subscription with the specified <code>label</code> 
 * and <code>clientID</code> is already subscribed, this subscription will be rejected. See also
 *  <a href="../classes/PubSubManager.html#rej" class="crosslink" target=_blank>SUBSCRIPTION_REJECTED</a> error
 * 
 **/
GIAPI.Subscription = function(_label, _constraints, _clientID) {
	
	  var subs = {};
	  var creation = new Date();
	  var expiration = new Date(creation.getTime() + ( 3600000 * 1 )); // 1 hour 3600000 // 1 min 60000;
	  var id = GIAPI.random();
	  
	  var label = _label;
	  var constraints = _constraints;
	  var options = {};
	  var clientID = _clientID;
	  
	  /**
       * Returns and/or set the label of this subscription.
       *  
       * @method label
       * @param {String} label a label to describe this subscription
       * @return {String} the label of this subscription
       */
	  subs.label = function(_label) {
		  
		  if(_label){
			  label = _label;
		  }
		  
	      return label;
      };
      
      /**
       * Returns and/or set the options of this subscription.
       *  
       * @method options
       * @param {Object} options an object with the options of this subscription 
       * (see also <a href="../classes/DAB.html#options" class="crosslink">DAB discover options</a>)
       * @return {Object} the options of this subscription
       */
	  subs.options = function(_options) {
		  
		  if(_options){
			  options = _options;
		  }
		  
          return options;
      };
	  
	  /**
       * Returns and/or set the constraints of this subscription.
       *  
       * @method constraints
       * @param {Object} constraints an object with the constraints of this subscription 
       * (see also <a href="../classes/DAB.html#constraints" class="crosslink">DAB discover constraints</a>)
       * @return {Object} the constraints of this subscription
       */
	  subs.constraints = function(_constraints) {
		  
		  if(_constraints){
			  constraints = _constraints;
		  }
		  
          return constraints;
      };

	  /**
       * Returns and/or set the client ID associated to this subscription.
       *  
       * @method clientID
       * @param {String} clientID a string to which identifies this API client
       * @return {String} the client ID associated to this subscription
       */
	  subs.clientID = function(_clientID) {
		  
		  if(_clientID){
			  clientID = _clientID;
		  }
		  
          return clientID;
      };
      
      /**
       * Returns the unique identifier of this subscription.
       * 
       * @method id
       * @return {String} the unique identifier of this subscription
       */
	  subs.id = function(_id) {// set param hidden used by PubSubManager
		  
		  if(_id){
			  id = _id; 
		  }
		  
          return id;
      };
      
      /**
       * Returns the creation <a href="http://www.w3schools.com/jsref/jsref_obj_date.asp" target="_blank">date</a> of this subscription.
       * 
       * @method creation
       * @return {<a href="http://www.w3schools.com/jsref/jsref_obj_date.asp" target="_blank">Date</a>} 
       * the creation <a href="http://www.w3schools.com/jsref/jsref_obj_date.asp" target="_blank">date</a> of this subscription
       */
      subs.creation = function(_creation){// set param hidden used by PubSubManager
    	      	  
    	  if(_creation){
    		  creation = _creation;
    	  }
    	  
    	  return creation;
      };
      
      /**
       * Returns the expiration <a href="http://www.w3schools.com/jsref/jsref_obj_date.asp" target="_blank">date</a> of this subscription;
       * the expiration takes place 1 hour after the {{#crossLink "Subscription/creation:method"}}{{/crossLink}}.<br>
       * 
       * See also {{#crossLink "Subscription/renew:method"}}renew method{{/crossLink}}
       * 
       * @method expiration
       * @return {<a href="http://www.w3schools.com/jsref/jsref_obj_date.asp" target="_blank">Date</a>} 
       * the creation <a href="http://www.w3schools.com/jsref/jsref_obj_date.asp" target="_blank">date</a> of this subscription
       */
      subs.expiration = function(_expiration){// set param hidden used by PubSubManager
    	  
    	  if(_expiration){
    		  expiration = _expiration;
    	  }
    	  
    	  return expiration;
      };
      
      // ----------------------
      // 
      //
	  subs._renew = function() {
		  
		  var sub = GIAPI.Subscription(label,constraints,clientID); 
		  
		  sub.options(options);
		  // preserves the original creation time
		  sub.creation(creation);
		  sub.id(id);
		  
		  return sub;
	  };
      
      return subs;
};