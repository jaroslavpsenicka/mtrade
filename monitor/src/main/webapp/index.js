var services = angular.module('services', ['ngResource']);

services.factory('stats', function($resource) {
    return $resource('rest/stats', { cc: '@cc' }, {
        list: {
            method: 'GET'
        }
    });
});

services.factory('tput', function($resource) {
    return $resource('rest/stats/tput', { cc: '@cc' }, {
        list: {
            isArray: true,
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

var app = angular.module('MonitorApp', ['services', 'highcharts-ng']);

app.directive('map', function($parse) {
    return {
        restrict: 'EAC',
        link: function(scope, element, attrs) {
            var map = $(element).vectorMap({
                map: 'world_mill_en',
                zoomButtons: false,
                zoomOnScroll: false,
                series: {
                    regions: [{
                        scale: ['#8db6c0', '#57d9f6']
                    }]
                },
                onRegionClick: function (event, code) {
                    var expressionHandler = $parse(attrs.ngClick);
                    var map = $(event.currentTarget.parentElement).vectorMap('get', 'mapObject');
                    expressionHandler(scope, {'code': code, 'name': map.getRegionName(code)});
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

app.controller('StatsCtrl', function($scope, $interval, $q, stats, tput) {

    $scope.tputChart = {
        title: { text: '' },
        loading: false,
        options: {
            chart: {
                type: 'area',
                width: 290,
                height: 150,
                marginTop: 20,
                marginLeft: 40,
                marginRight: 0,
                backgroundColor: 'transparent'
            },
            legend: { enabled: false },
            plotOptions: {
                area: {
                    lineWidth: 1,
                    lineColor: '#57D9F6',
                    fillOpacity: '0.5',
                    dataLabels: { enabled: false },
                    marker: { enabled: false }
                }
            },
            xAxis: {
                type: 'datetime',
                labels: {
                    style: { color: 'lightgray' }
                }
            },
            yAxis: {
                gridLineColor: 'gray',
                label: { enabled: false },
                labels: {
                    style: { color: 'lightgray' }
                },
                title: { enabled: false }
            }
        },
        series: [{
            name: 'Throughput [hour]',
            data: []
        }]
    },

    $scope.countrySelected = function(code, name) {
        if (code) {
            $scope.countryCode = code;
            $scope.countryName = name;
            $scope.refresh(code, name);
        }
    },

    $scope.refresh = function(code) {
        stats.list({cc: code}, function(data) {
            $scope.stats = data;
        });
        tput.list({cc: code}, function(data) {
            $scope.tputChart.series[0].data = [];
            angular.forEach(data, function(value) {
               $scope.tputChart.series[0].data.push([value[0], value[1]]);
            });
        });
    };

    $scope.refresh();

});
