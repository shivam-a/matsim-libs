package org.matsim.core.mobsim.jdeqsim.util;

import java.util.ArrayList;
import java.util.LinkedList;

import org.matsim.core.events.ActivityEndEvent;
import org.matsim.core.events.ActivityStartEvent;
import org.matsim.core.events.PersonEvent;
import org.matsim.core.mobsim.jdeqsim.EventLog;


public class DEQSimEventFileComparator extends TestHandlerDetailedEventChecker {

	private String pathToDEQSimEventsFile = null;

	public DEQSimEventFileComparator(String path) {
		pathToDEQSimEventsFile = path;
	}

	/*
	 * compare events to deq event file. The order of events must also be the
	 * same. (this test will only succeed for simple tests with one car
	 * often!!!) => reason: at junctions the order of cars can change + stuck
	 * vehicles are dealt with in different ways
	 */
	public void checkAssertions() {
 		LinkedList<PersonEvent> copyEventList=new LinkedList<PersonEvent>();
 		
 		// remove ActStartEvent and ActEndEvent, because this does not exist in
		// c++ DEQSim
 		for (int i=0;i<allEvents.size();i++){
	 		if (!(allEvents.get(i) instanceof ActivityStartEvent || allEvents.get(i) instanceof ActivityEndEvent)){
				copyEventList.add(allEvents.get(i));
			}
 		}
 		
		ArrayList<EventLog> deqSimLog=CppEventFileParser.parseFile(pathToDEQSimEventsFile);
		for (int i=0;i<copyEventList.size();i++){
			assertEquals(true,CppEventFileParser.equals(copyEventList.get(i), deqSimLog.get(i)));
		}
	}
}
