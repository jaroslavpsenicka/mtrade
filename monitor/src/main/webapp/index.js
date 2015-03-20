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
    setInterval(function() {
        $scope.refresh();
    }, 900000);

});

app.controller('JmxCtrl', function($scope, $interval, $q, stats, tput) {

    $scope.jmx = new Jolokia("jmx");
    $scope.req = [
        { type: "read", mbean: "java.lang:type=OperatingSystem", attribute: "SystemCpuLoad"},
        { type: "read", mbean: "kafka.server:type=BrokerTopicMetrics", attribute: "BytesInPerSec"},
        { type: "read", mbean: "kafka.server:type=BrokerTopicMetrics", attribute: "BytesOutPerSec"}
    ];

    $scope.loadChart = {
        title: { text: '' },
        loading: false,
        options: {
            chart: {
                type: 'area',
                width: 200,
                height: 100,
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
                min: 0,
                max: 100,
                label: { enabled: false },
                labels: {
                    style: { color: 'lightgray' }
                },
                title: { enabled: false }
            }
        },
        series: [{
            name: 'System load',
            data: []
        }]
    };

    $scope.topicChart = {
        title: { text: '' },
        loading: false,
        options: {
            chart: {
                type: 'area',
                width: 500,
                height: 100,
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
                min: 0,
                label: { enabled: false },
                labels: {
                    style: { color: 'lightgray' }
                },
                title: { enabled: false }
            }
        },
        series: [{
            name: 'Intake',
            data: []
        }, {
            name: 'Exhaust',
            data: []
        }]
    }

    $scope.refresh = function(code) {
        var resp = $scope.jmx.request($scope.req);
        $scope.refreshLoadChart(resp[0]);
        $scope.refreshTopicChart(resp[1], resp[2]);
    };

    $scope.refreshLoadChart = function(value) {
        var serie = $scope.loadChart.getHighcharts().series[0];
        serie.addPoint({x: value.timestamp * 1000, y: ((value.value * 1000) / 10)}, true, serie.data.length >= 60);
    }

    $scope.refreshTopicChart = function(intake, exhaust) {
        var serie1 = $scope.topicChart.getHighcharts().series[0];
        serie1.addPoint({x: intake.timestamp * 1000, y: intake.value.used}, true, serie1.data.length >= 200);
        serie2 = $scope.topicChart.getHighcharts().series[1];
        serie2.addPoint({x: exhaust.timestamp * 1000, y: exhaust.value.used}, true, serie2.data.length >= 200);
    }

    setInterval(function() {
        $scope.refresh();
    }, 2000);

});
