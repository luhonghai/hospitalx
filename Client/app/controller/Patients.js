/**
 * Created by luhonghai on 4/23/14.
 */

Ext.define('HPX.controller.Patients', {
    extend: 'Ext.app.Controller',
    required: [
        'HPX.form.Patient'
    ],
    config: {
        refs: {
            mainContainer: 'main',
            listPatients: 'patients',
            searchfield : 'patients searchfield',
            btnAddNew : 'main button[action=add-new]',
            formPatient: 'form-patient',
            btnResetForm: 'form-patient button[action=reset]',
            btnSaveForm: 'form-patient button[action=save]'
        },
        control: {
            'searchfield' : {
                clearicontap: 'onSearchClearIconTap',
                keyup: 'onSearchKeyUp'
            },
            'btnAddNew' : {
                tap: 'onButtonAddNewTap',
                click: 'onButtonAddNewTap'
            },
            'btnResetForm' : {
                tap: 'onButtonResetTap',
                click: 'onButtonResetTap'
            },
            'btnSaveForm' : {
                tap: 'onButtonSaveTap',
                click: 'onButtonSaveTap'
            }
        }
    },

    onButtonAddNewTap: function(ele, e) {
        if (this.formPatient) {
            this.formPatient.destroy();
        }
        this.formPatient = Ext.widget('form-patient');
        if (!this.formPatient.patient) {

            this.formPatient.patient = Ext.create('HPX.model.Patient');
        }
        this.getMainContainer().push(this.formPatient);

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
    },

    onButtonResetTap: function(e) {
        this.formPatient.reset();
    },

    onButtonSaveTap: function(e) {
        var form = this.formPatient;
        var main =  this.getMainContainer();
        if (form.patient) {
            form.updateRecord(form.patient,true);
            form.patient.save({
                success: function(p) {

                    store = Ext.getStore('Patients');
                    store.add(p);
                    main.pop();
                }
            });

        }
    }

});