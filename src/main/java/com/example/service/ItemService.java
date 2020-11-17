package com.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.Item;
import com.example.repository.ItemRepository;


@Service
@Transactional
public class ItemService {
	@Autowired
	ItemRepository itemRepository;

	
	/**
	 * 商品一覧取得サービス
	 * @return List<item>
	 */
	public List<Item> findAll() {
		return itemRepository.findAll();
	}
	
	/**
	 * 商品登録サービス
	 * @param item
	 * @return item
	 */
	public Item create(Item item) {
		return itemRepository.save(item);
	}
	/**
	 * 商品削除サービス
	 * @param id
	 */
	public void delete(Integer id) {
		itemRepository.deleteById(id);
	}
	
	/**
	 * 商品更新サービス
	 * @param item
	 * @return item
	 */
	public Item update(Item item) {
		return itemRepository.save(item);
	}

	/** ID検索   */
	public Optional<Item> findById(Integer id) {
		// TODO 自動生成されたメソッド・スタブ
		return itemRepository.findById(id);
	}

	/** 名前検索  
	public Optional<Item> findByName(String name) {

		// TODO 自動生成されたメソッド・スタブ
		
		return itemRepository.findByName(name);

	}
 */	
/*	
	public Optional<Item> findByIdAndName(String name) {

		// TODO 自動生成されたメソッド・スタブ
		
		return itemRepository.findByIdAndName(idandname);

	}
*/
	public Optional<Item> findByNamePrice(String name,Integer price) {
		// TODO 自動生成されたメソッド・スタブ
		return itemRepository.findByNameAndPrice(name,price);
	}




}
