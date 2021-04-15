package jp.co.acom.fehub.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.example.api.ItemRestController;
import com.ibm.msg.client.wmq.compat.base.internal.MQMessage;

import jp.co.acom.fehub.mq.QUEUE;

public class HttpClientIta extends HttpClientMain {
	

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class StartingRizaTest {
		// TODO パスをts3にする
		String normalPath = "/ts200.xml";

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test1and5_Normal")
		void test1and5_Normal(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(QUEUE.QC_DH_REQ.getQName(), putMQmessage);
			lastCheck(putMQmessage, mqGetWaitCorrelid(GET_QUEUE_NAME, putMQmessage.messageId), GET_QUEUE_NAME, 0);

		}

		Stream<Arguments> params_Normal() throws Exception {
			return Stream.of(Arguments.of(pathToString(normalPath)), Arguments.of(setRc(pathToString(normalPath), "")),
					Arguments.of(setRequestid(pathToString(normalPath), "")));
		}

		@ParameterizedTest
		@DisplayName("test6_HTTPResponseError")
		@MethodSource("params_HTTPResponseError")
		void test6_HTTPResponseError(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);
		}

		Stream<Arguments> params_HTTPResponseError() throws Exception {

			return Stream.of(Arguments.of(setServiceid(createMQMessageBody(), "DF999")),
					Arguments.of(setServiceid(createMQMessageBody(), "DF300")),
					Arguments.of(setServiceid(createMQMessageBody(), "DF400")),
					Arguments.of(setServiceid(createMQMessageBody(), "DF500")));
		}

		@Test
		@DisplayName("test7_NonXml")
		protected void test7_NonXml() throws Exception {

			MQMessage putMQmessage = setUpCreateMQ((setServiceid(createMQMessageBody(), "DF800")));
			mqput(QUEUE.QC_DH_REQ.getQName(), putMQmessage);

			MQMessage getMQmessage = mqGetWaitMsgid(QUEUE.QL_DH_ERR.getQName(), putMQmessage.correlationId);
			lastCheckMqmd(putMQmessage, getMQmessage, true, false);
			assertEquals(ItemRestController.STR_DF800, toStringMQMessage(getMQmessage));
		}

		@Test
		@DisplayName("test8_Normal_HeadDL")
		protected void test8_Normal_HeadDL() throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(setServiceid(pathToString(normalPath), "DL200"));
			mqput(QUEUE.QC_DH_REQ.getQName(), putMQmessage);
			// TODO DLどこまで確認するか
			mqGetWaitMsgid(QUEUE.QA_DH_DL.getQName(), putMQmessage.correlationId);
//			MQMessage getMQmessage = mqGetWaitMsgid(QUEUE.QA_DH_DL.getQName(), putMQmessage.correlationId);
//			lastCheck(putMQmessage, getMQmessage, QUEUE.QA_DH_DL.getQName(), 1);
		}

	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class StoppedRizaTest {

		@Test
		@DisplayName("test2_HTTPRequestError")
		void test2_HTTPRequestError() throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(createMQMessageBody());

			mqput(QUEUE.QL_DH_HTTP_LSR.getQName(), putMQmessage);

//			lastCheck(putMQmessage, mqGetWaitMsgid(QUEUE.QL_DW_REP.getQName(), putMQmessage.correlationId),
//					QUEUE.QL_DW_REP.getQName(), 0);
		}

	}
}
