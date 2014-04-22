/**
 * Created by luhonghai on 4/23/14.
 */

Ext.define('HPX.controller.Patients', {
    extend: 'Ext.app.Controller',

    config: {
        refs: {
            searchfield : 'patients searchfield',
            btnAddNew : 'patients button[action=add-new]'
        },
        control: {
            'searchfield' : {
                clearicontap: 'onSearchClearIconTap',
                keyup: 'onSearchKeyUp'
            },
            'btnAddNew' : {
                tap: 'onButtonAddNewTap',
                click: 'onButtonAddNewTap'
            }
        }
    },

    onButtonAddNewTap: function(ele, e) {
        Ext.Msg.alert('Add new', 'you selected menu add new');
    },

    onSearchClearIconTap: function() {
        var store = Ext.getStore('Patients');
        store.clearFilter();
    },

    onSearchKeyUp: function(field) {
        var value = field.getValue(),
            store = Ext.getStore('Patients');

        //first clear any current filters on the store. If there is a new value, then suppress the refresh event
        store.clearFilter(!!value);

        //check if a value is set first, as if it isnt we dont have to do anything
        if (value) {
            //the user could have entered spaces, so we must split them so we can loop through them all
            var searches = value.split(','),
                regexps = [],
                i, regex;

            //loop them all
            for (i = 0; i < searches.length; i++) {
                //if it is nothing, continue
                if (!searches[i]) continue;

                regex = searches[i].trim();
                regex = regex.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");

                //if found, create a new regular expression which is case insenstive
                regexps.push(new RegExp(regex.trim(), 'i'));
            }

            //now filter the store by passing a method
            //the passed method will be called for each record in the store
            store.filter(function(record) {
                var matched = [];

                //loop through each of the regular expressions
                for (i = 0; i < regexps.length; i++) {
                    var search = regexps[i],
                        didMatch = search.test(record.get('firstName') + ' ' + record.get('lastName'));

                    //if it matched the first or last name, push it into the matches array
                    matched.push(didMatch);
                }

                return (regexps.length && matched.indexOf(true) !== -1);
            });
        }
    }

});