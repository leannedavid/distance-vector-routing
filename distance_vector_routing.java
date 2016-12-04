package project2;
import java.net.*;
import java.util.*;
import java.io.*;


public class distance_vector_routing {
    
    public static void main(String[] args) {
        boolean start = false;
        boolean checker = true;
        
        Scanner input = new Scanner(System.in);
        String beginning = args[0];
        String init = "";
        String dashTArg = args[1];
        String filename = args[2];
        Server.routingTables = new HashMap<>();
        Server.populateData(filename);
        int myPort = Integer.parseInt(Server.neighbors.get(Server.myID).port);
        
        String dashIArg = args[3];
        int routUpd = Integer.parseInt(args[4]);
        
        do {
            if (beginning.equals("server")) {
                try {
                    if (!dashTArg.equals("-t")) {
                        checker = false;
                    } else if (!dashIArg.equals("-i")) {
                        checker = false;
                    } else if (routUpd <= 0) {
                        checker = false;
                    }
                    
                    if (checker == true) {
                        start = true;
                    }
                } catch (Exception e) {
                    System.out.println(
                                       "Please enter command \"server -t <topology-file-name> -i <routing-update-interval>\" to begin.");
                }
            } else {
                System.out.println(
                                   "Please enter command \"server -t <topology-file-name> -i <routing-update-interval>\" to begin.");
            }
        } while (!start);
        
        Server server = new Server(myPort);
        String command = "";
        server.setupListeningSocket();
        
        Server.setupIntervalUpdate(routUpd);
        
        
        do {
            boolean success = false;
            
            System.out.print("\nProvide a command: ");
            
            Scanner secondInput = new Scanner(System.in);
            command = secondInput.nextLine();
            
            String[] values = command.split(" ");
            
            
            if (command.equals("help")) { // help method
                Server.help();
                success = true;
            } else if (command.contains("update")) { // updates method
                System.out.println("value is length: " + values.length + " for string '" + command + "'");
                if(values.length < 4) {
                    System.out.println("Enter the right amount of arguments");
                }
                else {
                    try {
                        int cost = 0;
                        int server1 = Integer.parseInt(values[1]);
                        int server2 = Integer.parseInt(values[2]);
                        if(values[3].equals("inf") ){
                            cost = Integer.MAX_VALUE;
                        } else{
                            cost = Integer.parseInt(values[3]); // can be inf
                        }
                        Server.update(server1, server2, cost);
                        success = true;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (command.equals("step")) { // step method
                Server.updateAll();
                Server.numOfPacks++;
                success = true;
            } else if (command.equals("packets")) { // packets method
                System.out.println("You have received " + Server.numOfPacks + " packets since the last update.");
                Server.numOfPacks = 0;
                success = true;
            } else if (command.equals("display")) {
                Server.display();
                success = true;
            } else if (command.contains("disable")) { // disable method
                if(values.length < 2) {
                    System.out.println("Enter the right amount of arguments");
                }
                else {
                    int disabledServer = Integer.parseInt(values[1]);
                    Server.disable(disabledServer);
                    success = true;
                }
                
            } else if (command.contains("crash")) { // crash method
                for(int neigh : Server.edges.keySet() ){
                    Server.edges.put(neigh, Integer.MAX_VALUE);
                }
                Server.updateAll();
                
                Server.crash();
                Server.isCrashed = true;
                success = true;
            } else if (command.contains("exit")) { // exit method
                Server.crash();
                success = true;
                System.out.println("Program now exiting...");
                
            } else {
                System.out.println("Command '" + values[0] + "' not recognized!");
            }
            
            if(success == false){
                System.out.println(values[0] + ": FAIL!");
            }
            else{
                System.out.println(values[0] + ": SUCCESS!");
            }
            
            
        } while (!command.equals("exit"));
        if (command.equals("exit")) { // exit method
            System.exit(0);
        }
    }
}
