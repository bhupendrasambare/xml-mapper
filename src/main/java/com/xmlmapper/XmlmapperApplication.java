package com.xmlmapper;

import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class XmlmapperApplication {

	private static final Logger logger = Logger.getLogger(XmlmapperApplication.class.getName());
	private static final int INDENT_FACTOR = 4;

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter the XML input:");
		System.out.println("NOTE: Do not enter extra line break between xml input");

		StringBuilder xmlInputBuilder = new StringBuilder();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.trim().isEmpty()) break;
			xmlInputBuilder.append(line).append("\n");
		}
		scanner.close();

		String xmlInput = xmlInputBuilder.toString();
		String jsonOutput = convertXmlToJson(xmlInput);

		System.out.println("Converted JSON:\n" + jsonOutput);
	}

	public static String convertXmlToJson(String xmlInput) {
		try {
			Document xmlDoc = parseXmlToDocument(xmlInput);
			int totalMatchScore = calculateTotalMatchScore(xmlDoc);

			JSONObject jsonObject = XML.toJSONObject(xmlInput);

			// Add TotalMatchScore to JSON output
			jsonObject.getJSONObject("Response")
					.getJSONObject("ResultBlock")
					.put("MatchSummary", new JSONObject().put("TotalMatchScore", String.valueOf(totalMatchScore)));

			return jsonObject.toString(INDENT_FACTOR);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to convert XML to JSON", e);
			return "{}"; // Return empty JSON object on failure
		}
	}

	private static Document parseXmlToDocument(String xmlInput) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(xmlInput)));
	}

	private static int calculateTotalMatchScore(Document xmlDoc) {
		int totalScore = 0;
		NodeList scoreNodes = xmlDoc.getElementsByTagName("Score");

		for (int i = 0; i < scoreNodes.getLength(); i++) {
			try {
				int score = Integer.parseInt(scoreNodes.item(i).getTextContent().trim());
				totalScore = Math.addExact(totalScore, score); // Avoid overflow
			} catch (NumberFormatException e) {
				logger.log(Level.WARNING, "Invalid score value found, skipping...", e);
			} catch (ArithmeticException e) {
				logger.log(Level.WARNING, "Score sum exceeds allowable integer limit.", e);
				break; // Stop if the score sum exceeds integer range
			}
		}
		return totalScore;
	}

}
