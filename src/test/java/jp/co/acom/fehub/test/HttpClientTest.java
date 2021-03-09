package jp.co.acom.fehub.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	Map<String, String> checkflg = new HashMap<>();

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}

	@AfterEach
	void setAll() throws Exception {
		assertTrue(mqtoEmpty(getList()));
	}

	MQMessage setUpCreateMQ(String body, String outQueueName) throws Exception {
		MQMessage putMQmassage = createMQMessageRequest(body, outQueueName);
		putMQmassage.correlationId = getUnique24().getBytes();
		putMQmassage.applicationIdData = "SERVICEID";
		return putMQmassage;
	}

	void lastCheckBody(MQMessage putMQmassage, MQMessage getMQmassage, int flg)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		if (flg == (0 | 2)) {
			Document putMQmassageDocument = changeStringToDocument(toStringMQMessage(putMQmassage));
			Document getMQmassageDocument = changeStringToDocument(toStringMQMessage(getMQmassage));

			List<String> list = new ArrayList<>();
			list.add("TIMESTAMP");

			if (flg == 2) {
				list.add("RC");
				assertEquals("02", getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument));
			}
			assertTrue(check(putMQmassageDocument, getMQmassageDocument, list));
		}
	}

	void lastCheckMqmd(MQMessage putMQmassage, MQMessage getMQmassage, String getMQname, int flg) {

		assertAll(() -> assertTrue(mqCheck(putMQmassage, getMQmassage)),
				() -> assertEquals(putMQmassage.encoding, getMQmassage.encoding),
				() -> assertEquals(putMQmassage.applicationIdData, getMQmassage.applicationIdData.trim()), () -> {
					if (flg == 0) {
						assertEquals(MQC.MQFMT_STRING.trim(), getMQmassage.format.trim());
					}
					if (flg == (2 | 999)) {
						assertEquals(putMQmassage.format.trim(), getMQmassage.format.trim());
					}
				},

				() -> {
					if (flg == 0) {
						assertEquals(MQC.MQMT_REPLY, getMQmassage.messageType);
					}
					if (flg == (2 | 999)) {
						assertEquals(MQC.MQMT_REQUEST, getMQmassage.messageType);
					}
				}, () -> {
					if (getMQname == QUEUE.QL_DH_REP.getQName()) {
						assertNotEquals(MQC.MQEI_UNLIMITED, getMQmassage.expiry);
					}
					if (getMQname == QUEUE.QL_DH_ERR.getQName()) {
						assertEquals(MQC.MQEI_UNLIMITED, getMQmassage.expiry);
					}
				}, () -> {
					if (getMQname == QUEUE.QL_DH_REP.getQName()) {
						assertEquals(MQC.MQPER_NOT_PERSISTENT, getMQmassage.persistence);
					}
					if (getMQname == QUEUE.QL_DH_ERR.getQName()) {
						assertEquals(MQC.MQPER_PERSISTENT, getMQmassage.persistence);
					}
				});
	}

	void lastCheck(MQMessage putMQmassage, MQMessage getMQmassage, String getMQname, int flg) throws Exception {
		lastCheckMqmd(putMQmassage, getMQmassage, getMQname, flg);
		lastCheckBody(putMQmassage, getMQmassage, flg);

	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class MqXmlTestMainSotuStopAPI {

		@ParameterizedTest
		@MethodSource("test1and6_Per")
		@DisplayName("test1and6_Normal")
		void test1and6_Normal(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 0);
		}

		Stream<Arguments> test1and6_Per() throws Exception {
			return Stream.of(Arguments.of(createMQMAssageBody()),
					Arguments.of(createBreakeRc(createMQMAssageBody(), "")),
					Arguments.of(createBreakeRequestid(createMQMAssageBody(), "")));
		}

		@ParameterizedTest
		@DisplayName("test2_HTTPRequestError")
		@MethodSource("test2_Per")
		void test2_HTTPRequestError(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 2);
		}

		Stream<Arguments> test2_Per() throws Exception {
			return Stream.of(Arguments.of(createBreakeRc(createBreakeServiceid(createMQMAssageBody(), "S"), "01")),
					Arguments.of(createBreakeRc(createBreakeServiceid(createMQMAssageBody(), "S"), "")),
					Arguments.of(createBreakeServiceid(createMQMAssageBody(), "S")),
					Arguments.of(createBreakeServiceid(createMQMAssageBody(), "")));
		}

		@ParameterizedTest
		@DisplayName("test3_HTTPRequestErrorAndReplyDead")
		@MethodSource("test2_Per")
		void test3_HTTPRequestErrorAndReplyDead(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());
			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
				mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
			}
			MQMessage getMQmassage = mqGet(QUEUE.QL_DH_ERR.getQName());
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 2);
		}

		@ParameterizedTest
		@DisplayName("test4_ParseError")
		@MethodSource("test4_Per")
		void test4_ParseError(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
			lastCheckMqmd(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 999);
			assertEquals(toStringMQMessage(putMQmassage), toStringMQMessage(getMQmassage));
		}

		Stream<Arguments> test4_Per() throws Exception {
			return Stream.of(Arguments.of(createBreakeBody(createMQMAssageBody())),
					Arguments.of(createBreakeEndtag(createMQMAssageBody(), "APL_DATA")),
					Arguments.of(createBreakeEndtag(createMQMAssageBody(), "REQUESTID")),
					Arguments.of(createBreakeEndtag(createMQMAssageBody(), "SERVICEID")));
		}

		@ParameterizedTest
		@DisplayName("test5_HTTPRequestErrorAndReplyDeadAndDeadEnd")
		@MethodSource("test2_Per")
		void test5_HTTPRequestErrorAndReplyDeadAndDeadEnd(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());

			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				putDisabled(QUEUE.QL_DH_ERR.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
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
		@MethodSource("test4_Per")
		void test5_ParseErrorAndDeadEnd(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(createBreakeBody(createMQMAssageBody()), QUEUE.QL_DW_REP.getQName());
			try {
				putDisabled(QUEUE.QL_DH_ERR.getQName());

				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
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
			MQMessage putMQmassage = setUpCreateMQ((createBreakeServiceid(createMQMAssageBody(), "DF300")),
					QUEUE.QL_DW_REP.getQName());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
		}

		@ParameterizedTest
		@DisplayName("test7_HTTPResponseError")
		@MethodSource("test7_Per")
		void test7_HTTPTimeout_HTTPResponseError(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
		}

		Stream<Arguments> test7_Per() throws Exception {
			return Stream.of(Arguments.of(createBreakeServiceid(createMQMAssageBody(), "DF999")),
					Arguments.of(createBreakeServiceid(createMQMAssageBody(), "DF400")),
					Arguments.of(createBreakeServiceid(createMQMAssageBody(), "DF500")));
		}

		@ParameterizedTest
		@MethodSource("test1and6_Per")
		@DisplayName("test8_ReplyDead")
		void test8_ReplyDead(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());

			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
				mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
			}
			MQMessage getMQmassage = mqGet(QUEUE.QL_DH_ERR.getQName());
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 0);
		}

		@ParameterizedTest
		@MethodSource("test1and6_Per")
		@DisplayName("test9_ReplyDeadAndDeadEnd")
		void test9_ReplyDeadAndDeadEnd(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());

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
		@DisplayName("Test6_Normal_NonXml")
		protected void Test6_Normal_NonXml() throws Exception {
			MQMessage putMQmassage = setUpCreateMQ((createBreakeServiceid(createMQMAssageBody(), "DF800")),
					QUEUE.QL_DW_REP.getQName());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWait(QUEUE.QL_DW_REP.getQName());
			lastCheckMqmd(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 0);
			assertEquals("aaaaaa", toStringMQMessage(getMQmassage));

		}

		@ParameterizedTest
		@MethodSource("test1and6_Per")
		@DisplayName("test1and6_PERSISTENCE_FORMAT")
		void test1and6_PERSISTENCE_FORMAT(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());
			putMQmassage.persistence = MQC.MQPER_PERSISTENT;
			putMQmassage.format = MQC.MQFMT_NONE;
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 0);
		}

	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class MqXmlTestMainSotuStopAPI_手動 {

		@Test
		@DisplayName("test_手動テスト")
		void test_手動テスト() throws Exception {

			MQMessage putMQmassage = setUpCreateMQ(createMQMAssageBody(), QUEUE.QL_DW_REP.getQName());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);

			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 2);
		}

		@Test
		@DisplayName("test_手動テスト_ReplyDead")
		void test_手動テスト_ReplyDead() throws Exception {

			MQMessage putMQmassage = setUpCreateMQ(createMQMAssageBody(), QUEUE.QL_DW_REP.getQName());
			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
				mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
			}
			MQMessage getMQmassage = mqGet(QUEUE.QL_DH_ERR.getQName());
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 2);
		}

		@Test
		@DisplayName("test_手動テスト_ReplyDeadAndDeadEnd")
		void test_手動テスト_ReplyDeadAndDeadEnd() throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(createMQMAssageBody(), QUEUE.QL_DW_REP.getQName());

			try {
				putDisabled(QUEUE.QL_DW_REP.getQName());
				putDisabled(QUEUE.QL_DH_ERR.getQName());
				mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			} finally {
				putEnabled(QUEUE.QL_DW_REP.getQName());
				putEnabled(QUEUE.QL_DH_ERR.getQName());
			}
			MQMessage getMQmassage = mqGetWait(QUEUE.SYSTEM_ADMIN_EVENT.getQName());
			assertNotNull(getMQmassage);
		}

	}

}
