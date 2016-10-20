package yellowfinder2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;

public class Parser {
	
static int i = 1;

static int cautionCounter = 1;
	
	public static String[] takeFile (File file) throws FileNotFoundException {
		/*
		 * String-Array:
		 * [0]Mod
		 * [1]Track
		 * [2]Date
		 * [3]Number of Yellows
		 * [4]Yellows
		 */
		
		String[] Result = new String[8];
		
		//Find meta data
		String[] meta = extractMeta(file);
		Result[0] = meta[0];
		Result[1] = meta[1];
		Result[2] = meta[2];
		
		//Find actual yellows
//		String Yellows = "";
//		
//		Scanner scanner = new Scanner (file);
//				
//		while(scanner.hasNextLine()){
//			String OneYellow = scanLine(scanner.nextLine());
//			if (OneYellow != null){
//				Yellows = Yellows + OneYellow;
//			}
//		}
//		i = 1;
//		Result[4] = Yellows;
		
		//Get race start, cautions, restarts, and checkered times
		List<String[]> raceEvents = getRaceEvents(file);
		Result[4] = createStringOutput(raceEvents);
		Result[3] = String.valueOf(cautionCounter-1);
		
		//More race data
		String[] more = moreRaceData(raceEvents);
		Result[5] = more[0];
		Result[6] = more[1];
		Result[7] = more[2];
		
		cautionCounter = 1;
		
		return Result;
	}
	
	private static List<String[]> getRaceEvents(File file) {
		
		List<String[]> raceEvents = new ArrayList<String[]>();	//contains String Array: [0]Timecode, [1]Type (0=race start, 1=caution, 2=restart, 3=race end)

		try {
			//Parse XML file
		    DocumentBuilderFactory domFactory  = DocumentBuilderFactory.newInstance();
		    DocumentBuilder        builder  = domFactory.newDocumentBuilder();
		    org.w3c.dom.Document   document = builder.parse(file);
			//XPath object
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			
			//race start
			XPathExpression raceStart = xpath.compile("//Score[contains(.,'lap=0 point=0')][1]/@et");	//lap=0 point=0 !!!
			String raceStartResult = raceStart.evaluate(document);
			String[] raceStartArray = new String[2];
			raceStartArray[0] = raceStartResult;
			raceStartArray[1] = "0";
		    raceEvents.add(raceStartArray);
		    
		    //cautions
		    XPathExpression cautions = xpath.compile("//Score[text()='Yellow flag state 0->1']/@et");
		    Object cautionsResult = cautions.evaluate(document, XPathConstants.NODESET);
		    NodeList cautionNodes = (NodeList) cautionsResult;
		    for(int i = 0; i<cautionNodes.getLength(); i++) {
		    	String[] caution = new String[2];
		    	caution[0] = cautionNodes.item(i).getTextContent();
		    	caution[1] = "1";
		    	raceEvents.add(caution);
		    }
		    
		    //restarts
		    XPathExpression restarts = xpath.compile("//Score[text()='Yellow flag state 5->6']/@et");
		    Object restartsResult = restarts.evaluate(document, XPathConstants.NODESET);
		    NodeList restartNodes = (NodeList) restartsResult;
		    for(int i = 0; i<restartNodes.getLength(); i++) {
		    	String[] restart = new String[2];
		    	restart[0] = restartNodes.item(i).getTextContent();
		    	restart[1] = "2";
		    	raceEvents.add(restart);
		    }
		    
		    //race end
		    XPathExpression raceEnd = xpath.compile("//Score[starts-with(.,'Checkered for ')]/@et");
			String raceEndResult = raceEnd.evaluate(document);
			String[] raceEndArray = new String[2];
			raceEndArray[0] = raceEndResult;
			raceEndArray[1] = "3";
		    raceEvents.add(raceEndArray);
		    
		    raceEvents = sortByTime(raceEvents);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return raceEvents;
	}
	
	private static String createStringOutput(List<String[]> raceEvents){
		String raceEventsOutput = "";
		for(int i = 0; i<raceEvents.size(); i++){
	    	raceEventsOutput = raceEventsOutput + createStringOutput(raceEvents.get(i));
	    }
		return raceEventsOutput;
	}
	
	private static List<String[]> sortByTime(List<String[]> oList) {
		
		class timeComperator implements Comparator<String[]> {
			  @Override public int compare( String[] time1, String[] time2 )
			  {
				  return Float.valueOf(time1[0]).compareTo(Float.valueOf(time2[0]));
			  }
			}
		
		Collections.sort( oList, new timeComperator() );
		
		return oList;
	}
	
	private static String createStringOutput(String[] raceEvent) {
		
		String output = null;
		
		//time conversion
		String time = timeConversion(raceEvent[0]);
		
		//type determination + output
		if(raceEvent[1].equals("0")){
			output = "Race Start: " + time + "\n \n";
		}
		
		if(raceEvent[1].equals("1")){
			output = "Yellow #" + cautionCounter + ": " + time + "\n \n";
			cautionCounter++;
		}
		
		if(raceEvent[1].equals("2")){
			output = "Re-Start: " + time + "\n \n";
		}
		
		if(raceEvent[1].equals("3")){
			output = "Checkered Flag: " + time;
		}
		
		return output;
	}
	
	private static String timeConversion(String oTime){
		float time = Float.valueOf( oTime ).floatValue();
		int min = (int) Math.floor(time / 60);
		int sek = (int) (time%60);
		String sekS = ""+sek;	//int sek zu String
		if (sekS.length()==1){
			sekS = "0"+sekS;	//0 vorne dran stellen, falls Sek nur eine Stelle hat
		}
		return min+":"+sekS;
	}
	
	private static String[] moreRaceData(List<String[]> raceEvents){
		
		String[] raceData = new String[3];
		/*
		 * raceData[0] = race length
		 * raceData[1] = overall duration of cautions
		 * raceData[2] = average duration of green stint
		 */
		
		//race length
		raceData[0] = String.valueOf(Float.valueOf(raceEvents.get(raceEvents.size()-1)[0]) - Float.valueOf(raceEvents.get(0)[0]));
		
		//duration of cautions
		float duration = 0;
		for(int i = 1; i<raceEvents.size(); i=i+2){
			if(raceEvents.get(i)[1].equals("1")){	//check, that i is caution
				duration+=Float.valueOf((raceEvents.get(i+1))[0]) - Float.valueOf(raceEvents.get(i)[0]);
			}
		}
		raceData[1] = String.valueOf(duration);
		
		//avg green
		if(raceEvents.get(raceEvents.size()-1)[1].equals("3")) {	//race ended without last restart
			raceData[2] = String.valueOf((Float.valueOf(raceData[0])-Float.valueOf(raceData[1]))/cautionCounter);
		} else {
			raceData[2] = String.valueOf((Float.valueOf(raceData[0])-Float.valueOf(raceData[1]))/(cautionCounter+1));
		}
		
		//time convert
		String[] raceDataT = new String[3];
		for(int i = 0; i<raceData.length; i++) {
			raceDataT[i] = timeConversion(raceData[i]);
		}
		
		return raceDataT;
		
	}
	
	private static String scanLine (String line){
		
		String OneYellow = null;
		
		Scanner linescanner = new Scanner (line);
		if (linescanner.hasNext()){
			String inline = line;
			String yellow = linescanner.findInLine("Yellow flag state 0-&gt;1");
			if (yellow != null){
				Scanner inlinescanner = new Scanner (inline);
				inlinescanner.useDelimiter("\"");
				inlinescanner.next();
				String timeString = inlinescanner.next();
				//System.out.println(timeString);
				
				float time = Float.valueOf( timeString ).floatValue();
				//System.out.println(time);
				int min = (int) Math.floor(time / 60);
				//System.out.println(min);
				int sek = (int) (time%60);
				String sekS = ""+sek;	//int sek zu String
				if (sekS.length()==1){
					sekS = "0"+sekS;	//0 vorne dran stellen, falls Sek nur eine Stelle hat
				}
				
				OneYellow = "Yellow #" + i + ": " + min + ":" + sekS + "\n \n";
				
				i++;
			}
		}
		return OneYellow;
	}
	
	private static String[] extractMeta(File file){
		String[] meta = new String[3];
		
		try {
			//Parse XML file
		      DocumentBuilderFactory factory  = DocumentBuilderFactory.newInstance();
		      DocumentBuilder        builder  = factory.newDocumentBuilder();
		      org.w3c.dom.Document   document = builder.parse(file);
		      //Mod
		      NodeList ndList1 = document.getElementsByTagName("Mod");
		      meta[0] = ndList1.item(0).getChildNodes().item(0).getNodeValue();
		      //Track
		      NodeList ndList2 = document.getElementsByTagName("TrackCourse");
		      meta[1] = ndList2.item(0).getChildNodes().item(0).getNodeValue();
		      //Date
		      NodeList ndList3 = document.getElementsByTagName("TimeString");
		      meta[2] = ndList3.item(1).getChildNodes().item(0).getNodeValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
	      
		return meta;
	}

}
