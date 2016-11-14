package comcmput301f16t01.github.carrier.Requests;

import android.content.Context;
import android.location.Location;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import comcmput301f16t01.github.carrier.ElasticUserController;
import comcmput301f16t01.github.carrier.Listener;
import comcmput301f16t01.github.carrier.Notifications.NotificationController;
import comcmput301f16t01.github.carrier.User;

/**
 * Uses a singleton pattern to query and get results of requests.
 *
 * Typical use:
 *      rc.searchType( params );            // Change the singleton's information
 *      requestList = rc.getResults();      // Get the results of that search (global to program)
 *
 * Or use one of the getX() functions to get immediate info.
 */
public class RequestController {
    /** Singleton instance of RequestController */
    private static RequestList requestList;

    /**
     * Prevents errors when a RequestController is initialized and methods that require requestList
     * to not be null (i.e. getResult() )
     */
    public RequestController() {
        // Note that requestList is static, so it will not be null if you create a second instance of RequestController
        if (requestList == null) {
            requestList = new RequestList();
        }
    }

    /** Adds a request to elastic search. */
    public String addRequest(Request request) {
        if (request.getStart() == null || request.getEnd() == null) {
            return "You must first select a start and end location";
        } else if (request.getFare() == -1) {
            return "You must first estimate the fare";
        } else {
            ElasticRequestController.AddRequestTask art = new ElasticRequestController.AddRequestTask();
            art.execute(request);
            return null;
        }
    }

    /** Clears information in the singleton, not exactly necessary */
    // TODO check the necessity of this function.
    public void clear() {
        requestList = new RequestList();
    }

    /**
     * Deprecated: see fetch requests where rider?
     */
    @Deprecated
    public RequestList getRequests(User rider) {
        RequestList returnValue = new RequestList();
        for (Request request : requestList) {
            if (request.getRider() == rider) {
                returnValue.add(request);
            }
        }
        return returnValue;
    }


    // TODO Why does this need a rider? You can cancel a request just knowing the request.
    public void cancelRequest(User rider, Request request) {
        // TODO test elastic search component
        request.setStatus(Request.CANCELLED);
    }

    /**
     * Is used to add a driver to a request.
     *
     * @param request The request we are modifying
     * @param driver  the driver that is being added as a driver for the request.
     *
     * @see Offer
     */
    public void addDriver(Request request, User driver) {
        // create an offer object [[ potentially throws IllegalArgumentException if called wrong ]]
        Offer newOffer = new Offer(request, driver);
        // Add offer to elastic search
        ElasticRequestController.AddOfferTask aot = new ElasticRequestController.AddOfferTask();
        aot.execute( newOffer );
        // TODO add addOffer task to queue if offline

        // Add a notification
        NotificationController nc = new NotificationController();
        nc.addNotification( request.getRider(), request );
        // TODO add addNotification to queue if offline

        request.addOfferingDriver(driver);
    }

    /**
     * Is used to show that the user has accepted the provided driver. The accepted driver should
     * have been added with addDriver() before being accepted.
     *
     * @param request The request that is being modified
     * @param driver  The driver that is being accepted
     */
    public void confirmDriver(Request request, User driver) {

        // TODO Elastic Requests...
        // only on success should we send out a notification!
        NotificationController nc = new NotificationController();
        nc.addNotification( driver, request );
        // TODO check for notification success?
    }

    public void completeRequest(Request request) {
    }

    public void payForRequest(Request request) {
    }

    /**
     * Search requests by the keyword, will set it so the singleton contains the information for
     * this query. Use getResults() to get the information.
     * @param keyword
     */
    public void searchByKeyword(String keyword) {
        ElasticRequestController.SearchByKeywordTask sbkt = new ElasticRequestController.SearchByKeywordTask();
        sbkt.execute(keyword);
        try {
            requestList = sbkt.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        /**
     * Search requests by a location. This sets it so the singleton contains the information for
     * this query. Use getResults() to get the information.
     */
    public void searchByLocation( /* location parameters? */ ) {

    }

    /**
     * Is used to provide a driver with a list of the requests that the driver has offered to give a ride.
     *
     * @param driver The driver who is requesting the list of the requests they have offered
     *               but the rider has no confirmed their choice in driver.
     * @return An ArrayList of requests that the driver has offered to give a ride on.
     */
    // TODO rename this method? i.e. getRequestsWhereDriverOffered, or something
    public RequestList getOfferedRequests(User driver) {
        ElasticRequestController.GetOfferedRequestsTask gort = new ElasticRequestController.GetOfferedRequestsTask();
        gort.execute( driver.getUsername() );

        try {
            return gort.get();
        } catch (Exception e) {
            return new RequestList();
        }
    }

    public void clearAllRiderRequests(User rider) {
        ElasticRequestController.ClearRiderRequestsTask crrt = new ElasticRequestController.ClearRiderRequestsTask();
        crrt.execute( rider.getUsername() );
    }

    /**
     *
     * @param rider the rider you want to match requests against
     * @param statuses the statues you would like to see (filters non listed ones) (null means grab all)
     * @return A list of requests from the given criteria
     */
    public RequestList fetchRequestsWhereRider(User rider, Integer... statuses ) {
        // Open a fetch task for the user
        ElasticRequestController.FetchRiderRequestsTask frrt = new ElasticRequestController.FetchRiderRequestsTask();

        // Convert the parameters of this method to a string array for the execution of the task
        String[] vars = new String[1 + statuses.length];
        vars[0] = rider.getUsername();
        for (int i = 1; i <= statuses.length; i++ ) {
            vars[i] = Integer.toString( statuses[i-1] );
        }
        frrt.execute( vars );

        // Get the found requests from the task
        RequestList foundRequests = new RequestList();
        try {
            foundRequests = frrt.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestList.replaceList( foundRequests );
        return foundRequests;
    }

    public RequestList fetchAllRequestsWhereRider( User rider ) {
        ElasticRequestController.FetchRiderRequestsTask frrt = new ElasticRequestController.FetchRiderRequestsTask();
        frrt.execute( rider.getUsername() );
        RequestList foundRequests = new RequestList();
        try {
            foundRequests = frrt.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestList.replaceList( foundRequests );
        return foundRequests;
    }

    public void addListener( Listener listener ) {
        requestList.addListener( listener );
    }


    /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * * * * * * * * * * * * * * * * *    DEPRECATED FUNCTIONS   * * * * * * * * * * * * * * * * * *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

    /**
     * Deprecated: use the void getSearchByLocation
     */
    @Deprecated
    public ArrayList<Request> getSearchByLocation(Location location) {
        return new ArrayList<>();
    }

    /**
     * Deprecated: This is literally built into a Request. (see the ArrayList of offered drivers)
     */
    @Deprecated
    public ArrayList<User> getDrivers(Request request) {
        return new ArrayList<User>();
    }

    /**
     * Deprecated: There are several other functions that do this. Also, try to only use
     * getSearchByKeyword or getSearchByLocation
     */
    @Deprecated
    public ArrayList<Request> getAvailableRequests() {
        return new ArrayList<Request>();
    }

    /** Get the results of a searchByKeyword or a getSearchByLocation query. */
    public RequestList getResult() {
        return requestList;
    }

    /**
     * Deprecated: Use getResult instead.
     */
    @Deprecated
    public static RequestList getInstance() {
        if (requestList == null) {
            //requestList = new ArrayList<Request>();
        }
        return requestList;
    }

    /**
     * Deprecated: There is no user story that says we need to modify a request description after it has been
     * created
     */
    @Deprecated
    public void setRequestDescription(Request request, String description) {
    }

    /**
     * Deprecated: Only use getSearchByKeyword or getSearchByLocation?
     */
    @Deprecated
    public ArrayList<Request> getOpenRequests() {
        return new ArrayList<Request>();
    }


    /**
     * Deprecated: use the void function instead (singleton changer) so that this can be used with
     * the getResults() method
     */
    @Deprecated
    public ArrayList<Request> getSearchByKeyword(String query) {
        return new ArrayList<>();
    }

//    /**
//     * Deprecated: should use new function that uses elastic search or FileIO (depending on
//     * connectivity), not singleton?
//     */
//    @Deprecated
//    public ArrayList<Request> getRequests(User rider) {
//        return requestList;
//    }
}