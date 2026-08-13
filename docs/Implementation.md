This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is main for that work and switch on
it.
Do not touch :games:*
Do never delete implementation.md.

You are an expert and smart UX/UI expert.
This operation is done because we need to save tokens for our AI models-the files are too long.

# [DONE] Fix: Work on AccountConnectionActivity- when a user connects and that their mails is not validated they get the message "Vérifiez que vous êtes connectés à Internet."

It's because the backend developer returns this when mail is not validated : """{
"result": false,
"error": "USER_NOT_VALIDATED",
"uid": null,
"token": null
}"""

Handle the case to display a better message about the mail validation.

-> Fixed on branch `fix/user-not-validated-message`: a `USER_NOT_VALIDATED` login response
(null token/uid) now shows the existing "validate your mail" page (`VALIDATE_MAIL_WAITING`,
"Nous avons envoyé un mail à … / Accédez au lien du mail pour valider votre compte") instead of
the misleading generic network error. The page polls mail validation, then sends the user back
to sign-in. See `AccountConnectionActivity.onLoginPressed` / `showValidateMailWaitingPage()`.
Validated with `./gradlew :auth:assembleDebug --no-build-cache` (green).
