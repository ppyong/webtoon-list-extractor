package io.github.imsejin.file.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import io.github.imsejin.file.model.CompressionFormat;
import io.github.imsejin.file.model.Platform;
import io.github.imsejin.file.model.Webtoon;

/**
 * FileService
 * 
 * @author SEJIN
 */
public class FileService {

	private static final String DELIMITER_PLATFORM = "_";
	
	private static final String DELIMITER_TITLE = " - ";
	
	private static final String DELIMITER_AUTHOR = ", ";
	
	private static final String DELIMITER_COMPLETED = " [\u5B8C]"; // " [完]"

	/**
	 * Returns current working absolute directory
	 * 
	 * @return Current working absolute directory
	 */
	public String getCurrentAbsolutePath() {
		Path path = Paths.get("");
		String currentAbsolutePath = path.toAbsolutePath().toString(); // System.getProperty("user.dir")

		return currentAbsolutePath;
	}
	
	/**
	 * Returns list of files and directories in the path
	 * 
	 * @param Path
	 * @return List of files and directories
	 */
	public List<File> getFilesList(String path) {
		File file = new File(path);
		List<File> filesList = Arrays.asList(file.listFiles());

		return filesList;
	}
	
	/**
	 * Converts list of files and directories to list of webtoons
	 * 
	 * @param List of files and directories
	 * @return List of webtoons
	 */
	public List<Webtoon> convertFilesListToWebtoonsList(List<File> filesList) {
		if (filesList == null) {
			return new ArrayList<>();
		}
		
		List<Webtoon> webtoonsList = new ArrayList<>();
		
		filesList.forEach(file -> {
			String fileName = FilenameUtils.getBaseName(file.getName());
			String fileExtension = FilenameUtils.getExtension(file.getName());
			
			if (file.isFile() && isCompressedFile(fileExtension)) {
				Map<String, Object> webtoonInfo = classifyWebtoonInfo(fileName);
				
				Object title = webtoonInfo.get("title");
				Object author = webtoonInfo.get("author");
				Object platform = webtoonInfo.get("platform");
				Object completed = webtoonInfo.get("completed");
				Object creationTime = getFileOfCreationTime(file);
				Object size = file.length();
				
				Webtoon webtoon = createWebtoon(title, author, platform, completed, creationTime, fileExtension, size);
				webtoonsList.add(webtoon);
				
				// Prints console logs
				System.out.println(webtoon.toString());
			}
		});
		
		// Prints console logs
		System.out.println("\r\nTotal " + webtoonsList.size() + " webtoon" + (webtoonsList.size() > 1 ? "s" : ""));
		
		// Sorts list of webtoons
		webtoonsList.sort(Comparator.comparing(Webtoon::getPlatform).thenComparing(Webtoon::getTitle));
		
		return webtoonsList;
	}
	
	/**
	 * Checks that the file extension is compression format
	 * 
	 * @param File extension
	 * @return Is the file extension compression format?
	 */
	private boolean isCompressedFile(String fileExtension) {
		List<CompressionFormat> formatList = Arrays.asList(CompressionFormat.values());
		boolean result = false;

		for (CompressionFormat format : formatList) {
			// Erase _(underscore)
			String converted = format.name().substring(1);

			if (fileExtension.equalsIgnoreCase(converted)) {
				result = true;
				break;
			}
		}

		return result;
	}
	
	/**
	 * Creates webtoon instance
	 * 
	 * @param Title, author, platform, completed, creation time, file extension, size
	 * @return Webtoon instance
	 */
	@SuppressWarnings("unchecked")
	private Webtoon createWebtoon(Object... attributes) {
		Webtoon webtoon = new Webtoon();

		// Not allow webtoon information to be null
		for (Object attr : attributes) {
			if (attr == null) {
				return webtoon;
			}
		}

		webtoon.setTitle((String) attributes[0]);
		webtoon.setAuthor((List<String>) attributes[1]);
		webtoon.setPlatform((String) attributes[2]);
		webtoon.setCompleted((boolean) attributes[3]);
		webtoon.setCreationTime((String) attributes[4]);
		webtoon.setFileExtension((String) attributes[5]);
		webtoon.setSize((long) attributes[6]);
		
		return webtoon;
	}
	
	/**
	 * Analyzes the file name and classifies it as platform, title, author and completed
	 * 
	 * @param File name
	 * @return Classified webtoon information
	 */
	private Map<String, Object> classifyWebtoonInfo(String fileName) {
		StringBuffer sb = new StringBuffer(fileName);

		// Platform
		int i = sb.indexOf(DELIMITER_PLATFORM);
		String platform = convertAcronymToFullText(sb.substring(0, i));
		sb.delete(0, i + DELIMITER_PLATFORM.length());

		// Title
		int j = sb.indexOf(DELIMITER_TITLE);
		String title = sb.substring(0, j);
		sb.delete(0, j + DELIMITER_TITLE.length());

		// Completed or uncompleted
		boolean completed = fileName.contains(DELIMITER_COMPLETED);
		
		// Author
		List<String> author;
		if (completed) {
			int k = sb.indexOf(DELIMITER_COMPLETED);
			author = Arrays.asList(sb.substring(0, k).split(DELIMITER_AUTHOR));
			sb.delete(0, k + DELIMITER_COMPLETED.length());
		} else {
			author = Arrays.asList(sb.toString().split(DELIMITER_AUTHOR));
		}

		Map<String, Object> map = new HashMap<>();
		map.put("title", title);
		map.put("author", author);
		map.put("platform", platform);
		map.put("completed", completed);

		return map;
	}
	
	/**
	 * Returns creation time of the file
	 * (yyyy-MM-dd HH:mm:ss)
	 * 
	 * @param File
	 * @return Creation time of the file
	 */
	private String getFileOfCreationTime(File file) {
		BasicFileAttributes attributes;
		FileTime time = null;

		try {
			attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			time = attributes.creationTime();
		} catch (IOException e) {
			e.printStackTrace();
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String creationTime = simpleDateFormat.format(new Date(time.toMillis()));

		return creationTime;
	}
	
	/**
	 * Converts acronym of platform to full text
	 * 
	 * @param Acronym of platform
	 * @return Full text of platform
	 */
	private String convertAcronymToFullText(String acronym) {
		List<Platform> platformList = Arrays.asList(Platform.values());
		String result = acronym;

		for (Platform platform : platformList) {
			if (platform.name().equals(acronym)) {
				result = platform.getFullText();
				continue;
			}
		}

		return result;
	}

}
