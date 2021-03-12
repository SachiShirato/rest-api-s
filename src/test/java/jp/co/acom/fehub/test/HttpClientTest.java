package jp.co.acom.fehub.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01Test;
import jp.co.acom.fehub.mq.QUEUE;
import jp.co.acom.fehub.xml.XMLCENTERTest;

public class HttpClientTest implements QMFH01Test, XMLCENTERTest {

	// TODO 不要

	@BeforeEach
	// TODO setUp
	void setUp() throws Exception {
		mqtoEmpty(getList());
	}

	@AfterEach
	// TODO tearDown
	void tearDown() throws Exception {
		assertTrue(mqtoEmpty(getList()));
	}

	// TODO replyToQ、常に一緒
	MQMessage setUpCreateMQ(String body) throws Exception {

		MQMessage putMQmassage = createMQMessageRequest(body);

		putMQmassage.correlationId = getUnique24().getBytes();
		putMQmassage.applicationIdData = "SERVICEID";

		return putMQmassage;
	}

	// TODO boolean reply
	void lastCheckBody(MQMessage putMQmassage, MQMessage getMQmassage, int flg)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		// TODO いらない
		if (flg == (0 | 2)) {

			Document putMQmassageDocument = changeStringToDocument(toStringMQMessage(putMQmassage));
			Document getMQmassageDocument = changeStringToDocument(toStringMQMessage(getMQmassage));

			List<String> list = new ArrayList<>();
			// TODO いらない
			list.add("TIMESTAMP");

			if (flg == 2) {
				list.add("RC");
				assertEquals("02", getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument));
			}
			assertTrue(check(putMQmassageDocument, getMQmassageDocument, list));
		}
	}

	// TODO boolean errQ, boolean reply
	void lastCheckMqmd(MQMessage putMQmassage, MQMessage getMQmassage, String getMQname, int flg) {

		assertAll(

				() -> {
					if (flg == 0) {
						assertEquals(MQC.MQMT_REPLY, getMQmassage.messageType);
					}

					// TODO 同じ
					if (flg == (2 | 999)) {
						assertEquals(MQC.MQMT_REQUEST, getMQmassage.messageType);
					}
				},

				() -> {
					if (flg == 0) {
						assertEquals(MQC.MQFMT_STRING.trim(), getMQmassage.format.trim());
					}
					if (flg == (2 | 999)) {
						assertEquals(putMQmassage.format.trim(), getMQmassage.format.trim());
					}
				},

				// TODO mqCheck
				() -> assertTrue(mqCheck(putMQmassage, getMQmassage)),

				() -> assertEquals(putMQmassage.encoding, getMQmassage.encoding),

				() -> {
					if (getMQname == QUEUE.QL_DH_REP.getQName()) {
						assertNotEquals(MQC.MQEI_UNLIMITED, getMQmassage.expiry);
					}
					if (getMQname == QUEUE.QL_DH_ERR.getQName()) {
						assertEquals(MQC.MQEI_UNLIMITED, getMQmassage.expiry);
					}
				},

				() -> {
					// TODO repQ & reqest 同じ
					if (getMQname == QUEUE.QL_DH_REP.getQName()) {
						assertEquals(MQC.MQPER_NOT_PERSISTENT, getMQmassage.persistence);
					}
					if (getMQname == QUEUE.QL_DH_ERR.getQName()) {
						assertEquals(MQC.MQPER_PERSISTENT, getMQmassage.persistence);
					}
				},

				() -> assertEquals(putMQmassage.applicationIdData, getMQmassage.applicationIdData.trim())

		);
	}

	void lastCheck(MQMessage putMQmassage, MQMessage getMQmassage, String getMQname, int flg) throws Exception {

		lastCheckMqmd(putMQmassage, getMQmassage, getMQname, flg);
		lastCheckBody(putMQmassage, getMQmassage, flg);
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	// TODO StartingRizaTest
	class StartingRizaTest {

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test1and6_Normal")
		void test1and6_Normal(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str);
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 0);
		}

		// TODO params_Normal
		Stream<Arguments> params_Normal() throws Exception {
			return Stream.of(Arguments.of(createMQMessageBody()), Arguments.of(setTag(createMQMessageBody(), "RC", "")),
					Arguments.of(setTag(createMQMessageBody(), "REQUESTID", "")));
		}

		@ParameterizedTest
		@DisplayName("test2_ServiceidError")
		@MethodSource("params_ServiceidError")
		// TODO test2_ServiceidError
		void test2_ServiceidError(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str);
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 2);
		}

		// TODO params_ServiceidError
		Stream<Arguments> params_ServiceidError() throws Exception {
			return Stream.of(Arguments.of(setTag(setTag(createMQMessageBody(), "SERVICEID", "S"), "RC", "01")),
					Arguments.of(setTag(setTag(createMQMessageBody(), "SERVICEID", "S"), "RC", "")),
					Arguments.of(setTag(createMQMessageBody(), "SERVICEID", "S")),
					Arguments.of(setTag(createMQMessageBody(), "SERVICEID", "")));
		}

		@ParameterizedTest
		@DisplayName("test3_ServiceidErrorAndReplyDead")
		@MethodSource("params_ServiceidError")
		// TODO test3_ServiceidErrorAndReplyDead
		void test3_ServiceidErrorAndReplyDead(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str);
			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
			}
			// TODO idで拾いたい
			MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 2);
		}

		@ParameterizedTest
		@DisplayName("test4_ParseError")
		@MethodSource("params_ParseError")
		void test4_ParseError(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str);
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
			lastCheckMqmd(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 999);
			assertEquals(toStringMQMessage(putMQmassage), toStringMQMessage(getMQmassage));
		}

		// TODO params_ParseError
		Stream<Arguments> params_ParseError() throws Exception {
			return Stream.of(Arguments.of(createBreakeBody(createMQMessageBody())),
					Arguments.of(createBreakeEndtag(createMQMessageBody(), "APL_DATA")),
					Arguments.of(createBreakeEndtag(createMQMessageBody(), "REQUESTID")),
					Arguments.of(createBreakeEndtag(createMQMessageBody(), "SERVICEID")));
		}

		@ParameterizedTest
		@DisplayName("test5_ServiceidErrorAndReplyDeadAndDeadEnd")
		@MethodSource("params_ServiceidError")
		// TODO test5_ServiceidErrorAndReplyDeadAndDeadEnd
		void test5_ServiceidErrorAndReplyDeadAndDeadEnd(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str);

			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				putDisabled(QUEUE.QL_DH_ERR.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
				// TODO いらない (白)ないとERRへ入る waitしないと、enabledが先に動く？
				mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
				putEnabled(QUEUE.QL_DH_ERR.getQName());

			}
			MQMessage getMQmassage = mqGetWait(QUEUE.SYSTEM_ADMIN_EVENT.getQName());
			assertNotNull(getMQmassage);

		}

		@ParameterizedTest
		@DisplayName("test5_ParseErrorAndDeadEnd")
		@MethodSource("params_ParseError")
		void test5_ParseErrorAndDeadEnd(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(createBreakeBody(createMQMessageBody()));
			try {
				putDisabled(QUEUE.QL_DH_ERR.getQName());

				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
				// TODO いらない (白)ないとQA.DH.DFへ入る waitしないと、enabledが先に動く？
				mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			} finally {
				putEnabled(QUEUE.QL_DH_ERR.getQName());
			}
			MQMessage getMQmassage = mqGet(QUEUE.SYSTEM_ADMIN_EVENT.getQName());
			assertNotNull(getMQmassage);
		}

		@Test
		@DisplayName("test7_HTTPTimeout")
		protected void test7_HTTPTimeout() throws Exception {
			MQMessage putMQmassage = setUpCreateMQ((setTag(createMQMessageBody(), "SERVICEID", "DF300")));
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
		}

		@ParameterizedTest
		@DisplayName("test7_HTTPResponseError")
		@MethodSource("params_HTTPResponseError")
		// TODO test7_HTTPResponseError
		void test7_HTTPResponseError(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str);
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			// TODO いらない
		}

		// TODO params_HTTPResponseError
		Stream<Arguments> params_HTTPResponseError() throws Exception {
			return Stream.of(
					// TODO 404?
					Arguments.of(setTag(createMQMessageBody(), "SERVICEID", "DF999")),
					Arguments.of(setTag(createMQMessageBody(), "SERVICEID", "DF400")),
					Arguments.of(setTag(createMQMessageBody(), "SERVICEID", "DF500")));
		}

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test8_ReplyDead")
		void test8_ReplyDead(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str);

			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
				// TODO いらない (白)なくても大丈夫だが、他とそろえるなら残？ たぶんコンディションで結果がぶれる
//				mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
			}
			// (白)mqWaitから修正
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 0);
		}

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test9_ReplyDeadAndDeadEnd")
		void test9_ReplyDeadAndDeadEnd(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str);

			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				putDisabled(QUEUE.QL_DH_ERR.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
				putEnabled(QUEUE.QL_DH_ERR.getQName());
			}
		}

		@Test
		@DisplayName("test1and6_NonXml")
		// TODO test1and6_NonXml
		protected void test1and6_NonXml() throws Exception {
			MQMessage putMQmassage = setUpCreateMQ((setTag(createMQMessageBody(), "SERVICEID", "DF800")));
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			// TODO (白)GetWait()から修正
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			lastCheckMqmd(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 0);
			assertEquals("aaaaaa", toStringMQMessage(getMQmassage));

		}

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test1and6_PERSISTENCE_FORMAT")
		void test1and6_PERSISTENCE_FORMAT(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str);
			putMQmassage.persistence = MQC.MQPER_PERSISTENT;
			putMQmassage.format = MQC.MQFMT_NONE;
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 0);
		}

	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	// TODO StoppedRizaTest
	class StoppedRizaTest {

		@Test
		// TODO test2_HTTPRequestError
		@DisplayName("test2_HTTPRequestError")
		void test2_HTTPRequestError() throws Exception {

			MQMessage putMQmassage = setUpCreateMQ(createMQMessageBody());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 2);
		}

		@Test
		// TODO test3_HTTPRequestErrorAndReplyDead
		@DisplayName("test3_HTTPRequestErrorAndReplyDead")
		void test3_HTTPRequestErrorAndReplyDead() throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(createMQMessageBody());
			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
				// TODO いらない (白)なくても大丈夫だが、そろえるなら残
//				mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
			}

			// TODO idで拾いたい
			MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 2);
		}

		@Test
		// TODO test5_HTTPRequestErrorAndReplyDeadAndDeadEnd
		@DisplayName("test5_HTTPRequestErrorAndReplyDeadAndDeadEnd")
		void test5_HTTPRequestErrorAndReplyDeadAndDeadEnd() throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(createMQMessageBody());
			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				putDisabled(QUEUE.QL_DH_ERR.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
				// TODO (白)↓元々なかったが、ないとERRに入る
				mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
				putEnabled(QUEUE.QL_DH_ERR.getQName());
			}
			MQMessage getMQmassage = mqGetWait(QUEUE.SYSTEM_ADMIN_EVENT.getQName());
			assertNotNull(getMQmassage);
		}
	}
}
