package com.example.api;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.domain.Item;
import com.example.service.ItemService;

@RestController
@RequestMapping("/api/items")
public class ItemRestController {
	@Autowired
	ItemService itemService;

	/**
	 * 商品一覧取得API
	 * 
	 * @return List<item>
	 */
	@GetMapping
	List<Item> getItems() {
		List<Item> customers = itemService.findAll();
		return customers;
	}

	@GetMapping(path = "{id}")
	Optional<Item> getItem(@PathVariable Integer id) {
		return itemService.findById(id);
	}

	/**
	 * 商品名を検索して、結果のみ出力

	@GetMapping("findbyname")
	public Optional<Item> get(@RequestParam("name") String name) {
		return itemService.findByName(name);
	}
	 */
	/**
	 * 商品名を検索して、結果のみ出力
	 */
	@GetMapping("findbynameprice")
	public Optional<Item> get(
			@RequestParam("name") String name,
			@RequestParam("price") Integer price)
			 {
		return itemService.findByNamePrice(name,price);
	}
	
	
	/**
	 * 商品登録API
	 * 
	 * @param item
	 * @return item
	 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	Item postItem(@RequestBody Item item) {
		return itemService.create(item);
	}

	@PostMapping(path = "/DF200")
	@ResponseStatus(HttpStatus.CREATED)
	String postItem(@RequestBody String body) {
		return body;
	}
	
	
	
	/**
	 * 商品削除API
	 * 
	 * @param id
	 */
	@DeleteMapping(path = "{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void deleteItem(@PathVariable Integer id) {
		itemService.delete(id);
	}

	/**
	 * 商品更新API
	 * 
	 * @param id
	 * @param item
	 * @return item
	 */
	@PutMapping(path = "{id}")
	Item putItem(@PathVariable Integer id, @RequestBody Item item) {
		item.setId(id);
		return itemService.update(item);
	}

}