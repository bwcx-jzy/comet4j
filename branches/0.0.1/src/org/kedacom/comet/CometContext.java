package org.kedacom.comet;

import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.kedacom.comet.event.CometContextEvent;
import org.kedacom.comet.listener.CometContextListener;
import org.kedacom.event.Observable;

public class CometContext extends
		Observable<CometContextEvent, CometContextListener> {
	public  final String version = "0.0.2";
	
	
	/**
	 * 语言参数是一个有效的 ISO 语言代码。这些代码是由 ISO-639 定义的小写两字母代码。在许多网站上都可以找到这些代码的完整列表，如：
	 * http://www.loc.gov/standards/iso639-2/englangn.html。
	 * 目前可设置zh和en两种
	 */
	private Locale locale = Locale.CHINESE;
	
	private  String workStyle = CometProtocol.WORKSTYLE_AUTO;

	private  CometEngine engine;

	private  int timeout = 60000;

	private  int cacheExpires = 60 * 60 * 1000;

	private  int connExpires = 5000;

	private  boolean debug = false;

	
	private  boolean init = false;
	
	private  ServletContext servletContext;
	
	private static CometContext instance;

	public void init(ServletContextEvent event) {
		if (init) {
			return;
		}
		servletContext = event.getServletContext();
		loadConfig(servletContext);
		init = true;
		CometContextEvent e = new CometContextEvent(this,
				CometContextEvent.INITIALIZED);
		e.setCometContext(this);
		e.setServletContext(event.getServletContext());
		this.fireEvent(e);
		log("Comet v" + version + " 初始化完毕！");
	}

	public static CometContext getInstance() {
		if (instance == null) {
			instance = new CometContext();
		}
		return instance;
	}

	public void log(String str) {
		//if (debug) {
		instance.servletContext.log(str);
		//}
	}

	public void log(String str, Throwable trb) {
		//if (debug) {
		instance.servletContext.log(str, trb);
		//}
	}

	public Object createInstance(String className)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Object obj;
		obj = Class.forName(className).newInstance();
		return obj;
	}

	// Singleton
	private CometContext() {
		super();
		this.addEvent(CometContextEvent.class);
	}

	private void loadConfig(ServletContext ct) {

		String workStyleStr = ct.getInitParameter(CometProtocol.CONFIG_WORKSTYLE);
		if (workStyleStr != null && !"".equals(workStyleStr.trim())) {
			if (CometProtocol.WORKSTYLE_AUTO.equals(workStyleStr)
					|| CometProtocol.WORKSTYLE_LLOOP.equals(workStyleStr)
					|| CometProtocol.WORKSTYLE_STREAM.equals(workStyleStr)) {
				workStyle = workStyleStr;
			}
		}
		String timeoutStr = ct.getInitParameter(CometProtocol.CONFIG_TIMEOUT);
		if (timeoutStr != null && !"".equals(timeoutStr.trim())) {
			try {
				timeout = Integer.valueOf(timeoutStr);
			} catch (Exception e) {
				log("配置错误", e);
				// 忽略非法数值
			}
		}
		String cacheExpiresStr = ct.getInitParameter(CometProtocol.CONFIG_CACHEEXPIRES);
		if (cacheExpiresStr != null && !"".equals(cacheExpiresStr.trim())) {
			try {
				cacheExpires = Integer.valueOf(cacheExpiresStr);
			} catch (Exception e) {
				log("配置错误", e);
				// 忽略非法数值
			}
		}
		String connExpiresStr = ct.getInitParameter(CometProtocol.CONFIG_CONNEXPIRES);
		if (connExpiresStr != null && !"".equals(connExpiresStr.trim())) {
			try {
				connExpires = Integer.valueOf(connExpiresStr);
			} catch (Exception e) {
				log("配置错误", e);
				// 忽略非法数值
			}
		}
		String debugStr = ct.getInitParameter(CometProtocol.CONFIG_DEBUG);
		if (debugStr != null && "true".equals(debugStr.trim())) {
			debug = true;
		}
		
		String languageStr = ct.getInitParameter(CometProtocol.CONFIG_LANGUAGE);
		if(languageStr != null && !"".equals(languageStr.trim())){
			if(Locale.ENGLISH.getLanguage().equals(languageStr)){
				locale = Locale.ENGLISH;
			}
		}
		
		String engineStr = ct.getInitParameter(CometProtocol.CONFIG_ENGINE);
		if (engineStr != null && !"".equals(engineStr.trim())) {
			try {
				engine = (CometEngine) createInstance(engineStr);
			} catch (Exception e) {
				log("配置错误", e);
				engine = new CometEngine();
			}

		} else {
			engine = new CometEngine();
		}

	}

	public  String getVersion() {
		return version;
	}

	/**
	 * 工作方式
	 * loop,stream,auto(在前两者间自动选择)
	 * @return
	 */
	public  String getWorkStyle() {
		return workStyle;
	}

	public CometEngine getEngine() {
		return engine;
	}

	/**
	 * 连接超时时间(连接一次请求所能在服务器端保持的最大时间，默认1分钟)
	 * @return
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * 缓存信息过期时间(默认1小时)
	 * @return
	 */
	public int getCacheExpires() {
		return cacheExpires;
	}

	/**
	 * 连接过期时间(连接由于异常状况处于濒死状态到宣布死亡的时间段,默认5秒)
	 * @return
	 */
	public int getConnExpires() {
		return connExpires;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isInit() {
		return init;
	}

	public Locale getLocale() {
		return locale;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}
	
	public void destroy() {
		engine.destroy();
		engine = null;
		servletContext = null;
		instance = null;
		CometContextEvent e = new CometContextEvent(this,
				CometContextEvent.DESTROYED);
		this.fireEvent(e);
	}

}
