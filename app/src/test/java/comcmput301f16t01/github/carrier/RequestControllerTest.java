package comcmput301f16t01.github.carrier;

import org.junit.Test;
import static org.junit.Assert.*;

public class RequestControllerTest {
    @Test
    public void addRequestTest() {
        // TODO write tests to test adding a request via elastic search
    }

    @Test
    public void addOfferingDriverTest() {
        RequestController rc = new RequestController();
        User rider = new User("Rider");
        User offeringDriver = new User("Offering Driver");
        RequestList rl = RequestController.getInstance();
        Request request = new Request(rider, new Location(), new Location());
        rl.add(request);
        rc.addDriver(request, offeringDriver);
        assertTrue("Driver is not added as offering to the request.", request.getOfferedDrivers().contains(offeringDriver));
    }
    @Test
    public void getOfferedRequestsTest(){
        RequestController rc = new RequestController();
        User rider = new User("Rider");
        User offeringDriver = new User("Offering Driver");
        RequestList rl = RequestController.getInstance();
        Request request = new Request(rider, new Location(), new Location());
        rl.add(request);
        // Test that the empty requests work
        assertTrue("Driver has offered request when they shouldn't", rc.getOfferedRequests(offeringDriver).size() == 0);
        rc.addDriver(request, offeringDriver);
        // Make sure the driver is added the request
        assertTrue("Get offered requests is not returning the correct request", rc.getOfferedRequests(offeringDriver).contains(request));
        // Make sure that the driver is only added to one request.
        assertTrue("The driver is added to more than one reqeust", rc.getOfferedRequests(offeringDriver).size() == 1);
    }
    @Test
    public void getUserRequests() {
        RequestController rc = new RequestController();
        User rider1 = new User("Rider1");
        User rider2 = new User("Rider2");
        RequestList rl = RequestController.getInstance();
        // Make sure rider1 has no requests made
        assertTrue("rider1 has an open request when they shouldn't.", rc.getRequests(rider1).size() == 0);
        Request request2 = new Request(rider2, new Location(), new Location());
        rl.add(request2);
        // Make sure requests aren't added for the wrong rider.
        assertTrue("rider1 has an open request when they shouldn't.", rc.getRequests(rider1).size() == 0);
        Request request1 = new Request(rider1, new Location(), new Location());
        rl.add(request1);
        // Make sure the request is added to rider1's requests.
        assertTrue(rl == RequestController.getInstance());
        assertTrue(rl.contains(request1));
        assertTrue(request1.getRider() == rider1);
        assertTrue("Request not added for rider1", rc.getRequests(rider1).contains(request1));
        // Make sure only that request is added.
        assertTrue("More that one request was added to rider1's requests.", rc.getRequests(rider1).size() == 1);
    }


}