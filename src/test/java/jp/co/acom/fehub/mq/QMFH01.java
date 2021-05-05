package jp.co.acom.fehub.mq;

// TODO QMFH01Test → QMFH01 改名（白　済）
public interface QMFH01 extends MQExecutor {

	String HOSTNAME = "localhost";

	String CHANNEL = "SYSTEM.BKR.CONFIG";

	int PORT = 50014;

	String QUEUE_MANAGER_NAME = "QMFH01";

	String GET_QUEUE_NAME = QUEUE.QL_DW_REP.getQName();

	// TODO ERROR_QUEUE_NAME (白 済)
	String ERROR_QUEUE_NAME = QUEUE.QL_DH_ERR.getQName();

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
