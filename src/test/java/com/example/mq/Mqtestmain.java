package com.example.mq;

import org.junit.jupiter.api.Test;

//public class Mqtestmain extends Mqtestenv {
public class Mqtestmain implements QMFH01Test {

//	private static final String HOSTNAME = "localhost";
//	private static final String CHANNEL = "SYSTEM.BKR.CONFIG";
//	private static final int PORT = 50014;
//	private static final String QUEUE_MANAGER_NAME = "QMFH01";
//編集 キューの指定	
//	private static final String ACCESS_QUEUE_NAME = "QL.DH.ERR";
//
//	@Override
//	protected String qmgrname() {
//		return QUEUE_MANAGER_NAME;
//	}
//
//	@Override
//	protected String host() {
//		return HOSTNAME;
//	}
//
//	@Override
//	protected String channel() {
//		return CHANNEL;
//	}
//
//	@Override
//	protected int port() {
//		return PORT;
//	}

//	@BeforeAll
//	static void setUpAll() throws Exception {
//		System.out.println("クラス共通設定");
//
////		MQEnvironment.hostname = HOSTNAME;
////		MQEnvironment.channel = CHANNEL;
////		MQEnvironment.port = PORT;
//	}
//

// 編集 キューの指定

	@Test
	protected void test() {
//編集　メッセージ
		String putMassage = "This is a message";
//put
		//MQメッセージも作る（putMassage がbody）
		mqput(ACCESS_QUEUE_NAME, putMassage);
		
		//put
				//MQメッセージも作る（putMassage がbody）
				mqput(ACCESS_QUEUE_NAME, putMassage);
//get
		String getMassage = mqget(ACCESS_QUEUE_NAME);
		
		System.out.println(getMassage.length());
		//返ってきたmassage（ボディー）を入力（get）にして、MQメッセージをとる
	}
}
