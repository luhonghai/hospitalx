/**
 * Created by Hai Lu on 22/04/14.
 */
Ext.define('HPX.model.Treatment', {
    extend: 'Ext.data.Model',
    requires: [
        'HPX.config.Runtime'
    ],
    config: {
		fields: [
            {name: 'id' , type: 'string'},
			{name: 'patient_id' , type: 'string'},
            {name: 'type' , type: 'string'},
            {name: 'createdDate' , type: 'date'},
        ],
		belongsTo: 'HPX.model.Patient',
        proxy: {
            type: 'rest',
            url: 'http://localhost:8080/rest/api/core/com.luhonghai.hpx.jdo.Treatment',
            reader: {
                type: 'json'
            }
        }
    }
});