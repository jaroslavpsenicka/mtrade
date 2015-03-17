var services = angular.module('services', ['ngResource']);

services.factory('stats', function($resource) {
    return $resource('rest/stats/:cc', { cc: '@cc' }, {
        list: {
            method: 'GET'
        }
    });
});

services.factory('user', function($resource) {
    return $resource('rest/user', {}, {
        get: {
            method: 'GET'
        }
    });
});

var app = angular.module('MonitorApp', ['services']);

app.directive('map', function() {
    return {
        restrict: 'EAC',
        link: function(scope, element, attrs) {
            var map = $(element).vectorMap({
                map: 'world_mill_en',
                zoomButtons: false,
                zoomOnScrollSpeed: 6,
                series: {
                    regions: [{
                        scale: ['#8db6c0', '#57d9f6']
                    }]
                }
            });

            scope.$watch("stats" , function(value) {
                map.vectorMap('get', 'mapObject').series.regions[0].setValues(value ? value.tput : undefined);
            });
        }
    }
});

app.controller('UserCtrl', function($scope, user) {

    user.get({}, function(data) {
        $scope.user = data;
    });

    $scope.logoff = function() {
        window.history.back();
    };

});

app.controller('StatsCtrl', function($scope, $interval, $q, stats) {

    $scope.refresh = function() {
        stats.list({}, function(data) {
            $scope.stats = data;
        });
    };

    $scope.refresh();

});
