package fr.plaglefleau.api.receive

data class ReceiveDebitCard(
    val amount: Double,
    val standName: String
)