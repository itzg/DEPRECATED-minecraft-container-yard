
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
            var modalObj = $modal.open({
                templateUrl: '/views/create-server.html',
                controller: 'CreateServerController'
            });

            modalObj.result.then(function() {
                refresh();
            });
        };

        $scope.getLabelClassForStatus = function(status) {
            switch (status) {
                case 'RUNNING':
                    return 'label-success';
                case 'EXITED':
                case 'CREATED':
                    return 'label-danger';
                default:
                    return 'label-warning';
            }
        };

        $scope.renderServerAddress = function(server) {
            if (server.exposedPort) {
                return server.hostAddress + ":" + server.exposedPort;
            }
            else {
                return "";
            }
        }

        var hosts;

        $scope.refresh = function() {
            refresh();
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

        function refresh() {
            $rootScope.$broadcast('refresh', 'servers');
        }
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

    .controller('CreateServerController', function($scope, $modalInstance, Versions, Hosts, Servers){

        $scope.versionMode = 'LATEST';
        $scope.spec = {
            hostId: '',
            name: '',
            port: 25565,
            version: 'LATEST',
            type: 'VANILLA',
            ops: '',
            motd: '',
            useHostDataMount: false,
            uid: null,
            serverIcon: null,
            levelSeed: null
        };

        $scope.hosts = Hosts.getAll(function(response) {
            if (angular.isArray(response)) {
                if (response.length == 1) {
                    $scope.spec.hostId = response[0].dockerDaemonId;
                }
            }
        });

        $scope.versions = Versions.getAll({
            type: 'vanilla',
            release: 'STABLE'
        }, function(data){
            console.log("Got", data);
        });

        $scope.goCreateServer = function() {
            Servers.create($scope.spec, function() {
                $modalInstance.close(true);
            });
        };

        $scope.$watch('versionMode', function(newValue) {
            if (newValue != 'SPECIFIC') {
                $scope.spec.version = newValue;
            }
        });

    })

    .controller('ModsViewController', function ($scope) {
    })
;