package com.example.test;

import static com.example.mq.QUEUE.getList;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.mq.QMFH01Test;
import com.example.mq.QUEUE;
import com.ibm.mq.MQException;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

public class Mqtestmain implements QMFH01Test {

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}

	@Test
	protected void test() throws IOException, MQException {

		String putMassage = "HHHkkkkkkkkkkkkk";

		MQMessage putMQmassage = createMQMessage(putMassage);
		putMQmassage.replyToQueueManagerName = qmgrname();
		putMQmassage.replyToQueueName = QUEUE.QL_DW_REP.getQName();
//		putMQmassage.replyToQueueName=ACCESS_QUEUE_NAME;

		mqput(QUEUE.QC_DH_REQ.getQName(), putMQmassage);
//		mqput(ACCESS_QUEUE_NAME, putMQmassage);

		MQMessage getMQmassage = mqGetWaitMsgid(ACCESS_QUEUE_NAME, putMQmassage.messageId);
		mqCheck(putMQmassage, getMQmassage);

	}
}
