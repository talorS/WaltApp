package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @BeforeEach()
    public void prepareData(){
        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);
    }

    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("mexican", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }

    /**
     * Test case - a customer, which doesn't exist in the system, is trying to make a new order.
     * Expected result - test passed
     */
    @Test
    public void test1() {
        Throwable ex = assertThrows(Exception.class, () -> {
            Customer c1 = customerRepository.findByName("Moti");
            Restaurant r1 = restaurantRepository.findByName("cafe"); //TLV
            waltService.createOrderAndAssignDriver(c1, r1, new Date());
        });
        Assert.assertEquals("customer doesn't exist in the system!", ex.getMessage());
    }

    /**
     * Test case - customer is trying to order from restaurant, where the customer's city is different from
     * the restaurant's city.
     * Expected result - test passed
     */
    @Test
    public void test2() {
        Throwable ex = assertThrows(Exception.class, () -> {
            Customer c1 = customerRepository.findByName("Beethoven"); //TLV
            Restaurant r1 = restaurantRepository.findByName("meat"); //Jerusalem
            waltService.createOrderAndAssignDriver(c1, r1, new Date());
        });
        Assert.assertEquals("customer's city and restaurant's city doesn't match!", ex.getMessage());
    }

    /**
     * Test case - customer is trying to order from a city without any drivers
     * Expected result - test passed
     */
    @Test
    public void test3() {
        Throwable ex = assertThrows(Exception.class, () -> {
            City modiin = new City("Modiin");
            cityRepository.save(modiin);
            Restaurant r1 = new Restaurant("restaurant", modiin, "test");
            Customer c1 = new Customer("Moti", modiin, "test");
            waltService.createOrderAndAssignDriver(c1, r1, new Date());
        });
        Assert.assertEquals("Selected city doesn't have drivers!", ex.getMessage());
    }

    /**
     * Test case - customer is trying to order, but there are no available drivers right now
     * Expected result - test passed
     */
    @Test
    public void test4() {
        Throwable ex = assertThrows(Exception.class, () -> {
            Customer c1 = customerRepository.findByName("Rachmaninoff"); //TLV
            Restaurant r1 = restaurantRepository.findByName("cafe"); //TLV
            Delivery d1 = new Delivery(
                    driverRepository.findByName("Patricia"),//TLV
                    restaurantRepository.findByName("cafe"),//TLV
                    customerRepository.findByName("Beethoven"),//TLV
                    new GregorianCalendar(2021, Calendar.AUGUST, 6,21,0).getTime()
            );
            d1.setDistance(10);

            Delivery d2 = new Delivery(
                    driverRepository.findByName("Mary"),//TLV
                    restaurantRepository.findByName("cafe"),//TLV
                    customerRepository.findByName("Rachmaninoff"),//TLV
                    new GregorianCalendar(2021, Calendar.AUGUST, 6,20,45).getTime()
            );
            d2.setDistance(4);

            Delivery d3 = new Delivery(
                    driverRepository.findByName("Daniel"),//TLV
                    restaurantRepository.findByName("cafe"),//TLV
                    customerRepository.findByName("Rachmaninoff"),//TLV
                    new GregorianCalendar(2021, Calendar.AUGUST, 6,20,30).getTime()
            );
            d3.setDistance(4);
            deliveryRepository.saveAll(Lists.newArrayList(d1,d2,d3)); //TLV
            waltService.createOrderAndAssignDriver(c1, r1, new Date()); //TLV
        });
        Assert.assertEquals("There isn't available driver at this moment.", ex.getMessage());
    }

    /**
     * Test case - a few drivers are available, the least busy one should be chosen.
     * Expected result - Daniel assigned to the new order
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        Delivery d1 = new Delivery(
                driverRepository.findByName("Patricia"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Beethoven"),//TLV
                new GregorianCalendar(2021, Calendar.AUGUST, 6,11,0).getTime()
        );
        d1.setDistance(10);
        Delivery d2 = new Delivery(
                driverRepository.findByName("Patricia"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Beethoven"),//TLV
                new GregorianCalendar(2021, Calendar.AUGUST, 6,13,0).getTime()
        );
        d2.setDistance(10);

        Delivery d3 = new Delivery(
                driverRepository.findByName("Mary"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Rachmaninoff"),//TLV
                new GregorianCalendar(2021, Calendar.AUGUST, 6,21,0).getTime()
        );
        d3.setDistance(4);

        Delivery d4 = new Delivery(
                driverRepository.findByName("Daniel"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Rachmaninoff"),//TLV
                new GregorianCalendar(2021, Calendar.AUGUST, 6,12,30).getTime()
        );
        d3.setDistance(4);
        deliveryRepository.saveAll(Lists.newArrayList(d1,d2,d3,d4));

        Customer c1 = customerRepository.findByName("Bach"); //TLV
        Restaurant r1 = restaurantRepository.findByName("vegan"); //TLV
        Delivery newDel = waltService.createOrderAndAssignDriver(
                c1, r1, new Date());

        assertEquals("Daniel", newDel.getDriver().getName());
    }

    /**
     * checks if rank report is correct
     */
    @Test
    public void test6()
    {
        Delivery d1 = new Delivery(
                driverRepository.findByName("Patricia"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Beethoven"),//TLV
                new Date()
        );
        d1.setDistance(10);

        Delivery d2 = new Delivery(
                driverRepository.findByName("Patricia"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Beethoven"),//TLV
                new Date()
        );
        d2.setDistance(10);

        Delivery d3 = new Delivery(
                driverRepository.findByName("Patricia"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Beethoven"),//TLV
                new Date()
        );
        d3.setDistance(10);

        Delivery d4 = new Delivery(
                driverRepository.findByName("Robert"),//JSM
                restaurantRepository.findByName("meat"),//JSM
                customerRepository.findByName("Mozart"),//JSM
                new Date()
        );
        d4.setDistance(5);

        Delivery d5 = new Delivery(
                driverRepository.findByName("Robert"),//JSM
                restaurantRepository.findByName("meat"),//JSM
                customerRepository.findByName("Mozart"),//JSM
                new Date()
        );
        d5.setDistance(5);

        Delivery d6 = new Delivery(
                driverRepository.findByName("Mary"),//TLV
                restaurantRepository.findByName("vegan"),//TLV
                customerRepository.findByName("Bach"),//TLV
                new Date()
        );
        d6.setDistance(5);
        deliveryRepository.saveAll(Lists.newArrayList(d1,d2,d3,d4,d5,d6));

        List<DriverDistance> dd = waltService.getDriverRankReport();

        assertEquals(driverRepository.findByName("Patricia").getId(),dd.get(0).getDriver().getId());
        assertTrue(dd.get(0).getTotalDistance() == Long.valueOf(30));
        assertEquals(driverRepository.findByName("Robert").getId(),dd.get(1).getDriver().getId());
        assertTrue(dd.get(1).getTotalDistance() == Long.valueOf(10));
        assertEquals(driverRepository.findByName("Mary").getId(),dd.get(2).getDriver().getId());
        assertTrue(dd.get(2).getTotalDistance() == Long.valueOf(5));

        for(int i=3;i<dd.size();i++) //the rest must be 0
        {
            assertTrue(dd.get(i).getTotalDistance() == Long.valueOf(0));
        }
    }

    /**
     * checks if rank report by city is correct
     */
    @Test
    public void test7()
    {
        Delivery d1 = new Delivery(
                driverRepository.findByName("Patricia"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Beethoven"),//TLV
                new Date()
        );
        d1.setDistance(10);

        Delivery d2 = new Delivery(
                driverRepository.findByName("Patricia"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Beethoven"),//TLV
                new Date()
        );
        d2.setDistance(10);

        Delivery d3 = new Delivery(
                driverRepository.findByName("Patricia"),//TLV
                restaurantRepository.findByName("cafe"),//TLV
                customerRepository.findByName("Beethoven"),//TLV
                new Date()
        );
        d3.setDistance(10);

        Delivery d4 = new Delivery(
                driverRepository.findByName("Daniel"),//TLV
                restaurantRepository.findByName("chinese"),//TLV
                customerRepository.findByName("Bach"),//TLV
                new Date()
        );
        d4.setDistance(7);

        Delivery d5 = new Delivery(
                driverRepository.findByName("Daniel"),//TLV
                restaurantRepository.findByName("chinese"),//TLV
                customerRepository.findByName("Bach"),//TLV
                new Date()
        );
        d5.setDistance(9);

        Delivery d6 = new Delivery(
                driverRepository.findByName("Mary"),//TLV
                restaurantRepository.findByName("vegan"),//TLV
                customerRepository.findByName("Bach"),//TLV
                new Date()
        );
        d6.setDistance(8);
        deliveryRepository.saveAll(Lists.newArrayList(d1,d2,d3,d4,d5,d6));

        List<DriverDistance> dd = waltService.getDriverRankReportByCity(cityRepository.findByName("Tel-Aviv"));

        assertEquals(driverRepository.findByName("Patricia").getId(),dd.get(0).getDriver().getId());
        assertTrue(dd.get(0).getTotalDistance() == Long.valueOf(30));
        assertEquals(driverRepository.findByName("Daniel").getId(),dd.get(1).getDriver().getId());
        assertTrue(dd.get(1).getTotalDistance() == Long.valueOf(16));
        assertEquals(driverRepository.findByName("Mary").getId(),dd.get(2).getDriver().getId());
        assertTrue(dd.get(2).getTotalDistance() == Long.valueOf(8));
    }
    /**
     * checks few first orders in the system
     * @throws Exception
     */
    @Test
    public void test8() throws Exception
    {
        Customer c1 = customerRepository.findByName("Mozart"); //Jerusalem
        Customer c2 = customerRepository.findByName("Bach"); //TLV
        Customer c3 = customerRepository.findByName("Beethoven"); //TLV
        Customer c4 = customerRepository.findByName("Rachmaninoff"); //TLV
        Restaurant r1 = restaurantRepository.findByName("meat"); //Jerusalem
        Restaurant r2 = restaurantRepository.findByName("vegan"); //TLV
        Restaurant r3 = restaurantRepository.findByName("cafe"); //TLV

        Delivery d1 = waltService.createOrderAndAssignDriver(
                c1, r1, new Date());
        deliveryRepository.save(d1);
        d1 = waltService.createOrderAndAssignDriver(
                c2, r2, new Date());
        deliveryRepository.save(d1);
        d1 = waltService.createOrderAndAssignDriver(
                c2, r3, new Date());
        deliveryRepository.save(d1);
        d1 = waltService.createOrderAndAssignDriver(
                c3, r2, new Date());
        deliveryRepository.save(d1);
/*  Exception point - no available drivers is TLV
         d1 = waltService.createOrderAndAssignDriver(
                c4, r2, new Date());
        deliveryRepository.save(d1);
*/

        //check delivery list contains all orders
        assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(),4);

        //check not assigning a busy driver to a new order
        assertTrue(((List<Delivery>) deliveryRepository.findAll()).get(1).getDriver().getId() !=
                ((List<Delivery>) deliveryRepository.findAll()).get(2).getDriver().getId());

        //check highest total distance > 0
        List<DriverDistance> dd = waltService.getDriverRankReport();
        assertTrue(dd.get(0).getTotalDistance() > Long.valueOf(0));
    }
}
