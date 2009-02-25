package playground.mmoyo.PTCase1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.matsim.basic.v01.IdImpl;
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.network.Link;
import org.matsim.network.NetworkLayer;
import org.matsim.network.Node;

import playground.mmoyo.PTRouter.*;

/** 
 * Represent a network layer with independent route with special (transfer) links at intersections 
 * *
 * @param cityNet complete network from which the PT network is extracted
 */

public class PTNetworkLayer extends NetworkLayer {
	//--> Get these values out from the city network
	// These are temporary values for the 5x5 scenario
	int maxNodekey = 24;
	int maxLinkKey = 79;

	private NetworkLayer cityNet;
	

	// This map stores the children nodes of each father node, necessary to
	// create the transfer between them
	public Map<IdImpl, ArrayList<String>> childrenList = new TreeMap<IdImpl, ArrayList<String>>();
		
	public PTNetworkLayer(NetworkLayer cityNet) {
		super();
		this.cityNet = cityNet;
	}

	public void createPTNetwork(List<PTLine> ptLineList) {
		// Read the route of every PTline and adds the corresponding links and
		// nodes
		for (Iterator<PTLine> iterPTLines = ptLineList.iterator(); iterPTLines.hasNext();) {
			PTLine ptLine = iterPTLines.next();
			boolean firstLink = true;
			String idFromNode = "";
			for (Iterator<String> iter = ptLine.getRoute().iterator(); iter.hasNext();) {
				Link l = this.cityNet.getLink(iter.next());
				idFromNode = addToSubwayPTN(l, idFromNode, firstLink, ptLine.getId());
				firstLink = false;
			}
		}
		createTransferlinks();
	}

	public String addToSubwayPTN(Link l, String idFromNode, boolean firstLink,IdImpl IdPTLine) {
		// Create the "Metro underground paths" related to the city network
		
		// Create FromNode
		if (firstLink) {
			maxNodekey++;
			idFromNode = String.valueOf(maxNodekey);
			addPTNode(idFromNode, l.getFromNode(), IdPTLine);
		}

		// Create ToNode
		maxNodekey++;
		String idToNode = String.valueOf(maxNodekey);
		addPTNode(idToNode, l.getToNode(), IdPTLine);// ToNode

		// Create the Link
		maxLinkKey++;
		this.createLink(maxLinkKey, idFromNode, idToNode, "Standard");

		return idToNode;
	}// AddToSub
	
	private void addWalkingNode(IdImpl idImpl) {
		//System.out.print(idImpl.toString() + " " + this.nodes.containsKey(idImpl) + " " );
		
		if (this.nodes.containsKey(idImpl)) {
			throw new IllegalArgumentException(this + "[id=" + idImpl + " already exists]");
		}
		
		Node n = this.cityNet.getNode(idImpl);
		PTNode node = new PTNode(idImpl,	n.getCoord(), n.getType());
		node.setIdFather(idImpl);        //All ptnodes must have a father, including fathers (themselves)
		node.setIdPTLine(new IdImpl("Walk"));
		this.nodes.put(idImpl, node);
		n= null;
	}

	private void addPTNode(String id, Node original, IdImpl IdPTLine) {
		// Creates a underground clone of a node with a different ID
		IdImpl idImpl = new IdImpl(id);
		IdImpl idFather = new IdImpl(original.getId().toString());

		PTNode ptNode = new PTNode(idImpl, original.getCoord(), original.getType(), idFather, IdPTLine);
		Id i = new IdImpl(id);
		if (this.nodes.containsKey(i)) {
			throw new IllegalArgumentException(this + "[id=" + id + " already exists]");
		}
		this.nodes.put(i, ptNode);

		// updates this list of childrenNodes
		if (!childrenList.containsKey(idFather)) {
			ArrayList<String> ch = new ArrayList<String>();
			childrenList.put(idFather, ch);
		}
		childrenList.get(idFather).add(id);

		idImpl = null;
		idFather = null;
		ptNode = null;
		i = null;
	}

	// To print nodes and his respective children
	public static void showMap(Map map) {
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			System.out.println(entry.getKey() + " = " + entry.getValue());
		}
		iter = null;
	}

	private void createTransferlinks() {
		// (like stairs between lines in a subway station)
		Iterator it = childrenList.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			List chList1 = (ArrayList) pairs.getValue();
			List chList2 = (ArrayList) pairs.getValue();

			if (chList1.size() > 1) {
				String n1 = "";
				String n2 = "";
				for (Iterator<String> iter1 = chList1.iterator(); iter1.hasNext();) {
					n1 = iter1.next();
					// Create links between children nodes lines
					for (Iterator<String> iter2 = chList2.iterator(); iter2.hasNext();) {
						n2 = iter2.next();
						if (n1 != n2) {
							maxLinkKey++;
							this.createLink(maxLinkKey, n1, n2,"Transfer");
						}//if n1
					}//for iter2
				}// for iter1
				n1= null;
				n2= null;
			}// if chlist
		}// while
		it = null;
	}// CreateTransfer


	public List<String> createWalkingLinks(IdImpl idFromNode, IdImpl idToNode ){
		//Adds temporary the origin and destination node and create new temporary links between 
		//between them  and its respective children to the routing process
		
		addWalkingNode(idFromNode);
		addWalkingNode(idToNode);
		
		int i = 0;
		List<String> WalkingLinkList = new ArrayList<String>(); 
		
		//Starting links
		List<String> uChildren = this.childrenList.get(idFromNode);
		for (Iterator<String> iter = uChildren.iterator(); iter.hasNext();) {
			this.createLink(--i, idFromNode.toString(), iter.next(), "Walking");
			WalkingLinkList.add(String.valueOf(i));
		}
		
		//Endings links
		uChildren = this.childrenList.get(idToNode);
		for (Iterator<String> iter = uChildren.iterator(); iter.hasNext();) {
			this.createLink(--i, iter.next(), idToNode.toString(), "Walking");
			WalkingLinkList.add(String.valueOf(i));
		}
		return WalkingLinkList;
	}
	
	public void removeWalkinkLinks(List<String> WalkingLinkList){
		//Removes temporal links at the end of the ruting process
		for (Iterator<String> iter = WalkingLinkList.iterator(); iter.hasNext();) {
			this.removeLink(this.getLink(iter.next()));
		}
	}
	
	public void removeWalkingNodes(IdImpl node1, IdImpl node2){
		//Removes temporal links at the end of the routing process
		this.removeNode(this.getNode(node1));
		this.removeNode(this.getNode(node2));
	}
	
	// Creates only irrelevant values to create a PTLink. Actually the cost is calculated on other parameters
	private void createLink(int id, String from, String to, String ptType ){
		String length = "1";
		String freespeed= "1";
		String capacity = "1";
		String permlanes = "1";
		String origid = "0";
		this.createLink(String.valueOf(id), from, to, length, freespeed, capacity, permlanes, origid, ptType);
	}
		
	public void printLinks() {
		//Displays a quick visualization of links with from- and to- nodes
		for (org.matsim.network.Link l : this.getLinks().values()) {
			System.out.print("\n(" ); 
			System.out.print(l.getFromNode().getId().toString()); 
			System.out.print( ")----" ); 
			System.out.print( l.getId().toString() ); 
			System.out.print( "--->("); 
			System.out.print( l.getToNode().getId().toString() ); 
			System.out.print( ")   " + l.getType() ); 
			System.out.print( "      (" ); 
			System.out.print( ((PTNode) l.getFromNode()).getIdFather().toString()); 
			System.out.print( ")----" ); 
			System.out.print( l.getId().toString() ); 
			System.out.print( "--->(" ); 
			System.out.print( ((PTNode) l.getToNode()).getIdFather().toString() ); 
			System.out.print( ")");
		}
	}
}

/*
 * Old CODE
 *  public void setMaxNodekey(int maxNodekey) { 
 *  	this.maxNodekey = maxNodekey; 
 *  }
 * 
 * public void setMaxLinkKey(int maxLinkKey) { 
 *		this.maxLinkKey = maxLinkKey;
 * }
 * 
*/

