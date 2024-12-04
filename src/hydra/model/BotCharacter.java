package hydra.model;

import java.io.Serializable;
import java.util.List;

public class BotCharacter implements Serializable {
	private static final long serialVersionUID = 1L;
	private int attackAir;
	private int attackEarth;
	private int attackFire;
	private int attackWater;
	private int dmgAir;
	private int dmgEarth;
	private int dmgFire;
	private int dmgWater;
	private int alchemyLevel;
	private int cookingLevel;
	private int weaponcraftingLevel;
	private int gearcraftingLevel;
	private int jewelrycraftingLevel;
	private int woodcuttingLevel;
	private int miningLevel;
	private int resAir;
	private int resEarth;
	private int resFire;
	private int resWater;
	private int hp;
	private int maxHp;
	private String weaponSlot;
	private String amuletSlot;
	private String artifact1Slot;
	private String artifact2Slot;
	private String artifact3Slot;
	private String bodyArmorSlot;
	private String bootsSlot;
	private String utility1Slot;
	private String utility2Slot;
	private String helmetSlot;
	private String legArmorSlot;
	private String ring1Slot;
	private String ring2Slot;
	private String shieldSlot;
	private int inventoryMaxItems;
	private int level;
	private int x;
	private int y;
	private int fishingLevel;
	private List<BotInventoryItem> inventory;
	private int utility1SlotQuantity;
	private int utility2SlotQuantity;
	private String task;
	private int taskProgress;
	private int taskTotal;
	private BotTaskType taskType;
	private int fishingXp;
	private int gearcraftingXp;
	private int cookingXp;
	private int alchemyXp;
	private int jewelrycraftingXp;
	private int miningXp;
	private int woodcuttingXp;
	private int weaponcraftingXp;
	private int gold;
	private int haste;

	public int getAttackAir() {
		return attackAir;
	}

	public void setAttackAir(int attackAir) {
		this.attackAir = attackAir;
	}

	public int getAttackEarth() {
		return attackEarth;
	}

	public void setAttackEarth(int attackEarth) {
		this.attackEarth = attackEarth;
	}

	public int getAttackFire() {
		return attackFire;
	}

	public void setAttackFire(int attackFire) {
		this.attackFire = attackFire;
	}

	public int getAttackWater() {
		return attackWater;
	}

	public void setAttackWater(int attackWater) {
		this.attackWater = attackWater;
	}

	public int getCookingLevel() {
		return cookingLevel;
	}

	public void setCookingLevel(int cookingLevel) {
		this.cookingLevel = cookingLevel;
	}

	public int getWeaponcraftingLevel() {
		return weaponcraftingLevel;
	}

	public void setWeaponcraftingLevel(int weaponcraftingLevel) {
		this.weaponcraftingLevel = weaponcraftingLevel;
	}

	public int getGearcraftingLevel() {
		return gearcraftingLevel;
	}

	public void setGearcraftingLevel(int gearcraftingLevel) {
		this.gearcraftingLevel = gearcraftingLevel;
	}

	public int getJewelrycraftingLevel() {
		return jewelrycraftingLevel;
	}

	public void setJewelrycraftingLevel(int jewelrycraftingLevel) {
		this.jewelrycraftingLevel = jewelrycraftingLevel;
	}

	public int getWoodcuttingLevel() {
		return woodcuttingLevel;
	}

	public void setWoodcuttingLevel(int woodcuttingLevel) {
		this.woodcuttingLevel = woodcuttingLevel;
	}

	public int getMiningLevel() {
		return miningLevel;
	}

	public void setMiningLevel(int miningLevel) {
		this.miningLevel = miningLevel;
	}

	public void setInventory(List<BotInventoryItem> item) {
		this.inventory = item;
	}

	public List<BotInventoryItem> getInventory() {
		return inventory;
	}

	public int getResAir() {
		return resAir;
	}

	public void setResAir(int resAir) {
		this.resAir = resAir;
	}

	public int getResEarth() {
		return resEarth;
	}

	public void setResEarth(int resEarth) {
		this.resEarth = resEarth;
	}

	public int getResFire() {
		return resFire;
	}

	public void setResFire(int resFire) {
		this.resFire = resFire;
	}

	public int getResWater() {
		return resWater;
	}

	public void setResWater(int resWater) {
		this.resWater = resWater;
	}

	public final String getWeaponSlot() {
		return weaponSlot;
	}

	public final void setWeaponSlot(String weaponSlot) {
		this.weaponSlot = weaponSlot;
	}

	public final String getAmuletSlot() {
		return amuletSlot;
	}

	public final void setAmuletSlot(String amuletSlot) {
		this.amuletSlot = amuletSlot;
	}

	public final String getArtifact1Slot() {
		return artifact1Slot;
	}

	public final void setArtifact1Slot(String artifact1Slot) {
		this.artifact1Slot = artifact1Slot;
	}

	public final String getArtifact2Slot() {
		return artifact2Slot;
	}

	public final void setArtifact2Slot(String artifact2Slot) {
		this.artifact2Slot = artifact2Slot;
	}

	public final String getArtifact3Slot() {
		return artifact3Slot;
	}

	public final void setArtifact3Slot(String artifact3Slot) {
		this.artifact3Slot = artifact3Slot;
	}

	public final String getBodyArmorSlot() {
		return bodyArmorSlot;
	}

	public final void setBodyArmorSlot(String bodyArmorSlot) {
		this.bodyArmorSlot = bodyArmorSlot;
	}

	public final String getBootsSlot() {
		return bootsSlot;
	}

	public final void setBootsSlot(String bootsSlot) {
		this.bootsSlot = bootsSlot;
	}

	public final String getUtility1Slot() {
		return utility1Slot;
	}

	public final void setUtility1Slot(String utility1Slot) {
		this.utility1Slot = utility1Slot;
	}

	public final String getUtility2Slot() {
		return utility2Slot;
	}

	public final void setUtility2Slot(String utility2Slot) {
		this.utility2Slot = utility2Slot;
	}

	public final String getHelmetSlot() {
		return helmetSlot;
	}

	public final void setHelmetSlot(String helmetSlot) {
		this.helmetSlot = helmetSlot;
	}

	public final String getLegArmorSlot() {
		return legArmorSlot;
	}

	public final void setLegArmorSlot(String legArmorSlot) {
		this.legArmorSlot = legArmorSlot;
	}

	public final String getRing1Slot() {
		return ring1Slot;
	}

	public final void setRing1Slot(String ring1Slot) {
		this.ring1Slot = ring1Slot;
	}

	public final String getRing2Slot() {
		return ring2Slot;
	}

	public final void setRing2Slot(String ring2Slot) {
		this.ring2Slot = ring2Slot;
	}

	public final String getShieldSlot() {
		return shieldSlot;
	}

	public final void setShieldSlot(String shieldSlot) {
		this.shieldSlot = shieldSlot;
	}

	public final int getHp() {
		return hp;
	}

	public final void setHp(int hp) {
		this.hp = hp;
	}

	public int getInventoryMaxItems() {
		return inventoryMaxItems;
	}

	public void setInventoryMaxItems(int inventoryMaxItems) {
		this.inventoryMaxItems = inventoryMaxItems;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getDmgAir() {
		return dmgAir;
	}

	public void setDmgAir(int dmgAir) {
		this.dmgAir = dmgAir;
	}

	public int getDmgEarth() {
		return dmgEarth;
	}

	public void setDmgEarth(int dmgEarth) {
		this.dmgEarth = dmgEarth;
	}

	public int getDmgFire() {
		return dmgFire;
	}

	public void setDmgFire(int dmgFire) {
		this.dmgFire = dmgFire;
	}

	public int getDmgWater() {
		return dmgWater;
	}

	public void setDmgWater(int dmgWater) {
		this.dmgWater = dmgWater;
	}

	public final int getX() {
		return x;
	}

	public final void setX(int x) {
		this.x = x;
	}

	public final int getY() {
		return y;
	}

	public final void setY(int y) {
		this.y = y;
	}

	public int getFishingLevel() {
		return fishingLevel;
	}

	public void setFishingLevel(int fishingLevel) {
		this.fishingLevel = fishingLevel;
	}

	public int getUtility1SlotQuantity() {
		return utility1SlotQuantity;
	}

	public void setUtility1SlotQuantity(int utility1SlotQuantity) {
		this.utility1SlotQuantity = utility1SlotQuantity;
	}

	public int getUtility2SlotQuantity() {
		return utility2SlotQuantity;
	}

	public void setUtility2SlotQuantity(int utility2SlotQuantity) {
		this.utility2SlotQuantity = utility2SlotQuantity;
	}

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public int getTaskProgress() {
		return taskProgress;
	}

	public void setTaskProgress(int taskProgress) {
		this.taskProgress = taskProgress;
	}

	public int getTaskTotal() {
		return taskTotal;
	}

	public void setTaskTotal(int taskTotal) {
		this.taskTotal = taskTotal;
	}

	public BotTaskType getTaskType() {
		return taskType;
	}

	public void setTaskType(BotTaskType taskType) {
		this.taskType = taskType;
	}

	public int getFishingXp() {
		return fishingXp;
	}

	public void setFishingXp(int fishingXp) {
		this.fishingXp = fishingXp;
	}

	public int getGearcraftingXp() {
		return gearcraftingXp;
	}

	public void setGearcraftingXp(int gearcraftingXp) {
		this.gearcraftingXp = gearcraftingXp;
	}

	public int getCookingXp() {
		return cookingXp;
	}

	public void setCookingXp(int cookingXp) {
		this.cookingXp = cookingXp;
	}

	public int getJewelrycraftingXp() {
		return jewelrycraftingXp;
	}

	public void setJewelrycraftingXp(int jewelrycraftingXp) {
		this.jewelrycraftingXp = jewelrycraftingXp;
	}

	public int getMiningXp() {
		return miningXp;
	}

	public void setMiningXp(int miningXp) {
		this.miningXp = miningXp;
	}

	public int getWoodcuttingXp() {
		return woodcuttingXp;
	}

	public void setWoodcuttingXp(int woodcuttingXp) {
		this.woodcuttingXp = woodcuttingXp;
	}

	public int getWeaponcraftingXp() {
		return weaponcraftingXp;
	}

	public void setWeaponcraftingXp(int weaponcraftingXp) {
		this.weaponcraftingXp = weaponcraftingXp;
	}

	public final int getGold() {
		return gold;
	}

	public final void setGold(int gold) {
		this.gold = gold;
	}

	public final int getMaxHp() {
		return maxHp;
	}

	public final void setMaxHp(int maxHp) {
		this.maxHp = maxHp;
	}

	public final int getAlchemyLevel() {
		return alchemyLevel;
	}

	public final void setAlchemyLevel(int alchemyLevel) {
		this.alchemyLevel = alchemyLevel;
	}

	public final int getAlchemyXp() {
		return alchemyXp;
	}

	public final void setAlchemyXp(int alchemyXp) {
		this.alchemyXp = alchemyXp;
	}

	public final int getHaste() {
		return haste;
	}

	public final void setHaste(int haste) {
		this.haste = haste;
	}

}