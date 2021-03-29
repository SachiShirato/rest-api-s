package jp.co.acom.fehub.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import com.example.api.ItemRestController;
import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01Test;
import jp.co.acom.fehub.mq.QUEUE;
import jp.co.acom.fehub.xml.XMLCENTERTest;

public class XmlCenterTest implements QMFH01Test, XMLCENTERTest {

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
		putMQmessage.correlationId = getUnique24().getBytes();
		return putMQmessage;
	}

	void lastCheckBody(MQMessage putMQmessage, MQMessage getMQmessage, boolean request)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {

		Document putMQmessageDocument = changeStringToDocument(
				toStringMQMessage(putMQmessage).replaceAll(System.lineSeparator(), "").replaceAll("\t", ""));
		Document getMQmessageDocument = changeStringToDocument(
				toStringMQMessage(getMQmessage).replaceAll(System.lineSeparator(), "").replaceAll("\t", ""));

		List<String> list = new ArrayList<>();
		list.add("TIMESTAMP");
		list.add("RC");
		list.add("REPLY");
		list.add("REQ_PARM");
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
				for (int x = 0; x < TS_LIST.size(); x++) {
					String tsName = TS_LIST.get(x);
					String getEqual = getXmlEvaluate(xmlGlbPath("TIMESTAMP", ts, tsName), getMQmessageDocument);

					switch (x) {
					case 0:
						if (i == putSize || i == putSize + 1) {
							assertEquals("1", getEqual);
						} else {
							assertEquals("2", getEqual);
						}
						break;
					case 1:
						if (i == putSize || i == getSize - 1) {
							assertEquals("1", getEqual);
						} else {
							assertEquals("2", getEqual);
						}
						break;
					case 2:
						if (i < putMQmessageDocument.getElementsByTagName("TS").getLength() + 2) {
							assertEquals("RSHUBFX", getEqual);
						} else {
							assertEquals("QMFH01", getEqual);
						}
						break;
					case 3:
						assertEquals("DF200", getEqual);
						break;

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

	void lastCheckMqmd(MQMessage putMQmessage, MQMessage getMQmessage, boolean errQ, boolean request) {

		assertAll(

				() -> assertEquals(MQC.MQMT_REPLY, getMQmessage.messageType),
				() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmessage.format.trim()),
				() -> assertTrue(mqCheck(putMQmessage, getMQmessage)),
				() -> assertEquals(putMQmessage.encoding, getMQmessage.encoding),
				() -> assertNotEquals(MQC.MQEI_UNLIMITED, getMQmessage.expiry),
				() -> assertEquals(MQC.MQPER_NOT_PERSISTENT, getMQmessage.persistence),
				() -> assertEquals(getXmlTag(toStringMQMessage(putMQmessage), "SERVICEID"),
						getMQmessage.applicationIdData.trim())

		);
	}

	void lastCheck(MQMessage putMQmessage, MQMessage getMQmessage, String getMQname, int flg) throws Exception {

		lastCheckMqmd(putMQmessage, getMQmessage, getMQname == QUEUE.QL_DH_ERR.getQName(), flg == 0);
		lastCheckBody(putMQmessage, getMQmessage, flg == 0);
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class StartingRizaTest {

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test1and6_Normal")
		void test1and6_Normal(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(QUEUE.QC_DH_REQ.getQName(), putMQmessage);
			MQMessage getMQmessage = mqGetWaitCorrelid(QUEUE.QL_DW_REP.getQName(), putMQmessage.messageId);

			lastCheck(putMQmessage, getMQmessage, QUEUE.QL_DW_REP.getQName(), 0);

		}

		Stream<Arguments> params_Normal() throws Exception {

			return Stream.of(Arguments.of(pathToString("/ts200.xml")), Arguments.of(setRc(createMQMessageBody(), "")),
					Arguments.of(setRequestid(createMQMessageBody(), "")));
		}
		
		
		
		
		@ParameterizedTest
		@DisplayName("test7_HTTPResponseError")
		@MethodSource("params_HTTPResponseError")
		void test7_HTTPResponseError(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);
		}

		Stream<Arguments> params_HTTPResponseError() throws Exception {

			return Stream.of(Arguments.of(setServiceid(createMQMessageBody(), "DF999")),
					Arguments.of(setServiceid(createMQMessageBody(), "DF400")),
					Arguments.of(setServiceid(createMQMessageBody(), "DF500")));
		}
		
		
		
		
		@Test
		@DisplayName("test1and6_NonXml")
		protected void test1and6_NonXml() throws Exception {

			MQMessage putMQmessage = setUpCreateMQ((setServiceid(createMQMessageBody(), "DF800")));
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

			MQMessage getMQmessage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmessage.correlationId);
			lastCheckMqmd(putMQmessage, getMQmessage, false, false);
			assertEquals(ItemRestController.STR_DF800, toStringMQMessage(getMQmessage));
		}


	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class StoppedRizaTest {

		@Test
		@DisplayName("test2_HTTPRequestError")
		void test2_HTTPRequestError() throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(createMQMessageBody());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

//			lastCheck(putMQmessage, mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmessage.correlationId),
//					QUEUE.QL_DW_REP.getQName(), 0);
		}

	}
}
