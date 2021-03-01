package jp.co.acom.fehub.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;

import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01Test;
import jp.co.acom.fehub.mq.QUEUE;
import jp.co.acom.fehub.xml.XMLCENTERTest;

public class HttpClientTest implements QMFH01Test, XMLCENTERTest {

	public static String path = "/ts3.xml";
	Map<String, String> checkflg = new HashMap<>();

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}

	MQMessage setUpCreateMQ(String body, String outQueueName) throws Exception {
		return setUpCreateMQ(body, outQueueName, "SERVICEID");
	}

	MQMessage setUpCreateMQ(String body, String outQueueName, String applicationIdData) throws Exception {

		MQMessage putMQmassage = createMQMessage(body);
		putMQmassage.replyToQueueManagerName = qmgrname();
		putMQmassage.replyToQueueName = outQueueName;
		putMQmassage.correlationId = getUnique24().getBytes();
		putMQmassage.applicationIdData = applicationIdData;
		return putMQmassage;
	}

	void lastCheck(MQMessage putMQmassage, MQMessage getMQmassage, String getMQname, int flg) throws Exception {

		if (getMQname == QUEUE.QL_DH_ERR.getQName()) {
			assertAll(() -> assertEquals(MQC.MQEI_UNLIMITED, getMQmassage.expiry),
					() -> assertEquals(MQC.MQPER_PERSISTENT, getMQmassage.persistence));
			checkflg.put("expiry", "1");
			checkflg.put("persistence", "1");

		} else {
			assertAll(
					() -> assertNotEquals(toStringMQMessage(putMQmassage).replaceAll(System.lineSeparator(), ""),
							toStringMQMessage(getMQmassage).replaceAll(System.lineSeparator(), "")),
					() -> assertNotEquals(MQC.MQEI_UNLIMITED, getMQmassage.expiry), () -> {
						if (flg == 0) {
							assertEquals(MQC.MQPER_NOT_PERSISTENT, getMQmassage.persistence);
							checkflg.put("persistence", "1");
						} else {
							assertEquals(putMQmassage.persistence, getMQmassage.persistence);
							checkflg.put("persistence", "1");
						}
					});
			checkflg.put("body", "1");
			checkflg.put("expiry", "1");

		}

		switch (flg) {

		case 0:
			assertAll(
					() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.correlationId),
							(DatatypeConverter.printHexBinary(getMQmassage.messageId))),
					() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmassage.format.trim()),
					() -> assertEquals(MQC.MQMT_REPLY, getMQmassage.messageType));

			checkflg.put("id", "1");
			checkflg.put("format", "1");
			checkflg.put("messageType", "1");

			break;
		case 2:
			Document putMQmassageDocument = changeStringToDocument(toStringMQMessage(putMQmassage));
			Document getMQmassageDocument = changeStringToDocument(toStringMQMessage(getMQmassage));
			String getRc = getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument);

			List<String> list = new ArrayList<>();
			list.add("TIMESTAMP");
			list.add("RC");

			assertAll(() -> assertEquals("02", getRc),
					() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.correlationId),
							(DatatypeConverter.printHexBinary(getMQmassage.messageId))),
					() -> assertTrue(check(putMQmassageDocument, getMQmassageDocument, list)),
					() -> assertEquals(putMQmassage.format.trim(), getMQmassage.format.trim()),
					() -> assertEquals(MQC.MQMT_REQUEST, getMQmassage.messageType));
			checkflg.put("body", "1");
			checkflg.put("id", "1");
			checkflg.put("messageType", "1");
			checkflg.put("format", "1");
			checkflg.put("rc", "1");
			break;
		}

		assertAll(() -> assertTrue(mqCheck(putMQmassage, getMQmassage)),
				() -> assertEquals(putMQmassage.encoding, getMQmassage.encoding),
				() -> assertEquals(putMQmassage.applicationIdData, getMQmassage.applicationIdData.trim()));

		assertAll(() -> assertEquals("1", checkflg.get("expiry")), () -> assertEquals("1", checkflg.get("format")),
				() -> assertEquals("1", checkflg.get("persistence")),
				() -> assertEquals("1", checkflg.get("messageType")), () -> assertEquals("1", checkflg.get("id")));

	}

	void lastChack(MQMessage putMQmassage, MQMessage getMQmassage) throws Exception {
		assertAll(

				() -> assertTrue(mqCheck(putMQmassage, getMQmassage)),
				() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmassage.format.trim()),
				() -> assertEquals(putMQmassage.applicationIdData, getMQmassage.applicationIdData.trim()));
	}

	void lastCheck800(MQMessage putMQmassage, MQMessage getMQmassage) throws Exception {
		assertAll(() -> assertEquals("aaaaaa", toStringMQMessage(getMQmassage)),
				() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.correlationId),
						(DatatypeConverter.printHexBinary(getMQmassage.messageId))),
				() -> assertNotEquals(MQC.MQEI_UNLIMITED, getMQmassage.expiry),
				() -> assertEquals(MQC.MQPER_NOT_PERSISTENT, getMQmassage.persistence),
				() -> assertEquals(MQC.MQMT_REPLY, getMQmassage.messageType));
		lastChack(putMQmassage, getMQmassage);
	}

	void lastCheck999(MQMessage putMQmassage, MQMessage getMQmassage) throws Exception {
		assertAll(() -> assertEquals(MQC.MQEI_UNLIMITED, getMQmassage.expiry),
				() -> assertEquals(MQC.MQPER_PERSISTENT, getMQmassage.persistence),
				() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.messageId),
						DatatypeConverter.printHexBinary(getMQmassage.messageId)),
				() -> assertEquals(MQC.MQMT_REQUEST, getMQmassage.messageType),
				() -> assertEquals(toStringMQMessage(putMQmassage), toStringMQMessage(getMQmassage)));
		lastChack(putMQmassage, getMQmassage);

	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class RRR {

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
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName(), "DF200");
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
			lastCheck999(putMQmassage, getMQmassage);
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
					QUEUE.QL_DW_REP.getQName(), "DF300");
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
		@DisplayName("test_DF800")
		protected void test_DF800() throws Exception {
			MQMessage putMQmassage = setUpCreateMQ((createBreakeServiceid(createMQMAssageBody(), "DF800")),
					QUEUE.QL_DW_REP.getQName());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWait(QUEUE.QL_DW_REP.getQName());
			lastCheck800(putMQmassage, getMQmassage);

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

		@ParameterizedTest
		@MethodSource("test1and6_Per")
		@DisplayName("test8_ReplyDead_PERSISTENCE_FORMAT")
		void test8_ReplyDead_PERSISTENCE_FORMAT(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());
			putMQmassage.persistence = MQC.MQPER_PERSISTENT;
			putMQmassage.format = MQC.MQFMT_NONE;

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
		@DisplayName("test2_HTTPRequestError_PERSISTENCE_FORMAT")
		@MethodSource("test2_Per")
		void test2_HTTPRequestError_PERSISTENCE_FORMAT(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());
			putMQmassage.persistence = MQC.MQPER_PERSISTENT;
			putMQmassage.format = MQC.MQFMT_NONE;
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 2);
		}

		@ParameterizedTest
		@DisplayName("test3_HTTPRequestErrorAndReplyDead_PERSISTENCE_FORMAT")
		@MethodSource("test2_Per")
		void test3_HTTPRequestErrorAndReplyDead_PERSISTENCE_FORMAT(String str) throws Exception {
			MQMessage putMQmassage = setUpCreateMQ(str, QUEUE.QL_DW_REP.getQName());
			putMQmassage.persistence = MQC.MQPER_PERSISTENT;
			putMQmassage.format = MQC.MQFMT_NONE;
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

	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class MqXmlTestMainSotuStopAPI {

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
