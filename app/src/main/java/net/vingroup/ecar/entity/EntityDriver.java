package net.vingroup.ecar.entity;

/**
 * Created by dvmin on 1/19/2018.
 */

public class EntityDriver {

    String UserID;
    String UserName;
    String FullName;

    public EntityDriver(String userID, String userName, String fullName) {
        UserID = userID;
        UserName = userName;
        FullName = fullName;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }
}
