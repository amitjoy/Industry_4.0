'use strict';

(function() {

	var MODULE = angular.module('de.tum.in.led.controller',
			[ 'ngRoute', 'ngResource', 'enEasse' ]);
	var alerts = [];
	
	function error(msg) {
		alerts.push({
			type : 'error',
			msg : e
		});
	}

	MODULE.config( function($routeProvider) {
		$routeProvider.when('/', { controller: mainProvider, templateUrl: '/de.tum.in.led.controller/main/htm/home.htm'});
		$routeProvider.when('/about', { templateUrl: '/de.tum.in.led.controller/main/htm/about.htm'});
		$routeProvider.otherwise('/');
	});
	
	MODULE.run( function($rootScope, $location, en$easse) {
		$rootScope.alerts = alerts;
		$rootScope.closeAlert = function(index) {
			$rootScope.alerts.splice(index, 1);
		};
		$rootScope.page = function() {
			return $location.path();
		}
		
		en$easse.handle("led/off", function(e) {
			$rootScope.$applyAsync(function() {
				var path = "/de.tum.in.led.controller/images/";
				$rootScope.path = path + "bulb-off.png";
			});
		}, error );
		
		en$easse.handle("led/on", function(e) {
			$rootScope.$applyAsync(function() {
				var path = "/de.tum.in.led.controller/images/";
				$rootScope.path = path + "bulb-on.png";
			});
		}, error );
	});
	
	
	
	var mainProvider = function($scope, $http) {
		
		$scope.upper = function() {
			var name = prompt("Under what name?");
			if ( name ) {
				$http.put('/rest/topic', {time: new Date()}).then(
					undefined, function(d) {
						$scope.alerts.push( { type: 'danger', msg: 'Failed with ['+ d.status + '] '+ d.statusText });
					}
				);	
			}
		};
		
	}
	
})();
