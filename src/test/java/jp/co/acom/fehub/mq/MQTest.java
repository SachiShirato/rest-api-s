package jp.co.acom.fehub.mq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
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

		putBody.messageType = MQC.MQMT_REQUEST;
		putBody.format = MQC.MQFMT_STRING;
		putBody.persistence = MQC.MQPER_NOT_PERSISTENT;
		putBody.expiry = 30000; // 100ms

		putBody.writeString(MQMassageBody);
		putBody.messageId = MQC.MQMI_NONE;

		return (putBody);

	}

	
	
	
	
	default void mqtoEmpty(List<String> ACCESS_QUEUE_NAME_LIST) throws IOException, MQException {

		for (String name : ACCESS_QUEUE_NAME_LIST) {
			int i = 1;
			MQMessage str;
			do {
				str = mqGet(name);
				if (str != null) {
//					System.out.println("MQname:" + name + "/ごみ:" + str + "/件数:" + i++);
					System.out.println("MQname:" + name + "/件数:" + i++);
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
		return builder.toString();
	}

	default boolean mqCheck(MQMessage putMQmassage, MQMessage getMQmassage) throws IOException {

//		System.out.println("putMQmassage(body) :" + toStringMQMessage(putMQmassage) + ":");
//		System.out.println("getMQmassage(body) :" + toStringMQMessage(getMQmassage) + ":");
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

//		return ((toStringMQMessage(putMQmassage).equals(toStringMQMessage(getMQmassage)))
				return ((putMQmassage.priority == (getMQmassage.priority))
				&& (putMQmassage.characterSet == (getMQmassage.characterSet)));
//				&& (DatatypeConverter.printHexBinary(putMQmassage.messageId)
//						.equals(DatatypeConverter.printHexBinary(getMQmassage.messageId))));
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
			queue = qmgr.accessQueue(accessQueueName, MQC.MQOO_OUTPUT | MQC.MQOO_SET_IDENTITY_CONTEXT);

			MQPutMessageOptions mqmo = new MQPutMessageOptions();
			mqmo.options = MQC.MQPMO_NO_SYNCPOINT | MQC.MQPMO_SET_IDENTITY_CONTEXT;
			queue.put(putMessage, mqmo);

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
		MQMessage getMsg = new MQMessage();
		getMsg.messageId = messageId;

		return mqGetWait(accessQueueName, mqgmo, getMsg);
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
			if (e.getReason() != CMQC.MQRC_NO_MSG_AVAILABLE) {
				mqerr(e);
				throw e;
			}

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

	default String getUnique24() {
		try {
			return new StringBuffer()
					.append(getHex8(InetAddress.getByName(InetAddress.getLocalHost().getHostName()).hashCode()))
					.append(getHex8(new UID().hashCode())).append("00000000").toString().toUpperCase();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	default String getHex8(int value) {
		StringBuffer sb = new StringBuffer(8);
		for (int i = 0; i < 8; i++) {
			sb.append("0123456789abcdef".charAt(value & 15));
			value >>= 4;

		}
		return sb.reverse().toString();
	}
	
	
	
	
	
	default void putEnabled(String qName) throws MQException {
		this.alterQueue(qName, new int[] { MQC.MQIA_INHIBIT_PUT }, new int[] { MQC.MQQA_PUT_ALLOWED });
	}
	/**
	 * キュー属性を PUT(DISABLED)に変更する。
	 * 
	 * @param qName キュー
	 * @throws MQException
	 */
	default void putDisabled(String qName) throws MQException {
		this.alterQueue(qName, new int[] { MQC.MQIA_INHIBIT_PUT }, new int[] { MQC.MQQA_PUT_INHIBITED });
	}
	/**
	 * キュー属性を GET(ENABLED)に変更する。
	 * 
	 * @param qName キュー
	 * @throws MQException
	 */
	default void getEnabled(String qName) throws MQException {
		this.alterQueue(qName, new int[] { MQC.MQIA_INHIBIT_GET }, new int[] { MQC.MQQA_GET_ALLOWED });
	}
	/**
	 * キュー属性を GET(DISABLED)に変更する。
	 * 
	 * @param qName キュー
	 * @throws MQException
	 */
	default void getDisabled(String qName) throws MQException {
		this.alterQueue(qName, new int[] { MQC.MQIA_INHIBIT_GET }, new int[] { MQC.MQQA_GET_INHIBITED });
	}
	/**
	 * キュー属性を変更する。
	 * 
	 * @param qName  キュー
	 * @param column 属性
	 * @param value  値
	 * @throws MQException
	 */
	default void alterQueue(String qName, int[] column, int[] value) throws MQException {
		this.setMQEnvironment();
		MQQueueManager qmgr = null;
		MQQueue queue = null;
		try {
			qmgr = new MQQueueManager(qmgrname());
			queue = qmgr.accessQueue(qName, MQC.MQOO_SET);
			queue.set(column, value, null);
		} finally {
			mqclose(qmgr, queue);
		}
	}
}
