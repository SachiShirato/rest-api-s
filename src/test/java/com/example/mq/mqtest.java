	package com.example.mq;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.mq.MQException;
import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQEnvironment;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;
import com.ibm.msg.client.wmq.compat.base.internal.MQPutMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueue;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueueManager;

public class mqtest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		System.out.println("最初");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		System.out.println("最後");
	}

	@BeforeEach
	void setUp() throws Exception {
		System.out.println("途中最初");
	}

	@AfterEach
	void tearDown() throws Exception {
		System.out.println("途中最後");
	}

	@Test
	void test() {
		try {
			MQEnvironment.hostname = "localhost";
			MQEnvironment.channel = "SYSTEM.BKR.CONFIG";
			MQEnvironment.port = 50014;
			MQQueueManager qmgr = new MQQueueManager("QMFH01");
			int openOption = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT;
			MQQueue queue = qmgr.accessQueue("QL.DH.ERR", openOption);

			MQMessage putMessage = new MQMessage();
			putMessage.priority = 5;
			putMessage.characterSet = 943;
			putMessage.writeString("This is a message");
			MQPutMessageOptions mqpmo = new MQPutMessageOptions();
			mqpmo.options = MQC.MQPMO_NO_SYNCPOINT;
			queue.put(putMessage, mqpmo);
//
//			MQMessage getMessage = new MQMessage();
//			MQGetMessageOptions mqgmo = new MQGetMessageOptions(); 
//			queue.get(getMessage, mqgmo);
//			String strMessage = getMessage.readStringOfByteLength(17);
//			System.out.println(strMessage);
		

			queue.close();
			qmgr.disconnect();
//			throw new MQException(0, 9999, "dd");  //エラー投げている
		} catch (MQException | IOException e) {
//			System.out.println("MQException occurred" + System.lineSeparator() + "cc:" + ex.completionCode
//					+ System.lineSeparator() + "rc:" + ex.reasonCode);
//		} catch (IOException ex) {
			System.out.println("IOException occurred");
			e.printStackTrace();
		}

	}

}
