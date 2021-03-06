<h1 align="center">Silence</h1>  
<h3 align="center">Block unknown callers</h3>  
<br/>

<p align="center">
<img 
    src="https://raw.githubusercontent.com/x13a/Silence/master/data/presentation.png">
</p>

[<img 
    src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
    alt="Get it on F-Droid"
    height="80">](https://f-droid.org/packages/me.lucky.silence/)

<b>By default numbers not in your contacts are blocked.</b>

Optionally allow:
* Numbers you have contacted
* Numbers within the selected groups
* X registered call(s) from the same number within a set amount of minutes
* Numbers found in messages
* and more..

If the app rejects calls from contacts on Android 10, allow contacts permission manually in 
`App info → Permissions`.


<p align="center">
<img 
    src="https://raw.githubusercontent.com/x13a/Silence/master/data/contacted.png" 
    height="400"> 
<img 
    src="https://raw.githubusercontent.com/x13a/Silence/master/data/groups.png" 
    height="400"> 
<img 
    src="https://raw.githubusercontent.com/x13a/Silence/master/data/repeated.png" 
    height="400"> 
<img 
    src="https://raw.githubusercontent.com/x13a/Silence/master/data/messages.png" 
    height="400">
</p>

## Permissions

* CALL_SCREENING - block or allow call
* READ_CALL_LOG - check you have called the number and count times the number have called you in T minutes
* READ_SMS - check you have sent a message to the number and you received a message from the number
* NOTIFICATION_LISTENER - find mobile numbers in incoming messages
* READ_PHONE_STATE - check on which SIM the number is calling
* RECEIVE_BOOT_COMPLETED - persist clean expired numbers job across reboots
* READ_CONTACTS - check the number exists in contacts on Android 10

## Localization

Is `Silence` not in your language, or the translation is incorrect or incomplete? Get involved on 
[Weblate](https://hosted.weblate.org/engage/me-lucky-silence/).

[![Translation status](https://hosted.weblate.org/widgets/me-lucky-silence/-/app/horizontal-auto.svg)](https://hosted.weblate.org/engage/me-lucky-silence/)

## License
[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)  

This application is Free Software: You can use, study share and improve it at your will. 
Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License v3](https://www.gnu.org/licenses/gpl.html) as published by the Free 
Software Foundation.
