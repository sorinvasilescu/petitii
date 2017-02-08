function strLimit(data, limit) {
    if ((data!=null) && (data.length > limit)){
        data = data.substr(0, limit);
        data += '...';
    }
    return data;
}

function markAsSpam(clickEvent, msgId, table) {
    markAs(clickEvent, msgId, 'spam', 'Sunteți sigur că doriți să marcați ca Spam?', table);
}

function markAsEmail(clickEvent, msgId, table) {
    markAs(clickEvent, msgId, 'email', 'Sunteți sigur că doriți să mutați înapoi în Email?', table);
}

function markAs(clickEvent, msgId, actionType, message, table) {
    customAlert(message, function (result) {
        if (result) {
            /*<![CDATA[*/
            var actionUrl = "/api/markAs/" + actionType + "/" + msgId;
            /*]]>*/

            console.log("mark as: " + actionUrl);

            $(clickEvent.target).attr('data-loading-text', '<i class="fa fa-circle-o-notch fa-spin"></i> Se incarca');
            $(clickEvent.target).button('loading');
            $.ajax({
                url: actionUrl,
                method: 'POST'
            }).done(function (data) {
                if (data.success) {
                    addSuccess('#error-messages', data.message);
                } else {
                    addError('#error-messages', data.message);
                }
                if (table == null) {
                    location.reload();
                } else {
                    table.ajax.reload();
                }
                $(clickEvent.target).button('reset');
            }).fail(function (e) {
                $(clickEvent.target).button('reset');
            });
        }
    });
}