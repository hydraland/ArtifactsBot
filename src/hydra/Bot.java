package hydra;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openapitools.client.ApiClient;

import hydra.dao.BankDAO;
import hydra.dao.BankDAOImpl;
import hydra.dao.CharacterDAO;
import hydra.dao.CharacterDAOImpl;
import hydra.dao.EventsDAOImpl;
import hydra.dao.GrandExchangeDAO;
import hydra.dao.GrandExchangeDAOImpl;
import hydra.dao.ItemDAO;
import hydra.dao.ItemDAOImpl;
import hydra.dao.MapDAO;
import hydra.dao.MapDAOImpl;
import hydra.dao.MonsterDAO;
import hydra.dao.MonsterDAOImpl;
import hydra.dao.ResourceDAO;
import hydra.dao.ResourceDAOImpl;
import hydra.dao.TaskDAO;
import hydra.dao.TaskDAOImpl;
import hydra.dao.util.CharacterCache;
import hydra.dao.util.CharacterCacheImpl;
import hydra.dao.util.CooldownManagerImpl;
import strategy.achiever.GoalInterruptor;
import strategy.achiever.factory.util.ItemService;
import strategy.achiever.factory.util.ItemServiceImpl;
import strategy.util.BankRecorder;
import strategy.util.BankRecorderImpl;
import strategy.util.CharacterService;
import strategy.util.CharacterServiceImpl;
import strategy.util.MoveService;
import strategy.util.MoveServiceImpl;
import util.JsonToStringStyle;
import util.Utility;

public abstract class Bot {

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	protected final CharacterDAO characterDao;
	protected final ItemDAO itemDao;
	protected final MapDAO mapDao;
	protected final MonsterDAO monsterDao;
	protected final ResourceDAO resourceDAO;
	protected final GrandExchangeDAO grandExchangeDAO;
	protected final BankDAO bankDao;
	protected final TaskDAO taskDao;
	protected final BankRecorder bankRecorder;
	protected final ItemService itemService;
	protected final CharacterCache characterCache;
	private final ApiClient apiClient;
	protected final CharacterService characterService;
	protected final EventsDAOImpl eventsDao;
	protected final MoveService moveService;
	protected final GoalInterruptor interruptor;

	protected Bot(String persoName, String token) {
		try {
			String configuration = """
					handlers= java.util.logging.FileHandler
					.level= INFO
					java.util.logging.FileHandler.limit = 10000000
					java.util.logging.FileHandler.count = 5
					java.util.logging.FileHandler.append = true
					java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter
					java.util.logging.FileHandler.pattern  = """.concat(persoName).concat("%g.log");
			LogManager.getLogManager()
					.readConfiguration(new ByteArrayInputStream(configuration.getBytes(StandardCharsets.UTF_8)));
		} catch (SecurityException | IOException e) {
			LOGGER.severe("Impossible d'associer le FileHandler");
		}
		LOGGER.info("Initialisation de l'application");
		ToStringBuilder.setDefaultStyle(new JsonToStringStyle());
		apiClient = new ApiClient();
		apiClient.setConnectTimeout(30000);
		apiClient.setReadTimeout(120000);
		// apiClient.setDebugging(true);
		apiClient.setBearerToken(token);
		apiClient.setBasePath("https://api.artifactsmmo.com");
		CooldownManagerImpl cooldownManager = new CooldownManagerImpl();
		bankRecorder = new BankRecorderImpl(new File(persoName + ".xml"));
		characterCache = new CharacterCacheImpl();
		interruptor = new GoalInterruptor();
		characterDao = new CharacterDAOImpl(apiClient, persoName, cooldownManager, characterCache, interruptor);
		itemDao = new ItemDAOImpl(apiClient);
		itemService = new ItemServiceImpl(itemDao);
		mapDao = new MapDAOImpl(apiClient);
		monsterDao = new MonsterDAOImpl(apiClient);
		resourceDAO = new ResourceDAOImpl(apiClient);
		grandExchangeDAO = new GrandExchangeDAOImpl(apiClient, cooldownManager, persoName, characterCache);
		bankDao = new BankDAOImpl(apiClient, persoName, cooldownManager, bankRecorder, characterCache);
		taskDao = new TaskDAOImpl(apiClient, persoName, cooldownManager, characterCache);
		eventsDao = new EventsDAOImpl(apiClient);
		characterService = new CharacterServiceImpl(characterDao, itemDao);
		moveService = new MoveServiceImpl(characterDao, mapDao, characterService, itemService);
		
		//Chargement des caches
		resourceDAO.getAllResources();
		monsterDao.getMonsters();
		itemDao.getItems();
	}

	protected abstract void run();

	public final void launch() {
		int occ = 0;
		while (true) {
			try {
				run();
			} catch (NullPointerException npee) {
				LOGGER.log(Level.SEVERE, "Error", npee);
				occ++;
				Utility.sleep(60 * occ);
				if (occ > 20) {
					LOGGER.severe("Bot stop");
					System.exit(-1);
				}
			}
		}
	}
}