package com.example.mq;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.DatatypeConverter;

import com.ibm.mq.MQException;
import com.ibm.mq.constants.CMQC;
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

	default void setMQEnvironment() {
		MQEnvironment.hostname = host();
		MQEnvironment.channel = channel();
		MQEnvironment.port = port();
	}

	default MQMessage createMQMessage(String MQMassageBody) throws IOException {

		MQMessage putBody = new MQMessage();
		putBody.priority = PRIORITY;
		putBody.characterSet = CHARACTER_SET;
		putBody.writeString(MQMassageBody);
		putBody.messageId = MQC.MQMI_NONE;
		return (putBody);

	}

	default void mqtoEmpty(List<String> ACCESS_QUEUE_NAME_LIST) {
		String str2;

		for (String name : ACCESS_QUEUE_NAME_LIST) {
			int i = 1;
			do {
				str2 = mqGetString(name);
				System.out.println("MQname:" + name + "/ごみ:" + str2 + "/件数:" + i++);
			} while (str2 != null);
		}
	}

	default void mqputAll(List<String> ACCESS_QUEUE_NAME_LIST) {

		for (String name : ACCESS_QUEUE_NAME_LIST) {
			mqput(name, "a");
			mqput(name, "b");
			mqput(name, "c");
		}

	}

	default String toStringMQMessage(MQMessage msg) {
		if (msg == null)
			return null;
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

	default void mqCheck(MQMessage putMQmassage, MQMessage getMQmassage) {
		assertEquals(toStringMQMessage(putMQmassage), toStringMQMessage(getMQmassage));
		System.out.println("putMQmassage(body) :" + toStringMQMessage(putMQmassage) + ":");
		System.out.println("getMQmassage(body) :" + toStringMQMessage(getMQmassage) + ":");

		assertEquals(putMQmassage.priority, getMQmassage.priority);
		System.out.println("putMQmassage(priority) :" + putMQmassage.priority);
		System.out.println("getMQmassage(priority) :" + getMQmassage.priority);

		assertEquals(putMQmassage.characterSet, getMQmassage.characterSet);
		System.out.println("putMQmassage(characterSet) :" + putMQmassage.characterSet);
		System.out.println("getMQmassage(characterSet) :" + getMQmassage.characterSet);

		assertEquals(DatatypeConverter.printHexBinary(putMQmassage.messageId),
				DatatypeConverter.printHexBinary(getMQmassage.messageId));
		System.out.println("putMQmassage(messageId) :" + DatatypeConverter.printHexBinary(putMQmassage.messageId));
		System.out.println("getMQmassage(messageId) :" + DatatypeConverter.printHexBinary(getMQmassage.messageId));

	}

	default void mqCheck(String putMassage, String getMassage) {
		assertEquals(putMassage, getMassage);
		System.out.println("putMassage :" + putMassage + ":");
		System.out.println("getMassage :" + getMassage + ":");
	}

	default void mqput(String accessQueueName, MQMessage putMessage) {
		setMQEnvironment();
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

	default void mqput(String accessQueueName, String massage) {
		try {
			mqput(accessQueueName, createMQMessage(massage));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	default MQGetMessageOptions createMQGetMessageOptions() {
		MQGetMessageOptions mqgmo = new MQGetMessageOptions();
		mqgmo.options = MQC.MQGMO_WAIT;
		mqgmo.waitInterval = 10000;
		return mqgmo;
	}

	default MQMessage mqGet(String accessQueueName) {
		return mqGet(accessQueueName, new MQGetMessageOptions(), new MQMessage());
	}

	default MQMessage mqGetWaitMsgid(String accessQueueName, byte[] messageId) {

		MQGetMessageOptions mqgmo = new MQGetMessageOptions();
		mqgmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;

		return mqGetWait(accessQueueName, messageId, mqgmo);
	}

	default MQMessage mqGetWaitCorrelid(String accessQueueName, byte[] messageId) {

		MQGetMessageOptions mqgmo = new MQGetMessageOptions();
		mqgmo.matchOptions = MQC.MQMO_MATCH_CORREL_ID;

		return mqGetWait(accessQueueName, messageId, mqgmo);
	}

	default MQMessage mqGetWait(String accessQueueName, byte[] messageId, MQGetMessageOptions mqgmo) {

		MQMessage getMsg = new MQMessage();
		getMsg.messageId = messageId;

		return mqGetWait(accessQueueName, mqgmo, getMsg);
	}
	// get

	default String mqGetString(String accessQueueName) {
		return toStringMQMessage(mqGet(accessQueueName, new MQGetMessageOptions(), new MQMessage()));
	}

	default String mqGetWaitString(String accessQueueName) {

		return toStringMQMessage(mqGetWait(accessQueueName));

	}

	default MQMessage mqGetWait(String accessQueueName) {

		return mqGetWait(accessQueueName, new MQGetMessageOptions(), new MQMessage());
	}

	default MQMessage mqGetWait(String accessQueueName, MQGetMessageOptions mqgmo, MQMessage getMessage) {

		mqgmo.options = MQC.MQGMO_WAIT;
		mqgmo.waitInterval = 10000;

		return mqGet(accessQueueName, mqgmo, getMessage);
	}

	default MQMessage mqGet(String accessQueueName, MQGetMessageOptions mqgmo, MQMessage getMessage) {
		setMQEnvironment();
		MQQueueManager qmgr = null;
		MQQueue queue = null;
		try {
			qmgr = new MQQueueManager(qmgrname());

			queue = qmgr.accessQueue(accessQueueName, MQC.MQOO_INPUT_AS_Q_DEF);

			queue.get(getMessage, mqgmo);

			return getMessage;

		} catch (MQException e) {
			if (e.getReason() != CMQC.MQRC_NO_MSG_AVAILABLE)
				mqerr(e);
			return null;
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
