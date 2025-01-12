<h1 align="center">Silence</h1>  
<h3 align="center">Block unknown callers</h3>  
<br/>

<p align="center">
<img 
    src="data/presentation.png">
</p>

<b>By default numbers not in your contacts are blocked.</b>

Optionally allow:
* Numbers you have contacted
* Numbers within the selected groups
* X registered call(s) from the same number within a set amount of minutes
* Numbers found in messages
* Numbers matching REGEX patterns
* and more..

Optionally block:
* Numbers matching REGEX patterns, including numbers in contacts

If the app rejects calls from contacts on Android 10, allow _Contacts_ permission manually in 
`App info → Permissions`.

<details>
<summary>More</summary>

<p align="center">
<img 
    src="data/contacted.png" 
    height="400"> 
<img 
    src="data/groups.png" 
    height="400"> 
<img 
    src="data/repeated.png" 
    height="400"> 
<img 
    src="data/messages.png" 
    height="400">
<img 
    src="data/regex.png" 
    height="400">     
<img 
    src="data/sim.png" 
    height="400"> 
<img 
    src="data/extra.png" 
    height="400"> 
</p>
</details>

## Permissions

* CALL_SCREENING - block or allow call
* READ_CALL_LOG - check you have called/answered the number and count times the number have called you in X minutes
* READ_SMS - check you have sent a message to the number and you received a message from the number
* NOTIFICATION_LISTENER - find mobile numbers in messages
* READ_PHONE_STATE - check on which SIM the number is calling
* RECEIVE_BOOT_COMPLETED - persist clean expired numbers job across reboots
* READ_CONTACTS - check the number exists in contacts on Android 10 or block calls from them

## Localization

[<img 
    src="https://hosted.weblate.org/widgets/me-lucky-silence/-/app/287x66-grey.png" 
    alt="Weblate">](https://hosted.weblate.org/engage/me-lucky-silence/)

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)
