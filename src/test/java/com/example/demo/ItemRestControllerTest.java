package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import com.example.domain.Item;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ItemRestControllerTest {

	@Autowired // (1)
	private TestRestTemplate testRestTemplate;

	@LocalServerPort // (2)
	private int port;

	public ItemRestControllerTest() {
//		System.out.println("ItemRestControllerTest");
	}

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
//		System.out.println("setUpBeforeClass");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
//		System.out.println("tearDownAfterClass");
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
//		System.out.println("tearDown");
	}
	//テスト　POST
		@Test
		void postItemTest() {
			int id = 2;
		    String url = "http://localhost:" + port + "/api/items";
//		    "http://localhost:" + port + "/api/items/{id}", Item.class, id
		    System.out.println(testRestTemplate.getForObject("http://localhost:" + port + "/api/items/{id}", Item.class, id));
		    Item item = new Item();
		    
		    item.setId(2);
		    item.setName("ばなな");
		    item.setPrice(300);
		    item.setImgPath("banana.jpg");
			


		    Item result = testRestTemplate.postForObject(url, item, Item.class);
		    System.out.println(result);
		    assertThat(result).isEqualTo(item);

		}
	
//テスト　GK検索(名前と価格）
	@Test
	void findByNamePriceTest() {
		String name = "ばなな";
		Integer price = 200;
		Item item = testRestTemplate.getForObject("http://localhost:" + port + "/api/items/findbynameprice?name=ばなな&price=200", Item.class);


		Item expected = new Item();
		expected.setId(2);
		expected.setName(name);
		expected.setPrice(price);
		expected.setImgPath("banana.jpg");

		assertThat(item).isEqualTo(expected);


	}
	//テスト　ID検索
		@Test
		void getItemTest() {
			int id = 2;
			Item item = testRestTemplate.getForObject("http://localhost:" + port + "/api/items/{id}", Item.class, id);

			Item expected = new Item();
			expected.setId(id);
			expected.setName("ばなな");
			expected.setPrice(200);
			expected.setImgPath("banana.jpg");

			assertThat(item).isEqualTo(expected);
			assertEquals(id, item.getId(), "id");

			assertEquals("ばなな", item.getName());

		}

		
		
//		//テスト　PUT
//		@Test
//		void putItemTest() {
//			   String url = "\"http://localhost:\" + port + \"/api/items";
//			    Item item = new Item();
//			    item.setId(4);
//			    item.setName("ぶどう");
//			    item.setPrice(500);
//			    item.setImgPath("budou.jpg");
//
//			    // put
//			    testRestTemplate.put(url, item); // (1)
//
//			    // exchange
//			    URI uri = new URI(url);
//			    RequestEntity<Item> requestEntity = RequestEntity // (2)
//			            .put(uri)
//			            .body(item);
//
//			    ResponseEntity<Item> result = testRestTemplate.exchange(requestEntity, Item.class); // (3)
//			    assertThat(result.getBody()).isEqualTo(item);
//			
//			
//			
//			
//			
//			int id = 2;
//		    String url = "http://localhost:" + port + "/api/items";
////		    "http://localhost:" + port + "/api/items/{id}", Item.class, id
//		    Item item = new Item();
//		    
//		    item.setId(2);
//		    item.setName("ばなな");
//		    item.setPrice(200);
//		    item.setImgPath("banana.jpg");
//			
//
//
//		    Item result = testRestTemplate.postForObject(url, item, Item.class);
//		    assertThat(result).isEqualTo(item);
//			
//			
//
//		}
//		
		
		
		

//テスト　全部検索
	@Test
	void getitems() {

		JsonNode tmp = testRestTemplate.getForObject("http://localhost:" + port + "/api/items", JsonNode.class);

		List<Item> items = new ObjectMapper().convertValue(tmp, new TypeReference<List<Item>>() {
		});

//		@SuppressWarnings("unchecked")
//		List<Item> items = testRestTemplate.getForObject("http://localhost:" + port + "/api/items", List.class);
		int size = items.size();
		assertEquals(4, size);
//		System.out.println(items);

//		items.forEach(item -> System.out.println(item));ラムダ
//		items.forEach(System.out::println);メソッド参照

		IntStream.range(0, size).forEach(i -> {
			Item item = items.get(i);
			System.out.println(item);
			assertEquals(i + 1, item.getId());
		});
		
//		for (int i = 0; i < size; i++) {
//			Item item = items.get(i);
////			System.out.println(item);
//			assertEquals(i + 1, item.getId());
//		}
//
//		Item item = new Item();
//		ResponseEntity<List<Item>> response = item.exchange(
//		  "http://localhost:\" + port + \"/api/items",
//		  HttpMethod.GET,
//		  null,
//		  new ParameterizedTypeReference<List<Item>>(){});
//
//		List<Item> itemAll;
//		itemAll = response.getBody();
//		
//		System.out.println(itemAll);

//	    assertThat(item).isEqualTo(expected);

//	    assertEquals("ばななd", item.getName());

	}

}
