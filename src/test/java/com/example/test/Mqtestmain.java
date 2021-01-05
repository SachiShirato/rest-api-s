package com.example.test;

import static com.example.mq.QUEUE.getList;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.mq.QMFH01Test;
import com.example.mq.QUEUE;
//import com.example.mq.QUEUE;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

public class Mqtestmain implements QMFH01Test {

	@BeforeEach
	void setUpAll() throws Exception {
//		mqputnull(getList());
//		mqgetnull(getList());

		System.out.println(getList());
	}

	@Test
	protected void test() throws IOException {
////編集　メッセージ（入力）
		String putMassage = "HHHkkkkkkkkkkkkk";
////put(入力がMQメッセージ)
		MQMessage putMQmassage = createMQMessage(putMassage);
		putMQmassage.replyToQueueManagerName=qmgrname();
		putMQmassage.replyToQueueName=QUEUE.QL_DW_REP.getQName();

		mqput(QUEUE.QC_DH_REQ.getQName(), putMQmassage);



////get(MQメッセージを受け取る)
		MQMessage getMQmassage = mqGetWaitMsgid(ACCESS_QUEUE_NAME, putMQmassage.messageId);
//
//			// 前後の確認
		mqCheck(putMQmassage, getMQmassage);
//
//		}
//
	}
}
