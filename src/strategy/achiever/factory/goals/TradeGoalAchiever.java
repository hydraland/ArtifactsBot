package strategy.achiever.factory.goals;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

import hydra.dao.TaskDAO;
import hydra.model.BotCharacter;
import strategy.achiever.factory.util.Coordinate;
import strategy.achiever.factory.util.Cumulator;
import strategy.util.MoveService;

public final class TradeGoalAchiever implements ArtifactGoalAchiever {
	private final String code;
	private final int quantity;
	private boolean finish;
	private final MoveService moveService;
	private final TaskDAO taskDao;
	private final List<Coordinate> coordinates;

	public TradeGoalAchiever(MoveService moveService, TaskDAO taskDao, List<Coordinate> coordinates, String code,
			int quantity) {
		this.moveService = moveService;
		this.taskDao = taskDao;
		this.coordinates = coordinates;
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