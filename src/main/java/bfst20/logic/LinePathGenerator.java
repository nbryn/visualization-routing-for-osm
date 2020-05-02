package bfst20.logic;

import java.util.*;

import bfst20.logic.entities.Node;
import bfst20.logic.entities.Relation;
import bfst20.logic.entities.Way;
import bfst20.logic.entities.LinePath;
import bfst20.logic.misc.OSMType;


public class LinePathGenerator {
    private static LinePathGenerator linePathGenerator;
    private static boolean loaded = false;
    private AppController appController;
    private List<Relation> OSMRelations;
    private Map<Long, Node> OSMNodes;
    private List<Way> OSMWays;

    private LinePathGenerator() {
        appController = new AppController();
        OSMWays = appController.getAllWayData();
        OSMNodes = appController.fetchAllNodes();
        OSMRelations = appController.fetchRelations();
    }

    public void clearData() {
        OSMNodes = new HashMap<>();
        OSMWays = new ArrayList<>();
        OSMRelations = new ArrayList<>();

        loaded = false;
        System.gc();
    }

    public static LinePathGenerator getInstance() {
        if (!loaded) {
            loaded = true;
            linePathGenerator = new LinePathGenerator();
        }

        return linePathGenerator;
    }

    public void createLinePaths() {
        createWays();

        createRelations();
    }

    private void createWays() {
        for (Way way : OSMWays) {
            if (way.getOSMType() == OSMType.COASTLINE || way.getOSMType() == null) continue;

            LinePath linePath = createLinePath(way);

            OSMType type = linePath.getOSMType();

            if (!appController.fetchLinePathData().containsKey(type)) {
                appController.saveData(type);
            }

            if (type != OSMType.PLACE) {
                appController.saveData(type, linePath);

            }
        }
    }

    private void createRelations() {
        for (Relation relation : OSMRelations) {

            if (relation.getOSMType() == OSMType.FOREST) {

                connectWays(relation, OSMType.FOREST);

            } else if (relation.getOSMType() == OSMType.FARMLAND) connectWays(relation, OSMType.FARMLAND);


             else if (relation.getName() != null && relation.getName().startsWith("Region ")) {
                if (!appController.fetchLinePathData().containsKey(OSMType.COASTLINE)) {
                    appController.saveData(OSMType.COASTLINE);
                }
                connectWays(relation, OSMType.COASTLINE);
            } else if (relation.getOSMType() == OSMType.BUILDING) connectMultipolygon(relation, OSMType.BUILDING);
                //connectWays(relation, OSMType.BUILDING);

            else if (relation.getOSMType() == OSMType.MEADOW) connectMultipolygon(relation, OSMType.MEADOW);
                //connectWays(relation, OSMType.BUILDING);

            else if (relation.getOSMType() == OSMType.HEATH) connectMultipolygon(relation, OSMType.HEATH);
            //connectWays(relation, OSMType.BUILDING);


        }

        if (appController.getNodeTo(OSMType.HEATH) != null) {
            addRelation(OSMType.HEATH, appController.getNodeTo(OSMType.HEATH));
        }
        if (appController.getNodeTo(OSMType.MEADOW) != null) {
            addRelation(OSMType.MEADOW, appController.getNodeTo(OSMType.MEADOW));
        }
        if (appController.getNodeTo(OSMType.BUILDING) != null) {
            addRelation(OSMType.BUILDING, appController.getNodeTo(OSMType.BUILDING));
        }
        if (appController.getNodeTo(OSMType.FOREST) != null) {
            addRelation(OSMType.FOREST, appController.getNodeTo(OSMType.FOREST));
        }
        if (appController.getNodeTo(OSMType.FARMLAND) != null) {
            addRelation(OSMType.FARMLAND, appController.getNodeTo(OSMType.FARMLAND));
        }
        if (appController.getNodeTo(OSMType.COASTLINE) != null) {
            addRelation(OSMType.COASTLINE, appController.getNodeTo(OSMType.COASTLINE));
        }

    }

    private void addRelation(OSMType OSMType, Map<Node, Way> nodeTo) {
        for (Map.Entry<Node, Way> entry : nodeTo.entrySet()) {
            if (entry.getKey() == OSMNodes.get(entry.getValue().getLastNodeId())) {

                LinePath path = new LinePath(entry.getValue(), OSMType, OSMNodes, true);

                if (entry.getValue().isMultipolygon()) {
                    path.setMultipolygon(true);
                }

                appController.saveData(OSMType, path);
            }
        }
    }

    private void connectMultipolygon(Relation relation, OSMType osmType) {
        if (!relation.isMultipolygon()) return;
        Collections.sort(relation.getMembers());

        Way way = null;

        for (long entry : relation.getMembers()) {

            if (way == null) {
                way = (binarySearch(OSMWays, entry));
            } else {
                Way newWay = (binarySearch(OSMWays, entry));
                way = combine(way, newWay);
            }
        }

        way.setMultipolygon(true);

        appController.saveData(osmType, OSMNodes.get(way.getFirstNodeId()), way);
        appController.saveData(osmType, OSMNodes.get(way.getLastNodeId()), way);
    }

    private void connectWays(Relation relation, OSMType OSMType) {
        Collections.sort(relation.getMembers());

        for (long entry : relation.getMembers()) {

            Way way = (binarySearch(OSMWays, entry));
            if (way == null) continue;

            if (relation.isMultipolygon()) {
                way.setMultipolygon(true);
            }

            Way before = removeWayBefore(way, OSMType);
            Way after = removeWayAfter(way, OSMType);

            way = merge(merge(before, way), after);

            appController.saveData(OSMType, OSMNodes.get(way.getFirstNodeId()), way);
            appController.saveData(OSMType, OSMNodes.get(way.getLastNodeId()), way);
        }
    }

    //TODO: Naming/Explanation
    private Way removeWayAfter(Way way, OSMType OSMType) {
        Node node = OSMNodes.get(way.getLastNodeId());
        return getWay(OSMType, node);
    }
    //TODO: Naming/Explanation
    private Way removeWayBefore(Way way, OSMType OSMType) {
        Node node = OSMNodes.get(way.getFirstNodeId());
        return getWay(OSMType, node);
    }
    //TODO: Naming/Explanation
    private Way getWay(OSMType OSMType, Node node) {
        Way way = appController.removeWayFromNodeTo(OSMType, node);
        if (way != null) {
            Node firstNode = OSMNodes.get(way.getFirstNodeId());
            Node lastNode = OSMNodes.get(way.getLastNodeId());
            appController.removeWayFromNodeTo(OSMType, firstNode);
            appController.removeWayFromNodeTo(OSMType, lastNode);

        }

        return way;
    }



    private LinePath createLinePath(Way way) {
        OSMType type = OSMType.UNKNOWN;

        try {
            type = way.getOSMType();
        } catch (Exception e) {
            //This catch is here to check if the current way type exists in the Type enum, if it does, that will be used,
            //If it dosen't this will throw, and the program will use Type.UNKNOWN
        }
        Boolean fill = OSMType.getFill(type);

        // TODO: Does every LinePath need all nodes?
        return new LinePath(way, type, OSMNodes, fill);
    }
    //TODO: Naming/Explanation
    private Way combine(Way before, Way after) {
        if (before == null) return after;
        if (after == null) return before;

        Way way = new Way();
        way.addAllNodeIds(before);
        //way.addNodeId(-99999);
        //way.addNodeId(-99999);
        way.addAllNodeIds(after);

        return way;
    }
    //TODO: Naming/Explanation
    private Way merge(Way before, Way after) {
        if (before == null) return after;
        if (after == null) return before;

        Way way = new Way();
        // Why do we need this? Seems to do the same without it
        if (before.getFirstNodeId() == after.getFirstNodeId()) {
            way.addAllNodeIds(before);

            Collections.reverse(way.getNodeIds());
            way.getNodeIds().remove(way.getNodeIds().size() - 1);
            way.addAllNodeIds(after);

        } else if (before.getFirstNodeId() == after.getLastNodeId()) {

            addWayToMerge(way, after, before);

        } else if (before.getLastNodeId() == after.getFirstNodeId()) {

            addWayToMerge(way, before, after);
        }

        // Why do we need this? Seems to do the same without it
        else if (before.getLastNodeId() == after.getLastNodeId()) {
            Way tmp = new Way(after);

            Collections.reverse(tmp.getNodeIds());
            way.addAllNodeIds(before);
            way.getNodeIds().remove(way.getNodeIds().size() - 1);
            way.addAllNodeIds(tmp);
        } else {
            throw new IllegalArgumentException("Cannot merge unconnected OSMWays");
        }

        return way;
    }

    //Order of before and after depends on the context
    private void addWayToMerge(Way way, Way before, Way after) {
        way.addAllNodeIds(before);
        way.getNodeIds().remove(way.getNodeIds().size() - 1);
        way.addAllNodeIds(after);
    }

    private Way binarySearch(List<Way> list, long id) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            Way midElement = list.get(mid);
            long midId = midElement.getId();

            if (midId < id) {
                low = mid + 1;
            } else if (midId > id) {
                high = mid - 1;
            } else {
                return midElement;
            }
        }
        return null;
    }
}