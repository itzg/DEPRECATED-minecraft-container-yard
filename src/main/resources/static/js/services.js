angular.module('mccyServices', [
    'ngResource'
])
    .service('mccyServersResource', function($resource) {
        return $resource('/servers/:name', {}, {
            getAll: {
                method: 'GET',
                params: {}
            }
        })
    })
    .service('mccyServers', function(mccyServersResource) {
        // The service interface
        return {
            getAll: function() {

            }
        }
    })
;