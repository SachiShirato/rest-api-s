package com.example.mq;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
	int CHARACTER_SET = 1208;

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

	default void mqtoEmpty(List<String> ACCESS_QUEUE_NAME_LIST) throws IOException, MQException {

		for (String name : ACCESS_QUEUE_NAME_LIST) {
			int i = 1;
			String str;
			do {
				str = toStringMQMessage(mqGet(name));
				if (str != null) {
					System.out.println("MQname:" + name + "/ごみ:" + str + "/件数:" + i++);
				}
			} while (str != null);
		}
	}

	default void mqputAll(List<String> ACCESS_QUEUE_NAME_LIST) throws IOException, MQException {

		for (String name : ACCESS_QUEUE_NAME_LIST) {
			mqput(name, "a");
			mqput(name, "b");
			mqput(name, "c");
		}
	}

	default String toStringMQMessage(MQMessage msg) throws IOException {
		if (msg == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		msg.setDataOffset(0);
		builder.append(msg.readLine());
		while (msg.getDataLength() > 0)
			builder.append(System.lineSeparator() + msg.readLine());
		String strgetMassage = builder.toString();
		return strgetMassage.substring(0, strgetMassage.length());
	}

	default boolean mqCheck(MQMessage putMQmassage, MQMessage getMQmassage) throws IOException {

		System.out.println("putMQmassage(body) :" + toStringMQMessage(putMQmassage) + ":");
		System.out.println("getMQmassage(body) :" + toStringMQMessage(getMQmassage) + ":");
		System.out.println("putMQmassage(priority) :" + putMQmassage.priority);
		System.out.println("getMQmassage(priority) :" + getMQmassage.priority);
		System.out.println("putMQmassage(characterSet) :" + putMQmassage.characterSet);
		System.out.println("getMQmassage(characterSet) :" + getMQmassage.characterSet);
		System.out.println("putMQmassage(messageId) :" + DatatypeConverter.printHexBinary(putMQmassage.messageId));
		System.out.println("getMQmassage(messageId) :" + DatatypeConverter.printHexBinary(getMQmassage.messageId));
		System.out.println(
				"putMQmassage(correlationId) :" + DatatypeConverter.printHexBinary(putMQmassage.correlationId));
		System.out.println(
				"getMQmassage(correlationId) :" + DatatypeConverter.printHexBinary(getMQmassage.correlationId));

		return ((toStringMQMessage(putMQmassage).equals(toStringMQMessage(getMQmassage)))
				&& (putMQmassage.priority == (getMQmassage.priority))
				&& (putMQmassage.characterSet == (getMQmassage.characterSet))
				&& (DatatypeConverter.printHexBinary(putMQmassage.messageId)
						.equals(DatatypeConverter.printHexBinary(getMQmassage.messageId))));
	}

	default boolean mqCheck(String putMassage, String getMassage) {
		System.out.println("putMassage :" + putMassage + ":");
		System.out.println("getMassage :" + getMassage + ":");
		return (putMassage.equals(getMassage));
	}

	default void mqput(String accessQueueName, MQMessage putMessage) throws MQException {
		setMQEnvironment();
		MQQueueManager qmgr = null;
		MQQueue queue = null;
		try {
			qmgr = new MQQueueManager(qmgrname());
			queue = qmgr.accessQueue(accessQueueName, MQC.MQOO_OUTPUT);

			MQPutMessageOptions mqpmo = new MQPutMessageOptions();
			mqpmo.options = MQC.MQPMO_NO_SYNCPOINT;

			queue.put(putMessage, mqpmo);

		} finally {
			mqclose(qmgr, queue);
		}
	}

	default void mqput(String accessQueueName, String massage) throws IOException, MQException {
		mqput(accessQueueName, createMQMessage(massage));
	}

	default MQMessage mqGet(String accessQueueName) throws MQException {
		return mqGet(accessQueueName, new MQGetMessageOptions(), new MQMessage());
	}

	default MQMessage mqGetWaitMsgid(String accessQueueName, byte[] messageId) throws MQException {

		MQGetMessageOptions mqgmo = new MQGetMessageOptions();
		mqgmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;

		return mqGetWait(accessQueueName, mqgmo, new MQMessage());
	}

	default MQMessage mqGetWaitCorrelid(String accessQueueName, byte[] messageId) throws MQException {

		MQGetMessageOptions mqgmo = new MQGetMessageOptions();
		mqgmo.matchOptions = MQC.MQMO_MATCH_CORREL_ID;

		MQMessage getMsg = new MQMessage();
		getMsg.correlationId = messageId;
		return mqGetWait(accessQueueName, mqgmo, getMsg);
	}

	default String mqGetString(String accessQueueName) throws IOException, MQException {
		return toStringMQMessage(mqGet(accessQueueName, new MQGetMessageOptions(), new MQMessage()));
	}

	default String mqGetWaitString(String accessQueueName) throws IOException, MQException {

		return toStringMQMessage(mqGetWait(accessQueueName));

	}

	default MQMessage mqGetWait(String accessQueueName) throws MQException {

		return mqGetWait(accessQueueName, new MQGetMessageOptions(), new MQMessage());
	}

	default MQMessage mqGetWait(String accessQueueName, MQGetMessageOptions mqgmo, MQMessage getMessage)
			throws MQException {

		mqgmo.options = MQC.MQGMO_WAIT;
		mqgmo.waitInterval = 10000;

		return mqGet(accessQueueName, mqgmo, getMessage);
	}

	default MQMessage mqGet(String accessQueueName, MQGetMessageOptions mqgmo, MQMessage getMessage)
			throws MQException {
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

		} finally {
			mqclose(qmgr, queue);
		}
	}

	default void mqerr(Exception e) {
		System.out.println("Exception occurred");
		e.printStackTrace();
	}

	default void mqclose(MQQueueManager qmgr, MQQueue queue) throws MQException {
		if (queue != null) {
			queue.close();
		}

		if (Objects.nonNull(qmgr)) {
			qmgr.disconnect();
		}
	}
}
