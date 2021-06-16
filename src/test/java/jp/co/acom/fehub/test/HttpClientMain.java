package jp.co.acom.fehub.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.w3c.dom.Document;

import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01;
import jp.co.acom.fehub.mq.QUEUE;
import jp.co.acom.fehub.xml.XMLCenter;

public class HttpClientMain implements QMFH01, XMLCenter {

	@BeforeEach
	void setUp() throws Exception {

		mqtoEmpty(getList());
	}

	@AfterEach
	void tearDown() throws Exception {

		assertTrue(mqtoEmpty(getList()));
	}

	MQMessage setUpCreateMQ(String body) throws Exception {
		// TODO putMQmessageは不要 済
		return createMQMessageRequest(body, GET_QUEUE_NAME);
	}

	// TODO 不要になりました。 （白 済）
//	void checkBody_requestError(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
//
//		lastCheckBody(putMQmessage, getMQmessage, true);
//	}

	// TODO request → reply (白 済)
	// TODO checkBody/checkBody_requestError (白 済)
	// TODO throws多いので1つにまとめましょう。 済
	void lastCheckBody(MQMessage putMQmessage, MQMessage getMQmessage, boolean reply) throws Exception {

		// TODO replaceAllなので1発で置換できます。呼び出し先で実施していいかも (白 済)
		Document putMQmessageDocument = changeStringToDocument(
				messageToString(putMQmessage).replaceAll("[" + System.lineSeparator() + "\t]", ""));
		Document getMQmessageDocument = changeStringToDocument(
				messageToString(getMQmessage).replaceAll("[" + System.lineSeparator() + "\t]", ""));

		// TODO 1行で書きましょう。Arrays.asList(() 上のリストを下に追加 (白 済)
		assertTrue(check(putMQmessageDocument, getMQmessageDocument, Arrays.asList("REPLY", "TIMESTAMP", "RC", "D")));

		int putSize = putMQmessageDocument.getElementsByTagName("TS").getLength();
		int getSize = getMQmessageDocument.getElementsByTagName("TS").getLength();

		for (int i = 0; i < putSize; i++) {

			for (TsAttribute t : TsAttribute.values())
				assertEquals(getTimestampName(i + 1, t.getTName(), putMQmessageDocument),
						getTimestampName(i + 1, t.getTName(), getMQmessageDocument));

			assertEquals(getTimestampName(i + 1, putMQmessageDocument), getTimestampName(i + 1, getMQmessageDocument));
		}
//TODO 追加HC用（白）
		if ((getSize - putSize) != 0) {
			assertEquals(putMQmessage.replyToQueueManagerName.trim(), getTagData("R_PVR", getMQmessageDocument));
			assertEquals(putMQmessage.replyToQueueName.trim(), getTagData("R_DST", getMQmessageDocument));
			// TODO 1行で書きましょう ?のやつ (白 済)
			assertEquals(reply ? 4 : 3, getSize - putSize);

			for (int i = putSize; i < getSize; i++) {

				for (TsAttribute t : TsAttribute.values()) {

					String getEqual = getTimestampName(i + 1, t.getTName(), getMQmessageDocument);

					switch (t) {

					case KBN:
						assertEquals(i <= putSize + 1 ? "1" : "2", getEqual);
						break;

					case LVL:
						// TODO i == putSize
						assertEquals(i == putSize || i == getSize - 1 ? "1" : "2", getEqual);
						break;

					case SVC:
						assertEquals(getXmlTag(messageToString(putMQmessage), "SERVICEID"), getEqual);
						break;

					case SVR:
						// TODO 1行で書きましょう （白 済）
						assertEquals(reply ? qmgrName() : "RSHUBF", getEqual.substring(0, 6));
						break;
					}
				}

				assertTrue(isYmd(getTimestampName(i + 1, getMQmessageDocument)));
			}

			// TODO 1行で書きましょう。Arrays.asList(() （白 質問）
			assertEquals(reply ? "00" : "03", getTagData("RC", getMQmessageDocument));

			if (reply) {
				assertEquals(getBetweenTag(getMQmessageDocument, "D"),
						changeCode(getBetweenTag(putMQmessageDocument, "D")));
			}

			// TODO 追加HC用（白）
		} else {
			if (reply) {
				assertEquals(getXmlEvaluate(xmlGlbPath("RC"), putMQmessageDocument),
						getXmlEvaluate(xmlGlbPath("RC"), getMQmessageDocument));
			} else {
				assertEquals("02", getXmlEvaluate(xmlGlbPath("RC"), getMQmessageDocument));

			}
		}
	}

	// TODO request → reply （白 済）
	// TODO checkMqmd/checkMqmd_requestError
	// TODO checkMqmd_replyParseError/checkMqmd_requestParseError
	void lastCheckMqmd(MQMessage putMQmessage, MQMessage getMQmessage, boolean errQ, boolean reply) throws IOException {

		assertAll(

				// TODO 1行で書きましょう
				() -> assertEquals(reply ? MQC.MQMT_REPLY : MQC.MQMT_REQUEST, getMQmessage.messageType),
				() -> assertTrue(mqCheck(putMQmessage, getMQmessage)),
				() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmessage.format.trim()),
				() -> assertEquals(putMQmessage.encoding, getMQmessage.encoding),

				// TODO 1行で書きましょう (白 済)
				() -> {
					if (errQ) {
						assertEquals(MQC.MQEI_UNLIMITED, getMQmessage.expiry);
					} else {
						assertNotEquals(MQC.MQEI_UNLIMITED, getMQmessage.expiry);
					}
				},

				// TODO 1行で書きましょう 済
				() -> assertEquals(errQ ? MQC.MQPER_PERSISTENT : MQC.MQPER_NOT_PERSISTENT, getMQmessage.persistence),
				() -> assertEquals(qmgrName(), getMQmessage.replyToQueueManagerName.trim()),

				// TODO 追加HC用（白）  みかん　上合体したい
				() -> {
					if (messageToString(putMQmessage).equals(messageToString(getMQmessage))) {
						assertEquals(reply ? "" : QUEUE.QL_DW_REP.getQName(), getMQmessage.replyToQueueName.trim());
						assertEquals("", getMQmessage.applicationIdData.trim());

					} else if (("S".equals(getXmlTag(messageToString(putMQmessage), "SERVICEID")))||("".equals(getMQmessage.applicationIdData.trim()))) {
						assertEquals(reply ? "" : QUEUE.QL_DW_REP.getQName(), getMQmessage.replyToQueueName.trim());
						assertEquals("", getMQmessage.applicationIdData.trim());
					} else {

						// TODO 1行で書きましょう 済
						assertEquals(reply ? "" : QUEUE.QL_DH_REP.getQName(), getMQmessage.replyToQueueName.trim());
						assertEquals(getXmlTag(messageToString(putMQmessage), "SERVICEID"),
								getMQmessage.applicationIdData.trim());
					}
				});
	}

	// TODO Boolean → boolean errQ, boolean reply (白 済)
	// TODO checkAll/checkAll_requestError
	// TODO checkAll_replyParseError/checkAll_requestParseError
	void lastCheck(MQMessage putMQmessage, MQMessage getMQmessage, boolean errQ, boolean reply) throws Exception {
		lastCheckMqmd(putMQmessage, getMQmessage, errQ, reply);
		lastCheckBody(putMQmessage, getMQmessage, reply);
	}

	void checkAll_replyParseError(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheck(putMQmessage, getMQmessage, true, true);
	}

	void checkAll_requestParseError(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheck(putMQmessage, getMQmessage, true, false);
	}

	void checkAll_reply(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheck(putMQmessage, getMQmessage, false, true);
	}

	void checkAll_request(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheck(putMQmessage, getMQmessage, false, false);
	}

	void checkMqmd_replyParseError(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheckMqmd(putMQmessage, getMQmessage, true, true);
//		TT MQMD
	}

	void checkMqmd_requestParseError(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheckMqmd(putMQmessage, getMQmessage, true, false);
//		TF MQMD
	}

	void checkMqmd_reply(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheckMqmd(putMQmessage, getMQmessage, false, true);
//		FT MQMD
	}

	void checkMqmd_request(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheckMqmd(putMQmessage, getMQmessage, false, false);
//		FF MQMD
	}

	void checkBody_replyParseError(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
//		TT BODY 
	}

	void checkBody_requestParseError(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
//		TF BODY 
	}

	void checkBody_reply(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheckBody(putMQmessage, getMQmessage, true);
//		FT BODY 
	}

	void checkBody_request(MQMessage putMQmessage, MQMessage getMQmessage) throws Exception {
		lastCheckBody(putMQmessage, getMQmessage, false);
//		FF BODY 
	}

}
