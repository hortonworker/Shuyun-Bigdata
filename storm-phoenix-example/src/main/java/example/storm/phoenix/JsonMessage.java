package example.storm.phoenix;

/**
 * Created by chen.yang on 2018/6/26.
 */
import com.fasterxml.jackson.annotation.JsonProperty;


public class JsonMessage {
    @JsonProperty(value = "orderid")
    private String orderID;
    @JsonProperty(value = "account")
    private String account;
    @JsonProperty(value = "vendorid")
    private String vendorID;
    @JsonProperty(value = "itemid")
    private String itemID;
    @JsonProperty(value = "itemquantity")
    private int itemQuantity;
    @JsonProperty(value = "status")
    private String status;
    @JsonProperty(value = "time")
    private long time;

    //take orderid as key, and salt(orderid) for balance
    public String getOrderID() {
        return orderID;
    }
    public void setOrderID(String id) {
        this.orderID = id;
    }

    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

    public String getItemID() {
        return itemID;
    }
    public void setItemID(String item) {
        this.itemID = item;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }
    public void setItemQuantity(int number) {
        this.itemQuantity = number;
    }

    public String getVendorID() {
        return vendorID;
    }
    public void setVendorID(String vendor) {
        this.vendorID = vendor;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }
}