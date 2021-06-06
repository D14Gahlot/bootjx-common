package com.boot.jx.postman.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.model.CommonFile;
import com.boot.utils.ArgUtil;
import com.boot.utils.UniqueID;

@Component
public class PMFileStoreClient {

	@Autowired
	private AWSFileStore awsFileStore;

	public MultipartFile toMultipartFile(CommonFile commonFile) {
		return new CommonFile().url(commonFile.getUrl()).format(commonFile.getFileFormat())
				.name(ArgUtil.nonEmpty(commonFile.getName(), UniqueID.generateString())).toMultipartFile();
	}

	public CommonFile upload1(MultipartFile file, String pathFolder, String fileName) {
		return awsFileStore.upload1(file, pathFolder, fileName);
	}

	public CommonFile upload2(MultipartFile file, String pathFolder, String fileName) {
		return awsFileStore.upload2(file, pathFolder, fileName);
	}

	public CommonFile upload2(CommonFile commonFile, String pathFolder, String fileName) {
		MultipartFile multiParFile = toMultipartFile(commonFile);
		return awsFileStore.upload2(multiParFile, pathFolder, fileName);
	}

	// Session based File Uploads
	public CommonFile createSessionFile(CommonFile srcFile, String sessionId, String fileId) {
		String folderPath = String.format("%s/session/%s", AppContextUtil.getTenant(), sessionId);
		String fileName = String.format("%s_%s", fileId,
				ArgUtil.nonEmpty(srcFile.getName(), UniqueID.generateString()));
		return awsFileStore.createFile2(srcFile, folderPath, fileName);
	}

	public CommonFile commitSessionFile(CommonFile srcFile, CommonFile dstFile) {
		return awsFileStore.commitFile2(srcFile, dstFile);
	}

	public CommonFile uploadSessionFile(MultipartFile srcFile, String sessionId, String fileId) {
		String folderPath = String.format("%s/session/%s", AppContextUtil.getTenant(), sessionId);
		String fileName = String.format("%s_%s", fileId, srcFile.getOriginalFilename());

		return upload2(srcFile, folderPath, fileName);
	}

	public CommonFile uploadSessionFile(CommonFile srcFile, String sessionId, String fileId) {
		MultipartFile multiParFile = toMultipartFile(srcFile);
		return uploadSessionFile(multiParFile, sessionId, fileId);
	}

	public CommonFile uploadSessionFileAsync(CommonFile srcFile, String sessionId, String fileId) {
		CommonFile dstFile = createSessionFile(srcFile, sessionId, fileId);
		commitSessionFile(srcFile, dstFile);
		return dstFile;
	}
}
