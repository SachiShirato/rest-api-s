package com.example.mq;

import java.util.Objects;

import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQGetMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;
import com.ibm.msg.client.wmq.compat.base.internal.MQPutMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueue;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueueManager;

public class Mqstb {
	static MQQueueManager qmgr = null;
	static MQQueue queue = null;
	static final int PRIORITY = 5;
	static final int CHARACTER_SET = 943;
	static final int LENGTH = 17;

//
//put
	public static void mqput(String MQQueueManagerName, String accessQueueName, String massage) {

		try {
			qmgr = new MQQueueManager(MQQueueManagerName);
			queue = qmgr.accessQueue(accessQueueName, MQC.MQOO_OUTPUT);

			MQPutMessageOptions mqpmo = new MQPutMessageOptions();
			mqpmo.options = MQC.MQPMO_NO_SYNCPOINT;

			MQMessage putMessage = new MQMessage();
			putMessage.priority = PRIORITY;
			putMessage.characterSet = CHARACTER_SET;
			putMessage.writeString(massage);

			queue.put(putMessage, mqpmo);

		} catch (Exception e) {
			mqerr(e);
		} finally {
			mqclose(qmgr, queue);
		}
	}

// get
	public static String mqget(String MQQueueManagerName, String accessQueueName) {

		try {
			qmgr = new MQQueueManager(MQQueueManagerName);

			queue = qmgr.accessQueue(accessQueueName, MQC.MQOO_INPUT_AS_Q_DEF);

			MQMessage getMessage = new MQMessage();
			MQGetMessageOptions mqgmo = new MQGetMessageOptions();
			queue.get(getMessage, mqgmo);
			String strMessage = getMessage.readStringOfByteLength(LENGTH);

			return strMessage;

		} catch (Exception e) {
			mqerr(e);
			return null;
		} finally {
			mqclose(qmgr, queue);
		}
	}

//エラー処理
	public static void mqerr(Exception e) {
		System.out.println("Exception occurred");
		e.printStackTrace();

	}

//クローズ処理
	public static void mqclose(MQQueueManager qmgr, MQQueue queue) {
		try {
			if (queue != null) {
				queue.close();
				System.out.println("qClose");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			if (Objects.nonNull(qmgr)) {
				qmgr.disconnect();
				System.out.println("qmgrClose");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
