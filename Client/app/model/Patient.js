/**
 * Created by Hai Lu on 22/04/14.
 */
Ext.define('HPX.model.Patient', {
    extend: 'Ext.data.Model',

    config: {

        fields: [
            {name: 'id' , type: 'string'},
            {name: 'firstName' , type: 'string'},
            {name: 'lastName' , type: 'string'},
            {name: 'birthDay' , type: 'date'},
            {name: 'gender' , type: 'boolean'},
            {name: 'description' , type: 'string'},
            {name: 'address' , type: 'string'},
            {name: 'createdDate' , type: 'date'}
        ],
		hasMany: 'HPX.model.Treatment',
        proxy: {
            type: 'rest',
            url: HPX.config.RestUtil.generateRestUrl('Patient')
        }
    }
});