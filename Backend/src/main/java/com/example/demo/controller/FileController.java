package com.example.demo.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.UserContentModel;
import com.example.demo.model.UserModel;
import com.example.demo.repository.UserContentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AmazonClient;

@RestController
@CrossOrigin("*")
public class FileController {

	private AmazonClient amazonClient;
	@Autowired
	UserRepository userRepo;

	@Autowired
	UserContentRepository userContentRepo;

	FileController(AmazonClient amazonClient) {
		this.amazonClient = amazonClient;

	}

	@GetMapping("/getUserProfile")
	public List<UserModel> getUserProfile() {

		return userRepo.findAll();

	}

	@GetMapping("/getUserContent/{user_id}")
	public List<UserContentModel> getUserContent(@PathVariable(value = "user_id") Long user_id) {
		return userContentRepo.getUserContent(user_id);

	}

	@PostMapping("/uploadFile")
	public UserContentModel uploadFile(@RequestPart(value = "file") MultipartFile multipartFile,
			@RequestParam(value = "file_description") String file_description,
			@RequestParam(value = "user_id") Long user_id) {
		System.out.println("ID " + user_id);
		return this.amazonClient.uploadFile(multipartFile, file_description, user_id);

	}

	@DeleteMapping("/deleteFile/{user_id}")
	public String deleteFile(@PathVariable(value = "user_id") String user_id, @RequestParam(value = "url") String url)
			throws UnsupportedEncodingException {
		String decodedURL = java.net.URLDecoder.decode(url, "UTF-8");
		return this.amazonClient.deleteFile(Long.valueOf(user_id), decodedURL);
	}

	@GetMapping("/downloadFile/{user_id}")
	public void downloadFile(@PathVariable(value = "user_id") Long user_id, @RequestParam(value = "url") String url,
			HttpServletResponse response) throws IOException {
		String decodedURL = java.net.URLDecoder.decode(url, "UTF-8");
		this.amazonClient.downloadFileFromS3Bucket(user_id, decodedURL, response);
	}

}
