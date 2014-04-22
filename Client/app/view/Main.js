Ext.define('HPX.view.Main', {
    extend: 'Ext.Container',
    xtype: 'main',
    requires: [
        'Ext.TitleBar',
        'HPX.view.patient.List'
    ],
    config: {
        items: [{
            xtype: 'titlebar',
            title: 'Native Side Menu',
            items: [{
                xtype: 'button',
                iconCls: 'list',
                action: 'toggle-menu'
            }]
        }, {
            xtype: 'patients',
            store: 'Patients',
            grouped: true,
            pinHeaders: false
        }
        ]
    }
});
