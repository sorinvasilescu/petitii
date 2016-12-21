function customAlert(message, callback) {
    bootbox.confirm({
        message: '<h2>' + message + '</h2>',
        backdrop: true,
        buttons: {
            confirm: {
                label: '<i class="fa fa-check"></i> Da'
            },
            cancel: {
                label: '<i class="fa fa-times"></i> Nu'
            }
        },
        callback: callback
    });
}

function customInput(title, callback) {
    bootbox.prompt({
        title: title,
        backdrop: true,
        inputType: 'textarea',
        callback: function (result) {
            if (result != null && result.trim().length > 0) {
                callback(result.trim())
            }
        }
    });
}

function customUpload(title, csrfLabel, csrfValue, pid, callback) {
    bootbox.dialog({
        title: title,
        backdrop: true,
        message: '<iframe id="uploader" style="border:none;" src="/rest/attachments/iframe" postLocation="/rest/petitions/' + pid + '/attachments/add"></iframe>',
        buttons: {
            upload: {
                label: 'Incarca',
                className: 'btn-success',
                callback: function () {
                    callback($('#uploader').contents().find('#upload-form'));
                }
            },
            cancel: {
                label: 'Anuleaza'
            }
        }
    });
}