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
		@DisplayName("test1and5and8_Normal")
		void test1and5and8_Normal(String str, String q) throws Exception {
			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(q, putMQmessage);
			MQMessage getMQmessage = mqGetWaitCorrelid(GET_QUEUE_NAME, putMQmessage.messageId);
			if (getMQmessage == null) {
				MQMessage getMQmessage2 = mqGetWaitMsgid(QUEUE.QA_DH_DL.getQName(), putMQmessage.correlationId);
				lastCheckMqmd(putMQmessage, getMQmessage2, false, false);
			} else {
				lastCheck(putMQmessage, getMQmessage, false, true);
			}

		}

		Stream<Arguments> params_Normal() throws Exception {
			return Stream.of(Arguments.of(setUpCreateXML(normalPath), QUEUE.QC_DH_REQ.getQName()),
					Arguments.of(setUpCreateXML(normalPath), QUEUE.QL_DH_REQ.getQName())

			// TODO 消す不要ケース QC QL
//					,
//					Arguments.of(setRc(pathToString(normalPath), "")),
//					Arguments.of(setRequestid(pathToString(normalPath), ""))
			);
		}

		@ParameterizedTest
		@DisplayName("test6_HTTPResponseError")
		@MethodSource("params_HTTPResponseError")
		void test6_HTTPResponseError(String str) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(QUEUE.QC_DH_REQ.getQName(), putMQmessage);
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
			lastCheckMqmd(putMQmessage, getMQmessage, true, true);
			assertEquals(ItemRestController.STR_DF800, toStringMQMessage(getMQmessage));
		}

	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class StoppedRizaTest {

		@Test
		@DisplayName("test2_HTTPRequestError")
		void test2_HTTPRequestError() throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(createMQMessageBody());
			mqput(QUEUE.QC_DH_REQ.getQName(), putMQmessage);
			lastCheck(putMQmessage, mqGetWaitMsgid(GET_QUEUE_NAME, putMQmessage.correlationId), false, false);
		}

	}
}
