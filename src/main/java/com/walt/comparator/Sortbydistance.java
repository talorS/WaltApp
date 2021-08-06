package com.walt.comparator;

import com.walt.model.DriverDistance;
import java.util.Comparator;

public class Sortbydistance implements Comparator<DriverDistance> {
    public int compare(DriverDistance d1, DriverDistance d2)
    {
        return d2.getTotalDistance().compareTo(d1.getTotalDistance());
    }
}
