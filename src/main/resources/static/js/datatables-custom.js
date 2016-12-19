function selectTranslation() {
    return {
        rows: {
            _: "%d rânduri selectate",
            0: "",
            1: "1 rând selectat"
        }
    };
}

function selectActions(table) {
    table.on('select', function (e, dt, type, indexes) {
        table.rows(indexes).nodes().to$().find('input[type="checkbox"]').prop('checked', true);
    }).on('deselect', function (e, dt, type, indexes) {
        table.rows(indexes).nodes().to$().find('input[type="checkbox"]').prop('checked', false);
    });
}