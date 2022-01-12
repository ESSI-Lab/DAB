var gsInitApp = angular.module('gsConfApp', [ 'ngMaterial', 'ngAnimate',
		'ngMessages','ngMaterialDatePicker','loginModule' ])
        .config(function($mdThemingProvider) {
            $mdThemingProvider.theme('default').primaryPalette('blue', {
                'default' : '700'
            }).accentPalette('orange').warnPalette('red');

            $mdThemingProvider.theme('forest').primaryPalette('green').accentPalette(
                    'green');
        })
		.directive('esOlist', function() {
           return {
                   restrict: 'A',
                   transclude: true,
                   templateUrl: 'templates/optionsListTemplate.html',
                   scope : {
                        component : '=',
                        componentkeyprefix : '=',
                        tstest : '=',
                        save : '&',
						toggleall: '&',
						ischecked: '&',
						toggle: '&',
						areallselected: '&',
						isindeterminate: '&',
                        changevalueconcrete : '&'
                   }
           };
        })
		.directive('esDisplayOption', function() {
           return {
                   restrict: 'A',
                   transclude: true,
                   templateUrl: 'templates/displayOptionInput2.html',
                   scope : {
                        ao : '=',
                        ck : '=',
                        ckeyprefix : '=',
                        save : '&',
						toggleall: '&',
						ischecked: '&',
						toggle: '&',
						areallselected: '&',
					    isindeterminate: '&',
                        changevalueconcrete : '&'
                   }
           };
        })
		.controller('gsConfAppController',['$scope','$http','$log','$timeout','$sce','$mdDialog',function($scope, $http, $log, $timeout, $sce, $mdDialog) {

            self = this;
            self.configuration;
            self.oldConfiguration;
            self.options = {};
            self.addNewSource = false;
            self.addNewBatchJob = false;
            self.configurableAccessors = {};
            self.mustFlush = false;
            self.unauthorized = true;

            self.locationurl = window.location.origin;

            self.fabMenuOpen = false;         
            
            var confReceived = function(nconf) {

				var json = nconf;

				self.configuration = $.extend(true, {}, json);

				self.oldConfiguration = $.extend(true, {}, json);			
            };

            var setUnauthorized = function(auth) {
                self.unauthorized = auth;
            }

            $http({
                method : 'GET',
                url : '../services/admin/configuration',

                transformResponse : function(value) {

                    return JSON.parse(value, function (key, value) {

                        if (this.value != undefined) {

                            if (this.type === 'java.util.Date') {

                                this.value = new Date(this.value);

                            }

                        }

                        return value;

                    });

                }

            }).then(
                    function successCallback(response) {

                        setUnauthorized(false);

                        var json = response.data;

                        confReceived(json);

                    },
                    function errorCallback(response) {

                        var msg = 'Error loading the coinfiguration';
                        var t = 'Error';

                        if (response.data) {
                            if (response.data.Error) {
                                msg = response.data.Error;
                            }
                        }

                        if (response.status === 401) {
                            t = 'Unauthorized';
                            setUnauthorized(true);

                        }

                        $scope.showAlert(msg, t);

                        return;
            });

            $scope.flush = function(ev) {

                console.log('flush');
                showWaiting();

                $http({
                    method : 'POST',
                    url : '../services/admin/configuration?',

                    data : self.configuration,

                    transformResponse : function(value) {

                        return JSON.parse(value, function (key, value) {

                            if (this.value != undefined) {

                                if (this.type === 'java.util.Date') {

                                    this.value = new Date(this.value);

                                }

                            }

                            return value;

                        });

                    }

                }).then(


                    function successCallback(
                            response) {
                        hideWaiting();

                        confReceived(response.data);

                        self.mustFlush = false;

                    },
                    function errorCallback(response) {
                        hideWaiting();
                        var msg = 'Error loading the coinfiguration';

                        if (response.data) {
                            if (response.data.Error) {
                                msg = response.data.Error;
                            }
                        }

                        $scope.showAlert(msg);
                });

            };

            $scope.settingsClick = function(accKey) {

                if (self.configurableAccessors[accKey] === undefined) {
                    self.configurableAccessors[accKey] = {};
                }

                if (self.configurableAccessors[accKey].configure)
                    self.configurableAccessors[accKey].configure = false;
                else
                    self.configurableAccessors[accKey].configure = true;
            };

            $scope.removeSource = function(accKey) {
                var msg = "Are you sure you want to remove this source? This action is not undoable."
                $scope.showConfirm(msg, function() {
                    console.log('Deleting source with id ' + accKey);

                    doRemoveSourceAction(accKey);
                });
            };

            var doRemoveSourceAction = function(sid) {

                console.log('Remove Source: ' + sid);

                $http({ method : 'DELETE',
                        url : '../services/admin/source?',
                        data : {
                            "context" : self.oldConfiguration,
                            "source" : sid
                        },
                        transformResponse : function(value) {

                            return JSON.parse(value, function (key, value) {

                                if (this.value != undefined) {

                                    if (this.type === 'java.util.Date') {

                                        this.value = new Date(this.value);

                                    }

                                }

                                return value;

                            });

                        }
                      }
                ).then(function successCallback(response) {

                        var json = response.data;

                        confReceived(json);

                        self.mustFlush = true;


                      },function errorCallback(response) {
                        var msg = 'Error loading the coinfiguration';

                        if (response.data) {
                            if (response.data.Error) {
                                msg = response.data.Error;
                            }
                        }

                        $scope.showAlert(msg);
                });
            };

            var showWaiting = function () {
                $scope.showWaitMsg();
            };

            var hideWaiting = function () {
                $scope.hideWaitMsg();
            };

            var sendOptions = function(opts) {

                showWaiting();

                $http({
                    method : 'POST',
                    url : '../services/admin/option?',
                    data : {
                        "context" : self.oldConfiguration,
                        "options" : opts
                    },
                    transformResponse : function(value) {
                        return JSON.parse(value, function (key, value) {

                            if (this.value != undefined) {

                                if (this.type === 'java.util.Date') {

                                    this.value = new Date(this.value);

                                }

                            }

                            return value;

                        });
                    }
                }).then(function successCallback(response) {

                                hideWaiting();

                                var json = response.data;

                                confReceived(json);

                                self.mustFlush = true;

                        }, function errorCallback(response) {
                                hideWaiting();
                                var msg = 'Error loading the coinfiguration';
                                if (response.data) {
                                    if (response.data.Error) {
                                        msg = response.data.Error;
                                    }
                                }
                                $scope.showAlert(msg);
                        });
            };

            $scope.doAddSourceAction = function(ev, sop) {

                console.log('Adding new Source: ' + sop.label);
                showWaiting();

                $http({ method : 'POST',
                        url : '../services/admin/source?',
                        data : {
                            "context" : self.oldConfiguration,
                            "source" : sop
                        },
                        transformResponse : function(value) {

                            return JSON.parse(value, function (key, value) {

                                if (this.value != undefined) {

                                    if (this.type === 'java.util.Date') {

                                        this.value = new Date(this.value);

                                    }

                                }

                                return value;

                            });

                        }
                      }
                ).then(function successCallback(response) {
                        hideWaiting();

                        var json = response.data;

                        confReceived(json);

                        self.mustFlush = true;


                      },function errorCallback(response) {

                        hideWaiting();
                        var msg = 'Error loading the coinfiguration';

                        if (response.data) {
                            if (response.data.Error) {
                                msg = response.data.Error;
                            }
                        }

                        $scope.showAlert(msg);
                });
            };

            $scope.doSaveOptionAction = function(componentKey, supported) {

                var ddd = new Date('2016-01-01');

                var optionKey = supported.key;

                console.log('save ' + optionKey);

                console.log('component ' + componentKey);

                var concrete = supported.concrete;

                var label = supported.label;

                if (supported.type === 'eu.essi_lab.model.BrokeringStrategy' ||
                    supported.type === 'eu.essi_lab.model.configuration.Subcomponent' ||
                    supported.type === 'eu.essi_lab.jobs.scheduler.GS_JOB_INTERVAL_PERIOD' ||
                    supported.type === 'eu.essi_lab.model.StorageUri' ||
                    supported.type === 'eu.essi_lab.model.ResultsPriority') {

                    supported.value.valueConcrete =  supported.type
                }

                var opts = {};
                opts[componentKey] = supported;

                sendOptions(opts);
            };

            $scope.doChangeValueConcrete = function(opt, concrete) {

                if (opt.value === undefined)
                    opt.value = {};

                opt.value.valueConcrete = concrete;
            };

            $scope.doNewSourceAction = function() {

                self.addNewSource = true;
            };

            $scope.doNewBatchJobAction = function() {

                self.addNewBatchJob = !self.addNewBatchJob;
                if (self.addNewBatchJob) {

                    self.configuration.configurableComponents['BATCH_JOBS_KEY'].supportedOptions['BATCH_JOB_KEY'].value = null;


                }

            };

            $scope.guid = guid();

            $scope.hideWaitMsg = function(ev) {
			
                $mdDialog.hide();
            };
			
			// ------------------------------------
			//
			// SubcomponentList widget 
			//
				     
			$scope.doToggle = function (key, ao, label) {
            
				var subComponents = ao.value.subcomponents;
				 				
				for(var index in subComponents){
				
					if(subComponents[index] && subComponents[index].label === label){
					
						 subComponents.splice(index, 1);
						 
						 // console.log(JSON.stringify(ao,' ',4));
						 
						 $scope.doSaveOptionAction(key, ao);
						 						 
						 return;
					}
				};
				
			    var allowed = ao.allowedValues[0].subcomponents;
				
				for(var index in allowed){
				
					if(allowed[index] && allowed[index].label === label){
					
						 subComponents.push(allowed[index]);
						 
						 // console.log(JSON.stringify(ao,' ',4));
						 
						 $scope.doSaveOptionAction(key, ao);
						 
						 return;
					}
				}			
	        };
			
            $scope.doIsChecked = function(ao, label) {
			
				var subComponents = ao.value.subcomponents;
				
				for(var index in subComponents){
				
					if(subComponents[index] && subComponents[index].label === label){
						return true;
					}
				}
			
                return false;
            };
						 
            $scope.doToggleAll = function(key, ao) {
			
			    var subComponents = ao.value.subcomponents;
			    var allowed = ao.allowedValues[0].subcomponents;

			    if(subComponents.length === allowed.length){
					
					ao.value.subcomponents = [];
					
					// console.log(JSON.stringify(ao,' ',4));

					$scope.doSaveOptionAction(key, ao);
					return;
				}            

				ao.value.subcomponents = allowed;
				
				console.log(JSON.stringify(ao,' ',4));

				$scope.doSaveOptionAction(key, ao);					
            };
			
		    $scope.areAllSelected = function(ao) {
						
				var subComponents = ao.value.subcomponents;
			    var allowed = ao.allowedValues[0].subcomponents;
				
				return subComponents.length === allowed.length;
			};
			
			$scope.isIndeterminate = function(ao) {
						
				var subComponents = ao.value.subcomponents;
			    var allowed = ao.allowedValues[0].subcomponents;
				
				var ind = subComponents.length > 0 && subComponents.length < allowed.length;
			    
				// console.log("Indeterminate: "+ind);

				return ind;
			}

			//
			// ------------------------------------

            $scope.showWaitMsg = function(ev) {

                /*var tit = 'Please Wait';

                $mdDialog.show($mdDialog
                    .alert()
                    .parent(angular.element(document.querySelector('#popupContainer')))
                    .clickOutsideToClose(false)
                    .title(tit)
                    .textContent("Setting Option in progress...")
                    .ariaLabel('Alert Dialog Demo')
                    //.ok('OK')
                    .targetEvent(ev));
                */



               var parentEl = angular.element(document.querySelector('#popupContainer'));
               $mdDialog.show({
                 parent: parentEl,
                 targetEvent: ev,
                 template:
                   '<md-dialog aria-label="List dialog">' +
                   '  <md-toolbar>' +
                   '       <div class="md-toolbar-tools">' +
                   '         <h2>Option Setting in progress...</h2>' +
                   '        <span flex></span>' +
                   '       </div>' +
                   '     </md-toolbar>' +
                   '<br/>' +
                   //'<br/>' +
                   '  <md-dialog-content>'+
                   '    <div layout="row" layout-sm="column" layout-align="space-around">' +
                   '      <md-progress-circular md-mode="indeterminate"></md-progress-circular>' +
                   '    </div>' +
                   '    <br/>' +
                   '  </md-dialog-content>' +


                   '</md-dialog>'
              });



            };

            $scope.showAlert = function(msg, title, ev) {

                var tit = 'Error';

                if (title != undefined) {
                    tit = title;
                }

                $mdDialog.show($mdDialog
                    .alert()
                    .parent(angular.element(document.querySelector('#popupContainer')))
                    .clickOutsideToClose(true)
                    .title(tit)
                    .textContent(msg)
                    .ariaLabel('Alert Dialog Demo')
                    .ok('OK')
                    .targetEvent(ev));
            };

            $scope.showConfirm = function(msg, okFunct) {
                $mdDialog.show($mdDialog
                    .confirm()
                    .parent(angular.element(document.querySelector('#popupContainer')))
                    .clickOutsideToClose(true)
                    .title('Information')
                    .textContent(msg)
                    .ariaLabel('Alert Dialog Demo')
                    .ok('OK')
                    .cancel('Cancel')
                ).then(function(answer){
                    console.log(answer);
                    okFunct.apply();
                }, function() {
                    console.log('cancel')
                });
            };

		}])
        .run(function($templateRequest) {

            var urls = [ 'img/ic_add_circle_48px.svg',
                    'img/ic_settings_48px.svg',
                    'img/ic_highlight_off_48px.svg',
                    'img/ic_remove_circle_48px.svg' ];

            angular.forEach(urls, function(url) {
                $templateRequest(url);
            });

		});
