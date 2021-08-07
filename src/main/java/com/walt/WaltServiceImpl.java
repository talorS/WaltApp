package com.walt;

import com.walt.comparator.Sortbydistance;
import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class WaltServiceImpl implements WaltService {
    /**
     * min and max distance range for a delivery
     */
    private final int MIN_DISTANCE = 0;
    private final int MAX_DISTANCE = 20;

    @Autowired
    private DriverRepository driverRepo;
    @Autowired
    private DeliveryRepository deliveryRepo;
    @Autowired
    private CustomerRepository customerRepo;

    /**
     * creates a delivery and assign a driver that is free at te given time.
     * @param customer - customer who made the order.
     * @param restaurant - the place the customer ordered from.
     * @param deliveryTime - chosen time for delivery.
     * @return new Delivery for that customer.
     * @throws Exception
     */
    @Override
    public Delivery createOrderAndAssignDriver(final Customer customer,final Restaurant restaurant,final Date deliveryTime)
            throws Exception {

        //Assumption - customer may or may not exist in the system
        if(customer == null){
            throw new Exception("customer doesn't exist in the system!");
        }

        if (!customer.getCity().getId().equals(restaurant.getCity().getId())) {
            throw new Exception("customer's city and restaurant's city doesn't match!");
        }

        final List<Driver> allDrivers = driverRepo.findAllDriversByCity(customer.getCity());
        if (allDrivers.isEmpty()) {
            throw new Exception("Selected city doesn't have drivers!");
        }

        final List<Driver> availableDrivers = new ArrayList<>();
        for (final Driver d : allDrivers) {
            if (isDriverAvailable(d, deliveryTime)) {
                availableDrivers.add(d);
            }
        }

        if (availableDrivers.isEmpty()) {
            throw new Exception("There isn't available driver at this moment.");
        }

        Driver leastBusyDriver = getFreeDriver(availableDrivers);
        Delivery newOrder = new Delivery(leastBusyDriver, restaurant, customer, deliveryTime);
        int rand = getRandomDistance();
        newOrder.setDistance(rand);

        return newOrder;
    }
    //---------------------------------------------------------------------------
    /**
     * @return a random number between MIN_DISTANCE and MAX_DISTANCE (modulo)
     */
    private int getRandomDistance() {
        Random r = new Random(System.currentTimeMillis());
        return r.nextInt(MAX_DISTANCE - MIN_DISTANCE + 1) + MIN_DISTANCE;
    }
    //---------------------------------------------------------------------------
    /**
     * checks if a driver is available at a chosen time.
     * @param dr - current checked driver.
     * @param newDeliveryTime - chosen time for new delivery.
     * @return if the driver is free or not
     */
    private boolean isDriverAvailable(final Driver dr, final Date newDeliveryTime) {
        for (final Delivery currDelivery : deliveryRepo.findAll()) {
            //Assumption - Each drive takes a full hour – it will start and end in a full hour
            currDelivery.getDeliveryTime().setHours(currDelivery.getDeliveryTime().getHours() + 1);
            if (currDelivery.getDriver().getId().equals(dr.getId())
                    && (currDelivery.getDeliveryTime().getTime() > newDeliveryTime.getTime())) {
                return false;
            }
        }
        return true;
    }
//---------------------------------------------------------------------------
    /**
     * @param drivers -  list of available drivers in the customer's city.
     * @return free driver.
     */
    private Driver getFreeDriver(final List<Driver> drivers) {
        final Map<Long, Integer> driversMap = new HashMap<>(); //key - driver's ID, value - the number of deliveries

        for (Driver d : drivers) {
            driversMap.put(d.getId(), 0);
        }

        for (final Delivery d : deliveryRepo.findAll()) {
            final Long key = d.getDriver().getId();
            if (driversMap.containsKey(key)){
                driversMap.put(key, driversMap.get(key) + 1);
            }
        }

        final Long leastBusyDriver = getDeliveriesHistory(driversMap);
        return drivers
                .stream()
                .filter(d -> d.getId().equals(leastBusyDriver))
                .findFirst()
                .orElse(null);
    }
    //---------------------------------------------------------------------------
    /**
     * find the driver with the min number of deliveries.
     * @param map - stores key - driver id, value - num of deliveries.
     * @return free driver id.
     */
    private Long getDeliveriesHistory(final Map<Long, Integer> map) {
        Map.Entry<Long, Integer> min = Collections.min(map.entrySet(), new Comparator<Map.Entry<Long, Integer>>() {
            public int compare(Map.Entry<Long, Integer> e1, Map.Entry<Long, Integer> e2) {
                return e1.getValue().compareTo(e2.getValue());
            }
        });
        return min.getKey();
    }
    //---------------------------------------------------------------------
    /**
     * creates a List of drivers and sum the deliveries distance they did.
     * @return rank report list, sorted descending.
     */
    @Override
    public List<DriverDistance> getDriverRankReport() {
        Map<Long,DriverDistance> driverDistanceMap = new HashMap<>();
        for (final Driver d : driverRepo.findAll()) {
            driverDistanceMap.put(d.getId(), new DriverDistanceImpl(d, Long.valueOf(0)));
        }

        for (final Delivery delivery : deliveryRepo.findAll()) {
            final Long key = delivery.getDriver().getId();
            if (driverDistanceMap.containsKey(key)){
                final Long sum = driverDistanceMap.get(key).getTotalDistance() +
                        Double.valueOf(delivery.getDistance()).longValue();
                driverDistanceMap.get(key).setTotalDistance(sum);
            }
        }

        List<DriverDistance> rankLst = new ArrayList(driverDistanceMap.values());
        Collections.sort(rankLst, new Sortbydistance());
        return rankLst;
    }
    //---------------------------------------------------------------------
    /**
     * creates a List of drivers and sum the deliveries distance they did at a given city.
     * @param city - a chosen city to report.
     * @return rank report list by city, sorted descending.
     */
    @Override
    public List<DriverDistance> getDriverRankReportByCity(final City city) {
        Map<Long,DriverDistance> driverDistanceMap = new HashMap<>();
        for (final Driver d : driverRepo.findAllDriversByCity(city)) {
            driverDistanceMap.put(d.getId(), new DriverDistanceImpl(d, Long.valueOf(0)));
        }

        for (final Delivery delivery : deliveryRepo.findAll()) {
            final Long key = delivery.getDriver().getId();
            if (driverDistanceMap.containsKey(key)){
                final Long sum = driverDistanceMap.get(key).getTotalDistance() +
                        Double.valueOf(delivery.getDistance()).longValue();
                driverDistanceMap.get(key).setTotalDistance(sum);
            }
        }

        List<DriverDistance> rankCityLst = new ArrayList(driverDistanceMap.values());
        Collections.sort(rankCityLst, new Sortbydistance());
        return rankCityLst;
    }
}
