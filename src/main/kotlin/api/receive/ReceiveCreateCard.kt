package fr.plaglefleau.api.receive

data class ReceiveCreateCard(
    val pin: Int,
    val nfcCode: String
)
