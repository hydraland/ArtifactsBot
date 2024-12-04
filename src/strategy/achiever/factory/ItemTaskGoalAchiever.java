package strategy.achiever.factory;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.BankDAO;
import hydra.dao.CharacterDAO;
import hydra.dao.TaskDAO;
import hydra.model.BotCharacter;
import hydra.model.BotTaskType;
import strategy.achiever.GoalAchiever;
import strategy.achiever.GoalParameter;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.Cumulator;
import strategy.achiever.factory.util.GoalAverageOptimizer;
import strategy.util.CharacterService;
import strategy.util.MoveService;

public final class ItemTaskGoalAchiever extends AbstractTaskGoalAchiever {
	private final Map<String, ArtifactGoalAchiever> itemsGoals;
	private GoalAverageOptimizer goalAverageOptimizer;

	public ItemTaskGoalAchiever(CharacterDAO characterDAO, TaskDAO taskDao, BankDAO bankDAO,
			Map<String, ArtifactGoalAchiever> itemsGoals, List<Coordinate> coordinates,
			CharacterService characterService, MoveService moveService, GoalParameter parameter,
			GoalAverageOptimizer goalAverageOptimizer) {
		super(characterDAO, taskDao, bankDAO, coordinates, moveService, characterService, parameter);
		this.itemsGoals = itemsGoals;
		this.goalAverageOptimizer = goalAverageOptimizer;
	}

	@Override
	protected BotTaskType getTaskType() {
		return BotTaskType.ITEMS;
	}

	protected GoalAchiever createTaskGoalAchiever(String code, int total) {
		ArtifactGoalAchiever itemsGoal = itemsGoals.get(code);
		if (itemsGoal != null) {
			GoalAchiever goalAchiever;
			GoalAchieverList goalAchieverList = new GoalAchieverList();
			int freeSpace = characterService.getFreeInventorySpace();
			if (freeSpace < total) {
				DepositNoReservedItemGoalAchiever depositNoReservedItemGoalAchiever = new DepositNoReservedItemGoalAchiever(
						bankDAO, moveService, characterService);
				goalAchiever = new GoalAchieverTwoStep(characterDAO, depositNoReservedItemGoalAchiever,
						goalAchieverList, true, false);
				BotCharacter character = characterDAO.getCharacter();
				freeSpace = character.getInventoryMaxItems();
			} else {
				goalAchiever = goalAchieverList;
			}

			if (parameter.isOptimizeItemTask()) {
				int optimValue = goalAverageOptimizer.optimize(itemsGoal, total, 0.9f);
				if (optimValue > 1) {
					if (total > optimValue) {
						int nbPack = total / optimValue;
						for (int i = 0; i < nbPack; i++) {
							goalAchieverList.add(itemsGoal);
							goalAchieverList.add(new TradeGoalAchiever(code, optimValue));
							goalAchieverList.add(new ClearGoalAchiever(itemsGoal));
						}
						int quantity = total % optimValue;
						if (quantity > 0) {
							goalAchieverList
									.add(new OptimizeGoalAchiever(itemsGoal, goalAverageOptimizer, quantity, 0.9f));
							goalAchieverList.add(itemsGoal);
							goalAchieverList.add(new TradeGoalAchiever(code, quantity));
						}
					} else {
						goalAchieverList.add(itemsGoal);
						goalAchieverList.add(new TradeGoalAchiever(code, total));
					}
					return goalAchiever;
				}
			}
			BotCharacter character = characterDAO.getCharacter();
			// On prend au max 90% de l'inventaire
			freeSpace = Math.min(freeSpace, Math.round(character.getInventoryMaxItems() * 0.9f));
			if (total > freeSpace) {
				int nbPack = total / freeSpace;
				for (int i = 0; i < nbPack; i++) {
					ItemGetBankGoalAchiever itemGetBankGoalAchiever = new ItemGetBankGoalAchiever(bankDAO, code,
							moveService, characterService);
					itemGetBankGoalAchiever.setQuantity(freeSpace);
					goalAchieverList.add(itemGetBankGoalAchiever);
					goalAchieverList.add(new GoalAchieverLoop(itemsGoal, freeSpace));
					goalAchieverList.add(new TradeGoalAchiever(code, freeSpace));
				}
				int quantity = total % freeSpace;
				if (quantity > 0) {
					ItemGetBankGoalAchiever itemGetBankGoalAchiever = new ItemGetBankGoalAchiever(bankDAO, code,
							moveService, characterService);
					itemGetBankGoalAchiever.setQuantity(quantity);
					goalAchieverList.add(itemGetBankGoalAchiever);
					goalAchieverList.add(new GoalAchieverLoop(itemsGoal, quantity));
					goalAchieverList.add(new TradeGoalAchiever(code, quantity));
				}
			} else {
				ItemGetBankGoalAchiever itemGetBankGoalAchiever = new ItemGetBankGoalAchiever(bankDAO, code,
						moveService, characterService);
				itemGetBankGoalAchiever.setQuantity(total);
				goalAchieverList.add(itemGetBankGoalAchiever);
				goalAchieverList.add(new GoalAchieverLoop(itemsGoal, total));
				goalAchieverList.add(new TradeGoalAchiever(code, total));
			}
			return goalAchiever;
		}
		return null;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		return builder.toString();
	}

	private final class TradeGoalAchiever implements ArtifactGoalAchiever {

		private final String code;
		private final int quantity;
		private boolean finish;

		public TradeGoalAchiever(String code, int quantity) {
			this.code = code;
			this.quantity = quantity;
		}

		@Override
		public boolean isRealisable(BotCharacter character) {
			return true;
		}

		@Override
		public boolean execute(Map<String, Integer> reservedItems) {
			try {
				if (moveService.moveTo(coordinates) && taskDao.trade(code, quantity).ok()) {
					// On donne les items donc il ne sont plus dans l'inventaire
					reservedItems.remove(code);
					return true;
				}
				return false;
			} finally {
				this.finish = true;
			}
		}

		@Override
		public boolean isFinish() {
			return this.finish;
		}

		@Override
		public void clear() {
			this.finish = false;
		}

		@Override
		public void setRoot() {
			// Le fait d'être noeud racine ou pas ne change pas l'implémentation
		}

		@Override
		public void unsetRoot() {
			// Le fait d'être noeud racine ou pas ne change pas l'implémentation
		}

		@Override
		public double getRate() {
			return 1;
		}

		@Override
		public boolean acceptAndSetMultiplierCoefficient(int coefficient, Cumulator cumulator, int maxItem) {
			return false;
		}

		@Override
		public String toString() {
			ToStringBuilder builder = new ToStringBuilder(this);
			builder.append("code", code);
			builder.append("quantity", quantity);
			return builder.toString();
		}
	}
}
