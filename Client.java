package project2;

public class Client {
    private int id;
    private String address;
    private int port;
    
    public Client(int ID, String ipAddress, int destPort) {
        this.id = ID;
        address = ipAddress;
        port = destPort;
    }
    
    public int getId() {
        return id;
    }
    
    public String getAddress() {
        return address;
    }
    
    public int getPort() {
        return port;
    }
}
