package strategy.achiever.factory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import hydra.GameConstants;
import hydra.dao.CharacterDAO;
import hydra.dao.EventsDAO;
import hydra.dao.ItemDAO;
import hydra.dao.MapDAO;
import hydra.dao.MonsterDAO;
import hydra.dao.ResourceDAO;
import hydra.dao.response.FightResponse;
import hydra.model.BotBox;
import hydra.model.BotCharacterInventorySlot;
import hydra.model.BotDropDescription;
import hydra.model.BotEffect;
import hydra.model.BotItem;
import hydra.model.BotItemDetails;
import hydra.model.BotItemReader;
import hydra.model.BotItemType;
import hydra.model.BotMonster;
import hydra.model.BotResource;
import strategy.achiever.EventNotification;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.goals.ArtifactGoalAchiever;
import strategy.achiever.factory.goals.GoalAchieverChoose;
import strategy.achiever.factory.goals.GoalAchieverChoose.ChooseBehaviorSelector;
import strategy.achiever.factory.goals.GoalAchieverList;
import strategy.achiever.factory.goals.ItemGetBankGoalAchiever;
import strategy.achiever.factory.goals.MonsterGoalAchiever;
import strategy.achiever.factory.goals.MonsterItemDropGoalAchiever;
import strategy.achiever.factory.goals.ResourceGoalAchiever;
import strategy.achiever.factory.info.CraftGoalAchieverInfo;
import strategy.achiever.factory.info.GatheringGoalAchieverInfo;
import strategy.achiever.factory.info.GoalAchieverInfo;
import strategy.achiever.factory.info.GoalAchieverInfo.INFO_TYPE;
import strategy.achiever.factory.info.MonsterGoalAchieverInfo;
import strategy.achiever.factory.info.MultiGoalAchieverInfo;
import strategy.achiever.factory.info.SimpleGoalAchieverInfo;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.StopChecker;
import strategy.util.CharacterService;

public final class ArtifactGoalFactory implements GoalFactory {

	private final ResourceDAO resourceDao;
	private final MapDAO mapDAO;
	private final CharacterDAO characterDao;
	private final ItemDAO itemDao;
	private final MonsterDAO monsterDao;
	private final GoalParameter parameter;
	private final List<String> resourceItemsCraftable;
	private final CharacterService characterService;
	private final GoalFactoryCreator factoryCreator;
	private final List<String> eventMonstersCode;
	private final List<String> eventResourceBoxCode;
	private final List<String> eventResources;
	private final List<String> rareResourceItems;

	public ArtifactGoalFactory(ResourceDAO resourceDAO, MonsterDAO monsterDao, MapDAO mapDAO, ItemDAO itemDao,
			CharacterDAO characterDAO, EventsDAO eventsDAO, GoalParameter parameter, CharacterService characterService,
			GoalFactoryCreator factoryCreator) {
		this.resourceDao = resourceDAO;
		this.monsterDao = monsterDao;
		this.mapDAO = mapDAO;
		this.characterDao = characterDAO;
		this.itemDao = itemDao;
		this.parameter = parameter;
		this.characterService = characterService;
		this.factoryCreator = factoryCreator;
		this.resourceItemsCraftable = itemDao.getResourceItems().stream().<String>map(bid -> bid.getCode())
				.filter(code -> itemDao.useInCraft(code).isUseInCraft()).toList();
		this.eventMonstersCode = new ArrayList<>();
		this.eventResourceBoxCode = new ArrayList<>();
		this.eventResources = new ArrayList<>();
		eventsDAO.getAllEvents().forEach(event -> {
			String type = event.getContent().getType();
			String code = event.getContent().getCode();
			if (EventNotification.MONSTER_EVENT_TYPE.equals(type)) {
				eventMonstersCode.add(code);
				BotMonster monster = monsterDao.getMonster(code);
				monster.getDrops().forEach(bdd -> eventResources.add(bdd.getCode()));
			} else if (EventNotification.RESOURCE_EVENT_TYPE.equals(type)) {
				eventResourceBoxCode.add(code);
				BotResource resource = resourceDao.getResource(code);
				resource.getDrops().forEach(bdd -> eventResources.add(bdd.getCode()));
			}
		});
		rareResourceItems = new ArrayList<>();
		initRareResource();
	}

	private void createResourceGoal(Map<String, List<ArtifactGoalAchiever>> goalsMap, List<ArtifactGoalAchiever> goals,
			Map<ArtifactGoalAchiever, GoalAchieverInfo<ArtifactGoalAchiever>> goalInfos, GoalFilter filter) {
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
				boolean event = eventResourceBoxCode.contains(boxCode);
				if (GoalFilter.ALL.equals(filter) || (event && GoalFilter.EVENT.equals(filter))
						|| (!event && GoalFilter.NO_EVENT.equals(filter))) {
					ResourceGoalAchiever goalAchiever = factoryCreator.createGatheringGoalAchiever(resourceCode, rate,
							resourceLocation.get(boxCode), resource.getLevel(), resource.getSkill(), boxCode, event);
					GoalAchiever depositNoReservedItemGoalAchiever = factoryCreator
							.createDepositNoReservedItemGoalAchiever();
					ArtifactGoalAchiever achieverTwoStep = factoryCreator
							.createGoalAchieverTwoStep(depositNoReservedItemGoalAchiever, goalAchiever, true, true);
					goals.add(achieverTwoStep);
					getGoalInfo(goalInfos, achieverTwoStep,
							new GatheringGoalAchieverInfo<ArtifactGoalAchiever>(resourceCode, BotItemType.RESOURCE,
									resource.getSkill(), resource.getLevel(), boxCode, achieverTwoStep));
					List<ArtifactGoalAchiever> goal = goalsMap.computeIfAbsent(resourceCode, c -> new ArrayList<>());
					goal.add(achieverTwoStep);
				}
			}
		}
	}

	private void createResourceMonsterGoal(Map<String, List<ArtifactGoalAchiever>> goalsMap,
			List<ArtifactGoalAchiever> goals,
			Map<ArtifactGoalAchiever, GoalAchieverInfo<ArtifactGoalAchiever>> goalInfos, GoalFilter filter) {
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
				boolean event = eventMonstersCode.contains(monster.getCode());
				if (GoalFilter.ALL.equals(filter) || (event && GoalFilter.EVENT.equals(filter))
						|| (!event && GoalFilter.NO_EVENT.equals(filter))) {
					ResourceGoalAchiever goalAchiever = factoryCreator.createItemMonsterGoalAchiever(resourceCode, rate,
							monsterLocation.get(boxCode), monster, event);
					GoalAchiever depositNoReservedItemGoalAchiever = factoryCreator
							.createDepositNoReservedItemGoalAchiever();
					ArtifactGoalAchiever achieverTwoStep = factoryCreator
							.createGoalAchieverTwoStep(depositNoReservedItemGoalAchiever, goalAchiever, true, true);
					goals.add(achieverTwoStep);
					getGoalInfo(goalInfos, achieverTwoStep, new MonsterGoalAchieverInfo<ArtifactGoalAchiever>(
							resourceCode, BotItemType.RESOURCE, monster.getCode(), achieverTwoStep));
					List<ArtifactGoalAchiever> goal = goalsMap.computeIfAbsent(resourceCode, c -> new ArrayList<>());
					goal.add(achieverTwoStep);
				}
			}
		}
	}

	@Override
	public List<MonsterGoalAchiever> createMonstersGoals(StopChecker<FightResponse> stopCondition, GoalFilter filter) {
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
			boolean event = eventMonstersCode.contains(monster.getCode());
			if (GoalFilter.ALL.equals(filter) || (event && GoalFilter.EVENT.equals(filter))
					|| (!event && GoalFilter.NO_EVENT.equals(filter))) {
				MonsterGoalAchiever goalAchiever = factoryCreator.createMonsterGoalAchiever(
						monsterLocation.get(monster.getCode()), monster, stopCondition, event);
				goalMonster.add(goalAchiever);
			}
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
		goalTaskMaster.add(factoryCreator.createMonsterTaskGoalAchiever(monsterTaskMasterLocation));

		List<BotBox> itemsTaskMasterBox = this.mapDAO.getTasksBox().stream()
				.filter(bb -> "items".equals(bb.getContent().getCode())).toList();
		List<Coordinate> itemsTaskMasterLocation = itemsTaskMasterBox.stream()
				.map(bb -> new Coordinate(bb.getX(), bb.getY())).toList();
		goalTaskMaster.add(factoryCreator.createItemTaskGoalAchiever(itemsTaskMasterLocation));

		return goalTaskMaster;
	}

	private void createCraftGoal(Map<String, ArtifactGoalAchiever> goalsMap,
			ChooseBehaviorSelector chooseBehaviorSelector, List<ArtifactGoalAchiever> goals,
			Map<ArtifactGoalAchiever, GoalAchieverInfo<ArtifactGoalAchiever>> goalInfos) {
		List<BotItemDetails> items = itemDao.getItems();
		List<BotItemDetails> craftItems = items.stream().filter(botItem -> botItem.getCraft() != null)
				.collect(Collectors.toList());
		boolean finish = false;
		while (!finish) {
			boolean goalCreate = false;
			for (Iterator<BotItemDetails> iterator = craftItems.iterator(); iterator.hasNext();) {
				BotItemDetails botItemDetails = iterator.next();
				if (createGoal(goalsMap, botItemDetails, true, chooseBehaviorSelector, goals, goalInfos)) {
					iterator.remove();
					goalCreate = true;
				}

			}
			finish = craftItems.isEmpty() || !goalCreate;
		}
		// Création des taches avec des items obtenus autrements (par les taches
		// normalement)
		for (BotItemDetails botItemDetails : craftItems) {
			createGoal(goalsMap, botItemDetails, false, chooseBehaviorSelector, goals, goalInfos);
		}
	}

	private boolean createGoal(Map<String, ArtifactGoalAchiever> goalsMap, BotItemDetails botItemDetails,
			boolean ignoreNoPresentGoal, ChooseBehaviorSelector chooseBehaviorSelector,
			List<ArtifactGoalAchiever> goals,
			Map<ArtifactGoalAchiever, GoalAchieverInfo<ArtifactGoalAchiever>> goalInfos) {
		List<BotItem> items = botItemDetails.getCraft().getItems();
		GoalAchieverList goalAchieverList = factoryCreator.createGoalAchieverList();
		ResourceGoalAchiever craftGoal = factoryCreator.createItemCraftGoalAchiever(botItemDetails.getCode(),
				botItemDetails.getCraft().getLevel(), botItemDetails.getCraft().getSkill(), goalAchieverList);
		boolean containtsRareResource = false;
		boolean containtsTaskResource = false;
		for (BotItemReader botItem : items) {
			ArtifactGoalAchiever subGoal = goalsMap.get(botItem.getCode());
			ItemGetBankGoalAchiever itemGetBankGoalAchiever = factoryCreator
					.createItemGetBankGoalAchiever(botItem.getCode());
			if (!containtsRareResource && rareResourceItems.contains(botItem.getCode())) {
				containtsRareResource = true;
			}
			if (subGoal != null) {
				GoalAchiever subSubGoalAchiever;
				if (goalInfos.get(subGoal).isGathering()) {
					GoalAchiever equipToolGoalAchiever = factoryCreator
							.createEquipToolGoalAchiever(goalInfos.get(subGoal).getBotResourceSkill());
					ArtifactGoalAchiever goalAchieverLoop = factoryCreator.createGoalAchieverLoop(subGoal,
							botItem.getQuantity(), false);
					subSubGoalAchiever = factoryCreator.createGoalAchieverTwoStep(equipToolGoalAchiever,
							goalAchieverLoop, false, false);
				} else {
					subSubGoalAchiever = factoryCreator.createGoalAchieverLoop(subGoal, botItem.getQuantity(), false);
				}
				itemGetBankGoalAchiever.setQuantity(botItem.getQuantity());
				ArtifactGoalAchiever achieverTwoStep = factoryCreator.createGoalAchieverTwoStep(itemGetBankGoalAchiever,
						subSubGoalAchiever, false, false);
				goalAchieverList.add(achieverTwoStep);
			} else if (ignoreNoPresentGoal) {
				// sous objectif non présent
				return false;
			} else {
				goalAchieverList.add(factoryCreator.createItemGetInventoryOrBankGoalAchiever(botItem.getCode(),
						itemGetBankGoalAchiever, botItem.getQuantity()));
				if (!eventResources.contains(botItem.getCode())) {
					containtsTaskResource = true;
					containtsRareResource = true;
				}
			}
		}

		if (goalsMap.containsKey(craftGoal.getCode())) {
			// Item qui peuvent être collecté, droppé ou bien crafté
			MultiGoalAchieverInfo<ArtifactGoalAchiever> multiGoalAchieverInfo;
			ArtifactGoalAchiever oldGoalInMap = goalsMap.get(craftGoal.getCode());
			CraftGoalAchieverInfo<ArtifactGoalAchiever> craftInfo = createCraftInfo(craftGoal, botItemDetails,
					containtsTaskResource, containtsRareResource);
			if (oldGoalInMap instanceof GoalAchieverChoose goalAchieverChoose) {
				// On met null car il doit déjà exister
				multiGoalAchieverInfo = getGoalInfo(goalInfos, goalAchieverChoose,
						(MultiGoalAchieverInfo<ArtifactGoalAchiever>) null);
				goalAchieverChoose.addGoal(craftGoal, craftInfo);
			} else {
				GoalAchieverChoose goalAchieverChoose = factoryCreator.createGoalAchieverChoose(chooseBehaviorSelector);
				multiGoalAchieverInfo = getGoalInfo(goalInfos, goalAchieverChoose,
						new MultiGoalAchieverInfo<ArtifactGoalAchiever>(craftInfo.getItemCode(),
								craftInfo.getItemType(), goalAchieverChoose));
				goalAchieverChoose.addGoal(craftGoal, craftInfo);
				// On supprime l'ancien buts et ses infos
				GoalAchieverInfo<ArtifactGoalAchiever> infosRemoved = goalInfos.remove(oldGoalInMap);
				goalAchieverChoose.addGoal(oldGoalInMap, infosRemoved);
				goals.remove(oldGoalInMap);
				// On met à jour le nouveau
				if (infosRemoved.isGathering()) {
					multiGoalAchieverInfo.setGathering(infosRemoved.getBotResourceSkill(),
							((GatheringGoalAchieverInfo<ArtifactGoalAchiever>) infosRemoved).getBoxCode());
					multiGoalAchieverInfo.addLevel(infosRemoved.getLevel(), INFO_TYPE.GATHERING);
				}
				goalsMap.put(craftGoal.getCode(), goalAchieverChoose);
				goals.add(goalAchieverChoose);
			}
			// Ajout des infos du craft
			multiGoalAchieverInfo.setCraft(craftInfo.getBotCraftSkill());
			multiGoalAchieverInfo.addLevel(craftInfo.getLevel(), INFO_TYPE.CRAFTING);
			multiGoalAchieverInfo.setNeedTaskMasterResource(craftInfo.isNeedTaskMasterResource());
			multiGoalAchieverInfo.setNeedRareResource(craftInfo.isNeedRareResource());
		} else {
			getGoalInfo(goalInfos, craftGoal,
					createCraftInfo(craftGoal, botItemDetails, containtsTaskResource, containtsRareResource));
			goalsMap.put(craftGoal.getCode(), craftGoal);
			goals.add(craftGoal);
		}
		return true;
	}

	private CraftGoalAchieverInfo<ArtifactGoalAchiever> createCraftInfo(ArtifactGoalAchiever goalAchiever,
			BotItemDetails botItemDetails, boolean containtsTaskResource, boolean containtsRareResource) {
		return new CraftGoalAchieverInfo<>(botItemDetails.getCode(), botItemDetails.getType(),
				botItemDetails.getCraft().getSkill(), botItemDetails.getLevel(), containtsTaskResource,
				containtsRareResource, goalAchiever);
	}

	@Override
	public Collection<GoalAchieverInfo<ArtifactGoalAchiever>> createItemsGoals(
			ChooseBehaviorSelector chooseBehaviorSelector, GoalFilter filter) {
		List<ArtifactGoalAchiever> goals = new ArrayList<>();
		Map<ArtifactGoalAchiever, GoalAchieverInfo<ArtifactGoalAchiever>> goalInfos = new HashMap<>();
		Map<String, List<ArtifactGoalAchiever>> goalsMapResources = new HashMap<>();
		createResourceGoal(goalsMapResources, goals, goalInfos, filter);
		createResourceMonsterGoal(goalsMapResources, goals, goalInfos, filter);

		// Traitement des ressources multiples
		Map<String, ArtifactGoalAchiever> goalsMap = new HashMap<>();
		for (Entry<String, List<ArtifactGoalAchiever>> entry : goalsMapResources.entrySet()) {
			List<ArtifactGoalAchiever> value = entry.getValue();
			ArtifactGoalAchiever firstValueInList = value.getFirst();
			if (value.size() == 1) {
				goalsMap.put(entry.getKey(), firstValueInList);
			} else {
				GoalAchieverChoose goalAchieverChoose = factoryCreator.createGoalAchieverChoose(chooseBehaviorSelector);
				MultiGoalAchieverInfo<ArtifactGoalAchiever> artifactGoalAchieverInfo = getGoalInfo(goalInfos,
						goalAchieverChoose,
						new MultiGoalAchieverInfo<ArtifactGoalAchiever>(goalInfos.get(firstValueInList).getItemCode(),
								goalInfos.get(firstValueInList).getItemType(), goalAchieverChoose));

				for (ArtifactGoalAchiever goal : value) {
					GoalAchieverInfo<ArtifactGoalAchiever> infos = goalInfos.get(goal);
					goalAchieverChoose.addGoal(goal, infos);
					if (infos.isGathering()) {
						artifactGoalAchieverInfo.setGathering(infos.getBotResourceSkill(),
								((GatheringGoalAchieverInfo<ArtifactGoalAchiever>) infos).getBoxCode());
						artifactGoalAchieverInfo.addLevel(
								((GatheringGoalAchieverInfo<ArtifactGoalAchiever>) infos).getLevel(),
								INFO_TYPE.GATHERING);
					}
				}

				// Pour l'instant on ne fait rien pour les monstres (level, ...)
				goalsMap.put(entry.getKey(), goalAchieverChoose);
				goals.add(goalAchieverChoose);
				// On les supprime des buts et des infos
				goals.removeAll(value);
				value.stream().forEach(goalInfos::remove);
			}
		}
		if (GoalFilter.ALL.equals(filter) || GoalFilter.NO_EVENT.equals(filter)) {
			createUnequipGoal(goalsMap, goals, goalInfos);
			createCraftGoal(goalsMap, chooseBehaviorSelector, goals, goalInfos);
		}
		return goalInfos.values();
	}

	private void initRareResource() {
		rareResourceItems.addAll(itemDao.getTaskItems().stream().<String>map(bid -> bid.getCode()).toList());
		List<BotMonster> monsters = this.monsterDao.getMonsters();
		for (BotMonster monster : monsters) {
			for (BotDropDescription drop : monster.getDrops()) {
				String resourceCode = drop.getCode();
				int rate = drop.getRate();
				if (rate >= parameter.getRareItemSeuil()) {
					rareResourceItems.add(resourceCode);
				}
			}
		}
		List<BotResource> resources = this.resourceDao.getAllResources();
		for (BotResource resource : resources) {
			for (BotDropDescription drop : resource.getDrops()) {
				String resourceCode = drop.getCode();
				int rate = drop.getRate();
				if (rate >= parameter.getRareItemSeuil()) {
					rareResourceItems.add(resourceCode);
				}
			}
		}
	}

	@Override
	public List<GoalAchiever> createManagedInventoryCustomGoal() {
		List<BotBox> workshopsBox = this.mapDAO.getGrandExchangesBox();
		List<Coordinate> workshopLocation = new ArrayList<>();
		for (BotBox workshopBox : workshopsBox) {
			workshopLocation.add(new Coordinate(workshopBox.getX(), workshopBox.getY()));
		}
		ArrayList<GoalAchiever> customGoals = new ArrayList<>();
		customGoals.add(factoryCreator.createUseGoldItemManagerGoalAchiever(itemDao.getItems().stream()
				.filter(bid -> bid.getEffects().stream().anyMatch(bie -> bie.getName().equals(BotEffect.GOLD)))
				.map(bid -> bid.getCode()).toList()));
		customGoals.add(factoryCreator.createDepositGoldInBankGoalAchiever());
		customGoals.add(factoryCreator.createExtendBankSlotGoalAchiever());
		customGoals.add(factoryCreator.createDepositToolGoalAchiever());
		customGoals.add(factoryCreator.createDepositTaskCoinGoalAchiever());
		customGoals.add(factoryCreator.createDepositResourceGoalAchiever(resourceItemsCraftable));
		customGoals.add(factoryCreator.createEquipmentManagerGoalAchiever());
		customGoals.add(factoryCreator.createUselessEquipmentManagerGoalAchiever(monsterDao.getMonsters()));
		customGoals.add(factoryCreator.createUselessResourceManagerGoalAchiever(rareResourceItems));
		// Pour stocker les éventuelles ressources recyclées
		customGoals.add(factoryCreator.createDepositResourceGoalAchiever(resourceItemsCraftable));
		customGoals.add(factoryCreator.createFoodManagerGoalAchiever());
		customGoals.add(factoryCreator.createPotionManagerGoalAchiever());
		return customGoals;
	}

	@Override
	public List<GoalAchieverInfo<MonsterItemDropGoalAchiever>> createDropItemGoal() {
		List<String> noResourceItemCode = itemDao.getItems().stream().filter(
				bid -> !BotItemType.RESOURCE.equals(bid.getType()) && !BotItemType.CONSUMABLE.equals(bid.getType()))
				.map(bid -> bid.getCode()).toList();
		Map<String, List<ArtifactGoalAchiever>> goalsMap = new HashMap<>();
		List<ArtifactGoalAchiever> goals = new ArrayList<>();
		Map<ArtifactGoalAchiever, GoalAchieverInfo<ArtifactGoalAchiever>> goalInfos = new HashMap<>();
		createResourceMonsterGoal(goalsMap, goals, goalInfos, GoalFilter.NO_EVENT);
		return goalInfos.values().stream().filter(aga -> noResourceItemCode.contains(aga.getItemCode()))
				.<GoalAchieverInfo<MonsterItemDropGoalAchiever>>map(
						aga -> new MonsterGoalAchieverInfo<MonsterItemDropGoalAchiever>(aga.getItemCode(),
								itemDao.getItem(aga.getItemCode()).getType(), aga.getMonsterCode(),
								factoryCreator.createMonsterItemDropGoalAchiever(aga, characterDao, parameter)))
				.toList();
	}

	private void createUnequipGoal(Map<String, ArtifactGoalAchiever> goalsMap, List<ArtifactGoalAchiever> goals,
			Map<ArtifactGoalAchiever, GoalAchieverInfo<ArtifactGoalAchiever>> goalInfos) {
		ResourceGoalAchiever goalArchiever = factoryCreator.createUnequipFirstWeaponGoalAchiever(
				() -> characterDao.getCharacter().getWeaponSlot(), BotCharacterInventorySlot.WEAPON);
		goals.add(goalArchiever);
		getGoalInfo(goalInfos, goalArchiever, new SimpleGoalAchieverInfo<ArtifactGoalAchiever>(
				GameConstants.FIRST_WEAPON, BotItemType.WEAPON, goalArchiever));
		goalsMap.put(goalArchiever.getCode(), goalArchiever);
	}

	@SuppressWarnings("unchecked")
	private <T extends GoalAchieverInfo<ArtifactGoalAchiever>> T getGoalInfo(
			Map<ArtifactGoalAchiever, GoalAchieverInfo<ArtifactGoalAchiever>> goalInfos,
			ArtifactGoalAchiever artifactGoalAchiever, T artifactGoalAchieverInfo) {
		return (T) goalInfos.computeIfAbsent(artifactGoalAchiever, aga -> artifactGoalAchieverInfo);
	}

	@Override
	public ArtifactGoalAchiever addItemRecycleGoalAchiever(GoalAchieverInfo<ArtifactGoalAchiever> goalAchiever,
			int minPreserve) {
		String code = goalAchiever.getItemCode();
		ItemGetBankGoalAchiever itemGetBankGoalAchiever = factoryCreator.createItemGetBankGoalAchieverForceNoRoot(code);
		itemGetBankGoalAchiever.setQuantity(characterService.getFreeInventorySpace());
		GoalAchiever itemRecycleGoalAchiever = factoryCreator.createItemRecycleGoalAchiever(code,
				goalAchiever.getBotCraftSkill(), minPreserve);
		GoalAchiever goalAchieverTwoStep = factoryCreator.createGoalAchieverTwoStep(itemGetBankGoalAchiever,
				itemRecycleGoalAchiever, true, false);
		return factoryCreator.createGoalAchieverTwoStep(goalAchieverTwoStep, goalAchiever.getGoal(), true, false);
	}

	@Override
	public GoalAchiever addDepositNoReservedItemGoalAchiever(GoalAchiever goalAchiever) {
		GoalAchiever depositNoReservedItemGoalAchiever = factoryCreator.createDepositNoReservedItemGoalAchiever();
		goalAchiever = factoryCreator.createGoalAchieverTwoStep(depositNoReservedItemGoalAchiever, goalAchiever, true,
				true);
		return goalAchiever;
	}

	@Override
	public GoalAchiever addUsefullGoalToEventGoal(GoalAchieverInfo<ArtifactGoalAchiever> goalAchieverInfo) {
		GoalAchiever goalAchieverIntermediate = factoryCreator.createGoalAchieverConditional(goalAchieverInfo.getGoal(),
				() -> false, true);
		return factoryCreator.createGoalAchieverTwoStep(
				factoryCreator.createEquipToolGoalAchiever(goalAchieverInfo.getBotResourceSkill()),
				goalAchieverIntermediate, true, true);
	}
}
