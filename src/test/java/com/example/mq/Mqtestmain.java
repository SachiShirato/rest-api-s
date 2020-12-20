package com.example.mq;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

public class Mqtestmain implements QMFH01Test {

	@BeforeEach
	void setUpAll() throws Exception {
		mqput(ACCESS_QUEUE_NAME, "e");
		mqgetnull(ACCESS_QUEUE_NAME);

	}

	@Test
	protected void test() throws IOException {
//編集　メッセージ（入力）
		String putMassage = "This is a meaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
//put(入力がMQメッセージ)
		MQMessage putMQmassage = createMQMessage(putMassage);
		mqput(ACCESS_QUEUE_NAME, putMQmassage);
//get(ボディーを受け取る）
		String getMassage = mqget(ACCESS_QUEUE_NAME);

		// 前後の確認
		assertEquals(putMassage, getMassage);
		System.out.println("putMassage :"+putMassage+":");
		System.out.println("getMassage :"+getMassage+":");
//put（入力getのボディー)
		if (getMassage != null) {
			mqput(ACCESS_QUEUE_NAME, getMassage);
//get(MQメッセージを受け取る)
			MQMessage getMQmassage = mqget(ACCESS_QUEUE_NAME,"a");

			// 前後の確認
			assertEquals(readLine(putMQmassage), readLine(getMQmassage));
			System.out.println("putMQmassage(body) :"+readLine(putMQmassage)+":");
			System.out.println("getMQmassage(body) :"+readLine(getMQmassage)+":");
			
			assertEquals(putMQmassage.priority, getMQmassage.priority);
			System.out.println("putMQmassage(priority) :"+putMQmassage.priority);
			System.out.println("getMQmassage(priority) :"+getMQmassage.priority);
			
			assertEquals(putMQmassage.characterSet, getMQmassage.characterSet);
			System.out.println("putMQmassage(characterSet) :"+putMQmassage.characterSet);
			System.out.println("getMQmassage(characterSet) :"+getMQmassage.characterSet);

		}

	}

}
