package com.fiskkit.tos.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fiskkit.exception.FiskkitException;
import com.fiskkit.tos.darkbot.TestOfSignificance;
import com.fiskkit.tos.darkbot.network.MYSQLAccess;





public class ToSWorker extends HttpServlet {
	public static final String VERSION = "beanstalk-tos-2.0.1";
	
	private static final Logger LOGGER = Logger.getLogger("");
	
	
	
	static {
		try {
			System.out.println("====>ToSWorker static block");
			setLogLevel();
		} catch (Exception e){
			LOGGER.log(Level.SEVERE, "unable to set logging level", e);
		}
		
		
	}
//	static {
//		LOGGER.setUseParentHandlers(false);
//	}
	private static final long serialVersionUID = 1L;
	

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		
		LOGGER.info("====>ToSWorker.doPost(1):" + new Date());
		LOGGER.info("====>ToSWorker.doPost(2):" + new Date());
		LOGGER.info("====>ToSWorker.request.getRequestURL()=" + request.getRequestURL());
		
		
		String serviceDisabled = System.getProperty("FISKKIT_DISABLE_SERVICE", "");
		if (serviceDisabled.trim().equalsIgnoreCase("true")) {
			LOGGER.info("EmailDigestTask.run():FISKKIT_DISABLE_SERVICE=true ... no action taken");
			
			LOGGER.info("processRequest() complete");
			PrintWriter out = response.getWriter();
			response.setStatus(200);
			out.flush();
			LOGGER.info("returned 200:success");
			
			return;
		}

		try {
			printEnvironment(request);
			processRequest(request);
			LOGGER.info("processRequest() complete");
			PrintWriter out = response.getWriter();
			response.setStatus(200);
			out.flush();
			LOGGER.info("returned 200:success");
			
			

		} catch (Exception any) {
			LOGGER.log(Level.SEVERE, "unable to process request", any);
			PrintWriter out = response.getWriter();
			response.setStatus(500);
			out.flush();
			LOGGER.severe("returned 500:failure");
		}

	}

	private static void printEnvironment(final HttpServletRequest request) {
		
		Map<String, String> env = System.getenv();
		for (String var : env.keySet()) {
			System.out.println("env:" + var + "=" + env.get(var));
			LOGGER.log(Level.INFO, "env:{0}={1}", new Object[] { var, env.get(var) });
		}
		
		

		// probably do not need this
//		for (Enumeration<?> e = request.getAttributeNames(); e.hasMoreElements();) {
//			Object attribute = e.nextElement();
//			LOGGER.log(Level.INFO, "attribute:{0}={1}",
//					new Object[] { attribute, request.getAttribute(attribute.toString()) });
//		}

		for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();) {
			Object header = e.nextElement();
			LOGGER.log(Level.INFO, "header:{0}={1}",
					new Object[] { header, request.getHeader(header.toString()) });

		}

		for (Object key : System.getProperties().keySet()) {
			LOGGER.log(Level.INFO, "system:{0}={1}",
					new Object[] { key, System.getProperty(key.toString(), "<unset>") });
		}
		
		LOGGER.info("request.getRequestURL()=" + request.getRequestURL());

		
		Runtime runtime = Runtime.getRuntime();

		LOGGER.info("     version:" + VERSION);
		LOGGER.info(" free memory:" + runtime.freeMemory());
		LOGGER.info("total memory:" + runtime.totalMemory());
	}

	private static void processRequest(final HttpServletRequest request) throws IOException, FiskkitException {
		
		

		byte[] bytes = new byte[request.getContentLength() + 1];
		StringBuilder content = new StringBuilder(request.getContentLength() + 1);
		while (true) {
			int cc = request.getInputStream().read(bytes);
			if (cc <= 0) {
				break;
			}
			content.append(new String(bytes, 0, cc));
		}

		String message = content.toString();
		LOGGER.info("====SQS message=====");
		LOGGER.info(message);
		LOGGER.info("====SQS message=====");
		
		
		
		
		
		try {
			
			//FIXME move to MYSQLAccess constructor?
			MYSQLAccess.initializeConnection();
			
			
			LOGGER.info("====>Connection obtained -- Ready to perform test-of-significance run");
			TestOfSignificance.runTestOfSignificance();
			
			
		} catch (Exception e) {
			throw new FiskkitException("unable to process request", e);
		} finally {
			MYSQLAccess.closeConnection();
			
		}
		
		LOGGER.info("test no op ... returning");

	}
	
	
	
	
	
	
	
	
	
	

	
	private static void setLogLevel(){
		HashMap<String,Level> levelMap = new HashMap<String,Level>();
		levelMap.put("INFO", Level.INFO);
		levelMap.put("FINE", Level.FINE);
		levelMap.put("FINER", Level.FINER);
		levelMap.put("FINEST", Level.FINEST);
		
		String setting = System.getProperty("LOG_LEVEL", "INFO");
		Level level = levelMap.get(setting);
		if (level == null){
			level = Level.INFO;
		}
		LOGGER.setLevel(level);
		for (Handler handler: LOGGER.getHandlers()) {
			handler.setLevel(level);
		}
		
		LOGGER.log(level, "setLogLevel():setting=" + setting + ":" + level);
		LOGGER.fine("This is a FINE level message");
	}
	
	
	
	
	
}
