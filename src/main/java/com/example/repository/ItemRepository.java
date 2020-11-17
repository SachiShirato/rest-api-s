package com.example.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.domain.Item;


public interface ItemRepository extends JpaRepository<Item, Integer> {

	Optional<Item> findByNameAndPrice(String name, Integer price);

//	Optional<Item> findByName(String name);


}	

    // SELECT e FROM Employee e WHERE e.firstname = ?1 

