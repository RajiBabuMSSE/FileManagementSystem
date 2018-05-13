package com.example.demo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.example.demo.model.UserContentModel;
import com.example.demo.model.UserContentModelKeys;
import com.example.demo.repository.UserContentRepository;

@Service
public class AmazonClient {
	private AmazonS3 s3client;

	@Value("${endpointUrl}")

	private String endPointUrl;

	@Value("${bucketName}")
	private String bucketName;

	@Value("${accessKey}")
	private String accessKey;

	@Value("${secretKey}")
	private String secretKey;

	private String fileUrl;

	@Autowired
	UserContentRepository userContentRepo;

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3client = new AmazonS3Client(credentials);
	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	private String generateFileName(MultipartFile multiPart) {
		return multiPart.getOriginalFilename();
	}

	private void uploadFileToS3bucket(String fileName, File file) {

		List<PartETag> partETags = new ArrayList<PartETag>();
		long contentLength = file.length();
		long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.
		// Initiate the multipart upload.
		InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucketName, fileName);
		InitiateMultipartUploadResult initResponse = s3client.initiateMultipartUpload(initRequest);

		// Upload the file parts.
		long filePosition = 0;
		for (int i = 1; filePosition < contentLength; i++) {
			// Because the last part could be less than 5 MB, adjust the part size as
			// needed.
			partSize = Math.min(partSize, (contentLength - filePosition));

			// Create the request to upload a part.
			UploadPartRequest uploadRequest = new UploadPartRequest().withBucketName(bucketName).withKey(fileName)
					.withUploadId(initResponse.getUploadId()).withPartNumber(i).withFileOffset(filePosition)
					.withFile(file).withPartSize(partSize);

			// Upload the part and add the response's ETag to our list.
			UploadPartResult uploadResult = s3client.uploadPart(uploadRequest);
			partETags.add(uploadResult.getPartETag());

			filePosition += partSize;
		}

		// Complete the multipart upload.
		CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucketName, fileName,
				initResponse.getUploadId(), partETags);
		s3client.completeMultipartUpload(compRequest);
	}

	public UserContentModel uploadFile(MultipartFile multipartFile, String file_description, Long user_id) {
		String fileName = null;
		try {
			File file = convertMultiPartToFile(multipartFile);
			String fileNamewithExt = generateFileName(multipartFile);
			fileName = FilenameUtils.removeExtension(fileNamewithExt);
			this.fileUrl = endPointUrl + "/" + bucketName + "/" + fileNamewithExt;

			System.out.println("FileURL" + fileUrl);
			uploadFileToS3bucket(fileNamewithExt, file);
			file.delete();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return updateDB(user_id, fileName, file_description, fileUrl);
	}

	private UserContentModel updateDB(Long user_id, String file_name, String file_description, String url) {
		UserContentModel userContent = new UserContentModel();
		Date uploaded_on = new java.sql.Timestamp(new java.util.Date().getTime());
		Date updated_on = new java.sql.Timestamp(new java.util.Date().getTime());
		userContent = new UserContentModel(new UserContentModelKeys(user_id, url), file_description, file_name,
				uploaded_on, updated_on);

		return userContentRepo.save(userContent);
	}

	public String deleteFile(Long user_id, String url) {

		String fileName = url.substring(url.lastIndexOf("/") + 1);
		s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));
		deleteFromDB(user_id, url);
		return "Successfully deleted";
	}

	private void deleteFromDB(Long user_id, String url) {

		userContentRepo.deleteFile(user_id, url);

	}

	public void downloadFileFromS3Bucket(Long user_id, String fileURL, HttpServletResponse response)
			throws IOException {
		String fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1);

		try {

			S3Object o = s3client.getObject(bucketName, fileName);
			S3ObjectInputStream s3is = o.getObjectContent();

			String mimeType = URLConnection.guessContentTypeFromName(new File(fileURL).getName());

			if (mimeType == null) {
				// unknown mimetype so set the mimetype to application/octet-stream
				mimeType = "application/octet-stream";
			}
			response.setContentType(mimeType);
			response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + fileName + "\""));

			response.setContentLength((int) fileName.length());

			// InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

			FileCopyUtils.copy(s3is, response.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return response;
	}

}
