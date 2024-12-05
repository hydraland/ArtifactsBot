package strategy.achiever.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import hydra.GameConstants;
import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.GrandExchangeDAO;
import hydra.dao.ItemDAO;
import hydra.dao.MapDAO;
import hydra.dao.MonsterDAO;
import hydra.dao.ResourceDAO;
import hydra.dao.TaskDAO;
import hydra.dao.response.FightResponse;
import hydra.dao.response.UseInCraftResponse;
import hydra.model.BotBox;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotDropDescription;
import hydra.model.BotEffect;
import hydra.model.BotItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemType;
import hydra.model.BotMonster;
import hydra.model.BotResource;
import hydra.model.BotResourceSkill;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.custom.DepositGoldInBankGoalAchiever;
import strategy.achiever.factory.custom.DepositResourceGoalAchiever;
import strategy.achiever.factory.custom.DepositTaskCoinGoalAchiever;
import strategy.achiever.factory.custom.DepositToolGoalAchiever;
import strategy.achiever.factory.custom.EquipmentManagerGoalAchiever;
import strategy.achiever.factory.custom.ExtendBankSlotGoalAchiever;
import strategy.achiever.factory.custom.FoodManagerGoalAchiever;
import strategy.achiever.factory.custom.PotionManagerGoalAchiever;
import strategy.achiever.factory.custom.UseGoldItemManagerGoalAchiever;
import strategy.achiever.factory.custom.UselessEquipmentManagerGoalAchiever;
import strategy.achiever.factory.custom.UselessResourceManagerGoalAchiever;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.DepositNoReservedItemGoalAchiever;
import strategy.achiever.factory.goals.EquipToolGoalAchiever;
import strategy.achiever.factory.goals.GatheringGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.GoalAchieverList;
import strategy.achiever.factory.goals.GoalAchieverLoop;
import strategy.achiever.factory.goals.GoalAchieverTwoStep;
import strategy.achiever.factory.goals.ItemCraftGoalAchiever;
import strategy.achiever.factory.goals.ItemGetBankGoalAchiever;
import strategy.achiever.factory.goals.ItemGetInventoryOrBankGoalAchiever;
import strategy.achiever.factory.goals.ItemMonsterGoalAchiever;
import strategy.achiever.factory.goals.ItemRecycleGoalAchiever;
import strategy.achiever.factory.goals.ItemTaskGoalAchiever;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.goals.MonsterTaskGoalAchiever;
import strategy.achiever.factory.goals.UnequipFirstWeaponGoalAchiever;
import strategy.achiever.factory.info.CraftGoalAchieverInfo;
import strategy.achiever.factory.info.GatheringGoalAchieverInfo;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.info.GoalAchieverInfo.INFO_TYPE;
import strategy.achiever.factory.info.MultiGoalAchieverInfo;
import strategy.achiever.factory.info.SimpleGoalAchieverInfo;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.GameService;
import strategy.achiever.factory.util.StopValidator;
import strategy.util.CharacterService;
import strategy.util.MonsterEquipementService;
import strategy.util.MoveService;
import strategy.util.fight.FightService;

public final class ArtifactGoalFactory implements GoalFactory {

	private final ResourceDAO resourceDao;
	private final MapDAO mapDAO;
	private final ArrayList<ArtifactGoalAchiever> goals;
	private final CharacterDAO characterDao;
	private final ItemDAO itemDao;
	private final MonsterDAO monsterDao;
	private final GrandExchangeDAO grandExchangeDao;
	private final GoalParameter parameter;
	private final BankDAO bankDao;
	private final MonsterEquipementService monsterEquipementService;
	private final List<String> rareResourceItems;
	private final List<String> resourceItemsCraftable;
	private final TaskDAO taskDao;
	private final GameService gameService;
	private final Map<ArtifactGoalAchiever, GoalAchieverInfo> goalInfos;
	private final CharacterService characterService;
	private final FightService fightService;
	private final MoveService moveService;

	public ArtifactGoalFactory(ResourceDAO resourceDAO, MonsterDAO monsterDao, MapDAO mapDAO, ItemDAO itemDao,
			CharacterDAO characterDAO, GrandExchangeDAO grandExchangeDAO, BankDAO bankDAO, TaskDAO taskDao,
			GoalParameter parameter, GameService gameService, CharacterService characterService,
			MoveService moveService, FightService fightService, MonsterEquipementService monsterEquipementService) {
		this.resourceDao = resourceDAO;
		this.monsterDao = monsterDao;
		this.mapDAO = mapDAO;
		this.characterDao = characterDAO;
		this.itemDao = itemDao;
		this.grandExchangeDao = grandExchangeDAO;
		this.bankDao = bankDAO;
		this.taskDao = taskDao;
		this.parameter = parameter;
		this.gameService = gameService;
		this.characterService = characterService;
		this.moveService = moveService;
		this.goals = new ArrayList<>();
		this.rareResourceItems = new ArrayList<>();
		this.resourceItemsCraftable = itemDao.getResourceItems().stream().<String>map(bid -> bid.getCode())
				.filter(code -> itemDao.useInCraft(code).isUseInCraft()).toList();
		// Tous les items des task sont rares
		this.rareResourceItems.addAll(itemDao.getTaskItems().stream().<String>map(bid -> bid.getCode()).toList());
		this.fightService = fightService;
		this.monsterEquipementService = monsterEquipementService;
		goalInfos = new HashMap<>();
	}

	private void createResourceGoal(Map<String, List<ArtifactGoalAchiever>> goalsMap) {
		List<BotResource> resources = this.resourceDao.getAllResources();
		List<BotBox> resourcesBox = this.mapDAO.getResourcesBox();
		Map<String, List<Coordinate>> resourceLocation = new HashMap<>();
		for (BotBox resourceBox : resourcesBox) {
			String code = resourceBox.getContent().getCode();
			if (!resourceLocation.containsKey(code)) {
				resourceLocation.put(code, new ArrayList<>());
			}
			resourceLocation.get(code).add(new Coordinate(resourceBox.getX(), resourceBox.getY()));
		}
		for (BotResource resource : resources) {
			for (BotDropDescription drop : resource.getDrops()) {
				String resourceCode = drop.getCode();
				String boxCode = resource.getCode();
				int rate = drop.getRate();
				GatheringGoalAchiever goalAchiever = new GatheringGoalAchiever(characterDao, characterService, mapDAO,
						resourceCode, rate, resourceLocation.get(boxCode), resource.getLevel(), resource.getSkill(),
						boxCode, moveService);
				DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(
						bankDao, moveService, characterService, parameter);
				GoalAchieverTwoStep achieverTwoStep = new GoalAchieverTwoStep(characterDao,
						depositNoReservedItemGoalAchiever, goalAchiever, true, true);
				this.goals.add(achieverTwoStep);
				getGoalInfo(achieverTwoStep, new GatheringGoalAchieverInfo(resourceCode, BotItemType.RESOURCE,
						resource.getSkill(), resource.getLevel()));
				List<ArtifactGoalAchiever> goal = goalsMap.computeIfAbsent(resourceCode, c -> new ArrayList<>());
				goal.add(achieverTwoStep);
				if (rate >= parameter.getRareItemSeuil()) {
					UseInCraftResponse response = itemDao.useInCraft(goalAchiever.getCode());
					if (!response.isError() && response.isUseInCraft()) {
						// Les items non craftable ne sont pas pris en compte
						rareResourceItems.add(resourceCode);
					}
				}
			}
		}
	}

	private void createResourceMonsterGoal(Map<String, List<ArtifactGoalAchiever>> goalsMap) {
		List<BotMonster> monsters = this.monsterDao.getMonsters();
		List<BotBox> monstersBox = this.mapDAO.getMonstersBox();
		Map<String, List<Coordinate>> monsterLocation = new HashMap<>();
		for (BotBox monsterBox : monstersBox) {
			String code = monsterBox.getContent().getCode();
			monsterLocation.computeIfAbsent(code, s -> new ArrayList<>());
			monsterLocation.get(code).add(new Coordinate(monsterBox.getX(), monsterBox.getY()));
		}
		for (BotMonster monster : monsters) {
			for (BotDropDescription drop : monster.getDrops()) {
				String resourceCode = drop.getCode();
				String boxCode = monster.getCode();
				int rate = drop.getRate();
				ItemMonsterGoalAchiever goalAchiever = new ItemMonsterGoalAchiever(characterDao, mapDAO, resourceCode,
						rate, monsterLocation.get(boxCode), monster, monsterEquipementService, fightService,
						moveService, characterService, parameter);
				DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(
						bankDao, moveService, characterService, parameter);
				GoalAchieverTwoStep achieverTwoStep = new GoalAchieverTwoStep(characterDao,
						depositNoReservedItemGoalAchiever, goalAchiever, true, true);
				this.goals.add(achieverTwoStep);
				getGoalInfo(achieverTwoStep, new SimpleGoalAchieverInfo(resourceCode, BotItemType.RESOURCE));
				List<ArtifactGoalAchiever> goal = goalsMap.computeIfAbsent(resourceCode, c -> new ArrayList<>());
				goal.add(achieverTwoStep);
				if (rate >= parameter.getRareItemSeuil()) {
					UseInCraftResponse response = itemDao.useInCraft(goalAchiever.getCode());
					if (!response.isError() && response.isUseInCraft()) {
						// Les items non craftable ne sont pas pris en compte
						rareResourceItems.add(resourceCode);
					}
				}
			}
		}
	}

	@Override
	public List<MonsterGoalAchiever> createMonstersGoals(StopValidator<FightResponse> stopCondition) {
		List<MonsterGoalAchiever> goalMonster = new ArrayList<>();
		List<BotMonster> monsters = this.monsterDao.getMonsters();
		List<BotBox> monstersBox = this.mapDAO.getMonstersBox();
		Map<String, List<Coordinate>> monsterLocation = new HashMap<>();
		for (BotBox monsterBox : monstersBox) {
			String code = monsterBox.getContent().getCode();
			monsterLocation.computeIfAbsent(code, location -> new ArrayList<>());
			monsterLocation.get(code).add(new Coordinate(monsterBox.getX(), monsterBox.getY()));
		}
		for (BotMonster monster : monsters) {
			MonsterGoalAchiever goalAchiever = new MonsterGoalAchiever(characterDao, mapDAO,
					monsterLocation.get(monster.getCode()), monster, monsterEquipementService, stopCondition,
					fightService, moveService, parameter);
			goalMonster.add(goalAchiever);
		}
		return goalMonster;
	}

	@Override
	public List<GoalAchiever> createTaskGoals() {
		List<GoalAchiever> goalTaskMaster = new ArrayList<>();
		List<BotBox> monstersTaskMasterBox = this.mapDAO.getTasksBox().stream()
				.filter(bb -> "monsters".equals(bb.getContent().getCode())).toList();
		List<Coordinate> monsterTaskMasterLocation = monstersTaskMasterBox.stream()
				.map(bb -> new Coordinate(bb.getX(), bb.getY())).toList();
		goalTaskMaster.add(new MonsterTaskGoalAchiever(characterDao, taskDao, bankDao, monsterTaskMasterLocation,
				moveService, characterService, parameter));

		List<BotBox> itemsTaskMasterBox = this.mapDAO.getTasksBox().stream()
				.filter(bb -> "items".equals(bb.getContent().getCode())).toList();
		List<Coordinate> itemsTaskMasterLocation = itemsTaskMasterBox.stream()
				.map(bb -> new Coordinate(bb.getX(), bb.getY())).toList();
		goalTaskMaster.add(new ItemTaskGoalAchiever(characterDao, taskDao, bankDao, itemsTaskMasterLocation,
				characterService, moveService, parameter));

		return goalTaskMaster;
	}

	private void createCraftGoal(Map<String, ArtifactGoalAchiever> goalsMap,
			ChooseBehaviorSelector chooseBehaviorSelector) {
		List<BotItemDetails> items = itemDao.getItems();
		List<BotItemDetails> craftItems = items.stream().filter(botItem -> botItem.getCraft() != null)
				.collect(Collectors.toList());
		boolean finish = false;
		while (!finish) {
			boolean goalCreate = false;
			for (Iterator<BotItemDetails> iterator = craftItems.iterator(); iterator.hasNext();) {
				BotItemDetails botItemDetails = iterator.next();
				if (createGoal(goalsMap, botItemDetails, true, chooseBehaviorSelector)) {
					iterator.remove();
					goalCreate = true;
				}

			}
			finish = craftItems.isEmpty() || !goalCreate;
		}
		// Création des taches avec des items obtenus autrements (par les taches
		// normalement)
		for (BotItemDetails botItemDetails : craftItems) {
			createGoal(goalsMap, botItemDetails, false, chooseBehaviorSelector);
		}
	}

	private boolean createGoal(Map<String, ArtifactGoalAchiever> goalsMap, BotItemDetails botItemDetails,
			boolean ignoreNoPresentGoal, ChooseBehaviorSelector chooseBehaviorSelector) {
		List<BotItem> items = botItemDetails.getCraft().getItems();
		GoalAchieverList goalAchieverList = new GoalAchieverList();
		ItemCraftGoalAchiever craftGoal = new ItemCraftGoalAchiever(characterDao, characterService,
				botItemDetails.getCode(), moveService, botItemDetails.getCraft().getLevel(),
				botItemDetails.getCraft().getSkill(), goalAchieverList);
		boolean containtsRareResource = false;
		boolean containtsTaskResource = false;
		for (BotItem botItem : items) {
			ArtifactGoalAchiever subGoal = goalsMap.get(botItem.getCode());
			ItemGetBankGoalAchiever itemGetBankGoalAchiever = new ItemGetBankGoalAchiever(bankDao, botItem.getCode(),
					moveService, characterService);
			if (subGoal != null) {
				GoalAchiever subSubGoalAchiever;
				if (getInfos(subGoal).isGathering()) {
					GoalAchiever equipToolGoalAchiever = createEquipToolGoalAchiever(
							getInfos(subGoal).getBotResourceSkill());
					GoalAchieverLoop goalAchieverLoop = new GoalAchieverLoop(subGoal, botItem.getQuantity());
					subSubGoalAchiever = new GoalAchieverTwoStep(characterDao, equipToolGoalAchiever, goalAchieverLoop,
							false, false);
				} else {
					subSubGoalAchiever = new GoalAchieverLoop(subGoal, botItem.getQuantity());
				}
				itemGetBankGoalAchiever.setQuantity(botItem.getQuantity());
				GoalAchieverTwoStep achieverTwoStep = new GoalAchieverTwoStep(characterDao, itemGetBankGoalAchiever,
						subSubGoalAchiever, false, false);
				goalAchieverList.add(achieverTwoStep);
				if (!containtsRareResource && rareResourceItems.contains(getInfos(subGoal).getItemCode())) {
					containtsRareResource = true;
				}
			} else if (ignoreNoPresentGoal) {
				// sous objectif non présent
				return false;
			} else {
				goalAchieverList.add(new ItemGetInventoryOrBankGoalAchiever(characterService, botItem.getCode(),
						itemGetBankGoalAchiever, botItem.getQuantity()));
				containtsTaskResource = true;
				containtsRareResource = true;
			}
		}

		if (goalsMap.containsKey(craftGoal.getCode())) {
			// Item qui peuvent être collecté, droppé ou bien crafté
			MultiGoalAchieverInfo multiGoalAchieverInfo;
			ArtifactGoalAchiever oldGoalInMap = goalsMap.get(craftGoal.getCode());
			CraftGoalAchieverInfo craftInfo = createCraftInfo(botItemDetails, containtsTaskResource,
					containtsRareResource);
			if (oldGoalInMap instanceof GoalAchieverChoose goalAchieverChoose) {
				// On met null car il doit déjà exister
				multiGoalAchieverInfo = getGoalInfo(goalAchieverChoose, (MultiGoalAchieverInfo) null);
				goalAchieverChoose.addGoal(craftGoal, craftInfo);
			} else {
				GoalAchieverChoose goalAchieverChoose = new GoalAchieverChoose(characterService,
						chooseBehaviorSelector);
				multiGoalAchieverInfo = getGoalInfo(goalAchieverChoose,
						new MultiGoalAchieverInfo(craftInfo.getItemCode(), craftInfo.getItemType()));
				goalAchieverChoose.addGoal(craftGoal, craftInfo);
				// On supprime l'ancien buts et ses infos
				GoalAchieverInfo infosRemoved = goalInfos.remove(oldGoalInMap);
				goalAchieverChoose.addGoal(oldGoalInMap, infosRemoved);
				this.goals.remove(oldGoalInMap);
				// On met à jour le nouveau
				if (infosRemoved.isGathering()) {
					multiGoalAchieverInfo.setGathering(infosRemoved.getBotResourceSkill());
					multiGoalAchieverInfo.addLevel(infosRemoved.getLevel(), INFO_TYPE.GATHERING);
				}
				goalsMap.put(craftGoal.getCode(), goalAchieverChoose);
				this.goals.add(goalAchieverChoose);
			}
			// Ajout des infos du craft
			multiGoalAchieverInfo.setCraft(craftInfo.getBotCraftSkill());
			multiGoalAchieverInfo.addLevel(craftInfo.getLevel(), INFO_TYPE.CRAFTING);
			multiGoalAchieverInfo.setNeedTaskMasterResource(craftInfo.isNeedTaskMasterResource());
			multiGoalAchieverInfo.setNeedRareResource(craftInfo.isNeedRareResource());
		} else {
			getGoalInfo(craftGoal, createCraftInfo(botItemDetails, containtsTaskResource, containtsRareResource));
			goalsMap.put(craftGoal.getCode(), craftGoal);
			this.goals.add(craftGoal);
		}
		return true;
	}

	private CraftGoalAchieverInfo createCraftInfo(BotItemDetails botItemDetails, boolean containtsTaskResource,
			boolean containtsRareResource) {
		return new CraftGoalAchieverInfo(botItemDetails.getCode(), botItemDetails.getType(),
				botItemDetails.getCraft().getSkill(), botItemDetails.getLevel(), containtsTaskResource,
				containtsRareResource);
	}

	@Override
	public List<ArtifactGoalAchiever> createItemsGoals(ChooseBehaviorSelector chooseBehaviorSelector) {
		this.goals.clear();
		Map<String, List<ArtifactGoalAchiever>> goalsMapResources = new HashMap<>();
		createResourceGoal(goalsMapResources);
		createResourceMonsterGoal(goalsMapResources);

		// Traitement des ressources multiples
		Map<String, ArtifactGoalAchiever> goalsMap = new HashMap<>();
		for (Entry<String, List<ArtifactGoalAchiever>> entry : goalsMapResources.entrySet()) {
			List<ArtifactGoalAchiever> value = entry.getValue();
			ArtifactGoalAchiever firstValueInList = value.getFirst();
			if (value.size() == 1) {
				goalsMap.put(entry.getKey(), firstValueInList);
			} else {
				GoalAchieverChoose goalAchieverChoose = new GoalAchieverChoose(characterService,
						chooseBehaviorSelector);
				MultiGoalAchieverInfo artifactGoalAchieverInfo = getGoalInfo(goalAchieverChoose,
						new MultiGoalAchieverInfo(getInfos(firstValueInList).getItemCode(),
								getInfos(firstValueInList).getItemType()));

				for (ArtifactGoalAchiever goal : value) {
					GoalAchieverInfo infos = getInfos(goal);
					goalAchieverChoose.addGoal(goal, infos);
					if (infos.isGathering()) {
						artifactGoalAchieverInfo.setGathering(infos.getBotResourceSkill());
						artifactGoalAchieverInfo.addLevel(((GatheringGoalAchieverInfo) infos).getLevel(),
								INFO_TYPE.GATHERING);
					}
				}

				// Pour l'instant on ne fait rien pour les monstres (level, ...)
				goalsMap.put(entry.getKey(), goalAchieverChoose);
				this.goals.add(goalAchieverChoose);
				// On les supprime des buts et des infos
				this.goals.removeAll(value);
				value.stream().forEach(goalInfos::remove);
			}
		}
		createUnequipGoal(goalsMap);
		createCraftGoal(goalsMap, chooseBehaviorSelector);
		return this.goals;
	}

	@Override
	public List<GoalAchiever> createManagedInventoryCustomGoal() {
		List<BotBox> workshopsBox = this.mapDAO.getGrandExchangesBox();
		List<Coordinate> workshopLocation = new ArrayList<>();
		for (BotBox workshopBox : workshopsBox) {
			workshopLocation.add(new Coordinate(workshopBox.getX(), workshopBox.getY()));
		}
		ArrayList<GoalAchiever> customGoals = new ArrayList<>();
		customGoals.add(new UseGoldItemManagerGoalAchiever(characterDao, characterService,
				itemDao.getItems().stream()
						.filter(bid -> bid.getEffects().stream().anyMatch(bie -> bie.getName().equals(BotEffect.GOLD)))
						.map(bid -> bid.getCode()).toList()));
		customGoals.add(new DepositGoldInBankGoalAchiever(characterDao, bankDao, moveService, characterService));
		customGoals.add(new ExtendBankSlotGoalAchiever(characterDao, bankDao, moveService, characterService));
		customGoals.add(new DepositToolGoalAchiever(bankDao, gameService, moveService, parameter, characterService));
		customGoals.add(new DepositTaskCoinGoalAchiever(bankDao, moveService, parameter, characterService));
		customGoals.add(new DepositResourceGoalAchiever(bankDao, moveService, resourceItemsCraftable, parameter,
				characterService));
		customGoals.add(new EquipmentManagerGoalAchiever(characterDao, itemDao, bankDao, grandExchangeDao, moveService,
				parameter, characterService));
		customGoals.add(new UselessEquipmentManagerGoalAchiever(characterDao, itemDao, bankDao, grandExchangeDao,
				fightService, monsterDao.getMonsters(), moveService, gameService, characterService));
		customGoals.add(new UselessResourceManagerGoalAchiever(characterDao, bankDao, itemDao, characterService,
				moveService, rareResourceItems));
		// Pour stocker les éventuelles ressources recyclées
		customGoals.add(new DepositResourceGoalAchiever(bankDao, moveService, resourceItemsCraftable, parameter,
				characterService));
		customGoals.add(new FoodManagerGoalAchiever(itemDao, bankDao, parameter, moveService, characterService));
		customGoals.add(new PotionManagerGoalAchiever(itemDao, bankDao, parameter, moveService, characterService));
		return customGoals;
	}

	@Override
	public List<ArtifactGoalAchiever> getDropItemGoal() {
		List<String> noResourceItemCode = itemDao.getItems().stream()
				.filter(bid -> !BotItemType.RESOURCE.equals(bid.getType()) && !bid.getEffects().isEmpty())
				.map(bid -> bid.getCode()).toList();
		return this.goals.stream().filter(
				aga -> aga instanceof ItemMonsterGoalAchiever imga && noResourceItemCode.contains(imga.getCode()))
				.<ArtifactGoalAchiever>map(aga -> registerDropItemInfo(new GoalAchieverLoop(aga, 1),
						((ItemMonsterGoalAchiever) aga).getCode()))
				.toList();
	}

	private GoalAchiever createEquipToolGoalAchiever(BotResourceSkill resourceSkill) {
		return new EquipToolGoalAchiever(characterDao, bankDao, moveService, characterService, gameService,
				resourceSkill);
	}

	private void createUnequipGoal(Map<String, ArtifactGoalAchiever> goalsMap) {
		UnequipFirstWeaponGoalAchiever goalArchiever = new UnequipFirstWeaponGoalAchiever(characterDao,
				() -> characterDao.getCharacter().getWeaponSlot(), BotCharacterInventorySlot.WEAPON);
		this.goals.add(goalArchiever);
		getGoalInfo(goalArchiever, new SimpleGoalAchieverInfo(GameConstants.FIRST_WEAPON, BotItemType.WEAPON));
		goalsMap.put(goalArchiever.getCode(), goalArchiever);
	}

	private ArtifactGoalAchiever registerDropItemInfo(ArtifactGoalAchiever artifactGoalAchiever, String code) {
		getGoalInfo(artifactGoalAchiever, new SimpleGoalAchieverInfo(code, itemDao.getItem(code).getType()));
		return artifactGoalAchiever;
	}

	@SuppressWarnings("unchecked")
	private <T extends GoalAchieverInfo> T getGoalInfo(ArtifactGoalAchiever artifactGoalAchiever,
			T artifactGoalAchieverInfo) {
		return (T) goalInfos.computeIfAbsent(artifactGoalAchiever, aga -> artifactGoalAchieverInfo);
	}

	@Override
	public GoalAchieverInfo getInfos(ArtifactGoalAchiever goal) {
		return goalInfos.get(goal);
	}

	@Override
	public GoalAchiever addItemRecycleGoalAchiever(ArtifactGoalAchiever goalAchiever, int minPreserve) {
		String code = getInfos(goalAchiever).getItemCode();
		ItemGetBankGoalAchiever itemGetBankGoalAchiever = new ItemGetBankGoalAchiever(bankDao, code, moveService,
				characterService) {
			@Override
			public void setRoot() {
				// On force le comportement comme s'il n'est pas root
				super.unsetRoot();
			}
		};
		itemGetBankGoalAchiever.setQuantity(characterService.getFreeInventorySpace());
		ItemRecycleGoalAchiever itemRecycleGoalAchiever = new ItemRecycleGoalAchiever(code, characterDao, moveService,
				characterService, getInfos(goalAchiever).getBotCraftSkill(), minPreserve);
		GoalAchieverTwoStep goalAchieverTwoStep = new GoalAchieverTwoStep(characterDao, itemGetBankGoalAchiever,
				itemRecycleGoalAchiever, true, false);
		return new GoalAchieverTwoStep(characterDao, goalAchieverTwoStep, goalAchiever, true, false);
	}

	@Override
	public GoalAchiever addDepositNoReservedItemGoalAchiever(GoalAchiever goalAchiever) {
		DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(
				bankDao, moveService, characterService, parameter);
		goalAchiever = new GoalAchieverTwoStep(characterDao, depositNoReservedItemGoalAchiever, goalAchiever, true,
				true);
		return goalAchiever;
	}
}
