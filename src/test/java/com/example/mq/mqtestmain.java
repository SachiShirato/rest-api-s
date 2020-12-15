package com.example.mq;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.mq.MQException;
import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQEnvironment;
import com.ibm.msg.client.wmq.compat.base.internal.MQPutMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueue;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueueManager;

public class mqtestmain {

//編集箇所　1 共通		
	private static final String hostname = "localhost";
	private static final String channel = "SYSTEM.BKR.CONFIG";
	private static final int port = 50014;
	private static final String MQQueueManagerName = "QMFH01";
	private static final String accessQueueName = "QL.DH.ERR";
//	private static MQQueueManager qmgr;
//	private static MQQueue queue;

	@BeforeAll
	static void setUpAll() throws Exception {
		System.out.println("クラス共通設定");

		MQEnvironment.hostname = hostname;
		MQEnvironment.channel = channel;
		MQEnvironment.port = port;
	}

	@BeforeEach
	void setUp() throws Exception {
		System.out.println("共通設定");
//
//		MQEnvironment.hostname = mqtestmain.hostname;
//		MQEnvironment.channel = mqtestmain.channel;
//		MQEnvironment.port = mqtestmain.port;
//
	}

	@Test
	void test() {
		MQQueueManager qmgr = null;
		MQQueue queue = null;
		try {
//編集箇所　2			
			int priority = 5;
			int characterSet = 943;
			String massage = "This is a message";

			qmgr = new MQQueueManager(mqtestmain.MQQueueManagerName);
			
			int openOption = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT;
			queue = qmgr.accessQueue(mqtestmain.accessQueueName, openOption);

//put 
			MQPutMessageOptions mqpmo = new MQPutMessageOptions();
			mqpmo.options = MQC.MQPMO_NO_SYNCPOINT;
			queue.put(mqstb.mqput(priority, characterSet, massage));

//get
			System.out.println(mqstb.mqget(queue, 17));

//エラー投げている	throw new MQException(0, 9999, "dd");

		} catch (MQException | IOException e) {
			System.out.println("IOException occurred");
			e.printStackTrace();
		} finally {
			try {
				queue.close();
				System.out.println("qClose");
			} catch (MQException e) {
//				queue =null;
				e.printStackTrace();
			}

			try {
				qmgr.disconnect();
				System.out.println("qmgrClose");
			} catch (MQException e) {
//				qmgr =null;
				e.printStackTrace();
			}
		}
	}
}
