package com.example.mq;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

public class Mqtestmain implements QMFH01Test {

	@BeforeEach
	void setUpAll() throws Exception {
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
		mqchek(putMassage, getMassage);
//put（入力getのボディー)
		if (getMassage != null) {
			mqput(ACCESS_QUEUE_NAME, getMassage);
//get(MQメッセージを受け取る)
			MQMessage getMQmassage = mqget(ACCESS_QUEUE_NAME, "a");

			// 前後の確認
			mqchek(putMQmassage, getMQmassage);

		}

	}

}
