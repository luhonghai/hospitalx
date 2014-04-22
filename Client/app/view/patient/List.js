/**
 * Created by luhonghai on 4/22/14.
 */
Ext.define('HPX.view.patient.List', {
    extend: 'Ext.List',
    'xtype': 'patients',
    requires: [
        'Ext.plugin.PullRefresh',
        'Ext.plugin.ListPaging',
        'Ext.field.Search'
    ],
    config: {
        title: 'Danh sách bệnh nhân',
        ui: 'round',
        emptyText: '<div style="margin-top: 20px; text-align: center">Không tìm thấy</div>',
        itemCls: 'patient',
        variableHeights: true,

        grouped: true,
        pinHeaders: false,

        itemTpl: [
            '<img class="avatar" src="resources/images/avatar/unknown-male.jpg">',
            '<h3>{lastName} {firstName}</h3>',
            '<h4>{address}</h4>'
        ],
        plugins: [
            { type: 'listpaging' },
            { type: 'pullrefresh' }
        ],

        items: [
            {
                xtype: 'toolbar',
                docked: 'top',

                items: [
                    {
                        xtype: 'button',
                        iconCls: 'list',
                        action: 'toggle-menu'
                    },
                    { xtype: 'spacer' },
                    {
                        xtype: 'searchfield',
                        placeHolder: 'Tìm kiếm ...'
                    },
                    { xtype: 'spacer' },
                    {
                        xtype: 'button',
                        iconCls: 'add',
                        action: 'add-new'
                    }
                ]
            }
        ]
    }
});