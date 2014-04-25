/**
 * Created by luhonghai on 4/22/14.
 */
Ext.define('HPX.store.Patients', {
    extend: 'Ext.data.Store',

    config: {
        model: 'HPX.model.Patient',
        pageSize:9999,
        grouper: {
            groupFn: function(record) {
                return record.get('firstName').substr(0,1).toUpperCase();
            }
        },
        autoLoad: true,
        proxy: {
            type: 'rest',
            url: HPX.config.RestUtil.generateRestUrl('Patient')
        }
    }
});