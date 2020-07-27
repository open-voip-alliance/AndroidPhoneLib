package nl.spindle.phonelib.model

class PathConfigurations(private val basePath: String) {
    val linphoneChatDatabaseFile = "$basePath/linphone-history.db"
    val linphoneConfigFile = "$basePath/.linphonerc"
    val linphoneConfigXsp = "$basePath/lpconfig.xsd"
    val linphoneFactoryConfigFile = "$basePath/linphonerc"
    val linphoneRootCaFile = "$basePath/rootca.pem"

    val pauseSound = "$basePath/toy_mono.wav"
    val ringBackSound = "$basePath/ringback.wav"
    val ringSound = "$basePath/oldphone_mono.wav"
}