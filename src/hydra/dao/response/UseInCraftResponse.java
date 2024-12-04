package hydra.dao.response;

public class UseInCraftResponse {

	private boolean error;
	private boolean useInCraft;

	public UseInCraftResponse(boolean error, boolean useInCraft) {
		this.error = error;
		this.useInCraft = useInCraft;
	}

	public boolean isError() {
		return error;
	}

	public boolean isUseInCraft() {
		return useInCraft;
	}

}
