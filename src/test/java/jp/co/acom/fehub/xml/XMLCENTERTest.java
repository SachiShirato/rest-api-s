package jp.co.acom.fehub.xml;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

public interface XMLCENTERTest extends XMLTest {

	String PATH = "/ts3.xml";

	List<String> TS_LIST = Arrays.asList("@KBN", "@LVL", "@SVR", "@SVC");

	default String xmlGlbPath(String... path) {

		String gblpath = "/CENTER/GLB_HEAD/";

		for (String p : path)
			gblpath = gblpath + "/" + p;

		return gblpath;
	}

	default boolean checkDefault(Document putMQmessage, Document getMQmessage)
			throws ParseException, XPathExpressionException {

		return (checkDefaultRc(getMQmessage) && (checkDefaultTs(putMQmessage, getMQmessage)));
	}

	default boolean checkDefaultRc(Document getMQmessage) throws ParseException, XPathExpressionException {

		return "R".equals((getXmlEvaluate(xmlGlbPath("RC"), getMQmessage)).toString());
	}

	default boolean checkDefaultTs(Document putMQmessage, Document getMQmessage)
			throws ParseException, XPathExpressionException {

		for (int i = 0; i < putMQmessage.getElementsByTagName("TS").getLength(); i++) {

			String ts = "TS[" + (i + 1) + "]";

			for (int x = 0; x < TS_LIST.size(); x++) {

				String tsName = TS_LIST.get(x);

				if (!((getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts, tsName), putMQmessage))
						.equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts, tsName), getMQmessage))))
					return false;
			}

			if (!(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts), putMQmessage)
					.equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts), getMQmessage))))
				return false;
		}

		return true;
	}

	default boolean checkGetTs(Document getMQmessage) throws ParseException, XPathExpressionException {

		return ("2".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TS_LIST.get(0)), getMQmessage))
				&& ("1".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TS_LIST.get(1)), getMQmessage)))
				&& ("RSHUBF ".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TS_LIST.get(2)), getMQmessage)))
				&& ("S".equals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[4]", TS_LIST.get(3)), getMQmessage))));
	}

	default Date getTimestamp(Document getMQmessage) throws ParseException, XPathExpressionException {

		return new SimpleDateFormat("yyyyMMddhhmmssSSS").parse(getXmlEvaluate(
				xmlGlbPath("TIMESTAMP", "TS[" + getMQmessage.getElementsByTagName("TS").getLength() + "]"),
				getMQmessage));
	}

	default String createMQMessageBody() throws IOException {

		return pathToString(PATH);
	}

	default String getXmlTag(String body, String tag) throws IOException {

		String headtag = "<" + tag + ">";
		String lasttag = "</" + tag + ">";

		return body.substring(body.indexOf(headtag) + headtag.length(), body.indexOf(lasttag));
	}

	default String setTag(String body, String tag, String data) throws Exception {

		return body.replaceAll("<" + tag + ">.*</" + tag + ">",
				StringUtils.isEmpty(data) ? "<" + tag + "/>" : "<" + tag + ">" + data + "</" + tag + ">");
	}

	default String setServiceid(String body, String serviceid) throws Exception {

		return setTag(body, "SERVICEID", serviceid);
	}

	default String setRc(String body, String rc) throws Exception {

		return setTag(body, "RC", rc);
	}

	default String setRequestid(String body, String requestid) throws Exception {

		return setTag(body, "REQUESTID", requestid);
	}

	default String createBreakeEndtag(String body, String endtag) throws Exception {

		return body.replace("</" + endtag + ">", "<" + endtag + ">");
	}

	default String createBreakeBody(String body) throws Exception {

		return body.replace("CENTER xmlns=\"http://www.acom.co.jp/ACOM\"",
				"CENTER xmlns=\"http://www.acom.co.jp/ACOMMM\"");
	}

	default String getTagData(String tag, Document document) throws XPathExpressionException {

		return getXmlEvaluate(xmlGlbPath(tag), document);
	}

	default String getTimestampName(int i, String x, Document document) throws XPathExpressionException {

		return getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[" + i + "]", x), document);
	}

	default String getTimestampName(int i, Document document) throws XPathExpressionException {

		return getXmlEvaluate(xmlGlbPath("TIMESTAMP", "TS[" + i + "]"), document);
	}

	default boolean isYmd(String ymd) {
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			sdf.setLenient(false);
			sdf.parse(ymd);

			return true;

		} catch (Exception ex) {
			return false;
		}
	}
}
