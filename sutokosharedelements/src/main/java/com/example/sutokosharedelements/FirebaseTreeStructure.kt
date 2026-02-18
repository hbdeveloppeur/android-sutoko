package com.example.sutokosharedelements

object FirebaseTreeStructure {

    fun getUser(userId : String) : String {
        return "/users/$userId"
    }

    fun getUserProfilPicture500(userId : String) : String {
        return "/users/$userId/image_500.jpeg"
    }

    fun getUserProfilPicture64(userId : String) : String {
        return "/users/$userId/image_64.jpeg"
    }


    fun getUserProfilPicture32(userId : String) : String {
        return "/users/$userId/image_32.jpeg"
    }
}