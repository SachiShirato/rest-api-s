package com.example.mq;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.ibm.msg.client.wmq.compat.base.internal.MQEnvironment;

public class Mqtestmain {
	
	private static final String HOSTNAME = "localhost";
	private static final String CHANNEL = "SYSTEM.BKR.CONFIG";
	private static final int PORT = 50014;
	private static final String QUEUE_MANAGER_NAME = "QMFH01";
//編集 キューの指定	
	private static final String ACCESS_QUEUE_NAME = "QL.DH.ERR";

	@BeforeAll
	static void setUpAll() throws Exception {
		System.out.println("クラス共通設定");

		MQEnvironment.hostname = HOSTNAME;
		MQEnvironment.channel = CHANNEL;
		MQEnvironment.port = PORT;
	}

	@Test
	void test() throws Exception {
//編集　メッセージ
		String putMassage = "This is a message";
//put　編集ACCESS_QUEUE_NAME
		Mqstb.mqput(QUEUE_MANAGER_NAME, ACCESS_QUEUE_NAME, putMassage);
//get　編集ACCESS_QUEUE_NAME
		String getMassage = Mqstb.mqget(QUEUE_MANAGER_NAME, ACCESS_QUEUE_NAME);
		System.out.println(getMassage);

	}
}
