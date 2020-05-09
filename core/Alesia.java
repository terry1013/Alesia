/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package core;

import java.applet.*;
import java.awt.*;
import java.awt.Dialog.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.Action;

import org.apache.commons.logging.impl.*;
import org.apache.shiro.*;
import org.apache.shiro.authc.*;
import org.apache.shiro.config.*;
import org.apache.shiro.mgt.*;
import org.apache.shiro.session.*;
import org.apache.shiro.session.mgt.*;
import org.apache.shiro.subject.*;
import org.apache.shiro.util.*;
import org.javalite.activejdbc.*;
import org.javalite.activejdbc.connection_config.*;
import org.jdesktop.application.*;

import com.alee.extended.image.*;
import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.extended.window.*;
import com.alee.laf.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.window.*;
import com.alee.managers.notification.*;
import com.alee.managers.plugin.data.*;
import com.alee.managers.settings.*;
import com.alee.managers.style.*;
import com.alee.skin.dark.*;
import com.alee.utils.CollectionUtils;
import com.alee.utils.FileUtils;
import com.sun.jna.*;
import com.sun.jna.platform.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinDef.*;

import core.tasks.*;
import gui.*;
import gui.docking.*;
import gui.wlaf.*;

/**
 * Entrada aplicacion
 * 
 * @author Terry
 */
public class Alesia extends Application {

	public static TWebFrame mainFrame;
	public static Logger logger;

	public static ArrayList<Skin> skins;
	private static AudioClip newMsg, errMsg;

	public static TTaskManager manager;
	public static Font title1;
	public static Font title2;

	public static final String IS_RUNNING = "Running";
	public static final String REQUEST_MAXIMIZE = "RequestMaximize";

	private TPluginManager pluginManager;
	private static DockingContainer mainPanel;

	public static ActionMap getActionMap() {
		return getInstance().getContext().getActionMap();
	}

	public static DockingContainer getMainPanel() {
		return mainPanel;
	}

	public static ResourceMap getResourceMap() {
		return getInstance().getContext().getResourceMap();
	}

//	public interface TUser32 extends Library {
//		TUser32 INSTANCE = (TUser32) Native.loadLibrary("user32", TUser32.class);
//		HWND FindWindow(String lpClassName, String lpWindowName);
//		int GetWindowRect(HWND handle, int[] rect);
//		boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer data);
//		boolean GetWindowInfo(WinDef.HWND hWnd, WinUser.WINDOWINFO pwi);
//		WinDef.HWND	GetActiveWindow();
//	}

	/**
	 * inicio de aplicacion
	 * 
	 * @param arg - argumentos de entrada
	 */
	public static void main(String[] args) {
		Locale.setDefault(Locale.US);

// List<DesktopWindow> winds = WindowUtils.getAllWindows(true);
//		WinDef.HWND hwnd = TUser32.INSTANCE.GetActiveWindow();
//		WinUser.WINDOWINFO info = new WinUser.WINDOWINFO();
//		TUser32.INSTANCE.GetWindowInfo(hwnd, info);

//		for (DesktopWindow win : winds) {
//			System.out.println(win.getTitle());
//		}
		// HWND hwnd = TUser32.INSTANCE.FindWindow("Eclipse", null);
		// int[] rect = {0, 0, 0, 0};
		// int result = TUser32.INSTANCE.GetWindowRect(hwnd, rect);
//		System.out.println("Ok.");


		// Loggin configuration. this step is performed here for conbenence
		// TODO: For slf4j: the bridge betwen slf4j is set using the slf4j-jdk14-1.7.25 jar lib
		// for Apache: Set the system property
		// For Alesia. look on the configuration file. if the file exist, i use it
		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		System.setProperty("org.apache.commons.logging.Log", Jdk14Logger.class.getName());
		File logc = new File("logging.properties");
		logger = Logger.getLogger("Alesia");
		if (logc.exists()) {
			System.setProperty("java.util.logging.config.file", logc.getName());
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (Exception e) {
				ExceptionDialog.showDialog(e);
			}
		}
		logger.info("Wellcome to Alesia.");

		// update alesia.propertyes to eviorement variables. this step is performed here for convenience
		try {
			Properties prp = new Properties();
			prp.load(new FileInputStream(new File("Alesia.properties")));
			Properties sysprp = System.getProperties();
			prp.keySet().forEach(key -> sysprp.put(key, prp.get(key)));
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception found loading propertys from file system.", e);
			// ExceptionDialog.showDialog(e);
			System.exit(-1);
		}

		Application.launch(Alesia.class, args);
	}

	/**
	 * Open and return an instacen of {@link DB} for the given prefix. The connection parameters must be in the
	 * database.properties file or similar.
	 * 
	 * @param name - prefix name of the conneciton parameters
	 * @return instance of {@link DB}
	 * @see #getDBProperties()
	 */
	public static DB openDB(String name) {
		DB db = null;
		try {
			Properties orgPrp = getDBProperties();

			// remove all properties except those who star whit "name"
			Set<Object> keys = orgPrp.keySet();
			keys.removeIf(k -> !k.toString().startsWith(name));
			Properties properties = new Properties();

			// build a new propert list whiout the prefix. keeping only the property and value spected by the jdbc lib
			keys.forEach(k -> properties.put(k.toString().substring(name.length() + 1), orgPrp.get(k)));

			// mandatory parameters
			String drv = properties.getProperty("driver");
			properties.remove("driver");
			String url = properties.getProperty("url");
			properties.remove("url");

			db = new DB(name);
			db.open(drv, url, properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return db;
	}

	/**
	 * Convenient method to open the alesia local database connection using the system enviorement variables. call this
	 * method to create or open a new database connection and attach these to {@link Thread} that invoke this method.
	 * <p>
	 * this method is intentet for javaLite imeplementation. this method relly in the javaLite internal storage that
	 * determine if the connection name is aready opened. if is opened, just attach to the invoker thread
	 */
	public static void openDB() {
		if (alesiaDB == null)
			alesiaDB = new DB("AlesiaDatabase");
		// alesiaDB = openDB("activejdbc");
		ConnectionJdbcSpec spec = new ConnectionJdbcSpec(System.getProperty("activejdbc.driver"),
				System.getProperty("activejdbc.url"), System.getProperty("activejdbc.username"),
				System.getProperty("activejdbc.password"));
		alesiaDB.open(spec);
	}
	public static Hashtable<String, Object> showDialog(TUIFormPanel content, double withFactor, double heightFactor) {

		// standar behavior: if the title of the tuipanel is visible, this method remove the string and put in as this
		// dialog title
		String popOvertext = " ";
		if (content.isTitleVisible()) {
			popOvertext = content.getTitleText();
			content.setTitleVisible(false);
		}
		final WebPopOver popOver = new WebPopOver(Alesia.mainFrame);
		popOver.setModalityType(ModalityType.TOOLKIT_MODAL);
		popOver.setMovable(false);
		popOver.setLayout(new VerticalFlowLayout());
		final WebImage icon = new WebImage(TResources.getSmallIcon("alesia"));
		final WebLabel titleLabel = new WebLabel(popOvertext, WebLabel.CENTER);
		final WebButton closeButton = new WebButton(TResources.getSmallIcon("close"), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popOver.dispose();
			}
		});
		popOver.setUndecorated(true);
		GroupPanel tit = new GroupPanel(GroupingType.fillMiddle, 4, icon, titleLabel, closeButton);
		tit.setMargin(0, 0, 10, 0);
		popOver.add(tit);
		popOver.add(content);

		// popOver.setLocationRelativeTo(Alesia.mainFrame);
		popOver.pack();
		popOver.setLocationRelativeTo(Alesia.mainFrame);
		popOver.setVisible(true);
		// popOver.show(Alesia.mainFrame);

		return content.getValues();
	}
	public static void showNotification(String mid, int lt, Object... dta) {
		TError ae = new TError(mid, dta);
		WebNotification npop = NotificationManager.showNotification(Alesia.mainFrame, ae.getMessage(),
				ae.getExceptionIcon());
		// ae.getExceptionIcon(), NotificationOption.accept);
		npop.setDisplayTime(lt);
		if (mid.equals("notification.msg00")) {
			errMsg.play();
			// UIManager.getLookAndFeel().provideErrorFeedback(null);
		} else {
			newMsg.play();
		}
	}

	/**
	 * show notification that does't disapear ultil user click on it
	 * 
	 * @param mid - messate id for {@link AplicationException}
	 * @param dta - sustitution data for AplicationException
	 */
	public static void showNotification(String mid, Object... dta) {
		showNotification(mid, 0, dta);
	}
	/**
	 * show notification that disapear according to {@link AplicationException} limited time
	 * 
	 * @param mid
	 * @param dta
	 */
	public static void showNotificationLT(String mid, Object... dta) {
		showNotification(mid, new TError(mid).getMiliSeconds(), dta);
	}

	/**
	 * this method is only for reference of the old school clases.
	 */
	private static void configureWorkMenuBar() {

		// JMenu menu = new JMenu(TStringUtils.getBundleString("about.app.name"));

		// MenuActionFactory maf = null;
		// maf = new MenuActionFactory(SystemVars.class);
		// menu.add(maf);
		// maf = new MenuActionFactory(TConnectionDialog.class);
		// maf.setDimension(MenuActionFactory.PACK_DIMMENTION);
		// menu.add(maf);
		// maf = new MenuActionFactory(TDriverDialog.class);
		// maf.setDimension(MenuActionFactory.PACK_DIMMENTION);
		// menu.add(maf);
		// menu.add(new LoadView());
		// menu.add(new SaveView());
		// menu.add(new UpdateManifestView());
		// menuBar.add(menu);

		// menu.add(new JSeparator(JSeparator.HORIZONTAL));
		// menu.add(new DockingAction(Wellcome.class));
		// menu.add(new DockingAction(HelpBrowser.class));
		// menu.add(new UpdateManifestView());
		// MenuActionFactory maf = new MenuActionFactory(AuditLog.class);

		// 171201 1.24: Mierdaaa ya es diciembre y yo pelandooooooooo otro añooo !?!?! user options moved to
		// plancselector

		// 191031 7:16: Mierdaaaa ya an pasado 2 anos y continuo pelandooooo otro aöo mas !!!!! JAJAJAJAJAJA Ahora
		// pelando pero en alemania :D
		//
		// menu.add(new ChangePasswordAction());
		// menu.add(new SignOut());
		// menu.add(new JSeparator(JSeparator.HORIZONTAL));
		// menu.add(new Exit());
		// final PathSelector pcs = new PathSelector();
	}

	/**
	 * Look in the Alesia.properties file, look for the property "Alesia.database.file.name" and load an return the list
	 * of all prperties found in that file. This file contain all data base connection information.
	 * 
	 * @return all properties found in the database properties files
	 */
	private static Properties getDBProperties() throws Exception {
		// active jdbc propertie files pointed form main alesia property file
		Properties activeprp = new Properties();
		// TODO: convert to urls to allow more access support ???
		File fp = new File(System.getProperty("Alesia.database.file.name"));
		activeprp.load(new FileInputStream(fp));
		return activeprp;
	}
	private static DB alesiaDB;
	/**
	 * try to connect to local database. this method determine if an instance of this app is already running. in this
	 * case, send {@link TPreferences#REQUEST_MAXIMIZE} message throwout internal comunication file (_.properties file)
	 * to signal active instance to display main frame and this execution ends
	 * 
	 */
	private static void connectToLocalDB() {
		// System.getProperties().put("connectTimeout", 10 * 1000);
		// System.getProperties().put("socketTimeout", 10 * 1000);
		try {
			logger.info("Connecting to local database ...");
			Properties activeprp = getDBProperties();

			// set the produccion connection data to the sytem variables for activejdbc use
			// (only local database)
			String ae = activeprp.getProperty("active_env");
			System.setProperty("active_env", ae);
			System.setProperty("activejdbc.url", activeprp.getProperty(ae + ".url"));
			System.setProperty("activejdbc.user", activeprp.getProperty(ae + ".username"));
			System.setProperty("activejdbc.password", activeprp.getProperty(ae + ".password"));
			System.setProperty("activejdbc.driver", activeprp.getProperty(ae + ".driver"));

			openDB();
			// ConnectionJdbcSpec spec = new ConnectionJdbcSpec(activeprp.getProperty("AlesiaDatabase.driver"),
			// activeprp.getProperty("AlesiaDatabase.url"), activeprp.getProperty("AlesiaDatabase.username"),
			// activeprp.getProperty("AlesiaDatabase.password"));
			// alesiaDB = new DB("AlesiaDatabase");
			// alesiaDB.open(spec);
			// TODO: warm the activelitte enviorement for futer use by the trooper. i dont know way i need to do this
			// but
			// works.
			// Base.open(activeprp.getProperty("AlesiaDatabase.driver"),
			// activeprp.getProperty("AlesiaDatabase.url"), activeprp.getProperty("AlesiaDatabase.username"),
			// activeprp.getProperty("AlesiaDatabase.password"));
			// String jndi = activeprp.getProperty(ae + ".jndi");
			// System.setProperty("activejdbc.jndi", jndi);
		} catch (Exception e) {
			if (e instanceof InitException) {
				SQLException se = (SQLException) e.getCause();
				if (se.getSQLState().equals("S1000")) {
					logger.warning("Another active instance found. Sending request maximize.");
					SettingsManager.set(REQUEST_MAXIMIZE, true);
					System.exit(0);
				}
			}
			e.printStackTrace();
			ExceptionDialog.showDialog(e);
			System.exit(-1);
		}
	}

	/**
	 * retrive global identificator from <code>wmic</code>
	 * 
	 * @param gid - gobal id
	 * @param vn - variable name
	 * 
	 * @return variable value
	 */
	private static String getWmicValue(String gid, String vn) {
		String rval = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(new String[]{"wmic", gid, "get", vn});
			InputStream is = process.getInputStream();
			Scanner sc = new Scanner(is);
			while (sc.hasNext()) {
				String next = sc.next();
				if (vn.equals(next)) {
					rval = sc.next().trim();
					break;
				}
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rval;
	}

	/**
	 * request the user autentication for entry the aplication. this method chec the system variable ... to determnide
	 * the kind of autentication required. this method has 2 ending.
	 * <ol>
	 * <li>Exist this method normaly means the autentication step is succefully or maybe no autentication was required.
	 * The framework will continue his normal execuion steps.
	 * <li>If there is problem with autentication procedure, this method will finnish the app
	 */
	private static void requestAutentication() {
		Alesia.mainFrame.setSplashIncrementText("Request autentication");

		Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
		DefaultSecurityManager securityManager = (DefaultSecurityManager) factory.getInstance();
		DefaultSessionManager sessionManager = (DefaultSessionManager) securityManager.getSessionManager();
		// TODO: securiy breach!?!?! read this method documentation. anoter java program can run on the same vm and
		// retrive security??
		SecurityUtils.setSecurityManager(securityManager);
		Subject currentUser = SecurityUtils.getSubject();
		SessionListener slit = new SessionListenerAdapter() {
			@Override
			public void onExpiration(Session session) {
				System.out.println("Alesia.requestAutentication().new SessionListener() {...}.onExpiration()");
			}
		};
		sessionManager.setSessionListeners(Arrays.asList(slit));

		if (!currentUser.isAuthenticated()) {
			UserLogIn li = new UserLogIn();
			WebDialog dlg = li.createDialog(true);
			dlg.setVisible(true);
			Hashtable<String, Object> vals = li.getValues();
			if (vals == null) {
				getInstance().exit();
			}
			UsernamePasswordToken token = new UsernamePasswordToken((String) vals.get("user"),
					(String) vals.get("password"));
			token.setRememberMe(true);
			try {
				currentUser.login(token);
			} catch (UnknownAccountException uae) {
				logger.info("There is no user with username of " + token.getPrincipal());
			} catch (IncorrectCredentialsException ice) {
				logger.info("Password for account " + token.getPrincipal() + " was incorrect!");
			} catch (LockedAccountException lae) {
				logger.info("The account for username " + token.getPrincipal() + " is locked.  "
						+ "Please contact your administrator to unlock it.");
			}
			// ... catch more exceptions here (maybe custom ones specific to your application?
			catch (AuthenticationException ae) {
				// unexpected condition? error?
			}
		}

		// say who they are:
		// print their identifying principal (in this case, a username):
		logger.info("User [" + currentUser.getPrincipal() + "] logged in successfully.");

		// test a role:
		if (currentUser.hasRole("schwartz")) {
			logger.info("May the Schwartz be with you!");
		} else {
			logger.info("Hello, mere mortal.");
		}

		// test a typed permission (not instance-level)
		if (currentUser.isPermitted("lightsaber:wield")) {
			logger.info("You may use a lightsaber ring.  Use it wisely.");
		} else {
			logger.info("Sorry, lightsaber rings are for schwartz masters only.");
		}

		// a (very powerful) Instance Level permission:
		if (currentUser.isPermitted("winnebago:drive:eagle5")) {
			logger.info("You are permitted to 'drive' the winnebago with license plate (id) 'eagle5'.  "
					+ "Here are the keys - have fun!");
		} else {
			logger.info("Sorry, you aren't allowed to drive the 'eagle5' winnebago!");
		}

		// all done - log out!
		currentUser.logout();

		System.exit(0);
	}

	public void restarApplication() {
		try {
			shutdown();
			// espera para hasta q finalizen todos los subsys
			Thread.sleep(2000);
			Runtime.getRuntime().exec("cmd /c start /MIN restart.bat");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void initialize(String[] args) {

		// System.out.println(getWmicValue("bios", "SerialNumber"));
		// System.out.println(getWmicValue("cpu", "SystemName"));

		// Properties prp = System.getProperties();

		TResources.init();
		TStringUtils.addProperties(TResources.USER_DIR + "/core/resources/");

		// local storage configuration
		// File lf = new File("LocalStorage.txt").getParentFile();
		// getContext().getLocalStorage().setDirectory(lf.getParentFile());
		//
		// getContext().getLocalStorage().se

		Font fo;
		try {
			fo = Font.createFont(Font.TRUETYPE_FONT, TResources.getFile("Dosis-Regular.ttf"));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fo);
			fo = Font.createFont(Font.TRUETYPE_FONT, TResources.getFile("TitilliumWeb-Regular.ttf"));
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fo);
			title1 = fo.deriveFont(20f).deriveFont(Font.PLAIN);
			title2 = fo.deriveFont(14f).deriveFont(Font.PLAIN);
		} catch (Exception e) {
			e.printStackTrace();
			title1 = new Font("Arial", Font.PLAIN, 16);
			title2 = new Font("Arial", Font.PLAIN, 14);
		}

		// load all actions into the application
		new TActionsFactory();

		connectToLocalDB();

		newMsg = Applet.newAudioClip(TResources.getURL("newMsg.wav"));
		errMsg = Applet.newAudioClip(TResources.getURL("errMsg.wav"));

		// System.setProperty("org.apache.commons.logging.Log", Jdk14Logger.class.getName());

		// TPreferences.init();
		// TStringUtils.init();

		// parse app argument parameters and append to tpreferences to futher uses
		for (String arg : args) {
			String[] kv = arg.split("=");
			TPreferences.setProperty(kv[0], kv[1]);
		}
	}
	@Override
	protected void ready() {
		Alesia.mainFrame.setSplashIncrementText("Loading plugins ...");
		pluginManager = new TPluginManager();
		pluginManager.scanPluginsDirectory();

		Alesia.mainFrame.setSplashIncrementText("Starting task manager ...");
		Alesia.manager = new TTaskManager();
		// requestAutentication();

		// UserLogIn logIn = new UserLogIn();
		// TTilePanel tilePanel = new TTilePanel();
		// ChangePassword changePassword = new ChangePassword(null);

		// load left panel actions
		mainPanel = new DockingContainer();
		TLeftPanel leftPanel = mainPanel.getLeftPanel();
		ArrayList<DetectedPlugin<TPlugin>> dplist = new ArrayList<>(pluginManager.getDetectedPlugins());
		ArrayList<Action> alist = new ArrayList<>();
		for (DetectedPlugin<TPlugin> dp : dplist) {
			alist.addAll(dp.getPlugin().getUI(TPluginManager.leftPanelUI));
		}
		leftPanel.appendActions((Action[]) alist.toArray(new Action[alist.size()]));

		Alesia.mainFrame.setContentPane(mainPanel);
	}

	@Override
	protected void shutdown() {
		logger.info("Preapering to leave the application ...");
		SettingsManager.set(IS_RUNNING, false);
		alesiaDB.close();
		logger.info("Bye !!!");
	}

	@Override
	protected void startup() {
		// GenericStyle
		// Configuring settings location
		SettingsManager.setDefaultSettingsDir(FileUtils.getWorkingDirectoryPath());
		SettingsManager.setDefaultSettingsGroup("Alesia");
		SettingsManager.setSaveOnChange(true);

		// Adding demo data aliases before styles using it are read
		// XmlUtils.processAnnotations ( FeatureStateBackground.class );

		// Installing Look and Feel
		WebLookAndFeel.setForceSingleEventsThread(true);
		// alesia skin
		// WebLookAndFeel.install(TSkin.class);
		WebLookAndFeel.install();

		// WebLookAndFeel.install();
		// WebLookAndFeel.setDecorateFrames(true);
		// WebLookAndFeel.setDecorateDialogs(true);

		// try {
		// UIManager.setLookAndFeel(new PlasticMetroLookAndFeel());
		// } catch (UnsupportedLookAndFeelException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Saving skins for reference
		skins = CollectionUtils.asList(StyleManager.getSkin(), new DarkSkin());

		// Adding demo application skin extensions

		// XmlSkinExtension dark = new XmlSkinExtension(TResourceUtils.getFile("dark.extension.xml"));
		// XmlSkinExtension light = new XmlSkinExtension(TResourceUtils.getFile("light.extension.xml"));
		StyleManager.addExtensions(new TXmlSkinExtension());

		// TODO: no languaje manajer for now. still using old school i18n
		// Configurting languages
		// LanguageManager.addDictionary ( new Dictionary ( Alesia.class, "language/demo-language.xml" ) );
		// LanguageManager.addLanguageListener ( new LanguageLocaleUpdater () );

		mainFrame = new TWebFrame();
		mainFrame.setVisible(true);
	}
}
