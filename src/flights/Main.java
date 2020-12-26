package flights;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {


    static List<Flight> flightList = new ArrayList<>();
    static Graph graph = new Graph();

    public static void main(String[] args) {
        System.out.println("hello world");
        readCityFlightfile("inputPS7.txt");
        System.out.println("flights are " + flightList);
        createGraph();
        System.out.println("graph is " + graph);
        findServiceAvailable("Ahmedabad","Mumbai");
        displayDirectFlight("Ahmedabad","Mumbai");
    }

    public static void createGraph() {
        for (Flight flight : flightList) {
            GraphNode from;
            GraphNode to;
            if (!graph.containsCity(flight.fromCity)) {
                from = graph.addGraphNode(flight.fromCity);
            }

            if (!graph.containsCity(flight.toCity)) {
                to = graph.addGraphNode(flight.toCity);
            }
            graph.addConnection(flight.fromCity, flight.toCity, flight.flightNo);

        }
    }

    public static void readCityFlightfile(String filename) {
        BufferedReader reader;
        try {
            System.out.println("path is " + System.getProperty("user.dir"));
            reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + "\\" + filename));
            String line = reader.readLine();
            while (line != null) {
                Flight f = parseLineToFlight(line);
                if (f != null) {
                    flightList.add(f);
                }
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Flight parseLineToFlight(String str) {
        if (null == str) {
            return null;
        }
        String[] parts = str.split("/");
//        System.out.println(parts);
        if (parts.length != 3) {
            return null;
        }
        Flight f = new Flight(parts[0].trim(), parts[1].trim(), parts[2].trim());
        return f;
    }

    public static void displayDirectFlight(String fromCity, String toCity) {
        if (!graph.containsCity(fromCity) || !graph.containsCity(toCity)) {
            System.out.println("City codes not found");
        }
        ArrayList<GraphConnection> path = graph.hasDirectConnection(fromCity, toCity);
        if (path != null|| path.size()==0) {

            System.out.println("Direct flight found");
            StringBuilder sbr = new StringBuilder();

                sbr.append("FlightNo. ").append(path.get(0).flightNo);
            System.out.println(sbr.toString());
        } else {
            System.out.println("No direct flight found");
        }

    }

    public static void findServiceAvailable(String fromCity, String toCity) {
        if (!graph.containsCity(fromCity) || !graph.containsCity(toCity)) {
            System.out.println("City codes not found");
        }
        ArrayList<GraphConnection> path = graph.hasConnection(fromCity, toCity);
        if (path != null) {

            System.out.println("Direct flight found");
            StringBuilder sbr = new StringBuilder();
            sbr.append(fromCity);
            for (GraphConnection con : path) {
                sbr.append(">").append(con.flightNo);
                sbr.append(">").append(con.toCity.fromCity);
            }
            System.out.println(sbr.toString());
        } else {
            System.out.println("No direct flight found");
        }
    }
}


class GraphNode {
    String fromCity;
    List<GraphConnection> connections;
    Map<String,GraphConnection> connectionMapByFlight;

    public GraphNode(String fromCity) {
        this.fromCity = fromCity;
        connections=new ArrayList<>();
        connectionMapByFlight= new HashMap<>();
    }

    public void addConnection(GraphNode toNode, String flight){
        GraphConnection c = new GraphConnection(flight, toNode);
        connections.add(c);
        connectionMapByFlight.put(flight, c);
    }

    @Override
    public String toString() {
        return "GraphNode{" +
                "fromCity='" + fromCity + '\'' +
                ", connections=" + connections +"}\n";
    }
}


class GraphConnection {
    String flightNo;
    GraphNode toCity;

    public GraphConnection(String flightNo, GraphNode toCity) {
        this.flightNo = flightNo;
        this.toCity = toCity;
    }

    @Override
    public String toString() {
        return "GraphConnection{" +
                "flightNo='" + flightNo + '\'' +
                ", toCity=" + toCity.fromCity +
                '}';
    }
}

class Graph{
    List<GraphNode> nodes;
    Map<String,GraphNode> cityNodeMap;

    public Graph() {
        nodes=new ArrayList<>();
        cityNodeMap=new HashMap<>();
    }

    public boolean containsCity(String city){
        return cityNodeMap.containsKey(city);
    }

    public GraphNode addGraphNode( String city){
        GraphNode g = new GraphNode(city);
        nodes.add(g);
        cityNodeMap.put(g.fromCity,g);
        return g;
    }

    public GraphNode getNodeByCity(String fromCity) {
        return cityNodeMap.get(fromCity);
    }

    public void addConnection( String from, String to, String flight){
        GraphNode fromNode = cityNodeMap.get(from);
        GraphNode  toNode = cityNodeMap.get(to);
        fromNode.addConnection( toNode, flight);
    }

    @Override
    public String toString() {
        return "Graph{" +
                "nodes=" + nodes +
                '}';
    }


    public ArrayList<GraphConnection> hasConnection(String from, String to){
        GraphNode fromN = cityNodeMap.get(from);
        GraphNode toN = cityNodeMap.get(to);
        ArrayList<ArrayList<GraphConnection>> allPaths = new ArrayList<ArrayList<GraphConnection>>();
        ArrayList<GraphConnection> currentPath = new ArrayList<GraphConnection>();
        Set<GraphNode> visitedNodes = new HashSet<>();
        boolean hasPath =  traversals(fromN,toN, currentPath, visitedNodes, false, null);
        if(hasPath){
            return currentPath;
        }
        return null;
    }

    public  ArrayList<GraphConnection> hasDirectConnection(String from , String to){

        GraphNode fromN = cityNodeMap.get(from);
        GraphNode toN = cityNodeMap.get(to);

        for (GraphConnection con: fromN.connections){

            ArrayList<GraphConnection> currentPath = new ArrayList<GraphConnection>();
            Set<GraphNode> visitedNodes = new HashSet<>();
            boolean hasPath = traversals(fromN, toN, currentPath, visitedNodes, true, con.flightNo);
            if(hasPath){
                return currentPath;
            }
        }
        return null;
    }

    private boolean traversals(GraphNode fromN, GraphNode toN, ArrayList<GraphConnection> currentPath,  Set<GraphNode> visitedNodes, boolean strictFlight, String flightName) {
        if(visitedNodes.contains(fromN)){
            return false;
        }
        if (fromN.fromCity == toN.fromCity){
            return true;
        }
        visitedNodes.add(fromN);
        for ( GraphConnection connection : fromN.connections){
            if(strictFlight){
                if (!connection.flightNo.equals(flightName)){
                    continue;
                }
            }
            currentPath.add(connection);
            boolean hasPath = traversals(connection.toCity, toN, currentPath,  visitedNodes ,strictFlight, flightName);
            if(hasPath){
                return hasPath;
            }
            currentPath.remove(connection);

        }
        return false;
    }


}

class Flight {
    String flightNo;
    String fromCity;
    String toCity;

    public Flight(String flightNo, String fromCity, String toCity) {
        this.flightNo = flightNo;
        this.fromCity = fromCity;
        this.toCity = toCity;
    }

    public String getFlightNo() {
        return flightNo;
    }

    public void setFlightNo(String flightNo) {
        this.flightNo = flightNo;
    }

    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "flightNo='" + flightNo + '\'' +
                ", fromCity='" + fromCity + '\'' +
                ", toCity='" + toCity + '\'' +
                "}\n";
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }
}
