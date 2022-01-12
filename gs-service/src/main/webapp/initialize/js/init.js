// create angular app
var gsInitApp = angular.module('gsInitApp', [ 'ngMaterial', 'ngAnimate',
		'ngMessages' ]);

gsInitApp.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('blue', {
		'default' : '700'
	}).accentPalette('orange');
});

gsInitApp.directive('elemReady', function($parse) {
	return {
		restrict : 'A',
		link : function($scope, elem, attrs) {
			elem.ready(function() {
				$scope.$apply(function() {
					var func = $parse(attrs.elemReady);
					func($scope);
				})
			})
		}
	}
});
gsInitApp.directive('dynamic', function($compile) {
	return {
		restrict : 'A',
		replace : true,
		link : function(scope, ele, attrs) {
			scope.$watch(attrs.dynamic, function(html) {
				ele.html(html);
				$compile(ele.contents())(scope);
			});
		}
	};
});

// create angular controller
gsInitApp.controller('gsInitAppController',

[

'$scope', '$http', '$log', '$timeout', '$sce', '$mdDialog',

function($scope, $http, $log, $timeout, $sce, $mdDialog) {

	self = this;
	self.success = false;
	self.providers;

	$scope.openurl = function(url){
        window.open(url, '_blank');
    }

    self.useExistingTrue =  self.useExisting == 'true';

	$scope.submitForm = function() {

		var urltosend = self.inputURL;
		var inputUseExisting = self.useExisting;
		var inputrootuser = self.rootuser;
		var oauthProviderName = self.selectedprovider;

		var oauthProviderId = self.oauthProviderId;

		var oauthProviderSecret = self.oauthProviderSecret;

		$http({
			method : 'POST',
			url : '../services/admin/init?',

			data : {
				useExisting : inputUseExisting,
				url : urltosend,
				rootUser : inputrootuser,
				oauthProviderName : oauthProviderName,
				oauthProviderId : oauthProviderId,
				oauthProviderSecret : oauthProviderSecret
			},

			transformResponse : function(value) {

				return JSON.parse(value);

			}

		}).then(function successCallback(response) {


			var json = response.data;

			if (json.result === 'SUCCESS') {
				self.success = true;
			} else {
				$scope.showAlert(json.message);
			}


		}, function errorCallback(response) {

			return;
		});
	};


	$scope.retrieveOAuthProviders = function() {

		$http({
			method : 'GET',
			url : '../services/admin/oauthproviders',

			transformResponse : function(value) {

				return JSON.parse(value);

			}

		}).then(function successCallback(response) {

			var json = response.data;

			console.log(JSON.stringify(json));

			self.providers = json;


		}, function errorCallback(response) {

			return;
		});
	};
	$scope.retrieveOAuthProviders();


	
	$scope.showAlert = function(msg, ev) {
	    // Appending dialog to document.body to cover sidenav in docs app
	    // Modal dialogs should fully cover application
	    // to prevent interaction outside of dialog
	    $mdDialog.show(
	      $mdDialog.alert()
	        .parent(angular.element(document.querySelector('#popupContainer')))
	        .clickOutsideToClose(true)
	        .title('Error')
	        .textContent(msg)
	        .ariaLabel('Alert Dialog Demo')
	        .ok('OK')
	        .targetEvent(ev)
	    );
	  };

} ]);
