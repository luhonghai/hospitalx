/**
 * Created by Hai Lu on 22/04/14.
 */
Ext.define('HPX.model.Patient', {
    extend: 'Ext.data.Model',
    requires: [
        'HPX.config.Runtime'
    ],
    config: {
        fields: [
            {name: 'id' , type: 'string'},
            {name: 'firstName' , type: 'string'},
            {name: 'lastName' , type: 'string'},
            {name: 'age' , type: 'int'},
            {name: 'gender' , type: 'boolean'},
            {name: 'description' , type: 'string'},
            {name: 'address' , type: 'string'},
            {name: 'createdDate' , type: 'date'},
        ],
		hasMany: 'HPX.model.Treatment',
		proxy: {
            type: 'rest',
            url: 'http://localhost:8080/rest/api/core/com.luhonghai.hpx.jdo.Patient',
            reader: {
                type: 'json'
            }
        }
    }
});