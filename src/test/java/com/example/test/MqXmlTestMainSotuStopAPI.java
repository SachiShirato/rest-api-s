package com.example.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01Test;
import jp.co.acom.fehub.mq.QUEUE;
import jp.co.acom.fehub.xml.XMLCENTERTest;

public class MqXmlTestMainSotuStopAPI implements QMFH01Test, XMLCENTERTest {

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}


	@Test
	@DisplayName("test_手動テスト")
	protected void test_手動テスト() throws Exception {
		
		MQMessage putMQmassage = createMQMessage(createMQMessageBody());
		putMQmassage.replyToQueueManagerName = qmgrName();
		putMQmassage.replyToQueueName = QUEUE.QL_DW_REP.getQName();
		putMQmassage.correlationId = getUnique24().getBytes();
		putMQmassage.applicationIdData = getXmlEvaluate(xmlGlbPath("SERVICEID"),
				changeStringToDocument(toStringMQMessage(putMQmassage)));
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
		MQMessage getMQmassage = mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmassage.correlationId);
		Document getMQmassageDocument = changeStringToDocument(toStringMQMessage(getMQmassage));
		assertEquals("02",(getXmlEvaluate(xmlGlbPath("RC"), getMQmassageDocument)));
	}


}