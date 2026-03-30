package fr.plaglefleau.api.receive

data class ReceiveCardDebit(
    val id: Int?,
    val nfcCode: String?,
    val amount: Double,
    val standName: String
)