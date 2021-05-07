package jp.co.acom.fehub.test;

import static org.junit.Assert.assertNotNull;
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
	// TODO 定数 (白 済）
	final private String normalPath = "/ts200.xml";

	// TODO (白 済）
	Stream<Arguments> params_Normal_Origin() throws Exception {
		return Stream.of(Arguments.of(pathToString(normalPath), QUEUE.QC_DH_REQ.getQName()),
				Arguments.of(pathToString(normalPath), QUEUE.QL_DH_REQ.getQName()));
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class StartingRizaTest {

		// TODO (白 済）
		Stream<Arguments> params_Normal() throws Exception {
			return params_Normal_Origin();
		}

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test1and5and8_Normal")
		void test1and5and8_Normal(String str, String q) throws Exception {

			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(q, putMQmessage);
			// TODO putMQmessage.replyToQueueNameの方がカッコいいかも。できなければ元に戻す。トリムがいる (白 済)
			MQMessage getMQmessage = mqGetWaitCorrelid(putMQmessage.replyToQueueName.trim(), putMQmessage.messageId);
			// TODO デッドロジックになってる。あと別ケースで! (白 削除済)
			lastCheck(putMQmessage, getMQmessage, false, true);

		}

		// TODO サービスID（XMLがDLはじまり 完全別テストケースへ (白 済)
		@Test
		@DisplayName("test1and5and8_Normal_DL")
		void test1and5and8_Normal_DL() throws Exception {
			MQMessage putMQmessage = setUpCreateMQ(setServiceid(pathToString(normalPath), "DL200"));
			mqput(QUEUE.QL_DH_REQ.getQName(), putMQmessage);
			assertNotNull(mqGetWaitCorrelid(QUEUE.QA_DH_DL.getQName(), putMQmessage.messageId));
		}

		@ParameterizedTest
		@DisplayName("test6_HTTPResponseError")
		@MethodSource("params_HTTPResponseError")
		void test6_HTTPResponseError(String str) throws Exception {
			// TODO putMQmessageは不要 取り込み済み（白）
			mqput(QUEUE.QC_DH_REQ.getQName(), setUpCreateMQ(str));
		}

		Stream<Arguments> params_HTTPResponseError() throws Exception {

			// TODO createMQMessageBody()は使わない ｔｓ３使ってしまう。正常を参照 (白 済)
			return Stream.of(Arguments.of(setServiceid(pathToString(normalPath), "DF999")),
					Arguments.of(setServiceid(pathToString(normalPath), "DF400")),
					Arguments.of(setServiceid(pathToString(normalPath), "DF500")));
		}

		// TODO タイムアウトは待機して終了した方がいいかと。別ケース。 DF300 別ケース→ちょっと待つスレットスリー (白 済)
		@Test
		@DisplayName("test6_HTTPResponseError_TimeOut")
		void test6_HTTPResponseError_TimeOut() throws Exception {
			// TODO putMQmessageは不要 取り込み済み（白）
			mqput(QUEUE.QC_DH_REQ.getQName(), setUpCreateMQ(setServiceid(pathToString(normalPath), "DF300")));
			Thread.sleep(10000);
		}

		// TODO test7_ParseError 済み
		@Test
		@DisplayName("test7_ParseError")
		protected void test7_ParseError() throws Exception {

			// TODO createMQMessageBody()は使わない 済
			MQMessage putMQmessage = setUpCreateMQ((setServiceid(pathToString(normalPath), "DF800")));
			mqput(QUEUE.QC_DH_REQ.getQName(), putMQmessage);

			MQMessage getMQmessage = mqGetWaitMsgid(QUEUE.QL_DH_ERR.getQName(), putMQmessage.correlationId);
			lastCheckMqmd(putMQmessage, getMQmessage, true, true);
			assertEquals(ItemRestController.STR_DF800, messageToString(getMQmessage));
		}

		// TODO プライオリティーキャラクターセットなしver MQExecutorより (白 済 不要ケース？)
		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test_Non_Priority_Characterset")
		void test_Non_Priority_Characterset(String str, String q) throws Exception {
			MQMessage putMQmessage = setUpCreateMQ(str);
			putMQmessage.priority = 0;
			putMQmessage.characterSet = 0;
			mqput(q, putMQmessage);
			MQMessage getMQmessage = mqGetWaitCorrelid(putMQmessage.replyToQueueName.trim(), putMQmessage.messageId);
			lastCheck(putMQmessage, getMQmessage, false, false);
		}

	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@Nested
	class StoppedRizaTest {

		// TODO せっかくなのでparams_Normal使いましょう 済
		Stream<Arguments> params_Normal() throws Exception {
			return params_Normal_Origin();
		}

		@ParameterizedTest
		@MethodSource("params_Normal")
		@DisplayName("test2_HTTPRequestError")
		void test2_HTTPRequestError(String str) throws Exception {

			// TODO createMQMessageBody()は使わない パラメータ使う (白 済)
			MQMessage putMQmessage = setUpCreateMQ(str);
			mqput(QUEUE.QC_DH_REQ.getQName(), putMQmessage);

			lastCheck(putMQmessage, mqGetWaitMsgid(GET_QUEUE_NAME, putMQmessage.correlationId), false, false);
		}
	}
}
