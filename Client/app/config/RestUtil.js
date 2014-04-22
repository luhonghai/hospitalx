/**
 * Created by luhonghai on 4/22/14.
 */

Ext.define('HPX.config.RestUtil', {
    singleton: true,

    constructor: function(config) {
        this.initConfig(config);
    },

    generateRestUrl: function(jdoClass, persistentUnit, classPackage) {
        var runtime = HPX.config.Runtime;
        if (!persistentUnit) {
            persistentUnit = runtime.getBasePersistentUnit();
        }
        if (!classPackage) {
            classPackage = runtime.getBaseJdoPackage();
        }
        return runtime.getBaseUrl()
            + '/rest/api/'
            + persistentUnit
            + '/'
            + classPackage
            + '.'
            + jdoClass;
    }
});

