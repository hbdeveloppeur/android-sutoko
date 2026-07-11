This file details the unwanted behavior found around the account feature of the app:

Unwanted behavior number 1: Coins and Diamonds display "-1" coins and "-1" diamond.
How to reproduce?

1. Disconnect
2. Restart the app.
3. Navigate to AccountScreen
4. Click on connect, then connect through AccountActivity - Connection successful, AccountActivity
   closes - back to the AccountScreen
   Result 1. Coins and Diamonds display "-1" coins and "-1" diamond in AccountScreen.
   Result 2. Quitting AccountScreen, no coins appears in HomeScreen.
   Until we restart the app, then it works.

Unwanted behavior number 2: When not connected, on TopNavigation displays an Account icon (
sutoko_ic_account) we want to replace it by a button R.string.sutoko_sign_in (the same as we have in
AccountScreen/NotConnectedView-don't forget to localize by using)