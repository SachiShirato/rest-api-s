package com.example.mq;

import java.io.EOFException;
import java.io.IOException;

import com.ibm.mq.MQException;
import com.ibm.msg.client.wmq.compat.base.internal.MQGetMessageOptions;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;
import com.ibm.msg.client.wmq.compat.base.internal.MQQueue;
	
public class mqstb {


//put
	public static MQMessage mqput(int priority,int characterSet,String massage) throws IOException {
		MQMessage putMessage = new MQMessage();
		putMessage.priority = priority;
		putMessage.characterSet = characterSet;
		putMessage.writeString(massage);
		return putMessage;
	};
//get
	public static String mqget(MQQueue queue,int length) throws MQException, EOFException, IOException {
		MQMessage getMessage = new MQMessage();
		MQGetMessageOptions mqgmo = new MQGetMessageOptions();
		queue.get(getMessage, mqgmo);
		String strMessage = getMessage.readStringOfByteLength(length);
		return strMessage;
	}
		
}
