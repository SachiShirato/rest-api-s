package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URI;
import java.net.URISyntaxException;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

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

	@Autowired
	JdbcTemplate jdbcTemplate;

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
		this.jdbcTemplate.execute("delete from items");
		this.jdbcTemplate.update("INSERT INTO items(id, name, price, img_path) VALUES(1,'りんご', 100,  'apple.jpg')");
		this.jdbcTemplate.update("INSERT INTO items(id, name, price, img_path) VALUES(2,'ばなな', 200, 'banana.jpg')");
		this.jdbcTemplate.update("INSERT INTO items(id, name, price, img_path) VALUES(3,'みかん', 300, 'mikan.jpg')");
		this.jdbcTemplate.update("INSERT INTO items(id, name, price, img_path) VALUES(4,'ぶどう', 400, 'glape.jpg')");
//		this.jdbcTemplate.update("INSERT INTO items(name, price, img_path) VALUES('りんご', 100,  'apple.jpg')");
//		this.jdbcTemplate.update("INSERT INTO items(name, price, img_path) VALUES('ばなな', 200, 'banana.jpg')");
//		this.jdbcTemplate.update("INSERT INTO items(name, price, img_path) VALUES('みかん', 300, 'mikan.jpg')");
//		this.jdbcTemplate.update("INSERT INTO items(name, price, img_path) VALUES('ぶどう', 400, 'glape.jpg')");

		System.out.println("********************************");
		System.out.println("*テスト実行前*");

		this.jdbcTemplate.queryForList("select * from items").forEach(System.out::println);
		System.out.println("　");
	}

	@AfterEach
	void tearDown() throws Exception {
		System.out.println("　");
		System.out.println("*テスト実行後*");
		this.jdbcTemplate.queryForList("select * from items").forEach(System.out::println);

		System.out.println("********************************");
//		this.jdbcTemplate.execute("delete from items");

	}

//	@Test
//	void test() {
//		System.out.println("---");
//		this.jdbcTemplate.queryForList("select * from items").forEach(System.out::println);
//		this.jdbcTemplate.execute("delete from items");
//		System.out.println("---");
//		this.jdbcTemplate.queryForList("select * from items").forEach(System.out::println);
//		 
//		this.jdbcTemplate.update("INSERT INTO items(id, name, price, img_path) VALUES(1,'りんご', 100,  'apple.jpg')");
//
////		this.jdbcTemplate.execute("INSERT INTO items(name, price, img_path) VALUES('りんご', 100,  'apple.jpg')");
//		System.out.println("---");
//		this.jdbcTemplate.queryForList("select * from items").forEach(System.out::println);
//	}

	// テスト POST
	@Test
	void postItemTest() {
		System.out.println("*postItemTest*");

//		int id = 5;
		String url = "http://localhost:" + port + "/api/items";

//		System.out
//				.println(testRestTemplate.getForObject("http://localhost:" + port + "/api/items/{id}", Item.class, id));

		// TODO
//		Item item = testRestTemplate.getForObject("http://localhost:" + port + "/api/items/{id}", Item.class, id);

		Item item = new Item();
//		item.setId(id);
		item.setName("sssss");
		item.setPrice(30990);
		item.setImgPath("bananssa.jpg");

		Item result = testRestTemplate.postForObject(url, item, Item.class);
//ID以外の確認
		item.setId(result.getId());
		assertEquals(result, item);

	}

//テスト　GK検索(名前と価格）
	@Test
	void findByNamePriceTest() {
		System.out.println("*findByNamePriceTest*");

		String name = "ばなな";
		Integer price = 200;

		// TODO
		Item item = testRestTemplate.getForObject(
				"http://localhost:" + port + "/api/items/findbynameprice?name=" + name + "&price=" + price, Item.class);

		Item expected = new Item();
		expected.setId(2);
		expected.setName(name);
		expected.setPrice(price);
		expected.setImgPath("banana.jpg");

		// TODO
		assertEquals(item, expected);

	}

	// テスト ID検索
	@Test
	void getItemTest() {
		System.out.println("*getItemTest*");

		int id = 2;
		Item item = testRestTemplate.getForObject("http://localhost:" + port + "/api/items/{id}", Item.class, id);

		Item expected = new Item();
		expected.setId(id);
		expected.setName("ばなな");
		expected.setPrice(200);
		expected.setImgPath("banana.jpg");

		assertEquals(expected, item);
//		assertThat(item).isEqualTo(expected);
//		assertEquals(id, item.getId(), "id");
//
//		assertEquals("ばなな", item.getName());
//
	}

	// テスト PUT
	@Test
	void putItemTest() throws URISyntaxException {
		System.out.println("*putItemTest*");

		int id = 4;

//		System.out
//				.println(testRestTemplate.getForObject("http://localhost:" + port + "/api/items/{id}", Item.class, id));
//
		String url = "http://localhost:" + port + "/api/items/" + id;
		// TODO jdbc

//		Item item = this.jdbcTemplate.queryForObject("select * from items WHERE id=4", Item.class);

//		System.out.println("**" + item);

//		this.jdbcTemplate.queryForList("select * from items WHERE id=4").forEach(System.out::println);
		Item item = new Item();
		item.setId(id);
		item.setName("もも");
		item.setPrice(500);
		item.setImgPath("momo.jpg");

		// put
//		testRestTemplate.put(url, item); // (1)

		// exchange
		URI uri = new URI(url);
		RequestEntity<Item> requestEntity = RequestEntity // (2)
				.put(uri).body(item);

		ResponseEntity<Item> result = testRestTemplate.exchange(requestEntity, Item.class); // (3)
//		url = "http://localhost:" + port + "/api/items/{id}";
//		ResponseEntity<Item> result = testRestTemplate.exchange(url, // (2)
//				HttpMethod.PUT, new HttpEntity<Item>(item, new HttpHeaders()), Item.class, id);

//		System.out.println(result.getBody());

		// TODO
		assertEquals(result.getBody(), item);

	}

	// テスト DELETE
	@Test
	void deleteItemTest() throws URISyntaxException {
		System.out.println("*deleteItemTest*");
//		System.out.println(testRestTemplate.getForObject("http://localhost:" + port + "/api/items", JsonNode.class));

		String url = "http://localhost:" + port + "/api/items/{id}";
		int id = 1;

		// delete
//		testRestTemplate.delete(url, id);

		// exchange
		ResponseEntity<Item> result = testRestTemplate.exchange(url, // (2)
				HttpMethod.DELETE, HttpEntity.EMPTY, Item.class, id);

//		    assertThat(result.getBody()).isEqualTo(3);

//		System.out.println(testRestTemplate.getForObject("http://localhost:" + port + "/api/items", JsonNode.class));
		System.out.println(result.getBody());
//		assertEquals(new Item(), result.getBody());
		assertNull(result.getBody());

//			    assertThat(result.getBody()).isEqualTo(item);

	}

//テスト　全部検索
	@Test
	void getItemsTest() {
		System.out.println("*getItemsTes*");

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
