package com.usian.utils;

/**
 * @author Loser
 * @date 2021年11月22日 20:09
 */
import java.io.Serializable;
import java.util.List;

public class CatResult implements Serializable {
    private List<?> data;

    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }
}
