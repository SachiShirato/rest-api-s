package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.domain.Item;

public interface ItemRepository extends JpaRepository<Item, Integer>{
}