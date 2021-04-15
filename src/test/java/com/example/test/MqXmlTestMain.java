package com.example.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01Test;
import jp.co.acom.fehub.mq.QUEUE;
import jp.co.acom.fehub.xml.XMLCENTERTest;

public class MqXmlTestMain implements QMFH01Test, XMLCENTERTest {

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}

	@Test
	protected void test() throws Exception {
		String path = "/ts3.xml";

		MQMessage putMQmassage = createMQMessage(pathToString(path));
		putMQmassage.replyToQueueManagerName = qmgrName();
		putMQmassage.replyToQueueName = QUEUE.QL_DW_REP.getQName();
		mqput(QUEUE.QC_DH_REQ.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DW_REP.getQName(), putMQmassage.messageId);

		Document putMQmassageDocument = changeStringToDocument(toStringMQMessage(putMQmassage));
		Document getMQmassageDocument = changeStringToDocument(toStringMQMessage(getMQmassage));
		List<String> list = new ArrayList<>();
		list.add("TIMESTAMP");
		list.add("RC");

		assertTrue(check(putMQmassageDocument, getMQmassageDocument, list));
		assertTrue(checkDefault(putMQmassageDocument, getMQmassageDocument));
//		assertTrue(checkGetTs(getMQmassageDocument));
		assertEquals(DatatypeConverter.printHexBinary(putMQmassage.messageId),
				DatatypeConverter.printHexBinary(getMQmassage.correlationId));
		mqCheck(putMQmassage, getMQmassage);

		System.out.println(getTimestamp(getMQmassageDocument));
		

	}
}