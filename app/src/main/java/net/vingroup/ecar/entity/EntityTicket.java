package net.vingroup.ecar.entity;

/**
 * Created by dvmin on 1/19/2018.
 */

public class EntityTicket {

    int RowNumber;
    int WorlOrderId;
    String Title;
    String SiteName;
    String Requester;
    String ServiceName;
    String CategoryName;
    String CreatedTime;
    String DueByTime;
    String CompletedTime;
    String ResolvedTime;
    String Priority;
    String StatusName;
    String Place;
    String TotalTime;
    String OverTime;
    String StatusAlert;
    String SiteID;
    String TechnicianName;
    String Updated_Date;
    String StatusID;

    public EntityTicket(int rowNumber, int worlOrderId, String title, String siteName, String requester, String serviceName, String categoryName, String createdTime, String dueByTime, String completedTime, String resolvedTime, String priority, String statusName, String place, String totalTime, String overTime, String statusAlert,String StatusID1, String TechnicianName1, String Updated_Date1,String siteID) {
        RowNumber = rowNumber;
        WorlOrderId = worlOrderId;
        Title = title;
        SiteName = siteName;
        Requester = requester;
        ServiceName = serviceName;
        CategoryName = categoryName;
        CreatedTime = createdTime;
        DueByTime = dueByTime;
        CompletedTime = completedTime;
        ResolvedTime = resolvedTime;
        Priority = priority;
        StatusName = statusName;
        Place = place;
        TotalTime = totalTime;
        OverTime = overTime;
        StatusAlert = statusAlert;
        StatusID = StatusID1;
        TechnicianName = TechnicianName1;
        Updated_Date = Updated_Date1;
        SiteID = siteID;

    }


    public int getRowNumber() {
        return RowNumber;
    }

    public void setRowNumber(int rowNumber) {
        RowNumber = rowNumber;
    }

    public int getWorlOrderId() {
        return WorlOrderId;
    }

    public void setWorlOrderId(int worlOrderId) {
        WorlOrderId = worlOrderId;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getSiteName() {
        return SiteName;
    }

    public void setSiteName(String siteName) {
        SiteName = siteName;
    }

    public String getRequester() {
        return Requester;
    }

    public void setRequester(String requester) {
        Requester = requester;
    }

    public String getServiceName() {
        return ServiceName;
    }

    public void setServiceName(String serviceName) {
        ServiceName = serviceName;
    }

    public String getCategoryName() {
        return CategoryName;
    }

    public void setCategoryName(String categoryName) {
        CategoryName = categoryName;
    }

    public String getCreatedTime() {
        return CreatedTime;
    }

    public void setCreatedTime(String createdTime) {
        CreatedTime = createdTime;
    }

    public String getDueByTime() {
        return DueByTime;
    }

    public void setDueByTime(String dueByTime) {
        DueByTime = dueByTime;
    }

    public String getCompletedTime() {
        return CompletedTime;
    }

    public void setCompletedTime(String completedTime) {
        CompletedTime = completedTime;
    }

    public String getResolvedTime() {
        return ResolvedTime;
    }

    public void setResolvedTime(String resolvedTime) {
        ResolvedTime = resolvedTime;
    }

    public String getPriority() {
        return Priority;
    }

    public void setPriority(String priority) {
        Priority = priority;
    }

    public String getStatusName() {
        return StatusName;
    }

    public void setStatusName(String statusName) {
        StatusName = statusName;
    }

    public String getPlace() {
        return Place;
    }

    public void setPlace(String place) {
        Place = place;
    }

    public String getTotalTime() {
        return TotalTime;
    }

    public void setTotalTime(String totalTime) {
        TotalTime = totalTime;
    }

    public String getOverTime() {
        return OverTime;
    }

    public void setOverTime(String overTime) {
        OverTime = overTime;
    }

    public String getStatusAlert() {
        return StatusAlert;
    }

    public void setStatusAlert(String statusAlert) {
        StatusAlert = statusAlert;
    }

    public String getStatusID() {
        return StatusID;
    }

    public void setStatusID(String statusID) {
        StatusID = statusID;
    }

    public String getTechnicianName() {
        return TechnicianName;
    }

    public void setTechnicianName(String technicianName) {
        TechnicianName = technicianName;
    }

    public String getUpdated_Date() {
        return Updated_Date;
    }

    public void setUpdated_Date(String updated_Date) {
        Updated_Date = updated_Date;
    }

    public String getSiteID() {
        return SiteID;
    }

    public void setSiteID(String siteID) {
        SiteID = siteID;
    }
}
