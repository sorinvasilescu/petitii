toastCode =
    '<!-- alert -->' +
    '<div id="alert" class="alert alert-dismissible hidden" role="alert">' +
    '<button type="button" class="close" data-dismiss="alert" aria-label="Close">' +
    '<span aria-hidden="true">&times;</span>' +
    '</button>' +
    '<span id="alert-content"></span>' +
    '</div>' +
    '<!-- end alert -->';

function showToast(container, message, type) {
    $(container).prepend(toastCode);
    $('#alert')
        .removeClass('hidden','alert-success','alert-danger','alert-warning','alert-info')
        .addClass('alert-' + type)
        .children('#alert-content').text(message);
}

function addWarning(container, msg) {
    showToast(container, msg, "warning");
}

function addError(container, msg) {
    showToast(container, msg, "danger");
}

function addInfo(container, msg) {
    showToast(container, msg, "info");
}

function addSuccess(container, msg) {
    showToast(container, msg, "success");
}