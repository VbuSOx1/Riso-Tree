package commons;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.relation.Relation;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;

import commons.Labels.OSMLabel;
import commons.Labels.OSMRelation;
import commons.Labels.RTreeRel;

public class RTreeUtility {
	
	/**
	 * Get the height of current node in RTree.
	 * Leaf level will be of height 1.
	 * Leaf node is the level above the real spatial vertex.
	 * @param node the given node
	 * @return height of the input node. 
	 * where <code>-1</code> means node is not in RTree;
	 * <code>1</code> means leaf node in RTree.
	 */
	public static int getHeight(Node node)
	{
		if (node.hasRelationship(RTreeRel.RTREE_CHILD, Direction.OUTGOING))
		{
			Node curNode = node;
			int level = 1;
			while(true)
			{
				Iterable<Relationship> rels = curNode.getRelationships(RTreeRel.RTREE_CHILD, Direction.OUTGOING);
				Iterator<Relationship> iterator = rels.iterator();
				if (iterator.hasNext())
				{
					level++;
					curNode = iterator.next().getEndNode();
				}
				else
					return level;
				
			}
		}
		else
		{
			if (node.hasRelationship(RTreeRel.RTREE_REFERENCE, Direction.OUTGOING))
				return 1;
			else return -1;
		}
		
	}
	
	/**
	 * Get the MBR of a given node.
	 * @param node
	 * @return
	 */
	public static MyRectangle getNodeMBR(Node node)
	{
		double[] bbox = (double[]) node.getProperty("bbox");
		MyRectangle myRectangle = new MyRectangle(bbox[0], bbox[1], bbox[2], bbox[3]);
		return myRectangle;
	}
	
	/**
	 * (:ReferenceNode{name:spatial_root}) -[:LAYER]-> (return{layer:layer_name}) -[:RTREE_ROOT]-> (return)
	 * @param databaseService
	 * @param layer_name
	 * @return	r-tree root node given layer_name
	 */
	public static Node getRTreeRoot(GraphDatabaseService databaseService, String layer_name)
	{
		try
		{
			ResourceIterator<Node> nodes = databaseService.findNodes(OSMLabel.ReferenceNode);
			while(nodes.hasNext())
			{
				Node head = nodes.next();
				Iterable<Relationship> relationships = head.getRelationships(RTreeRel.LAYER, Direction.OUTGOING);
				for ( Relationship relationship : relationships)
				{
					Node rtree_layer_node = relationship.getEndNode();
					if(rtree_layer_node.getProperty("layer").equals(layer_name))
					{
						Node rtree_root = rtree_layer_node.getSingleRelationship(RTreeRel.RTREE_ROOT, Direction.OUTGOING).getEndNode();
						return rtree_root;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		OwnMethods.Print("getRTreeRoot return null!");
		return null;
	}
	
	/**
	 * Get all geometries in RTree. It traverses to leaf node of the r-tree
	 * @param databaseService
	 * @param layer_name
	 * @return	all geometries for an r-tree layer name
	 */
	public static Iterable<Node> getAllGeometries(GraphDatabaseService databaseService, String layer_name) 
	{
		Node rtree_root_node = getRTreeRoot(databaseService, layer_name);
		TraversalDescription td = databaseService.traversalDescription()
				.depthFirst()
				.relationships( OSMRelation.USERS, Direction.OUTGOING )
				.relationships( RTreeRel.RTREE_CHILD, Direction.OUTGOING )
				.relationships( RTreeRel.RTREE_REFERENCE, Direction.OUTGOING )
				.evaluator( Evaluators.includeWhereLastRelationshipTypeIs( RTreeRel.RTREE_REFERENCE ) );
		return td.traverse( rtree_root_node ).nodes();
	}
	
	/**
	 * Get the deepest non-leaf level of an r-tree
	 * @param databaseService
	 * @param layer_name
	 * @return
	 */
	public static Set<Node> getRTreeNonleafDeepestLevelNodes(GraphDatabaseService databaseService, String layer_name) 
	{
		Set<Node> nodes = new HashSet<Node>(); 
		Iterable<Node> geometry_nodes = getAllGeometries(databaseService, layer_name);
		for ( Node node : geometry_nodes)
		{
			Node parent = node.getSingleRelationship(RTreeRel.RTREE_REFERENCE, Direction.INCOMING).getStartNode();
			if(parent != null)
				nodes.add(parent);
		}
		return nodes;
	}
}
