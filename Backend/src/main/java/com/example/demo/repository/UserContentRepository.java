package com.example.demo.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.model.UserContentModel;


public interface UserContentRepository extends JpaRepository<UserContentModel, Long>{
	
	
	@Modifying
	@Transactional
	@Query(value = "delete from user_content where user_id = ?1 and url = ?2 " , nativeQuery = true )
	void deleteFile(Long user_id, String url);
	
	@Query(value = "Select * from user_content where user_id =?", nativeQuery = true)
	List<UserContentModel> getUserContent(Long user_id);
}
