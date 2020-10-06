# AndroidPhoneLib

This is a library intended for use by Spindle which is responsible for VoIP SIP communication.

It is designed to make it easier to implement SIP functions into an app.
Currently it uses Linphone as the underlying SIP SDK. But it's built in a way that the SIP SDK can easily be swapped by another one.

## Installation

### Under construction


## Permissions

Currently the permissions required are:


    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

Runtime you'll have to require the Microphone and Camera permission (Camera only for video calls).

## Registration

Step 1: Import
```
import the AndroidPhoneLib .aar
```

Step 2: Initalise `AndroidPhoneLib`.

```
PhoneLib.getInstance(CONTEXT).initialise(this)
```

Step 3: Use `register` to register the softphone. 

```
            PhoneLib.getInstance(this).register(account, password, serverIP, port, stunServer, object : RegistrationCallback() {
                override fun stateChanged(registrationState: RegistrationState) {
                    super.stateChanged(registrationState)
                    if (registrationState == RegistrationState.REGISTERED) {
                        Log.d(TAG, "registered: ")
                    } else {
                        Log.e(TAG, "registrationFailed")
                    }
                }
            })
```

If register succeeds the callback `stateChanged(RegistrationState.REGISTERED)` will be called.

To `unregister` use:

```
            PhoneLib.getInstance(this).unregister()
```

This will call `stateChanged(RegistrationState.CLEARED)` of the callback

## Set supported codecs.

### Set codecs
To set codecs you should use `setAudioCodecs(context: Context, codecs: Set<Codec>)`. Supported codecs are:
- GSM
- G722
- G729
- ILBC
- ISAC
- L16
- OPUS
- PCMU
- PCMA
- SPEEX
By default ALL codecs are turned on. This must happen before registering and won't work when already SIP-registered.

### Reset codecs
To turn off your codec preferences `resetAudioCodecs()` can be used. After using this, all codecs are turned on again.


## Call functions

All phone/call Callbacks are sent to the SessionCallback, you can easily register on it like this:

        PhoneLib.getInstance(this).setSessionCallback(object : SessionCallback() {
        abstract class SessionCallback {
            override fun incomingCall(incomingSession: Session) {
                super.incomingCall(incomingSession)
            }

            override fun outgoingInit(session: Session) {
                super.outgoingInit(session)
            }

            override fun sessionConnected(session: Session) {
                super.sessionConnected(session)
            }

            override fun sessionReleased(session: Session) {
                super.sessionReleased(session)
            }

            override fun sessionEnded(session: Session) {
                super.sessionEnded(session)
            }

            override fun sessionUpdated(session: Session) {
                super.sessionUpdated(session)
            }

            override fun error(session: Session) {
                super.error(session)
            }
        })

The session object as shown in `incomingSession` is needed to answer an incoming call.

### Outgoing call
Once registered you can make a call by calling `PhoneLib.getInstance(CONTEXT).callTo(NUMBER, IS_VIDEO)`. It will return a generic `Session` object which we'll need later.

```
val session = PhoneLib.getInstance(this@Activity).callTo('0612345678', false)
```

## Control an incoming call

### Incoming call
Incoming call are received via `incomingCall(session: Session?)` in the `PhoneCallback`.

### Decline call
Declining a call is done via `PhoneLib.getInstance(this@Activity).declineIncoming(session)` (here you use the session object from earlier).

### Accept call
Accepting a call is done via `PhoneLib.getInstance(this@Activity).acceptIncoming(session)` (here you use the session object from earlier).

### End call
Ending a call is done via `PhoneLib.getInstance(this@Activity).end(session)`.


## Controlling an active call

### Mute the microphone
Muting the microphone is done via `PhoneLib.getInstance(this@Activity).setMuteMicrophone(true)`.

### Set call on hold
Setting call on or off hold is done with `PhoneLib.getInstance(this@Activity).setHold(session, true)`.

### Set call on hold
Setting call on or off hold is done with `PhoneLib.getInstance(this@Activity).setHold(session, true)`.

### Transfer a session to a number unattended.
Transfer a session unattended to a number with `PhoneLib.getInstance(this@Activity).transferUnattended(session, "0612345678")`.


## Get call states

### Checking if microphone is muted
Checking if microphone is on can be done via `PhoneLib.getInstance(this@Activity).isMicrophoneMuted()`.

### Check if call is on hold
The call is on hold when the CallState is `Paused` (or to be exact: `CallState.Paused`). If the other party has set the call to paused the call state is `PausedByRemote`.


### Getting session/call information
The `Session` object contains all information about the session. 
`getState: CallState` returns the call state.
`getDisplayName: String` returns the display name of the caller.
`getPhoneNumber: String` returns the phone number of the caller.
`getDuration: Int` returns the duration in seconds of the caller.
`getReason: org.openvoipalliance.phonelib.model.Reason` returns the reason of the session state.


#### State can be:
    Idle,
    IncomingReceived,
    OutgoingInit,
    OutgoingProgress,
    OutgoingRinging,
    OutgoingEarlyMedia,
    Connected,
    StreamsRunning,
    Pausing,
    Paused,
    Resuming,
    Referred,
    Error,
    CallEnd,
    PausedByRemote,
    CallUpdatedByRemote,
    CallIncomingEarlyMedia,
    CallUpdating,
    CallReleased,
    CallEarlyUpdatedByRemote,
    CallEarlyUpdating,
    Unknown

#### Reason can be:
    NONE(0),
    NO_RESPONSE(1),
    BAD_CREDENTIALS(2),
    DECLINED(3),
    NOT_FOUND(4),
    NOT_ANSWERED(5),
    BUSY(6),
    MEDIA(7),
    IO_ERROR(8),
    DO_NOT_DISTURB(9),
    UNAUTHORISED(10),
    NOT_ACCEPTABLE(11),
    NO_MATCH(12),
    MOVED_PERMANENTLY(13),
    GONE(14),
    TEMPORARILY_UNAVAILABLE(15),
    ADDRESS_INCOMPLETE(16),
    NOT_IMPLEMENTED(17),
    BAD_GATEWAY(18),
    SERVER_TIMEOUT(19),
    UNKNOWN(20)


### Video Call
UNDER CONSTRUCTION

## Other
If you have any question please let us know via `info@coffeeit.nl`. Or by contacting the project manager.