package com.az; //Push to Git and GitHub

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class FileHash {
	private static int scanResultNumber = 0;
	private static String fileDigestAlgorithm = "MD5";
	private static String[] startPaths = {"/Volumes/MacOS Data/Users/Andy", 
			"/Volumes/Movie & Episode - 4TB", 
			"/Volumes/Time Machine_3.5in-4TB/Andy", 
			"/Volumes/Time Machine_3.5in-4TB/Backup.Seagate 1TB - 03-06",
			"/Volumes/Time Machine_3.5in-4TB/Software",
			"/Volumes/Time Machine_3.5in-4TB/VM Template",
			"/Volumes/Seagate 1TB - 2.5inch"};
//	private static String[] startPaths = {"/Volumes/MacOS Data/Users/Andy/Downloads/业支第三代", 
//			"/Volumes/MacOS Data/Users/Andy/Downloads/七中"};
	private static long fileSizeMin = 1 * 1024 * 1024l;
	private static long fileSizeMax = 1024 * 1024 * 1024l;

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		HashMap <String, Long> fileListMap = new HashMap<String, Long>();
		for (int id = 0; id < startPaths.length; id ++) {
			System.err.println("Scaning... " + startPaths[id]);
			fileListMap.putAll(scanFolderMap(startPaths[id]));
			System.err.println("Complete   " + startPaths[id]);
		}
		
		ArrayList<HashMap.Entry<String, Long>> list = new ArrayList<HashMap.Entry<String, Long>>(fileListMap.entrySet());

		Collections.sort(list, new Comparator<HashMap.Entry<String, Long>>(){
			public int compare(HashMap.Entry<String, Long> l1, HashMap.Entry<String, Long> l2) {
				return ((new Long(l2.getValue() - l1.getValue())).intValue());
			}
		});
		boolean isSameSize = false;
		for (int i = 0; i < list.size(); i ++) {
			long fileSize = list.get(i).getValue();
			String fileName = list.get(i).getKey();
			if (fileSize >= fileSizeMin && fileSize <= fileSizeMax) {
				if (i + 1 < list.size()) {
					if (fileSize == list.get(i + 1).getValue()) {
						isSameSize = true;
						System.out.println("Code: [" + calcFileMD(fileName) + "], Size: [" + fileSize + "], Name: [" + fileName + "]");
					} else if (isSameSize) {
						System.out.println("Code: [" + calcFileMD(fileName) + "], Size: [" + fileSize + "], Name: [" + fileName + "]");
						isSameSize = false;
					}
				} else {
					System.out.println("Code: [" + calcFileMD(fileName) + "], Size: [" + fileSize + "], Name: [" + fileName + "]");
				}
//			} else {
//				System.out.println("Size: [" + fileSize + "], Name: [" + fileName + "]");
			}
		}
		System.err.println(scanResultNumber);
	}
	
	public static HashMap<String, Long> scanFolderMap(String path) throws NoSuchAlgorithmException, IOException {
		HashMap<String, Long> fListMap = new HashMap<String, Long>(1024 * 1024, (float) 0.75);
		File file = new File(path);
		
		if (!file.exists()) {
			System.err.println("文件不存在 - " + path);
			return null;
		}
		
		if (!file.isDirectory()) {
			System.err.println("指定的名称不是文件夹 - " + path);
			return null;
		}
		
		if (!(Paths.get(path).normalize()).equals(Paths.get(path).toRealPath())) {
			System.err.println("指定的名称是软连接 - " + path + "，真实的文件（夹）是：" + (Paths.get(path).toRealPath()).toString() + "Normalize之后是：" + (Paths.get(path).normalize()).toString());
			return null;
		}
		
		File[] files = file.listFiles();
		
		try {
			if (files.length == 0) {
				System.err.println("文件夹是空的 - " + path);
				return null;
			}
			for (File f : files) {
				String filePath = f.getAbsolutePath(); 
				if (f.isDirectory()) {
					HashMap <String, Long> fl = scanFolderMap(filePath);
					if (null != fl) {
						fListMap.putAll(fl);
					}
				} else {
					fListMap.put(filePath, f.length());
					scanResultNumber ++;
				}
			}
		} catch (NullPointerException e) {
			System.err.println("文件夹无法访问 - " + path);
			e.printStackTrace();
		}
		
		return fListMap;
	}

	public static String calcFileMD(String fileName) throws NoSuchAlgorithmException {
		byte[] buffer = new byte[1024];
		int len = 1;
		
		File file = new File(fileName);
		try {
			FileInputStream fis = new FileInputStream(file);
			MessageDigest md = MessageDigest.getInstance(fileDigestAlgorithm);
			while ((len = fis.read(buffer, 0, 1024))!= -1) {
				md.update(buffer, 0, len);
			}
			BigInteger fileMD = new BigInteger(1, md.digest());
			fis.close();
			return fileMD.toString(16);
		} catch (IOException e) {
			System.err.println("Open File Failed! - " + fileName);
		}
		return "Calculate MD5 Failed!";
	}
}
