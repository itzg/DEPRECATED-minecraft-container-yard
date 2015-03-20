angular.module('mccyApp', [
    'ngRoute',
    'ui.bootstrap',
    'angularMoment',
    'mccyControllers',
    'mccyServices'
])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/servers', {
                templateUrl: 'views/servers.html',
                controller: 'ServersViewController'
            })
            .when('/mods', {
                templateUrl: 'views/mods.html',
                controller: 'ModsViewController'
            })
            .when('/', {
                templateUrl: 'views/start.html'
            })
            .otherwise({
                redirectTo: '/'
            });
    });