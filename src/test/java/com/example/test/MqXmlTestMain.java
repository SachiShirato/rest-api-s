package com.example.test;

import static com.example.mq.QUEUE.getList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import com.example.mq.QMFH01Test;
import com.example.mq.QUEUE;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

public class MqXmlTestMain implements QMFH01Test, XMLCENTERTest {

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}

	@Test
	protected void test() throws Exception {

		String path = "/ts3.xml";

		String putMassage = pathToString(path);
		MQMessage putMQmassage = createMQMessage(putMassage);
		putMQmassage.replyToQueueManagerName = qmgrname();
		putMQmassage.replyToQueueName = QUEUE.QL_DW_REP.getQName();
		mqput(QUEUE.QC_DH_REQ.getQName(), putMQmassage);

		MQMessage getMQmassage = mqGetWaitCorrelid(QUEUE.QL_DW_REP.getQName(), putMQmassage.messageId);
		
		Document putMQmassageDocument = changeStringToDocument(toStringMQMessage(putMQmassage));
		Document getMQmassageDocument = changeStringToDocument(toStringMQMessage(getMQmassage));
		List<String> list = new ArrayList<>();
		list.add("TIMESTAMP");
		list.add("RC");

		assertTrue(check(putMQmassageDocument,getMQmassageDocument,list));
		assertTrue(checkDefault(putMQmassageDocument,getMQmassageDocument));
		assertTrue(checkGetTs(getMQmassageDocument));

		System.out.println(getTimestamp(getMQmassageDocument));
	}
}