angular.module('mccyApp', [
    'ngRoute',
    'mccyControllers'
])
    .config(function ($routeProvider) {
        $routeProvider
            .when('/start', {
                templateUrl: 'views/start.html',
                controller: 'StartCtrl'
            })
            .otherwise({
                redirectTo: '/start'
            });
    });