package com.example.mq;

import java.util.Objects;

import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQEnvironment;
import com.ibm.msg.client.wmq.compat.base.internal.MQGetMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;
import com.ibm.msg.client.wmq.compat.base.internal.MQPutMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueue;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueueManager;

//public abstract class AbstractMqtestmain {
public interface MQTest {

//	private MQQueue queue = null;
//	private static final int PRIORITY = 5;
//	private static final int CHARACTER_SET = 943;
//	private static final int LENGTH = 17;
//
//	protected abstract String qmgrname();
//
//	protected abstract String host();
//
//	protected abstract String channel();
//
//	protected abstract int port();

//	final MQQueueManager qmgr = null;
//	final MQQueue queue = null;
	int PRIORITY = 5;
	int CHARACTER_SET = 943;
	int LENGTH = 17;

	String qmgrname();

	String host();

	String channel();

	int port();

//	@BeforeEach
//	void setUpAll() throws Exception {
//		System.out.println("クラス共通設定");
//
//		env();
////		MQEnvironment.hostname = HOSTNAME;
////		MQEnvironment.channel = CHANNEL;
////		MQEnvironment.port = PORT;
//	}
//
	default void env() {
		// TODO 自動生成されたメソッド・スタブ
		MQEnvironment.hostname = host();
		MQEnvironment.channel = channel();
		MQEnvironment.port = port();
	}

//
//put
	default void mqput(String accessQueueName, String massage) {

		env();
		MQQueueManager qmgr = null;
		MQQueue queue = null;
		try {
			qmgr = new MQQueueManager(qmgrname());
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
	default String mqget(String accessQueueName) {
		env();
		MQQueueManager qmgr = null;
		MQQueue queue = null;
		try {
			qmgr = new MQQueueManager(qmgrname());

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
	default void mqerr(Exception e) {
		System.out.println("Exception occurred");
		e.printStackTrace();

	}

//クローズ処理
	default void mqclose(MQQueueManager qmgr, MQQueue queue) {
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
