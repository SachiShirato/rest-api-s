package jp.co.acom.fehub.mq;

public interface QMFH01Test extends MQTest {

	String HOSTNAME = "localhost";
	String CHANNEL = "SYSTEM.BKR.CONFIG";
	int PORT = 50014;
	String QUEUE_MANAGER_NAME = "QMFH01";

	String ACCESS_QUEUE_NAME = QUEUE.QL_DH_ERR.getQName();

	@Override
	default String qmgrName() {
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
