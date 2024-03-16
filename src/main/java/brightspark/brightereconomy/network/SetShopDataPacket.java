package brightspark.brightereconomy.network;

/*
	Not using a Kotlin data class annotated with @JvmRecord as there's a bug in Kapt for those.
	Kapt issue: https://youtrack.jetbrains.com/issue/KT-44706
 */
public record SetShopDataPacket(int itemCount, int cost) {}
