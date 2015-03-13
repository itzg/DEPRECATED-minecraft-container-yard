angular.module('mccyApp', [
    'ngRoute',
    'mccyControllers',
    'mccyServices'
])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/servers', {
                templateUrl: 'views/servers.html',
                controller: 'ServersCtrl'
            })
            .otherwise({
                redirectTo: '/servers'
            });
    });