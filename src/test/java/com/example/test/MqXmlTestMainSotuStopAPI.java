package com.example.test;

import static com.example.mq.QUEUE.getList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.mq.QMFH01Test;
import com.example.mq.QUEUE;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

public class MqXmlTestMainSotuStopAPI implements QMFH01Test, XMLCENTERTest {

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}


	@Test
	@DisplayName("test_手動テスト")
	protected void test_手動テスト() throws Exception {
		
		MQMessage putMQmassage = createMQMessage(createMQMAssageBody());
		putMQmassage.replyToQueueManagerName = qmgrname();
		putMQmassage.replyToQueueName = QUEUE.QL_DW_REP.getQName();
		putMQmassage.correlationId = getUnique24().getBytes();
		putMQmassage.applicationIdData = getXmlEvaluate(xmlGlbPath("SERVICEID"),
				changeStringToDocument(toStringMQMessage(putMQmassage)));
		putMQmassage.expiry = 100; // 100ms
		mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmassage);
		mqGetWait(QUEUE.QL_DW_REP.getQName());

	}


}