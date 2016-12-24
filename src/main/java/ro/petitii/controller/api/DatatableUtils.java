package ro.petitii.controller.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;

import java.util.Map;

class DatatableUtils {
    private static Sort.Direction parseSortDirection(DataTablesInput input) {

        Sort.Direction sortDirection = null;
        if (input.getOrder().get(0).getDir().equals("asc")) {
            sortDirection = Sort.Direction.ASC;
        } else if (input.getOrder().get(0).getDir().equals("desc")) {
            sortDirection = Sort.Direction.DESC;
        }
        return sortDirection;
    }

    static PageRequest pageRequest(DataTablesInput input) {
        return pageRequest(input, null);
    }

    static PageRequest pageRequest(DataTablesInput input, Map<String, String> sortColumnRemapping) {
        String sortColumn = input.getColumns().get(input.getOrder().get(0).getColumn()).getName();
        sortColumn = remapSortColumn(sortColumn, sortColumnRemapping);
        Sort.Direction sortDirection = parseSortDirection(input);
        Integer start = input.getStart();
        Integer length = input.getLength();
        return new PageRequest(start / length, length, sortDirection, sortColumn);
    }

    private static String remapSortColumn(String sortColumn, Map<String, String> sortColumnRemapping) {
        if (sortColumn == null || sortColumnRemapping == null || !sortColumnRemapping.containsKey(sortColumn)) {
            return sortColumn;
        } else {
            return sortColumnRemapping.get(sortColumn);
        }
    }
}
