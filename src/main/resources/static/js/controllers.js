
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

    .controller('ServersViewController', function ($scope, Servers) {
    })

    .controller('ServersController', function ($scope, $rootScope, $modal, Servers) {
        $scope.servers = [];
        $scope.managing = false;

        $scope.createServer = function() {
            $modal.open({
                templateUrl: '/views/create-server.html',
                controller: 'CreateServerController'
            });
        };

        $scope.getLabelClassForStatus = function(status) {
            switch (status) {
                case 'RUNNING':
                    return 'label-success';
                case 'EXITED':
                    return 'label-danger';
                default:
                    return 'label-warning';
            }
        };

        var hosts;

        $scope.refresh = function() {
            $rootScope.$broadcast('refresh', 'servers');
        };

        $scope.$on('refresh', function(evt, target) {
            if (target == 'servers') {
                $scope.servers = Servers.getAll({host:hosts});
            }
        });

        $scope.$on('hosts.updated', function(evt, selection) {
            console.log("hosts update", selection);
            hosts = selection;
            $scope.$broadcast('refresh', 'servers');
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

        $scope.handleHostSelectionChange = function() {
            $rootScope.$broadcast('hosts.updated', buildHostsSelection($scope.hosts));
        }
    })

    .controller('CreateServerController', function($scope, $modalInstance, Versions, Hosts){
        $scope.$watch('type', getVersions);

        $scope.hostId = '';
        $scope.hosts = Hosts.getAll(function(response) {
            if (angular.isArray(response)) {
                if (response.length == 1) {
                    $scope.hostId = response[0].dockerDaemonId;
                }
            }
        });
        $scope.name = '';
        $scope.port = 25565;
        $scope.type = 'VANILLA';
        $scope.version = '';
        $scope.motd = null;
        $scope.serverIcon = null;

        $scope.levelSeed = null;

        function getVersions(type) {
            $scope.versions = Versions.getAll({
                type: 'vanilla',
                release: type.indexOf('_SNAPSHOTS') >= 0 ? 'SNAPSHOT' : 'STABLE'
            });
        }
    })

    .controller('ModsViewController', function ($scope) {
    })
;