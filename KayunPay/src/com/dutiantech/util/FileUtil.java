package com.dutiantech.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileUtil {
	@SuppressWarnings("unused")
	public static void downFIle(String filePath,String fileName,HttpServletResponse response,HttpServletRequest request) throws Exception{
		response.reset();
		java.io.File f = new java.io.File(filePath);
		response.setContentType("application/x-download");
		response.setHeader("Content-Disposition", "attachment;filename="
				+ java.net.URLEncoder.encode(fileName, "UTF8"));
		response.setContentLength((int) f.length()); // 设置下载内容大小
		if (fileName != null && filePath != null) {
			try {
				if (f.exists() && f.canRead()) {
					String mimetype = null;
					byte[] buffer = new byte[4096]; // 缓冲区
					BufferedOutputStream output = null;
					BufferedInputStream input = null;
					try {
						output = new BufferedOutputStream(response
								.getOutputStream());
						input = new BufferedInputStream(new FileInputStream(f));

						int n = (-1);
						while ((n = input.read(buffer, 0, 4096)) > -1) {
							output.write(buffer, 0, n);
						}
						response.flushBuffer();
					} catch (Exception e) {
					} // 用户可能取消了下载
					finally {
						if (input != null)
							input.close();
						if (output != null)
							output.close();
					}

				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}
}
