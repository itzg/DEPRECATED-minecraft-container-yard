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
            },

            create: {
                method: 'POST',
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

    .service('Versions', function($resource){
        return $resource('/versions/:type', {
            release: 'STABLE'
        }, {
            getAll: {
                method: 'GET',
                params: {},
                isArray: true
            }
        })
    })

/**
 * From http://stackoverflow.com/a/24570344/121324
 */
    .directive('showValidation', [function() {
        return {
            restrict: "A",
            require:'form',
            link: function(scope, element, attrs, formCtrl) {
                element.find('.form-group').each(function() {
                    var $formGroup=$(this);
                    var $inputs = $formGroup.find('input[ng-model],textarea[ng-model],select[ng-model]');

                    if ($inputs.length > 0) {
                        $inputs.each(function() {
                            var $input=$(this);
                            scope.$watch(function() {
                                return $input.hasClass('ng-invalid');
                            }, function(isInvalid) {
                                $formGroup.toggleClass('has-error', isInvalid);
                            });
                        });
                    }
                });
            }
        };
    }])
;