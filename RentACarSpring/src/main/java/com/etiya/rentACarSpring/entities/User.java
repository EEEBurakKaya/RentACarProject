package com.etiya.rentACarSpring.entities;

import java.util.List;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "users")
public class User  {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private int userId;
	
	@Column (name="user_email")
	private String email;
	
	@Column (name="user_password")
	private String password; 
	
	@Column(name="fidex_score")
	private int findexScore;
	
	@OneToMany(mappedBy = "user")
	private List<Rental> rentals;

}
