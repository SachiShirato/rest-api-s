package com.example.test;

import static jp.co.acom.fehub.mq.QUEUE.getList;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.mq.MQException;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QMFH01;
import jp.co.acom.fehub.mq.QUEUE;

public class Mqtestmain implements QMFH01 {

	@BeforeEach
	void setUpAll() throws Exception {
		mqtoEmpty(getList());
	}

	@Test
	protected void test() throws IOException, MQException {

		String putMassage = "HHHkkkkkkkkkkkkk";

		MQMessage putMQmassage = createMQMessage(putMassage);
		putMQmassage.replyToQueueManagerName = qmgrName();
		putMQmassage.replyToQueueName = QUEUE.QL_DW_REP.getQName();
//		putMQmassage.replyToQueueName=ERROR_QUEUE_NAME;

		mqput(QUEUE.QC_DH_REQ.getQName(), putMQmassage);
//		mqput(ERROR_QUEUE_NAME, putMQmassage);

		MQMessage getMQmassage = mqGetWaitMsgid(ERROR_QUEUE_NAME, putMQmassage.messageId);
		mqCheck(putMQmassage, getMQmassage);

	}
}
