// FileService.java
package com.mmb.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mmb.dao.FileDao; // 패키지 변경
import com.mmb.dto.FileDto; // 패키지 변경

@Service
public class FileService {
	
	@Value("${custom.file.dir}")
	private String fileDir;
	
	private FileDao fileDao;
	
	public FileService(FileDao fileDao) {
		this.fileDao = fileDao;
	}
	
	public void saveFile(MultipartFile file) throws IOException {
		String orgName = file.getOriginalFilename();
		String uuid = UUID.randomUUID().toString();
		String extension = orgName.substring(orgName.lastIndexOf("."));
		String savedName = uuid + extension;
		String savedPath = fileDir + "/" + savedName;
		fileDao.insertFile(orgName, savedName, savedPath);
		
		file.transferTo(new File(savedPath));
	}

	public List<FileDto> getFiles() {
		return fileDao.getFiles();
	}

	public FileDto getFileById(int id) {
		return fileDao.getFileById(id);
	}
}