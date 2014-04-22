/**
 * Created by Hai Lu on 22/04/14.
 */
Ext.define('HPX.config.Runtime', {
    singleton: true,
    config: {
        baseUrl: 'http://localhost:8080'
    },
    constructor: function(config) {
        this.initConfig(config);
    }
});