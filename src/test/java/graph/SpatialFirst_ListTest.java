package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.gis.spatial.rtree.RTreeRelationshipTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import commons.Config;
import commons.Entity;
import commons.Enums;
import commons.Labels.OSMRelation;
import commons.MyRectangle;
import commons.OwnMethods;
import commons.Query_Graph;
import commons.RTreeUtility;
import commons.Util;

public class SpatialFirst_ListTest {

  static Config config = new Config();
  static String dataset = config.getDatasetName();
  static String version = config.GetNeo4jVersion();
  static Enums.system systemName = config.getSystemName();
  static int MAX_HOPNUM = config.getMaxHopNum();

  static String db_path, entityPath, graph_pos_map_path;
  static String querygraphDir, spaPredicateDir;
  static String log_path;

  // query input
  static Query_Graph query_Graph;
  static long[] graph_pos_map_list;

  static int nodeCount = 10, query_id = 0, rectID = 2;

  // static int name_suffix = 1280;//Gowalla 0.001
  static int name_suffix = 5756;// wikidata_100 0.001
  static String queryrect_path = null, querygraph_path = null, queryrectCenterPath = null;

  @Before
  public void setUp() throws Exception {
    switch (systemName) {
      case Ubuntu:
        db_path = String.format(
            "/home/yuhansun/Documents/GeoGraphMatchData/%s_%s/data/databases/graph.db", version,
            dataset);
        entityPath = String.format("/mnt/hgfs/Ubuntu_shared/GeoMinHop/data/%s/entity.txt", dataset);
        graph_pos_map_path =
            "/mnt/hgfs/Ubuntu_shared/GeoMinHop/data/" + dataset + "/node_map_RTree.txt";
        querygraphDir =
            String.format("/mnt/hgfs/Google_Drive/Projects/risotree/query/query_graph/%s", dataset);
        spaPredicateDir = String
            .format("/mnt/hgfs/Google_Drive/Projects/risotree/query/spa_predicate/%s", dataset);
        querygraph_path = String.format("%s/%d.txt", querygraphDir, nodeCount);
        queryrectCenterPath = String.format("%s/%s_centerids.txt", spaPredicateDir, dataset);
        queryrect_path = String.format("%s/queryrect_%d.txt", spaPredicateDir, name_suffix);
        break;
      case Windows:
        String dataDirectory = "D:\\Ubuntu_shared\\GeoMinHop\\data";
        db_path = String.format("%s\\%s\\%s_%s\\data\\databases\\graph.db", dataDirectory, dataset,
            version, dataset);
        entityPath = String.format("D:\\Ubuntu_shared\\GeoMinHop\\data\\%s\\entity.txt", dataset);
        graph_pos_map_path =
            "D:\\Ubuntu_shared\\GeoMinHop\\data\\" + dataset + "\\node_map_RTree.txt";
        querygraphDir =
            String.format("D:\\Google_Drive\\Projects\\risotree\\query\\query_graph\\%s", dataset);
        spaPredicateDir = String
            .format("D:\\Google_Drive\\Projects\\risotree\\query\\spa_predicate\\%s", dataset);
        querygraph_path = String.format("%s\\%d.txt", querygraphDir, nodeCount);
        queryrectCenterPath = String.format("%s\\%s_centerids.txt", spaPredicateDir, dataset);
        queryrect_path = String.format("%s\\queryrect_%d.txt", spaPredicateDir, name_suffix);
      default:
        break;
    }
    iniQueryInput();
  }

  @After
  public void tearDown() throws Exception {}

  public void iniQueryInput() {
    ArrayList<Query_Graph> queryGraphs = Util.ReadQueryGraph_Spa(querygraph_path, query_id + 1);
    query_Graph = queryGraphs.get(query_id);

    ArrayList<MyRectangle> queryrect = OwnMethods.ReadQueryRectangle(queryrect_path);

    // ArrayList<Integer> centerIDs = OwnMethods.readIntegerArray(queryrectCenterPath);
    // ArrayList<Entity> entities = OwnMethods.ReadEntity(entityPath);
    // ArrayList<MyRectangle> queryrect = new ArrayList<MyRectangle>();
    // for ( int id : centerIDs)
    // {
    // Entity entity = entities.get(id);
    // queryrect.add(new MyRectangle(entity.lon, entity.lat, entity.lon, entity.lat));
    // }

    MyRectangle rectangle = queryrect.get(rectID);
    Util.println("query rectangle: " + rectangle);
    int j = 0;
    for (; j < query_Graph.graph.size(); j++)
      if (query_Graph.Has_Spa_Predicate[j])
        break;
    query_Graph.spa_predicate[j] = rectangle;

    int entityCount = OwnMethods.getEntityCount(entityPath);
    Util.println("read map from " + graph_pos_map_path);
    graph_pos_map_list = OwnMethods.ReadMap(graph_pos_map_path, entityCount);

  }

  @Test
  public void subgraphMatchQuery_Block_Test() {
    SpatialFirst_List spatialFirstlist =
        new SpatialFirst_List(db_path, dataset, graph_pos_map_list);

    spatialFirstlist.query_Block(query_Graph, -1);
    Util.println(String.format("result size: %d", spatialFirstlist.result_count));
    spatialFirstlist.shutdown();
  }

  @Test
  public void subgraphMatchQueryTest() {
    SpatialFirst_List spatialFirstlist =
        new SpatialFirst_List(db_path, dataset, graph_pos_map_list);

    spatialFirstlist.query(query_Graph, -1);
    Util.println(String.format("result size: %d", spatialFirstlist.result_count));
    spatialFirstlist.shutdown();
  }

  @Test
  public void rangeQueryTest() {
    try {
      SpatialFirst_List spatialFirstlist =
          new SpatialFirst_List(db_path, dataset, graph_pos_map_list);
      Transaction tx = spatialFirstlist.dbservice.beginTx();
      Node rootNode = RTreeUtility.getRTreeRoot(spatialFirstlist.dbservice, dataset);
      MyRectangle query_rectangle = new MyRectangle(38.090088, 36.413699, 41.962690, 40.286301);
      LinkedList<Node> result = spatialFirstlist.rangeQuery(rootNode, query_rectangle);
      Util.println(String.format("Result size: %d", result.size()));
      tx.success();
      tx.close();
      spatialFirstlist.dbservice.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void formSubgraphQueryTest() throws Exception {
    try {
      SpatialFirst_List spatialFirstlist =
          new SpatialFirst_List(db_path, dataset, graph_pos_map_list);

      HashMap<Integer, MyRectangle> spa_predicates = new HashMap<Integer, MyRectangle>();
      // spa_predicates.put(3, queryRectangle); //query id 3
      // query id 5 do nothing on spa_predicates

      int pos = 1;
      long id = 511901;

      HashMap<Integer, Integer> NL_hopnum = new HashMap<Integer, Integer>();
      // NL_hopnum.put(1, 1); NL_hopnum.put(2, 2); //query id 3
      // query id 5
      NL_hopnum.put(0, 1);
      // NL_hopnum.put(2, 2);


      Transaction tx = spatialFirstlist.dbservice.beginTx();

      Node node = spatialFirstlist.dbservice.getNodeById(id)
          .getSingleRelationship(OSMRelation.GEOM, Direction.OUTGOING).getEndNode()
          .getSingleRelationship(RTreeRelationshipTypes.RTREE_REFERENCE, Direction.INCOMING)
          .getStartNode();

      String query = spatialFirstlist.formSubgraphQuery(query_Graph, -1, Enums.Explain_Or_Profile.Profile,
          spa_predicates, pos, id, NL_hopnum, node);
      Util.println(query);

      Result result = spatialFirstlist.dbservice.execute(query);
      // ExecutionPlanDescription planDescription = result.getExecutionPlanDescription();
      // OwnMethods.Print(planDescription);
      int count = 0;
      HashSet<Long> ida1_list = new HashSet<Long>();
      while (result.hasNext()) {
        Map<String, Object> row = result.next();
        long ida1 = (Long) row.get("id(a0)");
        ida1_list.add(ida1);
        count++;
      }
      Util.println(count);

      for (long ida1 : ida1_list)
        OwnMethods.WriteFile(log_path, true, ida1 + "\n");

      tx.success();
      tx.close();
      spatialFirstlist.dbservice.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void LAGAQ_KNNtest() {
    SpatialFirst_List spatialFirst_List =
        new SpatialFirst_List(db_path, dataset, graph_pos_map_list);
    int K = 5;
    try {
      ArrayList<Long> resultIDs = spatialFirst_List.LAGAQ_KNN(query_Graph, K);
      Util.println(resultIDs);
      Util.println(spatialFirst_List.visit_spatial_object_count);
      Util.println(spatialFirst_List.page_hit_count);
    } catch (Exception e) {
      e.printStackTrace();
      spatialFirst_List.dbservice.shutdown();
    } finally {
      spatialFirst_List.dbservice.shutdown();
    }
  }

  @Test
  public void spatialJoinRTreeTest() {
    SpatialFirst_List spatialFirst_List =
        new SpatialFirst_List(db_path, dataset, graph_pos_map_list);
    try {
      double distance = 0.01;
      Util.println(distance);
      OwnMethods.ClearCache("syh19910205");
      // FileWriter writer = new FileWriter("D:\\temp\\output1.txt");
      long start = System.currentTimeMillis();
      List<Long[]> result = spatialFirst_List.spatialJoinRTree(distance, null);
      Util.println(System.currentTimeMillis() - start);
      Util.println(result.size());
      spatialFirst_List.shutdown();
      // for (Long[] element : result)
      // {
      // writer.write(String.format("%d,%d\n", element[0], element[1]));
      // }
      // writer.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  @Test
  public void spatialJoinRTreeValidate() {
    Util.println(entityPath);
    ArrayList<Entity> entities = OwnMethods.ReadEntity(entityPath);
    // STRtree stRtree = OwnMethods.ConstructSTRee(entities);
    double distance = 0.1;

    int resolution = 1000;
    int size = 1;

    HashMap<Integer, LinkedList<Integer>> gridMap = new HashMap<>();
    for (int i = 0; i < resolution * resolution; i++)
      gridMap.put(i, new LinkedList<>());

    for (int i = 0; i < entities.size(); i++) {
      Entity entity = entities.get(i);
      if (entity.IsSpatial) {
        int x = (int) (entity.lon / size);
        int y = (int) (entity.lat / size);
        x = (x == 1000 ? x - 1 : x);
        y = (y == 1000 ? y - 1 : y);
        int id = y * 1000 + x;
        gridMap.get(id).add(i);
      }
    }

    ArrayList<Integer[]> result = new ArrayList<>();

    ArrayList<Integer> offsets = new ArrayList<>();
    offsets.add(-1);
    offsets.add(0);
    offsets.add(1);
    offsets.add(-1000);
    offsets.add(-999);
    offsets.add(-1001);
    offsets.add(1000);
    offsets.add(999);
    offsets.add(1001);
    for (int i = 0; i < resolution * resolution; i++) {
      Util.println(i);
      for (int offset : offsets) {
        if (gridMap.containsKey(i + offset)) {
          for (int id1 : gridMap.get(i)) {
            for (int id2 : gridMap.get(i + offset)) {
              Entity entity1 = entities.get(id1);
              Entity entity2 = entities.get(id2);
              if (Util.distance(entity1.lon, entity1.lat, entity2.lon, entity2.lat) <= distance
                  && entity1.id != entity2.id) {
                Integer[] pair = new Integer[2];
                pair[0] = id1;
                pair[1] = id2;
                result.add(pair);
              }
            }
          }
        }
      }
    }


    // for ( Entity entity : entities)
    // {
    // OwnMethods.Print(entity.id);
    // if ( entity.IsSpatial)
    // {
    // for (Entity entity2 : entities)
    // {
    // if (entity2.IsSpatial)
    // {
    // if (Utility.distance(entity.lon, entity.lat,
    // entity2.lon, entity2.lat) <= distance)
    // {
    // Integer[] pair = new Integer[2];
    // if ( entity.id != entity2.id)
    // {
    // pair[0] = entity.id;
    // pair[1] = entity2.id;
    // result.add(pair);
    // }
    // }
    // }
    // }
    // }
    // }
    Util.println(result.size());

  }

  @Test
  public void LAGAQ_JoinTest() {
    // query_Graph = new Query_Graph(3);
    // query_Graph.label_list[0] = 3;
    // query_Graph.label_list[1] = 1;
    // query_Graph.label_list[2] = 1;
    //
    // query_Graph.graph.get(0).add(1);
    // query_Graph.graph.get(0).add(2);
    // query_Graph.graph.get(1).add(0);
    // query_Graph.graph.get(2).add(0);
    //
    // query_Graph.Has_Spa_Predicate[1] = true;
    // query_Graph.Has_Spa_Predicate[2] = true;

    OwnMethods.ClearCache("syh19910205");

    OwnMethods.convertQueryGraphForJoinRandom(query_Graph);
    Util.println(query_Graph.toString());

    SpatialFirst_List spatialFirst_List =
        new SpatialFirst_List(db_path, dataset, graph_pos_map_list);
    long start = System.currentTimeMillis();
    List<Long[]> result = spatialFirst_List.LAGAQ_Join(query_Graph, 0.01);
    Util.println(String.format("Total time: %d", System.currentTimeMillis() - start));

    Util.println("Join time: " + spatialFirst_List.join_time);
    Util.println("Get iterate time: " + spatialFirst_List.get_iterator_time);
    Util.println("Iterate time: " + spatialFirst_List.iterate_time);
    Util.println("Join result count: " + spatialFirst_List.join_result_count);
    Util.println(result);
    Util.println(result.size());

    spatialFirst_List.shutdown();
  }
}
