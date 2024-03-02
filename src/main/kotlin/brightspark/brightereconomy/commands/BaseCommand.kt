package brightspark.brightereconomy.commands

object BaseCommand : Command("brightereconomy", {
	thenCommand(BalanceCommand)
	thenCommand(ModifyBalanceCommand)
}) {
	init {
		aliases("be")
	}
}
