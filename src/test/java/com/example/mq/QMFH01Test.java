package com.example.mq;

public interface QMFH01Test extends MQTest {

//	private static final String HOSTNAME = "localhost";
//	private static final String CHANNEL = "SYSTEM.BKR.CONFIG";
//	private static final int PORT = 50014;
//	private static final String QUEUE_MANAGER_NAME = "QMFH01";

	String HOSTNAME = "localhost";
	String CHANNEL = "SYSTEM.BKR.CONFIG";
	int PORT = 50014;
	String QUEUE_MANAGER_NAME = "QMFH01";
	String ACCESS_QUEUE_NAME = "QL.DH.ERR";

//	@Override
	default String qmgrname() {
		return QUEUE_MANAGER_NAME;
	}

	@Override
	default String host() {
		return HOSTNAME;
	}

	@Override
	default String channel() {
		return CHANNEL;
	}

	@Override
	default int port() {
		return PORT;
	}

}
