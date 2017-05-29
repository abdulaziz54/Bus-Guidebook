package com.scenicbustour.Models;

import io.realm.RealmObject;

/**
 * Created by amryousef on 28/05/2017.
 */

public class RealmFloat extends RealmObject {

    private float value;

    public float getValue() {
        return value;
    }

    public RealmFloat setValue(float value) {
        this.value = value;
        return this;
    }
}
