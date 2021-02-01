package com.example.test;

import static com.example.mq.QUEUE.getList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import com.example.mq.QMFH01Test;
import com.example.mq.QUEUE;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

public class MqXmlTestMainSotu implements QMFH01Test, XMLCENTERTest {

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}

	MQMessage setUpCreate(String path, String outQueueName) throws Exception {

		MQMessage putMQmassage = createMQMessage(pathToString(path));
		putMQmassage.replyToQueueManagerName = qmgrname();
		putMQmassage.replyToQueueName = outQueueName;
		putMQmassage.correlationId = getUnique24().getBytes();

		putMQmassage.applicationIdData = getXmlEvaluate(xmlGlbPath("SERVICEID"),
				changeStringToDocument(toStringMQMessage(putMQmassage)));
		return putMQmassage;
	}
	MQMessage setUpCreateBreake(String path, String outQueueName) throws Exception {

		MQMessage putMQmassage = createMQMessage(path);
		putMQmassage.replyToQueueManagerName = qmgrname();
		putMQmassage.replyToQueueName = outQueueName;
		putMQmassage.correlationId = getUnique24().getBytes();

		putMQmassage.applicationIdData = getXmlEvaluate(xmlGlbPath("SERVICEID"),
				changeStringToDocument(toStringMQMessage(putMQmassage)));
		return putMQmassage;
	}

	void lastCheck(MQMessage putMQmassage, MQMessage getMQmassage, String getMQname, int flg) throws Exception {

		if (getMQname == "SYSTEM.ADMIN.EVENT") {

			assertTrue(mqCheck(putMQmassage, getMQmassage));
			assertEquals(1, getMQmassage.messageType);
			assertNotEquals(-1, getMQmassage.expiry);
			assertEquals(0, getMQmassage.persistence);
			assertEquals("MQDEAD", getMQmassage.format.trim());
			assertEquals(putMQmassage.characterSet, getMQmassage.characterSet);
			// 違う assertEquals(putMQmassage.encoding, getMQmassage.encoding);
			assertEquals(putMQmassage.applicationIdData, getMQmassage.applicationIdData.trim());

		} else {
			Document putMQmassageDocument = changeStringToDocument(toStringMQMessage(putMQmassage));
			Document getMQmassageDocument = changeStringToDocument(toStringMQMessage(getMQmassage));

			System.out.println((getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument)).toString());

			List<String> list = new ArrayList<>();
			list.add("TIMESTAMP");
			list.add("RC");

			switch (getMQname) {
			case "QL.DW.REP":
//				assertTrue(checkDefault(putMQmassageDocument, getMQmassageDocument));
				assertNotEquals(-1, getMQmassage.expiry);
				assertEquals(0, getMQmassage.persistence);

				System.out.println("QL.DW.REP");
				break;

			case "QL.DH.ERR":
				assertEquals(-1, getMQmassage.expiry);
				assertEquals(1, getMQmassage.persistence);

				System.out.println("QL.DH.ERR");
				break;

			}

			switch (flg) {
			case 0:

				if ("00" == (getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument))) {					
				}else {
					assertEquals((getXmlEvaluate(xmlGlbPath("RC"), putMQmassageDocument)).toString(),
							((getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument)).toString()));
				}
				assertEquals(DatatypeConverter.printHexBinary(putMQmassage.correlationId),
						(DatatypeConverter.printHexBinary(getMQmassage.messageId)));
				
				System.out.println("0正常");
//後で消す
				System.out.println("<RC>:" + getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument));
				System.out.println("getMQmassage(body) :" + toStringMQMessage(getMQmassage) + ":");

				break;
			case 1:
				System.out.println("1ロールバック");
				assertEquals(DatatypeConverter.printHexBinary(putMQmassage.messageId),
						(DatatypeConverter.printHexBinary(getMQmassage.messageId)));
				assertEquals((getXmlEvaluate(xmlGlbPath("RC"), putMQmassageDocument)).toString(),
						((getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument)).toString()));
				break;
			case 2:
				System.out.println("2リターンコード");
				assertEquals(DatatypeConverter.printHexBinary(putMQmassage.correlationId),
						(DatatypeConverter.printHexBinary(getMQmassage.messageId)));
				assertEquals("02", ((getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument)).toString()));
				break;
			}

//共通確認
			assertTrue(check(putMQmassageDocument, getMQmassageDocument, list));
			assertTrue(mqCheck(putMQmassage, getMQmassage));
			assertEquals("MQSTR", getMQmassage.format.trim());
			assertEquals(putMQmassage.characterSet, getMQmassage.characterSet);
			assertEquals(putMQmassage.encoding, getMQmassage.encoding);
			assertEquals(putMQmassage.applicationIdData, getMQmassage.applicationIdData.trim());

		}
		System.out.println("messageType:" + getMQmassage.messageType);
		System.out.println("format:" + getMQmassage.format.trim());
		System.out.println("ccsid:" + getMQmassage.characterSet);
		System.out.println("encoding:" + getMQmassage.encoding);
		System.out.println("expiry:" + getMQmassage.expiry);
		System.out.println("persistence:" + getMQmassage.persistence);
		System.out.println("applicationIdData:" + getMQmassage.applicationIdData);
	}

	@Test
	protected void test_NO1and6_ケース1_正常ケース() throws Exception {
		System.out.println("正常***************************");
		String path = "/ts3.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
		lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 0);

	}

	@Test
	protected void test_NO1and6_入力RCなし() throws Exception {
		System.out.println("正常RCなし***************************");
		String path = "/norc.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
		lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 0);
		


	}

	@Test
	protected void test_NO2_ケース2B_HTTPリクエストエラー_RC02() throws Exception {
		System.out.println("put(RC02)***************************");
		String path = "/ts777.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
		lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DW_REP.getQName(), 2);

	}

	@Test
	protected void test_NO2ERR_MQPUTエラー_QLDHERR() throws Exception {
		System.out.println("put(RC02)***************************");
		String path = "/ts777.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		try {
			putDisabled(QUEUE.QL_DW_REP.getQName());
//			putDisabled(QUEUE.QL_DH_ERR.getQName());

			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
		} finally {
			putEnabled(QUEUE.QL_DW_REP.getQName());
//			putEnabled(QUEUE.QL_DH_ERR.getQName());
		}

		MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
		lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 2);

	}

	@Test
	protected void test_NO2ERR_MQPUTエラー_EVENT() throws Exception {
		System.out.println("put(RC02)***************************");
		String path = "/ts777.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		try {
			putDisabled(QUEUE.QL_DW_REP.getQName());
			putDisabled(QUEUE.QL_DH_ERR.getQName());			
			
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
		} finally {
			putEnabled(QUEUE.QL_DW_REP.getQName());
			putEnabled(QUEUE.QL_DH_ERR.getQName());
			
		}
		MQMessage getMQmassage = mqGet(QUEUE.SYSTEM_ADMIN_EVENT.getQName());
		lastCheck(putMQmassage, getMQmassage, QUEUE.SYSTEM_ADMIN_EVENT.getQName(), 999);

	}

	@Test
	protected void test_NO4_ケース2A_XMLパースエラー_QLDHERR() throws Exception {
//xmlns="http://www.acom.co.jp/ACOMMM 誤り→エラーキューへ
		System.out.println("エラーキュー入り（Httpとホスト間のエラー）***************************");
		String path = "/ts4.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
		lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 1);
	}
	
	
	@Test
	protected void test_NO4_ケース2A_XMLパースエラー_breakbody() throws Exception {
		System.out.println("NO4_ケース2A_XMLパースエラー_breakbody***************************");
		String path = "/breakbody.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
		lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 1);
	}
	
	@Test
	protected void test_NO4_ケース2A_XMLパースエラー_breakrequestid() throws Exception {
		System.out.println("NO4_ケース2A_XMLパースエラー_breakrequestid***************************");
		String path = "/breakrequestid.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
		lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 1);
	}
	
	
	@Test
	protected void test_NO4_ケース2A_XMLパースエラー_breakserviceid() throws Exception {
		System.out.println("NO4_ケース2A_XMLパースエラー_breakserviceid***************************");
		String path = "/breakserviceid.xml";
//		String str1 = pathToString(path);
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);
		lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 1);
	}
	
	
	@Test
	protected void test_NO_ケース_DF999() throws Exception {
		System.out.println("DF999_breakserviceid***************************");
		String path = "/ts999.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DH_ERR.getQName(), putMQmassage.correlationId);

//		System.out.println("messageType:" + getMQmassage.messageType);
//		System.out.println("format:" + getMQmassage.format.trim());
//		System.out.println("ccsid:" + getMQmassage.characterSet);
//		System.out.println("encoding:" + getMQmassage.encoding);
//		System.out.println("expiry:" + getMQmassage.expiry);
//		System.out.println("persistence:" + getMQmassage.persistence);
//		System.out.println("applicationIdData:" + getMQmassage.applicationIdData);
		
		
//		lastCheck(putMQmassage, getMQmassage, QUEUE.QL_DH_ERR.getQName(), 1);
	}
	
	
	
	

	@Test
	protected void test_NO7_ケース3A_タイムアウト() {
		System.out.println("タイムアウト***************************");
		String path = "/ts300.xml";

		try {
			MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
			System.out.println("タイムアウト***************************");
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	protected void test_NO7_ケース3B_レスポンスエラー400() {
		System.out.println("400***************************");
		String path = "/ts400.xml";

		try {
			MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	protected void test_NO7_ケース3B_レスポンスエラー500() throws Exception {
		System.out.println("500***************************");
		String path = "/ts500.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
		mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);

	}

	@Test
	protected void test_NO8_ケース3C_MQPUTエラー_QLDHERR() throws Exception {
		System.out.println("エラーキュー入り（MQPUTエラー）***************************");
		String path = "/ts3.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());

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

	@Test
	protected void test_NO3_ケース3C_MQPUTエラーRC02_QLDHERR() throws Exception {
		System.out.println("エラーキュー入り（MQPUTエラー）***************************");
		String path = "/ts777.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());

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
	protected void test_NO5_QLDHERR() throws Exception {
		System.out.println("エラーキュー入れない（MQPUTエラー）***************************");
		String path = "/ts4.xml";
		MQMessage putMQmassage = setUpCreate(path, QUEUE.QL_DW_REP.getQName());

		try {
			putDisabled(QUEUE.QL_DH_ERR.getQName());

			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
			mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
		} finally {
			putEnabled(QUEUE.QL_DH_ERR.getQName());
		}
		MQMessage getMQmassage = mqGet(QUEUE.SYSTEM_ADMIN_EVENT.getQName());
		lastCheck(putMQmassage, getMQmassage, QUEUE.SYSTEM_ADMIN_EVENT.getQName(), 999);
	}

}