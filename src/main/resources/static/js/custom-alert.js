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

function customUpload(title, callback) {
    bootbox.dialog({
        title: title,
        backdrop: true,
        message:
            '<form id="upload-form">' +
                '<span class="btn btn-default btn-file">' +
                    'Selectează<input id="files" type="file" multiple="multiple" onchange="onInputChange()"/>' +
                '</span>' +
                '<span id="selection"></span>' +
            '</form>',
        buttons: {
            upload: {
                label: 'Încarcă',
                className: 'btn-success',
                callback: function() {
                    callback($('#files')[0].files);
                }
            },
            cancel: {
                label: 'Anulează',
                className: 'btn-default'
            }
        }
    });
}

function onInputChange() {
    var span = $('#selection');
    span.text('');
    $.each($('#files')[0].files, function (i,file) {
        span.text(span.text() + (i!=0?', ':'') + file.name);
    });
}