This file list messages from our human testers - it's features or fixes to implement.
You will have to create a git branch if the current branch is not main for that work and switch on
it.
task - you can compact your context between each task.
If necessary you are allowed to do any commands like curls, install script, etc

Task 1: ConversationScreen - Writing a message and pressing send button does nothing visually,
unless we restart the app-the message must appears locally.
DONE (branch fix/conversation-send-message-not-appearing): root cause was in
ConversationRepositoryImpl.addMessage - the list contract is newest-first (index 0 = bottom,
LazyColumn reverseLayout), but the optimistic local message was appended at the END of the list
(= visual top, off-screen), so nothing appeared until restart re-fetched the server-ordered list.
Fix: prepend with id-dedupe. Also fixes live websocket replies landing at the top.
Unit tests added: ai-conversation/data .../repository/ConversationRepositoryImplTest.kt (passing).