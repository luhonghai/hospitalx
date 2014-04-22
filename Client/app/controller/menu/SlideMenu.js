/**
 * Created by luhonghai on 4/21/14.
 */
Ext.define('HPX.controller.menu.SlideMenu', {
    extend: 'Ext.app.Controller',

    config: {
        refs: {
            btnToggleMenu : 'button[action=toggle-menu]',
            listMenu: 'slidemenu list'
        },
        control: {
            'btnToggleMenu' : {
                tap: 'toggleMenu',
                click: 'toggleMenu'
            },
            'listMenu': {
                select: 'onSelectListMenu'
            }
        }
    },

    toggleMenu: function(element, e) {
        Ext.Viewport.toggleMenu('left');
    },
    onSelectListMenu: function(view, record) {
        Ext.Msg.alert('Selected', 'you selected menu ' + record.get('title'));
    }
});