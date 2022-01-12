
var scripts = document.getElementsByTagName("script");
var currentScriptPath = scripts[scripts.length-1].src;
var templURL = currentScriptPath.replace('loginModule.js', 'loginTemplate.html');
var relUrl = currentScriptPath.replace('loginModule.js', '');


angular.module('loginModule', ['ngMaterial'])
.constant('MODULE_VERSION', '0.0.1')
.directive('loginWidget', function() {
    return {
           restrict: 'E',
           templateUrl: templURL,
           scope: {
                clienturl: '='
           }
    };
}).controller('gsLoginController',['$scope', '$http','$mdDialog', function($scope, $http, $mdDialog) {

    var self = this;

    self.isOpen = false;

    var cookie = document.cookie;

    self.isLoggedIn = function() {
        if (cookie != undefined) {
            var scookie = cookie.split("gput=");

            if (scookie.length > 1) {

                return true;
            }
        }

        return false;
    };


    self.getLockIcon = function() {

        var lockSrc = relUrl + 'ic_lock_white_24px.svg';

        if (self.isLoggedIn()) {

            lockSrc = relUrl + 'ic_lock_open_white_24px.svg';

        }

        return lockSrc;

    };

    self.getFABClass = function() {
        if (self.isLoggedIn()) {

            return 'md-primary';

        }

        return 'md-warn';
    };

    self.getGoogleIcon = function() {

        var lockSrc = relUrl + 'if_google_circle_color_107180.svg';

        return lockSrc;

    };

    self.getTwitterIcon = function() {

        var lockSrc = relUrl + 'if_twitter_circle_color_107170.svg';

        return lockSrc;

    };

    self.getFacebookIcon = function() {

        var lockSrc = relUrl + 'if_facebook_circle_color_107175.svg';

        return lockSrc;

    };

    self.authenticate = function(provider) {

        if (provider != undefined) {

            window.open(relUrl + '../../../auth/user/login/' + provider + '?url=' + $scope.clienturl,"_self");

        } else {
            if (self.isLoggedIn()) {

                var confirm = $mdDialog.confirm()
                .title('Confirm Logout')
                .textContent('Are you sure you want to logout?')
                .ariaLabel('Lucky day')
                .ok('Yes')
                .cancel('Cancel');

                $mdDialog.show(confirm).then(function() {

                    console.log('logout');
                    window.open(relUrl + '../../../auth/user/logout?url=' + $scope.clienturl,"_self");

                }, function() {
                    console.log('cancel');
                });
            }
        }

    };

}]);
