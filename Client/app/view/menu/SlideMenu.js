/**
 * Created by luhonghai on 4/21/14.
 */
Ext.define('HPX.view.menu.SlideMenu', {
    extend: 'Ext.Menu',
    xtype: 'slidemenu',
    requires: [
        'Ext.TitleBar',
        'Ext.dataview.List'
    ],
    config: {
        layout: 'fit',
        width: 220,
        items: [{
            xtype: 'titlebar',
            title: 'Side menu'
        }, {
            xtype: 'list',
            itemTpl: '{title}',
            data: [{
                title: 'Menu item 1'
            }, {
                title: 'Menu item 2'
            },

                {
                    title: 'Menu item 3'
                }, {
                    title: 'Menu item 4'
                }]
        }]
    }
});
