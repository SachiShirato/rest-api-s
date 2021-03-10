package jp.co.acom.fehub.xml;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

public interface XMLCENTERTest extends XMLTest {

	// TODO 大文字
	String path = "/ts3.xml";

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

	default boolean checkDefault(Document putMQmassage, Document getMQmassage)
			throws ParseException, XPathExpressionException {

		return (checkDefaultRc(getMQmassage) && (checkDefaultTs(putMQmassage, getMQmassage)));
	}

	default boolean checkDefaultRc(Document getMQmassage) throws ParseException, XPathExpressionException {

		return "R".equals((getXmlEvaluate(xmlGlbPath("RC"), getMQmassage)).toString());
	}

	default boolean checkDefaultTs(Document putMQmassage, Document getMQmassage)
			throws ParseException, XPathExpressionException {

		for (int i = 0; i < putMQmassage.getElementsByTagName("TS").getLength(); i++) {
			String ts = "TS[" + (i + 1) + "]";

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

	default boolean checkGetTs(Document getMQmassage) throws ParseException, XPathExpressionException {

		if (!("2".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TsList.get(0)), getMQmassage))
				&& ("1".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TsList.get(1)), getMQmassage)))
				&& ("RSHUBF ".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TsList.get(2)), getMQmassage)))
				&& ("S".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TsList.get(3)), getMQmassage))))) {
			return false;
		}
		return true;
	}

	default Date getTimestamp(Document getMQmassage) throws ParseException, XPathExpressionException {

		return new SimpleDateFormat("yyyyMMddhhmmssSSS").parse(getXmlEvaluate(
				xmlGlbPath("TIMESTAMP", "TS[" + getMQmassage.getElementsByTagName("TS").getLength() + "]"),
				getMQmassage));
	}

	// TODO Message
	default String createMQMAssageBody() throws IOException {
		return pathToString(path);
	}

	default String getXmlTag(String body, String tag) throws IOException {
		String headtag = "<" + tag + ">";
		String lasttag = "</" + tag + ">";
		return body.substring(body.indexOf(headtag) + headtag.length(), body.indexOf(lasttag));
	}

	// TODO setServiceid
	default String createBreakeServiceid(String body, String serviceid) throws Exception {

		return body.replaceAll("<SERVICEID>.*</SERVICEID>",
				StringUtils.isEmpty(serviceid) ? "<SERVICEID/>" : "<SERVICEID>" + serviceid + "</SERVICEID>");
	}

	default String createBreakeRc(String body, String rc) throws Exception {

		return body.replaceAll("<RC>.*</RC>", StringUtils.isEmpty(rc) ? "<RC/>" : "<RC>" + rc + "</RC>");

	}

	default String createBreakeRequestid(String body, String requestid) throws Exception {
		return body.replaceAll("<REQUESTID>.*</REQUESTID>",
				StringUtils.isEmpty(requestid) ? "<REQUESTID/>" : "<REQUESTID>" + requestid + "</REQUESTID>");
	}

	default String createBreakeEndtag(String body, String endtag) throws Exception {
		return body.replace("</" + endtag + ">", "<" + endtag + ">");
	}

	default String createBreakeBody(String body) throws Exception {
		return body.replace("CENTER xmlns=\"http://www.acom.co.jp/ACOM\"",
				"CENTER xmlns=\"http://www.acom.co.jp/ACOMMM\"");
	}
}
