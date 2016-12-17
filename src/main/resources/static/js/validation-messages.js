function addWarning(container, msg) {
    $(container).append('<div class="alert alert-warning alert-dismissible">' +
        '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>' +
        msg + '</div>');
}

function addError(container, msg) {
    $(container).append('<div class="alert alert-error alert-dismissible">' +
        '<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>' +
        msg + '</div>');
}