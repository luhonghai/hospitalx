Ext.define('HPX.view.Main', {
    extend: 'Ext.navigation.View',
    xtype: 'main',
    requires: [
        'Ext.TitleBar',
        'HPX.view.patient.List'
    ],
    config: {
        items: [ {
            xtype: 'patients',
            store: 'Patients'
        }
        ],
        navigationBar: {
            items: [

            ]
        }
    }
});
