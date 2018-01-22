package net.vingroup.ecar.entity;

/**
 * Created by dvmin on 1/18/2018.
 */

public class EntityLogin {

    public Integer LoginID;
    public Integer UserID;
    public String Name;
    public String DomainName;
    public String FirstName;
    public String Description;
    public String SiteName;

    public EntityLogin(Integer loginID, Integer userID, String name, String domainName, String firstName, String description, String siteName) {
        LoginID = loginID;
        UserID = userID;
        Name = name;
        DomainName = domainName;
        FirstName = firstName;
        Description = description;
        SiteName = siteName;
    }

    public Integer getLoginID() {
        return LoginID;
    }

    public void setLoginID(Integer loginID) {
        LoginID = loginID;
    }

    public Integer getUserID() {
        return UserID;
    }

    public void setUserID(Integer userID) {
        UserID = userID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDomainName() {
        return DomainName;
    }

    public void setDomainName(String domainName) {
        DomainName = domainName;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getSiteName() {
        return SiteName;
    }

    public void setSiteName(String siteName) {
        SiteName = siteName;
    }


}
