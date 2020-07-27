package nl.spindle.phonelib.service

import org.linphone.core.*
import java.nio.ByteBuffer

/**
 * This interface hides the LinphoneCoreListener methods that aren't used in Simple Linphone with default implementations.
 */
interface SimpleLinphoneCoreListener : LinphoneCoreListener {

    override fun authInfoRequested(linphoneCore: LinphoneCore, s: String, s1: String, s2: String) {}
    override fun authenticationRequested(linphoneCore: LinphoneCore, linphoneAuthInfo: LinphoneAuthInfo, authMethod: LinphoneCore.AuthMethod) {}
    override fun callStatsUpdated(linphoneCore: LinphoneCore, linphoneCall: LinphoneCall, linphoneCallStats: LinphoneCallStats) {}
    override fun newSubscriptionRequest(linphoneCore: LinphoneCore, linphoneFriend: LinphoneFriend, s: String) {}
    override fun notifyPresenceReceived(linphoneCore: LinphoneCore, linphoneFriend: LinphoneFriend) {}
    override fun dtmfReceived(linphoneCore: LinphoneCore, linphoneCall: LinphoneCall, i: Int) {}
    override fun notifyReceived(linphoneCore: LinphoneCore, linphoneCall: LinphoneCall, linphoneAddress: LinphoneAddress, bytes: ByteArray) {}
    override fun transferState(linphoneCore: LinphoneCore, linphoneCall: LinphoneCall, state: LinphoneCall.State) {}
    override fun infoReceived(linphoneCore: LinphoneCore, linphoneCall: LinphoneCall, linphoneInfoMessage: LinphoneInfoMessage) {}
    override fun subscriptionStateChanged(linphoneCore: LinphoneCore, linphoneEvent: LinphoneEvent, subscriptionState: SubscriptionState) {}
    override fun publishStateChanged(linphoneCore: LinphoneCore, linphoneEvent: LinphoneEvent, publishState: PublishState) {}
    override fun show(linphoneCore: LinphoneCore) {}
    override fun displayStatus(linphoneCore: LinphoneCore, s: String) {}
    override fun displayMessage(linphoneCore: LinphoneCore, s: String) {}
    override fun displayWarning(linphoneCore: LinphoneCore, s: String) {}
    override fun fileTransferProgressIndication(linphoneCore: LinphoneCore, linphoneChatMessage: LinphoneChatMessage, linphoneContent: LinphoneContent, i: Int) {}
    override fun fileTransferRecv(linphoneCore: LinphoneCore, linphoneChatMessage: LinphoneChatMessage, linphoneContent: LinphoneContent, bytes: ByteArray, i: Int) {}
    override fun fileTransferSend(linphoneCore: LinphoneCore, linphoneChatMessage: LinphoneChatMessage, linphoneContent: LinphoneContent, byteBuffer: ByteBuffer, i: Int): Int {
        return 0
    }
    override fun callEncryptionChanged(linphoneCore: LinphoneCore, linphoneCall: LinphoneCall, b: Boolean, s: String) {}
    override fun isComposingReceived(linphoneCore: LinphoneCore, linphoneChatRoom: LinphoneChatRoom) {}
    override fun ecCalibrationStatus(linphoneCore: LinphoneCore, ecCalibratorStatus: LinphoneCore.EcCalibratorStatus, i: Int, o: Any) {}
    override fun globalState(linphoneCore: LinphoneCore, globalState: LinphoneCore.GlobalState, s: String) {}
    override fun uploadProgressIndication(linphoneCore: LinphoneCore, i: Int, i1: Int) {}
    override fun uploadStateChanged(linphoneCore: LinphoneCore, logCollectionUploadState: LinphoneCore.LogCollectionUploadState, s: String) {}
    override fun friendListCreated(linphoneCore: LinphoneCore, linphoneFriendList: LinphoneFriendList) {}
    override fun friendListRemoved(linphoneCore: LinphoneCore, linphoneFriendList: LinphoneFriendList) {}
    override fun networkReachableChanged(linphoneCore: LinphoneCore, b: Boolean) {}
    override fun messageReceived(linphoneCore: LinphoneCore, linphoneChatRoom: LinphoneChatRoom, linphoneChatMessage: LinphoneChatMessage) {}
    override fun messageReceivedUnableToDecrypted(linphoneCore: LinphoneCore, linphoneChatRoom: LinphoneChatRoom, linphoneChatMessage: LinphoneChatMessage) {}
    override fun notifyReceived(linphoneCore: LinphoneCore, linphoneEvent: LinphoneEvent, s: String, linphoneContent: LinphoneContent) {}
    override fun configuringStatus(linphoneCore: LinphoneCore, remoteProvisioningState: LinphoneCore.RemoteProvisioningState, s: String) {}
    override fun callState(linphoneCore: LinphoneCore, linphoneCall: LinphoneCall, state: LinphoneCall.State, s: String) {}
    override fun registrationState(linphoneCore: LinphoneCore, linphoneProxyConfig: LinphoneProxyConfig, registrationState: LinphoneCore.RegistrationState, s: String) {}
}