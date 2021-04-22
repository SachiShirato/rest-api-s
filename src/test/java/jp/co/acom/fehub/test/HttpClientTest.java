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

import com.example.api.ItemRestController;
import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01Test;
import jp.co.acom.fehub.mq.QUEUE;
import jp.co.acom.fehub.xml.XMLCENTERTest;

public class HttpClientTest implements QMFH01Test, XMLCENTERTest {

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
		putMQmessage.applicationIdData = "SERVICEID";

		return putMQmessage;
	}

	void lastCheckBody(MQMessage putMQmessage, MQMessage getMQmessage, boolean request)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		Document putMQmessageDocument = changeStringToDocument(
				toStringMQMessage(putMQmessage).replaceAll(System.lineSeparator(), "").replaceAll("\t", ""));
		Document getMQmessageDocument = changeStringToDocument(
				toStringMQMessage(getMQmessage).replaceAll(System.lineSeparator(), "").replaceAll("\t", ""));

		List<String> list = new ArrayList<>();
		list.add("REQ_PARM");

		if (request) {
			list.add("RC");
			assertEquals("02", (getXmlEvaluate(xmlGlbPath("RC"), getMQmessageDocument)));
		}

		assertTrue(check(putMQmessageDocument, getMQmessageDocument, list));
	}

	void lastCheckMqmd(MQMessage putMQmessage, MQMessage getMQmessage, boolean errQ, boolean request) {

		assertAll(

				() -> {
					if (request) {
						assertEquals(putMQmessage.messageType, getMQmessage.messageType);
					} else {
						assertEquals(MQC.MQMT_REPLY, getMQmessage.messageType);
					}
				},

				() -> {
					if (request) {
						assertEquals(putMQmessage.format.trim(), getMQmessage.format.trim());
					} else {
						assertEquals(MQC.MQFMT_STRING.trim(), getMQmessage.format.trim());
					}
				},

				() -> assertTrue(mqCheck(putMQmessage, getMQmessage)),

				() -> assertEquals(putMQmessage.encoding, getMQmessage.encoding),

				() -> {
					if (errQ) {
						assertEquals(MQC.MQEI_UNLIMITED, getMQmessage.expiry);
					} else {
						assertNotEquals(MQC.MQEI_UNLIMITED, getMQmessage.expiry);
					}
				},

				() -> {
					if (errQ) {

						assertEquals(MQC.MQPER_PERSISTENT, getMQmessage.persistence);

					} else {

						if (request) {
							assertEquals(putMQmessage.persistence, getMQmessage.persistence);
						} else {
							assertEquals(MQC.MQPER_NOT_PERSISTENT, getMQmessage.persistence);
						}
					}
				},

				() -> assertEquals(putMQmessage.applicationIdData, getMQmessage.applicationIdData.trim())

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
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

			lastCheck(putMQmessage, mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmessage.correlationId),
					QUEUE.QL_DW_REP.getQName(), 1);
		}

		Stream<Arguments> params_Normal() throws Exception {

			return Stream.of(Arguments.of(createMQMessageBody()), Arguments.of(setRc(createMQMessageBody(), "")),
					Arguments.of(setRequestid(createMQMessageBody(), "")));
		}

		@ParameterizedTest
		@DisplayName("test2_ServiceidError")
		@MethodSource("params_ServiceidError")
		void test2_ServiceidError(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

			lastCheck(putMQmessage, mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmessage.correlationId),
					QUEUE.QL_DW_REP.getQName(), 0);
		}

		Stream<Arguments> params_ServiceidError() throws Exception {

			return Stream.of(Arguments.of(setRc(setServiceid(createMQMessageBody(), "S"), "01")),
					Arguments.of(setRc(setServiceid(createMQMessageBody(), "S"), "")),
					Arguments.of(setServiceid(createMQMessageBody(), "S")),
					Arguments.of(setServiceid(createMQMessageBody(), "")));
		}

		@ParameterizedTest
		@DisplayName("test3_ServiceidErrorAndReplyDead")
		@MethodSource("params_ServiceidError")
		void test3_ServiceidErrorAndReplyDead(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);

			try {

				putDisabled(QUEUE.QL_DW_REP.getQName());

				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

				lastCheck(putMQmessage, mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmessage.correlationId),
						QUEUE.QL_DH_ERR.getQName(), 0);

			} finally {

				putEnabled(QUEUE.QL_DW_REP.getQName());
			}
		}

		@ParameterizedTest
		@DisplayName("test4_ParseError")
		@MethodSource("params_ParseError")
		void test4_ParseError(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

			MQMessage getMQmessage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmessage.correlationId);
			lastCheckMqmd(putMQmessage, getMQmessage, true, true);
			assertEquals(toStringMQMessage(putMQmessage), toStringMQMessage(getMQmessage));
		}

		Stream<Arguments> params_ParseError() throws Exception {

			return Stream.of(Arguments.of(createBreakeBody(createMQMessageBody())),
					Arguments.of(createBreakeEndtag(createMQMessageBody(), "APL_DATA")),
					Arguments.of(createBreakeEndtag(createMQMessageBody(), "REQUESTID")),
					Arguments.of(createBreakeEndtag(createMQMessageBody(), "SERVICEID")));
		}

		@ParameterizedTest
		@DisplayName("test5_ServiceidErrorAndReplyDeadAndDeadEnd")
		@MethodSource("params_ServiceidError")
		void test5_ServiceidErrorAndReplyDeadAndDeadEnd(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);

			try {

				putDisabled(QUEUE.QL_DW_REP.getQName());
				putDisabled(QUEUE.QL_DH_ERR.getQName());

				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

				assertNotNull(mqGetWait(QUEUE.SYSTEM_ADMIN_EVENT.getQName()));

			} finally {

				putEnabled(QUEUE.QL_DW_REP.getQName());
				putEnabled(QUEUE.QL_DH_ERR.getQName());
			}
		}

		@ParameterizedTest
		@DisplayName("test5_ParseErrorAndDeadEnd")
		@MethodSource("params_ParseError")
		void test5_ParseErrorAndDeadEnd(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(createBreakeBody(createMQMessageBody()));

			try {

				putDisabled(QUEUE.QL_DH_ERR.getQName());

				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

				assertNotNull(mqGetWait(QUEUE.SYSTEM_ADMIN_EVENT.getQName()));

			} finally {

				putEnabled(QUEUE.QL_DH_ERR.getQName());
			}
		}

		@Test
		@DisplayName("test7_HTTPTimeout")
		protected void test7_HTTPTimeout() throws Exception {

			MQMessage putMQmessage = setUpCreateMQ((setServiceid(createMQMessageBody(), "DF300")));
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);
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

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test8_ReplyDead")
		void test8_ReplyDead(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);

			try {

				putDisabled(QUEUE.QL_DW_REP.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

				lastCheck(putMQmessage, mqGetWaitMsgid(QUEUE.QL_DH_ERR.getQName(), putMQmessage.correlationId),
						QUEUE.QL_DH_ERR.getQName(), 1);

			} finally {

				putEnabled(QUEUE.QL_DW_REP.getQName());
			}
		}

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test9_ReplyDeadAndDeadEnd")
		void test9_ReplyDeadAndDeadEnd(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);

			try {

				putDisabled(QUEUE.QL_DW_REP.getQName());
				putDisabled(QUEUE.QL_DH_ERR.getQName());

				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

			} finally {

				putEnabled(QUEUE.QL_DW_REP.getQName());
				putEnabled(QUEUE.QL_DH_ERR.getQName());
			}
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

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test1and6_PERSISTENCE_FORMAT")
		void test1and6_PERSISTENCE_FORMAT(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);
			putMQmessage.persistence = MQC.MQPER_PERSISTENT;
			putMQmessage.format = MQC.MQFMT_NONE;
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

			lastCheck(putMQmessage, mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmessage.correlationId),
					QUEUE.QL_DW_REP.getQName(), 1);
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

			lastCheck(putMQmessage, mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmessage.correlationId),
					QUEUE.QL_DW_REP.getQName(), 0);
		}

		@Test
		@DisplayName("test3_HTTPRequestErrorAndReplyDead")
		void test3_HTTPRequestErrorAndReplyDead() throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(createMQMessageBody());

			try {

				putDisabled(QUEUE.QL_DW_REP.getQName());

				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

				lastCheck(putMQmessage, mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmessage.correlationId),
						QUEUE.QL_DH_ERR.getQName(), 0);

			} finally {

				putEnabled(QUEUE.QL_DW_REP.getQName());
			}
		}

		@Test
		@DisplayName("test5_HTTPRequestErrorAndReplyDeadAndDeadEnd")
		void test5_HTTPRequestErrorAndReplyDeadAndDeadEnd() throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(createMQMessageBody());

			try {

				putDisabled(QUEUE.QL_DW_REP.getQName());
				putDisabled(QUEUE.QL_DH_ERR.getQName());

				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

				assertNotNull(mqGetWait(QUEUE.SYSTEM_ADMIN_EVENT.getQName()));

			} finally {

				putEnabled(QUEUE.QL_DW_REP.getQName());
				putEnabled(QUEUE.QL_DH_ERR.getQName());
			}
		}
	}
}
