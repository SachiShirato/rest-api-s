package com.example.mq;

import java.io.IOException;

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
	private static String hostname = "localhost";
	private static String channel = "SYSTEM.BKR.CONFIG";
	private static int port = 50014;
	private static String MQQueueManagerName = "QMFH01";
	private static String accessQueueName = "QL.DH.ERR";
	private static MQQueueManager qmgr;
	private static MQQueue queue;	
	
	
	@BeforeEach
	void setUp() throws Exception {	
		System.out.println("共通設定");
	
		MQEnvironment.hostname = mqtestmain.hostname;
		MQEnvironment.channel = mqtestmain.channel;
		MQEnvironment.port = mqtestmain.port;

	}


	@Test
	void test() {
		try {
			
			qmgr = new MQQueueManager(mqtestmain.MQQueueManagerName);
			int openOption = MQC.MQOO_INPUT_AS_Q_DEF | MQC.MQOO_OUTPUT;
			queue = qmgr.accessQueue(mqtestmain.accessQueueName, openOption);
			
			
//put 
			MQPutMessageOptions mqpmo = new MQPutMessageOptions();
			mqpmo.options = MQC.MQPMO_NO_SYNCPOINT;
//編集箇所　2 put(int priority,int characterSet,String massage)		
			queue.put(mqstb.mqput(5,943,"This is a message"));	
				
			
//get
			System.out.println(mqstb.mqget(queue,17));
			
//エラー投げている	throw new MQException(0, 9999, "dd");
		} catch (MQException | IOException e) {
			System.out.println("IOException occurred");
			e.printStackTrace();
		} finally {
			try {
				queue.close();
				qmgr.disconnect();
				System.out.println("close");
			} catch (MQException e) {
				e.printStackTrace();
			}
		}
	}
}
