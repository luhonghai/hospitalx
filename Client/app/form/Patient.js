/**
 * Created by luhonghai on 4/24/14.
 */
Ext.define('HPX.form.Patient', {
    extend: 'Ext.form.Panel',
    xtype:'form-patient',
    requires: [
        'Ext.form.*',
        'Ext.field.*',
        'Ext.Button',
        'Ext.Toolbar'
    ],
    config: {
        title: 'Thêm mới bệnh nhân',
        items: [
            {
                xtype: 'fieldset',
                title: 'Thông tin cơ bản',
                instructions: 'Vui lòng nhập những thông tin ở trên',
                defaults: {
                    required: true
                },
                items: [
                    {
                        xtype: 'textfield',
                        name: 'firstName',
                        label: 'Tên',
                        autoCapitalize: true
                    },
                    {
                        xtype: 'textfield',
                        name: 'lastName',
                        label: 'Họ',
                        autoCapitalize: true
                    },
                    {
                        xtype: 'datepickerfield',
                        name: 'birthDay',
                        label: 'Năm sinh',
                        value: new Date('01-09-1994'),
                        picker: {
                            yearFrom: 1920
                        }
                    }
                ]
            },
            {
                xtype: 'fieldset',
                title: 'Giới tính',
                defaults: {xtype : 'radiofield'},
                items: [
                    {name :'gender', label: 'Nam', checked: true, value: 'true'},
                    {name: 'gender', label: 'Nữ', value: 'false'}
                ]
            },
            {
                xtype: 'fieldset',
                title: 'Thông tin khác',
                items: [
                    {
                        xtype: 'textareafield',
                        name: 'address',
                        label: 'Địa chỉ',
                        maxRows: 5
                    },
                    {
                        xtype: 'textareafield',
                        name: 'description',
                        label: 'Mô tả',
                        maxRows: 5
                    },
                    {
                        xtype: 'datepickerfield',
                        name: 'createdDate',
                        label: 'Ngày tạo',
                        value: new Date(),
                        picker: {
                            yearFrom: 2010
                        }
                    }
                ]
            },
            {
                xtype: 'toolbar',
                docked: 'bottom',
                scrollable: {
                    direction: 'horizontal',
                    directionLock: true
                },
                items: [
                    { xtype: 'spacer' },
                    { xtype: 'spacer' },
                    {
                        action: 'reset',
                        text: 'Làm mới'
                    },
                    {
                        ui: 'confirm',
                        action: 'save',
                        text: 'Lưu'
                    }
                ]
            }
        ]
    }
});