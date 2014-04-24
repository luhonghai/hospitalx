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
        store: 'Patients',
        ui: 'round',
        emptyText: '<div style="margin-top: 20px; text-align: center">Không tìm thấy bệnh nhân</div>',
        itemCls: 'patient',
        variableHeights: true,

        grouped: true,

        scrollToTopOnRefresh: false,

        plugins: [
            //{ xclass: 'Ext.plugin.ListPaging' },
            { xclass: 'Ext.plugin.PullRefresh' }
        ],

        itemTpl:   new Ext.XTemplate(
            [
                '<img class="avatar" src="{[this.getAvatar(values)]}">',
                '<h3>{lastName} {firstName}</h3>',
                '<h4>Địa chỉ: {address}</h4>'
            ].join('')
            ,
            {
                getAvatar : function(values) {
                    return 'resources/images/avatar/unknown-' + (values.gender ? 'male' : 'female') +'.jpg';
                }
            }
        )
         ,


        items: [
            {
                xtype: 'toolbar',
                docked: 'bottom',

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