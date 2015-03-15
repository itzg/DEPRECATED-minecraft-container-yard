angular.module('mccyApp', [
    'ngRoute',
    'mccyControllers',
    'mccyServices'
])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/servers', {
                templateUrl: 'views/servers.html',
                controller: 'ServersController'
            })
            .when('/mods', {
                templateUrl: 'views/mods.html',
                controller: 'ModsController'
            })
            .when('/', {
                templateUrl: 'views/start.html'
            })
            .otherwise({
                redirectTo: '/'
            });
    });