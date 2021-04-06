package jp.co.acom.fehub.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01Test;
import jp.co.acom.fehub.mq.QUEUE;
import jp.co.acom.fehub.xml.XMLCENTERTest;

public class HttpClientMain implements QMFH01Test, XMLCENTERTest {

	@BeforeEach
	void setUp() throws Exception {

		mqtoEmpty(getList());
	}

	@AfterEach
	void tearDown() throws Exception {

		assertTrue(mqtoEmpty(getList()));
	}

	MQMessage setUpCreateMQ(String body) throws Exception {

		MQMessage putMQmessage = createMQMessageRequest(body, QUEUE.QL_DW_REP.getQName());
//TODO 不要		putMQmessage.correlationId = getUnique24().getBytes();
		return putMQmessage;
	}

	void lastCheckBody(MQMessage putMQmessage, MQMessage getMQmessage, boolean request)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {

		Document putMQmessageDocument = changeStringToDocument(
				toStringMQMessage(putMQmessage).replaceAll(System.lineSeparator(), "").replaceAll("\t", ""));
		Document getMQmessageDocument = changeStringToDocument(toStringMQMessage(getMQmessage)
				.replaceAll(System.lineSeparator(), "").replaceAll("\t", "").replaceAll("IBM-930", "UTF-8"));
//		System.out.println(toStringMQMessage(putMQmessage).replaceAll(System.lineSeparator(), "").replaceAll("\t", ""));
//		System.out.println(toStringMQMessage(getMQmessage).replaceAll(System.lineSeparator(), "").replaceAll("\t", ""));
		List<String> list = new ArrayList<>();
		list.add("TIMESTAMP");
		list.add("RC");
		list.add("REPLY");
		list.add("D");
//		list.add("REQ_PARM");

//TODO DLとDFの分岐が必要  DLの時は、エンコーディングと対応外文字を置換 cdata とエンコーディングを別だしでチェックする		

// ここから
		Diff diff = DiffBuilder.compare(putMQmessageDocument).withTest(getMQmessageDocument)
				.withNodeFilter(node -> !list.contains(node.getNodeName())).build();

		Iterator<Difference> iter = diff.getDifferences().iterator();
		int size = 0;
		while (iter.hasNext()) {
			System.out.println(iter.next().toString());
			size++;
		}
		assertTrue(size == 0);
// ここまで

		if (request) {
			assertEquals("00", (getXmlEvaluate(xmlGlbPath("RC"), getMQmessageDocument)));
			assertEquals("QMFH01", getXmlEvaluate(xmlGlbPath("R_PVR"), getMQmessageDocument));
			assertEquals("QL.DW.REP", getXmlEvaluate(xmlGlbPath("R_DST"), getMQmessageDocument));

			int putSize = putMQmessageDocument.getElementsByTagName("TS").getLength();
			int getSize = getMQmessageDocument.getElementsByTagName("TS").getLength();

			for (int i = 0; i < putSize; i++) {
				String ts = "TS[" + (i + 1) + "]";
				for (int x = 0; x < TS_LIST.size(); x++) {
					String tsName = TS_LIST.get(x);
					assertEquals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts, tsName), putMQmessageDocument),
							(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts, tsName), getMQmessageDocument)));
				}
				assertEquals(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts), putMQmessageDocument),
						(getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts), getMQmessageDocument)));

			}

			for (int i = putSize; i < getSize; i++) {
				String ts = "TS[" + (i + 1) + "]";
				// String getEqual = getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts, tsName),
				// getMQmessageDocument);

				for (int x = 0; x < TS_LIST.size(); x++) {
					String tsName = TS_LIST.get(x);
					String getEqual = getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts, tsName), getMQmessageDocument);

					for (String string : TS_LIST) {
						switch (string) {
						case "@KVN":

							if (i == putSize || i == putSize + 1) {
								assertEquals("1", getEqual);
							} else {
								assertEquals("2", getEqual);
							}
							break;
						case "LVL":
							if (i == putSize || i == getSize - 1) {
								assertEquals("1", getEqual);
							} else {
								assertEquals("2", getEqual);
							}
							break;
						case "SVR":
							if (i < putMQmessageDocument.getElementsByTagName("TS").getLength() + 2) {
								assertEquals("RSHUBFX", getEqual);
							} else {
								assertEquals("QMFH01", getEqual);
							}
							break;
						case "SVC":
							assertEquals("DF200", getEqual);
							break;
						}
					}

				}

//TODO タイムスタンプ 秒を省いている　　端末時間マイナス９→端末依存
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.HOUR_OF_DAY, -9);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

				assertEquals(dateFormat.format(calendar.getTime()).substring(0, 12),
						dateFormat.format(dateFormat.parse(getXmlEvaluate(
								xmlGlbPath("TIMESTAMP",
										"TS[" + getMQmessageDocument.getElementsByTagName("TS").getLength() + "]"),
								getMQmessageDocument))).substring(0, 12));
				
				

			}
		}
	}

	void lastCheckMqmd(MQMessage putMQmessage, MQMessage getMQmessage, boolean errQ, boolean request)
			throws IOException {
		if (errQ) {
//			assertEquals(putMQmessage.messageType, getMQmessage.messageType);
//			assertTrue(mqCheck(putMQmessage, getMQmessage));

			assertEquals(MQC.MQMT_REPLY, getMQmessage.messageType);
			assertTrue(mqCheck(putMQmessage, getMQmessage));
			assertAll(

					() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmessage.format.trim()),

					() -> assertEquals(putMQmessage.encoding, getMQmessage.encoding),
					() -> assertEquals(MQC.MQEI_UNLIMITED, getMQmessage.expiry),
					() -> assertEquals(MQC.MQPER_PERSISTENT, getMQmessage.persistence),
					() -> assertEquals(getXmlTag(toStringMQMessage(putMQmessage), "SERVICEID"),
							getMQmessage.applicationIdData.trim()));

		} else {
//			assertEquals(MQC.MQMT_REPLY, getMQmessage.messageType);
//			assertTrue(mqCheck(putMQmessage, getMQmessage));
			assertAll(

					() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmessage.format.trim()),

					() -> assertEquals(putMQmessage.encoding, getMQmessage.encoding),
					() -> assertNotEquals(MQC.MQEI_UNLIMITED, getMQmessage.expiry),
					() -> assertEquals(MQC.MQPER_NOT_PERSISTENT, getMQmessage.persistence),
					() -> assertEquals(getXmlTag(toStringMQMessage(putMQmessage), "SERVICEID"),
							getMQmessage.applicationIdData.trim()));

		}

	}

	void lastCheck(MQMessage putMQmessage, MQMessage getMQmessage, String getMQname, int flg) throws Exception {

		lastCheckMqmd(putMQmessage, getMQmessage, getMQname == QUEUE.QL_DH_ERR.getQName(), flg == 0);
		lastCheckBody(putMQmessage, getMQmessage, flg == 0);
	}
}
