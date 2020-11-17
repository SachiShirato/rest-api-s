package com.example.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.Data;




@Entity
@Table(name = "items")
@Data

public class Item implements Serializable{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private Integer price;
	
	private String imgPath;
/*	
	   @ManyToOne
	    @JoinTable(
	            name="department_employee",
	            joinColumns=@JoinColumn(name="employee_id"),
	            inverseJoinColumns=@JoinColumn(name="department_code")
	        )
*/	

	
}