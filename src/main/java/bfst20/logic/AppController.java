package bfst20.logic;

import java.io.*;
import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bfst20.data.*;
import bfst20.logic.entities.*;
import bfst20.logic.kdtree.KDTree;
import bfst20.logic.kdtree.Rect;
import bfst20.logic.entities.LinePath;
import bfst20.logic.misc.OSMType;
import bfst20.logic.misc.Vehicle;
import bfst20.logic.routing.Edge;
import bfst20.logic.routing.Graph;
import bfst20.logic.routing.RoutingController;
import bfst20.logic.ternary.TST;
import bfst20.presentation.View;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

import javax.xml.stream.XMLStreamException;

public class AppController {

    private RoutingController routingController;
    private LinePathGenerator linePathGenerator;
    private OSMElementData osmElementData;
    private LinePathData linePathData;
    private boolean isBinary = false;
    private RoutingData routingData;
    private AddressData addressData;
    private KDTreeData kdTreeData;
    private Parser parser;
    private View view;

    public AppController() {
        osmElementData = OSMElementData.getInstance();
        linePathData = LinePathData.getInstance();
        routingData = RoutingData.getInstance();
        addressData = AddressData.getInstance();
        kdTreeData = KDTreeData.getInstance();
        parser = Parser.getInstance();

    }

    public View initialize() throws IOException {
        routingController = new RoutingController(new AppController());

        if (!isBinary) {
            System.out.println("Creating linepaths");
            createLinePaths();
            clearNodeData();
            System.out.println("Generate Highways");
            generateHighways();
            System.out.println("Building graph");

            routingController.buildRoutingGraph();

        }
        System.out.println("Done");
        view.initialize(isBinary);
        System.gc();

        return view;
    }

    public void setSearchString(Address address) {
        view.setSearchString(address);
    }

    public void createLinePaths() {
        linePathGenerator = LinePathGenerator.getInstance();
        linePathGenerator.createLinePaths();
    }

    public void generateHighways() {
        Map<OSMType, List<LinePath>> linePaths = linePathData.getLinePaths();
        List<LinePath> highWays = new ArrayList<>();

        if (linePathData.getMotorways() != null) highWays.addAll(linePathData.getMotorways());
        for (Map.Entry<OSMType, List<LinePath>> entry : linePaths.entrySet()) {
            highWays.addAll(entry.getValue());
        }

        linePathData.saveHighways(highWays);
    }

    public Map<OSMType, KDTree> fetchAllKDTreeData() {
        return kdTreeData.getAllLKDTrees();
    }

    public void saveAllKDTrees(Map<OSMType, KDTree> tree) {
        kdTreeData.saveAllKDTrees(tree);
    }

    public List<LinePath> fetchHighwayData() {
        return linePathData.getHighways();
    }


    public double initializeRouting(String sourceQuery, String targetQuery, Vehicle vehicle) {
        routingController = new RoutingController(new AppController());

        Address source = addressData.findAddress(sourceQuery);
        Address target = addressData.findAddress(targetQuery);


        Graph graph = fetchGraphData();
        List<Edge> edges = graph.getEdges();

        return routingController.calculateShortestRoute(graph, edges, source, target, vehicle);
    }

    public Graph fetchGraphData() {
        return routingData.getGraph();
    }

    public void saveGraphData(Graph graph) {
        routingData.saveGraph(graph);
    }

    public List<Edge> fetchRouteData() {
        return routingData.getRoute();
    }

    public void saveRouteData(List<Edge> route) {
        routingData.saveRoute(route);
    }

    public void saveRouteInfo(Map<String, Double> routeInfo) {
        routingData.saveRouteInfo(routeInfo);
    }

    public Map<String, Double> fetchRouteInfoData() {
        return routingData.getRouteInfo();
    }

    public void clearRouteInfoData() {
        routingData.clearData();
    }

    public void loadFile(File file) {
        try {
            FileHandler fileHandler = FileHandler.getInstance();
            if (file.getName().endsWith(".bin")) isBinary = true;
            fileHandler.load(file);
        } catch (IOException ioException) {
            alertOK(Alert.AlertType.ERROR, "Invalid xml data, exiting.", true);
            System.exit(1);
        } catch (XMLStreamException xmlStreamException) {
            alertOK(Alert.AlertType.ERROR, "Invalid xml data, exiting.", true);
            System.exit(1);
        }
    }

    public void parseOSM(File file) throws IOException, XMLStreamException {
        parser.parseOSMFile(file);
    }

    public void parseString(String string) throws XMLStreamException {
        parser.parseString(string);
    }

    public void saveAddressData(long id, Address address) {
        addressData.saveAddress(id, address);
    }

    public void saveRelationData(Relation relation) {
        osmElementData.saveRelation(relation);
    }

    public List<Relation> fetchRelations() {
        return osmElementData.getRelations();
    }

    public void saveBoundsData(Bounds bounds) {
        osmElementData.saveBounds(bounds);
    }

    public Bounds fetchBoundsData() {
        return osmElementData.getBounds();
    }

    public void saveNodeData(long id, Node node) {
        osmElementData.addToNodeMap(id, node);
    }

    public void saveWayData(Way way) {
        osmElementData.saveWay(way);
    }

    public List<Way> getAllWayData() {
        return osmElementData.getWays();
    }

    public Node getNodeData(long id) {
        return osmElementData.getNode(id);
    }

    public Map<Long, Node> fetchAllNodes() {
        return osmElementData.getNodes();
    }

    public List<LinePath> fetchCoastlines() {
        return linePathData.getCoastlines();
    }

    public void saveCoastlines(List<LinePath> paths) {
        linePathData.saveCoastlines(paths);
    }

    public void clearNodeData() {
        OSMElementData.getInstance().clearNodeData();
    }

    public Map<OSMType, List<LinePath>> fetchLinePathData() {
        return linePathData.getLinePaths();
    }


    public Way removeWayFromNodeTo(OSMType type, Node node) {
        return linePathData.removeWayFromNodeTo(type, node);
    }

    public void saveNodeToData(OSMType type, Node node, Way way) {
        linePathData.addNodeTo(type, node, way);
    }

    public Map<Node, Way> getNodeTo(OSMType type) {
        return linePathData.getNodeTo(type);
    }

    public void saveLinePathData(OSMType type, LinePath linePath) {
        if (type == OSMType.COASTLINE) linePathData.saveSingleCoastLine(linePath);

        else linePathData.saveLinePath(type, linePath);
    }

    public void saveData(OSMType type) {
        linePathData.addType(type);
    }

    public List<LinePath> fetchMotorways() {
        return linePathData.getMotorways();
    }

    public void clearLinePathData() {
        LinePathGenerator.getInstance().clearData();
        linePathData.clearData();
    }

    public void setupRect() {
        Bounds bounds = fetchBoundsData();

        kdTreeData.saveRectValues(bounds.getMinLat(), bounds.getMaxLat(), bounds.getMinLon(), bounds.getMaxLon());
    }

    public Rect fetchRectData() {
        return kdTreeData.getRect();
    }

    public void saveKDTree(OSMType type, List<LinePath> linePaths) {
        if (type == OSMType.COASTLINE) return;
        kdTreeData.saveKDTree(type, new KDTree(linePaths, fetchRectData()));
    }

    public KDTree fetchKDTree(OSMType OSMType) {
        return kdTreeData.getKDTree(OSMType);
    }

    public void createView(Canvas canvas, Label mouseLocationLabel) {
        view = new View(canvas);
        view.setMouseLocationView(mouseLocationLabel);
    }

    public void alertOK(Alert.AlertType type, String text, boolean wait) {
        view.displayError(type, text, wait);
    }

    public void generateBinary() throws IOException {
        clearAllNonBinData();
        try {
            FileHandler fileHandler = FileHandler.getInstance();
            fileHandler.generateBinary();
        } catch (Exception e) {
            alertOK(Alert.AlertType.ERROR, "Error generating binary, please retry.", false);
        }
    }

    public void clearAllNonBinData() {
        clearNodeData();
        clearLinePathData();
        routingData.clearData();
    }

    public TST fetchTSTData() {
        return addressData.getTST();
    }

    public void saveTSTData(TST tst) {
        addressData.saveTST(tst);
    }

}