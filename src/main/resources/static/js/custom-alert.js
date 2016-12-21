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
        message: '<iframe id="uploader" style="border: none;"></iframe>',
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

    $('#uploader').contents().find('html').html(
        '<form id="upload-form" enctype="multipart/form-data" action="/rest/petitions/' + pid + '/attachments/add" method="POST">' +
        '<span class="btn btn-default btn-file">' +
        'Browse' +
        '<input id="files" name="files" type="file" multiple="multiple" onchange="onInputChange(this)"/>' +
        '<input type="hidden" name="' + csrfLabel + '" value="' + csrfValue +'"/>' +
        //'<input type="submit" name="submit" value="Incarca"/>' +
        '</span>' +
        '<span id="selected-files"></span>' +
        '</form>'
    );
}

function onInputChange(object) {
    var selected = $('#selected-files');
    selected.text('');
    for (var i=0; i<object.files.length; i++) selected.text(selected.text() + (i!=0? ',':'') + object.files[i].name);
}