package jp.co.acom.fehub.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01Test;
import jp.co.acom.fehub.mq.QUEUE;
import jp.co.acom.fehub.xml.TsAttribute;
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

		MQMessage putMQmessage = createMQMessageRequest(body, GET_QUEUE_NAME);
		return putMQmessage;
	}

	String setUpCreateXML(String path) throws Exception {
		return pathToString(path);
	}

	void lastCheckBody(MQMessage putMQmessage, MQMessage getMQmessage, boolean request)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {

		String putMQmessageStr = toStringMQMessage(putMQmessage).replaceAll(System.lineSeparator(), "").replaceAll("\t",
				"");
		String getMQmessageStr = toStringMQMessage(getMQmessage).replaceAll(System.lineSeparator(), "").replaceAll("\t",
				"");
		Document putMQmessageDocument = changeStringToDocument(putMQmessageStr);
		Document getMQmessageDocument = changeStringToDocument(getMQmessageStr);
		List<String> list = new ArrayList<>();
		list.add("RC");
		list.add("D");
		list.add("REPLY");
		list.add("TIMESTAMP");
//TODO DLとDFの分岐が必要  DLの時は、エンコーディングと対応外文字を置換 cdata とエンコーディングを別だしでチェックする		

		assertTrue(check(putMQmessageDocument, getMQmessageDocument, list));
		if (request) {
			assertEquals("00", getTagData("RC", getMQmessageDocument));
			assertEquals(getBetweenTag(getMQmessageStr, "D"), changeCode(getBetweenTag(putMQmessageStr, "D")));

		} else {
			assertEquals("03", getTagData("RC", getMQmessageDocument));
		}
		assertEquals(putMQmessage.replyToQueueManagerName.trim(), getTagData("R_PVR", getMQmessageDocument));
		assertEquals(putMQmessage.replyToQueueName.trim(), getTagData("R_DST", getMQmessageDocument));

		int putSize = putMQmessageDocument.getElementsByTagName("TS").getLength();
		int getSize = getMQmessageDocument.getElementsByTagName("TS").getLength();

		for (int i = 0; i < putSize; i++) {
			for (TsAttribute t : TsAttribute.values()) {
				assertEquals(getTimestampName(i + 1, t.getTName(), putMQmessageDocument),
						getTimestampName(i + 1, t.getTName(), getMQmessageDocument));
			}
			assertEquals(getTimestampName(i + 1, putMQmessageDocument), getTimestampName(i + 1, getMQmessageDocument));
		}

		// TODO サイズチェック 正常4件
		if (request) {
			assertEquals(4, getSize - putSize);
		} else {
			assertEquals(3, getSize - putSize);
		}
		for (int i = putSize; i < getSize; i++) {

			for (TsAttribute t : TsAttribute.values()) {

				String getEqual = getTimestampName(i + 1, t.getTName(), getMQmessageDocument);

				switch (t) {
				case KBN:
					assertEquals(i <= putSize + 1 ? "1" : "2", getEqual);
					break;
				case LVL:
					assertEquals(i <= putSize || i == getSize - 1 ? "1" : "2", getEqual);
					break;
				case SVC:
					assertEquals(getXmlTag(toStringMQMessage(putMQmessage), "SERVICEID"), getEqual);
					break;
				case SVR:
// TODO qmgrName()で書くように全般修正
					if (request) {
						assertEquals(qmgrName(), getEqual);
					} else {
						assertEquals("RSHUBF", getEqual.substring(0, 6));
					}
/**
 * sss
 * RSHUBFX
 * RSHUBFX
 * RSHUBF
 */
					
					break;
				}
			}

			assertTrue(isYmd(getTimestampName(i + 1, getMQmessageDocument)));
		}

	}

	void lastCheckMqmd(MQMessage putMQmessage, MQMessage getMQmessage, boolean errQ, boolean request)
			throws IOException {

//TODO      項目単位　昔見たいの
		assertAll(() -> {
			if (request) {
				assertEquals(MQC.MQMT_REPLY, getMQmessage.messageType);
			} else {
				assertEquals(MQC.MQMT_REQUEST, getMQmessage.messageType);
			}
		}, () -> assertTrue(mqCheck(putMQmessage, getMQmessage)),
				() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmessage.format.trim()),
				() -> assertEquals(putMQmessage.encoding, getMQmessage.encoding), () -> {
					if (errQ) {
						assertEquals(MQC.MQEI_UNLIMITED, getMQmessage.expiry);
					} else {
						assertNotEquals(MQC.MQEI_UNLIMITED, getMQmessage.expiry);
					}
				}, () -> {
					if (errQ) {
						assertEquals(MQC.MQPER_PERSISTENT, getMQmessage.persistence);
					} else {
						assertEquals(MQC.MQPER_NOT_PERSISTENT, getMQmessage.persistence);
					}
				},
				// TODO getMQmessage.replyToQueueManagerName
				() -> assertEquals(qmgrName(), getMQmessage.replyToQueueManagerName.trim()),
				// TODO getMQmessage.replyToQueueName
				() -> {
					if (request) {
						assertEquals("", getMQmessage.replyToQueueName.trim());
					} else {
						assertEquals(QUEUE.QL_DH_REP.getQName(), getMQmessage.replyToQueueName.trim());
					}
				}, () -> assertEquals(getXmlTag(toStringMQMessage(putMQmessage), "SERVICEID"),
						getMQmessage.applicationIdData.trim()));

	}

// TODO booleanにする（フラグらへん）
	void lastCheck(MQMessage putMQmessage, MQMessage getMQmessage, Boolean errflg, Boolean requestflg)
			throws Exception {

		lastCheckMqmd(putMQmessage, getMQmessage, errflg, requestflg);
		lastCheckBody(putMQmessage, getMQmessage, requestflg);
	}
}
