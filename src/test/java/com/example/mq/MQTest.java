package com.example.mq;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQEnvironment;
import com.ibm.msg.client.wmq.compat.base.internal.MQGetMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;
import com.ibm.msg.client.wmq.compat.base.internal.MQPutMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueue;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueueManager;

public interface MQTest {

	int PRIORITY = 5;
	int CHARACTER_SET = 943;

	String qmgrname();

	String host();

	String channel();

	int port();

//環境設定	
	default void env() {
		MQEnvironment.hostname = host();
		MQEnvironment.channel = channel();
		MQEnvironment.port = port();
	}

//MQメッセージ作成	
	default MQMessage createMQMessage(String MQMassageBody) throws IOException {

		MQMessage putBody = new MQMessage();
		putBody.priority = PRIORITY;
		putBody.characterSet = CHARACTER_SET;
		putBody.writeString(MQMassageBody);
		return (putBody);

	}

//初期設定（MQの中身を空にする）	
	default void mqgetnull(String ACCESS_QUEUE_NAME) {
		String str2;
		int i = 1;
		do {
			str2 = mqget(ACCESS_QUEUE_NAME);
			System.out.println("ごみ:" + str2 + "/件数:" + i++);
		} while (str2 != null);

	}

//MQMessageからボディー（String）抽出	
	default String readLine(MQMessage msg) {
		try {
			StringBuilder builder = new StringBuilder();
			msg.setDataOffset(0);
			while (msg.getDataLength() > 0)
				builder.append(msg.readLine() + System.lineSeparator());
			String strgetMassage = Optional.of(builder.toString()).get();
			return strgetMassage.substring(0, strgetMassage.length() - 2);

		} catch (IOException e) {
			return null;
		}
	}

//確認
	default void mqchek(MQMessage putMQmassage, MQMessage getMQmassage) {
		assertEquals(readLine(putMQmassage), readLine(getMQmassage));
		System.out.println("putMQmassage(body) :" + readLine(putMQmassage) + ":");
		System.out.println("getMQmassage(body) :" + readLine(getMQmassage) + ":");

		assertEquals(putMQmassage.priority, getMQmassage.priority);
		System.out.println("putMQmassage(priority) :" + putMQmassage.priority);
		System.out.println("getMQmassage(priority) :" + getMQmassage.priority);

		assertEquals(putMQmassage.characterSet, getMQmassage.characterSet);
		System.out.println("putMQmassage(characterSet) :" + putMQmassage.characterSet);
		System.out.println("getMQmassage(characterSet) :" + getMQmassage.characterSet);
	}

	default void mqchek(String putMassage, String getMassage) {
		assertEquals(putMassage, getMassage);
		System.out.println("putMassage :" + putMassage + ":");
		System.out.println("getMassage :" + getMassage + ":");
	}

	/*
	 * put
	 *************************************************************************************/
	// put(入力がMQMassage)
	default void mqput(String accessQueueName, MQMessage putMessage) {
		env();
		MQQueueManager qmgr = null;
		MQQueue queue = null;
		try {
			qmgr = new MQQueueManager(qmgrname());
			queue = qmgr.accessQueue(accessQueueName, MQC.MQOO_OUTPUT);

			MQPutMessageOptions mqpmo = new MQPutMessageOptions();
			mqpmo.options = MQC.MQPMO_NO_SYNCPOINT;

			queue.put(putMessage, mqpmo);

		} catch (Exception e) {
			mqerr(e);
		} finally {
			mqclose(qmgr, queue);
		}
	}

	// put(入力がメッセージ)
	default void mqput(String accessQueueName, String massage) {

		env();
		MQQueueManager qmgr = null;
		MQQueue queue = null;
		try {
			qmgr = new MQQueueManager(qmgrname());
			queue = qmgr.accessQueue(accessQueueName, MQC.MQOO_OUTPUT);

			MQPutMessageOptions mqpmo = new MQPutMessageOptions();
			mqpmo.options = MQC.MQPMO_NO_SYNCPOINT;

			MQMessage putMessage = createMQMessage(massage);

			queue.put(putMessage, mqpmo);

		} catch (Exception e) {
			mqerr(e);
		} finally {
			mqclose(qmgr, queue);
		}
	}

	/*
	 * get
	 *************************************************************************************/
	// getbody

	default MQMessage mqget(String accessQueueName, String a) {
		env();
		MQQueueManager qmgr = null;
		MQQueue queue = null;
		try {
			qmgr = new MQQueueManager(qmgrname());

			queue = qmgr.accessQueue(accessQueueName, MQC.MQOO_INPUT_AS_Q_DEF);

			MQMessage getMessage = new MQMessage();
			MQGetMessageOptions mqgmo = new MQGetMessageOptions();
			queue.get(getMessage, mqgmo);

			return getMessage;

		} catch (Exception e) {
			mqerr(e);
			return null;
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

			return readLine(getMessage);

		} catch (Exception e) {
			mqerr(e);
			return null;
		} finally {
			mqclose(qmgr, queue);
		}
	}

	/*
	 * err
	 *************************************************************************************/
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
