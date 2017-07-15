package com.scenicbustour.Models;

import io.realm.RealmObject;

/**
 * Created by amr_f on 12/07/2017.
 */

public class RealmString extends RealmObject {
    String value;

    public RealmString setValue(String value) {
        this.value = value;
        return this;
    }

    public String getValue() {
        return value;
    }
}
