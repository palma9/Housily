package com.salesianos.housify.data.response;

import java.util.List;

public class ResponseContainer<T> {

    private List<T> rows;
    private long count;

    public List<T> getRows() {
        return rows;
    }

    public long getCount() {
        return count;
    }
}
