package org.matsim.contrib.simulated_annealing;

import org.matsim.api.core.v01.Id;
import org.matsim.contrib.shifts.shift.DrtShift;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ReadShift {
    private List<SAShift> SAShiftList = new LinkedList<>();
    public ReadShift(File inputFile) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
//            File inputFile = new File("C:\\Users\\Shivam\\IdeaProjects\\matsim-external-development\\moiaDrt\\src\\test\\java\\io\\moia\\contrib\\drt\\shift\\run\\shifts.xml");
            SAXParser saxParser = factory.newSAXParser();
            ShiftHandler shiftHandler = new ShiftHandler();
            saxParser.parse(inputFile, shiftHandler);
            this.SAShiftList = shiftHandler.getShifts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<SAShift> getShifts() {
        return SAShiftList;
    }
}

class ShiftHandler extends DefaultHandler {
    private final List<SAShift> SAShiftList = new LinkedList<>();
    private SAShift SAShift;

    public List<SAShift> getShifts() {
        return SAShiftList;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if (qName.equalsIgnoreCase("shift")) {
            SAShift = new SAShift();
            SAShift.setId(Id.create(attributes.getValue("id"), DrtShift.class));
            SAShift.setStartTime(Double.parseDouble(attributes.getValue("start")));
            SAShift.setEndTime(Double.parseDouble(attributes.getValue("end")));
            SAShift.encodeShiftV2();
        }
        if (qName.equalsIgnoreCase("break")) {
            double earliestStart  = Double.parseDouble(attributes.getValue("earliestStart"));
            double latestEnd  = Double.parseDouble(attributes.getValue("latestEnd"));
            double duration  = Double.parseDouble(attributes.getValue("duration"));
            SABreak saBreak = new SABreak(earliestStart, latestEnd, duration);
//            shift = new Shift(shift.getId(), shift.getStart(), shift.getEnd(), aBreak);
            SAShift.setSABreak(saBreak);
            SAShift.encodeShiftV2();
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if(qName.equalsIgnoreCase("shift")) {
            SAShiftList.add(SAShift);
            SAShift = null;
        }
    }
}
