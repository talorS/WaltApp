package com.walt.model;

public class DriverDistanceImpl implements DriverDistance{
    private Driver m_driver;
    private Long m_totalDistance;

    public DriverDistanceImpl(final Driver driver,final Long total) {
        m_driver = driver;
        m_totalDistance = total;
    }

    public Driver getDriver() {
        return m_driver;
    }

    public Long getTotalDistance() {
        return m_totalDistance;
    }

    public void setTotalDistance(final Long total) {
        m_totalDistance = total;
    }
}
