package project2;
import java.net.*;
import java.util.*;
import java.io.*;

public class Server {
    
    static final boolean DEBUG_MODE = true;
    
    static Map<Integer, HashMap<Integer, Integer>> routingTables; // neighbor, neighborofneighbor, cost between neighbor and their neighbors
    static Map<String, Integer> neighborIpToId;
    static Map<Integer, Node> neighbors;
    static Map<Integer, Integer> edges;
    static int myID;
    static int numOfPacks = 0;
    static boolean isCrashed = false;
    
    void readFileFromCmdArg() {
        Scanner scanner = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
        while (scanner.hasNextLine()) {
            System.out.println(scanner.nextLine());
        }
    }
    
    public static int maxNumPeers;
    private int id;
    private String hostAddress;
    private ServerSocket socketListener;
    private Socket clientSocket;
    static int listeningPort;
    public static HashMap<Integer, Client> clientList;
    private static HashMap<Client, Socket> clientSocketMap;
    DatagramSocket datagramSocket;
    
    public Server(int port) {
        listeningPort = port;
        maxNumPeers = 0;
        
        try {
            socketListener = new ServerSocket(listeningPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        hostAddress = "";
        clientSocket = null;
        id = 0;
        clientList = new HashMap<>();
        clientSocketMap = new HashMap<>();
        System.out.println("Welcome!");
    }
    
    public static void setupIntervalUpdate(int routUpd){
        new Thread(() -> {
            try{
                
                while(!isCrashed){
                    Thread.sleep(routUpd * 1000);  //1000 milliseconds is one second.
                    if (!isCrashed ){
                        Server.updateAll();
                    }
                    
                }
            } catch (Exception e){
                System.out.println("Not able to run.");
                e.printStackTrace();
            }
        }).start();
    }
    
    public String myIP() {
        String address = "";
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println("Cannot recognize host");
        }
        
        hostAddress = address;
        return hostAddress;
    }
    
    // Get the listening port number of the host
    public int getPortNumber() {
        return listeningPort;
    }
    /*
     
     Number_of_update_fields        Server_port
     Server_IP
     
     
     Server_IP_address_1
     Server_port_1       0x0
     Server_ID_1       Cost_1
     
     
     Server_IP_address_2
     Server_port_2       0x0
     Server_ID_2       Cost_2
     
     ....
     ....
     
     
     */
    public void processRecvdPacketData(String data) {
        
        String [] messageElements = data.split(",");
        
        //System.out.println("Received message: " + data + " which has " + messageElements.length + " elements");
        int i = 0;
        
        /*
         String message = "";
         message += edges.size() + ",";
         message += neighbors.get(myID).port + ",";
         message += neighbors.get(myID).ip + ",";
         for(Integer neighborId : neighbors.keySet()){
         
         message += neighbors.get(neighborId).ip + ",";
         message += neighbors.get(neighborId).port + ",";
         message += neighborId + ",";
         message += edges.get(neighborId) + ",";
         
         }
         
         */
        
        int numUpdates = Integer.parseInt(messageElements[0]);
        String serverPort = messageElements[1];
        String serverIp = messageElements[2];
        int serverId = neighborIpToId.get(serverIp);
        
        int messageElementsIndex = 3;
        int myCostToServer;
        if(edges.containsKey(serverId)){
            myCostToServer = edges.get(serverId);
        } else {
            myCostToServer = Integer.MAX_VALUE;
        }
        
        for(int j = 0; j < numUpdates; j++) {
            String neighborIp = messageElements[
                                                messageElementsIndex++];
            String neighborPort = messageElements[messageElementsIndex++];
            int neighborId;
            int neighborCost;
            if(!messageElements[messageElementsIndex].equals("null")){
                neighborId = Integer.parseInt(messageElements[messageElementsIndex++]);
                
            } else {
                neighborId = -1;
                messageElementsIndex++;
            }
            if(!messageElements[messageElementsIndex].equals("null")) {
                if(messageElements[messageElementsIndex].contains("inf")) {
                    neighborCost = Integer.MAX_VALUE;
                    messageElementsIndex++;
                } else {
                    
                    neighborCost = Integer.parseInt(messageElements[messageElementsIndex++]);
                }
                
            } else {
                neighborCost = Integer.MAX_VALUE;
                messageElementsIndex++;
            }
            
            if(neighborId == myID) {
                edges.put(serverId, neighborCost);
            }
            
            routingTables.get(serverId).put(neighborId, neighborCost);
        }
        /*
        System.out.println("\n\nUpdated table:");
        
        for(Integer id : routingTables.keySet()) {
            
            System.out.println("Server "+id+":");
            System.out.println("+---+---+-----+");
            
            for(Integer neighbId : routingTables.get(id).keySet() ){
                String curCost = (routingTables.get(id).get(neighbId) == Integer.MAX_VALUE) ? "inf" : (routingTables.get(id).get(neighbId) + "");
                
                System.out.println("| " + id + " | " +neighbId+" | " + curCost +" | ");
                System.out.println("+---+---+-----+");
            }
            System.out.println("");
        }
        */
        
        
        if(serverId != myID && isCrashed == false){
            System.out.println("\nMESSAGE RECEIVED FROM SERVER " + serverId);
        }
        
        
        //PRINTS OUT THE MESSAGE ELEMENTS
        
        /*for(String str : messageElements){
         System.out.println("element " + i++ + ": " + str);
         
         }*/
        
        
    }
    
    // Sets up the server socket and establishes connection with clients
    public void setupListeningSocket() {
        System.out.println("Waiting for a client to connect on port " + listeningPort + "...");
        
        byte[] incomingMessage = new byte[1024];
        DatagramPacket packet = new DatagramPacket(incomingMessage, incomingMessage.length);
        
        new Thread(() -> {
            
            try {
                datagramSocket = new DatagramSocket(listeningPort);
                
                while (true) {
                    try {
                        
                        if (datagramSocket != null) {
                            
                            datagramSocket.receive(packet);
                            numOfPacks++;
                            String modifiedSentence = new String(packet.getData(), 0, packet.getLength());
                            //System.out.println("\n\nFROM SERVER: " + modifiedSentence);
                            
                            processRecvdPacketData(modifiedSentence);
                            
                            
                            
                        } else {
                            System.out.println("The datagram socket is NULL!");
                            Thread.currentThread().interrupt();
                            
                            return;
                        }
                    } catch (Exception e) {
                        System.out.println("Cannot connect to client!");
                        e.printStackTrace();
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            
        }).start();
    }
    
    public static void populateData(String filename) {
        Scanner scanner = new Scanner(filename);
        try {
            scanner = new Scanner(new BufferedReader(new FileReader(filename)));
        } catch (FileNotFoundException e) {
            System.out.println("File must be added to run.");
        }
        
        neighbors = new HashMap<>();
        edges = new HashMap<>();
        neighborIpToId = new HashMap<>();
        int numberOfServers = Integer.parseInt(scanner.nextLine());
        int numberOfLinks = Integer.parseInt(scanner.nextLine());
        for (int serverIndex = 0; serverIndex < numberOfServers; serverIndex++) {
            String[] serverStrings = scanner.nextLine().split(" ");
            int serverID = Integer.parseInt(serverStrings[0]);
            String serverIP = serverStrings[1];
            String serverPort = serverStrings[2];
            Node newNeighbor = new Node();
            newNeighbor.ip = serverIP;
            newNeighbor.port = serverPort;
            //newNeighbor.neighbors = new HashMap<>();
            routingTables.put(serverID, new HashMap<>());
            //routingTables.get(newNeighbor).put();
            neighbors.put(serverID, newNeighbor);
            neighborIpToId.put(serverIP, serverID);
            
        }
        boolean foundMyID = false;
        myID = 0;
        for (int linkIndex = 0; linkIndex < numberOfLinks; linkIndex++) {
            String[] linkStrings = scanner.nextLine().split(" ");
            int toServerID = Integer.parseInt(linkStrings[1]);
            int edgeCost = Integer.parseInt(linkStrings[2]);
            if (!foundMyID) {
                foundMyID = true;
                myID = Integer.parseInt(linkStrings[0]);
            }
            edges.put(toServerID, edgeCost);
        }
        edges.put(myID, 0);
        
        listeningPort = Integer.parseInt(neighbors.get(myID).port);
        
        if (DEBUG_MODE) {
            
            Set<Integer> neighborIds = neighbors.keySet();
            
            for (int id : neighborIds) {
                Node currentNode = neighbors.get(id);
                System.out.println("Server " + id + " ~~ " + currentNode.ip + ":" + currentNode.port);
            }
        }
    }
    
    public static void help() {
        System.out.println("**************************HELP MENU*****************************\n");
        System.out.println("update <server-ID1><server-ID2><link_cost>");
        System.out.println("   : Change cost between 2 links.\n");
        System.out.println("step");
        System.out.println("   : Send routing update to neighbors right away.\n");
        System.out.println("packets");
        System.out.println("   : Display the number of distance vector packets this server\n"
                           + "     has received since the last invocation of this information\n");
        System.out.println("display");
        System.out.println("   : Display the current routing number.\n");
        System.out.println("disable <server-ID>");
        System.out.println("   : Disable the link to a given server.\n");
        System.out.println("crash");
        System.out.println("   : Close all connections on all links.\n");
        System.out.println("****************************************************************");
    }
    
    public static void update(int server1, int server2, int cost) {
        if (myID != server1) {
            System.out.println("Can't do that! Can only change the cost between you and your neighbor!");
        } else {
            edges.put(server2, cost);
        }
    }
    
    public static void display() {
        Set<Integer> edgeIds = edges.keySet();
        System.out.println("\nServer " + myID + "'s Cost Table: \n<Neighbor-ID><Server-ID><Cost>");
        for (int id : edgeIds) {
            int currentEdgeCost = edges.get(id);
            String curId = (myID == Integer.MAX_VALUE) ? "inf" : (myID + "") ;
            String curCost = (currentEdgeCost == Integer.MAX_VALUE) ? "inf" : (currentEdgeCost + "");
            System.out.println("      "+id + "\t        " + curId + "\t  " + curCost);
            
        }
        int [] costs = new int[routingTables.size()];
        int [] nextHops = new int[routingTables.size()];
        boolean [] visited = new boolean[routingTables.size()];
        
        for(int i = 0; i < routingTables.size(); i++) {
            costs[i] = Integer.MAX_VALUE;
            nextHops[i] = Integer.MAX_VALUE;
        }
        
        for(int nodeId : routingTables.keySet()) {
            
            if(edges.containsKey(nodeId)) {
                costs[nodeId - 1] = edges.get(nodeId);
                nextHops[nodeId - 1] = nodeId;
            }
            
        }
        
        for(int nodeId : routingTables.keySet()) {
            if(nodeId != myID) {
                for(int nodeNeighborId : routingTables.get(nodeId).keySet()) {
                    if(nodeNeighborId != myID) {
                        if(costs[nodeId - 1] < Integer.MAX_VALUE) {
                            if (routingTables.get(nodeId).get(nodeNeighborId) != Integer.MAX_VALUE) {
                                int totalCostFromMeToThisNode = routingTables.get(nodeId).get(nodeNeighborId) + costs[nodeId - 1];
                                if(totalCostFromMeToThisNode < costs[nodeNeighborId - 1]){
                                    costs[nodeNeighborId - 1] = totalCostFromMeToThisNode;
                                    nextHops[nodeNeighborId - 1] = nodeId;
                                } else {
                                    //System.out.println("totalCostFromMeToThisNode not smaller than costs[" + (nodeNeighborId - 1)+"]");
                                }
                                
                            }
                        } else {
                            //System.out.println("costs[nodeId - 1] > Integer.MAX_VALUE]" + (nodeNeighborId - 1));
                        }
                    } else {
                        //System.out.println("nodeNeighborId == myID: " + (nodeNeighborId - 1));
                    }
                }
            } else {
                //System.out.println("(nodeId == myID) is: " + (nodeId - 1));
            }
        }
        System.out.println("\nServer " + myID + "'s Routing Table");
        System.out.println("+---dest----+--nexthop--+----cost---+");
        System.out.println("+-----------+-----------+-----------+");
        for(int i = 0; i < routingTables.size(); i++){
            int check = costs[i];
            if (check == Integer.MAX_VALUE)
            {
                if(nextHops[i] == Integer.MAX_VALUE) {
                    System.out.println("|      "+(i+1)+"    |    "+"inf"+"    |     "+"inf"+"   |");
                    
                } else  {
                    System.out.println("|      "+(i+1)+"    |     "+nextHops[i]+"     |     "+"inf"+"   |");
                }
            } else {
                if(nextHops[i] == Integer.MAX_VALUE) {
                    System.out.println("|      "+(i+1)+"    |    "+"inf"+"    |      "+costs[i]+"    |");
                    
                } else {
                    System.out.println("|      "+(i+1)+"    |     "+nextHops[i]+"     |      "+costs[i]+"    |");
                    
                }
                
            }
            System.out.println("+-----------+-----------+-----------+");
        }
    }
    
    public static void disable(int disabledServer) {
        Server.update(myID, disabledServer, Integer.MAX_VALUE);
    }
    
    public static void crash() {
        for (int neighborid : edges.keySet()) {
            Server.update(myID, neighborid, Integer.MAX_VALUE);
        }
        
        /*
         * when this closes, its neighbouring servers' cost should be changed to
         * inf i.e. server 3 is connected to server 2 with cost 5 and to server
         * 1 with cost 4 sever 3 "crashes" so server 2's and server 1's cost
         * with 3 is now inf instead of cost 5 and 4 respectively.
         */
    }
    /*
     Number of update fields Server port
     Server IP
     Server IP address 1
     Server port 1 0x0
     Server ID 1 Cost 1
     Server IP address 2
     Server port 2 0x0
     Server ID 2 Cost 2
     */
    
    public static byte [] createDistanceVectorUpdatePacket() {
        
        String message = "";
        message += edges.size() + ",";
        message += neighbors.get(myID).port + ",";
        message += neighbors.get(myID).ip + ",";
        for(Integer neighborId : neighbors.keySet()){
            message += neighbors.get(neighborId).ip + ",";
            message += neighbors.get(neighborId).port + ",";
            message += neighborId + ",";
            message += edges.get(neighborId) + ",";
            
            
        }
        
        return message.getBytes();
        
    }
    
    public static void updateAll() {
        if(!isCrashed){
            for (int neighborId : neighbors.keySet()) {
                byte [] packet = createDistanceVectorUpdatePacket();
                try {
                    
                    send(neighborId, packet);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            
        } else {
            System.out.println("Cannot updateAll() after a crash!");
        }
    }
    
    // Allows any Client to send a message to another Client
    public void send(int id, String message) throws IOException {
        if(!isCrashed){
            Node client = neighbors.get(id);
            DatagramSocket datagramSocket = new DatagramSocket();
            byte[] messageBytes = message.getBytes();
            InetAddress recipient = InetAddress.getByName(client.ip);
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, recipient, Integer.parseInt(client.port));
            datagramSocket.send(packet);
            
        } else {
            System.out.println("Cannot send() after a crash!");
        }
    }
    
    public static void send(int id, byte [] messageBytes) throws IOException {
        // System.out.println("about to send message: " + new String(messageBytes) + " to client " + id);
        Node client = neighbors.get(id);
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress recipient = InetAddress.getByName(client.ip);
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, recipient,
                                                   Integer.parseInt(client.port));
        try {
            datagramSocket.send(packet);
            
        } catch(Exception e){
            System.out.println("Error sending to server " + id);
            e.printStackTrace();
        }
    }
}
