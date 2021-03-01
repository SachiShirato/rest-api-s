package jp.co.acom.fehub.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
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

public class MqXmlTestMainSotu implements QMFH01Test, XMLCENTERTest {

	public static String path = "/ts3.xml";
	List<String> list = new ArrayList<String>();

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}

	String creanBodyN(String str) throws Exception {
		str = str.replaceAll("(\n|\r|\n\r|\r\n){2,}", "\n");
		str = str.replaceAll("[ \t\\x0B\f]+(\n|\r|\n\r|\r\n)", "");
		if (str.substring(str.length() - 1).equals("\n")) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
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
		if (getMQname == "QL.DH.ERR") {
			assertAll(() -> assertEquals(-1, getMQmassage.expiry), () -> assertEquals(1, getMQmassage.persistence));

			System.out.println("QL.DH.ERR");
		} else {
			assertAll(() -> assertNotEquals(-1, getMQmassage.expiry), () -> assertEquals(0, getMQmassage.persistence),
//body notはおかしい 確認
					() -> assertNotEquals(toStringMQMessage(putMQmassage), toStringMQMessage(getMQmassage)));
		}
//		String aaa ==toStringMQMessage(getMQmassage);
//		aaa == toStringMQMessage(getMQmassage).replaceAll("(\n|\r|\n\r|\r\n){2,}", "\n");
//		// 空白行をマッチさせて削除する
//		aaa == aaa.replaceAll("[ \t\\x0B\f]+(\n|\r|\n\r|\r\n)", "");
//		//最後の改行を削除する
//		if(after.substring(after.length() - 1).equals("\n")){
//		    after=after.substring(0,after.length()-1);

		String a=toStringMQMessage(getMQmassage);
		String b=toStringMQMessage(putMQmassage);
		a=a.replaceAll("(\n|\r|\n\r|\r\n){2,}", "\n");
		a=a.replaceAll("[ \t\\x0B\f]+(\n|\r|\n\r|\r\n)", "");
		if(a.substring(a.length() - 1).equals("\n")){
		    a=a.substring(0,a.length()-1);
		
		b=b.replaceAll("(\n|\r|\n\r|\r\n){2,}", "\n");
		b=b.replaceAll("[ \t\\x0B\f]+(\n|\r|\n\r|\r\n)", "");
		if(b.substring(b.length() - 1).equals("\n")){
		    b=b.substring(0,b.length()-1);}
		System.out.println(a);
		assertEquals(a,b);
//		assertEquals(creanBodyN(toStringMQMessage(putMQmassage)), creanBodyN(toStringMQMessage(getMQmassage)));

//			System.out.println("QL.DW.REP");
//			break;
//
//		case "QL.DH.ERR":
//			assertAll(
//					() -> assertEquals(MQC.MQPER_NOT_PERSISTENT, getMQmassage.expiry),
//					() -> assertEquals(1, getMQmassage.persistence));
//				
//			System.out.println("QL.DH.ERR");
//			break;
//		case "SYSTEM.ADMIN.EVENT":
//			assertAll(() -> assertNotEquals(-1, getMQmassage.expiry), () -> assertEquals(0, getMQmassage.persistence),
//					() -> assertEquals("MQDEAD", getMQmassage.format.trim()),
//					() -> assertEquals(putMQmassage.encoding * 2, getMQmassage.encoding));
//			System.out.println("SYSTEM.ADMIN.EVENT");
//			break;
//		}
//		if ((flg == 999) || (flg == 800)) {
//			switch (flg) {
//			case 999:
//				System.out.println("999");
//				assertAll(
//						() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.messageId),
//								(DatatypeConverter.printHexBinary(getMQmassage.messageId))),
//						() -> assertEquals(1, getMQmassage.messageType));
//				break;
//			case 800:
//				System.out.println("800");
//				assertAll(() -> assertEquals("aaaaaa", toStringMQMessage(getMQmassage)),
//						() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.correlationId),
//								(DatatypeConverter.printHexBinary(getMQmassage.messageId))),
//						() -> assertEquals(2, getMQmassage.messageType));
//				System.out.println("getMQmassage(body) :" + toStringMQMessage(getMQmassage) + ":");
//				break;
//			}
//
//		} else {
		Document putMQmassageDocument = changeStringToDocument(toStringMQMessage(putMQmassage));
		Document getMQmassageDocument = changeStringToDocument(toStringMQMessage(getMQmassage));
		String getRc = getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument);

		List<String> list = new ArrayList<>();
		list.add("TIMESTAMP");
		list.add("RC");

		switch (flg) {

		case 0:
//ドキュメント（RC）使うのは02のみ　0のRCはボディーチェックで確認
			assertAll(() -> assertEquals((getXmlEvaluate(xmlGlbPath("RC"), putMQmassageDocument)).toString(), getRc),
					() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.correlationId),
							(DatatypeConverter.printHexBinary(getMQmassage.messageId))),
					() -> assertEquals(2, getMQmassage.messageType));
			System.out.println("0正常");

			break;
		case 2:
			System.out.println("2リターンコード");
			assertAll(() -> assertEquals("02", getRc),
					() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.correlationId),
							(DatatypeConverter.printHexBinary(getMQmassage.messageId))),
					() -> assertEquals(1, getMQmassage.messageType));
			assertTrue(check(putMQmassageDocument, getMQmassageDocument, list));
			System.out.println("putMQmassage(RC) :" + getRc);
			break;
		}

//		}
		assertAll(() -> assertTrue(mqCheck(putMQmassage, getMQmassage)),
				() -> assertEquals(putMQmassage.applicationIdData, getMQmassage.applicationIdData.trim()),
				() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmassage.format.trim()),
				() -> assertEquals(putMQmassage.encoding, getMQmassage.encoding));

//		System.out.println("putMQmassage(messageType) :" + putMQmassage.messageType);
//		System.out.println("getMQmassage(messageType) :" + getMQmassage.messageType);
//		System.out.println("putMQmassage(format) :" + putMQmassage.format.trim());
//		System.out.println("getMQmassage(format) :" + getMQmassage.format.trim());
//		System.out.println("putMQmassage(encoding) :" + putMQmassage.encoding);
//		System.out.println("getMQmassage(encoding) :" + getMQmassage.encoding);
//		System.out.println("putMQmassage(expiry) :" + putMQmassage.expiry);
//		System.out.println("getMQmassage(expiry) :" + getMQmassage.expiry);
//		System.out.println("putMQmassage(persistence) :" + putMQmassage.persistence);
//		System.out.println("getMQmassage(persistence) :" + getMQmassage.persistence);
//		System.out.println("putMQmassage(applicationIdData) :" + putMQmassage.applicationIdData);
//		System.out.println("getMQmassage(applicationIdData) :" + getMQmassage.applicationIdData);

	}

	void lastCheck999(MQMessage putMQmassage, MQMessage getMQmassage) throws Exception {
		System.out.println("999");
		assertAll(() -> assertEquals(-1, getMQmassage.expiry), () -> assertEquals(1, getMQmassage.persistence),
				() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.messageId),
						(DatatypeConverter.printHexBinary(getMQmassage.messageId))),
				() -> assertEquals(1, getMQmassage.messageType),
				() -> assertEquals(toStringMQMessage(putMQmassage), toStringMQMessage(getMQmassage)),
				() -> assertTrue(mqCheck(putMQmassage, getMQmassage)),
				() -> assertEquals(putMQmassage.applicationIdData, getMQmassage.applicationIdData.trim()),
				() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmassage.format.trim()),
				() -> assertEquals(putMQmassage.encoding, getMQmassage.encoding));
	}

	void lastCheck800(MQMessage putMQmassage, MQMessage getMQmassage) throws Exception {
		System.out.println("800");
		System.out.println("getMQmassage(body) :" + toStringMQMessage(getMQmassage) + ":");
		assertAll(() -> assertEquals("aaaaaa", toStringMQMessage(getMQmassage)),
				() -> assertEquals(DatatypeConverter.printHexBinary(putMQmassage.correlationId),
						(DatatypeConverter.printHexBinary(getMQmassage.messageId))),
				() -> assertEquals(2, getMQmassage.messageType), () -> assertTrue(mqCheck(putMQmassage, getMQmassage)),
				() -> assertEquals(putMQmassage.applicationIdData, getMQmassage.applicationIdData.trim()),
				() -> assertEquals(MQC.MQFMT_STRING.trim(), getMQmassage.format.trim()),
				() -> assertEquals(putMQmassage.encoding, getMQmassage.encoding),
				() -> assertNotEquals(MQC.MQPER_NOT_PERSISTENT, getMQmassage.expiry),
				() -> assertEquals(0, getMQmassage.persistence),
				() -> assertNotEquals(toStringMQMessage(putMQmassage), toStringMQMessage(getMQmassage)));
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
			return Stream.of(
					Arguments.of(createMQMAssageBody()));
//					Arguments.of(createBreakeRc(createMQMAssageBody(), "")),
//					Arguments.of(createBreakeRequestid(createMQMAssageBody(), "")));
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
//			lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 800);
			lastCheck800(putMQmassage, getMQmassage);

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