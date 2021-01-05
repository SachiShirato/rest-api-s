package com.example.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.example.xml.XMLTest;

public interface XMLCENTERTest extends XMLTest {

	String RC = "01";

	List<String> TsList = new ArrayList<String>() {
		{
			add("@KBN");
			add("@LVL");
			add("@SVR");
			add("@SVC");
		}
	};

	default String xmlGlbPath(String... path) {

		String gblpath = "/CENTER/GLB_HEAD/";
		for (String p : path) {
			gblpath = gblpath + "/" + p;
		}
		return gblpath;
	}

	default boolean checkDefault(Document putMQmassage, Document getMQmassage) throws ParseException {

		if (!checkDefaultRc(getMQmassage)
				|| (!checkDefaultTs(putMQmassage, getMQmassage))) {
			return false;
		}
		return true;
	}
	
	default boolean checkDefaultRc(Document getMQmassage) throws ParseException {

		return RC.equals((getXmlEvaluate(xmlGlbPath("RC"), getMQmassage)).toString());				
	}

	default boolean checkDefaultTs(Document putMQmassage, Document getMQmassage) throws ParseException {

		NodeList putlist = putMQmassage.getElementsByTagName("TS");

		for (int i = 0; i < putlist.getLength(); i++) {
			int y = i + 1;
			String ts = "TS[" + y + "]";

			for (int x = 0; x < TsList.size(); x++) {
				String tsName = TsList.get(x);

				if (!((getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts, tsName), putMQmassage))
						.equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts, tsName), getMQmassage)))) {
					return false;
				}
			}
			if (!(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts), putMQmassage)
					.equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts), getMQmassage)))) {
				return false;
			}
		}
		return true;
	}

	default boolean checkGetTs(Document getMQmassage) throws ParseException {

		if (!("2".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TsList.get(0)), getMQmassage))
				&& ("1".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TsList.get(1)), getMQmassage)))
				&& ("RSHUBF ".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TsList.get(2)), getMQmassage)))
				&& ("S".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TsList.get(3)), getMQmassage))))) {
			return false;
		}
		return true;
	}

	default Date getTimestamp(Document getMQmassage) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmssSSS");
		NodeList getlist = getMQmassage.getElementsByTagName("TS");

		return dateFormat
				.parse(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[" + getlist.getLength() + "]"), getMQmassage));
	}
}
