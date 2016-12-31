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
    var dialog = bootbox.dialog({
        title: title,
        size: 'large',
        message: '<textarea class="resizable_textarea form-control" name="description" id="message" rows="30" style="width:100%"></textarea>',
        buttons: {
            add: {
                label: 'Adaugă',
                className: 'btn-success',
                callback: function () {
                    var result = $('#message').val();
                    if (result != null && result.trim().length > 0) {
                        callback(result.trim())
                    }
                }
            },
            cancel: {
                label: 'Anulează',
                className: 'btn-default'
            }
        }
    });

    dialog.init(function () {
        $("#message").wysihtml5();
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

function linkPopup(title, petitionsApiUrl, languageUrl, callback) {
    var dialog = bootbox.dialog({
        title: title,
        size: 'large',
        backdrop: true,
        message: '<div class="container"><table id="linked-petitions-popup"' +
        ' class="table table-striped row-border compact stripe dt-responsive nowrap"' +
        ' cellspacing="0" width="100%">' +
        '</table></div>',
        buttons: {
            upload: {
                label: 'Selectează',
                className: 'btn-success',
                callback: function () {
                    var selected = $('#linked-petitions-popup').DataTable().rows({selected: true}).data().toArray();
                    if (selected.length > 0) {
                        callback(selected);
                    }
                }
            },
            cancel: {
                label: 'Anulează',
                className: 'btn-default'
            }
        }
    });

    dialog.init(function () {
        $('#linked-petitions-popup').DataTable({
            select: {
                style: 'multi'
            },
            processing: true,
            serverSide: true,
            sDom: 'lrtip ',
            rowId: "id",
            order: [[3, 'desc']],
            ajax: {
                url: petitionsApiUrl,
                type: 'POST'
            },
            language: {
                url: languageUrl,
                select: selectTranslation()
            },
            fixedColumns: true,
            columns: [
                {
                    name: 'id',
                    data: 'id',
                    title: '#'
                },
                {
                    name: 'regNo',
                    data: 'regNo',
                    title: 'Nr. înregistrare'
                },
                {
                    name: '_abstract',
                    data: '_abstract',
                    title: 'Titlu'
                },
                {
                    name: 'receivedDate',
                    data: 'receivedDate',
                    title: 'Data'
                },
                {
                    name: 'user',
                    data: 'user',
                    title: 'Responsabil'
                }
            ]
        });
    })
}

function attachmentPopup(title, attachmentsApiUrl, languageUrl, callback) {
    var dialog = bootbox.dialog({
        title: title,
        size: 'large',
        backdrop: true,
        message: '<div class="container"><table id="attachments-popup"' +
        ' class="table table-striped row-border compact stripe dt-responsive nowrap"' +
        ' cellspacing="0" width="100%">' +
        '</table></div>',
        buttons: {
            upload: {
                label: 'Selectează',
                className: 'btn-success',
                callback: function () {
                    var selected = $('#attachments-popup').DataTable().rows({selected: true}).data().toArray();
                    if (selected.length > 0) {
                        callback(selected);
                    }
                }
            },
            cancel: {
                label: 'Anulează',
                className: 'btn-default'
            }
        }
    });

    dialog.init(function () {
        $('#attachments-popup').DataTable({
            select: {
                style: 'multi'
            },
            processing: true,
            serverSide: true,
            sDom: 'lrtip ',
            rowId: "id",
            order: [[0, 'asc']],
            ajax: {
                url: attachmentsApiUrl,
                type: 'POST'
            },
            language: {
                url: languageUrl,
                select: selectTranslation()
            },
            fixedColumns: true,
            columns: [
                {
                    name: 'id',
                    title: "#",
                    data: 'id',
                    sortable: false
                },
                {
                    name: 'filename',
                    data: 'filename',
                    title: 'Nume fișier'
                },
                {
                    name: 'date',
                    data: 'date',
                    title: 'Data'
                },
                {
                    name: 'origin',
                    data: 'origin',
                    title: 'Adăugat de',
                    sortable: false
                },
                {
                    data: null,
                    title: '',
                    render: function (data, type, full, meta) {
                        if (data != null) {
                            return '<a href="/api/attachments/download/' + data.id + '"><i class="fa fa-paperclip"></i> Vizualizează</a>';
                        } else {
                            return '';
                        }
                    },
                    sortable: false
                }
            ]
        });
    })
}