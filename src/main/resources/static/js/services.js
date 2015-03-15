angular.module('mccyServices', [
    'ngResource'
])
    .service('Servers', function($resource) {
        return $resource('/servers/:id', {
        }, {
            getAll: {
                method: 'GET',
                isArray: true,
                params: {
                    details: true
                },
                url: '/servers'
            }
        })
    })

    .service('Hosts', function($resource){
        return $resource('/hosts/:idOrName', {}, {
            getAll: {
                method: 'GET',
                params: {},
                isArray: true
            }
        })
    })

;