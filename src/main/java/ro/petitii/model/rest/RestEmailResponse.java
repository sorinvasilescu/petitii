package ro.petitii.model.rest;

import java.util.List;

public class RestEmailResponse {
    int draw;
    long recordsTotal;
    long recordsFiltered;
    List<RestEmailResponseElement> data;

    public RestEmailResponse() {}

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }

    public long getRecordsTotal() {
        return recordsTotal;
    }

    public void setRecordsTotal(long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }

    public long getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public List<RestEmailResponseElement> getData() {
        return data;
    }

    public void setData(List<RestEmailResponseElement> data) {
        this.data = data;
    }
}