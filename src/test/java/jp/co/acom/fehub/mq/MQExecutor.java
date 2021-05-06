package jp.co.acom.fehub.mq;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;
import java.util.List;

import com.ibm.mq.MQException;
import com.ibm.mq.constants.CMQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQC;
import com.ibm.msg.client.wmq.compat.base.internal.MQEnvironment;
import com.ibm.msg.client.wmq.compat.base.internal.MQGetMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;
import com.ibm.msg.client.wmq.compat.base.internal.MQPutMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueue;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueueManager;

//TODO MQTest → MQExecutor　(白済)
public interface MQExecutor {
	// TODO 下記二つをいれないで実行したケースを増やす ・ なにか入るはず

	int PRIORITY_5 = 5;
	int CHARACTER_SET_1208 = 1208;

	String qmgrName();

	String host();

	String channel();

	int port();

	default void setMQEnvironment() {

		MQEnvironment.hostname = host();
		MQEnvironment.channel = channel();
		MQEnvironment.port = port();
	}

	default MQMessage createMQMessage(String mqMessageBody) throws IOException {

		MQMessage putBody = new MQMessage();
		putBody.format = MQC.MQFMT_STRING;
		putBody.characterSet = CHARACTER_SET_1208;
		putBody.expiry = 30000; // 100ms
		putBody.persistence = MQC.MQPER_NOT_PERSISTENT;
		putBody.messageId = MQC.MQMI_NONE;
		putBody.priority = PRIORITY_5;
		putBody.writeString(mqMessageBody);

		return putBody;
	}

	default MQMessage createMQMessageRequest(String mqMessageBody, String qName) throws IOException {

		MQMessage putBody = createMQMessage(mqMessageBody);
		putBody.messageType = MQC.MQMT_REQUEST;
		putBody.replyToQueueManagerName = qmgrName();
		putBody.replyToQueueName = qName;

		return putBody;
	}

	default boolean mqtoEmpty(List<String> accessQueueNameList) throws IOException, MQException {

		boolean flg = true;

		for (String name : accessQueueNameList) {

			MQMessage str;
			int i = 0;
			do {
				str = mqGet(name);

				if (str != null) {
					flg = false;
					i++;
				}

			} while (str != null);
			if (i > 0) {
				System.out.println("MQname:" + name + "/件数:" + i);
			}
		}

		return flg;
	}

	default void mqputAll(List<String> accessQueueList) throws IOException, MQException {

		for (String name : accessQueueList) {

			mqput(name, "a");
			mqput(name, "b");
			mqput(name, "c");
		}
	}

	// TODO messageToStringがいいです (白済)
	default String messageToString(MQMessage msg) throws IOException {

		if (msg == null)
			return null;

		StringBuilder builder = new StringBuilder();
		msg.setDataOffset(0);
		builder.append(msg.readLine());

		while (msg.getDataLength() > 0)
			builder.append(System.lineSeparator() + msg.readLine());

		return builder.toString();
	}

	default boolean mqCheck(MQMessage putMQmessage, MQMessage getMQmessage) throws IOException {
//TODO 白　修正した
		return ((putMQmessage.priority == (getMQmessage.priority))
				&& (CHARACTER_SET_1208 == (getMQmessage.characterSet)));
	}

	default void mqput(String accessQueueName, MQMessage putMessage) throws MQException {

		setMQEnvironment();

		MQQueueManager qmgr = null;
		MQQueue queue = null;

		try {

			qmgr = new MQQueueManager(qmgrName());
			queue = qmgr.accessQueue(accessQueueName, MQC.MQOO_OUTPUT | MQC.MQOO_SET_IDENTITY_CONTEXT);

			MQPutMessageOptions mqmo = new MQPutMessageOptions();
			mqmo.options = MQC.MQPMO_NO_SYNCPOINT | MQC.MQPMO_SET_IDENTITY_CONTEXT;
			queue.put(putMessage, mqmo);

		} finally {

			mqclose(qmgr, queue);
		}
	}

	default void mqput(String accessQueueName, String message) throws IOException, MQException {

		mqput(accessQueueName, createMQMessage(message));
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

		return messageToString(mqGet(accessQueueName, new MQGetMessageOptions(), new MQMessage()));
	}

	default String mqGetWaitString(String accessQueueName) throws IOException, MQException {

		return messageToString(mqGetWait(accessQueueName));
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

			qmgr = new MQQueueManager(qmgrName());
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

		if (queue != null)
			queue.close();

		if (qmgr != null)
			qmgr.disconnect();
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

//TODO (白　感謝)	

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

			qmgr = new MQQueueManager(qmgrName());
			queue = qmgr.accessQueue(qName, MQC.MQOO_SET);
			queue.set(column, value, null);

		} finally {

			mqclose(qmgr, queue);
		}
	}
}
