
angular.module('mccyControllers', [
    'mccyServices'
])
    .controller('OuterCtrl', function ($scope) {

    })

    .controller('ServersCtrl', function ($scope, mccyServersResource) {
        $scope.servers = mccyServersResource.getAll();
    })
;