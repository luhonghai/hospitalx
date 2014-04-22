Ext.define('HPX.view.Main', {
    extend: 'Ext.Panel',
    xtype: 'main',
    id: "mainboard",
    requires: [
        'Ext.TitleBar'
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
        }]
    }
});
