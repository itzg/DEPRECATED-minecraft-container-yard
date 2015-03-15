
angular.module('mccyControllers', [
    'mccyServices'
])
    .controller('OuterController', function ($scope) {

    })

    .controller('NavController', function($scope, $location){
        $scope.isCollapsed = true;
        $scope.$on('$routeChangeSuccess', function () {
            $scope.isCollapsed = true;
        });

        $scope.getClass = function (path) {
            if ($location.path().substr(0, path.length) === path) {
                return "active";
            } else {
                return "";
            }
        };
    })

    .controller('ServersController', function ($scope, Servers) {
        $scope.servers = [];

        $scope.$on('hosts.updated', function(evt, selection) {
            console.log("hosts update", selection);
            $scope.servers = Servers.getAll({host:selection});
        })
    })

    .controller('HostsController', function($scope, $rootScope, Hosts){
        function buildHostsSelection(data) {
            var result = [];
            angular.forEach(data, function(entry){
                if (entry.selected) {
                    result.push(entry.dockerDaemonId);
                }                
            });
            return result;
        }

        $scope.hosts = Hosts.getAll(function(data){
            angular.forEach(data, function(entry){
                entry.selected = true;
            });
            $rootScope.$broadcast('hosts.updated', buildHostsSelection(data));
        });
    })

    .controller('ModsController', function ($scope) {
    })
;