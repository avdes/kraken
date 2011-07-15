package org.krakenapps.webconsole.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.krakenapps.dom.api.FileUploadApi;
import org.krakenapps.dom.model.UploadedFile;
import org.krakenapps.webconsole.ServletRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "webconsole-file-upload-servlet")
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(FileUploadServlet.class.getName());

	@Requires
	private ServletRegistry servletRegistry;
	
	@Requires
	private FileUploadApi upload;

	/**
	 * Register servlet to servlet registry of webconsole
	 */
	@Validate
	public void start() {
		servletRegistry.register("/upload", this);
	}

	/**
	 * Unregister servlet from servlet registry
	 */
	@Invalidate
	public void stop() {
		if (servletRegistry != null)
			servletRegistry.unregister("/upload");
	}

	@Override
	public void log(String message, Throwable t) {
		logger.warn("kraken webconsole: upload servlet error", t);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		UploadedFile f = null;
		FileInputStream is = null;
		ServletOutputStream os = null;
		String token = null;
		int resourceId = 0;

		try {
			token = getDownloadToken(req);
			resourceId = Integer.parseInt(req.getParameter("resource"));

			if (token == null)
				throw new IllegalStateException("download token not found");
			
			f = upload.getFile(token, resourceId);
			is = new FileInputStream(f.getFile());
			os = resp.getOutputStream();
			logger.trace("kraken webconsole: open downstream for {}", f.getFile().getAbsolutePath());

			String mimeType = MimeTypes.instance().getByFile(f.getFileName());
			resp.setHeader("Content-Type", mimeType);

			String dispositionType = null;
			if (req.getParameter("force_download") != null)
				dispositionType = "attachment";
			else
				dispositionType = "inline";

			String encodedFilename = URLEncoder.encode(f.getFileName(), "UTF-8").replaceAll("\\+", "%20");
			resp.setHeader("Content-Disposition", dispositionType + "; filename*=UTF-8''" + encodedFilename);
			resp.setStatus(200);
			resp.setContentLength((int) f.getFileSize());

			byte[] b = new byte[8096];

			while (true) {
				int readBytes = is.read(b);
				if (readBytes <= 0)
					break;

				os.write(b, 0, readBytes);
			}
		} catch (Exception e) {
			resp.setStatus(500);
			logger.warn("kraken webconsole: cannot download id " + resourceId);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
				}
		}
	}

	private String getDownloadToken(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				logger.trace("kraken webconsole: checking all cookie for download, {} = {}", cookie.getName(),
						cookie.getValue());
				if (cookie.getName().equals("kraken_session"))
					return cookie.getValue();
			}
		}

		return req.getParameter("session");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		logger.debug("kraken webconsole: received post request from [{}]", req.getRemoteAddr());
		
		String token = req.getHeader("X-Upload-Token");
		if (token == null) {
			logger.warn("kraken webconsole: upload token header not found for [{}] stream", req.getRemoteAddr());
			return;
		}

		InputStream is = null;
		try {
			is = req.getInputStream();
			upload.writeFile(token, is);
			resp.setStatus(200);
		} catch (Exception e) {
			resp.setStatus(500);
			logger.warn("kraken webconsole: upload post failed", e);
		} finally {
			if (is == null)
				return;

			try {
				is.close();
			} catch (IOException e) {
			}

			try {
				resp.getOutputStream().close();
			} catch (IOException e) {
			}
		}
	}
}