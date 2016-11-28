package comcmput301f16t01.github.carrier.Notifications;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import comcmput301f16t01.github.carrier.Listener;
import comcmput301f16t01.github.carrier.Requests.Request;
import comcmput301f16t01.github.carrier.Users.User;

/**
 * Controller. Allows a view to get information about notifications and/or allows
 * other controllers to set up new notifications.
 *
 * @see comcmput301f16t01.github.carrier.Requests.RequestController
 */
public class NotificationController {
    private static ArrayList<Notification> notificationList = new ArrayList<>();

    /**
     * Returns the list of notifications held by this controller relating to the user logged in
     * @see comcmput301f16t01.github.carrier.Users.UserController
     */
    public static ArrayList<Notification> getNotificationListInstance() {
        return notificationList;
    }

    /**
     * Fetches notifications for a user, does this on the main UI thread.
     * @see #asyncUnreadNotification(User, Listener) for better implementation off UI thread. 
     *
     * @return A sorted NotificationList
     * @see Notification#compareTo(Notification)
     */
    public ArrayList<Notification> fetchNotifications( User user ) {
        ElasticNotificationController.FindNotificationTask fnt = new ElasticNotificationController.FindNotificationTask();
        fnt.execute( user.getUsername() );
        try {
            notificationList.clear();
            notificationList.addAll( fnt.get() );
            Collections.sort(notificationList);
        } catch (Exception e) {
            Log.i("NotificationController", "bad error");
        }
        return notificationList;
    }

    /**
     *
     *
     * @param user The user you would like to search for unread notifications with
     * @param listener A listener that will be called if an unread notification is detected
     *                 If the listener is null,
     *
     * @see comcmput301f16t01.github.carrier.Notifications.ElasticNotificationController.FindNotificationTask
     */
    public void asyncUnreadNotification( User user, @Nullable Listener listener ) {
        ElasticNotificationController.FindNotificationTask fut = new ElasticNotificationController.FindNotificationTask();
        fut.addListener(listener);
        fut.execute( user.getUsername() );
    }

    /**
     * Clears all notifications for a user.
     *
     * @param user A user is anyone who uses our app. This is who we clear notifications for
     */
    public void clearAllNotifications( User user ) {
        ElasticNotificationController.ClearAllTask cat = new ElasticNotificationController.ClearAllTask();
        cat.execute( user.getUsername() );
        try {
            cat.get();
        } catch (Exception e) {
            // Make the Async in sync
            e.printStackTrace();
        }
        if (notificationList != null) {
            notificationList.clear();
        }
    }

    /**
     * Allows the creation of a new notification.
     *
     * @param userToAlert The user to send the notification to
     * @param relatedRequest The request that this notification is about.
     * @return the notification generated by this method
     */
    public Notification addNotification(@NonNull User userToAlert, @NonNull Request relatedRequest) {
        Notification newNotification = new Notification( userToAlert, relatedRequest );

        if(ConnectionChecker.isThereInternet()) {
            ElasticNotificationController.AddNotificationTask ant = new ElasticNotificationController.AddNotificationTask();
            ant.execute(newNotification);
        }
        return newNotification;
    }

    /**
     * Marks the given notification as read in elastic search.
     *
     * @param notification This is a message that is sent to a user
     */
    public void markNotificationAsRead( Notification notification ) {
        ElasticNotificationController.MarkAsReadTask mart = new ElasticNotificationController.MarkAsReadTask();
        mart.execute( notification.getID() );
        try {
            mart.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        notification.setRead( true );
    }

    /**
     * Marks all request for user as read, if they are currently unread.
     *
     * @param user A user is anyone who uses our app. This is who we will clear notifications for.
     * @see #markNotificationAsRead(Notification)
     */
    public void markAllAsRead( User user ) {
        ArrayList<Notification> notificationList = this.fetchNotifications( user );
        for (Notification notification : notificationList ) {
            if( !notification.isRead() ) {
                this.markNotificationAsRead(notification);
            }
        }
    }
}
