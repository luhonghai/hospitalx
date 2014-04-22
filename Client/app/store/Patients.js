/**
 * Created by luhonghai on 4/22/14.
 */
Ext.define('HPX.store.Patients', {
    extend: 'Ext.data.Store',

    config: {
        model: 'HPX.model.Patient',

        grouper: {
            groupFn: function(record) {
                return record.get('firstName').substr(0,1);
            },
            sortProperty: 'firstName'
        },
        autoLoad: true,
        proxy: {
            type: 'rest',
            url: HPX.config.RestUtil.generateRestUrl('Patient')
        }
    }
});