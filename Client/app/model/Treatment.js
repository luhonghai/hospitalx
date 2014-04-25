/**
 * Created by Hai Lu on 22/04/14.
 */
Ext.define('HPX.model.Treatment', {
    extend: 'Ext.data.Model',

    config: {
		fields: [
            {name: 'id' , type: 'string'},
			{name: 'patient_id' , type: 'string'},
            {name: 'type' , type: 'string'},
            {name: 'createdDate' , type: 'date'}
        ],
		belongsTo: 'HPX.model.Patient',
        proxy: {
            type: 'rest',
            url: HPX.config.RestUtil.generateRestUrl('Treatment')
        }
    }
});